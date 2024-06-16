import java.net.MalformedURLException;

// class with all the operations for the branch and bound

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BranchAndBound implements Runnable {
    int upperBound = Integer.MAX_VALUE;
    int currentDistance = 0;
    LowerBound lowerBound;
    
    Problem problem;

    List<Umpire> solutions;

    int checkedNodes = 0;

    public BranchAndBound(LowerBound lowerBound, Problem problem) {
        this.lowerBound = lowerBound;
        this.problem = problem;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();

        // Fix the first round
        for(int i = 0; i < Main.n; i++){
            Match match = problem.matches.get(i);
            Umpire umpire = problem.umpires.get(i);
            currentDistance += umpire.addToMatch(match);

            // Adjust feasible umpires for the next rounds
            problem.rounds.get(0).adjustSameRound(umpire, match);
            for(int j = 1; j < Main.q1; j++){
                Round round = problem.rounds.get(j);
                round.adjustFirstConstraint(umpire, match);
            }
            for(int j = 1; j < Main.q2; j++){
                Round round = problem.rounds.get(j);
                round.adjustSecondConstraint(umpire, match);
            }
        }

        branch(Main.n);

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("BRANCHNG: " + totalTime + "");
    }

    public int getTotalDistance() {
        return upperBound;
    }
    
    public void branch(int matchIndex) {
        Match match = problem.matches.get(matchIndex);
        Round round = problem.rounds.get(match.round);

        // FOR SOME REASON SORTING RESULTS IN DOUBLE THE VISITED NODES?
        // match.sortFeasibleUmpires();

        umpireLoop: for(Umpire u: match.feasibleUmpires){ 

            // Assign umpire to match
            int addDistance = u.addToMatch(match);
            currentDistance += addDistance;

            // Prune if current distance is already greater than upper bound
            // Partial matching
            int partialDistance = Main.minimalDistances[round.index][matchIndex % Main.n];
            if(currentDistance + partialDistance + lowerBound.lowerBounds[round.index][Main.nRounds - 1] >= upperBound) {
                currentDistance -= u.removeFromMatch();
                continue umpireLoop;
            }

            // TODO: even when a umpire can't do all rounds now, like if it's the last round and i still need to visit 2 more teams,
            // then we know that we must visit that team now and in the next round also, so just lock both of them in already
            int roundsLeft = Main.nRounds - (round.index + 1);
            int teamsNotVisited = u.getTeamsNotVisited();
            if(teamsNotVisited > roundsLeft){
                currentDistance -= u.removeFromMatch();
                continue umpireLoop;
            }

            checkedNodes++;

            // If not all matches are assigned umpires
            Set<Match> adjustedMatches = new HashSet<>();
            if(matchIndex < problem.matches.size()-1){

                // Check constraints
                if(!round.checkSameRound(u, match)){
                    currentDistance -= u.removeFromMatch();
                    continue umpireLoop;
                }
                for(int i = round.index + 1; i < problem.rounds.size() && i <= round.index + Main.q1 - 1; i++){
                    Round r = problem.rounds.get(i);
                    if(!r.checkFirstConstraint(u, match)){
                        currentDistance -= u.removeFromMatch();
                        continue umpireLoop;
                    }
                }
                for(int i = round.index + 1; i < problem.rounds.size() && i <= round.index + Main.q2 - 1; i++){
                    Round r = problem.rounds.get(i);
                    if(!r.checkSecondConstraint(u, match)){
                        currentDistance -= u.removeFromMatch();
                        continue umpireLoop;
                    }
                }

                // Commit changes
                // remove selected umpire form other matches feasible umpires
                adjustedMatches = round.adjustSameRound(u, match);
                for(int i = round.index + 1; i < problem.rounds.size() && i <= round.index + Main.q1 - 1; i++){
                    Round r = problem.rounds.get(i);
                    HashSet<Match> m = r.adjustFirstConstraint(u, match);
                    adjustedMatches.addAll(m);
                }
                for(int i = round.index + 1; i < problem.rounds.size() && i <= round.index + Main.q2 - 1; i++){
                    Round r = problem.rounds.get(i);
                    HashSet<Match> m = r.adjustSecondConstraint(u, match);
                    adjustedMatches.addAll(m);
                }

                // Branch and bound to next match
                branch(matchIndex+1);
                
            }  
            else{
                // Check if each umpire visited each team's home -> sum visitedTeams has to be size teams for each umpire
                for(Umpire umpire: problem.umpires){
                    if(!umpire.checkAllVisited()) {
                        currentDistance -= u.removeFromMatch();
                        continue umpireLoop;
                    }
                }

                // TODO Localsearch when we have a solution that is feasible and better, maybe in other thread?
                
                

                // Check if current distance is less than upper bound
                if(currentDistance < upperBound){
                    // Localsearch
                    


                    upperBound = currentDistance;

                    solutions = new ArrayList<>();
                    for(Umpire umpire: problem.umpires){
                        solutions.add(new Umpire(umpire));
                    }

                    LocalSearch localSearch = new LocalSearch(solutions, problem);
                    localSearch.search();
                }
            }
            
            // Rollback changes
            currentDistance -= u.removeFromMatch();
            for(Match m: adjustedMatches){
                m.addFeasibleUmpire(u);

            }
        }
    }

    public void feasibilityCheck(){
        // Generating matrix
        int[][] formattedSolution = new int[Main.nRounds][Main.n];
        for(Umpire u: solutions){
            for(Match m: u.matches){
                formattedSolution[m.round][m.index] = u.id;
            }
        }

        // Check if a umpire doesn't go to 2 or more matches at the same time
        for(int i = 0; i < Main.nRounds; i++){
            for(int j = 0; j < Main.n; j++){
                for(int k = j + 1; k < Main.n; k++){
                    if(formattedSolution[i][j] == formattedSolution[i][k]){
                        System.out.println("ERROR: Umpire " + formattedSolution[i][j] + " goes to match " + j + " and " + k + " at the same time");
                    }
                }
            }
        }

        // Check if a umpire visits every team's home once
        for(Umpire u : solutions) {
            int[] visitedTeams = new int[Main.nTeams];
            for(int i = 0; i < Main.nRounds; i++) {
                Match m = u.matches.get(i);
                visitedTeams[m.homeTeam.teamId] = 1;
            }

            for(int i = 0; i < Main.nTeams; i++) {
                if(visitedTeams[i] == 0) {
                    System.out.println("ERROR: Umpire " + u.id + " does not visit team " + i + "'s home");
                }
            }
        }
        
        // Check Q1 constraint, same location not visited
        for(Umpire u : solutions) {
            for(int i = 0; i < Main.nRounds; i++) {
                Match m1 = u.matches.get(i);

                for(int j = i + 1; j < Main.q1 - 1; j++) {
                    Match m2 = u.matches.get(j);

                    if(m1 == m2) continue;

                    if(m1.homeTeam.teamId == m2.homeTeam.teamId) {
                        System.out.println("ERROR: Umpire " + u.id + " visits same location in Q1 in round " + i);
                    }
                }
            }
        }

        // Check Q2 constraint, teams not visited 
        for(Umpire u : solutions) {
            for(int i = 0; i < Main.nRounds; i++) {
                Match m1 = u.matches.get(i);
                int homeTeam1 = m1.homeTeam.teamId;
                int outTeam1 = m1.outTeam.teamId;

                for(int j = i + 1; j < Main.q2 - 1; j++) {
                    Match m2 = u.matches.get(i);

                    if(m1 == m2) continue;

                    int homeTeam2 = m2.homeTeam.teamId;
                    int outTeam2 = m2.outTeam.teamId;

                    if(homeTeam1 == homeTeam2 || homeTeam1 == outTeam2 || outTeam1 == homeTeam2 || outTeam1 == outTeam2){
                        System.out.println("ERROR: Umpire " + u.id + " hosts same team in Q2 in round " + i);
                    }
                }
            }
        }

        // Create validator output
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Main.nRounds; i++) {
            for (int j = 0; j < Main.n; j++) {
                sb.append(formattedSolution[i][j] + 1);
                if (j < Main.n - 1) {
                    sb.append(",");
                }
            }
            if (i < Main.nRounds - 1) {
                sb.append(",");
            }
        }
        String result = sb.toString();

        // Create output.txt for validator
        try {
            java.io.FileWriter myWriter = new java.io.FileWriter("output.txt");
            myWriter.write(result);
            myWriter.close();
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }   

        // Run validator
        String command = "java -jar validator.jar Input/"+ Main.file +".txt "+ Main.q1+" "+ Main.q2 + " output.txt";
        try {
            executeCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void executeCommand(String command) throws IOException {
        Process process = Runtime.getRuntime().exec(command);

        // Read output
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("--------------------");
            System.out.println("Nodes: " + checkedNodes);
            System.out.println("Our distance: " + upperBound);
            System.out.println(line);
            System.out.println("--------------------");
        }
    }
}
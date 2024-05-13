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

    List<Round> rounds;
    List<Match> matches;
    List<Umpire> umpires;
    List<Team> teams;

    List<Umpire> solutions;

    int branchCounter = 0;

    public BranchAndBound(LowerBound lowerBound, List<Round> rounds, List<Match> matches, List<Umpire> umpires, List<Team> teams) {
        this.lowerBound = lowerBound;
  
        // Copy construct the rounds, matches, umpires and teams
        this.rounds = rounds;
        this.matches = matches;
        this.umpires = umpires;
        this.teams = teams;
    }

    @Override
    public void run() {
        branch(0);
    }

    public int getTotalDistance() {
        return upperBound;
    }
    

    // TODO aanpassen dat result gereturned wordt
    public void branch(int matchIndex) {
        Match match = matches.get(matchIndex);
        Round round = rounds.get(match.round);

        umpireLoop: for(Umpire u: match.feasibleUmpires){ 
            branchCounter++;
            // Assign umpire to match
            currentDistance += u.addToMatch(match);

            // Prune if current distance is already greater than upper bound
            if(currentDistance + lowerBound.lowerBounds[round.index][Main.nRounds - 1] >= upperBound) {
                currentDistance -= u.removeFromMatch();
                continue umpireLoop;
            }

            // If not all matches are assigned umpires
            Set<Match> adjustedMatches = new HashSet<>();
            if(matchIndex < matches.size()-1){
                // Check constraints
                if(!round.checkSameRound(u, match)){
                    currentDistance -= u.removeFromMatch();
                    continue umpireLoop;
                }
                for(int i = round.index + 1; i < rounds.size() && i <= round.index + Main.q1 - 1; i++){
                    Round r = rounds.get(i);
                    if(!r.checkFirstConstraint(u, match)){
                        currentDistance -= u.removeFromMatch();
                        continue umpireLoop;
                    }
                }
                for(int i = round.index + 1; i < rounds.size() && i <= round.index + Main.q2 - 1; i++){
                    Round r = rounds.get(i);
                    if(!r.checkSecondConstraint(u, match)){
                        currentDistance -= u.removeFromMatch();
                        continue umpireLoop;
                    }
                }

                // TODO if we assign umpire but visited teams total is smaller then rounds left, abord early

                // Commit changes
                // remove selected umpire form other matches feasible umpires
                adjustedMatches = round.adjustSameRound(u, match);
                for(int i = round.index + 1; i < rounds.size() && i <= round.index + Main.q1 - 1; i++){
                    Round r = rounds.get(i);
                    HashSet<Match> m = r.adjustFirstConstraint(u, match);
                    adjustedMatches.addAll(m);
                }
                for(int i = round.index + 1; i < rounds.size() && i <= round.index + Main.q2 - 1; i++){
                    Round r = rounds.get(i);
                    HashSet<Match> m = r.adjustSecondConstraint(u, match);
                    adjustedMatches.addAll(m);
                }

                // Branch and bound to next match
                branch(matchIndex+1); 
            }  
            else{
                // Check if each umpire visited each team's home -> sum visitedTeams has to be size teams for each umpire
                for(Umpire umpire: umpires){
                    if(!umpire.checkAllVisited()) {
                        currentDistance -= u.removeFromMatch();
                        continue umpireLoop;
                    }
                }
                

                // Check if current distance is less than upper bound
                if (currentDistance < upperBound){
                    upperBound = currentDistance;

                    solutions = new ArrayList<>();
                    for(Umpire umpire: umpires){
                        solutions.add(new Umpire(umpire));
                    }
                }
            }
            
            // Rollback changes
            currentDistance -= u.removeFromMatch();
            for(Match m: adjustedMatches){
                m.addUmpire(u);
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

        // Print the formattedSolution
        // for(int i = 0; i < Main.nRounds; i++){
        //     for(int j = 0; j < Main.n; j++){
        //         System.out.print(formattedSolution[i][j] + " ");
        //     }
        //     System.out.println();
        // }


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
                        System.out.println("ERROR: Umpire " + u.id + " visits same location in Q1 in round");
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
                        System.out.println("ERROR: Umpire " + u.id + " hosts same team in Q2");
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
            System.out.println("Our distance: " + upperBound);
            System.out.println(line);
            System.out.println("--------------------");
        }
    }
}
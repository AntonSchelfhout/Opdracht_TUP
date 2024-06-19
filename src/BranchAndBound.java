import java.net.MalformedURLException;

// class with all the operations for the branch and bound

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BranchAndBound {
    int currentDistance = 0;
    LowerBound lowerBound;
    
    Problem problem;

    List<Umpire> solutions;

    long checkedNodes = 0;

    public BranchAndBound(LowerBound lowerBound, Problem problem) {
        this.lowerBound = lowerBound;
        this.problem = problem;
    }

    public long startBranching(int u) {
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

        // Fix the u'th umpire in the second round
        Match match = problem.matches.get(Main.n);
        Umpire umpire = problem.umpires.get(u);

        if(!match.feasibleUmpires.contains(umpire)){
            System.out.println("ABORD");
            return 0;
        }

        currentDistance += umpire.addToMatch(match);
        problem.rounds.get(1).adjustSameRound(umpire, match);
        for(int j = 2; j < Main.q1; j++){
            Round round = problem.rounds.get(j);
            round.adjustFirstConstraint(umpire, match);
        }
        for(int j = 2; j < Main.q2; j++){
            Round round = problem.rounds.get(j);
            round.adjustSecondConstraint(umpire, match);
        }


        branch(Main.n + 1);

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("BRANCHNG: " + totalTime + "");

        return checkedNodes;
    }
    
    public void branch(int matchIndex) {
        Match match = problem.matches.get(matchIndex);
        Round round = problem.rounds.get(match.round);

        // Sorting feasible umpires on distance from last match
        match.sortFeasibleUmpires();

        umpireLoop: for(Umpire u: match.feasibleUmpires){ 

            // Assign umpire to match
            int addDistance = u.addToMatch(match);
            currentDistance += addDistance;

            // Prune if current distance is already greater than upper bound
            // Partial matching
            int partialDistance = Main.minimalDistances[round.index][matchIndex % Main.n];
            if(currentDistance + partialDistance + Main.lowerBounds[round.index][Main.nRounds - 1] >= Main.upperBound) {
                currentDistance -= u.removeFromMatch();
                continue umpireLoop;
            }

            // Prune if we can't visit all rounds anymore
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

                // Check if current distance is less than upper bound
                synchronized(this) {
                    if(currentDistance < Main.upperBound){
                        Main.upperBound = currentDistance;
    
                        Main.solutions = new ArrayList<>();
                        for(Umpire umpire: problem.umpires){
                            Main.solutions.add(new Umpire(umpire));
                        }
                        Main.problem = problem;
                    }
                }
            }
            
            // Rollback changes
            currentDistance -= u.removeFromMatch();
            for(Match m: adjustedMatches){
                m.addFeasibleUmpire(u);

            }
        }
    }
}
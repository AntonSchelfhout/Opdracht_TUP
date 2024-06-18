// class with all the operations for the branch and bound

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FastBranchAndBound implements Runnable {
    int upperBound = Integer.MAX_VALUE;
    int currentDistance = 0;

    int startRound = 0;
    int endRound = 0;
    int numOfRounds = 0;

    LowerBound lowerBound;
   
    Problem problem;

    public FastBranchAndBound(LowerBound lowerBound, int startRound, int endRound, Problem problem) {    
        this.lowerBound = lowerBound;
        this.startRound = startRound;
        this.endRound = endRound;
        this.numOfRounds = endRound - startRound;

        this.problem = problem;
    }

    @Override
    public void run() {
        // Fix the first round
        for(int i = 0; i < Main.n; i++){
            Match match = problem.matches.get(i);
            Umpire umpire = problem.umpires.get(i);
            currentDistance += umpire.addToMatch(match);

            // Adjust feasible umpires for the next rounds
            problem.rounds.get(0).adjustSameRound(umpire, match);
            for(int j = 1; j < Main.q1 && j < problem.rounds.size(); j++){
                Round round = problem.rounds.get(j);
                round.adjustFirstConstraint(umpire, match);
            }
            for(int j = 1; j < Main.q2 && j < problem.rounds.size(); j++){
                Round round = problem.rounds.get(j);
                round.adjustSecondConstraint(umpire, match);
            }
        }

        branch(Main.n);
    }


    public int getTotalDistance() {
        return upperBound;
    }
    

    public void branch(int matchIndex) {
        Match match = problem.matches.get(matchIndex);
        Round round = problem.rounds.get(match.round - startRound);

        // Don't activate, Enabling this sort umpires from the closest to the farthest, but has delay so not usefull in lowerbounds
        //match.sortFeasibleUmpires();
    
        umpireLoop: for(Umpire u: match.feasibleUmpires) { 

            // Assign umpire to match
            currentDistance += u.addToMatch(match);

            // Prune if current distance is already greater than upper bound
            int partialDistance = round.getPartialDistance(match);
            if(currentDistance + partialDistance + lowerBound.lowerBounds[round.index][endRound] >= upperBound) {  
                currentDistance -= u.removeFromMatch();
                continue umpireLoop;
            }

            // Don't activate, If numOfRounds is more than half of the total rounds, check if all teams are visited, delay not usefull 
            // if(numOfRounds > (Main.nRounds - Main.nTeams)) {
            //     int teamsNeedToVisit = Main.nRounds - numOfRounds;
            //     int roundsLeft = endRound - (round.index + 1);
            //     int teamsNotVisited = u.getTeamsNotVisited();
            //     if(teamsNotVisited - teamsNeedToVisit > roundsLeft){
            //         currentDistance -= u.removeFromMatch();
            //         continue umpireLoop;
            //     }
            // }

            // If not all matches are assigned umpires
            Set<Match> adjustedMatches = new HashSet<>();
            if(matchIndex < problem.matches.size() - 1) {
                
                // === CHECKS === 
                // Check same round
                if(!round.checkSameRound(u, match)){
                    currentDistance -= u.removeFromMatch();
                    continue umpireLoop;
                }
                
                // Check Q1
                for(int i = round.index - startRound + 1; i < problem.rounds.size() && i <= round.index - startRound + Main.q1 - 1; i++){
                    Round r = problem.rounds.get(i);
                    if(!r.checkFirstConstraint(u, match)){
                        currentDistance -= u.removeFromMatch();
                        continue umpireLoop;
                    }
                }

                // Check Q2
                for(int i = round.index - startRound + 1; i < problem.rounds.size() && i <= round.index - startRound + Main.q2 - 1; i++){
                    Round r = problem.rounds.get(i);
                    if(!r.checkSecondConstraint(u, match)){
                        currentDistance -= u.removeFromMatch();
                        continue umpireLoop;
                    }
                }

                // === COMMIT CHANGES === 
                // Remove selected umpire form other matches feasible umpires
                adjustedMatches = round.adjustSameRound(u, match);

                // Adjust the fesaible umpires for the next Q1 rounds
                for(int i = round.index - startRound + 1; i < problem.rounds.size() && i <= round.index - startRound + Main.q1 - 1; i++){
                    Round r = problem.rounds.get(i);
                    HashSet<Match> m = r.adjustFirstConstraint(u, match);
                    adjustedMatches.addAll(m);
                }
                
                // Adjust the fesaible umpires for the next Q2 rounds
                for(int i = round.index - startRound + 1; i < problem.rounds.size() && i <= round.index - startRound + Main.q2 - 1; i++){
                    Round r = problem.rounds.get(i);
                    HashSet<Match> m = r.adjustSecondConstraint(u, match);
                    adjustedMatches.addAll(m);
                }
                
                branch(matchIndex+1); 
            }  
            else {
                // Check if current distance is less than upper bound
                if (currentDistance < upperBound) {
                    upperBound = currentDistance;
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
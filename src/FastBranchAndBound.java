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
   
    List<Round> rounds = new ArrayList<>();
    List<Match> matches = new ArrayList<>();
    List<Umpire> umpires = new ArrayList<>();
    List<Team> teams = new ArrayList<>();

    public FastBranchAndBound(LowerBound lowerBound, int startRound, int endRound, List<Round> rounds, List<Match> matches, List<Umpire> umpires, List<Team> teams) {    
        this.lowerBound = lowerBound;
        this.startRound = startRound;
        this.endRound = endRound;
        this.numOfRounds = endRound - startRound;

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
    

    public void branch(int matchIndex) {
        Match match = matches.get(matchIndex);
        Round round = rounds.get(match.round - startRound);

        match.sortFeasibleUmpires();
        
        umpireLoop: for(Umpire u: match.feasibleUmpires) { 

            // Assign umpire to match
            currentDistance += u.addToMatch(match);
            
            // TODO Partial matching (also add minimum distance for all remaining matches and umpires in this round)
            // Prune if current distance is already greater than upper bound
            if(currentDistance + lowerBound.lowerBounds[round.index][endRound] >= upperBound) {  
                currentDistance -= u.removeFromMatch();
                continue umpireLoop;
            }

            // If not all matches are assigned umpires
            Set<Match> adjustedMatches = new HashSet<>();
            if(matchIndex < matches.size() - 1) {
                
                // === CHECKS === 
                // Check same round
                if(!round.checkSameRound(u, match)){
                    currentDistance -= u.removeFromMatch();
                    continue umpireLoop;
                }
                
                // Check Q1
                for(int i = round.index - startRound + 1; i < rounds.size() && i <= round.index - startRound + Main.q1 - 1; i++){
                    Round r = rounds.get(i);
                    if(!r.checkFirstConstraint(u, match)){
                        currentDistance -= u.removeFromMatch();
                        continue umpireLoop;
                    }
                }

                // Check Q2
                for(int i = round.index - startRound + 1; i < rounds.size() && i <= round.index - startRound + Main.q2 - 1; i++){
                    Round r = rounds.get(i);
                    if(!r.checkSecondConstraint(u, match)){
                        currentDistance -= u.removeFromMatch();
                        continue umpireLoop;
                    }
                }

                // === COMMIT CHANGES === 
                // Remove selected umpire form other matches feasible umpires
                adjustedMatches = round.adjustSameRound(u, match);

                // Adjust the fesaible umpires for the next Q1 rounds
                for(int i = round.index - startRound + 1; i < rounds.size() && i <= round.index - startRound + Main.q1 - 1; i++){
                    Round r = rounds.get(i);
                    HashSet<Match> m = r.adjustFirstConstraint(u, match);
                    adjustedMatches.addAll(m);
                }
                
                // Adjust the fesaible umpires for the next Q2 rounds
                for(int i = round.index - startRound + 1; i < rounds.size() && i <= round.index - startRound + Main.q2 - 1; i++){
                    Round r = rounds.get(i);
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
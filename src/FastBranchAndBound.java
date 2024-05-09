// class with all the operations for the branch and bound

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FastBranchAndBound implements Runnable {       // misleading name, this is slow
    int upperBound = Integer.MAX_VALUE;
    int currentDistance = 0;
    int startRound = 0;

    LowerBound lowerBound;
   
    List<Round> rounds = new ArrayList<>();
    List<Match> matches = new ArrayList<>();
    List<Umpire> umpires = new ArrayList<>();
    List<Team> teams = new ArrayList<>();

    public FastBranchAndBound(LowerBound lowerBound, int startRound, List<Round> rounds, List<Match> matches, List<Umpire> umpires, List<Team> teams) {    
        this.lowerBound = lowerBound;
        this.startRound = startRound;

        // Copy construct the rounds, matches, umpires and teams
        this.rounds = rounds;
        this.matches = matches;
        this.umpires = umpires;
        this.teams = teams;
    }

    @Override
    public void run() {
        branch(0, startRound);
    }

    public int getTotalDistance() {
        return upperBound;
    }
    

    public void branch(int umpireIndex, int roundIndex) {
        Round currentRound = rounds.get(roundIndex);
        Umpire currentUmpire = umpires.get(umpireIndex);
        List<Match> feasibleMatches = currentUmpire.feasibleMatches.get(roundIndex);

        umpireLoop: for(Match currenMatch: feasibleMatches){ 
            if (currenMatch.isAssigned) continue; 

            // Assign umpire to match
            currentDistance += currentUmpire.addToMatch(currenMatch);

            // Prune if current distance is already greater than upper bound
            if(currentDistance + lowerBound.lowerBounds[roundIndex][Main.nRounds - 1] >= upperBound) {
                currentDistance -= currentUmpire.removeFromMatch();
                continue umpireLoop;
            }

            // If not all matches are assigned umpires
            if(roundIndex < Main.nRounds - 1 || umpireIndex < Main.n - 1) {

                // Branch and bound to next umpire
                if(umpireIndex == Main.n - 1){
                    branch(0, roundIndex + 1);
                }
                else{
                    branch(umpireIndex + 1, roundIndex); 
                }

            }  
            else{
                // Check if current distance is less than upper bound
                if (currentDistance < upperBound){
                    upperBound = currentDistance;
                }
            }
            
            // Rollback changes
            currentDistance -= currentUmpire.removeFromMatch();
        }
    }
}
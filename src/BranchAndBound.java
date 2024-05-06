// class with all the operations for the branch and bound

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BranchAndBound implements Runnable {
    int q1;
    int q2;
    int upperBound = Integer.MAX_VALUE;
    int currentDistance = 0;
    LowerBound lowerBound;

    int startRound;
       
    List<Round> rounds = new ArrayList<>();
    List<Match> matches = new ArrayList<>();
    List<Umpire> umpires = new ArrayList<>();
    List<Team> teams = new ArrayList<>();

    public BranchAndBound(int q1, int q2, LowerBound lowerBound, List<Round> rounds, List<Match> matches, List<Umpire> umpires, List<Team> teams, int startRound) {
        this.q1 = q1;
        this.q2 = q2;
        this.lowerBound = lowerBound;
        this.startRound = startRound;
        
        // Copy the rounds, matches, umpires and teams
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
        Round round = rounds.get(match.round - startRound);

        umpireLoop: for(Umpire u: match.feasibleUmpires){ 

            // Assign umpire to match
            match.umpire = u;
            currentDistance += u.addToMatch(match);

            // Prune if current distance is already greater than upper bound
            if (currentDistance + lowerBound.lowerBounds[round.index][Main.nRounds - 1] >= upperBound) {
                match.umpire = null;
                currentDistance -= u.removeFromMatch();
                continue umpireLoop;
            }

            // If not all matches are assigned umpires
            Set<Match> adjustedMatches = new HashSet<>();
            if(matchIndex < matches.size()-1){
                // Check constraints
                if(!round.checkSameRound(u, match)){
                    match.umpire = null;
                    currentDistance -= u.removeFromMatch();
                    continue umpireLoop;
                }
                for(int i = round.index + 1; i < round.index + q1 - 1; i++){
                    if(i >= rounds.size()) break;

                    Round r = rounds.get(i);
                    if(!r.checkFirstConstraint(u, match)){
                        match.umpire = null;
                        currentDistance -= u.removeFromMatch();
                        continue umpireLoop;
                    }
                }
                for(int i = round.index + 1; i < round.index + q2 - 1; i++){
                    if(i >= rounds.size()) break;

                    Round r = rounds.get(i);
                    if(!r.checkSecondConstraint(u, match)){
                        match.umpire = null;
                        currentDistance -= u.removeFromMatch();
                        continue umpireLoop;
                    }
                }

                // Commit changes
                // remove selected umpire form other matches
                adjustedMatches = round.adjustSameRound(u, match);
                for(int i = round.index + 1; i <= round.index + q1 - 1; i++){
                    if(i >= rounds.size()) break;

                    Round r = rounds.get(i);
                    HashSet<Match> m = r.adjustFirstConstraint(u, match);
                    adjustedMatches.addAll(m);
                }
                for(int i = round.index + 1; i <= round.index + q2 - 1; i++){
                    if(i >= rounds.size()) break;

                    Round r = rounds.get(i);
                    HashSet<Match> m = r.adjustSecondConstraint(u, match);
                    adjustedMatches.addAll(m);
                }

                // Branch and bound to next match
                branch(matchIndex+1); 
            }  
            else{
                // If all matches have assigned umpires

                // Check if each umpire visited each team's home -> sum visitedTeams has to be size teams for each umpire
                for(Umpire umpire: umpires){
                    if(!umpire.checkAllVisited()) {
                        match.umpire = null;
                        currentDistance -= u.removeFromMatch();
                        continue umpireLoop;
                    }
                }

                // Check if current distance is less than upper bound
                if (currentDistance < upperBound){
                    upperBound = currentDistance;
                    System.out.println("--------------------");
                    System.out.println(currentDistance);
                    System.out.println("--------------------");
                }
            }
            
            // Rollback changes
            match.umpire = null;
            currentDistance -= u.removeFromMatch();
            for(Match m: adjustedMatches){
                m.addUmpire(u);
            }
        }
    }
}
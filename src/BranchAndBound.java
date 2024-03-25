// class with all the operations for the branch and bound

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BranchAndBound {
    int upperBound = Integer.MAX_VALUE;
    static int currentDistance = 0;

    public boolean branchAndBound(int matchIndex) {
        Match match = Main.matches.get(matchIndex);
        Round round = Main.rounds.get(match.round);

        umpireLoop: for(Umpire u: match.feasibleUmpires){ 

            // Assign umpire to match
            match.umpire = u;

            // Check constraints
            for(int i = round.index; i < round.index + Main.q1; i++){
                Round r = Main.rounds.get(i);
                if(!r.checkFirstConstraint(u, match)){
                    continue umpireLoop;
                }
            }
            for(int i = round.index; i < round.index + Main.q2; i++){
                Round r = Main.rounds.get(i);
                if(!r.checkSecondConstraint(u, match)){
                    continue umpireLoop;
                }
            }

            // Completed
            if(matchIndex == Main.matches.size()-1){
                return true;
            } 

            // Commit changes
            // TODO change same round also
            Set<Match> adjustedMatches = new HashSet<>();
            for(int i = round.index + 1; i <= round.index + Main.q1; i++){
                Round r = Main.rounds.get(i);
                adjustedMatches.addAll(r.adjustFirstConstraint(u, match));
            }
            for(int i = round.index + 1; i <= round.index + Main.q2; i++){
                Round r = Main.rounds.get(i);
                adjustedMatches.addAll(r.adjustSecondConstraint(u, match));
            }

            // Branch and bound to next match
            boolean res = branchAndBound(matchIndex+1);  
            if(res) return true;
            
            // Rollback changes
            match.umpire = null;
            for(Match m: adjustedMatches){
                m.addUmpire(u);
            }
        }

        return false;
    }
}

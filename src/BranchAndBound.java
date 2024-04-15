// class with all the operations for the branch and bound

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BranchAndBound {
    int upperBound = Integer.MAX_VALUE;
    int currentDistance = 0;

    public boolean branchAndBound(int matchIndex) {
        Match match = Main.matches.get(matchIndex);
        Round round = Main.rounds.get(match.round);

        umpireLoop: for(Umpire u: match.feasibleUmpires){ 

            // Assign umpire to match
            match.umpire = u;
            currentDistance += u.addToMatch(match);

            // Completed
            if(matchIndex == Main.matches.size()-1){
                return true;
            } 
            
            // TODO Every umpire crew should visit the home of every teamat least once
            // Check constraints
            if(!round.checkSameRound(u, match)){
                match.umpire = null;
                currentDistance -= u.removeFromMatch();
                continue umpireLoop;
            }
            for(int i = round.index + 1; i < round.index + Main.q1 - 1; i++){
                if(i >= Main.rounds.size()) break;

                Round r = Main.rounds.get(i);
                if(!r.checkFirstConstraint(u, match)){
                    match.umpire = null;
                    currentDistance -= u.removeFromMatch();
                    continue umpireLoop;
                }
            }
            for(int i = round.index + 1; i < round.index + Main.q2 - 1; i++){
                if(i >= Main.rounds.size()) break;

                Round r = Main.rounds.get(i);
                if(!r.checkSecondConstraint(u, match)){
                    match.umpire = null;
                    currentDistance -= u.removeFromMatch();
                    continue umpireLoop;
                }
            }

            // Commit changes
            Set<Match> adjustedMatches = round.adjustSameRound(u, match);
            for(int i = round.index + 1; i <= round.index + Main.q1 - 1; i++){
                if(i >= Main.rounds.size()) break;

                Round r = Main.rounds.get(i);
                HashSet<Match> m = r.adjustFirstConstraint(u, match);
                adjustedMatches.addAll(m);
            }
            for(int i = round.index + 1; i <= round.index + Main.q2 - 1; i++){
                if(i >= Main.rounds.size()) break;

                Round r = Main.rounds.get(i);
                HashSet<Match> m = r.adjustSecondConstraint(u, match);
                adjustedMatches.addAll(m);
            }

           // Main.printOutput();

            // Branch and bound to next match
            boolean res = branchAndBound(matchIndex+1);  
            if(res){
                return true;
            }

            // Rollback changes
            match.umpire = null;
            currentDistance -= u.removeFromMatch();
            for(Match m: adjustedMatches){
                m.addUmpire(u);
            }
        }

        return false;
    }
}

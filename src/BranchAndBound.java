// class with all the operations for the branch and bound

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BranchAndBound {
    int upperBound = Integer.MAX_VALUE;
    int currentDistance = 0;

    public void branchAndBound(int matchIndex) {
        Match match = Main.matches.get(matchIndex);
        Round round = Main.rounds.get(match.round);

        umpireLoop: for(Umpire u: match.feasibleUmpires){ 

            // Assign umpire to match
            match.umpire = u;
            currentDistance += u.addToMatch(match);

            // Prune if current distance is already greater than upper bound
            if (currentDistance >= upperBound) {
                match.umpire = null;
                currentDistance -= u.removeFromMatch();
                continue umpireLoop;
            }

            // If not all matches are assigned umpires
            Set<Match> adjustedMatches = new HashSet<>();
            if(matchIndex < Main.matches.size()-1){
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
                // remove selected umpire form other matches
                adjustedMatches = round.adjustSameRound(u, match);
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

                // Branch and bound to next match
                branchAndBound(matchIndex+1); 
            }  
            else{
                // If all matches have assigned umpires

                // Check if each umpire visited each team's home
                // for(Umpire umpire: Main.umpires){
                //     List<Team> tempTeams = Main.teams;
                //     for(Match m: u.matches){
                //         if (tempTeams.contains(m.homeTeam)){
                //             tempTeams.remove(m.homeTeam);
                //         }
                //     }
                //     if(tempTeams.size() > 0){
                    //     match.umpire = null;
                    //     currentDistance -= u.removeFromMatch();
                    //     continue umpireLoop;
                //         continue umpireLoop;
                //     }
                // }

                // Check if current distance is less than upper bound
                if (currentDistance < upperBound){
                    upperBound = currentDistance;
                    Main.printOutput(this);
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

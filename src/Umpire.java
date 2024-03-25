import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Umpire {
    public int id;
    public ArrayList<Match> assignedMatches;    // index = round, element on index = assigned match for that round
    public ArrayList<Match> feasibleMatches;
    public ArrayList<Match[]> historyFeasibleMatches;

    public Umpire(int id) {
        this.id = id;
        assignedMatches = new ArrayList<>(8);
        feasibleMatches = new ArrayList<>();
        historyFeasibleMatches = new ArrayList<>(Main.nRounds);
    }

    public boolean isFeasibleC1(Match m) {

        return false;
    }

    public boolean isFeasibleC2(Match m) {
        // in the beginning we can always allow
        if (m.round-Main.q2 <= 0) return true;

        // if m contains the same teams as the previously assigned matches, don't allow
        for (int i = m.round; i < m.round - Main.q2; i--) {
            if (assignedMatches.get(i).containsTeamsOfRound(m)) {
                return false;
            }
        }
        return false;
    }

    // Assign a match to this umpire
    public void assignMatch(Match m) {
        assignedMatches.add(m);
        BranchAndBound.currentDistance += Main.dist[m.homeTeam.teamId][m.outTeam.teamId];
    }

    public void unAssignMatch(Match m) {
        assignedMatches.remove(m);
        BranchAndBound.currentDistance -= Main.dist[m.homeTeam.teamId][m.outTeam.teamId];
    }


    public void getFeasibleMatches(int round){
        for(int i = round; i < Main.nRounds; i++) {
            List<Match> matchRound = Main.matches.get(i);
            for(Match m : matchRound) {

            }
        }
    }

    // Add current feasiblematches to history datastructure for fast backtracking
    public void addFeasibleMatchesToHistory(int round) {
        Match[] history = (Match[]) feasibleMatches.toArray();
        historyFeasibleMatches.add(round, history);
    }

    @Override
    public String toString() {
        //for (int i = 0; i < assignedMatches.size(); i++)


        return "Umpire{" +
                "id=" + id +
                ", assignedMatches=" + assignedMatches +
                ", feasibleMatches=" + feasibleMatches +
                ", historyFeasibleMatches=" + historyFeasibleMatches +
                '}';
    }
}
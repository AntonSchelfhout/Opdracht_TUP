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

    // Homelocatie
    public boolean isFeasibleC1(Match m) {
        if (m.round-Main.q1 < 0) return true;

        // if m contains the same teams as the previously assigned matches, don't allow
        for (int i = m.round-1; i > m.round - Main.q1; i--) {
            if (assignedMatches.get(i).homeTeam == m.homeTeam) {
                return false;
            }
        }
        return true;
    }


    public boolean isFeasibleC1_2(Match m) {
        int index = m.round - 1;

        while (index >= 0 && index >= m.round - Main.q1) {
            if (assignedMatches.get(index).homeTeam == m.homeTeam) {
                return false;
            }
            index--;
        }
        return true;
    }




    // Teams
    public boolean isFeasibleC2(Match m) {
        // in the beginning we can always allow
        if (m.round-Main.q2 < 0) return true;

        // if m contains the same teams as the previously assigned matches, don't allow
        for (int i = m.round-1; i > m.round - Main.q2; i--) {
            if (assignedMatches.get(i).containsTeamsOfRound(m)) {
                return false;
            }
        }
        return true;
    }

    // Assign a match to this umpire
    public void assignMatch(Match m) {
        assignedMatches.add(m);
        m.isAsigned = true;
        BranchAndBound.currentDistance += Main.dist[m.homeTeam.teamId][m.outTeam.teamId];
    }

    public void unAssignMatch(Match m) {
        assignedMatches.remove(m);
        m.isAsigned = false;
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

    public String assignedMatchesToString() {


        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < assignedMatches.size(); i++) {
            sb.append("Round ").append(i).append(": ").append(assignedMatches.get(i).toString()).append(" \n");
        }
        sb.append("\n");
        return sb.toString();
    }
}
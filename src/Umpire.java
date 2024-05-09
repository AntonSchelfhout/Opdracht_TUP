import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Umpire {
    public int id;

    public List<Match> matches = new ArrayList<>();
    public ArrayList<List<Match>> feasibleMatches = new ArrayList<>();
    public int[] visitedTeams = new int[Main.nTeams];

    public Umpire(int id) {
        this.id = id;
    }

    public Umpire(Umpire other) {
        this.id = other.id;
        this.matches = new ArrayList<>(other.matches);
        this.visitedTeams = other.visitedTeams.clone();
    }

    public int addToMatch(Match m) {
        m.isAssigned = true;
        visitedTeams[m.homeTeam.teamId]++;
        matches.add(m);
        // if (m.round != 13) System.out.println("Added match: " + m.index + " to umpire: " + this.id + " in round: " + m.round);

        if(matches.size() == 1){
            return 0;
        }
        
        Match prevMatch = matches.get(matches.size() - 2);
        return Main.dist[prevMatch.homeTeam.teamId][m.homeTeam.teamId];
    }

    public int removeFromMatch(){
        Match m = matches.get(matches.size() - 1);
        m.isAssigned = false;
        visitedTeams[m.homeTeam.teamId]--;
        Match removedMatch = matches.remove(matches.size() - 1);

        // if (m.round != 13) System.out.println("    Removed match: " + m.index + " from umpire: " + this.id + " in round: " + m.round);

        if(matches.size() == 0){
            return 0;
        }

        Match prevMatch = matches.getLast();
        return Main.dist[prevMatch.homeTeam.teamId][removedMatch.homeTeam.teamId];
    }

    public boolean checkAllVisited(){
        // Check if visitedTeams contains Main.nTeams diffrent teams
        int sum = 0;
        for(int i : visitedTeams){
            if(i > 0){
                sum++;
            }
        }
        return sum == Main.nTeams;
    }

    public boolean canStillVisitAllTeams(Round round){
        int roundToGo = Main.nRounds - round.index - 1;
        int teamsToVisit = Main.nTeams;

        for(int i : visitedTeams){
            if(i > 0){
                teamsToVisit--;
            }
        }

        return teamsToVisit <= roundToGo;
    }

    public void fillFeasibleMatches(int round){
        List<Match> matchesForRound = new ArrayList<>();
        for(Match m : Main.matches){
            if (m.round == round){
                matchesForRound.add(m);
            }
        }
        feasibleMatches.add(round, matchesForRound);
    }

    public boolean removeFeasibleMatch(int round, Match m){
        feasibleMatches.get(round).remove(m);
        return feasibleMatches.get(round).size() == 0;
    }

    @Override
    public String toString() {
        return "Umpire{" +
                "id=" + id +
                '}';
    }
}
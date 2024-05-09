import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Umpire {
    public int id;

    public List<Match> matches = new ArrayList<>();
    public List<Match> feasibleMatches = new ArrayList<>();
    public int[] visitedTeams = new int[Main.nTeams];

    public Umpire(int id) {
        this.id = id;
    }

    public Umpire(Umpire other) {
        this.id = other.id;
        this.matches = new ArrayList<>(other.matches);
        this.visitedTeams = other.visitedTeams.clone();
    }

    public int addToMatch(Match m){
        Match prevMatch = matches.getLast();
        visitedTeams[m.homeTeam.teamId]++;
        matches.add(m);

        if(matches.size() == 1){
            return 0;
        }
        
        return Main.dist[prevMatch.homeTeam.teamId][m.homeTeam.teamId];
    }

    public int removeFromMatch(){
        Match m = matches.getLast();
        visitedTeams[m.homeTeam.teamId]--;
        Match removedMatch = matches.removeLast();

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

    @Override
    public String toString() {
        return "Umpire{" +
                "id=" + id +
                '}';
    }
}
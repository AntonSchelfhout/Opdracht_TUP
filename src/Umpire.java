import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Umpire{
    public int id;
    public List<Match> matches = new ArrayList<>();
    public int[] visitedTeams = new int[Main.nTeams];

    public Umpire(int id) {
        this.id = id;
        this.visitedTeams = new int[Main.nTeams];
    }

    public Umpire(Umpire other) {
        this.id = other.id;
        this.matches = new ArrayList<>(other.matches);
        this.visitedTeams = other.visitedTeams.clone();
    }

    public int addToMatch(Match m){
        visitedTeams[m.homeTeam.teamId]++;
        matches.add(m);

        if(matches.size() == 1){
            return 0;
        }

        Match prevMatch = matches.get(matches.size() - 2);
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

    // for branch and bound
    public boolean checkAllVisited(){
        // Check if visitedTeams contains Main.nTeams diffrent teams
        int sum = 0;
        for(int i = 0; i < Main.nTeams; i++){
            if(visitedTeams[i] > 0){
                sum++;
            }
        }
        return sum == Main.nTeams;
    }

    // for fast branch and bound
    public int getTeamsNotVisited(){
        // Check if it is possible to visit all teams in the remaining rounds
        int notVisitedTeams = 0;
        for(int i = 0; i < Main.nTeams; i++){
            if(visitedTeams[i] <= 0){
                notVisitedTeams++;
            }
        }

        return notVisitedTeams;
    }

    @Override
    public String toString() {
        return "Umpire{" +
                "id=" + id +
                '}';
    }

}
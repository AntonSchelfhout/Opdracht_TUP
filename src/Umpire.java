import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Umpire {
    public int id;
    public List<Match> matches = new ArrayList<>();
    // Array conatining 1 if team has been visited by umpire, 0 otherwise
    public Integer[] visitedTeams = new Integer[Main.teams.size()];

    public Umpire(int id) {
        this.id = id;
        for(int i = 0; i < visitedTeams.length; i++){
            visitedTeams[i] = 0;
        }
    }

    public int addToMatch(Match m){
        if(matches.size() == 0){
            visitedTeams[m.homeTeam.teamId] = 1;
            matches.add(m);
            return 0;
        }

        Match prevMatch = matches.getLast();
        matches.add(m);
        return Main.dist[prevMatch.homeTeam.teamId][m.homeTeam.teamId];
    }

    public int removeFromMatch(){
        if(matches.size() == 1){
            Match m = matches.getLast();
            visitedTeams[m.homeTeam.teamId] = 0;
            matches.removeLast();
            return 0;
        }
        Match m = matches.getLast();
        visitedTeams[m.homeTeam.teamId] = 0;
        Match removedMatch = matches.removeLast();
        Match prevMatch = matches.getLast();
        return Main.dist[prevMatch.homeTeam.teamId][removedMatch.homeTeam.teamId];
    }

    public boolean checkAllVisited(){
        int sum = 0;
        for(int i = 0; i < visitedTeams.length; i++){
            sum += visitedTeams[i];
        }
        return sum == Main.nTeams;
    }

    @Override
    public String toString() {
        return "Umpire{" +
                "id=" + id +
                '}';
    }
}
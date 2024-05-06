import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Umpire {
    public int id;
    public List<Match> matches = new ArrayList<>();
    public int[] visitedTeams = new int[Main.nTeams];

    public Umpire(int id) {
        this.id = id;
        for(int i = 0; i < visitedTeams.length; i++){
            visitedTeams[i] = 0;
        }
    }
    public Umpire(Umpire other) {
        this.id = other.id;
        this.matches = new ArrayList<>(other.matches);
        this.visitedTeams = other.visitedTeams.clone();
    }

    public int addToMatch(Match m){
        visitedTeams[m.homeTeam.teamId] = 1;
        matches.add(m);

        if(matches.size() == 1){
            return 0;
        }

        Match prevMatch = matches.get(matches.size() - 2);
        return Main.dist[prevMatch.homeTeam.teamId][m.homeTeam.teamId];
    }

    public int removeFromMatch(){
        Match m = matches.getLast();
        visitedTeams[m.homeTeam.teamId] = 0;
        Match removedMatch = matches.removeLast();

        if(matches.size() == 0){
            return 0;
        }

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
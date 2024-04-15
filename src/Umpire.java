import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Umpire {
    public int id;
    public List<Match> matches = new ArrayList<>();

    public Umpire(int id) {
        this.id = id;
    }

    public int addToMatch(Match m){
        if(matches.size() == 0){
            matches.add(m);
            return 0;
        }

        Match prevMatch = matches.getLast();
        matches.add(m);
        return Main.dist[prevMatch.homeTeam.teamId][m.homeTeam.teamId];
    }

    public int removeFromMatch(){
        if(matches.size() == 1){
            matches.removeLast();
            return 0;
        }

        Match removedMatch = matches.removeLast();
        Match prevMatch = matches.getLast();
        return Main.dist[prevMatch.homeTeam.teamId][removedMatch.homeTeam.teamId];
    }

    @Override
    public String toString() {
        return "Umpire{" +
                "id=" + id +
                '}';
    }
}
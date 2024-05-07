import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Umpire {
    public int id;
    public List<Match> matches = new ArrayList<>();
    public List<Team> visitedTeams = new ArrayList<>();

    public Umpire(int id) {
        this.id = id;
        this.visitedTeams = new ArrayList<>();
    }

    public Umpire(Umpire other) {
        this.id = other.id;
        this.matches = new ArrayList<>(other.matches);
        this.visitedTeams = new ArrayList<>(other.visitedTeams);
    }

    public int addToMatch(Match m){
        visitedTeams.add(m.homeTeam);
        matches.add(m);

        if(matches.size() == 1){
            return 0;
        }

        Match prevMatch = matches.get(matches.size() - 2);
        return Main.dist[prevMatch.homeTeam.teamId][m.homeTeam.teamId];
    }

    public int removeFromMatch(){
        Match m = matches.getLast();
        visitedTeams.remove(m.homeTeam);
        Match removedMatch = matches.removeLast();

        if(matches.size() == 0){
            return 0;
        }

        Match prevMatch = matches.getLast();
        return Main.dist[prevMatch.homeTeam.teamId][removedMatch.homeTeam.teamId];
    }

    public boolean checkAllVisited(){
        // Check if visitedTeams contains Main.nTeams diffrent teams
        for(int i = 0; i < Main.nTeams; i++){
            if(!visitedTeams.contains(Main.teams.get(i))){
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "Umpire{" +
                "id=" + id +
                '}';
    }
}
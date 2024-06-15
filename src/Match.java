import java.util.ArrayList;
import java.util.List;

public class Match{
    public int round;
    public Team homeTeam;
    public Team outTeam;
    public int index;

    public List<Umpire> feasibleUmpires = new ArrayList<>();

    public Match(int round, Team homeTeam, Team outTeam, int index){
        this.round = round;
        this.homeTeam = homeTeam;
        this.outTeam = outTeam;
        this.index = index;
    }

    public Match(Match other, List<Umpire> umpires){
        this.round = other.round;
        this.homeTeam = other.homeTeam;
        this.outTeam = other.outTeam;
        this.index = other.index;

        this.feasibleUmpires = new ArrayList<>(umpires);
    }

    public boolean isEmptyAfterRemove(Umpire u){
        if(feasibleUmpires.size() == 1 && feasibleUmpires.get(0).id == u.id){
            return true;
        }
        return false;
    }

    public boolean removeFeasibleUmpire(Umpire u){
        boolean res = feasibleUmpires.contains(u);
        feasibleUmpires.remove(u);
        return res;
    }

    public void addFeasibleUmpire(Umpire u){
        feasibleUmpires.add(u);
    }

    public void sortFeasibleUmpires(){
        feasibleUmpires.sort((u1, u2) -> {
            if (u1.matches.isEmpty() || u2.matches.isEmpty()) {
                return 0;
            }
            Match u1LastMatch = u1.matches.getLast();
            Match u2LastMatch = u2.matches.getLast();
            int d1 = Main.dist[u1LastMatch.homeTeam.teamId][homeTeam.teamId];
            int d2 = Main.dist[u2LastMatch.homeTeam.teamId][homeTeam.teamId];
            return d2 - d1;
        });
    }

    public void setAsigned(boolean b) {
        isAsigned = b;
    }

    @Override
    public String toString() {
        return "Match{" +
                "round=" + round +
                ", index=" + index +
                ", homeTeam=" + homeTeam +
                ", outTeam=" + outTeam +
                '}';
    }
}
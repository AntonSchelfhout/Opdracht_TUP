import java.util.ArrayList;
import java.util.List;

public class Match {
    public int round;
    public Team homeTeam;
    public Team outTeam;

    public Umpire umpire;
    public List<Umpire> feasibleUmpires = new ArrayList<>();

    public Match(int round, Team homeTeam, Team outTeam){
        this.round = round;
        this.homeTeam = homeTeam;
        this.outTeam = outTeam;

        feasibleUmpires = Main.umpires;
    }

    public boolean isEmptyAfterRemove(Umpire u){
        if(feasibleUmpires.size() == 1 && feasibleUmpires.get(0).id == u.id){
            return true;
        }
        return false;
    }

    public boolean removeUmpire(Umpire u){
        boolean res = feasibleUmpires.contains(u);
        feasibleUmpires.remove(u);
        return res;
    }

    public void addUmpire(Umpire u){
        feasibleUmpires.add(u);
    }

    @Override
    public String toString() {
        return "Match{" +
                "round=" + round +
                ", homeTeam=" + homeTeam +
                ", outTeam=" + outTeam +
                '}';
    }
}
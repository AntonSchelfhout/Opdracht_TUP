import java.util.ArrayList;
import java.util.List;

public class Match {
    public int round;
    public Team homeTeam;
    public Team outTeam;
    public int index;

    public Umpire umpire;
    public List<Umpire> feasibleUmpires = new ArrayList<>();

    public Match(int round, Team homeTeam, Team outTeam, int index){
        this.round = round;
        this.homeTeam = homeTeam;
        this.outTeam = outTeam;
        this.index = index;

        feasibleUmpires = new ArrayList<>(Main.umpires);
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

    // public boolean homeTeamCanBeReachedByAllUmpires(){
    //     for(Umpire u : feasibleUmpires){
    //         if(homeTeam.umpiresToVisit.contains(u)){
    //             return false;
    //         }
    //     }
    //     return true;
    // }

    public void addUmpire(Umpire u){
        feasibleUmpires.add(u);
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
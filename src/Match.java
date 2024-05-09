import java.util.ArrayList;
import java.util.List;

public class Match {
    public int round;
    public Team homeTeam;
    public Team outTeam;
    public int index;

    public Match(int round, Team homeTeam, Team outTeam, int index){
        this.round = round;
        this.homeTeam = homeTeam;
        this.outTeam = outTeam;
        this.index = index;
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
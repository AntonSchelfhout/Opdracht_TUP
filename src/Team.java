import java.util.ArrayList;
import java.util.List;

public class Team {
    public int teamId;
    // List of all upmires that still need to visit this Team
    public List<Umpire> umpiresToVisit = new ArrayList<>();

    public Team(int id) {
        this.teamId = id;
        umpiresToVisit = Main.umpires;
    }

    @Override
    public String toString() {
        return "Team{" +
                "teamId=" + teamId +
                '}';
    }
}

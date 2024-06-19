import java.util.ArrayList;
import java.util.List;

public class Team {
    public int teamId;
    public List<Match> umpiresToVisit;

    public List<Match> awayMatches = new ArrayList<>();
    public List<Match> homeMatches = new ArrayList<>();

    public Team(int id) {
        this.teamId = id;
    }

    public ArrayList<Match> getHomeMatchesAfterRound(Umpire u, Round r) {
        int roundIndex = r.index;

        // Get all home matches after this round
        ArrayList<Match> hm = new ArrayList<>();
        for(int i = homeMatches.size() - 1; i >= 0; i--) {
            Match m = homeMatches.get(i);

            if(!m.feasibleUmpires.contains(u) || m.claimMatch != null) {
                continue;
            }

            if(m.round <= roundIndex) {
                break;
            }

            hm.add(m);
        }

        return hm;
    }

    @Override
    public String toString() {
        return "Team{" +
                "teamId=" + teamId +
                '}';
    }
}

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Round {
    public int index;

    public List<Match> matches = new ArrayList<>();
    public Map<Team, Match> locations = new HashMap<>();
    public Map<Team, Match> teams = new HashMap<>();

    public Round(int index, List<Match> matches) {
        this.index = index;
        this.matches = matches;

        for(Match m : matches) {
            locations.put(m.homeTeam, m);
            teams.put(m.homeTeam, m);
            teams.put(m.outTeam, m);
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Match match : matches) {
            sb.append("\n\t * ").append(match.toString());
        }

        return "Round{" +
                "index=" + index +
                ",matches=" + sb.toString() +
                '}';
    }
}
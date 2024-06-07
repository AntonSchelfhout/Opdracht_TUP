import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Problem implements Cloneable{
    public List<Round> rounds = new ArrayList<>();
    public List<Match> matches = new ArrayList<>();
    public List<Umpire> umpires = new ArrayList<>();
    public List<Team> teams = new ArrayList<>();

    Problem(List<Round> rounds, List<Match> matches, List<Umpire> umpires, List<Team> teams) {
        this.rounds = rounds;
        this.matches = matches;
        this.umpires = umpires;
        this.teams = teams;

        for(Match m : matches) {
            m.feasibleUmpires = new ArrayList<>(umpires);
        }
    }

    public Problem clone() {
        List<Round> newRounds = new ArrayList<>();
        List<Match> newMatches = new ArrayList<>();
        List<Umpire> newUmpires = new ArrayList<>();

        // Duplicate all umpires
        for(Umpire u : umpires) {
            newUmpires.add(new Umpire(u));
        }

        // Duplicate all matches 
        for(Match m : matches) {
            newMatches.add(new Match(m, newUmpires));
        }
        
        // Duplicate all rounds
        for(Round r : rounds) {
            newRounds.add(new Round(r, newMatches));
        }

        return new Problem(newRounds, newMatches, newUmpires, teams);
    }
}
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

    public boolean checkSameRound(Umpire u, Match m){
        boolean res = true;
        for(Match match : matches){
            if(!match.equals(m) && match.index > m.index){
                res &= !match.isEmptyAfterRemove(u);
            }
        }
        return res;
    }

    public boolean checkFirstConstraint(Umpire u, Match m){
        Match matchWithSameLocation = locations.get(m.homeTeam);
        boolean res = true;
        if(matchWithSameLocation != null){
            res = !matchWithSameLocation.isEmptyAfterRemove(u);
        }
        return res;
    }


    public boolean checkSecondConstraint(Umpire u, Match m){
        Match team1 = teams.get(m.homeTeam);
        Match team2 = teams.get(m.outTeam);

        boolean res = true;
        res &= !team1.isEmptyAfterRemove(u);
        res &= !team2.isEmptyAfterRemove(u);

        return res;
    }

    public HashSet<Match> adjustSameRound(Umpire u, Match m){
        HashSet<Match> res = new HashSet<>();
        for(Match match : matches){
            if(!match.equals(m) && match.index > m.index){
                if(match.removeUmpire(u)){
                    res.add(match);
                }
            }
        }
        return res;
    }

    public HashSet<Match> adjustFirstConstraint(Umpire u, Match m){
        Match matchWithSameLocation = locations.get(m.homeTeam);
        if(matchWithSameLocation != null){
            if(matchWithSameLocation.removeUmpire(u)){
                return new HashSet<Match>(){{
                    add(matchWithSameLocation);
                }};
            }
        }
        return new HashSet<>();
    }

    public HashSet<Match> adjustSecondConstraint(Umpire u, Match m){
        Match team1 = teams.get(m.homeTeam);
        Match team2 = teams.get(m.outTeam);

        HashSet<Match> res = new HashSet<>();
        if(team1.removeUmpire(u)){
            res.add(team1);
        }
        if(team2.removeUmpire(u)){
            res.add(team2);   
        }
        return res;
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
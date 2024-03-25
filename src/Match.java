public class Match {
    public int round;
    public Team homeTeam;
    public Team outTeam;
    public boolean isAsigned;

    public Match(int round, Team homeTeam, Team outTeam){
        this.round = round;
        this.homeTeam = homeTeam;
        this.outTeam = outTeam;
        this.isAsigned = false;
    }

    // check if this match contains a team that's also part of match m
    public boolean containsTeamsOfRound(Match m) {
        if (this.homeTeam == m.outTeam) return false;
        if (this.outTeam == m.homeTeam) return false;
        else return true;
    }

    public void setAsigned(boolean b) {
        isAsigned = b;
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
public class Team {
    public int teamId;

    public Team(int id) {
        this.teamId = id;
    }

    @Override
    public String toString() {
        return "Team{" +
                "teamId=" + teamId +
                '}';
    }
}

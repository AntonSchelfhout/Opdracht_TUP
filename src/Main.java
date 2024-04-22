import java.io.*;
import java.util.*;
public class Main {
    // https://benchmark.gent.cs.kuleuven.be/tup/en/results/
    static int q1 = 4;
    static int q2 = 2;
    static int n;
    static int nTeams;
    static int nRounds;
    static int[][] dist;

    static List<Round> rounds = new ArrayList<>();
    static List<Match> matches = new ArrayList<>();
    static List<Umpire> umpires = new ArrayList<>();
    static List<Team> teams = new ArrayList<>();

    public static void main(String[] args) throws FileNotFoundException {

        // Read the file
        readFile("umps8");

        long startTime = System.currentTimeMillis();

        // Sort nodes on distance

        // Fix de eerste ronde
        for (int i = 0; i < rounds.get(0).matches.size(); i++) {
            umpires.get(i).addToMatch(matches.get(i));
            matches.get(i).addUmpire(umpires.get(i));
        }

        // Start thread for lowerbounds
        Thread lowerBounds = new Thread(new LowerBound());
        lowerBounds.start();

        // Start new thread for branching
        Thread branching = new Thread(new BranchAndBound(lowerBounds));
        branching.start();

        // Print finished results
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Total runtime: " + totalTime + " milliseconds");
    }

    public static void printOutput(BranchAndBound branchAndBound) {
        System.out.println("--------------------");
        System.out.println(branchAndBound.currentDistance);
        System.out.println("--------------------");
        // for(Round r: rounds){
        //     System.out.println("Round " + r.index);
        //     for(Match m: r.matches){
        //         System.out.println("\t* ("+ m.homeTeam.teamId + " - " + m.outTeam .teamId+") => " + m.umpire);
        //     }
        // }
    }

    public static void readFile(String file) throws FileNotFoundException{
        Scanner sc = new Scanner(new File("Input/" + file + ".txt"));

        String line = removePadding(sc, "nTeams");
        assert line != null;
        int nTeams = Integer.parseInt(line.split("=")[1].split(";")[0]);

        removePadding(sc, "dist");
        dist = parseArray(sc, nTeams);

        removePadding(sc, "opponents");
        int[][] opponents = parseArray(sc, (nTeams - 1) * 2);

        // Create all the teams
        for (int i=0; i<nTeams; i++) {
            Team t = new Team(i);
            teams.add(t);
        }

        // Create all the umpires
        for(int i = 0; i < nTeams / 2; i++){
            Umpire u = new Umpire(i);
            umpires.add(u);
        }

        // Read all the rounds/matches
        matches = new ArrayList<>();
        int round = 0;
        for(int[] opponentsRound: opponents) {
            // Create a list of matches for this round (skip the returning negative matches)
            List<Match> roundMatches = new ArrayList<>();
            
            for(int i = 0; i < opponentsRound.length; i++) {
                int o = opponentsRound[i];
                if(o < 0) continue;

                Match m = new Match(round, teams.get(i), teams.get(o - 1), i);
                roundMatches.add(m);
                matches.add(m);
            }
            
            // Create a round object and add it to the list of rounds
            Round r = new Round(round, roundMatches);
            rounds.add(r);
            round++;
        }
        
        //Debug print the rounds
        // for (Round r: rounds) {
        //     System.out.println(r.toString());
        // }

        sc.close();
    }

    private static int[][] parseArray(Scanner scanner, int nTeams) {
        int[][] array = new int[nTeams][nTeams];
        for (int i = 0; i < nTeams; i++) {
            String line = scanner.nextLine().split("\\[")[1].split("]")[0];
            array[i] = Arrays.stream(line.trim().split("\\s+" )).mapToInt(Integer::parseInt).toArray();
        }
        return array;
    }

    private static String removePadding(Scanner scanner, String end) {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains(end)) {
                return line;
            }
        }
        return null;
    }
}

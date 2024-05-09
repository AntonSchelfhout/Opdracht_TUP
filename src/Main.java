import java.io.*;
import java.util.*;
public class Main {
    // https://benchmark.gent.cs.kuleuven.be/tup/en/results/
    static int q1 = 4;
    static int q2 = 2;
    static String file = "umps8";
    static int n;
    static int nTeams;
    static int nRounds;
    static int[][] dist;

    static List<Round> rounds = new ArrayList<>();
    static List<Match> matches = new ArrayList<>();
    static List<Umpire> umpires = new ArrayList<>();
    static List<Team> teams = new ArrayList<>();

    static List<Round> LBrounds = new ArrayList<>();
    static List<Match> LBmatches = new ArrayList<>();
    static List<Umpire> LBumpires = new ArrayList<>();
    static List<Team> LBteams = new ArrayList<>();

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {

        // Read the file
        readFile(file); 

        long startTime = System.currentTimeMillis();

        // Fix de eerste ronde
        for (int i = 0; i < Main.n; i++) {
            LBumpires.get(i).addToMatch(LBmatches.get(i));
        }

        // Start thread for lowerbounds
        LowerBound lowerBound = new LowerBound(LBrounds, LBmatches, LBumpires, LBteams);
        Thread lowerBounds = new Thread(lowerBound);
        //lowerBounds.run();

        // Start new thread for branching
        BranchAndBound branchAndBound = new BranchAndBound(lowerBound, rounds, matches, umpires, teams);
        Thread branching = new Thread(branchAndBound);
        branching.run();

        // Wait for the threads to finish
        branching.join();

        // Print finished results
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Total runtime: " + totalTime + " milliseconds");

        // Print the results
        branchAndBound.feasibilityCheck();

        // Print lower bounds
        // for (int i=0; i<nRounds; i++) {
        //     for (int j=0; j<nRounds; j++) {
        //         System.out.print(lowerBound.lowerBounds[i][j] + " ");
        //     }
        //     System.out.println();
        // }
    }

    public static void readFile(String file) throws FileNotFoundException {
        Scanner sc = new Scanner(new File("Input/" + file + ".txt"));

        String line = removePadding(sc, "nTeams");
        assert line != null;
        nTeams = Integer.parseInt(line.split("=")[1].split(";")[0]);
        n = nTeams / 2;
        nRounds = 4 * n - 2;


        removePadding(sc, "dist");
        dist = parseArray(sc, nTeams);

        removePadding(sc, "opponents");
        int[][] opponents = parseArray(sc, (nTeams - 1) * 2);

        // Create all the teams
        for (int i=0; i<nTeams; i++) {
            Team t = new Team(i);
            Team LBt = new Team(i);
            teams.add(t);
            LBteams.add(LBt);
        }

        // Create all the umpires
        for(int i = 0; i < nTeams / 2; i++){
            Umpire u = new Umpire(i);
            Umpire LBu = new Umpire(i);
            umpires.add(u);
            LBumpires.add(LBu);
        }

        // Read all the rounds/matches
        matches = new ArrayList<>();
        LBmatches = new ArrayList<>();
        int round = 0;
        for(int[] opponentsRound: opponents) {
            // Create a list of matches for this round (skip the returning negative matches)
            List<Match> roundMatches = new ArrayList<>();
            List<Match> LBroundMatches = new ArrayList<>();
            
            for(int i = 0; i < opponentsRound.length; i++) {
                int o = opponentsRound[i];
                if(o < 0) continue;

                Match m = new Match(round, teams.get(i), teams.get(o - 1), roundMatches.size());
                Match LBm = new Match(round, LBteams.get(i), LBteams.get(o - 1), LBroundMatches.size());
                roundMatches.add(m);
                LBroundMatches.add(LBm);
                matches.add(m);
                LBmatches.add(LBm);
            }
            
            // Create a round object and add it to the list of rounds
            Round r = new Round(round, roundMatches);
            Round LBr = new Round(round, LBroundMatches);
            rounds.add(r);
            LBrounds.add(LBr);
            round++;
        }

        for(int i = 0; i < n; i++){
            for (int r = 0; r < nRounds; r++) {
                umpires.get(i).fillFeasibleMatches(r);
                LBumpires.get(i).fillFeasibleMatches(r);
            }
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

import java.io.*;
import java.util.*;
public class Main {
    // https://benchmark.gent.cs.kuleuven.be/tup/en/results/
    static int q1 = 5;
    static int q2 = 2;
    static String file = "umps10";
    static int n;
    static int nTeams;
    static int nRounds;
    static int[][] dist;

    static Problem problem;

    public static void main(String[] args) throws FileNotFoundException, InterruptedException {

        // Read the file
        problem = readFile(file); 

        // Create the lower problem deep copy
        Problem lowerProblem = problem.clone();

        // TODO Preprocessing, remove unfeasible edges (see paper)
        long startTime = System.currentTimeMillis();

        // Start thread for lowerbounds
        LowerBound lowerBound = new LowerBound(lowerProblem);
        Thread lowerBounds = new Thread(lowerBound);
        lowerBounds.start();

        
        // Start new thread for branching
        BranchAndBound branchAndBound = new BranchAndBound(lowerBound, problem);
        Thread branching = new Thread(branchAndBound);
        branching.start();

        // Wait for the threads to finish
        branching.join();

        // Print finished results
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Total runtime branch and bound: " + totalTime + " milliseconds");

        // Print the results
        branchAndBound.feasibilityCheck();

        // Print lower bounds
        for (int i=0; i<nRounds; i++) {
            for (int j=0; j<nRounds; j++) {
                System.out.print(lowerBound.lowerBounds[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static Problem readFile(String file) throws FileNotFoundException {
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
        List<Team> teams = new ArrayList<>();
        for (int i=0; i<nTeams; i++) {
            Team t = new Team(i);
            Team LBt = new Team(i);
            teams.add(t);
        }

        // Create all the umpires
        List<Umpire> umpires = new ArrayList<>();
        for(int i = 0; i < nTeams / 2; i++){
            Umpire u = new Umpire(i);
            Umpire LBu = new Umpire(i);
            umpires.add(u);
        }

        // Read all the rounds/matches
        List<Round> rounds = new ArrayList<>();
        List<Match> matches = new ArrayList<>();
        int round = 0;
        for(int[] opponentsRound: opponents) {
            // Create a list of matches for this round (skip the returning negative matches)
            List<Match> roundMatches = new ArrayList<>();

            for(int i = 0; i < opponentsRound.length; i++) {
                int o = opponentsRound[i];
                if(o < 0) continue;

                Match m = new Match(round, teams.get(i), teams.get(o - 1), roundMatches.size());
                roundMatches.add(m);
                matches.add(m);
            }
            
            // Create a round object and add it to the list of rounds
            Round r = new Round(round, roundMatches);
            rounds.add(r);
            round++;
        }

        sc.close();

        return new Problem(rounds, matches, umpires, teams);
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

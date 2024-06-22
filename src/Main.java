import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
public class Main {
    static int q1 = 7;
    static int q2 = 2;
    static String file = "umps14";
    static int n;
    static int nTeams;
    static int nRounds;
    static int[][] dist;

    static Problem problem;
    static int[][] minimalDistances; // matrix containing the minimal umpire distances for all rounds

    static AtomicLong totalNodes = new AtomicLong(0);
    static int upperBound = Integer.MAX_VALUE;
    static List<Umpire> solutions = new ArrayList<>();
      // matrix containing the values of solutions for the subproblems
    public static int[][] lowerBounds;     // matrix containing the lower bounds for all pairs of rounds

    public static void main(String[] args) throws FileNotFoundException, InterruptedException, ExecutionException {

        // Read the file
        problem = readFile(file); 

        long startTime = System.currentTimeMillis();

        Problem fastLowerProblem = problem.clone();
        FastLowerBound fastLowerBound = new FastLowerBound(fastLowerProblem);
        Thread fastLowerBoundThread = new Thread(fastLowerBound);
        fastLowerBoundThread.start();

        // Start thread for lowerbounds
        Problem lowerProblem = problem.clone();
        LowerBound lowerBound = new LowerBound(lowerProblem);
        Thread lowerBoundThread = new Thread(lowerBound);
        lowerBoundThread.start();

        // Small X mseconds delay to make sure the lower bounds are calculated
        Thread.sleep(1000);
        
        // Start N new thread for branching
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<?>> futures = new ArrayList<>();
        for(int i = 0; i < n - 2; i++){
            final int index = i;
            Problem clone = problem.clone();
            BranchAndBound branchAndBound = new BranchAndBound(lowerBound, clone);
            futures.add(executorService.submit(() -> totalNodes.addAndGet(branchAndBound.startBranching(index))));
        }

        // Wait for all threads to finish 
        for(Future<?> future : futures){
            future.get();
        }

        executorService.shutdown();


        // Print finished results
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Total runtime branch and bound: " + totalTime + " milliseconds");
        System.out.println("Upper bound: " + upperBound);
        System.out.println("Total nodes: " + totalNodes);

        // Print lower bounds
        System.out.println("Lower bounds: ");
        for (int i=0; i<nRounds; i++) {
            for (int j=0; j<nRounds; j++) {
                System.out.print(lowerBounds[i][j] + " ");
            }
            System.out.println();
        }

        // feasibilityCheck();
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
            teams.add(t);
        }

        // Create all the umpires
        List<Umpire> umpires = new ArrayList<>();
        for(int i = 0; i < nTeams / 2; i++){
            Umpire u = new Umpire(i);
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

        // Calculate the minimal distances between matches
        int[][] md = new int[nRounds][n];
        for(int i = 1; i < nRounds; i++){
            for(int j = 0; j < n; j++){
                Match toMatch = matches.get(n*i+j);
                int minDist = Integer.MAX_VALUE;
                for(int k = 0; k < n; k++){
                    Match fromMatch = matches.get(n*(i-1)+k);
                    if(toMatch.homeTeam.teamId == fromMatch.homeTeam.teamId || toMatch.outTeam == fromMatch.outTeam || toMatch.homeTeam == fromMatch.outTeam || toMatch.outTeam == fromMatch.homeTeam){
                        continue;
                    }
                    minDist = Math.min(minDist, dist[fromMatch.homeTeam.teamId][toMatch.homeTeam.teamId]);
                }

                for(int k = 0; k < j; k++){
                    md[i][k] += minDist;
                }
            }
        }
        minimalDistances = md;

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

    public static void feasibilityCheck(){
        // Generating matrix
        int[][] formattedSolution = new int[Main.nRounds][Main.n];
        for(Umpire u: solutions){
            for(Match m: u.matches){
                int index = problem.rounds.get(m.round).matches.indexOf(m);
                formattedSolution[m.round][index] = u.id;
            }
        }

        // Check if a umpire doesn't go to 2 or more matches at the same time
        for(int i = 0; i < Main.nRounds; i++){
            for(int j = 0; j < Main.n; j++){
                for(int k = j + 1; k < Main.n; k++){
                    if(formattedSolution[i][j] == formattedSolution[i][k]){
                        System.out.println("ERROR: Umpire " + formattedSolution[i][j] + " goes to match " + j + " and " + k + " at the same time");
                    }
                }
            }
        }

        // Check if a umpire visits every team's home once
        for(Umpire u : solutions) {
            int[] visitedTeams = new int[Main.nTeams];
            for(int i = 0; i < Main.nRounds; i++) {
                Match m = u.matches.get(i);
                visitedTeams[m.homeTeam.teamId] = 1;
            }

            for(int i = 0; i < Main.nTeams; i++) {
                if(visitedTeams[i] == 0) {
                    System.out.println("ERROR: Umpire " + u.id + " does not visit team " + i + "'s home");
                }
            }
        }
        
        // Check Q1 constraint, same location not visited
        for(Umpire u : solutions) {
            for(int i = 0; i < Main.nRounds; i++) {
                Match m1 = u.matches.get(i);

                for(int j = i + 1; j < Main.q1 - 1; j++) {
                    Match m2 = u.matches.get(j);

                    if(m1 == m2) continue;

                    if(m1.homeTeam.teamId == m2.homeTeam.teamId) {
                        System.out.println("ERROR: Umpire " + u.id + " visits same location in Q1 in round " + i);
                    }
                }
            }
        }

        // Check Q2 constraint, teams not visited 
        for(Umpire u : solutions) {
            for(int i = 0; i < Main.nRounds; i++) {
                Match m1 = u.matches.get(i);
                int homeTeam1 = m1.homeTeam.teamId;
                int outTeam1 = m1.outTeam.teamId;

                for(int j = i + 1; j < Main.q2 - 1; j++) {
                    Match m2 = u.matches.get(i);

                    if(m1 == m2) continue;

                    int homeTeam2 = m2.homeTeam.teamId;
                    int outTeam2 = m2.outTeam.teamId;

                    if(homeTeam1 == homeTeam2 || homeTeam1 == outTeam2 || outTeam1 == homeTeam2 || outTeam1 == outTeam2){
                        System.out.println("ERROR: Umpire " + u.id + " hosts same team in Q2 in round " + i);
                    }
                }
            }
        }

        // Create validator output
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Main.nRounds; i++) {
            for (int j = 0; j < Main.n; j++) {
                sb.append(formattedSolution[i][j] + 1);
                if (j < Main.n - 1) {
                    sb.append(",");
                }
            }
            if (i < Main.nRounds - 1) {
                sb.append(",");
            }
        }
        String result = sb.toString();

        // Create output.txt for validator
        try {
            java.io.FileWriter myWriter = new java.io.FileWriter("output.txt");
            myWriter.write(result);
            myWriter.close();
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }   

        // Run validator
        String command = "java -jar validator.jar Input/"+ Main.file +".txt "+ Main.q1+" "+ Main.q2 + " output.txt";
        try {
            executeCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void executeCommand(String command) throws IOException {
        Process process = Runtime.getRuntime().exec(command);

        // Read output
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("--------------------");
            System.out.println("Nodes: " + totalNodes);
            System.out.println("Our distance: " + upperBound);
            System.out.println(line);
            System.out.println("--------------------");
        }
    }
}

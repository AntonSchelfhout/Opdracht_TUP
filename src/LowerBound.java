 // LOWERBOUNDS
    // HungarianAlgorithm ha = new HungarianAlgorithm(dataMatrix);
    // int[][] assignment = ha.findOptimalAssignment();

<<<<<<< Updated upstream
import java.util.ArrayList;
import java.util.List;
import java.lang.Thread;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

 public class LowerBound implements Runnable{
    public int[][] solutions;       // matrix containing the values of solutions for the subproblems
    public int[][] lowerBounds;     // matrix containing the lower bounds for all pairs of rounds

    Problem problem;

    public LowerBound(Problem problem) {
        this.solutions = new int[Main.nRounds][Main.nRounds];
        this.lowerBounds = new int[Main.nRounds][Main.nRounds];

        this.problem = problem;

        for (int i=0; i<Main.nRounds; i++) {
            for (int j=0; j<Main.nRounds; j++) {
                this.solutions[i][j] = 0;
                this.lowerBounds[i][j] = 0;
            }
        }
    }

    public int[][] createMatrix(int startRound) {       // matrix with the length from each match to other matches in the next round
        int[][] matrix = new int[Main.n][Main.n];
        for(int i = 0; i < Main.n; i++) {
            for (int j = 0; j < Main.n; j++) {
                Match matchRound1 = problem.matches.get(Main.n*startRound+i);
                Match matchRound2 = problem.matches.get(Main.n*(startRound+1)+j);
                if(matchRound1.homeTeam.teamId == matchRound2.homeTeam.teamId || matchRound1.outTeam == matchRound2.outTeam || matchRound1.homeTeam == matchRound2.outTeam || matchRound1.outTeam == matchRound2.homeTeam){
                    matrix[i][j] = 999999;
                    continue;
=======
    import java.util.ArrayList;
    import java.util.List;
    import java.lang.Thread;
    import java.util.concurrent.ExecutorService;
    import java.util.concurrent.Executors;
    import java.util.concurrent.Future;
    
     public class LowerBound implements Runnable{
        public int[][] solutions;       // matrix containing the values of solutions for the subproblems
        public int[][] lowerBounds;     // matrix containing the lower bounds for all pairs of rounds
    
        Problem problem;
    
        public LowerBound(Problem problem) {
            this.solutions = new int[Main.nRounds][Main.nRounds];
            this.lowerBounds = new int[Main.nRounds][Main.nRounds];
    
            this.problem = problem;
    
            for (int i=0; i<Main.nRounds; i++) {
                for (int j=0; j<Main.nRounds; j++) {
                    this.solutions[i][j] = 0;
                    this.lowerBounds[i][j] = 0;
>>>>>>> Stashed changes
                }
            }
        }
<<<<<<< Updated upstream
        return matrix;
    }


    public int calculateDistance(int[][] matrix, int startRound) {
        int cost = 0;
        for(int i = 0; i < Main.n; i++) {
            Team teamRound1 = problem.matches.get(Main.n*startRound + matrix[i][0]).homeTeam;
            Team teamRound2 = problem.matches.get(Main.n*(startRound+1) + matrix[i][1]).homeTeam;
            cost += Main.dist[teamRound1.teamId][teamRound2.teamId];
        }
        return cost;
    }

     public void calculateLowerBounds() {
         // initial lower bounds for all pairs of rounds
         for(int r=Main.nRounds-2; r>=0; r--) {
             HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm();

             int[][] matrix = createMatrix(r);

             int[][] result = hungarianAlgorithm.computeAssignments(matrix);
             int afstand = calculateDistance(result, r);

             for(int r2=r+1; r2 < Main.nRounds; r2++) {
                 this.lowerBounds[r][r2] = afstand + lowerBounds[r+1][r2];
             }
         }

         // Calculate lower bounds for bigger subproblems
         ExecutorService executor = Executors.newFixedThreadPool(2); // Two threads for parallel execution

         for(int k = 2; k < Main.nRounds; k++) { // size of the subproblem+1
             int r = Main.nRounds - 1 - k; // start round

             List<Future<?>> futures = new ArrayList<>();        // holds differrent threads

             while (r >= 1) {
                 final int startRound = r;
                 final int endRound = r + k;

                 futures.add(executor.submit(() -> {             // fills the futures with different startround and endround values to execute in parrallel
                     for (int r0 = endRound - 2; r0 >= startRound; r0--) {
                         if (solutions[r0][endRound] != 0) continue;

                         // Get subset of rounds and matches
                         List<Round> roundsSubset = problem.rounds.subList(r0, endRound + 1);
                         List<Match> matchSubset = problem.matches.subList(r0 * Main.n, (endRound + 1) * Main.n);
                         Problem problemSubset = problem.cloneSubset(roundsSubset, matchSubset);

                         // Create a new FastBranchAndBound task
                         FastBranchAndBound branchAndBound = new FastBranchAndBound(this, r0, endRound, problemSubset);
                         branchAndBound.run();

                         solutions[r0][endRound] = branchAndBound.getTotalDistance();
                         for(int r1 = r0; r1 >= 0; r1--) {
                             for (int r2 = endRound; r2 < Main.nRounds; r2++) {
                                 lowerBounds[r1][r2] = Math.max(lowerBounds[r1][r2], lowerBounds[r1][r0] + solutions[r0][endRound] + lowerBounds[endRound][r2]);
                             }
                         }
                     }
                 }));

                 r -= k;
             }

             // Wait for all tasks to complete
             for (Future<?> future : futures) {
                 try {
                     future.get();       // run the futures
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         }

         executor.shutdown();
     }

    @Override
    public void run() {
        calculateLowerBounds();
    }
}
=======
    
        public int[][] createMatrix(int startRound) {       // matrix with the length from each match to other matches in the next round
            int[][] matrix = new int[Main.n][Main.n];
            for(int i = 0; i < Main.n; i++) {
                for (int j = 0; j < Main.n; j++) {
                    Match matchRound1 = problem.matches.get(Main.n*startRound+i);
                    Match matchRound2 = problem.matches.get(Main.n*(startRound+1)+j);
                    if(matchRound1.homeTeam.teamId == matchRound2.homeTeam.teamId || matchRound1.outTeam == matchRound2.outTeam || matchRound1.homeTeam == matchRound2.outTeam || matchRound1.outTeam == matchRound2.homeTeam){
                        matrix[i][j] = 999999;
                        continue;
                    }
                    matrix[i][j] = Main.dist[matchRound1.homeTeam.teamId][matchRound2.homeTeam.teamId];
                }
            }
            return matrix;
        }
    
    
        public int calculateDistance(int[][] matrix, int startRound) {
            int cost = 0;
            for(int i = 0; i < Main.n; i++) {
                Team teamRound1 = problem.matches.get(Main.n*startRound + matrix[i][0]).homeTeam;
                Team teamRound2 = problem.matches.get(Main.n*(startRound+1) + matrix[i][1]).homeTeam;
                cost += Main.dist[teamRound1.teamId][teamRound2.teamId];
            }
            return cost;
        }
    
         public void calculateLowerBounds() {
             // initial lower bounds for all pairs of rounds
             for(int r=Main.nRounds-2; r>=0; r--) {
                 HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm();
    
                 int[][] matrix = createMatrix(r);
    
                 int[][] result = hungarianAlgorithm.computeAssignments(matrix);
                 int afstand = calculateDistance(result, r);
    
                 for(int r2=r+1; r2 < Main.nRounds; r2++) {
                     this.lowerBounds[r][r2] = afstand + lowerBounds[r+1][r2];
                 }
             }
    
             // Calculate lower bounds for bigger subproblems
             ExecutorService executor = Executors.newFixedThreadPool(2); // Two threads for parallel execution
    
             for(int k = 2; k < Main.nRounds; k++) { // size of the subproblem+1
                 int r = Main.nRounds - 1 - k; // start round
    
                 List<Future<?>> futures = new ArrayList<>();        // holds differrent threads
    
                 while (r >= 1) {
                     final int startRound = r;
                     final int endRound = r + k;
    
                     futures.add(executor.submit(() -> {             // fills the futures with different startround and endround values to execute in parrallel
                         for (int r0 = endRound - 2; r0 >= startRound; r0--) {
                             if (solutions[r0][endRound] != 0) continue;
    
                             // Get subset of rounds and matches
                             List<Round> roundsSubset = problem.rounds.subList(r0, endRound + 1);
                             List<Match> matchSubset = problem.matches.subList(r0 * Main.n, (endRound + 1) * Main.n);
                             Problem problemSubset = problem.cloneSubset(roundsSubset, matchSubset);
    
                             // Create a new FastBranchAndBound task
                             FastBranchAndBound branchAndBound = new FastBranchAndBound(this, r0, endRound, problemSubset);
                             branchAndBound.run();
    
                             solutions[r0][endRound] = branchAndBound.getTotalDistance();
                             for(int r1 = r0; r1 >= 0; r1--) {
                                 for (int r2 = endRound; r2 < Main.nRounds; r2++) {
                                     lowerBounds[r1][r2] = Math.max(lowerBounds[r1][r2], lowerBounds[r1][r0] + solutions[r0][endRound] + lowerBounds[endRound][r2]);
                                 }
                             }
                         }
                     }));
    
                     r -= k;
                 }
    
                 // Wait for all tasks to complete
                 for (Future<?> future : futures) {
                     try {
                         future.get();       // run the futures
                     } catch (Exception e) {
                         e.printStackTrace();
                     }
                 }
             }
    
             executor.shutdown();
         }
    
        @Override
        public void run() {
            calculateLowerBounds();
        }
    }
>>>>>>> Stashed changes

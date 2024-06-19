 // LOWERBOUNDS
    // HungarianAlgorithm ha = new HungarianAlgorithm(dataMatrix);
    // int[][] assignment = ha.findOptimalAssignment();

    import java.util.ArrayList;
    import java.util.List;
    import java.util.concurrent.ExecutionException;
    import java.util.concurrent.ExecutorService;
    import java.util.concurrent.Executors;
    import java.util.concurrent.Future;
    import java.lang.Thread;
    
    public class FastLowerBound implements Runnable{
    
        Problem problem;
        long startTime = System.currentTimeMillis();
    
        public FastLowerBound(Problem problem) {
            Main.lowerboundsSolutions = new int[Main.nRounds][Main.nRounds];
            Main.lowerBounds = new int[Main.nRounds][Main.nRounds];
    
            this.problem = problem;
    
            for (int i=0; i<Main.nRounds; i++) {
                for (int j=0; j<Main.nRounds; j++) {
                    Main.lowerboundsSolutions[i][j] = 0;
                    Main.lowerBounds[i][j] = 0;
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
                    }
                    matrix[i][j] = Main.dist[matchRound1.homeTeam.teamId][matchRound2.homeTeam.teamId];
                }
            }
            return matrix;
        }
    
    
        public int calculateDistance(int[][] matrix, int startRound) {
            int cost = 0;
            for(int i = 0; i < Main.n; i++) {
                Match matchRound1 = problem.matches.get(Main.n*startRound + matrix[i][0]);
                Match matchRound2 = problem.matches.get(Main.n*(startRound+1) + matrix[i][1]);
                Team teamRound1 = matchRound1.homeTeam;
                Team teamRound2 = matchRound2.homeTeam;
    
                int dist = Main.dist[teamRound1.teamId][teamRound2.teamId];
    
                cost += dist;
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
                Main.lowerBounds[r][r2] = afstand + Main.lowerBounds[r+1][r2];
               }
           }
    
           // Calculate lower bounds for bigger subproblems
           ExecutorService executor = Executors.newFixedThreadPool(Main.nRounds); // Two threads for parallel execution
    
           for(int k = 2; k < Main.nRounds; k++) { // size of the subproblem+1
               int r = Main.nRounds - 1 - k; // start round
    
    
               // Run all these in parallel
               while (r >= 1) {
                    for (int r0 = r + k - 2; r0 >= r; r0--) {
                        if (Main.lowerboundsSolutions[r0][r + k] != 0) continue;
                        final int startRound = r;
                        final int endRound = r + k;
                        final int round = r0;
    
                            // Get subset of rounds and matches
                            List<Round> roundsSubset = problem.rounds.subList(round, endRound + 1);
                            List<Match> matchSubset = problem.matches.subList(round * Main.n, (endRound + 1) * Main.n);
                            Problem problemSubset = problem.cloneSubset(roundsSubset, matchSubset);
    
                            // Create a new FastBranchAndBound task
                            FastestBranchAndBound branchAndBound = new FastestBranchAndBound(this, round, endRound, problemSubset);
                            branchAndBound.run();
    
                            Main.lowerboundsSolutions[round][endRound] = branchAndBound.getTotalDistance();
                            for(int r1 = round; r1 >= 0; r1--) {
                                for (int r2 = endRound; r2 < Main.nRounds; r2++) {
                                    Main.lowerBounds[r1][r2] = Math.max(Main.lowerBounds[r1][r2], Main.lowerBounds[r1][round] + Main.lowerboundsSolutions[round][endRound] + Main.lowerBounds[endRound][r2]);
                                }
                            }
                    }
    
                   r -= k;
               }
    
            
           }
    
           executor.shutdown();
       }
    
        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
    
            calculateLowerBounds();
    
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println("LOWERBOUNDS " + totalTime + "");
    
            // Print lower bounds
            // for (int i=0; i<Main.nRounds; i++) {
            //     for (int j=0; j<Main.nRounds; j++) {
            //         System.out.print(lowerBounds[i][j] + " ");
            //     }
            //     System.out.println();
            // }
        }
    }
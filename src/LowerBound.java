 // LOWERBOUNDS
    // HungarianAlgorithm ha = new HungarianAlgorithm(dataMatrix);
    // int[][] assignment = ha.findOptimalAssignment();

import java.util.ArrayList;
import java.util.List;

public class LowerBound implements Runnable{
    public int[][] solutions;       // matrix containing the values of solutions for the subproblems
    public int[][] lowerBounds;     // matrix containing the lower bounds for all pairs of rounds

    public int[] bestLowerBounds;   // array containing the best lower bounds for each startround

    List<Round> rounds = new ArrayList<>();
    List<Match> matches = new ArrayList<>();
    List<Umpire> umpires = new ArrayList<>();
    List<Team> teams = new ArrayList<>();

    public LowerBound(List<Round> rounds, List<Match> matches, List<Umpire> umpires, List<Team> teams) {
        this.solutions = new int[Main.nRounds][Main.nRounds];
        this.lowerBounds = new int[Main.nRounds][Main.nRounds];

        this.rounds = rounds;
        this.matches = matches;
        this.umpires = umpires;
        this.teams = teams;

        for (int i=0; i<Main.nRounds; i++) {
            for (int j=0; j<Main.nRounds; j++) {
                this.solutions[i][j] = 0;
                this.lowerBounds[i][j] = 0;
            }
        }
    }

    public void calculateLowerBounds() {
        // initial lower bounds for all pairs of rounds
        for(int r=Main.nRounds-2; r>=1; r--) {
            HungarianAlgorithm ha = new HungarianAlgorithm(r, r+1);
            this.solutions[r][r+1] = ha.getCost();
            
            for (int r2=r+1; r2<Main.nRounds; r2++) {
                this.lowerBounds[r][r2] = solutions[r][r+1] + lowerBounds[r+1][r2];
            }
        }

        // calculate lower bounds for bigger subproblems
        for(int k = 2; k < Main.nRounds; k++) {         // size of the subproblem+1
            System.out.println("==== k = " + k + " ====");
            int r = Main.nRounds - 1 - k;                   // start round
            while(r >= 1) {
                System.out.println("r = " + r);
                for(int r0 = r + k - 2; r0 <= r; r0++) {
                    if(solutions[r0][r+k] == 0) {
                        // get subset of rounds and matches
                        List<Round> roundsSubset = rounds.subList(r0, r + k); 
                        List<Match> matchSubset = matches.subList(r0 * Main.n, (r + k) * Main.n); 
                        
                        System.out.println("From: " + r0 + " TO: " + (r + k));
                        BranchAndBound branchAndBound = new BranchAndBound(0, 0, this, new ArrayList<>(roundsSubset), new ArrayList<>(matchSubset), new ArrayList<>(umpires), new ArrayList<>(teams), r0);
                        branchAndBound.branch(0);
                        solutions[r0][r+k] = branchAndBound.getTotalDistance();

                        for(int r1=r0; r1>=1; r1--) {
                            for (int r2=r+k; r2<Main.nRounds; r2++) {
                                lowerBounds[r1][r2] = Math.max(lowerBounds[r1][r2], lowerBounds[r1][r0] + solutions[r0][r+k] + lowerBounds[r+k][r2]);
                            }
                        }
                    }
                }
                r -= k;
            }
                
        }

        for(int i=0; i<Main.nRounds; i++) {
            for (int j=0; j<Main.nRounds; j++) {
                System.out.print(lowerBounds[i][j] + "    ");
            }
            System.out.println(" ");
        }
    }

    @Override
    public void run() {
        this.calculateLowerBounds();
    }
    




   
}
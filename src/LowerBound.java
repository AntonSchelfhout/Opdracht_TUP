 // LOWERBOUNDS
    // HungarianAlgorithm ha = new HungarianAlgorithm(dataMatrix);
    // int[][] assignment = ha.findOptimalAssignment();

public class LowerBound implements Runnable{
    public int[][] solutions;       // matrix containing the values of solutions for the subproblems
    public int[][] lowerBounds;     // matrix containing the lower bounds for all pairs of rounds

    public int[] bestLowerBounds;   // array containing the best lower bounds for each startround

    public LowerBound() {
        this.solutions = new int[Main.nRounds][Main.nRounds];
        this.lowerBounds = new int[Main.nRounds][Main.nRounds];

        for (int i=0; i<=Main.nRounds; i++) {
            for (int j=0; j<=Main.nRounds; j++) {
                this.solutions[i][j] = 0;
                this.lowerBounds[i][j] = 0;
            }
        }
    }

    public void calculateLowerBounds() {
        // initial lower bounds for all pairs of rounds
        for (int r=Main.nRounds-2; r>=1; r--) {
            this.solutions[r][r+1] = hungarian(r, r+1);
            for (int r2=r+1; r2<Main.nRounds; r2++) {
                this.lowerBounds[r][r2] = solutions[r][r+1] + lowerBounds[r+1][r2];
            }
        }




        // subproblems with more than two rounds
    }

    @Override
    public void run() {

    }
    


   
}
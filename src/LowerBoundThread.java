import java.util.List;

public class LowerBoundThread implements Runnable {
    private int r;
    private int k;
    private int[][] solutions;
    private int[][] lowerBounds;

    private LowerBound lb;

    private Problem problem;
    int stopround;

    public LowerBoundThread(LowerBound lb, int r, int k, int[][] solutions, int[][] lowerBounds, Problem problem, int stopround) {
        this.lb = lb;
        this.r = r;
        this.k = k;
        this.solutions = solutions;
        this.lowerBounds = lowerBounds;
        this.problem = problem;
        this.stopround = stopround;
    }

    @Override
    public void run() {
        while (r >= stopround) {
            for (int r0 = r + k - 2; r0 >= r; r0--) {
                if (solutions[r0][r + k] != 0) continue;

                // Get subset of rounds and matches
                List<Round> roundsSubset = problem.rounds.subList(r0, r + k + 1);
                List<Match> matchSubset = problem.matches.subList(r0 * Main.n, (r + k + 1) * Main.n);
                Problem problemSubset = problem.cloneSubset(roundsSubset, matchSubset);

                // Create a new FastBranchAndBound task
                FastBranchAndBound branchAndBound = new FastBranchAndBound(lb, r0, r + k, problemSubset);
                branchAndBound.run();

                solutions[r0][r + k] = branchAndBound.getTotalDistance();
                for (int r1 = r0; r1 >= 0; r1--) {
                    for (int r2 = r + k; r2 < Main.nRounds; r2++) {
                        lowerBounds[r1][r2] = Math.max(lowerBounds[r1][r2], lowerBounds[r1][r0] + solutions[r0][r + k] + lowerBounds[r + k][r2]);
                    }
                }
            }
            r -= k;
        }
    }
}


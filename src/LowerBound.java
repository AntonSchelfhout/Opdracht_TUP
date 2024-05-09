 // LOWERBOUNDS
    // HungarianAlgorithm ha = new HungarianAlgorithm(dataMatrix);
    // int[][] assignment = ha.findOptimalAssignment();

import java.util.ArrayList;
import java.util.List;

public class LowerBound implements Runnable{
    public int[][] solutions;       // matrix containing the values of solutions for the subproblems
    public int[][] lowerBounds;     // matrix containing the lower bounds for all pairs of rounds

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

    public int[][] createMatrix(int startRound) {       // matrix with the length from each match to other matches in the next round
        int[][] matrix = new int[Main.n][Main.n];
        for(int i = 0; i < Main.n; i++) {
            for (int j = 0; j < Main.n; j++) {
                Match matchRound1 = Main.matches.get(Main.n*startRound+i);
                Match matchRound2 = Main.matches.get(Main.n*(startRound+1)+j);
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
            Team teamRound1 = Main.matches.get(Main.n*startRound + matrix[i][0]).homeTeam;
            Team teamRound2 = Main.matches.get(Main.n*(startRound+1) + matrix[i][1]).homeTeam;
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

        // calculate lower bounds for bigger subproblems
        for(int k = 2; k < Main.nRounds; k++) {         // size of the subproblem+1
            //System.out.println("==== k = " + k + " ====");
            int r = Main.nRounds - 1 - k;                   // start round
            while(r >= 1) {
                //System.out.println("r = " + r);
                for(int r0 = r + k - 2; r0 >= r; r0--) {
                    if(solutions[r0][r+k] != 0) continue;

                    // reset();

                    FastBranchAndBound branchAndBound = new FastBranchAndBound(this, r0, rounds, matches, umpires, teams);
                    branchAndBound.branch(0, r0);
                    solutions[r0][r+k] = branchAndBound.getTotalDistance();

                    for(int r1=r0; r1>=0; r1--) {
                        for (int r2=r+k; r2<Main.nRounds; r2++) {
                            lowerBounds[r1][r2] = Math.max(lowerBounds[r1][r2], lowerBounds[r1][r0] + solutions[r0][r+k] + lowerBounds[r+k][r2]);
                        }
                    }
                }
                r -= k;
            }  
        }

        // DEBUG print matrix
        // for(int i=0; i<Main.nRounds; i++) {
        //     for (int j=0; j<Main.nRounds; j++) {
        //         System.out.print(lowerBounds[i][j] + "    ");
        //     }
        //     System.out.println(" ");
        // }
    }

    @Override
    public void run() {
        this.calculateLowerBounds();
    }

    // public void reset() {
    //     // Reset all umpires
    //     for(Umpire u: umpires) {
    //         u.matches = new ArrayList<>();
    //         u.visitedTeams = new int[Main.nTeams];
    //     }
    // }
}
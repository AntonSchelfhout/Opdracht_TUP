import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.net.MalformedURLException;

public class LocalSearch implements Runnable {
    private List<Umpire> bestSolution;
    private Problem problem;
    private int violationPenalty;
    private int currentDistance;
    private ConcurrentLinkedQueue<Solution> queue;

    LocalSearch(List<Umpire> s, Problem p, int c, ConcurrentLinkedQueue<Solution> q) {
        this.bestSolution = s;
        this.problem = p;
        this.violationPenalty = 1000;
        this.currentDistance = c;
        this.queue = q;
    }

    public List<Umpire> getBestSolution() {
        return this.bestSolution;
    }


    public void search() {
        boolean improvement = true;
        boolean newSolution = false;
        int currenCost = currentDistance;
        
        while (improvement) {
            improvement = false;
            ArrayList<List<Umpire>> neighborhood = generateNeighborhood();
            // int currenCost = calculateCost(bestSolution);

            for (List<Umpire> neighbour: neighborhood) {
                int newCost = calculateDistance(neighbour);
            
                if (newCost < currenCost) {
                    this.bestSolution = neighbour;
                    currenCost = newCost;
                    improvement = true;
                    newSolution = true;
                }
            }
            currenCost = calculateDistance(bestSolution);
        }
        if (newSolution) {
            Solution solution = new Solution(this.bestSolution, currenCost);
            this.queue.add(solution);
        }
    }


    public ArrayList<List<Umpire>> generateNeighborhood() {
        ArrayList<List<Umpire>> neighborhood = new ArrayList<>();
        for (Umpire umpire: problem.umpires) {
            List<Match> currentMatches = umpire.matches;

            for (int i=0; i<currentMatches.size(); i++) {

                for (Umpire otherUmpire: problem.umpires) {
                    if (umpire.id != otherUmpire.id) {
                        // Deep copy because every assignment needs it own matches list to perform swaps on it
                        List<Umpire> newAssignment = deepCopy();
                        Match tempMatch = otherUmpire.matches.get(i);

                        // VisitedTeams changes because of swap
                        newAssignment.get(otherUmpire.id).visitedTeams[tempMatch.homeTeam.teamId] -= 1;
                        newAssignment.get(otherUmpire.id).visitedTeams[currentMatches.get(i).homeTeam.teamId] += 1;
                        newAssignment.get(umpire.id).visitedTeams[tempMatch.homeTeam.teamId] += 1;
                        newAssignment.get(umpire.id).visitedTeams[currentMatches.get(i).homeTeam.teamId] -= 1;

                        if (newAssignment.get(otherUmpire.id).getTeamsNotVisited() > 0 || newAssignment.get(umpire.id).getTeamsNotVisited() > 0) {
                            continue;
                        }

                        if (!checkConstraints(newAssignment)) {
                            continue;
                        }

                        // Swap
                        newAssignment.get(umpire.id).matches.set(i, tempMatch);
                        newAssignment.get(otherUmpire.id).matches.set(i, currentMatches.get(i));
                        neighborhood.add(newAssignment);
                    }
                }
            }
        }
        return neighborhood;
    }


    public List<Umpire> deepCopy() {
        List<Umpire> copy = new ArrayList<>();
        for (Umpire u: bestSolution) {
            Umpire copyUmpire = new Umpire(u, false);
            for (Match m: u.matches) {
                copyUmpire.matches.add(m);
            }
            copy.add(copyUmpire);
        } 
        return copy;
    }


    public int calculateDistance(List<Umpire> solution) {
        int distance = 0;
        for (Umpire umpire: solution) {
            for (int i=1; i<umpire.matches.size(); i++) {
                Match currentMatch = umpire.matches.get(i);
                Match prevMatch = umpire.matches.get(i - 1);

                distance += Main.dist[prevMatch.homeTeam.teamId][currentMatch.homeTeam.teamId];
            }
        }
        return distance;
    }


    public boolean checkConstraints(List<Umpire> assignment) {
        boolean feasible = true;

        for (Umpire u: assignment) {
            feasible = u.checkAllVisited();
        }

        if (!feasible) return feasible;

        for (Umpire u : assignment) {
            for(int i = 0; i < Main.nRounds; i++) {
                Match m1 = u.matches.get(i);

                for(int j = i + 1; j < Main.q1 - 1; j++) {
                    Match m2 = u.matches.get(j);

                    if(m1 == m2) continue;

                    if(m1.homeTeam.teamId == m2.homeTeam.teamId) {
                        feasible = false;
                        // System.out.println("ERROR: Umpire " + u.id + " visits same location in Q1 in round " + i);
                        break;
                        
                    }
                }
            }
        }

        if (!feasible) return feasible;

        // Check Q2 constraint, teams not visited 
        for(Umpire u : assignment) {
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
                        feasible = false;
                        break;
                    }
                }
            }
        }
        return feasible;
    }


    @Override
    public void run() {
        search();
    }


    public void tempCheck(List<Umpire> solutions){
        // Generating matrix
        int[][] formattedSolution = new int[Main.nRounds][Main.n];
        for(Umpire u: solutions){
            for(Match m: u.matches){
                formattedSolution[m.round][m.index] = u.id;
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

    }
    
}

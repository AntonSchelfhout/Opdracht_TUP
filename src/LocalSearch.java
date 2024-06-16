import java.util.ArrayList;
import java.util.List;

public class LocalSearch {
    private List<Umpire> bestSolution;
    private Problem problem;

    LocalSearch(List<Umpire> s, Problem p) {
        this.bestSolution = s;
        this.problem = p;
        
    }


    public ArrayList<List<Umpire>> generateNeighborhood() {
        boolean print = true;
        

        ArrayList<List<Umpire>> neighborhood = new ArrayList<>();
        for (Umpire umpire: problem.umpires) {
            List<Match> currentMatches = umpire.matches;

            for (int i=0; i<currentMatches.size(); i++) {

                for (Umpire otherUmpire: problem.umpires) {
                    if (umpire.id != otherUmpire.id) {
                        
                        List<Umpire> newAssignment = deepCopy();

                        Match tempMatch = otherUmpire.matches.get(i);
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
            Umpire copyUmpire = new Umpire(u.id);
            for (Match m: u.matches) {
                copyUmpire.matches.add(m);
            }
            copy.add(copyUmpire);
        } 
        return copy;
    }


    private List<Umpire> copySolution(List<Umpire> solution) {
        List<Umpire> copy = new ArrayList<>();
        for (Umpire umpire : solution) {
            Umpire newUmpire = new Umpire(umpire.id);
            newUmpire.matches = new ArrayList<>(umpire.matches);
            copy.add(newUmpire);
        }
        return copy;
    }


    public void search() {
        boolean improvement = true;
        
        
        while (improvement) {
            improvement = false;
            generateNeighborhood();
        }
    }
    
}

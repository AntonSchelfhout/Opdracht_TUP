// class with all the operations for the branch and bound
public class BranchAndBound {
    int upperBound = Integer.MAX_VALUE;
    static int currentDistance = 0;

    public void branchAndBound(int umpireIndex, int ronde) {
        Umpire currentUmpire = Main.umpires.get(umpireIndex);
        int nextUmpire = umpireIndex++ % Main.n;

        int nextRonde = ronde;
        if (umpireIndex == Main.n) nextRonde++;

        for(Match match: Main.matches.get(ronde)) {
            if (currentUmpire.isFeasibleC1(match) && currentUmpire.isFeasibleC2(match)) {
                // upper bound checken:
                if (currentDistance > upperBound) {
                    currentUmpire.assignMatch(match);


                    if (!isComplete(umpireIndex, ronde)) {
                        branchAndBound(nextUmpire, nextRonde);
                    }
                }

            }
        }
    }

    public boolean isComplete(int umpire, int ronde) {
        boolean complete = false;
        if (ronde == Main.nRounds-- && umpire == Main.n-1) {
            complete = true;
        }
        return complete;

    }
}

import java.net.MalformedURLException;

// class with all the operations for the branch and bound
public class BranchAndBound {
    int upperBound = Integer.MAX_VALUE;
    static int currentDistance = 0;

    public void branchAndBound(int umpireIndex, int ronde) {
        Umpire current = Main.umpires.get(umpireIndex);
        int next = umpireIndex+1;
        if (next == Main.n) next = 0;

        int nextRonde = ronde;
        if (next == 0) nextRonde += 1;

        for (Match m: Main.matches.get(ronde)) {
            if (currentDistance < upperBound) {
                if (!m.isAsigned) {
                    if (current.isFeasibleC1(m) && current.isFeasibleC2(m)) {
                        current.assignMatch(m);
                        if (umpireIndex == Main.n - 1 && ronde == Main.nRounds - 1)
                            // save solution
                            return;     // later vervangen met local search
                        else branchAndBound(next, nextRonde);
                        // nog unassingen
                        current.unAssignMatch(m);
                    }
                }
            }
        }
    }

//    public void branchAndBound(int umpireIndex, int ronde) {
//        Umpire currentUmpire = Main.umpires.get(umpireIndex);
//        int nextUmpire = umpireIndex+1;
//        if (nextUmpire == 4) nextUmpire = 0;
//
////        System.out.println("umpire " + umpireIndex + " ronde: " + ronde);
////        System.out.println("next: " + nextUmpire);
//
//        int nextRonde = ronde;
//        if (umpireIndex == Main.n-1) nextRonde += 1;
//
//        for(Match match: Main.matches.get(ronde)) {
//            if (currentUmpire.isFeasibleC1(match) && currentUmpire.isFeasibleC2(match)) {
//                // Upper bound checken:
//                if (currentDistance < upperBound) {
//                    currentUmpire.assignMatch(match);
//
//                    if (!isComplete(umpireIndex, ronde)) {
//                        branchAndBound(nextUmpire, nextRonde);
//                        if (nextRonde == Main.nRounds) return;
//                    }
//                    else {
//                        System.out.printf("BREAK");
//                        return;
//                    }
////                    else {
////                        // Local search
////                    }
////                    currentUmpire.unAssignMatch(match);
//                }
//
//            }
//        }
//    }

    public boolean isComplete(int umpire, int ronde) {
        boolean complete = false;
        if (ronde == Main.nRounds-1 && umpire == Main.n-1) {
            complete = true;
        }
        return complete;

    }
}

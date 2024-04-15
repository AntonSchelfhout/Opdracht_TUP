import java.io.*;
import java.util.*;
public class Main {
    // https://benchmark.gent.cs.kuleuven.be/tup/en/results/
    static int q1 = 5;
    static int q2 = 2;
    static int n;
    static int nTeams;
    static int nRounds;
    static int[][] dist;

    static List<Round> rounds = new ArrayList<>();
    static List<Match> matches = new ArrayList<>();
    static List<Umpire> umpires = new ArrayList<>();

    public static void main(String[] args) throws FileNotFoundException {
        readFile("umps10");

        // CALCULATE ALL DINSTANCES

        // SORT NODES ON DISTANCE

        // FIX EERSTE RONDE

        // Branch and bound
        BranchAndBound bb = new BranchAndBound();
        bb.branchAndBound(0);

        // Print output
        printOutput(bb);
    }

    public static void printOutput(BranchAndBound branchAndBound) {
        System.out.println("--------------------");
        System.out.println(branchAndBound.currentDistance);
        System.out.println("--------------------");
        for(Round r: rounds){
            System.out.println("Round " + r.index);
            for(Match m: r.matches){
                System.out.println("\t* ("+ m.homeTeam.teamId + " - " + m.outTeam .teamId+") => " + m.umpire);
            }
        }
    }

    public static void readFile(String file) throws FileNotFoundException{
        Scanner sc = new Scanner(new File("Input/" + file + ".txt"));
        
        // Read the nTeams line
        while(sc.hasNextLine()){
            String line = sc.nextLine();
            if(line.trim().contains("nTeams")){
                nTeams = Integer.parseInt(line.split("=")[1].trim().replace(";",""));
                n = nTeams/2;
                nRounds = 2*nTeams-2;
                dist = new int[nTeams][nTeams];
                break;
            }
        }

        // Read the dist matrix
        while(sc.hasNextLine()){
            String line = sc.nextLine();
            if(line.contains("dist= [")){
                for(int i = 0; i < nTeams; i++) {
                    String[] roundLine = sc.nextLine().replace("[","").replace("]","").split("\\s+");
                    for(int j = 1; j <= nTeams; j++) {
                        dist[i][j-1] = Integer.parseInt(roundLine[j]);
                    }
                }
                break;
            }
        }

        // Create all the teams
        List<Team> teams = new ArrayList<>();
        for (int i=0; i<nTeams; i++) {
            Team t = new Team(i);
            teams.add(t);
        }

        // Create all the umpires
        for(int i = 0; i < nTeams / 2; i++){
            Umpire u = new Umpire(i);
            umpires.add(u);
        }

        // Read all the rounds/matches
        matches = new ArrayList<>();
        while(sc.hasNextLine()) {
            String line = sc.nextLine();
            if(line.contains("opponents=[")){

                for(int round = 0; round < nRounds; round++) {
                    // Get the round input line and trim all useless characters
                    String[] roundLine = sc.nextLine().replace("[","").replace("]","").split("\\s+");

                    // Create a list of matches for this round (skip the returning negative matches)
                    List<Match> roundMatches = new ArrayList<>();
                    for (int i = 0; i < roundLine.length; i++) {
                        int o = Integer.parseInt(roundLine[i]);
                        if(o < 0) continue;

                        Match m = new Match(round, teams.get(i), teams.get(o - 1), i);
                        roundMatches.add(m);
                        matches.add(m);
                    }
                    
                    // Create a round object and add it to the list of rounds
                    Round r = new Round(round, roundMatches);
                    rounds.add(r);
                }
                break;
            }
        }

        //Debug print the rounds
        for (Round r: rounds) {
                System.out.println(r.toString());
        }

        sc.close();
    }
}


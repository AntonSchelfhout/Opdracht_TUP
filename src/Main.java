import java.io.*;
import java.util.*;
public class Main {
    static int q1;
    static int q2;
    static int n;
    static int nTeams;
    static int nRounds;
    static int[][] dist;
    static List<List<Match>> matches = new ArrayList<>();
    static List<Umpire> umpires = new ArrayList<>();
    public static void main(String[] args) throws FileNotFoundException {
        Scanner sc = new Scanner(new File("Input/umps8.txt"));
        
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

        for (int i = 0; i < nRounds; i++) {
            List<Match> matcherForRound = new ArrayList<>();
            matches.add(matcherForRound);
        }

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

        for (int i=0; i<nTeams;i++) {
            for (int j=0; j<nTeams; j++) {
                System.out.printf("%d, ", dist[i][j]);
            }
            System.out.println(" ");
        }

        List<Team> teams = new ArrayList<>();
        for (int i=0; i<nTeams; i++) {
            Team t = new Team(i);
            teams.add(t);
        }

        while(sc.hasNextLine()) {
            String line = sc.nextLine();
            if(line.contains("opponents=[")){
                for(int ronde = 0; ronde < nRounds; ronde++) {
                    String[] roundLine = sc.nextLine().replace("[","").replace("]","").split("\\s+");
                    System.out.println(roundLine.toString());
                    for (int i = 1; i < roundLine.length; i++) {
                        int o = Integer.parseInt(roundLine[i]);
                        if(o < 0) continue;

                        Match m = new Match(ronde,teams.get(i-1), teams.get(Math.abs(o) -1));
                        matches.get(ronde).add(m);
                    }
                }
                break;
            }
        }



        for(int i = 0; i < n; i++){
            Umpire u = new Umpire(i);
            umpires.add(u);
        }

        for(int i=0; i < nRounds; i++){
            System.out.println("Ronde " + i);
            for (Match m: matches.get(i)) {
                System.out.println(m.toString());
            }
        }

        sc.close();

        // Now the variables nTeams, dist, and opponents hold your data
    }
}


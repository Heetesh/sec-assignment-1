package sec.assignment.app.controller;

import java.util.concurrent.ExecutorService;

public class LCSComparison {
//    private ExecutorService executor;
    public LCSComparison(/*ExecutorService executor*/) {
//        this.executor = executor; // Assign thread pool

    }

    public double calSimilarity(char[] file1 , char[] file2)
    {

        int [][]subsolution = new int[file1.length+1][file2.length+1];
        boolean [][] direction = new boolean[file1.length+1][file2.length+1];

        for(int i = 0;i < subsolution.length; i++)
        {
            subsolution[i][0]= 0;
        }
        for(int i = 0;i < subsolution[0].length; i++)
        {
            subsolution[0][i]= 0;
        }

        for(int i = 1; i <= file1.length; i++) {
            for(int j = 1; j <= file2.length; j++) {
                if(file1[i-1] == file2[j-1]) {
                    subsolution[i][j]= subsolution[i-1][j-1] +1;
                }
                else if(subsolution[i-1][j] > subsolution[i][j-1] ) {
                    subsolution[i][j] = subsolution[i-1][j];
                    direction[i][j] = true;
                }
                else {
                    subsolution[i][j] = subsolution[i][j-1];
                    direction[i][j] = false;
                }

            }
        }
        int match = 0;
        int i = file1.length;
        int j = file2.length;
        while (i>0 && j>0)
        {
            if(file1[i-1] == file2[j-1])
            {
                match += 1;
                i-=1;
                j-=1;
            }else if(direction[i][j])
            {
                i-=1;
            }else
            {
                j-=1;
            }
        }


        return (double) ( match*2)/ (double) (file1.length+ (double) file2.length);
    }
}

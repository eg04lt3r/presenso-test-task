package com.eg0.matrix.multiplier;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Aleksey Kurochka
 * @version 1.0
 */
public class MatrixTest {

    public static void main(String[] args) throws IOException {
        int[][] a = new int[][] {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };
        int[][] b = new int[][] {
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };



        /*for (int i = 0; i < a.length; i++) {
            for (int c = 0; c < a.length; c++) {
                a[i][c] = 1;
                b[i][c] = 1;
            }
        }*/

        long start = System.nanoTime() / 1000000;
        int[][] result = multiply(a, b);
        long stop = System.nanoTime() / 1000000;

        System.out.printf("Spent time: %dms%n", (stop - start));
        System.out.println(Arrays.deepToString(result));

    }

    public static int[][] multiply(int[][] a, int[][] b) {
        return SquareMatrixMultiplier
                .create()
                .parallel()
                .multiply(a, b);
    }
}

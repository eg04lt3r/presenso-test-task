package com.eg0.matrix.multiplier;

/**
 * Base interface provides ability to multiply 2 matrices.
 * @author Aleksey Kurochka
 * @version 1.0
 */
public interface MatrixMultiplier {

    /**
     * Multiplies 2 matrices according to standard rules.
     * @param a source matrix
     * @param b target matrix
     * @return result matrix
     */
    int[][] multiply(int[][] a, int[][] b);

    /**
     * Creates multiplier which can multiply matrices concurrently.
     * @param nThreads concurrency level
     */
    MatrixMultiplier parallel(int nThreads);

    default MatrixMultiplier parallel() {
        return parallel(Runtime.getRuntime().availableProcessors());
    }
}

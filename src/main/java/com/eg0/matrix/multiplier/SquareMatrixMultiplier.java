package com.eg0.matrix.multiplier;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.annotations.VisibleForTesting;

/**
 * Matrix multiplier which can multiply only squared matrices.
 * <p>
 *     Square matrix definition <a href="https://en.wikipedia.org/wiki/Square_matrix">Square Matrix</a>.
 * </p>
 * @author Aleksey Kurochka
 * @version 1.0
 */
public class SquareMatrixMultiplier implements MatrixMultiplier {

    /**
     * Hide ctor from explicit instantiation.
     */
    private SquareMatrixMultiplier() {}

    @Override
    public int[][] multiply(int[][] a, int[][] b) {
        checkMatrices(a, b);

        final int commonLength = a.length;
        final int[][] result = new int[commonLength][commonLength];

        multiply(a, b, result, 0, commonLength);

        return result;
    }

    @Override
    public MatrixMultiplier parallel(int nThreads) {
        return new SquareMatrixParallelMultiplier(nThreads);
    }

    /**
     * Creates squared matrices sequential multiplier.
     */
    public static MatrixMultiplier create() {
        return new SquareMatrixMultiplier();
    }

    /**
     * Checks that matrix is squared, if not - throws exception with <tt>errorMsg</tt>
     * @param matrix source matrix to be checked
     * @param errorMsg error message
     * @throws IllegalArgumentException if matrix is not squared
     */
    @VisibleForTesting
    protected static void checkMatrixSquare(int[][] matrix, String errorMsg) {
        for (int[] row : matrix) {
            if (row.length != matrix.length) {
                throw new IllegalArgumentException(errorMsg);
            }
        }
    }

    /**
     * Ensures that both matrices have the same length.
     * @param a source matrix
     * @param b target matrix
     * @throws IllegalArgumentException if matrices have diff length
     */
    @VisibleForTesting
    protected static void checkMatricesEqualsLength(int[][] a, int[][] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Both matrices should have the same length");
        }
    }

    /**
     * Applies multiple checks for both matrices.
     * <p>
     *     This check is mandatory before applying multiplication operation.
     * </p>
     * @param a source matrix
     * @param b target matrix
     */
    @VisibleForTesting
    protected static void checkMatrices(int[][] a, int[][] b) {
        Objects.requireNonNull(a, "'a' matrix is null");
        Objects.requireNonNull(b, "'b' matrix is null");
        checkMatrixSquare(a, "'a' matrix is not squared");
        checkMatrixSquare(b, "'b' matrix is not squared");
        checkMatricesEqualsLength(a, b);
    }

    /**
     * Simple matrices multiplication using 3 for loops.
     * <p>
     *     In 'res' matrix will be filled rows in range ['from', 'to'].
     * </p>
     * @param a squared matrix
     * @param b squared matrix
     * @param res result matrix
     * @param from start row index of the 'a' matrix
     * @param to end row index
     */
    //TODO(aleksey) Can be moved outside this class, left for simplicity
    @VisibleForTesting
    protected static void multiply(int[][] a, int[][]b, int[][] res, int from, int to) {
        for (int i = from; i < to; i++) {
            for (int k = 0; k < a.length; k++) {
                for (int j = 0; j < a.length; j++) {
                    res[i][j] += a[i][k] * b[k][j];
                }
            }
        }
    }

    /**
     * Provides ability to multiply 2 matrices in parallel.
     */
    private static class SquareMatrixParallelMultiplier implements MatrixMultiplier {
        private final int nThreads;

        /**
         * Main ctor.
         * @param threads concurrency level
         * @throws IllegalArgumentException if 'threads' is <= 0
         */
        public SquareMatrixParallelMultiplier(int threads) {
            if (threads <= 0) {
                throw new IllegalArgumentException("'threads' should be > 0");
            }
            this.nThreads = threads;
        }

        @Override
        public int[][] multiply(int[][] a, int[][] b) {
            checkMatrices(a, b);

            final int batchSize = (int) Math.round(a.length * 1.0 / nThreads);
            // adjust number of threads required for parallel computation
            // if has at least one batch we can use effectively all threads, otherwise
            // can decrease this number to the length of the matrix
            final int parThreadsNum = batchSize < 1 ? a.length : nThreads;

                return multiplyInParallel(a, b, Math.max(batchSize, 1), parThreadsNum);
        }

        @Override
        public MatrixMultiplier parallel(int nThreads) {
            return new SquareMatrixParallelMultiplier(nThreads);
        }

        private int[][] multiplyInParallel(int[][]a, int[][] b, int batchSize, int threadsNum) {

            final ExecutorService executor = Executors.newFixedThreadPool(threadsNum);
            final List<CompletableFuture<?>> computations = new LinkedList<>();

            final int[][] result = new int[a.length][a.length];
            for (int i = 0; i < a.length; i += batchSize) {
                final int from = i;
                final int to = Math.min(a.length, from + batchSize);
                // run async sequential computations
                computations.add(CompletableFuture.runAsync(() ->
                    SquareMatrixMultiplier.multiply(a, b, result, from, to), executor)
                );
            }

            try {
                // waiting until all async operations performed
                CompletableFuture
                    .allOf(computations.toArray(new CompletableFuture[computations.size()]))
                    .join();

                return result;
            } finally {
                executor.shutdown();
            }
        }
    }
}

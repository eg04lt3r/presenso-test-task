package com.eg0.matrix.multiplier;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Aleksey Kurochka
 * @version 1.0
 */
//TODO(me): complete tests with all test cases, currently covered only main
public class SquareMatrixMultiplierTest {

    int[][] sourceMatrix = new int[][] {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
    };

    int[][] targetMatrix = new int[][] {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
    };

    int[][] expectedMatrix = new int[][] {
            {30, 36, 42},
            {66, 81, 96},
            {102, 126, 150}
    };

    @Test
    public void multiplyShouldReturnMultiplicationMatrix() {


        int[][] result = SquareMatrixMultiplier.create().multiply(sourceMatrix, targetMatrix);

        assertEquals(expectedMatrix.length, result.length);

        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result.length; j++) {
                assertEquals(expectedMatrix[i][j], result[i][j]);
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfSourceMatrixIsNotSquared() {
        SquareMatrixMultiplier.create().multiply(new int[1][2], new int[3][3]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfTargetMatrixIsNotSquared() {
        SquareMatrixMultiplier.create().multiply(new int[1][1], new int[2][3]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfMatricesHaveDiffLength() {
        SquareMatrixMultiplier.create().multiply(new int[2][2], new int[3][3]);
    }

    @Test
    public void multiplyShouldReturnMultiplicationMatrixInParallel() {
        int[][] result = SquareMatrixMultiplier.create()
                .parallel()
                .multiply(sourceMatrix, targetMatrix);

        assertEquals(expectedMatrix.length, result.length);

        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result.length; j++) {
                assertEquals(expectedMatrix[i][j], result[i][j]);
            }
        }
    }

    /**
     * Using 'dirty' benchmark testing seq vs parallel matrices multiplication.
     * <p>
     *     Anyway with enough matrices size and available CPU cores > 1 always should be passed.
     * </p>
     */
    @Test
    public void parallelMultiplyShouldSpendsLessTimeThanSequential() {

        // test should be run if more than 1 processor available
        if (Runtime.getRuntime().availableProcessors() > 1) {
            int[][] a = new int[1024][1024];
            int[][] b = new int[1024][1024];

            final MatrixMultiplier sequentialMultiplier = SquareMatrixMultiplier.create();
            final long seqTime = timedOp(() -> sequentialMultiplier.multiply(a, b));

            final MatrixMultiplier parallelMultiplier = sequentialMultiplier.parallel();
            final long parallelTime = timedOp(() -> parallelMultiplier.multiply(a, b));

            assertTrue("'parallelTime' exceeds 'seqTime'", parallelTime < seqTime);
        }
    }

    /**
     * Measure <tt>op</tt> execution time in nano seconds.
     * @param op operation to be measured
     * @return execution time in nano seconds
     */
    private long timedOp(Runnable op) {
        final long start = System.nanoTime();
        op.run();
        final long end = System.nanoTime();

        return (end - start);
    }
}
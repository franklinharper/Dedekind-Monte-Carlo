package com.franklinharper;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DedekindMonteCarloTest {

    @Test
    public void testGenerateNTuplesOfRank_K() {
        assertArrayEquals( new int[] { 1 }, DedekindMonteCarlo.generateNTuplesOfRank_K( 1, 1 ) );
        assertArrayEquals( new int[] { 1, 2 }, DedekindMonteCarlo.generateNTuplesOfRank_K( 2, 1 ) );
        assertArrayEquals( new int[] { 0 }, DedekindMonteCarlo.generateNTuplesOfRank_K( 2, 0 ) );
        assertArrayEquals( new int[] { 3, 5, 6 }, DedekindMonteCarlo.generateNTuplesOfRank_K( 3, 2 ) );
    }

    @Test
    public void testAllPredecessorsAreInSample() {
        {
            int n = 4;
            int successor = 11; // 1 above middle is rank 3
                                // predecessors of 11 are 3, 9, 10
            int[] sample = new int[] { 3, 9, 10 }; // => middle rank k is 2
            assertTrue( DedekindMonteCarlo.allPredecessorsAreInSample( successor, n, sample ) );
        }
        {
            int n = 4;
            int successor = 7;  // 1 above middle is rank 3
                                // predecessors of 7 are 3, 5, 6
            int[] sample = new int[] { 3, 5, 6, 9, 10 }; // => middle rank k is 2
            assertTrue( DedekindMonteCarlo.allPredecessorsAreInSample( successor, n, sample ) );
        }
        {
            int n = 4;
            int successor = 11; // 1 above middle is rank 3
                                // predecessors of 11 are 3, 9, 10
            int[] sample = new int[] { 3, 5, 9 }; // => middle rank k is 2
                                                  // largest predecessor is missing
            assertFalse( DedekindMonteCarlo.allPredecessorsAreInSample( successor, n, sample ) );
        }
        {
            int n = 4;
            int successor = 11; // 1 above middle is rank 3
                                // predecessors of 11 are 3, 9, 10
            int[] sample = new int[] { 5, 9, 10 };  // => middle rank k is 2
                                                    // smallest predecessor is missing
            assertFalse( DedekindMonteCarlo.allPredecessorsAreInSample( successor, n, sample ) );
        }
    }

    @Test
    public void testCalculateX() {
        {
            int n = 3; // => k = 1
            int[] aboveSample = new int[] { 3, 6 }; // rank 2
                                                    // all predecessors of 3 are in the sample
                                                    // 6 is missing a predecessor (4)
            int[] sample = new int[] { 1, 2 }; // rank 1
            assertEquals( 1, DedekindMonteCarlo.calculateX( n, aboveSample, sample ) );
        }
        {
            int n = 4; // => k = 2
            int[] aboveSample = new int[] { 7, 14 }; // rank 3: minimum and maximum elements
            int[] sample = new int[] { 3, 5, 6, 10, 12 }; // rank 2
            assertEquals( 2, DedekindMonteCarlo.calculateX( n, aboveSample, sample ) );
        }
    }

    @Test
    public void testNoneOfSuccessorsAreInSample() {
        {
            int n = 4; // k => 2
            int predecessor = 1; // Minimal element of rank 1
                               // successors of 1 are 3, 5, 9
            int[] sample = new int[] { 6, 12 }; // => middle rank contains elements of rank 2
            assertTrue( DedekindMonteCarlo.noneOfSuccessorsAreInSample( predecessor, n, sample ) );
        }
        {
            int n = 4; // k => 2
            int predecessor = 1; // Minimal element of rank 1
                               // successors of 1 are 3, 5, 9
            int[] sample = new int[] { 6, 9 }; // => middle rank contains elements of rank 2
            assertFalse( DedekindMonteCarlo.noneOfSuccessorsAreInSample( predecessor, n, sample ) );
        }

        {
            int n = 7; // k => 3
            int predecessor = 96; // Maximal element of rank 2
                                  // successors of 96 are 97, 99, 100, 104, 112,
            int[] sample = new int[] { 3, 5, 6, 9, 10, 12, 24 }; // => middle rank contains elements of rank 3
            assertTrue( DedekindMonteCarlo.noneOfSuccessorsAreInSample( predecessor, n, sample ) );
        }

        {
            int n = 7; // k => 3
            int predecessor = 96; // Maximal element of rank 2
                                  // successors of 96 are 97, 99, 100, 104, 112,
            int[] sample = new int[] { 3, 5, 6, 9, 10, 12, 24, 112 }; // => middle rank contains elements of rank 3
            assertFalse( DedekindMonteCarlo.noneOfSuccessorsAreInSample( predecessor, n, sample ) );
        }
        {
            int n = 7; // k => 3
            int predecessor = 96; // Maximal element of rank 2
                                  // successors of 96 are 97, 99, 100, 104, 112,
            int[] sample = new int[] { 3, 5, 6, 9, 10, 12, 24, 97 }; // => middle rank contains elements of rank 3
            assertFalse( DedekindMonteCarlo.noneOfSuccessorsAreInSample( predecessor, n, sample ) );
        }
    }

    @Test
    public void testCalculateY() {
        {
            int n = 4; // k => 2
            int[] belowMiddlePredecessors = new int[] { 1, 8 }; // minimum and maximum elements of rank 1
            // Successors of 1: 3, 5, 9
            // Successors of 8: 9, 10, 12
            int[] sample = new int[] { 3, 9 }; // rank 2
            assertEquals( 0, DedekindMonteCarlo.calculateY( n, belowMiddlePredecessors, sample ) );
        }
        {
            int n = 4; // k => 2
            int[] belowMiddlePredecessors = new int[] { 1, 8 }; // minimum and maximum elements of rank 1
            // Successors of 1: 3, 5, 9
            // Successors of 8: 9, 10, 12
            int[] sample = new int[] { 6, 10, 12 }; // rank 2
            assertEquals( 1, DedekindMonteCarlo.calculateY( n, belowMiddlePredecessors, sample ) );
        }
        {
            int n = 4; // k => 2
            int[] belowMiddlePredecessors = new int[] { 1, 8 }; // minimum and maximum elements of rank 1
            // Successors of 1: 3, 5, 9
            // Successors of 8: 9, 10, 12
            int[] sample = new int[] { 9, 12 }; // rank 2
            assertEquals( 0, DedekindMonteCarlo.calculateY( n, belowMiddlePredecessors, sample ) );
        }
        {
            int n = 4; // k => 2
            int[] belowMiddlePredecessors = new int[] { 1, 8 }; // minimum and maximum elements of rank 1
            // Successors of 1: 3, 5, 9
            // Successors of 8: 9, 10, 12
            int[] sample = new int[] { 6 }; // rank 2
            assertEquals( 2, DedekindMonteCarlo.calculateY( n, belowMiddlePredecessors, sample ) );
        }
    }

    @Test
    public void testBinomial() {
        assertEquals( 1, DedekindMonteCarlo.binomial( 1, 1 ) );
        assertEquals( 2, DedekindMonteCarlo.binomial( 2, 1 ) );
        assertEquals( 6435, DedekindMonteCarlo.binomial( 15, 8 ) );
    }

}

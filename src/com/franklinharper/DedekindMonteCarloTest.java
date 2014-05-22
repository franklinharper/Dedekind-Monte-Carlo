package com.franklinharper;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apfloat.Apfloat;
import org.apfloat.Apint;
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
            // => middle rank k is 2
            // 11 is of rank 3, which is 1 above the middle rank
            int successor = 11;
            // predecessors of 11 are 3, 9, 10
            Set<Integer> sample = new HashSet<Integer>(Arrays.asList(3, 9, 10));
            assertTrue( DedekindMonteCarlo.allPredecessorsAreInSample( successor, n, sample ) );
        }
        {
            int n = 4;
            // 7 is of rank 3, which is 1 above the middle rank
            int successor = 7;
            // predecessors of 7 are 3, 5, 6
            Set<Integer> sample = new HashSet<Integer>(Arrays.asList( 3, 5, 6, 9, 10));
            assertTrue( DedekindMonteCarlo.allPredecessorsAreInSample( successor, n, sample ) );
        }
        {
            int n = 4;
            int successor = 11;
            // The largest predecessor is missing.
            Set<Integer> sample = new HashSet<Integer>(Arrays.asList( 3, 5, 9));
            assertFalse( DedekindMonteCarlo.allPredecessorsAreInSample( successor, n, sample ) );
        }
        {
            int n = 4;
            int successor = 11;
            // The smallest predecessor is missing.
            Set< Integer > sample = new HashSet< Integer >( Arrays.asList( 5, 9, 10 ) );
            assertFalse( DedekindMonteCarlo.allPredecessorsAreInSample( successor, n, sample ) );
        }
    }

    @Test
    public void testCalculateX() {
        {
            int n = 3;
            // n-tuples of rank 2
            int[] aboveSample = new int[] { 3, 6 };
            // All predecessors of 3 are in the sample.
            // 6 is missing a predecessor (4).
            // n-tuples of rank 1
            Set< Integer > sample = new HashSet< Integer >( Arrays.asList( 1, 2 ) );
            assertEquals( 1, DedekindMonteCarlo.calculateX( n, aboveSample, sample ) );
        }
        {
            int n = 4; // => k = 2

            // n-tuples of rank 3, minimum and maximum elements
            int[] aboveSample = new int[] { 7, 14 };

            // n-tuples of rank 2
            Set< Integer > sample = new HashSet< Integer >( Arrays.asList(  3, 5, 6, 10, 12 ) );
            assertEquals( 2, DedekindMonteCarlo.calculateX( n, aboveSample, sample ) );
        }
    }

    @Test
    public void testNoneOfSuccessorsAreInSample() {
        {
            int n = 4;
            // The "middle" rank k is 2.
            // 1 is the Minimal element of rank 1
            int predecessor = 1;
            // successors of 1 are 3, 5, 9
            // => rank k contains elements of rank 2
            Set< Integer > sample = new HashSet< Integer >( Arrays.asList(  6, 12 ) );
            assertTrue( DedekindMonteCarlo.noneOfSuccessorsAreInSample( predecessor, n, sample ) );
        }
        {
            int n = 4;
            // The "middle" rank k is 2.
            // 1 is the Minimal element of rank 1
            int predecessor = 1;
            // The successors of 1 are 3, 5, 9
            // => rank k contains elements of rank 2
            Set< Integer > sample = new HashSet< Integer >( Arrays.asList( 6, 9 ) );
            assertFalse( DedekindMonteCarlo.noneOfSuccessorsAreInSample( predecessor, n, sample ) );
        }

        {
            int n = 7;
            // The "middle" rank k is 3.
            // 96 is the maximal element of rank 2.
            int predecessor = 96;
            // successors of 96 are 97, 99, 100, 104, 112,
            // => rank k contains elements of rank 2
            Set< Integer > sample = new HashSet< Integer >( Arrays.asList(  3, 5, 6, 9, 10, 12, 24 ) );
            assertTrue( DedekindMonteCarlo.noneOfSuccessorsAreInSample( predecessor, n, sample ) );
        }

        {
            int n = 7;
            // The "middle" rank k is 3.
            // 96 is the maximal element of rank 2.
            int predecessor = 96;
            // successors of 96 are 97, 99, 100, 104, 112,
            // => rank k contains elements of rank 2
            Set< Integer > sample = new HashSet< Integer >( Arrays.asList(  3, 5, 6, 9, 10, 12, 24, 112 ) );
            assertFalse( DedekindMonteCarlo.noneOfSuccessorsAreInSample( predecessor, n, sample ) );
        }
        {
            int n = 7;
            // The "middle" rank k is 3.
            // 96 is the maximal element of rank 2.
            int predecessor = 96;
            // successors of 96 are 97, 99, 100, 104, 112,
            // => rank k contains elements of rank 2
            Set< Integer > sample = new HashSet< Integer >( Arrays.asList( 3, 5, 6, 9, 10, 12, 24, 97 ) );
            assertFalse( DedekindMonteCarlo.noneOfSuccessorsAreInSample( predecessor, n, sample ) );
        }
    }

    @Test
    public void testCalculateY() {
        {
            int n = 4;
            // The "middle" rank k is 2.
            // The minimum and maximum elements of rank 1
            int[] belowMiddlePredecessors = new int[] { 1, 8 };
            // Successors of 1: 3, 5, 9
            // Successors of 8: 9, 10, 12
            Set< Integer > sample = new HashSet< Integer >( Arrays.asList( 3, 9 ) );
            assertEquals( 0, DedekindMonteCarlo.calculateY( n, belowMiddlePredecessors, sample ) );
        }
        {
            int n = 4;
            // The "middle" rank k is 2.
            // The minimum and maximum elements of rank 1
            int[] belowMiddlePredecessors = new int[] { 1, 8 };
            // Successors of 1: 3, 5, 9
            // Successors of 8: 9, 10, 12
            Set< Integer > sample = new HashSet< Integer >( Arrays.asList( 6, 10, 12 ) );
            assertEquals( 1, DedekindMonteCarlo.calculateY( n, belowMiddlePredecessors, sample ) );
        }
        {
            int n = 4;
            // The "middle" rank k is 2.
            // The minimum and maximum elements of rank 1
            int[] belowMiddlePredecessors = new int[] { 1, 8 };
            // Successors of 1: 3, 5, 9
            // Successors of 8: 9, 10, 12
            Set< Integer > sample = new HashSet< Integer >( Arrays.asList( 9, 12 ) );
            assertEquals( 0, DedekindMonteCarlo.calculateY( n, belowMiddlePredecessors, sample ) );
        }
        {
            int n = 4;
            // The minimum and maximum elements of rank 1
            int[] belowMiddlePredecessors = new int[] { 1, 8 };
            // Successors of 1: 3, 5, 9
            // Successors of 8: 9, 10, 12
            Set< Integer > sample = new HashSet< Integer >( Arrays.asList( 6 ) );
            assertEquals( 2, DedekindMonteCarlo.calculateY( n, belowMiddlePredecessors, sample ) );
        }
    }

    @Test
    public void testBinomial() {
        assertEquals( 1, DedekindMonteCarlo.binomial( 1, 1 ) );
        assertEquals( 2, DedekindMonteCarlo.binomial( 2, 1 ) );
        assertEquals( 6435, DedekindMonteCarlo.binomial( 15, 8 ) );
    }

    @Test
    public void testStandardDeviation() {
        {
            long[] samples = new long[] { 1, 2 };
            Apint multiplier = new Apint( 512 );
            Apfloat estimate = new Apfloat( 512 );
            Apfloat expected = new Apfloat( 512 );
            Apfloat tolerance = expected.multiply( new Apfloat( 0.0001 ) );
            checkResult( expected, tolerance, DedekindMonteCarlo.standardDeviation( samples, multiplier , estimate ) );
        }
        {
            Apfloat expected = new Apfloat( 1000 );
            Apfloat tolerance = expected.multiply( new Apfloat( 0.0001 ) );
            checkResult( expected, tolerance, DedekindMonteCarlo.standardDeviation( new long[] { 30, 31, 29}, new Apint( 1000 ), new Apfloat( 30000 ) ) );
        }
    }

    @Test
    public void test_an() {
        //   a(n) = (n choose (n/2 - 1)) * ( 2^(-n/2) + n^2*2^(-n-5) - n*2^(-n-4) )
        Apfloat expectedResults[] = {
            null,
            null,
            // For n = 2
            //   a(2) = ( 2 choose 0) * (2^-1 + 2^2*2^-7 - 2*2^-6)
            //   a(2) = 1 * (0.5 + 4*2^-7 - 2*2^-6)
            //   a(2) = 0.5 + 4*2^-7 - 2*2^-6
            //   a(2) = 0.5
            new Apfloat( "0.5" ),
            null,
            // For n = 4
            //   a(4) = (4 choose (4/2 - 1)) * ( 2^(-4/2) + 4^2*2^(-4-5) - 4*2^(-4-4) )
            //   a(4) = 1.0625
            new Apfloat( "1.0625" ),
        };
        Apfloat tolerance = new Apfloat( "10e-9" );
        for( int n = 0; n < expectedResults.length; n++ ) {
            Apfloat expected = expectedResults[ n ];
            if( expected != null ) {
                checkResult( expected, tolerance, DedekindMonteCarlo.a( n ) );
            }
        }
    }

    @Test
    public void test_bn() {
        // b(n) = (n choose ((n-3)/2)) * ( 2^( -(n+3)/2 ) + n^2*2^(-n-6) - n*2^(-n-3) )
        Apfloat expectedResults[] = {
            null,
            null,
            null,
            // b(3) = (3 choose ((3-3)/2)) * ( 2^( -(3+3)/2 ) + 3^2*2^(-3-6) - 3*2^(-3-3) )
            // b(3) = (3 choose ((3-3)/2)) * ( 2^( -(3+3)/2 ) + 3^2*2^(-3-6) - 3*2^(-3-3) )
            // b(3) = (3 choose ((3-3)/2)) * ( 2^( -(3+3)/2 ) + 3^2*2^(-3-6) - 3*2^(-3-3) )
            // b(3) = 0.095703125
//            new Apfloat( "0.095703125" ),
            null,
            null,
            // For n = 5
            // b(5) = (5 choose ((5-3)/2)) * ( 2^( -(5+3)/2 ) + 5^2*2^(-5-6) - 5*2^(-5-3) )
            // b(5) = 0.27587890625
            new Apfloat( "0.27587890625" ),
        };
        Apfloat tolerance = new Apfloat( "10e-9" );
        for( int n = 0; n < expectedResults.length; n++ ) {
            Apfloat expected = expectedResults[ n ];
            if( expected != null ) {
                checkResult( expected, tolerance, DedekindMonteCarlo.b( n ) );
            }
        }
    }

    @Test
    public void test_cn() {
        // c(n) = (n choose ((n-1)/2)) * ( 2^(-(n+1)/2) + n^2*2^(-n-4) )
        Apfloat expectedResults[] = {
            null,
            null,
            null,
            // c(3) = (3 choose ((3-1)/2)) * ( 2^(-(3+1)/2) + 3^2*2^(-3-4) )
            // c(3) = 0.9609375
            new Apfloat( "0.9609375" ),
            null,
            // For n = 5
            // c(5) = (5 choose ((5-1)/2)) * ( 2^(-(5+1)/2) + 5^2*2^(-5-4) )
            // c(5) = 1.73828125
            new Apfloat( "1.73828125" ),
        };
        Apfloat tolerance = new Apfloat( "10e-9" );
        for( int n = 0; n < expectedResults.length; n++ ) {
            Apfloat expected = expectedResults[ n ];
            if( expected != null ) {
                checkResult( expected, tolerance, DedekindMonteCarlo.c( n ) );
            }
        }
    }

    @Test
    public void testKorshunov() {
        // Definition of k(n)
        //
        // For n even
        //   korshunov(n) = 2^(n choose (n / 2)) * exp( a(n) )
        //
        // For n odd
        //   korshunov(n) = 2^( (n choose ((n-1)/2)) + 1) * exp( b(n) + c(n) )

        Apfloat expectedResults[] = {
            null,
            null,

            //  korshunov(2) = 2^2 * exp( a(2) )
            //  korshunov(2) = 4 * exp( 0.5 ) = 6.5948850828
            new Apfloat( "6.5948850828" ),

            // korshunov(3) = 2^( (3 choose ((3-1)/2)) + 1) * exp( 0.095703125 + 0.9609375 )
            // korshunov(3) = 46.02705368692
            new Apfloat( "46.0270536869" ),

            //   korshunov(4) = 2^(4 choose (4 / 2)) * exp( 1.0625 )
            //   korshunov(4) = 185.190140427
            new Apfloat( "185.190140427" ),

            // korshunov(5) = 2^( (5 choose ((5-1)/2)) + 1) * exp( b(5) + c(5) )
            // korshunov(5) = 2^( (5 choose ((5-1)/2)) + 1) * exp( 0.27587890625 + 1.73828125 )
            // korshunov(5) = 15348.5938416
            new Apfloat( "15348.5938416" ),
        };
        for( int n = 0; n < expectedResults.length; n++ ) {
            Apfloat expected = expectedResults[ n ];
            if( expected != null ) {
                Apfloat tolerance = expected.multiply( new Apfloat( "0.00001" ) );
                checkResult( expected, tolerance, DedekindMonteCarlo.korshunov( n ) );
            }
        }
    }

    private void checkResult( Apfloat expected, Apfloat tolerance, Apfloat actual ) {
        Apfloat lowestExpected = expected.subtract( tolerance );
        int compareLowestExpected = actual.compareTo( lowestExpected );
        assertTrue( "Expected > " + lowestExpected + " Actual: " + actual, compareLowestExpected == 1 || compareLowestExpected == 0  );
        Apfloat highestExpected = expected.add( tolerance );
        int compareHighestExpected = actual.compareTo( highestExpected );
        assertTrue( "Expected < " + highestExpected + " Actual: " + actual, compareHighestExpected == -1 || compareHighestExpected == 0 );
    }
}

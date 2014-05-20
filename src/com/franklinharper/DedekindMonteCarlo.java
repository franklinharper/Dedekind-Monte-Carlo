package com.franklinharper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;
import org.apfloat.Apint;
import org.apfloat.ApintMath;

public class DedekindMonteCarlo {

    // TODO
    // Add version number to output
    // send bill 5 days ( tues., wed, 1/2 thur., Fri May 16, Mon. 19, Tues. 20)

    private static final boolean TRACE = false;

    private static final Apfloat[] DEDEKIND_KNOWN_VALUES = {
        new Apfloat( "2" ),
        new Apfloat( "3" ),
        new Apfloat( "6" ),
        new Apfloat( "20" ),
        new Apfloat( "168" ),
        new Apfloat( "7581" ),
        new Apfloat( "7828354" ),
        new Apfloat( "2414682040998" ),
        new Apfloat( "56130437228687557907788" ), };

    public static final Apint TWO = new Apint( 2 );

    public static void main( String[] args ) {
        if( !TRACE ) {
            printColumnHeaders();
        }
//        for( int n = 2; n < 17; n++ ) {
//            DedekindMonteCarlo.dedekindEstimation( n, 1000000 );
//        }
      DedekindMonteCarlo.dedekindEstimation( 14, 100000 );
    }


    public static void dedekindEstimation( int n, int nIterations ) {
        final long startMillis = System.currentTimeMillis();
        trace( "n: " + n );
        trace( String.format( "iterations: %4.0E", (double) nIterations ) );

        int k;
        if( isOdd( n ) ) {
            k = ( n - 1 ) / 2;
        } else {
            k = n / 2;
        }
        trace( "k: " + k );

        Random random = new Random();
        int[] middleRank = generateNTuplesOfRank_K( n, k );
        int[] aboveMiddle = generateNTuplesOfRank_K( n, k + 1 );
        int[] belowMiddle = generateNTuplesOfRank_K( n, k - 1 );
        long[] sampleValues = new long[ nIterations ];
        for( int i = 0; i < nIterations; i++ ) {
            int[] sampleSet = randomSample( random, middleRank );
            trace( "sample " + Arrays.toString( sampleSet ) );
            int X = calculateX( n, aboveMiddle, sampleSet );
            trace( "X: " + X );
            int Y = calculateY( n, belowMiddle, sampleSet );
            trace( "Y: " + Y );
            long sample = pow2( X + Y );
            if( isOdd( n ) ) {
                // When n is odd multiply by 2 to account for the 2 rows which are
                // which are just above and below the "middle" and then subtract
                // the intersection of the two sets, so that it will not be counted twice.
                sample = sample * 2 - pow2( X );
            }
            sampleValues[ i ] = sample;
            trace( "sampleValue " + i + ": " + sampleValues[ i ] );
        }
        trace( "time to calculate sampleValues: " + elapsedTime( startMillis ) );

        final long startSumSampleValues = System.currentTimeMillis();
        // The estimated D(n) is ( sumSamples * nChooseK ) / numberOfSamples
        Apint sumSampleValues = Apint.ZERO;
        for( int i = 0; i < sampleValues.length; i++ ) {
            sumSampleValues = sumSampleValues.add( new Apint( sampleValues[ i ] ) );
        }
        trace( "time to calculate sumSampleValues: " + elapsedTime( startSumSampleValues ) );

        Apint multiplier = ApintMath.pow( TWO, binomial( n, k ) );
        trace( "multiplier: " + multiplier );
        Apfloat estimate = sumSampleValues.multiply( multiplier ).divide( new Apfloat( sampleValues.length ) );

        final long startStandardDeviation = System.currentTimeMillis();
        Apfloat standardDeviation = standardDeviation( sampleValues, estimate );
        trace( "time to calculate standardDeviation: " + elapsedTime( startStandardDeviation ) );

        printResults( n, estimate, standardDeviation, nIterations, startMillis );
    }

    private static Apfloat standardDeviation( long[] sampleValues, Apfloat estimate ) {
        // In a previous version which used BigDecimal, the calculation of the
        // square root would fail for n > 12, because The recursive sqrt
        // function would cause a StackOverflowError.
        Apfloat sumOfSquaresOfDifferences = Apfloat.ZERO;
        for( int i = 0; i < sampleValues.length; i++ ) {
            Apfloat difference = new Apfloat( sampleValues[ i ] ).subtract( estimate );
            sumOfSquaresOfDifferences = difference.multiply( difference );
        }
        Apfloat floatSumOfSquaresOfDifferences = new Apfloat( sumOfSquaresOfDifferences.toString(), 100 );
        Apfloat variance = floatSumOfSquaresOfDifferences.divide( new Apint( sampleValues.length - 1 ) );
        trace( String.format( "variance: " + variance ) );
        Apfloat sqrt = ApfloatMath.sqrt( variance );
        trace( String.format( "standardDeviation: " + sqrt ) );
        return sqrt;
    }

    private static void printColumnHeaders() {
        System.out.println("n,D(n),KD(n),ED(n),ED(n)/D(n),ED(n)/KD(n), number of iterations,sample standard deviation, calculation time");
    }

    private static void printResults( int n, Apfloat estimate, Apfloat standardDeviation, int nIterations, long startMillis ) {
        String format = "%.10s,";
        System.out.print( n + ",");
        if( n < DEDEKIND_KNOWN_VALUES.length ) {
            System.out.format( format, DEDEKIND_KNOWN_VALUES[ n ] );
        } else {
            System.out.print("N/A,");
        }
        Apfloat kdn = korshunov( n );
        System.out.format( format, kdn );
        System.out.format( format, new Apfloat( estimate.toString() ) );
        Apfloat floatEstimate = new Apfloat( estimate.toString() );
        if( n < DEDEKIND_KNOWN_VALUES.length ) {
            Apfloat ratio = floatEstimate.divide( DEDEKIND_KNOWN_VALUES[ n ] );
            System.out.print( ratio.toString( true ) + "," );
        } else {
            System.out.print("N/A,");
        }
        System.out.format( format, floatEstimate.divide( kdn ) );
        System.out.format( format, new Apfloat( nIterations ));
        System.out.format( format, standardDeviation );
        System.out.format( format, standardDeviation );
        System.out.print( elapsedTime( startMillis ) + ",");
        System.out.println();
    }


    private static String elapsedTime( long startMillis ) {
        long elapsedMillis = System.currentTimeMillis() - startMillis;
        long ms = elapsedMillis % 1000;
        long s = (elapsedMillis / 1000) % 60;
        long m = (elapsedMillis / (1000 * 60)) % 60;
        long h = (elapsedMillis / (1000 * 60 * 60)) % 24;
        return String.format("%dh:%dm:%ds:%dms", h, m, s, ms );
    }

    private static long pow2( int integer ) {
        // Shifting all the bits left by one position multiplies by 2.
        // This works iff no high order bits are shifted out.
        return 1l << ( integer );
    }

    private static void trace( String s ) {
        if( TRACE ) {
            System.out.println( s );
        }
    }

    /**
    *
    * @param k is the rank of the n-tuples contained in the sample
    * @param belowSample n-tuples of rank k -1 1
    * @param sample an array of n-tuples of rank k
    * @return
    */
   static int calculateY( int n, int[] belowSample, int[] sample ) {
       int Y = 0;
       for( int i = 0; i < belowSample.length; i++ ) {
           if( noneOfSuccessorsAreInSample( belowSample[ i ], n, sample ) ) {
               Y = Y + 1;
           }
       }
       return Y;
   }

   /**
    * @param predecessor an n-tuple of rank k - 1
    * @param n is the rank of the n-tuples contained in the sample
    * @param sample an array of n-tuples of rank k
    * @return true iff all successors of the predecessor are in the sample
    */
    public static boolean noneOfSuccessorsAreInSample( int predecessor, int n, int[] sample ) {
        int bitmask = 1;
        for( int i = 0; i < n; i++ ) {
            if( ( predecessor & bitmask ) == 0 ) {
                int successor = predecessor ^ bitmask;
                if( contains( successor, sample ) ) {
                    return false;
                }
            }
            bitmask = bitmask << 1;
        }
        return true;
    }

    /**
     *
     * @param k is the rank of the n-tuples contained in the sample
     * @param aboveSample n-tuples of rank k + 1
     * @param sample an array of n-tuples of rank k
     * @return
     */
    static int calculateX( int n, int[] aboveSample, int[] sample ) {
        int X = 0;
        for( int i = 0; i < aboveSample.length; i++ ) {
            if( allPredecessorsAreInSample( aboveSample[ i ], n, sample ) ) {
                X = X + 1;
            }
        }
        return X;
    }

    /**
     * @param successor an n-tuple of rank k + 1
     * @param n is the rank of the n-tuples contained in the sample
     * @param sample an array of n-tuples of rank k
     * @return true iff all predecessors of the successor are in the sample
     */
    public static boolean allPredecessorsAreInSample( int successor, int n, int[] sample ) {
        int bitmask = 1;
        for( int i = 0; i < n ; i++ ) {
            if( ( successor & bitmask) != 0 ) {
                int predecessor = successor ^ bitmask;
                if( !contains( predecessor, sample ) ) {
                    return false;
                }
            }
            bitmask = bitmask << 1;
        }
        return true;
    }

    private static boolean contains( int searchValue, int[] nTuples ) {
        // Could be optimized because nTuples is in lex order.
        for( int i = 0; i < nTuples.length; i++ ) {
            if( nTuples[ i ] == searchValue ) {
                return true;
            }
        }
        return false;
    }

    private static int[] randomSample( Random random, int[] rank_K ) {
        boolean[] includeInSample = new boolean[ rank_K.length ];
        int elementCount = 0;
        for( int i = 0; i < includeInSample.length; i++ ) {
            includeInSample[ i ] = random.nextBoolean();
            if( includeInSample[ i ] ) {
                elementCount = elementCount + 1;
            }
        }
        int[] randomSample = new int[ elementCount ];
        int j = 0;
        for( int i = 0; i < includeInSample.length; i++ ) {
            if( includeInSample[ i ] ) {
                randomSample[ j ] = rank_K[ i ];
                j = j + 1;
            }
        }
        return randomSample;
    }

    private static boolean isOdd( int integer ) {
        if( ( integer & 1 ) == 1 ) {
            return true;
        } else {
            return false;
        }
    }

    public static int[] generateNTuplesOfRank_K( int n, int k ) {
        ArrayList< Integer > nTuples = new ArrayList< Integer >();
        int upperLimit = ( 1 << n ) - 1; // (2 ^ n) - 1
        for( int i = 0; i <= upperLimit; i++ ) {
            if( Integer.bitCount( i ) == k ) {
                nTuples.add( i );
            }
        }

        int[] result = new int[ nTuples.size() ];
        for( int i = 0; i < result.length; i++ ) {
            result[ i ] = nTuples.get( i );
        }
        return result;
    }

    public static Apfloat korshunov( int n ) {
        final long start = System.currentTimeMillis();
        Apfloat kdn;
        if( isOdd( n ) ) {
            Apint f = ApintMath.pow( TWO, binomial( n, (n - 1)/2 ) );
            trace( "f:" + f );
            Apfloat bn = b(n);
            trace( "b(n): " + bn );
            Apfloat cn = c(n);
            trace( "c(n): " + cn );
            Apfloat expbncn = ApfloatMath.exp( bn.add( cn ) );
            kdn = f.multiply( expbncn );
        } else {
            Apint f = ApintMath.pow( TWO, binomial( n, n/2 ) );
            trace( "f:" + f );
            trace( "f.precision():" + f.precision() );
            Apfloat a = a( n );
            trace( "a(n): " + a );
            trace( "a(n).precision(): " + a.precision() );
            Apfloat expa = ApfloatMath.exp( a );
            trace( "exp(a):" + expa );
            trace( "exp(a).precision(): " + expa.precision() );
            kdn = f.multiply( expa );
        }
        trace( String.format( "kd(%d): %s", n, kdn ) );
        trace( String.format( "kd(%d).precision(): %s", n, kdn.precision() ) );
        trace( String.format( "time to calculate kd(%d): %s", n, elapsedTime( start ) ) );
        return kdn;
    }

    static Apfloat a( int intN ) {
        Apint n = new Apint( intN );
        Apfloat two = new Apfloat( 2, 100 );
        Apint factor1 = new Apint( binomial( intN, intN/2 - 1 ) );
        Apfloat sumand1 = ApfloatMath.pow( two, -intN / 2 );
        Apfloat sumand2 = n.multiply( n ).multiply( ApfloatMath.pow( two, -intN - 5 ) );
        Apfloat sumand3 = n.multiply( ApfloatMath.pow( two, -intN -4 ) );
        Apfloat result = factor1.multiply( sumand1.add( sumand2 ).subtract( sumand3 ) );
        return result;
    }

    private static Apfloat b( int intN ) {
        Apint n = new Apint( intN );
        Apfloat two = new Apfloat( 2, 100 );
        Apint factor1 = new Apint( binomial( intN, ( intN - 3 ) / 2 ) );
        Apfloat sumand1 = ApfloatMath.pow( two, -( intN + 3 ) / 2 );
        Apfloat sumand2 = n.multiply( n ).multiply( ApfloatMath.pow( two, -intN - 6 ) );
        Apfloat sumand3 = n.multiply( ApfloatMath.pow( two, -intN + 3 ) );
        Apfloat result = factor1.multiply( sumand1.subtract( sumand2 ).subtract( sumand3 ) );
        return result;
    }

    private static Apfloat c( int intN ) {
        Apint n = new Apint( intN );
        Apfloat two = new Apfloat( 2, 100 );
        Apint factor1 = new Apint( binomial( intN, ( intN - 1 ) / 2 ) );
        Apfloat sumand1 = ApfloatMath.pow( two, -( intN + 1 ) / 2 );
        Apfloat sumand2 = n.multiply( n ).multiply( ApfloatMath.pow( two, -intN - 4 ) );
        Apfloat result = factor1.multiply( sumand1.add( sumand2 ) );
        return result;
    }

    public static int binomial( long total, long choose ) {
        if( total < choose )
            return 0;
        if( choose == 0 || choose == total )
            return 1;
        return binomial( total - 1, choose - 1 ) + binomial( total - 1, choose );
    }
}

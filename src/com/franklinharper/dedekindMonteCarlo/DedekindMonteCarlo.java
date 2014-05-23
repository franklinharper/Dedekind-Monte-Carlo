package com.franklinharper.dedekindMonteCarlo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;
import org.apfloat.Apint;
import org.apfloat.ApintMath;

public class DedekindMonteCarlo {

    // TODO
    // send bill 5 days ( tues., wed, 1/2 thur., Fri May 16, Mon. 19, Tues. 20, Wed. 21)

    @SuppressWarnings ( "unused")
    private static final int OFF = 0;
    private static final int INFO = 1;
    private static final int DEBUG = 2;

    private static final int TRACE_LEVEL = INFO;

    private static final String version = "0.9.3";

    static final Apfloat[] DEDEKIND_KNOWN_VALUES = {
        new Apfloat( 2.0 ),
        new Apfloat( 3.0 ),
        new Apfloat( 6.0 ),
        new Apfloat( 20.0 ),
        new Apfloat( 168.0 ),
        new Apfloat( 7581.0 ),
        new Apfloat( 7828354.0 ),
        new Apfloat( 2414682040998.0 ),
        new Apfloat( 56130437228687557907788.0 ), };

    public static final Apint TWO = new Apint( 2 );

    public static void main( String[] args ) {
//        for( int n = 2; n <= 9; n++ ) {
//            DedekindMonteCarlo.dedekindEstimation( n, 1000000 );
//        }

        DedekindResult result = dedekindEstimation( 7, 1000000 );
    }

    public static DedekindResult dedekindEstimation( int n, int nIterations ) {
        final long startMillis = System.currentTimeMillis();
        final String columnHeaders = columnHeaders();
        info( columnHeaders );

        final int k;
        if( isOdd( n ) ) {
            k = ( n - 1 ) / 2;
        } else {
            k = n / 2;
        }

        final Apint multiplier = ApintMath.pow( TWO, binomial( n, k ) );;

        debug( "n: " + n );
        debug( "k: " + k );
        debug( String.format( "iterations: %4.0E", (double) nIterations ) );
        debug( "multiplier: " + multiplier );

        final Random random = new Random();
        // The middle rank contains n choose k elements
        final int[] middleRank = generateNTuplesOfRank_K( n, k );
        final int[] aboveMiddle = generateNTuplesOfRank_K( n, k + 1 );
        final int[] belowMiddle = generateNTuplesOfRank_K( n, k - 1 );
        final int showInfo = nIterations / 10;
        final DedekindResult result = new DedekindResult();
        final Apfloat korshunov = korshunov( n );

        long[] sampleValues = new long[ nIterations ];
        for( int i = 0; i < nIterations; i++ ) {
            Set<Integer> sampleSet = randomSample( random, middleRank );
            debug( "sample " + sampleSet );
            int X = calculateX( n, aboveMiddle, sampleSet );
            debug( "X: " + X );
            int Y = calculateY( n, belowMiddle, sampleSet );
            debug( "Y: " + Y );
            long sample = pow2( X + Y );
            if( isOdd( n ) ) {
                // When n is odd multiply by 2 to account for the 2 rows which are
                // which are just above and below the "middle" and then subtract
                // the intersection of the two sets, so that it will not be counted twice.
                sample = sample * 2 - pow2( X );
            }
            sampleValues[ i ] = sample;
            debug( "sampleValue " + i + ": " + sampleValues[ i ] );
            if( i > 0 && i % showInfo == 0 ) {
                Apint sumSampleValues = sumSampleValues( sampleValues );
                Apfloat estimate = estimate( multiplier, sumSampleValues, i + 1 );
                Apfloat standardDeviation = standardDeviation( sampleValues, multiplier, estimate, i + 1 );
                result.n = n;
                result.estimate = estimate;
                result.standardDeviation = standardDeviation;
                result.nIterations = i;
                result.elapsedMillis = System.currentTimeMillis() - startMillis;
                result.korshunov = korshunov;
                System.out.println( result );
            }
        }
        debug( "time to calculate sampleValues: " + formatElapsedTime( System.currentTimeMillis() - startMillis ) );

        Apint sumSampleValues = sumSampleValues( sampleValues );
        Apfloat estimate = estimate( multiplier, sumSampleValues, sampleValues.length );

        final long startStandardDeviation = System.currentTimeMillis();
        Apfloat standardDeviation = standardDeviation( sampleValues, multiplier, estimate, sampleValues.length );
        debug( "time to calculate standardDeviation: " + formatElapsedTime( System.currentTimeMillis() - startStandardDeviation ) );

        result.n = n;
        result.estimate = estimate;
        result.standardDeviation = standardDeviation;
        result.nIterations = nIterations;
        result.elapsedMillis = System.currentTimeMillis() - startMillis;
        result.korshunov = korshunov;
        debug( columnHeaders );
        System.out.println( result );
        return result;
    }

    private static Apfloat estimate( Apint multiplier, Apint sumSampleValues, int sampleCount ) {
        Apfloat estimate = sumSampleValues.multiply( multiplier ).divide( new Apfloat( sampleCount, 20 ) );
        return estimate;
    }

    private static Apint sumSampleValues( long[] sampleValues ) {
        final long startSumSampleValues = System.currentTimeMillis();
        // The estimated D(n) is ( sumSamples * nChooseK ) / numberOfSamples
        Apint sumSampleValues = Apint.ZERO;
        for( int i = 0; i < sampleValues.length; i++ ) {
            sumSampleValues = sumSampleValues.add( new Apint( sampleValues[ i ] ) );
        }
        debug( "time to calculate sumSampleValues: " + formatElapsedTime( System.currentTimeMillis() - startSumSampleValues ) );
        return sumSampleValues;
    }

    static Apfloat standardDeviation( long[] sampleValues, Apint multiplier, Apfloat estimate, int sampleCount ) {
        // In a previous version which used BigDecimal, the calculation of the
        // square root would fail for n > 12, because The recursive sqrt
        // function would cause a StackOverflowError.
        Apfloat sumOfSquaresOfDifferences = Apfloat.ZERO;
        Apfloat difference = Apfloat.ZERO;
        try {
            for( int i = 0; i < sampleCount; i++ ) {
                difference = new Apfloat( sampleValues[ i ] ).multiply( multiplier ).subtract( estimate );
                sumOfSquaresOfDifferences = sumOfSquaresOfDifferences.add( difference.multiply( difference ) );
            }
        } catch( ArrayIndexOutOfBoundsException e ) {
            System.out.println( "Caught ArrayIndexOutOfBoundsException!" );
            System.out.println( "difference: " + difference );
            return new Apfloat( -1 );
        }
        Apfloat floatSumOfSquaresOfDifferences = new Apfloat( sumOfSquaresOfDifferences.toString(), 100 );
        Apfloat variance = floatSumOfSquaresOfDifferences.divide( new Apint( sampleCount - 1 ) );
        debug( String.format( "variance: " + variance ) );
        Apfloat sqrt = ApfloatMath.sqrt( variance );
        debug( String.format( "standardDeviation: " + sqrt ) );
        return sqrt;
    }

    private static final long[] POWERS_OF_2 = {
        1,
        2,
        4,
        8,
        16,
        32,
        64,
        128,
        256,
        512,
        1024,
        2048,
        4096,
        8192,
        16384,
        32768,
        65536,
        131072,
        262144,
        524288,
        1048576,
        2097152,
        4194304,
        8388608,
        16777216,
        33554432,
        67108864,
        134217728,
        268435456,
        536870912,
        1073741824,
        2147483648L,
        4294967296L,
    };

    public static long pow2( int integer ) {
        return POWERS_OF_2[ integer ];
    }

    /**
    *
    * @param k is the rank of the n-tuples contained in the sample
    * @param belowSample n-tuples of rank k -1 1
    * @param sample an array of n-tuples of rank k
    * @return
    */
   static int calculateY( int n, int[] belowSample, Set<Integer> sample ) {
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
    public static boolean noneOfSuccessorsAreInSample( int predecessor, int n, Set<Integer> sample ) {
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
    static int calculateX( int n, int[] aboveSample, Set<Integer> sample ) {
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
    public static boolean allPredecessorsAreInSample( int successor, int n, Set<Integer> sample ) {
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

    private static boolean contains( int searchValue, Set<Integer> nTuples ) {
        return nTuples.contains( searchValue );
    }

    private static Set<Integer> randomSample( Random random, int[] rank_K ) {
        boolean[] includeInSample = new boolean[ rank_K.length ];
        int elementCount = 0;
        for( int i = 0; i < includeInSample.length; i++ ) {
            includeInSample[ i ] = random.nextBoolean();
            if( includeInSample[ i ] ) {
                elementCount = elementCount + 1;
            }
        }
        Set<Integer> randomSample = new HashSet<Integer>(elementCount);
        for( int i = 0; i < includeInSample.length; i++ ) {
            if( includeInSample[ i ] ) {
                randomSample.add( rank_K[ i ] );
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
            // korshunov(n) = 2^( (n choose ((n-1)/2)) + 1) * exp( b(n) + c(n) )
            Apint f = ApintMath.pow( TWO, binomial( n, (n - 1)/2 ) + 1 );
            debug( "f:" + f );
            Apfloat bn = b(n);
            debug( "b(n): " + bn );
            Apfloat cn = c(n);
            debug( "c(n): " + cn );
            Apfloat expbncn = ApfloatMath.exp( bn.add( cn ) );
            kdn = f.multiply( expbncn );
        } else {
            Apint f = ApintMath.pow( TWO, binomial( n, n/2 ) );
            debug( "f:" + f );
            debug( "f.precision():" + f.precision() );
            Apfloat a = a( n );
            debug( "a(n): " + a );
            debug( "a(n).precision(): " + a.precision() );
            Apfloat expa = ApfloatMath.exp( a );
            debug( "exp(a):" + expa );
            debug( "exp(a).precision(): " + expa.precision() );
            kdn = f.multiply( expa );
        }
        debug( String.format( "kd(%d): %s", n, kdn ) );
        debug( String.format( "kd(%d).precision(): %s", n, kdn.precision() ) );
        debug( String.format( "time to calculate kd(%d): %s", n, formatElapsedTime( System.currentTimeMillis() - start ) ) );
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

    static Apfloat b( int intN ) {
        Apint n = new Apint( intN );
        Apfloat two = new Apfloat( 2, 100 );
        Apint factor1 = new Apint( binomial( intN, ( intN - 3 ) / 2 ) );
        Apfloat sumand1 = ApfloatMath.pow( two, -( intN + 3 ) / 2 );
        Apfloat sumand2 = n.multiply( n ).multiply( ApfloatMath.pow( two, -intN - 6 ) );
        Apfloat sumand3 = n.multiply( ApfloatMath.pow( two, -intN - 3 ) );
        Apfloat result = factor1.multiply( sumand1.add( sumand2 ).subtract( sumand3 ) );
        return result;
    }

    static Apfloat c( int intN ) {
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

    private static String columnHeaders() {
        StringBuilder sb = new StringBuilder( 132 );
        sb.append( "n" );

        String columnNames[] = new String[] {
            "D(n)",
            "KD(n)",
            "ED(n)",
            "ED(n)/D(n)",
            "ED(n)/KD(n)",
            "Iterations",
            "sample standard deviation",
            "calculation time",
            "SW Version", };
        for( String name : columnNames ) {
            sb.append( "\t" );
            sb.append( name  );
        }
        return sb.toString();
    }

    private static String formatElapsedTime( long elapsedMillis ) {
        long ms = elapsedMillis % 1000;
        long s = (elapsedMillis / 1000) % 60;
        long m = (elapsedMillis / (1000 * 60)) % 60;
        long h = (elapsedMillis / (1000 * 60 * 60)) % 24;
        return String.format("%dh:%dm:%ds:%dms", h, m, s, ms );
    }

    @SuppressWarnings ( "unused")
    private static void debug( String s ) {
        if( ( TRACE_LEVEL & DEBUG ) != 0 ) {
            System.out.println( s );
        }
    }

    private static void info( String s ) {
        if( ( TRACE_LEVEL & INFO ) != 0 ) {
            System.out.println( s );
        }
    }

    static class DedekindResult {
        int n;
        Apfloat estimate;
        Apfloat standardDeviation;
        int nIterations;
        long elapsedMillis;
        Apfloat korshunov;

        @Override
        public String toString() {

            StringBuilder result = new StringBuilder( 200 );
            String outputFormat = "%.10s";
            addTsv( result, n );
            if( n < DEDEKIND_KNOWN_VALUES.length ) {
                addTsv( result, String.format( outputFormat, DEDEKIND_KNOWN_VALUES[ n ] ) );
            } else {
                addTsv( result, "N/A," );
            }
            addTsv( result, String.format( outputFormat, korshunov ) );
            addTsv( result, String.format( outputFormat, estimate ) );
            if( n < DEDEKIND_KNOWN_VALUES.length ) {
                debug( "estimate.precision: " + estimate.precision() );
                Apfloat dn = DEDEKIND_KNOWN_VALUES[ n ];
                debug( "dn.precision: " + dn.precision() );
                Apfloat ratio = estimate.divide( DEDEKIND_KNOWN_VALUES[ n ] );
                addTsv( result, String.format( outputFormat, ratio ) );
            } else {
                addTsv( result, "N/A,");
            }
            addTsv( result, String.format( outputFormat, estimate.divide( korshunov ) ) );
            addTsv( result, String.format( outputFormat, new Apfloat( nIterations ) ) );
            addTsv( result, String.format( outputFormat, standardDeviation ) );
            addTsv( result, formatElapsedTime( elapsedMillis ) );
            addTsv( result, version );
            return result.toString();
        }

        public static void addTsv(StringBuilder sb, Object o ) {
            sb.append( o );
            sb.append( "\t" );
        }

        public static void addTsv( StringBuilder sb, int integer ) {
            sb.append( integer );
            sb.append( "\t" );
        }

    }

}

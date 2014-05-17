package com.franklinharper;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class DedekindMonteCarlo {

    // TODO
    // add korshunov's formula to the results
    // format output for including in a table
    // send bill 3.5 days ( tues., wed, 1/2 thur., Fri May 16)
    // fix: calulation of std. dev. for large numbers

    private static final boolean TRACE = false;
    private static final MathContext MATH_CONTEXT = new MathContext( 10, RoundingMode.HALF_DOWN );
    private static final double[] DEDEKIND_KNOWN_VALUES = {
        2,
        3,
        6,
        20,
        168,
        7581,
        7828354,
        2414682040998.0,
        56130437228687557907788.0 };

    public static void main( String[] args ) {
        DedekindMonteCarlo.dedekindEstimation( 8, 1000000 );
    }

    public static final BigDecimal TWO = new BigDecimal( 2 );

    public static void dedekindEstimation( int n, int nIterations ) {
        final long startTime = System.currentTimeMillis();
        System.out.println( "n: " + n );
        System.out.format( "iterations: %4.0E\n", (double) nIterations );

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

        BigDecimal sumSampleValues = new BigDecimal( 0 );
        for( int i = 0; i < sampleValues.length; i++ ) {
            sumSampleValues = sumSampleValues.add( new BigDecimal( sampleValues[ i ] ) );
        }
        BigDecimal nChooseK = new BigDecimal( 2 ).pow( binomial( n, k ) );
        trace( "nChooseK: " + nChooseK );
        BigDecimal estimate =
            sumSampleValues.multiply( multiplier ).divide( new BigDecimal( sampleValues.length ), MATH_CONTEXT );

//        BigDecimal sumOfSquaresOfDifferences = new BigDecimal( 0 );
//        for( int i = 0; i < sampleValues.length; i++ ) {
//            BigDecimal difference = new BigDecimal( sampleValues[ i ] ).subtract( estimate );
//            sumOfSquaresOfDifferences = difference.multiply( difference );
//        }
//        BigDecimal variance = sumOfSquaresOfDifferences.divide( new BigDecimal( sampleValues.length - 1 ), MATH_CONTEXT );
//        BigDecimal standardDeviation = bigSqrt( variance );

        System.out.format( "iterations: %4.0E\n", (double) nIterations );
        BigDecimal referenceValue;
        if( n < DEDEKIND_KNOWN_VALUES.length ) {
            referenceValue = new BigDecimal( DEDEKIND_KNOWN_VALUES[ n ] );
            System.out.format( "RD(%d): %s (Reference Dedekind number, exact value)\n", n, referenceValue );
        } else {
//            referenceValue = korshunov( n );
            referenceValue = new BigDecimal( 0 ); // placeholder value
            System.out.format( "RD(%d): %s (Reference Dedekind number, Korshunov estimation)\n", n, referenceValue );
        }
        System.out.format( "ED(%d): %s (estimate for D(%d))\n", n, estimate, n );
        System.out.format(
            "RD(%d) / ED(%d): %s\n",
            n,
            n,
            referenceValue.divide( estimate, MATH_CONTEXT ) );
        System.out.format( "sample standard deviation: %s\n", scientificFormat( standardDeviation ) );
    }

    private static void printElapsedTime( final long startTime ) {
        long ellapsedMillis = System.currentTimeMillis() - startTime;
        long ms = ellapsedMillis % 1000;
        long s = (ellapsedMillis / 1000) % 60;
        long m = (ellapsedMillis / (1000 * 60)) % 60;
        long h = (ellapsedMillis / (1000 * 60 * 60)) % 24;
        System.out.format("Total execution time %dh:%dm:%ds:%dms", h, m, s, ms );
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

//    public static BigDecimal korshunov( int n ) {
//    BigDecimal x;
//    if( isOdd( n ) ) {
////        x = Math.log(2)*(Math.binom(n,n2)+1)
////        x = x+Math.binom(n,n2+1)*(2^(-n2-1)+n^2*2^(-n-4))
////        x = x+Math.binom(n,n2+2)*(2^(-n2-2)-n^2*2^(-n-6)-n*2^(-n-3));
//    } else {
//        int k = n / 2;
//        double binomialFactor = Math.pow( 2, binomial( n, k ) );
//        double exponentialFactor = BigDecimal..exp( a );
//
//        x = TWO.multiply( new BigDecimal( binomial(n,n2) ) );
//        x = x+Math.binom(n,n2+1)*(2^(-n2)+n^2*2^(-n-5)-n*2^(-n-4))
//    }
//    return x;
//    }

    public static int binomial( long total, long choose ) {
        if( total < choose )
            return 0;
        if( choose == 0 || choose == total )
            return 1;
        return binomial( total - 1, choose - 1 ) + binomial( total - 1, choose );
    }

//    public static class DedekindResult {
//        double estimate;
//        double variance;
//
//        public DedekindResult( double estimate, double variance ) {
//            this.estimate = estimate;
//            this.stdDeviation = variance;
//        }
//    }

    private static final BigDecimal SQRT_DIG = new BigDecimal(150);
    private static final BigDecimal SQRT_PRE = new BigDecimal(10).pow(SQRT_DIG.intValue());

    /**
     * Private utility method used to compute the square root of a BigDecimal.
     *
     * @author Luciano Culacciatti
     * @url http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
     */
    private static BigDecimal sqrtNewtonRaphson  (BigDecimal c, BigDecimal xn, BigDecimal precision){
        BigDecimal fx = xn.pow(2).add(c.negate());
        BigDecimal fpx = xn.multiply(new BigDecimal(2));
        BigDecimal xn1 = fx.divide(fpx,2*SQRT_DIG.intValue(),RoundingMode.HALF_DOWN);
        xn1 = xn.add(xn1.negate());
        BigDecimal currentSquare = xn1.pow(2);
        BigDecimal currentPrecision = currentSquare.subtract(c);
        currentPrecision = currentPrecision.abs();
        if (currentPrecision.compareTo(precision) <= -1){
            return xn1;
        }
        return sqrtNewtonRaphson(c, xn1, precision);
    }

    /**
     * Uses Newton Raphson to compute the square root of a BigDecimal.
     *
     * @author Luciano Culacciatti
     * @url http://www.codeproject.com/Tips/257031/Implementing-SqrtRoot-in-BigDecimal
     */
    public static BigDecimal bigSqrt(BigDecimal c){
        return sqrtNewtonRaphson(c,new BigDecimal(1), new BigDecimal( 10 ));
    }


    private static final DecimalFormat BD_SCIENTIFIC_FORMAT = new DecimalFormat("#.######E0");
    public static String scientificFormat( BigDecimal bd ) {
        return BD_SCIENTIFIC_FORMAT.format(bd);
    }

}

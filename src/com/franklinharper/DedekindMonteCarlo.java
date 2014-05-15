package com.franklinharper;

import java.util.ArrayList;
import java.util.Random;

public class DedekindMonteCarlo {

    public static void main( String[] args ) {
    }

    public static DedekindResult dedekindEstimation( int n, int nIterations ) {
        int k;
        if( isEven( n ) ) {
            k = n / 2;
        } else {
            k = (n - 1) / 2;
        }

        Random random = new Random();

        long nChooseK = binomial( n, k );
        int[] middleRank = generateNTuplesOfRank_K( n, k );
        int[] aboveMiddle = generateNTuplesOfRank_K( n, k + 1 );
        int[] belowMiddle = generateNTuplesOfRank_K( n, k - 1 );
        double[] sampleValues = new double[ nIterations ];
        for( int i = 0; i < nIterations; i++ ) {
            int[] sample = randomSample( random, middleRank );
            double X = calculateX( n, aboveMiddle, sample );
            double Y = calculateY( n, belowMiddle, sample );
            sampleValues[ i ] = Math.pow( 2, X + Y ) * nChooseK;
            if( isEven( n ) ) {
                sampleValues[ i ] = sampleValues[ i ] * 2;
            }
//            System.out.println( "sampleValue " + i + ": " + sampleValues[ i ] );
        }

        double sumSampleValues = 0;
        for( int i = 0; i < sampleValues.length; i++ ) {
            sumSampleValues = sumSampleValues + sampleValues[ i ];
        }
        double estimate = sumSampleValues / n;

        double sumOfSquaresOfDifferences = 0;
        for( int i = 0; i < sampleValues.length; i++ ) {
            double difference = sampleValues[ i ] - estimate;
            sumOfSquaresOfDifferences = difference * difference;
        }
        double variance = sumOfSquaresOfDifferences / ( n - 1 );
        System.out.println( "iterations: " + nIterations );
        System.out.println( "estimate for D(" + n + "): " + estimate );
        System.out.println( "variance : " + variance );
        return new DedekindResult( estimate, variance );
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

    private static boolean isEven( int integer ) {
        if( ( integer & 1 ) == 0 ) {
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

    public static long binomial( long total, long choose ) {
        if( total < choose )
            return 0;
        if( choose == 0 || choose == total )
            return 1;
        return binomial( total - 1, choose - 1 ) + binomial( total - 1, choose );
    }

    public static class DedekindResult {
        double estimate;
        double variance;

        public DedekindResult( double estimate, double variance ) {
            this.estimate = estimate;
            this.variance = variance;
        }
    }
}

package com.franklinharper;

import org.apfloat.Apfloat;
import org.junit.Test;

import com.franklinharper.DedekindMonteCarlo.DedekindResult;

public class DedekindMonteCarloIntegrationTests {

    @Test
    public void testDedekind2() {
        checkDedekindEstimate( 2, 1000 );
    }

    @Test
    public void testDedekind3() {
        checkDedekindEstimate( 2, 1000 );
    }

    @Test
    public void testDedekind4() {
        checkDedekindEstimate( 2, 1000 );
    }

    @Test
    public void testDedekind5() {
        checkDedekindEstimate( 5, 100000 );
    }

    @Test
    public void testDedekind6() {
        checkDedekindEstimate( 6, 100000 );
    }

    @Test
    public void testDedekind7() {
        checkDedekindEstimate( 7, 1000000 );
    }

    @Test
    public void testDedekind8() {
        checkDedekindEstimate( 8, 1000000 );
    }

    private void checkDedekindEstimate(int n, int nIterations) {
        Apfloat[] knownValues = DedekindMonteCarlo.DEDEKIND_KNOWN_VALUES;
        Apfloat tolerancePercentage = new Apfloat( 0.05 );
        Apfloat knownValue = knownValues[ n ];
        System.out.println( "n: " + n );
        DedekindResult result = DedekindMonteCarlo.dedekindEstimation( n, nIterations );
        System.out.println("tolerancePercentage: " + tolerancePercentage );
        Apfloat tolerance = knownValue.multiply( tolerancePercentage );
        System.out.println("tolerance: " + tolerance );
        System.out.println("estimate: " + result.estimate );
        System.out.println("estimate/knownValue: " + result.estimate.divide( knownValue ) );
        DedekindMonteCarloUnitTests.checkResult( knownValue, tolerance, result.estimate );
    }

}

package com.franklinharper.dedekindMonteCarlo;

import org.apfloat.Apfloat;
import org.junit.Test;

import com.franklinharper.dedekindMonteCarlo.DedekindMonteCarlo.DedekindResult;

public class DedekindMonteCarloIntegrationTests {

    @Test
    public void testDedekind2() {
        checkDedekindEstimate( 2, 1000 );
    }

    @Test
    public void testDedekind3() {
        checkDedekindEstimate( 3, 10000 );
    }

    @Test
    public void testDedekind4() {
        checkDedekindEstimate( 4, 10000 );
    }

    @Test
    public void testDedekind5() {
        checkDedekindEstimate( 5, 100000 );
    }

    @Test
    public void testDedekind6() {
        checkDedekindEstimate( 6, 1000000 );
    }

    @Test
    public void testDedekind7() {
        checkDedekindEstimate( 7, 10000000 );
    }

    @Test
    public void testDedekind8() {
        checkDedekindEstimate( 8, 10000000 );
    }

    private void checkDedekindEstimate(int n, int nIterations) {
        Apfloat[] knownValues = DedekindMonteCarlo.DEDEKIND_KNOWN_VALUES;
        Apfloat tolerancePercentage = new Apfloat( 0.05 );
        Apfloat knownValue = knownValues[ n ];
        DedekindResult result = DedekindMonteCarlo.dedekindEstimation( n, nIterations );
        Apfloat tolerance = knownValue.multiply( tolerancePercentage );
        DedekindMonteCarloUnitTests.checkResult( knownValue, tolerance, result.estimate );
    }

}

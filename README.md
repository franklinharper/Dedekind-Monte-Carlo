Dedekind number. See wikipedia.

Ideal
======
A subset of a partially ordered set. If y is in the set and x < y then x is in the ideal.
The maximal elements can be used to generate all the elements of the ideal.


Basic Monte Carlo Dedekind algorithm
====================================


For n odd there are two middle equal ranks of the boolean lattice: (n-1)/2 and (n+1)/2, but we only need to do the calcuation for one those.
  we'll choose k = (n-1)/2, k is the rank of the elements


For n even there is a unique middle rank k = n/2

  1. Select a random subset S among the elements of rank k
  ========================================================
  Each element of rank k is an n-tuple of 0s and 1s which have exactly k ones.
  Go through the n-tuples of rank k in lex order and for each n-tuple flip a coin to decide if it should be included or not.
  then once the random set S has been selected

  2. Using S calculate two random variables X and Y
  =================================================
    X is the number of elements of rank k+1 all of whose predecessors of rank k are in S.
        Generate the elements of rank k+1 (k+1 ones) in lex order
        for each one look at all the predecessors by removing the 1s one by one
            if all predecessors are in S then add 1 to X

    Y is the number of elements of rank k-1 all of whose successors of rank k are NOT in S.
        Generate the elements of rank k-1 (k-1 ones) in lex order
        for each one look at all the successors by replacing the 0s by ones, one by one
            if none of successors are in S then add 1 to Y
    
    The value of the MC sample MCS = 2^(X+Y)
    We have calculated N MCS samples.
    Calculate the average value of MCS: AVG_MCS = sum of all MCS / N
    Calculate the sample variance: VAR_MCS = (sum for all values of MCS of (MCS - AVG_MCS)^2)/(N-1)

How to generate the n-tuples of rank k in lex order 
===================================================
   start off with the lowest n-tuple of rank k: 0,0....0,0,1,1,1,...,1,1,1
                             then               0,0....0,1,0,1,1,...,1,1,1
                             then               0,0....0,1,1,0,1,...,1,1,1
                             then               0,0....0,1,1,1,0,...,1,1,1
                             ....
                             then               0,0....0,1,1,1,1,...,1,1,0
                             then               0,0....1,0,1,1,1,1,...,1,1

On the right hand side, there are l contiguous 0s followed by i contiguous 1s.
Replace the 0 to the left of the congtiguous 1s by a 1 (adding a high order 1)
Put l+1 0s to the right of the high order 1
Put i-1 1s furthest to the right.

test: there are exactly n choose k n-tuples of rank k.

Simpler example: How to generate all the n-tuples of in lex order 
=================================================================
   start off with n zeros 0,0....,0,0,0
   then                   0,0....,0,0,1 2^0
   then                   0,0....,0,1,0 2^1
   then                   0,0....,0,1,1 2^1 + 2^0
   starting from the right replace all contiguous 1s by 0s, then replace the 0 by a 1. This is equivalent to a adding 1 in binary code.


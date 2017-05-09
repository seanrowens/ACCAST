/*******************************************************************************
 * Copyright (C) 2017, Paul Scerri, Sean R Owens
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package AirSim.Machinetta.CostMaps;

public class TestFoo {

    private static double uniformPrior;
    private static double invBeliefUniformPrior;

    static int freeIndex = 0;
    static int headIndex = -1;
    static int nextIndex[] = new int[10];
    static int prevIndex[] = new int[10];
    static double[][] invCache =  new double[10][2];
    {
	for(int loopi = 0; loopi < invCache.length; loopi++) {
	    invCache[loopi][0] = Double.NEGATIVE_INFINITY;
	    invCache[loopi][1] = Double.NEGATIVE_INFINITY;
	    nextIndex[loopi] = -1;
	    prevIndex[loopi] = -1;
	}
    }
    public static double invBelief(double belief) {
 	if(belief == uniformPrior)
	    return invBeliefUniformPrior;
	
	System.err.println("Dumping list");
	if(headIndex != -1) {
	    int ind = headIndex;
	    while(ind != -1) {
		System.err.println("        "+ind+" : " +invCache[ind][0]+" "+prevIndex[ind]+" "+nextIndex[ind]);
		ind = nextIndex[ind];
	    }

// 	for(int loopi = 0; loopi < invCache.length; loopi++) {
// 	    System.err.println(loopi+" : " +invCache[loopi][0]+" "+prevIndex[loopi]+" "+nextIndex[loopi]);
// 	}
	}

	for(int loopi = 0; loopi < invCache.length; loopi++) {
	    if(invCache[loopi][0] == belief) {
		System.err.println("Moving old node "+loopi+" to  head.");
		int next = nextIndex[loopi];
		int prev = prevIndex[loopi];
		if(prev != -1)
		    nextIndex[prev] = next;
		if(next != -1)
		    prevIndex[next] = prev;
		nextIndex[loopi] = headIndex;
		prevIndex[loopi] = -1;
		if(headIndex != -1)
		    prevIndex[headIndex] = loopi;
		headIndex = loopi;
		return invCache[loopi][1];
	    }
	}
	double inv2 = Math.log((1/(1-belief))-1);
	
	int tailIndex = -1;
	if(freeIndex < invCache.length) {
	    tailIndex = freeIndex++;
	    System.err.println("Grabbed node "+tailIndex+" from freelist");
	}
	else {
	    for(int loopi = 0; loopi < invCache.length; loopi++) {
		if(nextIndex[loopi] == -1) {
		    tailIndex = loopi;
		    break;
		}
	    }
	    System.err.println("Found tail at "+tailIndex);
	}


	System.err.println("Moving new node "+tailIndex+" to head.");
	invCache[tailIndex][0] = belief;
	invCache[tailIndex][1] = inv2;
	int next = nextIndex[tailIndex];
	int prev = prevIndex[tailIndex];
	if(prev != -1)
	    nextIndex[prev] = next;
	nextIndex[tailIndex] = headIndex;
	prevIndex[tailIndex] = -1;
	if(headIndex != -1)
	    prevIndex[headIndex] = tailIndex;
	headIndex = tailIndex;

	return inv2;
    }
    
    public static void main(String argv[]) {
	TestFoo foo = new TestFoo();
	TestFoo.uniformPrior = .01;
	TestFoo.invBeliefUniformPrior = Math.log((1/(1-.01))-1);

	TestFoo.invBelief(1.0);
	TestFoo.invBelief(2.0);
	TestFoo.invBelief(3.0);
	TestFoo.invBelief(4.0);
	TestFoo.invBelief(5.0);
	TestFoo.invBelief(6.0);
	TestFoo.invBelief(7.0);
	TestFoo.invBelief(8.0);
	TestFoo.invBelief(9.0);
	TestFoo.invBelief(10.0);
	TestFoo.invBelief(11.0);
	TestFoo.invBelief(12.0);
	TestFoo.invBelief(13.0);
	TestFoo.invBelief(6.0);
	TestFoo.invBelief(7.0);
	TestFoo.invBelief(8.0);
	TestFoo.invBelief(1.0);
	TestFoo.invBelief(2.0);
	TestFoo.invBelief(3.0);
	TestFoo.invBelief(4.0);
	TestFoo.invBelief(5.0);
	TestFoo.invBelief(6.0);
	TestFoo.invBelief(7.0);
	TestFoo.invBelief(8.0);
	TestFoo.invBelief(9.0);
	TestFoo.invBelief(10.0);
	TestFoo.invBelief(11.0);
	TestFoo.invBelief(12.0);
	TestFoo.invBelief(13.0);
	TestFoo.invBelief(6.0);
	TestFoo.invBelief(7.0);
	TestFoo.invBelief(8.0);
	TestFoo.invBelief(1.0);
	TestFoo.invBelief(2.0);
	TestFoo.invBelief(3.0);
	TestFoo.invBelief(4.0);
	TestFoo.invBelief(5.0);
	TestFoo.invBelief(6.0);
	TestFoo.invBelief(7.0);
	TestFoo.invBelief(8.0);
	TestFoo.invBelief(9.0);
	TestFoo.invBelief(10.0);
	TestFoo.invBelief(11.0);
	TestFoo.invBelief(12.0);
	TestFoo.invBelief(13.0);
	TestFoo.invBelief(6.0);
	TestFoo.invBelief(7.0);
	TestFoo.invBelief(8.0);
	TestFoo.invBelief(1.0);
	TestFoo.invBelief(2.0);
	TestFoo.invBelief(3.0);
	TestFoo.invBelief(4.0);
	TestFoo.invBelief(5.0);
	TestFoo.invBelief(6.0);
	TestFoo.invBelief(7.0);
	TestFoo.invBelief(8.0);
	TestFoo.invBelief(9.0);
	TestFoo.invBelief(10.0);
	TestFoo.invBelief(11.0);
	TestFoo.invBelief(12.0);
	TestFoo.invBelief(13.0);
	TestFoo.invBelief(6.0);
	TestFoo.invBelief(7.0);
	TestFoo.invBelief(8.0);
	TestFoo.invBelief(1.0);
	TestFoo.invBelief(2.0);
	TestFoo.invBelief(3.0);
	TestFoo.invBelief(4.0);
	TestFoo.invBelief(5.0);
	TestFoo.invBelief(6.0);
	TestFoo.invBelief(7.0);
	TestFoo.invBelief(8.0);
	TestFoo.invBelief(9.0);
	TestFoo.invBelief(10.0);
	TestFoo.invBelief(11.0);
	TestFoo.invBelief(12.0);
	TestFoo.invBelief(13.0);
	TestFoo.invBelief(6.0);
	TestFoo.invBelief(7.0);
	TestFoo.invBelief(8.0);
	TestFoo.invBelief(1.0);
	TestFoo.invBelief(2.0);
	TestFoo.invBelief(3.0);
	TestFoo.invBelief(4.0);
	TestFoo.invBelief(5.0);
	TestFoo.invBelief(6.0);
	TestFoo.invBelief(7.0);
	TestFoo.invBelief(8.0);
	TestFoo.invBelief(9.0);
	TestFoo.invBelief(10.0);
	TestFoo.invBelief(11.0);
	TestFoo.invBelief(12.0);
	TestFoo.invBelief(13.0);
	TestFoo.invBelief(6.0);
	TestFoo.invBelief(7.0);
	TestFoo.invBelief(8.0);

// 2398387 1.0E-5
// 1133768 1.99998E-5
// 1000000 0.010009800000000001
//  950268 0.010019599804
//  905064 2.9999400004000004E-5
//  781418 0.01002939941200392
//  663119 3.9998800015999924E-5
//  590698 0.01003919882401568
//  500786 0.0100489980400392
//  443664 4.9998000039999604E-5
//  398535 0.0100587970600784
//  318803 5.999700007999881E-5
//  274216 0.010068595884137199
//  219572 0.010078394512219516
//  198370 6.999580013999721E-5
//  125496 7.999440022399441E-5
//  120659 0.010088192944329272
//   95835 0.010097991180470388
//   68575 8.999280033598995E-5
//   59358 0.01010778922064678
//   12587 0.010117587064862368
//    2659 9.999100047998323E-5
//    1782 0.99999
//    1026 0.9999800002000001
//    1010 0.9999700005999962
//     316 0.9999600011999843
//     273 0.9999500019999604
//     254 1.0000000000222042E-5
//     134 1.999980000022204E-5
//     119 1.000000000044408E-5
//     104 2.999940000422204E-5
//      92 0.9999899999999999
//      74 3.9998800016221956E-5
//      68 1.0000000000666121E-5
//      62 1.9999800000444073E-5
//      57 1.00000000011102E-5
//      56 0.9999400029999204
//      50 0.9999899999999998
//      49 4.9998000040221635E-5
//      49 1.0000000001332241E-5
//      46 0.9999800002

    }
}

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
package AirSim.Machinetta;

import java.text.DecimalFormat;

public class BeliefShare {
    private final static DecimalFormat fmt = new DecimalFormat("0.000000000");
    private final static DecimalFormat fmt2 = new DecimalFormat("0.00000");

    public static final double KL_THRESHOLD = .5;
    public static final int TTL_INCREMENT = 1;
    public static final int TTL_DECREMENT = -1;

    public static double calculateKLDivergence(double[][] beliefBefore, double[][] beliefAfter, int minX, int maxX, int minY, int maxY, double beliefMaxCap, double beliefMinCap) {
	double totalKLDivergence = 0.0;
	for(int loopX = minX; loopX < maxX; loopX++ ){
	    for(int loopY = minY; loopY < maxY; loopY++){	
		totalKLDivergence +=  calculateBinaryKLDivergence(beliefBefore[loopX][loopY], beliefAfter[loopX][loopY], beliefMaxCap, beliefMinCap);
	    }
	}
	return totalKLDivergence;
    }

    public static int calculateTTLIncrement(double totalKLDivergence, int numBeliefCells) {
	double avgTotalKLDivergence = totalKLDivergence/numBeliefCells;
	if(avgTotalKLDivergence > calcKLThreshold()){
	    //	    Machinetta.Debugger.debug("        totalKLDivergence="+fmt2.format(totalKLDivergence)+", numBeliefCells="+numBeliefCells+", avg KLDivergence per cell="+fmt2.format(totalKLDivergence/(double)numBeliefCells)+", returning "+TTL_INCREMENT,1,"BeliefShare");
	    return TTL_INCREMENT;
	}
	else{
	    //	    Machinetta.Debugger.debug("        totalKLDivergence="+fmt2.format(totalKLDivergence)+", numBeliefCells="+numBeliefCells+", avg KLDivergence per cell="+fmt2.format(totalKLDivergence/(double)numBeliefCells)+", returning "+TTL_DECREMENT,1,"BeliefShare");
	    return TTL_DECREMENT;
	}
    }

    //This method takes as input two beliefs, one before and another after the incorporation
    //of an RSSI reading. The return value is to be added to the TTL of the token associated
    //with the RSSI reading that altered the belief
    //Caller is responsible for making sure minX etc is within bounds
    public static int calculateTTLIncrement(double[][] beliefBefore, double[][] beliefAfter, int minX, int maxX, int minY, int maxY, double beliefMaxCap, double beliefMinCap) {
	int numBeliefCells = (maxX-minX) * (maxY-minY);
	double totalKLDivergence = 0.0;
	if(beliefBefore == null || beliefAfter == null) return 0;
	
	totalKLDivergence = calculateKLDivergence(beliefBefore, beliefAfter, minX, maxX, minY, maxY, beliefMaxCap, beliefMinCap);
	return calculateTTLIncrement(totalKLDivergence, numBeliefCells);
    }

    //This method is self documenting :)
    public static double calculateBinaryKLDivergence(double pi, double qi, double beliefMaxCap, double beliefMinCap){

	if(pi > beliefMaxCap)
	    pi = beliefMaxCap;
	if(pi < beliefMinCap)
	    pi = beliefMinCap;

	if(qi > beliefMaxCap)
	    qi = beliefMaxCap;
	if(qi < beliefMinCap)
	    qi = beliefMinCap;

// 	double smallInc = 0.00000001; //The purpose of this value is to prevent division by zero
// 	if(0.0 == pi)
// 	    pi += smallInc;
// 	if(0.0 == qi)
// 	    qi += smallInc;
	double pi_prime = 1-pi;
	double qi_prime = 1-qi;
// 	System.err.println("        pi="+fmt.format(pi)+", qi="+fmt.format(qi)
// 			   +",pi_prime="+fmt.format(pi_prime)+", qi_prime="+fmt.format(qi_prime)
// 			   +", (pi/qi)="+fmt.format((pi/qi))+", (pi'/qi')="+fmt.format(pi_prime/qi_prime)
// 			   +", log(pi/qi)="+fmt.format(Math.log(pi/qi))
// 			   +", log(pi'/qi')="+fmt.format(Math.log(pi_prime/qi_prime)));
	return pi*Math.log(pi/qi) + pi_prime*Math.log(pi_prime/qi_prime);
    }

    //This method is currently a stub, the dream is that it will take into consideration
    //rough estimate of team size as well as estimates of the uncertainty of others and change
    //the threshold, this possibly remain a constant threshold and the method would be removed
    //Don't know if the overhead of having a method call right now is too much to justify the stub
    public static double calcKLThreshold(){
	//might throw a logit curve in here
	//or some statistical mechanics mumbo jumbo muahhhhaaahahahaa
	return KL_THRESHOLD;
    }


    public static void main(String argv[]) {
	System.err.println("BinaryKLDivergence of pi=.98, qi=.00001  = "+BeliefShare.calculateBinaryKLDivergence(.98,.00001,.98,.00001));
	System.err.println("BinaryKLDivergence of pi=.00001, qi=.98  = "+BeliefShare.calculateBinaryKLDivergence(.00001,.98,.98,.00001));
	System.err.println("BinaryKLDivergence of pi=0, qi=1  = "+BeliefShare.calculateBinaryKLDivergence(0,1,.98,.00001));
	System.err.println("BinaryKLDivergence of pi=1, qi=0  = "+BeliefShare.calculateBinaryKLDivergence(0,1,.98,.00001));
	System.err.println("BinaryKLDivergence of pi=.5, qi=.5  = "+BeliefShare.calculateBinaryKLDivergence(.5,.5,.98,.00001));
	for(double pi = 0; pi <= 1.001; pi += .01) {
	    System.err.println("BinaryKLDivergence of pi="+fmt2.format(pi)+", qi="+fmt2.format(1.0-pi)+"  = "+fmt2.format(BeliefShare.calculateBinaryKLDivergence(pi,1.0-pi,.98,.00001)));
	}
    }
}

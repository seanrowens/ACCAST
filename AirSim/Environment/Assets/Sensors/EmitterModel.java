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
/*
 * EmitterModel.java
 *
 * Created on October 16, 2006, 4:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Environment.Assets.Sensors;

/**
 *
 * @author Owner
 */
import java.util.Random;
import java.text.DecimalFormat;

public class EmitterModel {

    public final static double ORIGINAL_SIZE_METERS = 1000;

    public int x;
    public int y;
    public static final double SIGNAL_STRENGTH_COEFFICIENT = 0.3111*Math.pow(10,-4);
    public static final double SIGNAL_STRENGTH_INCREMENT = 0; 

    public static final double  NOISE_MODEL_STD1 =  0.2*Math.pow(10,-9); 
    public static final double  NOISE_MODEL_STD2 =  0.05*Math.pow(10,-9); 
    
    /*
    public static final double  NOISE_MODEL_STD1 =  0.2*Math.pow(10,-4); 
    public static final double  NOISE_MODEL_STD2 =  0.05*Math.pow(10,-4); 
    */

    private static final int NOISE_MODEL_ITERATIONS = 13;
    private static final int NOISE_MODEL_STD1_RANGE = 300;
    private static final int NOISE_MODEL_STD1_RANGE_SQD = NOISE_MODEL_STD1_RANGE*NOISE_MODEL_STD1_RANGE;

    
    private double mapScale;
    private double mapScaleSqd;
    double[] stdRange1 = null;       
    double[] stdRange2 = null;            
    Random rand = null;


    public EmitterModel(int x, int y, double mapScale) {
        this.x = x;
        this.y = y;
	this.mapScale = mapScale;
	this.mapScaleSqd = mapScale*mapScale;

	// System.err.println("Emitter created at "+x+", "+y+" with mapScale= "+mapScale);
	double inc = 0.00000000001;
	
	stdRange1 = new double[(int)(2*NOISE_MODEL_STD1/inc)];
	double startVal = -NOISE_MODEL_STD1;
	for(int i = 0; i<stdRange1.length; i++){
	    stdRange1[i] = startVal;
	    startVal+=inc;
	}

	stdRange2 = new double[(int)(2*NOISE_MODEL_STD2/inc)];
	startVal = -NOISE_MODEL_STD2;
	for(int i = 0; i<stdRange2.length; i++){
	    stdRange2[i] = startVal;
	    startVal+=inc;
	}

	if(null == this.rand)
	    this.rand = new Random();
    }

    private double getExpectedSignal2Grid(double rangeSqd){
	return SIGNAL_STRENGTH_INCREMENT+(SIGNAL_STRENGTH_COEFFICIENT/(rangeSqd));
    }
    private double getSignal2Grid(double rangeSqd){
	return SIGNAL_STRENGTH_INCREMENT+(SIGNAL_STRENGTH_COEFFICIENT/rangeSqd)+getProbNorm2(rangeSqd);
    }
    private double getSignalGrid(double range){
	return SIGNAL_STRENGTH_INCREMENT+(SIGNAL_STRENGTH_COEFFICIENT/(range*range))+getProbNorm(range);
    }
    private double getExpectedSignalGrid(double range){
	return SIGNAL_STRENGTH_INCREMENT+(SIGNAL_STRENGTH_COEFFICIENT/(range*range));
    }

    public double getExpectedSignal2(double rangeSqdMeters){
	return getExpectedSignal2Grid(rangeSqdMeters/mapScaleSqd);
    }
    public double getSignal2(double rangeSqdMeters){
	return getSignal2Grid(rangeSqdMeters/mapScaleSqd);
    }
    public double getSignal(double rangeMeters){
	return getSignalGrid(rangeMeters/mapScale);
    }
    public double getExpectedSignal(double rangeMeters){
	return getExpectedSignalGrid(rangeMeters/mapScale);
    }

//     public double getNoise() {
// 	return getProbNorm();
//     }

    private double getProbNorm(double range){
	double prob = 0.0;
	for(int i = 0; i < NOISE_MODEL_ITERATIONS; i++){
	    if(range < NOISE_MODEL_STD1_RANGE)
		prob+=stdRange1[rand.nextInt(stdRange1.length)];
	    else
		prob+=stdRange2[rand.nextInt(stdRange2.length)];
	}
	return prob;
	
    }

    private double getProbNorm2(double rangeSqd){
	double prob = 0.0;
	for(int i = 0; i < NOISE_MODEL_ITERATIONS; i++){
	    if(rangeSqd < NOISE_MODEL_STD1_RANGE_SQD)
		prob+=stdRange1[rand.nextInt(stdRange1.length)];
	    else
		prob+=stdRange2[rand.nextInt(stdRange2.length)];
	}
	return prob;
	
    }

    public double getNoise() {
	double prob = 0.0;
	for(int i = 0; i<NOISE_MODEL_ITERATIONS; i++){
	    prob += stdRange2[rand.nextInt(stdRange2.length)];
	}
	return prob;
    }
}

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
 * BinaryBayesFilterSub.java
 *
 * Created on August 31, 2006, 6:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Machinetta.CostMaps;

import AirSim.Machinetta.BeliefShare;
import AirSim.Machinetta.Beliefs.RSSIReading;
import AirSim.Environment.Assets.Sensors.EmitterModel;
import Gui.DoubleGrid;

import java.awt.Point;
import java.util.*;
import java.text.DecimalFormat;

/**
 *
 * @author Glinton/Owens
 *
 * Robin Glinton wrote this originally but Sean Owens is responsible
 * for many of the crimes committed herein.  
 *
 * This class isn't that large but there's an awful lot happening
 * inside it, and much complexity in the name of efficiency.  Need to
 * figure out some way to break some of this stuff out without
 * sacrificing efficiency.
 *
 * Also its historically been very easy to 'break' the filter
 * accidentally and not so easy to verify that some change isn't going
 * to break it.  (Though the BinaryBayesFilterTest class helps.)
 *
 */
public class BinaryBayesFilterSub {
    public final static double SUBFILTER_THRESHOLD = .9;
    public final static int SUBFILTER_INSTANCE_MAX = 400;
    public static int subfilter_instance_count = 0;
    public static int subfilter_instance_cant_count = 0;
    public static int subfilter_instance_update_count = 0;
    

    public final static double BELIEF_MAX_CAP = .98;
    public final static double BELIEF_MIN_CAP = .00001;
    
    public final static boolean BELIEF_SHARE_KLD_ON = true;
    public final static boolean BELIEF_SHARE_GTKLD_ON = false;
    
    private static final double MAX_POWER_DB = -40;
    private static final double MIN_POWER_DB = -85;
    
    private static final double SQRT_2PI = Math.sqrt(2*Math.PI);
    
    private final static DecimalFormat fmt = new DecimalFormat("0.0000000000000000");
    private final static DecimalFormat fmt2 = new DecimalFormat("0.0000");
    private final static DecimalFormat fmt3 = new DecimalFormat("0.###E0");
    private PriorityQueue<RSSIReading> bestReadings;

    // THIS SIGMA WORKED (BOTH LEFT AND RIGHT) FOR A SINGLE EMITTER
    private static final double SIGMA_HIT_LEFT = 0.5*Math.pow(10,-9.5);
    // private static final double SIGMA_HIT_LEFT = 0.5*Math.pow(10,-9.75);
    //    private static final double SIGMA_HIT_LEFT = 0.5*Math.pow(10,-9.775);
    //    private static final double SIGMA_HIT_LEFT = 0.200001*Math.pow(10,-9);
    private static final double SIGMA_HIT_RIGHT = 0.5*Math.pow(10,-8.5);
    
    // These sigma's worked for multi emitters
    //    private static final double SIGMA_HIT_LEFT = 0.200001*Math.pow(10,-9);
    //    private static final double SIGMA_HIT_RIGHT = 2.0*Math.pow(10,-9);
    
    private static final double SIGMA_HIT_LEFT_SQD = SIGMA_HIT_LEFT*SIGMA_HIT_LEFT;
    private static final double SIGMA_HIT_LEFT_COEFF = 1/(SQRT_2PI*SIGMA_HIT_LEFT);
    private static final double SIGMA_HIT_RIGHT_SQD = SIGMA_HIT_RIGHT*SIGMA_HIT_RIGHT;
    private static final double SIGMA_HIT_RIGHT_COEFF = 1/(SQRT_2PI*SIGMA_HIT_RIGHT);
    
    private static final double ILLEGAL_PROB_VAL = -1.0;
    private static final boolean PRINT_ON = false;

    private double normalizer = 0.0;
    
    private int gridSize = 0;
    public int getSize() { return gridSize; }
    private double emitterModelScaleFactor;
    private double mapToProbArrayScaleFactor;
    private double logPrior;
    
    private EmitterModel emitterModel;
    
    private double gridSensorMaxRange;
    private double gridSensorMaxRangeSqd;
    private double gridSensorMinRange;
    private double gridSensorMinRangeSqd;
    private double metersSensorMaxRange;
    private double metersSensorMaxRangeSqd;
    private double metersSensorMinRange;
    private double metersSensorMinRangeSqd;
    
    private boolean createSubFilters = false;
    private BinaryBayesFilterSub[][] subFilters;
    public BinaryBayesFilterSub[][] getSubFilters(){ return subFilters; }
    private boolean[][] subFiltersFlags;
    public boolean[][] getSubFiltersFlags(){ return subFiltersFlags; }

    private double[][] occupancyMap;
    public double[][] getOccupancyMap(){ return occupancyMap; }
    
    // The general point of the pinned is basically, once a belief
    // cell has gone down to 0.0, or up to 1.0. we stop updating that
    // belief from new readings.  This prevents the cells from cycling
    // on and off, and hence prevents thrashing of sub filter
    // creation.
    //
    // Once a particular grid cell has gone to 0.0 or up to 1.0, we
    // 'pin' it at 0 or 1 by setting its pinned flag to true.
    private boolean useNoUpdateFlags = false;
    private boolean[][] noUpdateFlags;
    public boolean[][] getNoUpdateFlags(){ return noUpdateFlags; }

    // @TODO: Note that this method loops while < gridSize, while the
    // percent method loops to < (gridSize - 1) - I don't remember
    // why.  But certainly they should be the same.
    public boolean noUpdateFlagsAllFalse() { 
	if(!useNoUpdateFlags) 
	    return false;

	for(int loopx = 0; loopx < gridSize; loopx++) {
            for(int loopy  = 0; loopy  < gridSize; loopy++) {
                if(noUpdateFlags[loopx][loopy])
		    return false;
            }
        }
	return true;
    }
    public double noUpdateFlagsFalsePercent() { 
	if(!useNoUpdateFlags) 
	    return 1.0;

	int count = 0;
	for(int loopx = 0; loopx < (gridSize-1); loopx++) {
            for(int loopy  = 0; loopy  < (gridSize-1); loopy++) {
                if(!noUpdateFlags[loopx][loopy])
		    count++;
            }
        }
	return ((double)count)/((double)(gridSize*gridSize));
    }
    
    private double[][] beliefs;
    public double[][] getBeliefs(){return beliefs;}
    public void copyBeliefs(double[][] copy) {
	for(int loopx = 0; loopx < gridSize; loopx++) {
	    System.arraycopy(beliefs[loopx],0,copy[loopx],0,copy[loopx].length);
	}	
    }
    
    private double[][] oldBeliefs;
    public double[][] getOldBeliefs(){return oldBeliefs;}
    
    private double[][] gTKLD;
    public double[][] getGTKLD() { return gTKLD; }
    public double getGTKLDSum() {
        double sum = 0;
        for(int loopx = 0; loopx < gridSize; loopx++) {
            for(int loopy  = 0; loopy  < gridSize; loopy++) {
                sum += gTKLD[loopx][loopy];
            }
        }
        return sum;
    }
    
    private double[][] gTBeliefs;
    public double[][] getGTBeliefs() { return gTBeliefs; }
    public void initializeGTBeliefs(ArrayList<Point> emitters) {
        Point[] emitterAry = emitters.size() > 0 ? emitters.toArray(new Point[1]) : null;
        for(int loopx = 0; loopx < gridSize; loopx++) {
            for(int loopy  = 0; loopy  < gridSize; loopy++) {
                gTBeliefs[loopx][loopy] = .001;
                for(int loopi = 0; emitterAry != null && loopi < emitterAry.length; loopi++) {
                    double distSqd = distanceSquared((int)emitterAry[loopi].x,(int)emitterAry[loopi].y,loopx,loopy);
                    if(distSqd < 1)
                        gTBeliefs[loopx][loopy] = 1.0;
                    else {
                        if((1/distSqd) > gTBeliefs[loopx][loopy])
                            gTBeliefs[loopx][loopy] = 1/distSqd;
                    }
                }
            }
        }
        for(int loopx = 0; loopx < gridSize; loopx++) {
            for(int loopy  = 0; loopy  < gridSize; loopy++) {
                if(BELIEF_SHARE_GTKLD_ON) gTKLD[loopx][loopy] = BeliefShare.calculateBinaryKLDivergence(gTBeliefs[loopx][loopy], beliefs[loopx][loopy], BELIEF_MAX_CAP, BELIEF_MIN_CAP);
            }
        }
    }
    
    private double[][] beliefKLD;
    public double[][] getBeliefKLD() { return beliefKLD; }
    public double getBeliefKLDSum() {
        double sum = 0;
        for(int loopx = 0; loopx < gridSize; loopx++) {
            for(int loopy  = 0; loopy  < gridSize; loopy++) {
                sum += beliefKLD[loopx][loopy];
            }
        }
        return sum;
    }
    
    private double beliefKLDPartial = 0.0;
    public double getBeliefKLDPartial() { return beliefKLDPartial; }
    
    private double gTKLDPartial = 0.0;
    public double getGTKLDPartial() { return gTKLDPartial; }
    
    private int gridLastUpdateSize = 0;
    public int getLastUpdateSize() { return gridLastUpdateSize; }
    
    private int gridLastUpdateStartX;
    public int getLastBeliefStartX() { return gridLastUpdateStartX; }
    private int gridLastUpdateStartY;
    public int getLastBeliefStartY() { return gridLastUpdateStartY; }
    private int gridLastUpdateEndX;
    public int getLastBeliefEndX() { return gridLastUpdateEndX; }
    private int gridLastUpdateEndY;
    public int getLastBeliefEndY() { return gridLastUpdateEndY; }
    
    private static double uniformPrior;
    private static double invBeliefUniformPrior;
    private static long diffuseIntervalMS;
    private static double diffuseDelta;
    

    // @TODO: Can we break out the metersToGrid/gridTometers stuff?
    // Or use Gui/ViewPort?  Afraid to mess with it at the moment for
    // fear it will break the filter.
    private int lowerLeftX = 0;
    private int lowerLeftY = 0;
    private int gridToMetersX(double gridx) {
	return (int)((lowerLeftX) + (mapToProbArrayScaleFactor * gridx)+(mapToProbArrayScaleFactor/2));
    }
    private int gridToMetersY(double gridy) {
	return (int)((lowerLeftY) + (mapToProbArrayScaleFactor * gridy)+(mapToProbArrayScaleFactor/2));
    }
    private int metersToGridX(double metersX) {
	return (int) ((metersX - lowerLeftX) / mapToProbArrayScaleFactor);
    }
    private int metersToGridY(double metersY) {
	return (int) ((metersY - lowerLeftY) / mapToProbArrayScaleFactor);
    }

    /** Creates a new instance of BinaryBayesFilterSub */
    public BinaryBayesFilterSub(final int gridSize, 
			     int lowerLeftXMeters, 
			     int lowerLeftYMeters, 
			     double mapToProbArrayScaleFactor, 
			     double emitterModelScaleFactor, 
			     double metersSensorMaxRange, 
			     double metersSensorMinRange, 
			     double uniformPrior,
			     final long diffuseIntervalMS, 
			     final double diffuseDelta,
			     final boolean useNoUpdateFlags,
			     boolean createSubFilters) {
        Machinetta.Debugger.debug("Constructor: gridSize="+gridSize
				  +" mapToProbArrayScaleFactor="+mapToProbArrayScaleFactor
				  +" lowerLeftXMeters="+lowerLeftXMeters
				  +" lowerLeftYMeters="+lowerLeftYMeters
				  +" emitterModelScaleFactor="+emitterModelScaleFactor
				  +" metersSensorMaxRange="+metersSensorMaxRange
				  +" metersSensorMinRange="+metersSensorMinRange
				  +" uniformPrior="+uniformPrior
				  +" diffuseInterval=" + diffuseIntervalMS
				  +" diffuseDelta= " + diffuseDelta
				  +" usePinned="+useNoUpdateFlags
				  +" createSubFilters="+createSubFilters,1,this);
	
	this.emitterModelScaleFactor = emitterModelScaleFactor;
	this.lowerLeftX = lowerLeftXMeters;
	this.lowerLeftY = lowerLeftYMeters;
	this.uniformPrior = uniformPrior;
	this.invBeliefUniformPrior = Math.log((1/(1-uniformPrior))-1);
	this.diffuseIntervalMS = diffuseIntervalMS;
	this.diffuseDelta = diffuseDelta;
	this.useNoUpdateFlags = useNoUpdateFlags;
	this.createSubFilters = createSubFilters;
        
        if (!BELIEF_SHARE_KLD_ON) {
            Machinetta.Debugger.debug("BELIEF SHARING OFF", 4, this);
        }
        
        this.gridSize = gridSize;
        bestReadings = new PriorityQueue<RSSIReading>();
        this.mapToProbArrayScaleFactor = mapToProbArrayScaleFactor;
        emitterModel = new EmitterModel(0,0,emitterModelScaleFactor);
        
        gridSensorMaxRange = metersSensorMaxRange/mapToProbArrayScaleFactor;
        gridSensorMaxRangeSqd = gridSensorMaxRange*gridSensorMaxRange;
        gridSensorMinRange = metersSensorMinRange/mapToProbArrayScaleFactor;
        gridSensorMinRangeSqd = gridSensorMinRange*gridSensorMinRange;
        this.metersSensorMaxRange = metersSensorMaxRange;
        this.metersSensorMaxRangeSqd = metersSensorMaxRange*metersSensorMaxRange;
        this.metersSensorMinRange = metersSensorMinRange;
        this.metersSensorMinRangeSqd = metersSensorMinRange*metersSensorMinRange;
        
        Machinetta.Debugger.debug("init: sensor max range="+this.metersSensorMaxRange
                +" meters ("+gridSensorMaxRange+" grid cells) min range="
                +metersSensorMinRange+" meters ("+gridSensorMinRange+") grid cells)",1,this);

	if(createSubFilters) {
	    subFilters = new BinaryBayesFilterSub[gridSize][gridSize];
	    subFiltersFlags = new boolean[gridSize][gridSize];
	}

        occupancyMap = new double[gridSize][gridSize];
	if(useNoUpdateFlags)
	    noUpdateFlags = new boolean[gridSize][gridSize];
        beliefs = new double[gridSize][gridSize];
        oldBeliefs = new double[gridSize][gridSize];
        gTBeliefs = new double[gridSize][gridSize];
	if(BELIEF_SHARE_KLD_ON) {
	    beliefKLD = new double[gridSize][gridSize];
	}
	if(BELIEF_SHARE_GTKLD_ON) {
	    gTKLD = new double[gridSize][gridSize];
	}
        
	// original
	double fac = 0.0001;

        logPrior = Math.log((fac)/(1.0-fac));

        double occPrior = invBelief(uniformPrior);
        for(int gridi = 0; gridi<gridSize; gridi++){
            for(int gridj=0; gridj<gridSize; gridj++){
		if(createSubFilters) {
		    subFilters[gridi][gridj] = null;
		    subFiltersFlags[gridi][gridj] = false;
		}

		occupancyMap[gridi][gridj] = occPrior;

		if(useNoUpdateFlags) {
		    noUpdateFlags[gridi][gridj] = true;
		}

                beliefs[gridi][gridj] = uniformPrior;
                oldBeliefs[gridi][gridj] = uniformPrior;
		if(BELIEF_SHARE_KLD_ON) {
		    beliefKLD[gridi][gridj] = .000001;
		}
		if(BELIEF_SHARE_GTKLD_ON) {
		    gTBeliefs[gridi][gridj] = .000001;
		}
            }
        }
        
	if(diffuseDelta != 0) {
	    Machinetta.Debugger.debug("Starting diffusion thread with period "+diffuseIntervalMS+" and delta "+diffuseDelta,1,this);
	    (new Thread() {
		    public void run() {
			
			while (true) {
			    for(int i = 0; i<gridSize; i++){
				for(int j=0; j<gridSize; j++){
				    
				    if(!useNoUpdateFlags || noUpdateFlags[i][j]) {
					double probArrive = diffuseDelta, probLeave = diffuseDelta;
					oldBeliefs[i][j] = beliefs[i][j];
					beliefs[i][j] = (beliefs[i][j])*(1.0 - probLeave) + (1.0 - beliefs[i][j]) * probArrive;
					occupancyMap[i][j] = BinaryBayesFilterSub.invBelief(beliefs[i][j]);
				    }
				    
				}
			    }
			    try {
				sleep(diffuseIntervalMS);
			    } catch (InterruptedException e) {}
			}
			
		    }
		    
		}).start();
	}
    }

    public double belief(double logodds){
        return 1-1/(1+Math.exp(logodds));
    }
    
    static double lastBelief=Double.NEGATIVE_INFINITY;
    static double lastInvBeliefRetVal;
    static double lastBelief2=Double.NEGATIVE_INFINITY;
    static double lastInvBeliefRetVal2;
    public static double invBelief(double belief) {
	if(belief == lastBelief)
	    return lastInvBeliefRetVal;
	else if(belief == lastBelief2)
	    return lastInvBeliefRetVal2;
 	else if(belief == uniformPrior)
	    return invBeliefUniformPrior;
	lastBelief2 = lastBelief;
	lastInvBeliefRetVal2 = lastInvBeliefRetVal;
	lastBelief = belief;
 	lastInvBeliefRetVal =  Math.log((1/(1-belief))-1);
	return lastInvBeliefRetVal;
    }

    public void setLargestReadings(RSSIReading r){
        if(bestReadings.size() > 1000){
            bestReadings.clear();
        } else
            bestReadings.offer(r);
    }
    
    public void paintLargestReadings(){
        RSSIReading r = null;
        for(int i=0; i<100; i++){
            r = ((RSSIReading)bestReadings.poll());
            if(r != null){
                beliefs[(int)r.x][(int)r.y] = 1.0;
            }
        }
    }
    
    public double likelihood(RSSIReading reading, int metersX, int metersY, boolean print){
        double metersDistSqd = distanceSquared((int)reading.x,(int)reading.y,metersX,metersY);
        double expectedSignalStrength = emitterModel.getExpectedSignal2(metersDistSqd);
        double actualSignalStrength = reading.strength;
        double probHit = getProbHit(actualSignalStrength, expectedSignalStrength);
        if(print) {
	    System.err.println("FILT:  distm= "+fmt2.format(Math.sqrt(metersDistSqd))
			       +" exp= "+fmt2.format(expectedSignalStrength*1000000000000.0)
			       +" act= "+fmt2.format(actualSignalStrength*1000000000000.0)
			       +" diff= "+fmt2.format((actualSignalStrength - expectedSignalStrength)*1000000000000.0)
			       +" ratio= "+fmt2.format(expectedSignalStrength/actualSignalStrength)
			       +" prob= "+fmt.format(probHit)
			       );
	}
        return probHit;
    }
    
    public double distance(int x1, int y1, int x2, int y2){
        return Math.sqrt(square(x1-x2)+square(y1-y2));
    }
    public double distanceSquared(int x1, int y1, int x2, int y2){
        return square(x1-x2)+square(y1-y2);
    }
    public double square(double a){
        return a*a;
    }
    public double calcNormalizer(double expected){
        double norm = 0.0;
        for(double meas = MIN_POWER_DB; meas<MAX_POWER_DB; meas+=0.02){
            double measP = Math.pow(10,-3)*Math.pow(10,meas/10);
            norm+=getProbHitForNormal(measP, expected);
        }
        
        return 1/norm;
    }
    public double getProbHitForNormal(double measured, double expected){
        double sigma_hit;
        if(measured <= expected) sigma_hit = SIGMA_HIT_LEFT;
        else sigma_hit = SIGMA_HIT_RIGHT;
        double arg = -0.5*square(measured-expected)/square(sigma_hit);
        double coeff = 1/(SQRT_2PI*sigma_hit);
        return coeff*Math.exp(arg);
    }
    public double getProbHit(double measured, double expected){
        double sigma_hit;
        double arg;
        double coeff;
        if(measured <= expected)  {
            sigma_hit = SIGMA_HIT_LEFT;
            arg = -0.5*square(measured-expected)/SIGMA_HIT_LEFT_SQD;
            coeff = SIGMA_HIT_LEFT_COEFF;
        } else {
            sigma_hit = SIGMA_HIT_RIGHT;
            arg = -0.5*square(measured-expected)/SIGMA_HIT_RIGHT_SQD;
            coeff = SIGMA_HIT_RIGHT_COEFF;
        }
        if(normalizer == 0.0){
            normalizer = calcNormalizer(expected);
            System.out.println("Normalizer: " + normalizer);
        }
        double outProb = coeff*normalizer*Math.exp(arg);
        
        return outProb;
    }
    public boolean legalReading(RSSIReading reading, int metersX, int metersY, double metersDistSqd){
        if(metersDistSqd > metersSensorMaxRangeSqd)
            return false;
        if(metersDistSqd < metersSensorMinRangeSqd)
            return false;
        return true;
    }
    public void updateBinaryFilter(RSSIReading reading){
        long startTime = System.currentTimeMillis();
        double probability = 0.0;

        Machinetta.Debugger.debug("signal="+reading,0,this);
        
        gridLastUpdateStartX = metersToGridX(reading.x - metersSensorMaxRange) - 1;
        gridLastUpdateEndX = metersToGridX(reading.x + metersSensorMaxRange) + 1;
        gridLastUpdateStartY = metersToGridY(reading.y - metersSensorMaxRange) - 1;
        gridLastUpdateEndY = metersToGridY(reading.y + metersSensorMaxRange) + 1;
        if(gridLastUpdateStartX < 0) gridLastUpdateStartX = 0;
        if(gridLastUpdateStartX > gridSize) gridLastUpdateStartX = gridSize - 1;
        if(gridLastUpdateEndX < 0) gridLastUpdateEndX = 0;
        if(gridLastUpdateEndX > gridSize) gridLastUpdateEndX = gridSize - 1;
        if(gridLastUpdateStartY < 0) gridLastUpdateStartY = 0;
        if(gridLastUpdateStartY > gridSize) gridLastUpdateStartY = gridSize - 1;
        if(gridLastUpdateEndY < 0) gridLastUpdateEndY = 0;
        if(gridLastUpdateEndY > gridSize) gridLastUpdateEndY = gridSize - 1;
        gridLastUpdateSize = (gridLastUpdateEndX - gridLastUpdateStartX) * (gridLastUpdateEndY - gridLastUpdateStartY);
        
        int gridCellsUpdatedCounter = 0;
        beliefKLDPartial = 0.0;
        gTKLDPartial = 0.0;
        double metersDistSqd;
        
        for(int gridi = gridLastUpdateStartX; gridi<gridLastUpdateEndX; gridi++){
            for(int gridj=gridLastUpdateStartY; gridj<gridLastUpdateEndY; gridj++){
		int metersX = gridToMetersX(gridi);
		int metersY = gridToMetersY(gridj);
                metersDistSqd = distanceSquared((int)reading.x,(int)reading.y,metersX,metersY)
		    -(mapToProbArrayScaleFactor*mapToProbArrayScaleFactor);

                if(legalReading(reading, metersX, metersY,metersDistSqd)){
                    gridCellsUpdatedCounter++;
                    probability = likelihood(reading, metersX, metersY, false);
                    
		    if(!useNoUpdateFlags || noUpdateFlags[gridi][gridj]) {
			// Log odds version
			occupancyMap[gridi][gridj] = occupancyMap[gridi][gridj] + Math.log(probability/(1-probability)) - logPrior;
			oldBeliefs[gridi][gridj] = beliefs[gridi][gridj];
			beliefs[gridi][gridj] = belief(occupancyMap[gridi][gridj]);
		    
			if(useNoUpdateFlags) {
			    if(beliefs[gridi][gridj] > 1.0)
				noUpdateFlags[gridi][gridj] = false;
			    if(beliefs[gridi][gridj] <= 0.0)
				noUpdateFlags[gridi][gridj] = false;
			}

			if(createSubFilters) {
			    //			    if(null == subFilters[gridi][gridj]) {
			    // 
			    // I don't remember why... I put in these
			    // flags.  There WAS a reason, I definitely
			    // remember that.  Shame on me for not
			    // documenting properly.  Originally just
			    // checked for null.  I think this was to
			    // prevent 'thrashing' (repeatedly
			    // creating and removing) sub filters.
			    if(false == subFiltersFlags[gridi][gridj]) {
				if(beliefs[gridi][gridj] > SUBFILTER_THRESHOLD) {
				    subFiltersFlags[gridi][gridj] = true;
				    if(subfilter_instance_count < SUBFILTER_INSTANCE_MAX) {
					subfilter_instance_count++;
					int subLowerLeftX = (int)(lowerLeftX + gridi * mapToProbArrayScaleFactor);
					int subLowerLeftY = (int)(lowerLeftY + gridj * mapToProbArrayScaleFactor);
					Machinetta.Debugger.debug("Creating subFilter "+subfilter_instance_count+" at "+gridi+","+gridj,1, this);				    
					subFilters[gridi][gridj] = 
					    new BinaryBayesFilterSub(gridSize,
								  subLowerLeftX,
								  subLowerLeftY,
								  mapToProbArrayScaleFactor,
								  emitterModelScaleFactor,
								  metersSensorMaxRange,
								  metersSensorMinRange,
								  uniformPrior,
								  diffuseIntervalMS,
								  diffuseDelta,
								  useNoUpdateFlags,
								  false);
				    }
				    else {
					subfilter_instance_cant_count++;
					Machinetta.Debugger.debug("Can't create "+subfilter_instance_cant_count+"th subFilter, already have "+SUBFILTER_INSTANCE_MAX+" created",1, this);				    
				    }
				}
			    }
			    if(null != subFilters[gridi][gridj]) {
				subfilter_instance_update_count++;
				subFilters[gridi][gridj].updateBinaryFilter(reading);
				if(subFilters[gridi][gridj].noUpdateFlagsAllFalse()) {
				    subFilters[gridi][gridj] = null;
				    Machinetta.Debugger.debug("Destroying subFilter "+subfilter_instance_count+" at "+gridi+","+gridj,1, this);
				    subfilter_instance_count--;
				}
			    }
			}
		    }
		    
                    
                    if(BELIEF_SHARE_KLD_ON) {
			beliefKLD[gridi][gridj] = BeliefShare.calculateBinaryKLDivergence(oldBeliefs[gridi][gridj], beliefs[gridi][gridj], BELIEF_MAX_CAP, BELIEF_MIN_CAP);
			beliefKLDPartial += beliefKLD[gridi][gridj];
		    }
                    if(BELIEF_SHARE_GTKLD_ON) {
			gTKLD[gridi][gridj] = BeliefShare.calculateBinaryKLDivergence(gTBeliefs[gridi][gridj], beliefs[gridi][gridj], BELIEF_MAX_CAP, BELIEF_MIN_CAP);
			gTKLDPartial = gTKLD[gridi][gridj];
		    }
                }
// 		else {
// 		    Machinetta.Debugger.debug("NOT LEGAL: grid "+gridi+","+gridj+" in meters is "+metersX+","+metersY+" metersDistSqd= "+metersDistSqd,1, this);
// 		}
            }
        }
        long endTime = System.currentTimeMillis();
	//        Machinetta.Debugger.debug("time to update filter="+(endTime - startTime)+", grid cells updated="+gridCellsUpdatedCounter,1,this);
        
        paintLargestReadings();
    }
    
    private final double log2 = Math.log(2);
    public double[][] getEntropy() {
        double[][] entropy = new double[gridSize][gridSize];
        
        for(int loopi = 0; loopi < gridSize; loopi++) {
            for(int loopj = 0; loopj < gridSize; loopj++) {
                double p = beliefs[loopi][loopj];
                double pp = 1 - beliefs[loopi][loopj];
                double log2p = Math.log(p)/log2;
                double log2pp = Math.log(pp)/log2;
                
                entropy[loopi][loopj] = 0 - ( (log2p * p) + (log2pp* pp) );
            }
        }
        return entropy;
    }
    
    public void getEntropyGrid(DoubleGrid grid) {
        double entropy=0.0;
        double lastp=Double.NEGATIVE_INFINITY;
        int saveCounter = 0;
        for(int loopi = 0; loopi < gridSize; loopi++) {
            for(int loopj = 0; loopj < gridSize; loopj++) {
		double p = beliefs[loopi][loopj];
                if(p == lastp) {
                    grid.fastSetValue(loopi, loopj, entropy);
                    saveCounter++;
                } else {
		    lastp = p;
                    if (p == 0) {
                        entropy = 0.0;
                    } else {
			double prior = uniformPrior;
			// Attempt to change entropy so that prior is maximum entropy
			if (p < prior) {
			    p = 0.5 * p/prior;
			} else {
			    p = 0.5 * (1.0-p)/(1.0-prior);
			}
			double pp = 1.0 - p;
			double logp = Math.log(p);
			double logpp = Math.log(pp);
			entropy = 0 - ( (logp*p) + (logpp*pp) )/log2;
                    }
		    if(Double.isNaN(entropy))
		       entropy = 0.0;
                    grid.fastSetValue(loopi, loopj, entropy);
                }
            }
        }
        Machinetta.Debugger.debug("Saved "+saveCounter+" calculations of entropy.",-1,this);
    }

    private void print() {
        double entropy=0.0;
        double lastp=Double.NEGATIVE_INFINITY;
	StringBuffer buf = new StringBuffer();
	for(double loopi = 0; loopi < gridSize; loopi += (gridSize/100)) {
	    for(double loopj = 0; loopj < gridSize; loopj += (gridSize/100)) {
		double p = beliefs[(int)loopi][(int)loopj];
                if(p == lastp) {
		    //
                } else {
		    lastp = p;
		    if (p == 0) {
			entropy = 0.0;
		    } else {
			double prior = uniformPrior;
			// Attempt to change entropy so that prior is maximum entropy
			if (p < prior) {
			    p = 0.5 * p/prior;
			} else {
			    p = 0.5 * (1.0-p)/(1.0-prior);
			}
			double pp = 1.0 - p;
			double logp = Math.log(p);
			double logpp = Math.log(pp);
			entropy = 0 - ( (logp*p) + (logpp*pp) )/log2;
		    }
		}
		if(0.0 == entropy) {
		    buf.append(" ");
		}
		else if(entropy >= .0 && entropy < .1) {
		    buf.append("0");
		}
		else if(entropy >= .1 && entropy < .2) {
		    buf.append("1");
		}
		else if(entropy >= .2 && entropy < .3) {
		    buf.append("2");
		}
		else if(entropy >= .3 && entropy < .4) {
		    buf.append("3");
		}
		else if(entropy >= .4 && entropy < .5) {
		    buf.append("4");
		}
		else if(entropy >= .5 && entropy < .6) {
		    buf.append("5");
		}
		else if(entropy >= .6 && entropy < .7) {
		    buf.append("6");
		}
		else if(entropy >= .7 && entropy < .8) {
		    buf.append("7");
		}
		else if(entropy >= .8 && entropy < .9) {
		    buf.append("8");
		}
		else if(entropy >= .9 && entropy < .999) {
		    buf.append("9");
		}
		else if(entropy >= .999) {
		    buf.append("@");
		}
	    }
	    buf.append("\n");
	}
	Machinetta.Debugger.debug("grid=\n"+buf.toString(),1,this);
    }
}

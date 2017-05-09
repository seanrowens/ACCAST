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
 * MixGaussiansCostMap.java
 *
 * Created on July 13, 2006, 5:44 PM
 *
 */

package AirSim.Machinetta.CostMaps;

import AirSim.Machinetta.UAVRI;
import java.awt.Rectangle;
import java.util.ArrayList;

/**
 *
 * @author pscerri
 */
public class MixGaussiansCostMap implements CostMap {
    private final static double SQRT_TWO_PI = Math.sqrt(2*Math.PI);

    protected ArrayList<Gaussian> gaussians = new ArrayList<Gaussian>();
    Gaussian[] gaussianAry = null;
    
    /** Creates a new instance of MixGaussiansCostMap */
    public MixGaussiansCostMap() {
    }
    
    public void clear() {
	gaussians.clear();
	gaussianAry = gaussians.toArray(new Gaussian[1]);;
    }

    public double getCost(double x1, double y1, double z1, long t1, double x2, double y2, double z2, long t2) {
        
        return getCost(x1, y1, t1, x2, y2, t2);
    }
    
    public double getCost(double x1, double y1, long t1, double x2, double y2, long t2) {
        
        double cost = 0.0;
        
        if (x1 > x2) {
            double temp = x2;
            x2 = x1;
            x1 = temp;
        }
        
        if (y1 > y2) {
            double temp = y2;
            y2 = y1;
            y1 = temp;
        }
        
        double steps = 10;
        double ldx = (x2-x1)/steps;
        double ldy = (y2-y1)/steps;
        
        /**
         * @fix It seems like a short path segment might get more value than a long one, because
         * it has the same number of steps, and there is no division by length.
         */
        
        for (int i = 0; i < steps; i++) {
            x1 += ldx; y1 += ldy;

	    // switching from foreach to an array of gaussians speed
	    // up drawing our costmaps by 350%.
	    //
	    //            for (Gaussian g: gaussians) {
	    if(gaussianAry != null) {
		for (int loopg = 0; loopg < gaussianAry.length; loopg++) {
		    Gaussian g = gaussianAry[loopg];
		    double xdiff = x1-g.x;
		    double ydiff = y1-g.y;
		    double distsq = xdiff * xdiff + ydiff * ydiff;
                
		    //                double dist = Math.hypot(x1-g.x, y1 - g.y) / 500.0;
                
		    if (distsq < 1.0) distsq = 1.0;
                
		    // @TODO: the uncommented cost line below is
		    // _wrong_.  The commented stuff just below is, I
		    // think, more or less correct if we really want a
		    // guassian, but I'm not completely sure.

		    // formula for univariate normal dist is;
		    //
		    // ( 1 / (stddev * SQRT_TWO_PI)) * ( Math.exp( -0.5 * (((x - mean)/stddev )^2) ) )
		    //
		    // or
		    //
		    // ( 1 / (stddev * SQRT_TWO_PI)) * ( Math.exp  (-(x - mean)^2 / (2 * stddev^2)))
		    //
		    // which is
		    //
		    // Math.exp  (-(x - mean)^2 / (2 * stddev^2)) / (stddev * SQRT_TWO_PI)
		    //
		    // For this, mean is always zero, 
		    // 'divisor' is stddev - stddev should be between
		    // 0.1 and 1.0; 0.1 is a high peak, 1.0 is shallower
		    // hump, 2.0 is a pretty flattisih hump, etc.
		    //
		    // multiply above result by amplitude to make cost
		    // 'larger'
		    //
		    // So, 
		    //		    distsq = distsq/(g.divisor*g.divisor);
// 		    double stddev = .4473;
// 		    double two_times_stddev_sqd = 2*stddev*stddev;
// 		    double x_minus_mean_sqd = distsq;
// 		    cost += g.amplitude * Math.exp(-(x_minus_mean_sqd/two_times_stddev_sqd)) / (stddev * SQRT_TWO_PI);

		    cost += g.amplitude * (1-(distsq / (g.divisor*g.divisor)));

		    //		    cost += g.amplitude / (Math.sqrt(distsq)/g.divisor);
		    //		    cost += g.amplitude * 1.0 / (Math.sqrt(distsq)/g.divisor);
		    // System.out.println("For " + dist + " adding " + (g.amplitude * 1 / (dist*dist)));
		}
	    }
        }
        
        return cost;
    }
    
    public void timeElapsed(long t) {
        // Ignoring
    }
    
    public ArrayList<Rectangle> getGoodAreas(long t) {
        return null;
    }
    
    public ArrayList<Rectangle> getBadAreas(long t) {
        return null;
    }
    
    public void addGaussian(int x, int y, double amplitude) {
        
        Gaussian g = new Gaussian();
        g.x = x;
        g.y = y;
        g.amplitude = amplitude;
        
        gaussians.add(g);
	gaussianAry = gaussians.toArray(new Gaussian[1]);
    }
    
    public void addGaussian(int x, int y, double amplitude, double divisor) {
        
        Gaussian g = new Gaussian();
        g.x = x;
        g.y = y;
        g.amplitude = amplitude;
        g.divisor = divisor;
        
        gaussians.add(g);
	gaussianAry = gaussians.toArray(new Gaussian[1]);
    }
    
    
    protected class Gaussian {
        int x = 0, y = 0;
        double amplitude = 0.0;
        double divisor = UAVRI.MIX_GAUSSIANS_COSTMAP_DEFAULT_DIVISOR;
    }
    
}

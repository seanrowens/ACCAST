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
 * GAConf.java
 *
 * Created on May 19, 2007, 4:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Machinetta.GA;

import java.util.Random;
import java.text.DecimalFormat;

/**
 *
 * @author pscerri
 */
public class GAConf {
    
    private final static DecimalFormat fmt = new DecimalFormat("0.00");
    public static int DEF_NO_EXPANSIONS = 4500;
    public static int RNG_NO_EXPANSIONS = 500;
    public static int DEF_BRANCHES_PER_EXPANSION = 2;
    public static int RNG_BRANCHES_PER_EXPANSION = 8;
    public static double DEF_MAX_THETA_CHANGE = 60;
    public static double RNG_MAX_THETA_CHANGE = 30;
    public static double DEF_MAX_PSI_CHANGE = 2;
    public static double RNG_MAX_PSI_CHANGE = 5;
//     public static double DEF_MAX_BRANCH_LENGTH = 2;
//     public static double RNG_MAX_BRANCH_LENGTH = 2;
//     public static double DEF_MIN_BRANCH_LENGTH = 0;
//     public static double RNG_MIN_BRANCH_LENGTH = .5;
     public static double DEF_MAX_BRANCH_LENGTH = .05;
     public static double RNG_MAX_BRANCH_LENGTH = .01;
     public static double DEF_MIN_BRANCH_LENGTH = 0;
     public static double RNG_MIN_BRANCH_LENGTH = .05;


    public int NO_EXPANSIONS;
    public int BRANCHES_PER_EXPANSION;
    
    /** Maximum angle between two branches, horizontally. */
    public double MAX_THETA_CHANGE;
    /** Maximum angle between two branches, vertically. */
    public double MAX_PSI_CHANGE;
    /** Maximum distance, as ratio of env width, branch can be */
    public double MAX_BRANCH_LENGTH;
    /** Minimum distance, as ratio of env width, branch can be */
    public double MIN_BRANCH_LENGTH;
    //public int RRT_BRANCH_RANGE_X_METERS;
    //public int RRT_BRANCH_RANGE_Y_METERS;
    //public int RRT_BRANCH_RANGE_Z_METERS;
    
    private Random rand = new Random(); 

    private void genNoExpansions() {
	NO_EXPANSIONS = DEF_NO_EXPANSIONS + (int)(rand.nextDouble()*RNG_NO_EXPANSIONS); 
    }

    private void genBranchesPerExpansion() {
	BRANCHES_PER_EXPANSION = DEF_BRANCHES_PER_EXPANSION + (int)(rand.nextDouble()*RNG_BRANCHES_PER_EXPANSION);
    }

    private void genMaxThetaChange() {
	MAX_THETA_CHANGE = DEF_MAX_THETA_CHANGE + (rand.nextDouble()*RNG_MAX_THETA_CHANGE);
    }

    private void genMaxPSIChange() {
        this.MAX_PSI_CHANGE = DEF_MAX_PSI_CHANGE + (rand.nextDouble()*RNG_MAX_PSI_CHANGE);
    }

    private void genMaxBranchLength() {
        this.MAX_BRANCH_LENGTH = DEF_MAX_BRANCH_LENGTH + (rand.nextDouble()*RNG_MAX_BRANCH_LENGTH);
    }

    private void genMinBranchLength() {
        this.MIN_BRANCH_LENGTH = DEF_MIN_BRANCH_LENGTH + (rand.nextDouble()*RNG_MIN_BRANCH_LENGTH);
    }

    /*
    private void genBranchRangeXYMeters() {
        this.RRT_BRANCH_RANGE_X_METERS = (int)(2 * new GAEnv().xsize * (1.0 * (MAX_BRANCH_LENGTH - MIN_BRANCH_LENGTH) + MIN_BRANCH_LENGTH));
        this.RRT_BRANCH_RANGE_Y_METERS = RRT_BRANCH_RANGE_X_METERS;
    }*/

    /** Creates a new instance of Configuration */
    //@NOTE: we need to set boundary for each value to generate random values at any place.
    public GAConf() {
	genNoExpansions();
	genBranchesPerExpansion();
	genMaxThetaChange();
	genMaxPSIChange();
	genMaxBranchLength();
	genMinBranchLength();
	//genBranchRangeXYMeters();
    }
    
    public void genNewConfig(GAConf c1, double fitness1, GAConf c2, double fitness2) {
        double sum = fitness1+fitness2;
        double weight1 = 1-(fitness1/sum);
        double weight2 = 1-(fitness2/sum);
        
        this.NO_EXPANSIONS = (int)(c1.NO_EXPANSIONS*weight1 + c2.NO_EXPANSIONS*weight2);
        this.BRANCHES_PER_EXPANSION = (int)(c1.BRANCHES_PER_EXPANSION*weight1 + c2.BRANCHES_PER_EXPANSION*weight2);
        this.MAX_THETA_CHANGE = c1.MAX_THETA_CHANGE*weight1 + c2.MAX_THETA_CHANGE*weight2;
        this.MAX_PSI_CHANGE = c1.MAX_PSI_CHANGE*weight1 + c2.MAX_PSI_CHANGE*weight2;
        this.MAX_BRANCH_LENGTH = c1.MAX_BRANCH_LENGTH*weight1 + c2.MAX_BRANCH_LENGTH*weight2;
        this.MIN_BRANCH_LENGTH = c1.MIN_BRANCH_LENGTH*weight1 + c2.MIN_BRANCH_LENGTH*weight2;
        //this.RRT_BRANCH_RANGE_X_METERS = (int)(c1.RRT_BRANCH_RANGE_X_METERS*weight1) + (int)(c2.RRT_BRANCH_RANGE_X_METERS*weight2);
        //this.RRT_BRANCH_RANGE_Y_METERS = (int)(c1.RRT_BRANCH_RANGE_Y_METERS*weight1) + (int)(c2.RRT_BRANCH_RANGE_Y_METERS*weight2);        
        
    }
    
    public void genRandomConfig(GAConf c, int windowSize) {
        if(windowSize == 3) {
	    genNoExpansions();
	    genBranchesPerExpansion();
	    genMaxThetaChange();
            this.MAX_PSI_CHANGE = c.MAX_PSI_CHANGE;
            //this.RRT_BRANCH_RANGE_X_METERS = c.RRT_BRANCH_RANGE_X_METERS;
            //this.RRT_BRANCH_RANGE_Y_METERS = c.RRT_BRANCH_RANGE_Y_METERS;
            this.MAX_BRANCH_LENGTH = c.MAX_BRANCH_LENGTH;
            this.MIN_BRANCH_LENGTH = c.MIN_BRANCH_LENGTH;
        } else if (windowSize == 2) {
	    genNoExpansions();
	    genBranchesPerExpansion();
            this.MAX_PSI_CHANGE = c.MAX_PSI_CHANGE;
            this.MAX_THETA_CHANGE = c.MAX_THETA_CHANGE;
            //this.RRT_BRANCH_RANGE_X_METERS = c.RRT_BRANCH_RANGE_X_METERS;
            //this.RRT_BRANCH_RANGE_Y_METERS = c.RRT_BRANCH_RANGE_Y_METERS;
            this.MAX_BRANCH_LENGTH = c.MAX_BRANCH_LENGTH;
            this.MIN_BRANCH_LENGTH = c.MIN_BRANCH_LENGTH;
        } else if (windowSize == 1) {
	    genNoExpansions();
            this.BRANCHES_PER_EXPANSION = c.BRANCHES_PER_EXPANSION;
            this.MAX_PSI_CHANGE = c.MAX_PSI_CHANGE;
            this.MAX_THETA_CHANGE = c.MAX_THETA_CHANGE;
            //this.RRT_BRANCH_RANGE_X_METERS = c.RRT_BRANCH_RANGE_X_METERS;
            //this.RRT_BRANCH_RANGE_Y_METERS = c.RRT_BRANCH_RANGE_Y_METERS;
            this.MAX_BRANCH_LENGTH = c.MAX_BRANCH_LENGTH;
            this.MIN_BRANCH_LENGTH = c.MIN_BRANCH_LENGTH;
        } else {
            System.out.println("windowSize is wrong!!!");
            System.out.println("Keep the current configuration.");            
        }
    }
    public String toString() { 
	return "NO_EXP="+NO_EXPANSIONS
	    +" BRNCHS="+BRANCHES_PER_EXPANSION
	    +" THETA="+fmt.format(MAX_THETA_CHANGE)
	    +" PSI="+fmt.format(MAX_PSI_CHANGE)
	    +" MAX="+fmt.format(MAX_BRANCH_LENGTH)
	    +" MIN="+fmt.format(MIN_BRANCH_LENGTH);
	    //+" XRNG="+RRT_BRANCH_RANGE_X_METERS
	    //+" YRNG="+RRT_BRANCH_RANGE_Y_METERS;
	//	return "NO_EXP="+NO_EXPANSIONS+", BRANCHES_PER_EXP="+BRANCHES_PER_EXPANSION+", MAX_THETA_CHANGE="+MAX_THETA_CHANGE+", MAX_PSI_CHANGE="+MAX_PSI_CHANGE+", MAX_BRANCH_LENGTH="+MAX_BRANCH_LENGTH+", MIN_BRANCH_LENGTH="+MIN_BRANCH_LENGTH+", RRT_BRANCH_RANGE_X_METERS="+RRT_BRANCH_RANGE_X_METERS+", RRT_BRANCH_RANGE_Y_METERS="+RRT_BRANCH_RANGE_Y_METERS;
    }
}

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
 * Main.java
 *
 * Created on May 19, 2007, 3:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Machinetta.GA;

import AirSim.Machinetta.RRTPlanner;
import AirSim.Machinetta.UAVRI;
import java.util.ArrayList;
import java.util.PriorityQueue;

/**
 *
 * @author pscerri
 */
public class Main {
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int overallIter = 1;
        int popSize = 100;
        int maxGenerations = 1000;
        int sizeOfInds = 5;
	//	RRTPlanner.setRrtPathPanelOn(true);
	//        RRTPlanner.setRrtTreePanelOn(true);
        RRTPlanner.setRrtPathPanelY(350);
        RRTPlanner.setRrtTreePanelY(350);

	RRTPlanner.mapSize = (int)new GAEnv().xsize;
	// if MapSize == 50km, this get us a 500 pixel window, if
	// mapSize = 1000, this still gets us a 500 pixel window
	RRTPlanner.mapStep = (RRTPlanner.mapSize/1000)/2;
	UAVRI.MAP_WIDTH_METERS = RRTPlanner.mapSize;
	UAVRI.MAP_HEIGHT_METERS = RRTPlanner.mapSize;
        
        /*
        for (int i=0; i<3; i++) {
            performExps(overallIter, popSize, maxGenerations, sizeOfInds, i+1);
        }*/
        
        
	performExps(overallIter, popSize, maxGenerations, sizeOfInds, 7);
	//        for (int i=7; i<8; i++) {
	//            performExps(overallIter, popSize, maxGenerations, sizeOfInds, i+1);
	//        }
	System.exit(0);
    }
    
    private static void performExps(int overallIter, int popSize, int maxGenerations, int sizeOfInds, int expNum) {
        double avgFit = 0.0;
        double sumFit = 0.0;
        double avgIter = 0.0;
        int sumIter = 0;
        int countFit = 0;
        int countIter = 0;
        
        if(expNum == 1) {
            System.out.println("*** EXP1: Without any GA");
        } else if (expNum == 2) {
            System.out.println("*** EXP2: Basic GA w/o update");
        } else if (expNum == 3) {
            System.out.println("*** EXP3: GA w/o preCal & DMM");
        } else if (expNum == 4) {
            System.out.println("*** EXP4: GA w/o preCal & w/ DMM1: SWM");
        } else if (expNum == 5) {
            System.out.println("*** EXP5: GA w/o preCal & w/ DMM2: SA");
        } else if (expNum == 6) {
            System.out.println("*** EXP6: GA w/ preCal & w/o DMM");
        } else if (expNum == 7) {
            System.out.println("*** EXP7: GA w/ preCal & DMM1: SWM");
        } else if (expNum == 8) {
            System.out.println("*** EXP8: GA w/ preCal & DMM2: SA");
        }
        
        long start = System.currentTimeMillis();
        
        for (int i=0; i<overallIter; i++) {
            //System.out.println("*** iteration " + i);
            GA ga = new GA(popSize, maxGenerations, sizeOfInds, expNum);            
            
            if(ga.keepIterate) {
                countIter++;
                ArrayList<Individual> ret = null;
                
                if(expNum == 1) {
                    ret = ga.retRandInds();
                } else {
                    ret = ga.retBestInds();
                }
                
		System.err.println("Retrieved "+ret.size()+" best individuals");

                for(Individual ind : ret) {
                    //System.out.println("Fitness: " + ind.getFitness());
                    
		    System.err.println("GAConf fitness = "+ind.getFitness()+" params = "+ind.getConfigs().get(0));
                    if(Math.abs(ind.getFitness()) < Math.pow(10, -6)) {
                        System.out.println("0!!!!!!!!!");
                    } else {
                        
                        // If you wanna print out, you can do that. Otherwise, you can simply get a solution list including best individuals,
                        // and apply them to RRT planner.
                        /*
                        System.out.println("< Configuration >\n BRANCHES_PER_EXPANSION: " + ind.getConfigs().get(0).BRANCHES_PER_EXPANSION);
                        System.out.println("< Configuration >\n RRT_BRANCH_RANGE_X_METERS: " + ind.getConfigs().get(0).RRT_BRANCH_RANGE_X_METERS);
                        System.out.println("< Configuration >\n RRT_BRANCH_RANGE_Y_METERS: " + ind.getConfigs().get(0).RRT_BRANCH_RANGE_Y_METERS);
                        System.out.println("< Configuration >\n MAX_PSI_CHANGE: " + ind.getConfigs().get(0).MAX_PSI_CHANGE);
                        System.out.println("< Configuration >\n MAX_THETA_CHANGE: " + ind.getConfigs().get(0).MAX_THETA_CHANGE);
                        System.out.println("< Configuration >\n NO_EXPANSIONS: " + ind.getConfigs().get(0).NO_EXPANSIONS);
                        */
                        countFit++;
                        sumFit += ind.getFitness();                        
                    }

                }
                
                sumIter += ga.numIterations;
            }
                
        }        
            
        long time = System.currentTimeMillis() - start;
        System.out.println("Elapsed time: " + time/1000 + "sec");
        if(countIter == 0) {
            avgIter = 0.0;
        } else {
            avgIter = (double)sumIter/countIter;
        }
        System.out.println("Final avg iterations: " + avgIter);
        if(countFit == 0) {
            avgFit = 0.0;
        } else {
            avgFit = sumFit/countFit;
        }
        System.out.println("Avg fitness: " + avgFit);
        System.out.println();
        System.out.println();
    }
}

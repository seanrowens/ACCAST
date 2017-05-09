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
 * GA.java
 *
 * Created on May 19, 2007, 4:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Machinetta.GA;

import java.util.ArrayList;
import java.util.Random;
import AirSim.Machinetta.CostMaps.CostMap;
import AirSim.Machinetta.GA.GAEnv;
import AirSim.Machinetta.GA.Vehicle;
import AirSim.Machinetta.Path3D;
import AirSim.Machinetta.RRTPlanner;
import AirSim.Machinetta.GA.RandomGaussianCostMap;
import AirSim.Machinetta.CostMaps.MixGaussiansCostMap;
import AirSim.Machinetta.CostMaps.OtherVehicleCostMap;
import AirSim.Machinetta.CostMaps.SimpleStaticCostMap;

/**
 *
 * @author pscerri
 */
public class GA {
    public final static int PRINT_BEST_EVERY_N = 100;
    
    // Configuration
    // 100/500, 500/2000 for updatePopulation1    
    public static double changeRatio = 0.8; //80.0%
    public static boolean GUIOn = false;
    public static int displayNo = 1;
    //public static int sizeOfIndividuals = 5;
    public static boolean keepIterate = false;
    public static int numIterations = 0;
    
    // Operations
    private int popSize = 0;
    private int maxGenerations = 0;
    private int sizeOfIndividuals = 0;
    private ArrayList<Individual> population = null;
    //private GUI gui = null;
    ArrayList<CostMap> cms = null;
    private double prevFitness = 0.0;
    private double curFitness = 0.0;
    private double mutationThreshold = 0.5;
    private double scoreThreshold = 0.0;
    private ArrayList<Individual> pool = null;
    private int sx = 250;
    private int sy = 250;
    private int sz = 0;
    private double sTheta = 60.0;
    private double sPsi = 0.0;
    
    private double fitnessThreshold = -150.0;
    
    /** Creates a new instance of GA */
    public GA() {
        this(100, 10000, 5, 1);
    }
    
    public GA(int popSize, int maxGenerations, int sizeOfIndividuals, int expNum) {        
        this.popSize = popSize;
        this.maxGenerations = maxGenerations;
        this.sizeOfIndividuals = sizeOfIndividuals;
        
	GAEnv gaenv = new GAEnv();
        RRTPlanner.setEnv(gaenv);
	// pick a start point using the map size from GAEnv.
	// 
	// @TODO: Test using random start point rather than center of
	// map - random is better but may break things the way they
	// are now since our fitness score is simply our path score
	// from the last path we planned, when we compare fitness we
	// have to compare fitness from the same start point
	//
	// 	sx = rand.nextInt(gaenv.xsize);
	// 	sy = rand.nextInt(gaenv.ysize);
	sx = (int)(gaenv.xsize/2);
	sy = (int)(gaenv.ysize/2);

        RRTPlanner.setVehicle(new Vehicle());
        
        SimpleStaticCostMap edgeCM = new SimpleStaticCostMap();
        cms = new ArrayList<CostMap>();
        // Need to play around with this
        // change costmap complexity based on repetition number
//         cms.add(new RandomGaussianCostMap(3, 50000, 50000));
//         cms.add(new RandomGaussianCostMap(3, 50000, 50000));
//         cms.add(new RandomGaussianCostMap(3, 50000, 50000));
//         cms.add(new RandomGaussianCostMap(3, 50000, 50000));
        cms.add(new RandomGaussianCostMap(3, 5000, 5000));
        cms.add(new RandomGaussianCostMap(3, 1000, 1000));
        cms.add(new RandomGaussianCostMap(3, 500, 2000));
        cms.add(new RandomGaussianCostMap(3, 3000, 500));
        
        // For normal
        
        population = new ArrayList<Individual>(popSize);
        pool = new ArrayList<Individual>(popSize*10);
        for (int i = 0; i < popSize; i++) {
            population.add(new Individual());
        }
        
        for (int i = 0; i < popSize*10; i++) {
            pool.add(new Individual());
        }
        
        // For testing SM
        /*
        population = new ArrayList<Individual>(sizeOfIndividuals);
        pool = new ArrayList<Individual>(popSize*10);
        
        for (int i = 0; i < popSize*10; i++) {
            pool.add(new Individual());
        }
        // SM end
        */
        
        initFitness();
        
        keepIterate = false;
        
        if(expNum >= 6) {
            // For testing preEval        
            double preEvalScore = preEvaluate();
            if(preEvalScore > this.fitnessThreshold) {
                keepIterate = true;
                //System.out.println("preEvalScore: " + preEvalScore);
            }
        } else {
            keepIterate = true;
        }
        
        if(keepIterate) {
            int worstInd = findWorstIndex(population);
            //scoreThreshold = population.get(worstInd).getFitness();
            double sum = 0.0;
            for (int i=0; i<population.size(); i++) {
                sum += population.get(i).getFitness();
            }
            scoreThreshold = sum/(double)population.size();

            curFitness = population.get(worstInd).getFitness();
            boolean isStop = false;
            int i = 0;
            int mode = 0; // 0: crossover, 1: SWM, 2: SA
            boolean isUpdate = true;

            //for (int i = 0; i < generations; i++) {
            if(expNum <= 2) {
                isStop = true;
            }
            
            while(!isStop) {
                System.out.println("Generation: " + i + "   , popSize: " + population.size());
                double genRatio = (double)i/(double)maxGenerations;

                // for normal;
                updatePopulation(null, genRatio, mode);

                // for testing SM
                /*
                isUpdate = screeningModule(genRatio, mode);

                if(isUpdate) {
                    updatePopulation(genRatio, mode);
                } else {
                    System.out.println("Do not update!");
                }
                // SM end
                */

                if(Math.abs(curFitness-prevFitness) < Math.pow(10, -3)) {
                    //System.out.println("Diff of fitness: " + (curFitness - prevFitness));
                    // For mutate!
                    
                    if(expNum == 4 || expNum == 7) {
                        if(genRatio < mutationThreshold) {
                            //System.out.println("mutation!!!!!");
                            mode = 1;
                        }
                        else
                            isStop = true;
                    } else if (expNum == 5 || expNum == 8) {
                        if(genRatio < mutationThreshold) {
                            //System.out.println("mutation!!!!!");
                            mode = 2;
                        }
                        else
                            isStop = true;
                    } else {
                        isStop = true;
                    }
                    
                } else
                    mode = 0;

                i++;

                if(maxGenerations <= i)
                    isStop = true;

		// @TODO: Wed Apr 23 20:10:51 EDT 2008 SRO - For some
		// reason I'm totally unable to figure out, when this
		// code is uncommented, printBestInds() is called, the
		// experiment exits immediately without printing
		// anything and without doing further updates to the
		// pool of individuals.  I.e. if it's supposed to do
		// 1000 iterations, adn print every 100, the first
		// time we reach 100 it just ends instead of printing
		// anything or doing the other 900.  Aieee!  I have NO
		// idea why.
		//
                
                // @NOTE: Thr Apr 24 11:45:00 EDT 2008 jun - I tested this code
                // and this is working well with all experiments. Please 
                // test this code again. The reason why this code somtimes stops before the maximum
                // generation reaches is I'm applying two different stop conditions.
                // One is checking the difference btw current and previous avg fitness,
                // and if that is smaller than some threshold (i.e. currently, 10^-3)
                // value, then it stops, and print the best set of configurations. (This means
                // GA already reaches the pretty good quality value now.)
                // The other one is, during the update process, if it does not converge and reaches
                // the maximum iteration number, then it stops and print out the current best
                // set of configurations.
                // In conclusion, for the former case, you might not see the printBestInds result,
                // and of course printBestInds method does not affect the operation of GA process at all.
   		if(0 == (i % PRINT_BEST_EVERY_N)) {
                    System.err.println("**** Current iteration num: " + i);
  		    printBestInds(this.sizeOfIndividuals);
  		}
            }
            
            this.numIterations = i;
            
            /*
            //Display
            if(GUIOn) {
                gui = new GUI(displayNo, 1000, 1000);
                gui.setVisible(true);
                ArrayList<Individual> ret = retBestInds();

                for (int j = 0; j < ret.size(); j++) {
                    // Set up the planner according to the individual
                    Individual ind = ret.get(4);
                    Planner.setConfigs(ind.getConfigs());
                    // Do a planning
                    Path3D path = Planner.plan(sx, sy, sz, sTheta, sPsi, 0, cms);

                    if (j < displayNo*displayNo) {
                        gui.update(j, cms, path, Planner.queue);
                    }
                }
            }
             **/
        }
        
    }
    
    public ArrayList<Individual> retBestInds() {
        ArrayList<Individual> inds = new ArrayList<Individual>(sizeOfIndividuals);
               
        //init
        for (int i=0; i<sizeOfIndividuals; i++) {
            inds.add(population.get(i));
        }
        int worstIndex = findWorstIndex(inds);
        
        for (int i=sizeOfIndividuals; i<population.size(); i++) {
            Individual ind = population.get(i);
            double worstFitness = inds.get(worstIndex).getFitness();
            if(ind.getFitness() > worstFitness) {
                inds.remove(worstIndex);
                inds.add(worstIndex, ind);
                worstIndex = findWorstIndex(inds);
            }
        }
        
        return inds;
    }
    
    public ArrayList<Individual> retRandInds() {
        ArrayList<Individual> inds = new ArrayList<Individual>(sizeOfIndividuals);
        
        Random rand = new Random();
        int index = 0;
        
        //init
        for (int i=0; i<sizeOfIndividuals; i++) {
            index = rand.nextInt(population.size());
            inds.add(population.get(index));
        }
                
        return inds;
    }
    
    private int findWorstIndex(ArrayList<Individual> inds) {
        double worst = Double.MAX_VALUE;
        int retIndex = -1;
        
        for (int i=0; i<inds.size(); i++) {
            Individual ind = inds.get(i);
            
            if(worst > ind.getFitness()) {
                worst = ind.getFitness();
                retIndex = i;
            }
        }
        
        return retIndex;
    }
    
    private int findBestIndex(ArrayList<Individual> inds) {
        double best = -Double.MAX_VALUE;
        int retIndex = -1;
        
        for (int i=0; i<inds.size(); i++) {
            Individual ind = inds.get(i);
            
            if(best < ind.getFitness()) {
                best = ind.getFitness();
                retIndex = i;
            }
        }
        
        return retIndex;
    }
    
    // need to update wrt pool init and update
    private void initFitness() {
        // For normal;
        
        for (int i = 0; i < population.size(); i++) {
            // Set up the planner according to the individual
            Individual ind = population.get(i);
            RRTPlanner.setConfigs(ind.getConfigs());
            // Do a planning
	    long start = System.currentTimeMillis();
	    //System.err.println("1: GAConf="+ind.getConfigs().get(0).toString());
	    //System.err.print("1: Calling RRTPlanner.plan at "+start);
            Path3D path = RRTPlanner.plan(sx, sy, sz, 0, cms);
	    long end = System.currentTimeMillis();
	    //System.err.println("  ... Done at "+System.currentTimeMillis() +" elapsed ms="+(end - start) +" size="+path.size()+" length="+path.getLength()+" score= "+path.getScore()+", path="+path.toString());
            //System.out.println("path size: " + path.waypoints.size());
            ind.setFitness(path.getScore());
            population.set(i, ind);
        }      
        
        // For testing SM. 
        /*
        for (int i = 0; i < pool.size(); i++) {
            // Set up the planner according to the individual
            Individual ind = pool.get(i);
            Planner.setConfigs(ind.getConfigs());
            // Do a planning
            Path3D path = Planner.plan(sx, sy, sz, sTheta, sPsi, 0, cms);
            //System.out.println("path size: " + path.waypoints.size());
            ind.setFitness(path.getScore());
            pool.set(i, ind);
        }
        
        Random rand = new Random();
        int index = 0;
        
        for (int i=0; i<sizeOfIndividuals; i++) {
            index = rand.nextInt(pool.size());
            population.add(pool.get(index));
        }*/
        // SM end
    }
    
    private double preEvaluate() {
        ArrayList<Individual> preSet = this.retRandInds();
        
        for (int i=0; i<100; i++) {
            double genRatio = (double)i/(double)maxGenerations;            
            // for normal;
            updatePopulation(preSet, genRatio, 0);
        }
        
        double sum = 0.0;
        for (int i=0; i<preSet.size(); i++) {
            sum += preSet.get(i).getFitness();
        }
        
        return sum/(double)preSet.size();
    }
    
    private ArrayList<Individual> selectParents(ArrayList<Individual> preSet) {
        ArrayList<Individual> ret = new ArrayList<Individual>(2);
        double sum = 0.0;
        double sumProb = 0.0;
        
        if(preSet == null)
            preSet = population;
        
        double[] probs = new double[preSet.size()];
        
        for (int i=0; i<preSet.size(); i++) {
            sum += preSet.get(i).getFitness();
        }
        
        //System.out.println("sum: "+ sum);
        
        for (int i=0; i<preSet.size(); i++) {
            probs[i] = sumProb + (preSet.get(i).getFitness()/sum);
            sumProb += probs[i];
        }
        
        for (int k=0; k<2; k++) {
            Random rand = new Random();
            double number = rand.nextDouble();

            if(number <= probs[0]) {
                ret.add(preSet.get(0));
            } else {
                for (int i=0; i<preSet.size()-1; i++) {
                    if(number > probs[i] && number <= probs[i+1]) {
                        ret.add(preSet.get(i+1));                    
                    }
                }
            }
        }
        
        return ret;
    }
    
    private ArrayList<Individual> selectParents2(ArrayList<Individual> keepPopulation) {
        ArrayList<Individual> ret = new ArrayList<Individual>(2);
        double sum = 0.0;
        double sumProb = 0.0;
        double[] probs = new double[keepPopulation.size()];
        
        for (int i=0; i<keepPopulation.size(); i++) {
            sum += keepPopulation.get(i).getFitness();
        }
        
        //System.out.println("sum: "+ sum);
        
        for (int i=0; i<keepPopulation.size(); i++) {
            probs[i] = sumProb + (keepPopulation.get(i).getFitness()/sum);
            sumProb += probs[i];
        }
        
        for (int k=0; k<2; k++) {
            Random rand = new Random();
            double number = rand.nextDouble();

            if(number <= probs[0]) {
                ret.add(keepPopulation.get(0));
            } else {
                for (int i=0; i<keepPopulation.size()-1; i++) {
                    if(number > probs[i] && number <= probs[i+1]) {
                        ret.add(keepPopulation.get(i+1));                    
                    }
                }
            }
        }
        
        return ret;
    }
    
    private Individual generateOffspring(ArrayList<Individual> preSet, double genRatio, int mode) {
        ArrayList<Individual> parents = selectParents(preSet);
        Individual offspring = null;
        
        if(parents.size() != 0) {
            if(mode == 0) {
                offspring = crossover(parents);
            } else if (mode == 1) {
                offspring = mutation(parents, genRatio);
            } else if (mode == 2) {
                offspring = simulatedAnnealing(parents);
            }
        }
        
        return offspring;
    }
    
    private void updatePopulation(ArrayList<Individual> preSet, double genRatio, int mode) {
        //at this time, we just consider crossover to generate offspring
        //System.out.println("parent? " + parents.size());
        Individual offspring = generateOffspring(preSet, genRatio, mode);
        
        if(offspring != null) {
            RRTPlanner.setConfigs(offspring.getConfigs());
            // Do a planning
	    long start = System.currentTimeMillis();
	    //System.err.println("2: GAConf="+offspring.getConfigs().get(0).toString());
	    //System.err.print("2: Calling RRTPlanner.plan at "+start);
            Path3D path = RRTPlanner.plan(sx, sy, sz, 0, cms);
	    long end = System.currentTimeMillis();
	    //System.err.println("  ... Done at "+System.currentTimeMillis() +" elapsed ms="+(end - start)+" score= "+path.getScore() +" size="+path.size()+" length="+path.getLength());
            double score = path.getScore();
            offspring.setFitness(score);
            
            if(preSet == null) {
                if(population.size() < popSize) {
                    population.add(offspring);

                    prevFitness = curFitness;
                    curFitness = score;
                } else {
                    double worst = Double.MAX_VALUE;
                    int index = -1;

                    for(int i=0; i<population.size(); i++) {
                        if(worst > population.get(i).getFitness()) {
                            worst = population.get(i).getFitness();
                            index = i;
                        }  
                    }

                    if(score > worst) {
                        population.remove(index);
                        population.add(index, offspring);

                        prevFitness = curFitness;
                        curFitness = score;
                    }
                }
            } else {
                double worst = Double.MAX_VALUE;
                int index = -1;

                for(int i=0; i<preSet.size(); i++) {
                    if(worst > preSet.get(i).getFitness()) {
                        worst = preSet.get(i).getFitness();
                        index = i;
                    }  
                }

                if(score > worst) {
                    preSet.remove(index);
                    preSet.add(index, offspring);

                    prevFitness = curFitness;
                    curFitness = score;
                }
            }
        }
    }
    
    private void updatePopulation2() {
        //at this time, we just consider crossover to generate offspring
        //System.out.println("parent? " + parents.size());
        int changeNum = (int)(population.size()*changeRatio);
        int keepNum = population.size() - changeNum;
        ArrayList<Individual> removePopulation = population;
        ArrayList<Individual> keepPopulation = new ArrayList(keepNum);
        ArrayList<Individual> newPopulation = new ArrayList(changeNum);
        
        for (int i=0; i<keepNum; i++) {
            int index = findBestIndex(removePopulation);
            keepPopulation.add(removePopulation.get(index));
            removePopulation.remove(index);
        }
        
        for (int i=0; i<changeNum; i++) {
            ArrayList<Individual> parents = selectParents2(keepPopulation);
        
            if(parents.size() != 0) {
                Individual offspring = crossover(parents);
                RRTPlanner.setConfigs(offspring.getConfigs());
                // Do a planning
                Path3D path = RRTPlanner.plan(sx, sy, sz, 0, cms);
                offspring.setFitness(path.getScore());
                newPopulation.add(offspring);
            }
        }

        population.clear();
        population = newPopulation;
        for (int i=0; i<keepNum; i++) {
            population.add(keepPopulation.get(i));
        }
        
        //System.out.println("new generated pop size: " + population.size());
    }
    
    private Individual crossover(ArrayList<Individual> parents) {
        Individual offspring = new Individual();
        
        Individual ind0 = parents.get(0);
        Individual ind1 = parents.get(1);
        
        GAConf temp = offspring.getConfigs().get(0);
        temp.genNewConfig(ind0.getConfigs().get(0), ind0.getFitness(), ind1.getConfigs().get(0), ind1.getFitness());
        offspring.setConfig(0, temp);
        
        return offspring;
    }
    
    private Individual mutation(ArrayList<Individual> parents, double genRatio) {
        Individual offspring = crossover(parents);
        GAConf temp = offspring.getConfigs().get(0);
        
        if(genRatio > mutationThreshold && genRatio <= mutationThreshold + 0.2) {
            temp.genRandomConfig(temp, 3);
        } else if (genRatio > mutationThreshold + 0.2 && genRatio <= mutationThreshold + 0.4) {
            temp.genRandomConfig(temp, 2);
        } else if (genRatio > mutationThreshold + 0.4) {
            temp.genRandomConfig(temp, 1);
        }
        offspring.setConfig(0, temp);
        
        return offspring;
    }
    
    private Individual simulatedAnnealing(ArrayList<Individual> parents) {
        Individual offspring = crossover(parents);
        GAConf s0 = offspring.getConfigs().get(0);
        GAConf s = s0;
        GAConf sb = s;
        RRTPlanner.setConfigs(offspring.getConfigs());
        // Do a planning
        Path3D path = RRTPlanner.plan(sx, sy, sz, 0, cms);
        double e = path.getScore();
        double eb = e;
        int k = 0;
        int kmax = 20;
        Random rand = new Random();
        GAConf sn = s;
        double scaleFactor = 0.3;
        
        while(k < kmax) {
            sn.genRandomConfig(s, 3);
            offspring.setConfig(0, sn);
            RRTPlanner.setConfigs(offspring.getConfigs());
            // Do a planning
            path = RRTPlanner.plan(sx, sy, sz, 0, cms);
            double en = path.getScore();
            
            if(en > eb) {
                sb = sn;
                eb = en;
            }
            
            //System.out.println("    en: " + en + "  , e: " + e);
            //System.out.println("    ***Prob: " + (1 - Math.exp(-Math.abs(en - e)*(scaleFactor*(double)k+1/(double)kmax))));
            if ((1 - Math.exp(-Math.abs(en - e)*(scaleFactor*(double)k+1/(double)kmax))) > rand.nextDouble()) {
                s = sn;
                e = en;
            }
            
            k++;
        }
        
        offspring.setConfig(0, sb);
        
        return offspring;
    }
    
    // need to update
    private boolean screeningModule(double genRatio, int mode) {
        int worstInd = findWorstIndex(population);
        //scoreThreshold = population.get(worstInd).getFitness();
        double sum = 0.0;
        for (int i=0; i<population.size(); i++) {
            sum += population.get(i).getFitness();
        }
        scoreThreshold = sum/(double)population.size();
        
        System.out.println("scoreThreshold: " + scoreThreshold);
        int k=20;
        Individual offspring = generateOffspring(null, genRatio, mode);
        ArrayList<Individual> nearestList = findNearestInds(offspring, k);
        boolean isUpdate = false;
        int count = 0;
        
        for (int i=0; i<nearestList.size(); i++) {
            Individual ind = nearestList.get(i);
            if(scoreThreshold < ind.getFitness()) {
                //isUpdate = true;
                //continue;
                count++;
            }
        }
        
        System.out.println("Count? " + count);
        if (count > (double)k/2.0) {
            isUpdate = true;
        }
        return isUpdate;
    }
    
    // return nomalizedDistance with respect to the fitness of individuals
    // need to update
    private double distanceBtwInds(Individual ind1, Individual ind2) {
        return -Math.abs(ind1.getFitness() - ind2.getFitness())/scoreThreshold;
    }
    
    // K-nearest neighbor
    private ArrayList<Individual> findNearestInds(Individual ind, int k) {
        ArrayList<Individual> retArray = new ArrayList<Individual>(k);
        
        //init
        for (int i=0; i<k; i++) {
            retArray.add(pool.get(i));
        }
        int worstIndex = findFarestIndex(ind, retArray);
        
        for (int i=k; i<pool.size(); i++) {
            Individual indTemp = pool.get(i);
            double biggestDist = distanceBtwInds(ind, retArray.get(worstIndex));
            
            if(distanceBtwInds(ind, indTemp) < biggestDist) {
                retArray.remove(worstIndex);
                retArray.add(worstIndex, indTemp);
                worstIndex = findFarestIndex(ind, retArray);
            }
        }
        
        return retArray;
    }
    
    private int findFarestIndex(Individual ind, ArrayList<Individual> inds) {
        double worst = -Double.MAX_VALUE;
        int retIndex = -1;
        
        for (int i=0; i<inds.size(); i++) {
            Individual indTemp = inds.get(i);
            double dist = distanceBtwInds(ind, indTemp);
            
            if(worst < dist) {
                worst = dist;
                retIndex = i;
            }
        }
        
        return retIndex;
    }

    public void printBestInds(int numInds) {
        double avgFit = 0.0;
        double sumFit = 0.0;
        int countFit = 0;

	ArrayList<Individual> ret = null;
	ret = retBestInds();
        System.err.println("printBestInds: found "+ret.size()+" best inds");
	int count = 0;
	for(Individual ind : ret) {
	    count++;
	    System.err.println("printBestInds: GAConf "+count+" fitness = "+ind.getFitness()+" params = "+ind.getConfigs().get(0));
	    countFit++;
	    sumFit += ind.getFitness();                        
	}
        if(countFit == 0) {
            avgFit = 0.0;
        } else {
            avgFit = sumFit/countFit;
        }
        System.err.println("printBestInds: Avg fitness: " + avgFit);
    }

}

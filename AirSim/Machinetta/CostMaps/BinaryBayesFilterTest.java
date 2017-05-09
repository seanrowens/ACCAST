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
 * BinaryBayesFilterTest.java
 *
 * Created on December 5, 2006, 10:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Machinetta.CostMaps;

import AirSim.Environment.Env;
import AirSim.Environment.Vector3D;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.SA9;
import AirSim.Environment.Assets.SmallUAV;
import AirSim.Environment.Assets.Sensors.RSSISensor;
import AirSim.Environment.Assets.Sensors.RSSISensorReading;
import AirSim.Machinetta.CostMaps.BinaryBayesDisplay;
import AirSim.SensorReading;
import AirSim.Machinetta.Beliefs.RSSIReading;
import AirSim.Environment.Assets.Emitter;

import AirSim.Machinetta.UAVRI;
import AirSim.Machinetta.BeliefShare;
import AirSim.Environment.Assets.Sensors.EmitterModel;
import Gui.DoubleGrid;
import Machinetta.ConfigReader;
import Machinetta.Configuration;
import java.util.ArrayList;
import java.awt.Point;
import java.util.Random;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 * @author Sean Owens
 */
public class BinaryBayesFilterTest {
    
    private final static double UNIFORM_PRIOR = 0.01;

    private int repaintSleep = 1000;
    private int mapSizeMeters;
    private int gridSize;
    private int numUavs;
    private boolean subfilterson = false;
    private double gridScaleFactor;
    private double emitterModelScaleFactor;
    private int DEFAULT_ALTITUDE = 0;
    private int botLeftX;
    private int botLeftY;
    private int rangeX;
    private int rangeY;
    
    private double sensorMaxRangeMeters;
    private double sensorMinRangeMeters;
    private int moveRangeX;
    private int moveRangeY;
    
    private Env env;
    private SA9 emitter;
    private SmallUAV[] uavs;
    private Vector3D[] uavVectors;
    private RSSISensor[] sensors;
    private static int probDistPanelX = 1100;
    private static int probDistPanelY = 520;
    private static ArrayList<Point> uavPoints = new ArrayList<Point>();
    private static ArrayList<Point> emitterPoints = new ArrayList<Point>();
    private static ArrayList<EmitterModel> emitterModels = new ArrayList<EmitterModel>();
    private Random rand;
    private EmitterModel emitterModel;

//     private JFrame bbfFrame = null;
    private BinaryBayesDisplay bbfDisplay = null;

    private final static int BBF_DISPLAY_X = 20;
    private final static int BBF_DISPLAY_Y = 20;

    private BinaryBayesFilter bbf;
    private BinaryBayesFilter bbfLeft;
    private ArrayList<BinaryBayesFilter> bbfList = new ArrayList<BinaryBayesFilter>();
    private ArrayList<JFrame> bbfFrameList = new ArrayList<JFrame>();
    private ArrayList<BinaryBayesDisplay> bbfDisplayList = new ArrayList<BinaryBayesDisplay>();

    private BinaryBayesFilter bbfSmall;
    private static JFrame bayesFilterSmallFrame = null;
    private static BinaryBayesDisplay bayesFilterSmallPanel = null;
    
    private ProbDistDisplay pdd;

    private ClusterUtils clusterUtils;

    private class RepaintTicker implements Runnable {
        public void run() {
            while(true) {
                try {
                    Thread.sleep(repaintSleep);
                } catch (InterruptedException e) {
                }
		for(int loopi = 0; loopi < bbfList.size(); loopi++) {
		    JFrame frame = bbfFrameList.get(loopi);
		    frame.repaint();
		}
            }
        }
    }
    
    private BinaryBayesDisplay createDisplay(String title, int x, int y) {
        JFrame bbfFrame = new JFrame("BBF Beliefs "+title);
        BinaryBayesDisplay bbfDisplay = new BinaryBayesDisplay(-20,-20, null, null, false, true,mapSizeMeters, mapSizeMeters);
        bbfDisplay.setEmitters(emitterPoints);
        bbfFrame.setLocation(x,y);
        bbfFrame.getContentPane().setLayout(new BorderLayout());
        bbfFrame.getContentPane().add(bbfDisplay, BorderLayout.CENTER);
        bbfFrame.pack();
	bbfFrame.setSize(550, 550);
	//	bbfFrame.setSize(gridSize/2+50, gridSize/2+50);
	//        bbfFrame.setSize(gridSize+50, gridSize+50);
        bbfFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        Machinetta.Debugger.debug("Done creating bbfDisplay", 1, "RRTPlanner");
        bbfFrame.setVisible(true);
	bbfFrameList.add(bbfFrame);
	bbfDisplayList.add(bbfDisplay);
	return bbfDisplay;
    }

    private void setDisplayData() {
	for(int loopi = 0; loopi < bbfList.size(); loopi++) {
	    BinaryBayesFilter filter = bbfList.get(loopi);
	    double [][] beliefs = filter.getBeliefs();	    
	    BinaryBayesDisplay disp = bbfDisplayList.get(loopi);
	    disp.setData(beliefs, uavPoints);
	}
    }

    private void updateReadings(RSSIReading reading) {
	for(int loopi = 0; loopi < bbfList.size(); loopi++) {
	    BinaryBayesFilter filter = bbfList.get(loopi);
	    long startTime = System.currentTimeMillis();
	    //	    BinaryBayesFilter.subfilter_instance_update_count = 0;
	    filter.updateBinaryFilter(reading);
	    long endTime = System.currentTimeMillis();
// 	    if((endTime - startTime) > 30)
// 		Machinetta.Debugger.debug("time to update filter="+(endTime - startTime)+" subfilters updated="+BinaryBayesFilter.subfilter_instance_update_count,1,this);
	}
    }

    public BinaryBayesFilterTest(double moveMultiplier, int mapSizeMeters, int gridSize, int numUavs, int repaintSleep, int numEmitters, boolean randomPlace, boolean subfilterson) {

        this.mapSizeMeters = mapSizeMeters;
        this.gridSize = gridSize;
        this.numUavs = numUavs;
	this.repaintSleep = repaintSleep;
	this.subfilterson = subfilterson;

	// ======================================================================
	// @TODO: 
	//
	// Very important to set RSSISensor.MAP_SCALE, otherwise the
	// sensor will create the model with the default MAP_SCALE
	// value of 1.0 and it simply won't work.  This is kinda
	// hackish.  Normally, in Sanjaya, this is set by Env.java
	// when it loads Env.txt.
	//
	// ok, so to clarify a bit - cause I messed this up earlier,
	// it just happened to work out ok at the time.  Our emitter
	// model was built by Robin using some data that assumed
	// things like "sensor ranges are 20 to 30 meters" and "we're
	// looking for something in a 1km x 1km map".  In order to
	// have something usable for a 50km x 50km map, we 'stretch'
	// that model over the entire map - hence we're always scaling
	// the model from its original 1km x 1km, to the size of the
	// map.  It just so happened for a long time that we were
	// always using 50km maps, hence RSSISensor.MAP_SCALE was 50,
	// and we also happened to always be using 1000x1000 grids, so
	// gridScaleFactor was ALSO 50 all the time, so there was some
	// confusion between the two values. But since we're now using
	// different grid sizes, we have to be more careful about
	// this.  We _also_ have to pass that into the
	// BinaryBayesFilter now.  Really we should just pass in an
	// EmitterModel instance.  Maybe later.
	//
	// ======================================================================
	emitterModelScaleFactor = mapSizeMeters/EmitterModel.ORIGINAL_SIZE_METERS;
	RSSISensor.MAP_SCALE= emitterModelScaleFactor;

        gridScaleFactor = mapSizeMeters/gridSize;

        botLeftX = 0;
        botLeftY = 0;
        rangeX = mapSizeMeters;
        rangeY = mapSizeMeters;
        moveRangeX = (int)((mapSizeMeters/1000)*moveMultiplier);
        moveRangeY = (int)((mapSizeMeters/1000)*moveMultiplier);
        Machinetta.Debugger.debug("moveRangeX="+moveRangeX+", moveRangeY="+moveRangeY,1,this);

	// auto-size the sensor ranges based on map size - for big
	// map, big ranges, for small map, small ranges.
	sensorMaxRangeMeters = mapSizeMeters/10;
	sensorMinRangeMeters = mapSizeMeters/20;
        Machinetta.Debugger.debug("sensorMzxRangeMeters="+sensorMaxRangeMeters+", sensorMinRangeMeters="+sensorMinRangeMeters,1,this);
        
        int emitterx = (mapSizeMeters/2) + (int)gridScaleFactor/2;
        int emittery = (mapSizeMeters/2) + (int)gridScaleFactor/2;
        
        rand = new Random();
        env = new Env();
	if(numEmitters <= 1) {
	    // base class Asset adds the SA9 to Env in constructor.
	    emitter = new SA9("SA9-1", emitterx, emittery);	
	    emitterPoints.add(new Point(emitterx,emittery));
	}
	else if(numEmitters <= 2) {
	    // base class Asset adds the SA9 to Env in constructor.
	    emitter = new SA9("SA9-1", emitterx, emittery);	
	    emitterPoints.add(new Point(emitterx,emittery));
	    // base class Asset adds the SA9 to Env in constructor.
	    emitter = new SA9("SA9-2", emitterx-15000, emittery);	
	    emitterPoints.add(new Point(emitterx-15000,emittery));
	}
	else {
	    // base class Asset adds the SA9 to Env in constructor.
	    emitter = new SA9("SA9-1", emitterx, emittery);	
	    emitterPoints.add(new Point(emitterx,emittery));
	    for(int loopi = 1; loopi < numEmitters; loopi++) {
		emitterx = rand.nextInt(mapSizeMeters);
		emittery = rand.nextInt(mapSizeMeters);
		// base class Asset adds the SA9 to Env in constructor.
		emitter = new SA9("SA9-"+loopi, emitterx, emittery);	
		emitterPoints.add(new Point(emitterx,emittery));
	    }
	    
	}

// 	emitterModels.add(new EmitterModel(emitterx, emittery, gridScaleFactor));
        
        sensors = new RSSISensor[numUavs];
        uavs = new SmallUAV[numUavs];
        uavVectors = new Vector3D[numUavs];
        Vector3D emitterVec = new Vector3D(emitterx, emittery, DEFAULT_ALTITUDE);
        for(int loopi = 0; loopi < sensors.length; loopi++) {
            int x = botLeftX + rand.nextInt(rangeX);
            int y = botLeftY + rand.nextInt(rangeY);
            int z = DEFAULT_ALTITUDE;
            uavs[loopi] = new SmallUAV("RSSI-UAV"+loopi,x,y,z);
            sensors[loopi] = new RSSISensor();
            uavs[loopi].addSensor(sensors[loopi]);
            double vectorx = rand.nextDouble()*2.0 - 1.0;
            double vectory = rand.nextDouble()*2.0 - 1.0;
            uavVectors[loopi] = new Vector3D(vectorx, vectory, 0);
            uavVectors[loopi].normalize();
            uavVectors[loopi]= emitterVec.toVector(x,y,z);
            uavVectors[loopi].setLength(moveRangeX);
            uavVectors[loopi].normalize2d();
            emitterModels.add(new EmitterModel(emitterx, emittery, gridScaleFactor));
	    //	    emitterModels.add(new EmitterModel(emitterx, emittery, 1.0));
        }       
        
	//	bbf = new BinaryBayesFilter(gridSize/10, 0, 0, gridScaleFactor*10, emitterModelScaleFactor, sensorMaxRangeMeters, sensorMinRangeMeters, UNIFORM_PRIOR, UAVRI.BBF_DIFFUSE_INTERVAL_MS, UAVRI.BBF_DIFFUSE_DELTA);
	//	bbf = new BinaryBayesFilter(1000, 20000, 20000, 10, emitterModelScaleFactor, sensorMaxRangeMeters, sensorMinRangeMeters, UNIFORM_PRIOR, UAVRI.BBF_DIFFUSE_INTERVAL_MS, UAVRI.BBF_DIFFUSE_DELTA);
	// 100x100 grid, scalefactor is 500 meters, hence 50km x 50km
	//	bbf = new BinaryBayesFilter(gridSize/10, 0, 0, gridScaleFactor*10, emitterModelScaleFactor, sensorMaxRangeMeters, sensorMinRangeMeters, UNIFORM_PRIOR, UAVRI.BBF_DIFFUSE_INTERVAL_MS, UAVRI.BBF_DIFFUSE_DELTA,false,false);
	//	bbf = new BinaryBayesFilter(gridSize/10, 0, 0, gridScaleFactor*10, emitterModelScaleFactor, sensorMaxRangeMeters, sensorMinRangeMeters, UNIFORM_PRIOR, UAVRI.BBF_DIFFUSE_INTERVAL_MS, 0,false,false);

// 	int mainGridSize = gridSize/10;
// 	double mainGridScaleFactor = gridScaleFactor*10;
	int mainGridSize = gridSize;
	double mainGridScaleFactor = gridScaleFactor;
	bbf = new BinaryBayesFilter(-1,mainGridSize, 0, 0, mainGridScaleFactor, emitterModelScaleFactor, sensorMaxRangeMeters, sensorMinRangeMeters, UNIFORM_PRIOR, UAVRI.BBF_DIFFUSE_INTERVAL_MS, 0,true,subfilterson);
	bbfList.add(bbf);
	bbfDisplay = createDisplay("Main",BBF_DISPLAY_X, BBF_DISPLAY_Y);

	// @TODO: need to create the guis we hand in a this point.
	BinaryBayesFilterCostMap bbfCM = new BinaryBayesFilterCostMap(mapSizeMeters,mapSizeMeters,null,null,null,bbf);
	clusterUtils = new ClusterUtils(mainGridScaleFactor,sensorMaxRangeMeters,gridSize);

	//	bbf = new BinaryBayesFilter(gridSize, 0, 0, gridScaleFactor, emitterModelScaleFactor, sensorMaxRangeMeters, sensorMinRangeMeters, UNIFORM_PRIOR, UAVRI.BBF_DIFFUSE_INTERVAL_MS, UAVRI.BBF_DIFFUSE_DELTA);

//  	// 100x100 grid, scalefactor is 50 meters, hence 5000m x 5000m, located left center
//   	bbf = new BinaryBayesFilter(gridSize/10, 25000 - 7500, 25000-2500, gridScaleFactor, emitterModelScaleFactor, sensorMaxRangeMeters, sensorMinRangeMeters, UNIFORM_PRIOR, UAVRI.BBF_DIFFUSE_INTERVAL_MS, 0,false,false);
//   	bbfList.add(bbf);
//   	createDisplay("Small Left",BBF_DISPLAY_X, BBF_DISPLAY_Y+500);
//  	bbfLeft = bbf;

//  	// 100x100 grid, scalefactor is 50 meters, hence 5000m x 5000m, located dead center
// 	//  	bbf = new BinaryBayesFilter(gridSize/10, 25000 - 2500, 25000-2500, gridScaleFactor, emitterModelScaleFactor, sensorMaxRangeMeters, sensorMinRangeMeters, UNIFORM_PRIOR, UAVRI.BBF_DIFFUSE_INTERVAL_MS, UAVRI.BBF_DIFFUSE_DELTA,false,false);
//   	bbf = new BinaryBayesFilter(gridSize/10, 25000 - 2500, 25000-2500, gridScaleFactor, emitterModelScaleFactor, sensorMaxRangeMeters, sensorMinRangeMeters, UNIFORM_PRIOR, UAVRI.BBF_DIFFUSE_INTERVAL_MS, 0,false,false);
//   	bbfList.add(bbf);
//   	createDisplay("Small Center",BBF_DISPLAY_X+500, BBF_DISPLAY_Y+500);

//   	// 100x100 grid, scalefactor is 5 meters, hence 500m x 500m, located right center
//    	bbf = new BinaryBayesFilter(gridSize/10, 25000 - 250, 25000-250, gridScaleFactor/10, emitterModelScaleFactor, sensorMaxRangeMeters, sensorMinRangeMeters, UNIFORM_PRIOR, UAVRI.BBF_DIFFUSE_INTERVAL_MS, 0,false,false);
//    	bbfList.add(bbf);
//    	createDisplay("Smaller Center",BBF_DISPLAY_X+500, BBF_DISPLAY_Y);


	//        bbf = new BinaryBayesFilter(gridSize, 25000 - 2500, 25000-2500, gridScaleFactor/10, emitterModelScaleFactor, sensorMaxRangeMeters, sensorMinRangeMeters, UNIFORM_PRIOR, UAVRI.BBF_DIFFUSE_INTERVAL_MS, UAVRI.BBF_DIFFUSE_DELTA);
        
        
        
        EmitterModel[] emitterAry = emitterModels.toArray(new EmitterModel[1]);
	//         pdd = new ProbDistDisplay(gridScaleFactor,emitterAry);
	//         pdd.setLocation(new Point(probDistPanelX,probDistPanelY));
 	//         pdd.setVisible(true);
    }
    
    public void run() {
        RepaintTicker repainter = new RepaintTicker();
        Thread repaintThread = new Thread(repainter);
        repaintThread.start();
        Random randSig = new Random(123456789);
        int numIterations = 0;
	int lastHighPixelCount = 0;
	int lastHighPixelCountReps = 0;
	int clusterCounter = 0;
	int clusterCounter2 = 0;
        while(true) {
            numIterations++;
	    if(0 == (numIterations % 10)) 
		Machinetta.Debugger.debug("number of Iterations now "+numIterations,1,this);
            uavPoints.clear();
            for(int loopk = 0; loopk < 1; loopk++) {
                for(int loopi = 0; loopi < sensors.length; loopi++) {
                    SmallUAV uav = uavs[loopi];
                    Vector3D vector = uavVectors[loopi];
                    
                    // update uav location randomly
                    boolean MOVE_RANDOMLY = false;
                    if(MOVE_RANDOMLY) {
                        int ew = rand.nextInt(3);
                        int ns = rand.nextInt(3);
                        if (ew == 0) uav.location.x-=moveRangeX;
                        else if (ew == 2) uav.location.x+=moveRangeX;
                        if (ns == 0) uav.location.y-=moveRangeY;
                        else if (ns == 2) uav.location.y+=moveRangeY;
                        
                        if(uav.location.x > rangeX){
                            uav.location.x = rangeX - 1;
                        }
                        if(uav.location.x < botLeftX){
                            uav.location.x = 1;
                        }
                        if(uav.location.y > rangeY){
                            uav.location.y = rangeY - 1;
                        }
                        if(uav.location.y < botLeftY){
                            uav.location.y = 1;
                        }
                    } else {
                        uav.location.x += vector.x;
                        uav.location.y += vector.y;
                        if(uav.location.x < botLeftX) {
                            uav.location.x = botLeftX;
                            vector.turn(180);
                        }
                        if(uav.location.x > botLeftX+rangeX) {
                            uav.location.x = botLeftX+rangeX;
                            vector.turn(180);
                        }
                        if(uav.location.y < botLeftY) {
                            uav.location.y = botLeftY;
                            vector.turn(180);
                        }
                        if(uav.location.y > botLeftY+rangeY) {
                            uav.location.y = botLeftY+rangeY;
                            vector.turn(180);
                        }
                        double turnDegrees =  (rand.nextDouble() * 3.0) - 1.5;
                        vector.turn(turnDegrees);
                    }
                    uavPoints.add(new Point((int)uav.location.x,(int)uav.location.y));
                }
                
		RSSIReading[] readings = computeRSSIReadingsInRSSISensor(uavs);
		if(null != readings) {
		    for(int loopj = 0; loopj < readings.length; loopj++) {
			RSSIReading reading = readings[loopj];
			if(null != reading) {
			    updateReadings(reading);
			}
		    }
		}
		
		// @TODO: ERROR - NEEDS FIXING, doing this for the
		// moment so when I check it in it doesn't throw
		// errors for other people.
		//		double [][] beliefs = filter.getBeliefs();
		double [][] beliefs = null;
		int highPixelCount = clusterUtils.countHighPixels(beliefs);

		boolean  doSeanStuff = true;
		boolean doKMeansStuff = false;

		if(doSeanStuff) {
// 		    if((highPixelCount > 100) && clusterCounter2 > 10) {
		    if(clusterCounter2 > 50) {
 			long startTime = System.currentTimeMillis();
			// @TODO: ERROR - NEEDS FIXING, doing this for the
			// moment so when I check it in it doesn't throw
			// errors for other people.
			DoubleGrid entropyGrid = null;
 			clusterUtils.checkClusters(-1,entropyGrid, beliefs);
 			long endTime = System.currentTimeMillis();
 			Machinetta.Debugger.debug("time for checkSeanClusters()="+(endTime - startTime),1,this);
			clusterCounter2 = 0;
		    }
		    clusterCounter2++;
		}

		if(doKMeansStuff){

// 		    int pixelDiff = lastHighPixelCount - highPixelCount;
// 		    if(pixelDiff < 0) pixelDiff = 0 - pixelDiff;
// 		    double pixelDiffPercent = ((double)pixelDiff)/((double)highPixelCount);

		    if(clusterCounter > 50) {
			// @TODO: ERROR - NEEDS FIXING, doing this for the
			// moment so when I check it in it doesn't throw
			// errors for other people.
			DoubleGrid entropyGrid = null;
			clusterUtils.checkKmeansClusters(entropyGrid, beliefs);
			clusterCounter = 0;
		    }
// 		    else if(lastHighPixelCountReps > 5 && clusterCounter > 50) {
// 			clusterUtils.checkKmeansClusters();
// 			clusterCounter = 0;
// 		    }
// 		    if(highPixelCount != lastHighPixelCount) {
// 			if(lastHighPixelCountReps != 1)
// 			    Machinetta.Debugger.debug("reps "+lastHighPixelCountReps+" high count="+highPixelCount,1,this);
// 			lastHighPixelCountReps = 1;
// 		    }
// 		    else {
// 			lastHighPixelCountReps++;
// 		    }
// 		    lastHighPixelCount = highPixelCount;
		    clusterCounter++;
		}


// 		if(null != bbfLeft)
// 		    Machinetta.Debugger.debug("bbfLeft percent pinned = "+bbfLeft.percentPinned()+" all pinned="+bbfLeft.allPinned(),1,this);
	    }
            
	    boolean extraDiffusion = false;
	    if(extraDiffusion) {
		if (numIterations % 5 == 0) {
		    // System.out.println("Doing diffusion @ " + numIterations);
		    double probArrive = 0.01, probLeave = 0.01;
		    double [][] beliefs = bbf.getBeliefs();
		    double [][] oldBeliefs = bbf.getOldBeliefs();
		    double [][] occupancyMap = bbf.getOccupancyMap();
		    for (int i = 0; i < beliefs.length; i++) {
			for (int j = 0; j < beliefs[0].length; j++) {
			    oldBeliefs[i][j] = beliefs[i][j];
			    beliefs[i][j] = (beliefs[i][j])*(1.0 - probLeave) 
				+ (1.0 - beliefs[i][j]) * probArrive;
			    occupancyMap[i][j] = BinaryBayesFilter.invBelief(beliefs[i][j]);
			}
		    }
		    System.out.println("Was: " + oldBeliefs[100][100]  + " now " + beliefs[100][100]);
		}
	    }

	    setDisplayData();
	}
    }
    
    public RSSIReading[] computeRSSIReadingsLocally(SmallUAV[] uavs){
        RSSIReading[] readings = new RSSIReading[uavs.length];
        for(int i=0; i<uavs.length; i++){
            EmitterModel e = emitterModels.get(i);
            readings[i] = getRFReading(e, (int)uavs[i].location.x, (int)uavs[i].location.y);
        }
        return readings;
    }

    public RSSIReading[] computeRSSIReadingsInRSSISensor(SmallUAV[] uavs){
 	ArrayList<RSSIReading> readings = new ArrayList<RSSIReading>();
	for(int i=0; i<uavs.length; i++){
	    ArrayList<SensorReading> r = sensors[i]._step(uavs[i],env);
	    if(r == null)
		continue;
	    for(int loopj = 0; loopj< r.size(); loopj++) {
		RSSISensorReading s = (RSSISensorReading)r.get(loopj);
		for(int loopk = 0; loopk < s.channels.length; loopk++) {
		    RSSIReading reading = new RSSIReading();
		    reading.x = s.x;
		    reading.y = s.y;
		    reading.strength = s.strengths[loopk];
		    reading.channel = s.channels[loopk];
		    readings.add(reading);
		}
	    }
	}

	RSSIReading[] readingsAry = readings.toArray(new RSSIReading[1]);
	return readingsAry;
    }
    
    public RSSIReading getRFReading(EmitterModel e, int x, int y){
        RSSIReading reading = new RSSIReading();
        reading.x = x;
        reading.y = y;
        reading.z = DEFAULT_ALTITUDE;
	reading.channel = Emitter.CHANNEL_DEFAULT;
        reading.strength = getSignal(e, x,y);
        return reading;
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
    public double getSignal(EmitterModel e, int x, int y){
        double signal = 0.0;
//         for(int i=0; i<emitterModels.size(); i++){
//             EmitterModel e = emitterModels.get(i);
        signal += e.getSignal2(distanceSquared(x,y,e.x,e.y));
//         }
        return signal;
    }
    
    public static void main(String argv[]) {
        double moveMultiplier = 20;
        int numUavs = 5;
        int mapSizeMeters = 50000;
        int gridSize = 1000;
	int repaintSleep = 1000;
	int numEmitters = 1;
	boolean randomEmitterPlacement = false;
	boolean subfilterson = false;

// 	double exp = -9.5;
// 	for(exp = -9.0; exp > -10.0; exp -= .01)
// 	    System.err.println("exp="+exp+", 0.5*Math.pow(10,exp)= "+(0.5*Math.pow(10,exp)));

//  	double fac = -9.5;
//  	for(fac = .1; fac > .0001; fac -= .0001)
//  	    System.err.println("fac="+fac+", Math.log((fac)/(1.0-fac))= "+Math.log((fac)/(1.0-fac)));


        if(argv.length >= 10) {
            System.err.println("Usage: test uav.cfg [moveMultiplier] [numUavs] [mapSizeMeters] [gridSize] [repaintSleep] [numEmitters] [randomEmitterPlacementFlag] [subfilterson]");
            System.err.println("        UAV .cfg file");
            System.err.println("        moveMultiplier defaults to "+moveMultiplier);
            System.err.println("        numUavs defaults to "+numUavs);
            System.err.println("        mapSizeMeters defaults to "+mapSizeMeters);
            System.err.println("        gridSize defaults to "+gridSize);
            System.err.println("        repaintSleep defaults to "+repaintSleep);
            System.err.println("        numEmitters defaults to "+numEmitters);
            System.err.println("        randomEmitterPlacement defaults to "+randomEmitterPlacement);
            System.err.println("        subfilterson defaults to "+subfilterson);
            System.exit(1);
        }

        if(argv.length >= 2) {
            moveMultiplier = Double.parseDouble(argv[1]);
            System.err.println("moveMultiplier set to "+moveMultiplier);
        }
        if(argv.length >= 3) {
            numUavs = Integer.parseInt(argv[2]);
            System.err.println("numUavs set to "+numUavs);
        }
        if(argv.length >= 4) {
            mapSizeMeters = Integer.parseInt(argv[3]);
            System.err.println("mapSizeMeters set to "+mapSizeMeters);
        }
        if(argv.length >= 5) {
            gridSize = Integer.parseInt(argv[4]);
            System.err.println("gridSize set to "+gridSize);
        }
        if(argv.length >= 6) {
            repaintSleep = Integer.parseInt(argv[5]);
            System.err.println("repaintSleep set to "+repaintSleep);
        }
        if(argv.length >= 7) {
            numEmitters = Integer.parseInt(argv[6]);
            System.err.println("numEmitters set to "+numEmitters);
        }
        if(argv.length >= 8) {
            int random = Integer.parseInt(argv[7]);
	    if(random > 0) {
		randomEmitterPlacement = true;
	    }
	    else {
		randomEmitterPlacement = false;
	    }
	    System.err.println("randomEmitterPlacement set to "+randomEmitterPlacement);
        }
        if(argv.length >= 9) {
            if(argv[8].equalsIgnoreCase("subfilterson"))
		subfilterson=true;
	    else
		subfilterson=false;
            System.err.println("subfilterson set to "+subfilterson);
        }
        
	Configuration config = new Configuration(argv[0]);
	UAVRI.readConfigs();

	BinaryBayesFilterTest test = new BinaryBayesFilterTest(moveMultiplier, mapSizeMeters, gridSize, numUavs,repaintSleep, numEmitters, randomEmitterPlacement, subfilterson);
	//        BinaryBayesFilterTest test = new BinaryBayesFilterTest(20, 1000, gridSize, 20);                
        test.run();
    }
}
	

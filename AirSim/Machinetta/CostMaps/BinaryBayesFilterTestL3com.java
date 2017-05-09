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
 * BinaryBayesFilterTestL3com.java
 *
 * Created on December 5, 2006, 10:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Machinetta.CostMaps;

import AirSim.Environment.Assets.Sensors.RSSISensor;
import AirSim.Machinetta.Beliefs.RSSIReading;
import AirSim.Environment.Assets.Sensors.EmitterModel;
import AirSim.Environment.Assets.Emitter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.awt.Point;
import java.util.Random;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 * @author Robin Glinton
 */
public class BinaryBayesFilterTestL3com {
    
    private final static DecimalFormat fmt = new DecimalFormat("0.0000000000000000");
    private final static DecimalFormat fmt2 = new DecimalFormat("0.0000");
    private final static DecimalFormat fmt3 = new DecimalFormat("0.###E0");

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
    
    private static int probDistPanelX = 1100;
    private static int probDistPanelY = 520;
    private static ArrayList<Point> uavPoints = new ArrayList<Point>();
    private static ArrayList<Point> emitterPoints = new ArrayList<Point>();

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
//	    if((endTime - startTime) > 30)
//		Machinetta.Debugger.debug("time to update filter="+(endTime - startTime)+" subfilters updated="+BinaryBayesFilter.subfilter_instance_update_count,1,this);
	}
    }

    public BinaryBayesFilterTestL3com(int mapSizeMeters, int gridSize, int repaintSleep) {

        this.mapSizeMeters = mapSizeMeters;
        this.gridSize = gridSize;
	this.repaintSleep = repaintSleep;

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

	// auto-size the sensor ranges based on map size - for big
	// map, big ranges, for small map, small ranges.
	sensorMaxRangeMeters = mapSizeMeters/10;
	sensorMinRangeMeters = mapSizeMeters/20;
        Machinetta.Debugger.debug("sensorMzxRangeMeters="+sensorMaxRangeMeters+", sensorMinRangeMeters="+sensorMinRangeMeters,1,this);
        
        int emitterx = (mapSizeMeters/2) + (int)gridScaleFactor/2;
        int emittery = (mapSizeMeters/2) + (int)gridScaleFactor/2;
        
	int mainGridSize = gridSize;
	double mainGridScaleFactor = gridScaleFactor;
	bbf = new BinaryBayesFilter(-1,mainGridSize, 0, 0, mainGridScaleFactor, emitterModelScaleFactor, sensorMaxRangeMeters, sensorMinRangeMeters, UNIFORM_PRIOR, 0, 0,false,false);
	bbfList.add(bbf);
	bbfDisplay = createDisplay("Main",BBF_DISPLAY_X, BBF_DISPLAY_Y);
    }
    
    public void run() {
        RepaintTicker repainter = new RepaintTicker();
        Thread repaintThread = new Thread(repainter);
        repaintThread.start();

	ArrayList<RSSIReading> readingList = buildReadingsList();

	for(int loopi = 0 ; loopi < readingList.size(); loopi++) {
	    updateReadings(readingList.get(loopi));
	    setDisplayData();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }

	}
    }
    
    public static void main(String argv[]) {
        
	BinaryBayesFilterTestL3com test = new BinaryBayesFilterTestL3com(1000, 1000,100);
        test.run();
    }

    //    http://www.terabeam.com/support/calculations/watts-dbm.php
    // dBm = (10Log10(milliWatts))
    // 1 mW = 0 dBm
    // Watts = 10^((dBm - 30)/10)
    // milliWatts = 10^(dBm/10)
    double dbmToWatts(double dbm) {
	//	return Math.pow(10,signal_in_dbm) / 1000;
	return  Math.pow(10,(dbm - 30)/10);
    }

    // A word about this by-hook-or-by-crook coordinate conversion;
    //
    // We picked two points somewhat randomly from the data file (it
    // would have been wiser perhaps to pick two points at the most extreme values)
    //
    // the first was;
    // Lat: N 33.287393
    // Lon: W -96.136084
    //
    // which we converted to UTM; 
    // Northing 3686808
    // Easting	 233291
    // Zone	47S
    //
    // And the second was
    // Lat: N 33.282109
    // Lon: W -96.13473
    //
    // which we converted to UTM;
    // Northing 3686225
    // Easting	 233148
    // Zone	47S
    //
    // Lat: (Y values)
    // LAT: 33.287393-33.282109 = .005284 decimal degrees
    // UTM: 3686808-3686225 = 583 meters
    //
    // Lon: (X Values)
    // LON: -96.136084 - -96.13473 = -.001354 decimal degrees
    // UTM: 233291-233148 = 143 meters
    // 
    // to convert a lat/lon coordinate, we first subtract from each
    // value the lowest value in the data file, hence 'zeroing' on
    // that value.  Then we use the degrees/meters ratios above
    // (i.e. .005284 degrees = 583 meters for lat) to convert the
    // partial lat (or lon) from decimal degrees, to cartesian
    // coordinates in meters.
    RSSIReading buildReading(double lon, double lat, double signal_in_dbm) {
	RSSIReading r = new RSSIReading();
	r.z = 0;
	r.strength = dbmToWatts(signal_in_dbm);
	r.channel = Emitter.CHANNEL_DEFAULT;

	double lat2 = lat - 33.280195;
	double lon2 = lon - -96.136451;

	r.y = (lat2 / .005284) * 583;
	r.x = (lon2 / .001354) * 143;
        Machinetta.Debugger.debug("raw "+fmt2.format(lon)+","+fmt2.format(lat)+","+signal_in_dbm+" becomes "+r.toString(),1,this);
	return r;
    }

    ArrayList<RSSIReading> buildReadingsList() {
	ArrayList<RSSIReading> list = new ArrayList<RSSIReading>();

	list.add(buildReading(-96.136084,33.287393,-81.525841));
	list.add(buildReading(-96.135940,33.287333,-81.525841));
	list.add(buildReading(-96.135922,33.287206,-81.525841));
	list.add(buildReading(-96.135917,33.287071,-81.525841));
	list.add(buildReading(-96.135913,33.286936,-81.525841));
	list.add(buildReading(-96.135909,33.286802,-80.612051));
	list.add(buildReading(-96.135904,33.286667,-80.612051));
	list.add(buildReading(-96.135900,33.286532,-80.353105));
	list.add(buildReading(-96.135895,33.286398,-80.353105));
	list.add(buildReading(-96.135891,33.286263,-80.353105));
	list.add(buildReading(-96.135886,33.286128,-80.353105));
	list.add(buildReading(-96.135882,33.285993,-79.760585));
	list.add(buildReading(-96.135878,33.285859,-79.595648));
	list.add(buildReading(-96.135873,33.285724,-79.595648));
	list.add(buildReading(-96.135869,33.285589,-79.595648));
	list.add(buildReading(-96.135864,33.285455,-79.595648));
	list.add(buildReading(-96.135841,33.285465,-79.595648));
	list.add(buildReading(-96.135803,33.285622,-79.595648));
	list.add(buildReading(-96.135766,33.285778,-79.595648));
	list.add(buildReading(-96.135728,33.285935,-79.309312));
	list.add(buildReading(-96.135691,33.286092,-79.367803));
	list.add(buildReading(-96.135654,33.286248,-79.451249));
	list.add(buildReading(-96.135616,33.286405,-79.451249));
	list.add(buildReading(-96.135579,33.286562,-79.723228));
	list.add(buildReading(-96.135541,33.286719,-79.723228));
	list.add(buildReading(-96.135504,33.286875,-80.011377));
	list.add(buildReading(-96.135466,33.287032,-80.011377));
	list.add(buildReading(-96.135429,33.287189,-80.011377));
	list.add(buildReading(-96.135408,33.287291,-80.557567));
	list.add(buildReading(-96.135260,33.287390,-80.557567));
	list.add(buildReading(-96.135103,33.287522,-80.557567));
	list.add(buildReading(-96.134947,33.287654,-80.557567));
	list.add(buildReading(-96.134790,33.287786,-80.557567));
	list.add(buildReading(-96.134634,33.287918,-80.557567));
	list.add(buildReading(-96.134538,33.287977,-80.726682));
	list.add(buildReading(-96.134513,33.287985,-80.726682));
	list.add(buildReading(-96.134360,33.288030,-80.726682));
	list.add(buildReading(-96.134208,33.288074,-80.726682));
	list.add(buildReading(-96.134056,33.288119,-80.726682));
	list.add(buildReading(-96.134001,33.288129,-80.576336));
	list.add(buildReading(-96.133984,33.288148,-80.576336));
	list.add(buildReading(-96.134018,33.288138,-80.576336));
	list.add(buildReading(-96.133804,33.288116,-80.576336));
	list.add(buildReading(-96.133576,33.288073,-80.239424));
	list.add(buildReading(-96.133348,33.288029,-80.123710));
	list.add(buildReading(-96.133232,33.288030,-79.852756));
	list.add(buildReading(-96.133225,33.288016,-79.852756));
	list.add(buildReading(-96.133015,33.287973,-79.688982));
	list.add(buildReading(-96.132804,33.287929,-79.688982));
	list.add(buildReading(-96.132614,33.287904,-79.688982));
	list.add(buildReading(-96.132603,33.287905,-79.365122));
	list.add(buildReading(-96.132603,33.287887,-79.367882));
	list.add(buildReading(-96.132469,33.287925,-79.367882));
	list.add(buildReading(-96.132335,33.287963,-79.367882));
	list.add(buildReading(-96.132201,33.288001,-79.367882));
	list.add(buildReading(-96.132067,33.288039,-79.478764));
	list.add(buildReading(-96.131933,33.288077,-79.554480));
	list.add(buildReading(-96.131799,33.288115,-79.611254));
	list.add(buildReading(-96.131607,33.288149,-79.611254));
	list.add(buildReading(-96.131409,33.288182,-79.795022));
	list.add(buildReading(-96.131211,33.288215,-79.795022));
	list.add(buildReading(-96.131013,33.288248,-79.978222));
	list.add(buildReading(-96.130975,33.288267,-79.978222));
	list.add(buildReading(-96.130998,33.288264,-79.978222));
	list.add(buildReading(-96.130916,33.288281,-79.978222));
	list.add(buildReading(-96.130698,33.288296,-80.170889));
	list.add(buildReading(-96.130480,33.288310,-80.274330));
	list.add(buildReading(-96.130262,33.288325,-80.274330));
	list.add(buildReading(-96.130270,33.288333,-80.274330));
	list.add(buildReading(-96.130255,33.288332,-80.274330));
	list.add(buildReading(-96.130280,33.288340,-80.274330));
	list.add(buildReading(-96.130080,33.288361,-80.451507));
	list.add(buildReading(-96.129880,33.288381,-80.571581));
	list.add(buildReading(-96.129680,33.288402,-80.835175));
	list.add(buildReading(-96.129644,33.288397,-80.835175));
	list.add(buildReading(-96.129632,33.288418,-80.897998));
	list.add(buildReading(-96.129622,33.288417,-80.897998));
	list.add(buildReading(-96.129534,33.288426,-80.897998));
	list.add(buildReading(-96.129343,33.288442,-81.091838));
	list.add(buildReading(-96.129151,33.288458,-81.091838));
	list.add(buildReading(-96.128959,33.288475,-81.091838));
	list.add(buildReading(-96.128768,33.288491,-81.091838));
	list.add(buildReading(-96.128594,33.288523,-81.597885));
	list.add(buildReading(-96.128597,33.288508,-81.597885));
	list.add(buildReading(-96.128600,33.288527,-81.597885));
	list.add(buildReading(-96.128599,33.288506,-81.759808));
	list.add(buildReading(-96.128596,33.288509,-81.759808));
	list.add(buildReading(-96.128481,33.288430,-81.759808));
	list.add(buildReading(-96.128365,33.288350,-81.759808));
	list.add(buildReading(-96.128250,33.288270,-81.759808));
	list.add(buildReading(-96.128135,33.288190,-81.645407));
	list.add(buildReading(-96.128019,33.288111,-81.641355));
	list.add(buildReading(-96.127904,33.288031,-81.641355));
	list.add(buildReading(-96.127789,33.287951,-81.641355));
	list.add(buildReading(-96.127673,33.287872,-81.641355));
	list.add(buildReading(-96.127667,33.287873,-81.625280));
	list.add(buildReading(-96.127662,33.287886,-81.625280));
	list.add(buildReading(-96.127650,33.287895,-81.625280));
	list.add(buildReading(-96.127679,33.287897,-81.625280));
	list.add(buildReading(-96.127679,33.287873,-81.625280));
	list.add(buildReading(-96.127669,33.287882,-81.625280));
	list.add(buildReading(-96.127660,33.287892,-81.625280));
	list.add(buildReading(-96.127691,33.287877,-81.625280));
	list.add(buildReading(-96.127649,33.287812,-81.625280));
	list.add(buildReading(-96.127635,33.287701,-81.625280));
	list.add(buildReading(-96.127620,33.287591,-81.453130));
	list.add(buildReading(-96.127606,33.287481,-81.346972));
	list.add(buildReading(-96.127603,33.287375,-81.346972));
	list.add(buildReading(-96.127707,33.287305,-80.941097));
	list.add(buildReading(-96.127810,33.287236,-80.941097));
	list.add(buildReading(-96.127913,33.287166,-80.600243));
	list.add(buildReading(-96.128017,33.287096,-80.344596));
	list.add(buildReading(-96.128120,33.287027,-80.344596));
	list.add(buildReading(-96.128224,33.286957,-80.029078));
	list.add(buildReading(-96.128253,33.286896,-79.821492));
	list.add(buildReading(-96.128318,33.286778,-79.821492));
	list.add(buildReading(-96.128383,33.286660,-79.821492));
	list.add(buildReading(-96.128448,33.286542,-79.821492));
	list.add(buildReading(-96.128513,33.286424,-79.821492));
	list.add(buildReading(-96.128578,33.286306,-79.821492));
	list.add(buildReading(-96.128626,33.286184,-79.821492));
	list.add(buildReading(-96.128635,33.286054,-77.990141));
	list.add(buildReading(-96.128644,33.285925,-77.990141));
	list.add(buildReading(-96.128652,33.285795,-77.990141));
	list.add(buildReading(-96.128661,33.285665,-77.429063));
	list.add(buildReading(-96.128733,33.285568,-77.212057));
	list.add(buildReading(-96.128868,33.285504,-77.212057));
	list.add(buildReading(-96.129004,33.285441,-77.212057));
	list.add(buildReading(-96.129139,33.285377,-76.055884));
	list.add(buildReading(-96.129275,33.285313,-76.055884));
	list.add(buildReading(-96.129330,33.285294,-76.055884));
	list.add(buildReading(-96.129318,33.285313,-76.055884));
	list.add(buildReading(-96.129329,33.285288,-75.365171));
	list.add(buildReading(-96.129370,33.285266,-75.365171));
	list.add(buildReading(-96.129470,33.285207,-75.365171));
	list.add(buildReading(-96.129569,33.285148,-74.604167));
	list.add(buildReading(-96.129669,33.285089,-74.604167));
	list.add(buildReading(-96.129768,33.285031,-73.757626));
	list.add(buildReading(-96.129868,33.284972,-73.757626));
	list.add(buildReading(-96.129967,33.284913,-73.757626));
	list.add(buildReading(-96.130067,33.284855,-72.744450));
	list.add(buildReading(-96.130166,33.284796,-72.158555));
	list.add(buildReading(-96.130266,33.284737,-71.852112));
	list.add(buildReading(-96.130310,33.284720,-71.852112));
	list.add(buildReading(-96.130297,33.284714,-71.502838));
	list.add(buildReading(-96.130366,33.284594,-71.502838));
	list.add(buildReading(-96.130434,33.284474,-70.828519));
	list.add(buildReading(-96.130503,33.284354,-70.828519));
	list.add(buildReading(-96.130572,33.284234,-70.828519));
	list.add(buildReading(-96.130641,33.284114,-70.828519));
	list.add(buildReading(-96.130640,33.284115,-69.526704));
	list.add(buildReading(-96.130737,33.284077,-69.193463));
	list.add(buildReading(-96.130843,33.283987,-69.028722));
	list.add(buildReading(-96.130948,33.283897,-68.756348));
	list.add(buildReading(-96.131053,33.283808,-68.476313));
	list.add(buildReading(-96.131158,33.283718,-68.476313));
	list.add(buildReading(-96.131264,33.283628,-68.248973));
	list.add(buildReading(-96.131355,33.283530,-68.282382));
	list.add(buildReading(-96.131424,33.283419,-68.640300));
	list.add(buildReading(-96.131493,33.283307,-69.120529));
	list.add(buildReading(-96.131562,33.283196,-69.686265));
	list.add(buildReading(-96.131553,33.283200,-69.686265));
	list.add(buildReading(-96.131581,33.283174,-69.686265));
	list.add(buildReading(-96.131568,33.283182,-69.686265));
	list.add(buildReading(-96.131653,33.283108,-70.054597));
	list.add(buildReading(-96.131737,33.283034,-70.193244));
	list.add(buildReading(-96.131822,33.282960,-70.825834));
	list.add(buildReading(-96.131906,33.282886,-70.825834));
	list.add(buildReading(-96.131991,33.282812,-71.519077));
	list.add(buildReading(-96.132076,33.282738,-71.672731));
	list.add(buildReading(-96.132160,33.282664,-71.672731));
	list.add(buildReading(-96.132245,33.282590,-72.590009));
	list.add(buildReading(-96.132329,33.282516,-72.590009));
	list.add(buildReading(-96.132414,33.282442,-72.590009));
	list.add(buildReading(-96.132498,33.282368,-73.835990));
	list.add(buildReading(-96.132578,33.282291,-74.054655));
	list.add(buildReading(-96.132657,33.282213,-74.054655));
	list.add(buildReading(-96.132736,33.282134,-74.054655));
	list.add(buildReading(-96.132815,33.282056,-74.054655));
	list.add(buildReading(-96.132894,33.281978,-75.489464));
	list.add(buildReading(-96.132928,33.281952,-75.489464));
	list.add(buildReading(-96.132944,33.281928,-75.704364));
	list.add(buildReading(-96.132921,33.281943,-75.704364));
	list.add(buildReading(-96.132952,33.281922,-75.704364));
	list.add(buildReading(-96.132949,33.281923,-75.704364));
	list.add(buildReading(-96.132951,33.281921,-75.795212));
	list.add(buildReading(-96.132943,33.281928,-75.795212));
	list.add(buildReading(-96.132935,33.281936,-75.795212));
	list.add(buildReading(-96.132925,33.281941,-75.795212));
	list.add(buildReading(-96.132961,33.281935,-75.795212));
	list.add(buildReading(-96.132945,33.281927,-75.704364));
	list.add(buildReading(-96.133081,33.281929,-75.704364));
	list.add(buildReading(-96.133226,33.281932,-75.704364));
	list.add(buildReading(-96.133371,33.281934,-75.704364));
	list.add(buildReading(-96.133305,33.281887,-76.294729));
	list.add(buildReading(-96.133186,33.281767,-76.294729));
	list.add(buildReading(-96.133067,33.281647,-76.651043));
	list.add(buildReading(-96.132948,33.281526,-76.789431));
	list.add(buildReading(-96.132829,33.281406,-76.789431));
	list.add(buildReading(-96.132710,33.281286,-76.789431));
	list.add(buildReading(-96.132591,33.281165,-77.439601));
	list.add(buildReading(-96.132476,33.281073,-77.439601));
	list.add(buildReading(-96.132388,33.281008,-77.439601));
	list.add(buildReading(-96.132237,33.280933,-77.439601));
	list.add(buildReading(-96.132087,33.280858,-78.032288));
	list.add(buildReading(-96.131936,33.280783,-78.125541));
	list.add(buildReading(-96.131785,33.280707,-78.280940));
	list.add(buildReading(-96.131638,33.280653,-78.280940));
	list.add(buildReading(-96.131663,33.280643,-78.280940));
	list.add(buildReading(-96.131644,33.280658,-78.561358));
	list.add(buildReading(-96.131562,33.280653,-78.561358));
	list.add(buildReading(-96.131365,33.280634,-78.593663));
	list.add(buildReading(-96.131167,33.280614,-78.731617));
	list.add(buildReading(-96.130970,33.280594,-78.785833));
	list.add(buildReading(-96.130772,33.280574,-78.941128));
	list.add(buildReading(-96.130758,33.280573,-78.941128));
	list.add(buildReading(-96.130741,33.280586,-78.941128));
	list.add(buildReading(-96.130772,33.280582,-78.941128));
	list.add(buildReading(-96.130660,33.280550,-78.941128));
	list.add(buildReading(-96.130476,33.280488,-78.941128));
	list.add(buildReading(-96.130293,33.280426,-78.941128));
	list.add(buildReading(-96.130109,33.280364,-79.538308));
	list.add(buildReading(-96.129925,33.280302,-79.911321));
	list.add(buildReading(-96.129775,33.280278,-79.996377));
	list.add(buildReading(-96.129795,33.280255,-80.117659));
	list.add(buildReading(-96.129783,33.280280,-80.117659));
	list.add(buildReading(-96.129700,33.280277,-80.117659));
	list.add(buildReading(-96.129483,33.280269,-80.117659));
	list.add(buildReading(-96.129267,33.280261,-80.434937));
	list.add(buildReading(-96.129050,33.280252,-80.434937));
	list.add(buildReading(-96.128937,33.280245,-80.434937));
	list.add(buildReading(-96.128927,33.280255,-80.434937));
	list.add(buildReading(-96.128918,33.280266,-80.434937));
	list.add(buildReading(-96.128924,33.280254,-80.756857));
	list.add(buildReading(-96.128942,33.280267,-80.756857));
	list.add(buildReading(-96.128948,33.280253,-80.756857));
	list.add(buildReading(-96.128764,33.280314,-80.756857));
	list.add(buildReading(-96.128571,33.280364,-80.756857));
	list.add(buildReading(-96.128378,33.280415,-80.756857));
	list.add(buildReading(-96.128237,33.280458,-80.756857));
	list.add(buildReading(-96.128243,33.280475,-81.061811));
	list.add(buildReading(-96.128112,33.280597,-81.061811));
	list.add(buildReading(-96.127982,33.280719,-81.061811));
	list.add(buildReading(-96.127851,33.280841,-81.061811));
	list.add(buildReading(-96.127720,33.280963,-81.061811));
	list.add(buildReading(-96.127589,33.281085,-81.061811));
	list.add(buildReading(-96.127459,33.281206,-80.909622));
	list.add(buildReading(-96.127404,33.281262,-80.909622));
	list.add(buildReading(-96.127332,33.281346,-80.908612));
	list.add(buildReading(-96.127260,33.281430,-80.895004));
	list.add(buildReading(-96.127188,33.281513,-80.905823));
	list.add(buildReading(-96.127117,33.281597,-80.908091));
	list.add(buildReading(-96.127045,33.281681,-80.908091));
	list.add(buildReading(-96.126973,33.281765,-80.928746));
	list.add(buildReading(-96.126901,33.281849,-80.937853));
	list.add(buildReading(-96.126829,33.281933,-80.950837));
	list.add(buildReading(-96.126799,33.281988,-80.950837));
	list.add(buildReading(-96.126824,33.282101,-80.879982));
	list.add(buildReading(-96.126837,33.282222,-80.879982));
	list.add(buildReading(-96.126851,33.282342,-80.879982));
	list.add(buildReading(-96.126864,33.282463,-80.879982));
	list.add(buildReading(-96.126878,33.282583,-80.398510));
	list.add(buildReading(-96.126891,33.282704,-80.298510));
	list.add(buildReading(-96.126947,33.282811,-80.197587));
	list.add(buildReading(-96.127032,33.282909,-80.037571));
	list.add(buildReading(-96.127117,33.283008,-79.804220));
	list.add(buildReading(-96.127202,33.283106,-79.609940));
	list.add(buildReading(-96.127241,33.283163,-79.609940));
	list.add(buildReading(-96.127282,33.283231,-79.435715));
	list.add(buildReading(-96.127306,33.283319,-79.296621));
	list.add(buildReading(-96.127331,33.283406,-79.296621));
	list.add(buildReading(-96.127355,33.283494,-79.296621));
	list.add(buildReading(-96.127379,33.283581,-79.086586));
	list.add(buildReading(-96.127404,33.283669,-78.992699));
	list.add(buildReading(-96.127450,33.283745,-78.870607));
	list.add(buildReading(-96.127531,33.283804,-78.870607));
	list.add(buildReading(-96.127612,33.283863,-78.870607));
	list.add(buildReading(-96.127693,33.283923,-78.870607));
	list.add(buildReading(-96.127774,33.283982,-78.294626));
	list.add(buildReading(-96.127854,33.284041,-78.294626));
	list.add(buildReading(-96.127935,33.284100,-78.294626));
	list.add(buildReading(-96.128016,33.284159,-77.789883));
	list.add(buildReading(-96.128097,33.284219,-77.461530));
	list.add(buildReading(-96.128177,33.284278,-77.461530));
	list.add(buildReading(-96.128258,33.284337,-77.280858));
	list.add(buildReading(-96.128292,33.284374,-77.280858));
	list.add(buildReading(-96.128279,33.284383,-77.280858));
	list.add(buildReading(-96.128319,33.284368,-77.280858));
	list.add(buildReading(-96.128294,33.284373,-77.056040));
	list.add(buildReading(-96.128290,33.284392,-77.056040));
	list.add(buildReading(-96.128261,33.284347,-77.056040));
	list.add(buildReading(-96.128172,33.284224,-77.056040));
	list.add(buildReading(-96.128083,33.284101,-77.056040));
	list.add(buildReading(-96.127994,33.283978,-77.544905));
	list.add(buildReading(-96.127905,33.283855,-77.544905));
	list.add(buildReading(-96.127816,33.283733,-78.031265));
	list.add(buildReading(-96.127728,33.283610,-78.031265));
	list.add(buildReading(-96.127639,33.283487,-78.031265));
	list.add(buildReading(-96.127602,33.283437,-78.582392));
	list.add(buildReading(-96.127532,33.283358,-78.753435));
	list.add(buildReading(-96.127462,33.283280,-78.972594));
	list.add(buildReading(-96.127392,33.283202,-78.972594));
	list.add(buildReading(-96.127323,33.283124,-78.972594));
	list.add(buildReading(-96.127253,33.283045,-78.972594));
	list.add(buildReading(-96.127183,33.282967,-79.642609));
	list.add(buildReading(-96.127113,33.282889,-79.642609));
	list.add(buildReading(-96.127044,33.282810,-79.642609));
	list.add(buildReading(-96.126974,33.282732,-80.141713));
	list.add(buildReading(-96.126904,33.282654,-80.298510));
	list.add(buildReading(-96.126871,33.282619,-80.298510));
	list.add(buildReading(-96.126866,33.282621,-80.381334));
	list.add(buildReading(-96.126867,33.282569,-80.398510));
	list.add(buildReading(-96.126870,33.282455,-80.398510));
	list.add(buildReading(-96.126873,33.282341,-80.496467));
	list.add(buildReading(-96.126876,33.282226,-80.641657));
	list.add(buildReading(-96.126879,33.282112,-80.641657));
	list.add(buildReading(-96.126882,33.281998,-80.641657));
	list.add(buildReading(-96.126881,33.281954,-80.641657));
	list.add(buildReading(-96.126869,33.281961,-80.894745));
	list.add(buildReading(-96.126891,33.281971,-80.884546));
	list.add(buildReading(-96.126890,33.281946,-80.884546));
	list.add(buildReading(-96.126833,33.281869,-80.970831));
	list.add(buildReading(-96.126752,33.281782,-80.970831));
	list.add(buildReading(-96.126672,33.281696,-81.341682));
	list.add(buildReading(-96.126591,33.281610,-81.341682));
	list.add(buildReading(-96.126511,33.281523,-81.341682));
	list.add(buildReading(-96.126443,33.281492,-81.341682));
	list.add(buildReading(-96.126447,33.281463,-81.341682));
	list.add(buildReading(-96.126468,33.281361,-81.856720));
	list.add(buildReading(-96.126489,33.281259,-81.856720));
	list.add(buildReading(-96.126509,33.281157,-81.856720));
	list.add(buildReading(-96.126530,33.281055,-81.856720));
	list.add(buildReading(-96.126525,33.281024,-82.143941));
	list.add(buildReading(-96.126534,33.281009,-82.143941));
	list.add(buildReading(-96.126546,33.281026,-82.143941));
	list.add(buildReading(-96.126548,33.280999,-82.143941));
	list.add(buildReading(-96.126533,33.281016,-82.143941));
	list.add(buildReading(-96.126541,33.281029,-82.143941));
	list.add(buildReading(-96.126522,33.281010,-82.143941));
	list.add(buildReading(-96.126523,33.280914,-82.143941));
	list.add(buildReading(-96.126524,33.280819,-82.323456));
	list.add(buildReading(-96.126525,33.280723,-82.323456));
	list.add(buildReading(-96.126526,33.280627,-82.434872));
	list.add(buildReading(-96.126527,33.280531,-82.434872));
	list.add(buildReading(-96.126528,33.280435,-82.434872));
	list.add(buildReading(-96.126618,33.280405,-82.662083));
	list.add(buildReading(-96.126732,33.280391,-82.555381));
	list.add(buildReading(-96.126845,33.280377,-82.555381));
	list.add(buildReading(-96.126958,33.280362,-82.375542));
	list.add(buildReading(-96.127072,33.280348,-82.340862));
	list.add(buildReading(-96.127185,33.280334,-82.243577));
	list.add(buildReading(-96.127287,33.280351,-82.243577));
	list.add(buildReading(-96.127373,33.280415,-81.922922));
	list.add(buildReading(-96.127458,33.280478,-81.807025));
	list.add(buildReading(-96.127544,33.280542,-81.710601));
	list.add(buildReading(-96.127629,33.280606,-81.608552));
	list.add(buildReading(-96.127715,33.280670,-81.363884));
	list.add(buildReading(-96.127801,33.280733,-81.363884));
	list.add(buildReading(-96.127886,33.280797,-81.363884));
	list.add(buildReading(-96.127972,33.280861,-81.363884));
	list.add(buildReading(-96.128057,33.280925,-80.723170));
	list.add(buildReading(-96.128143,33.280988,-80.723170));
	list.add(buildReading(-96.128228,33.281052,-80.723170));
	list.add(buildReading(-96.128314,33.281116,-80.093127));
	list.add(buildReading(-96.128303,33.281220,-80.093127));
	list.add(buildReading(-96.128173,33.281362,-79.887920));
	list.add(buildReading(-96.128043,33.281505,-79.887920));
	list.add(buildReading(-96.127912,33.281647,-79.809170));
	list.add(buildReading(-96.127812,33.281775,-79.806145));
	list.add(buildReading(-96.127809,33.281753,-79.801332));
	list.add(buildReading(-96.127790,33.281810,-79.801332));
	list.add(buildReading(-96.127858,33.281964,-79.711645));
	list.add(buildReading(-96.127926,33.282117,-79.223081));
	list.add(buildReading(-96.127995,33.282271,-78.950483));
	list.add(buildReading(-96.128063,33.282425,-78.950467));
	list.add(buildReading(-96.128087,33.282549,-78.950467));
	list.add(buildReading(-96.128108,33.282555,-78.430335));
	list.add(buildReading(-96.128152,33.282652,-78.293536));
	list.add(buildReading(-96.128186,33.282788,-78.056032));
	list.add(buildReading(-96.128221,33.282924,-78.056032));
	list.add(buildReading(-96.128256,33.283060,-77.649268));
	list.add(buildReading(-96.128291,33.283196,-77.485862));
	list.add(buildReading(-96.128326,33.283332,-77.485862));
	list.add(buildReading(-96.128346,33.283366,-77.485862));
	list.add(buildReading(-96.128334,33.283356,-77.485862));
	list.add(buildReading(-96.128330,33.283361,-77.485862));
	list.add(buildReading(-96.128313,33.283369,-77.227197));
	list.add(buildReading(-96.128335,33.283381,-77.227197));
	list.add(buildReading(-96.128307,33.283379,-77.247021));
	list.add(buildReading(-96.128302,33.283379,-77.227197));
	list.add(buildReading(-96.128327,33.283360,-77.227197));
	list.add(buildReading(-96.128327,33.283360,-77.227197));
	list.add(buildReading(-96.128322,33.283383,-77.227197));
	list.add(buildReading(-96.128371,33.283404,-77.247021));
	list.add(buildReading(-96.128418,33.283482,-77.247021));
	list.add(buildReading(-96.128465,33.283560,-76.794148));
	list.add(buildReading(-96.128512,33.283638,-76.794132));
	list.add(buildReading(-96.128559,33.283716,-76.794132));
	list.add(buildReading(-96.128606,33.283794,-76.375912));
	list.add(buildReading(-96.128653,33.283872,-76.257378));
	list.add(buildReading(-96.128700,33.283950,-76.257378));
	list.add(buildReading(-96.128788,33.283971,-76.257378));
	list.add(buildReading(-96.128892,33.283967,-76.257378));
	list.add(buildReading(-96.128997,33.283963,-75.346728));
	list.add(buildReading(-96.129101,33.283959,-75.346728));
	list.add(buildReading(-96.129205,33.283954,-75.346728));
	list.add(buildReading(-96.129310,33.283950,-75.346728));
	list.add(buildReading(-96.129414,33.283946,-75.346728));
	list.add(buildReading(-96.129519,33.283942,-73.836600));
	list.add(buildReading(-96.129614,33.283911,-73.552864));
	list.add(buildReading(-96.129705,33.283869,-73.255532));
	list.add(buildReading(-96.129796,33.283826,-73.255532));
	list.add(buildReading(-96.129888,33.283783,-72.639634));
	list.add(buildReading(-96.129979,33.283741,-72.639634));
	list.add(buildReading(-96.130070,33.283698,-72.147176));
	list.add(buildReading(-96.130162,33.283656,-72.147176));
	list.add(buildReading(-96.130253,33.283613,-72.147176));
	list.add(buildReading(-96.130344,33.283570,-72.147176));
	list.add(buildReading(-96.130435,33.283528,-72.147176));
	list.add(buildReading(-96.130527,33.283485,-72.147176));
	list.add(buildReading(-96.130618,33.283442,-72.147176));
	list.add(buildReading(-96.130679,33.283431,-72.147176));
	list.add(buildReading(-96.130773,33.283476,-72.147176));
	list.add(buildReading(-96.130887,33.283555,-72.147176));
	list.add(buildReading(-96.131001,33.283634,-69.405424));
	list.add(buildReading(-96.131115,33.283713,-68.798816));
	list.add(buildReading(-96.131230,33.283792,-67.975650));
	list.add(buildReading(-96.131344,33.283871,-67.525658));
	list.add(buildReading(-96.131458,33.283950,-67.119273));
	list.add(buildReading(-96.131572,33.284029,-66.696987));
	list.add(buildReading(-96.131681,33.284114,-66.525583));
	list.add(buildReading(-96.131692,33.284139,-66.525583));
	list.add(buildReading(-96.131836,33.284199,-66.509047));
	list.add(buildReading(-96.131980,33.284258,-66.654784));
	list.add(buildReading(-96.132124,33.284317,-67.081217));
	list.add(buildReading(-96.132268,33.284377,-67.648927));
	list.add(buildReading(-96.132351,33.284439,-67.648927));
	list.add(buildReading(-96.132372,33.284428,-67.648927));
	list.add(buildReading(-96.132475,33.284387,-67.648927));
	list.add(buildReading(-96.132582,33.284339,-68.700252));
	list.add(buildReading(-96.132688,33.284291,-68.700252));
	list.add(buildReading(-96.132794,33.284242,-68.700252));
	list.add(buildReading(-96.132900,33.284194,-69.599199));
	list.add(buildReading(-96.133006,33.284146,-70.259236));
	list.add(buildReading(-96.133112,33.284098,-70.259236));
	list.add(buildReading(-96.133219,33.284049,-70.994259));
	list.add(buildReading(-96.133325,33.284001,-70.994259));
	list.add(buildReading(-96.133431,33.283953,-71.822546));
	list.add(buildReading(-96.133427,33.283955,-71.822546));
	list.add(buildReading(-96.133410,33.283968,-71.822546));
	list.add(buildReading(-96.133418,33.283961,-71.822546));
	list.add(buildReading(-96.133423,33.283970,-72.090415));
	list.add(buildReading(-96.133442,33.283954,-72.090415));
	list.add(buildReading(-96.133402,33.283971,-71.822546));
	list.add(buildReading(-96.133496,33.283920,-72.090415));
	list.add(buildReading(-96.133593,33.283873,-72.442919));
	list.add(buildReading(-96.133693,33.283830,-72.848774));
	list.add(buildReading(-96.133793,33.283788,-73.244140));
	list.add(buildReading(-96.133894,33.283746,-73.569192));
	list.add(buildReading(-96.133994,33.283704,-74.045038));
	list.add(buildReading(-96.134094,33.283661,-74.045038));
	list.add(buildReading(-96.134195,33.283619,-74.634917));
	list.add(buildReading(-96.134295,33.283577,-74.634917));
	list.add(buildReading(-96.134395,33.283535,-75.207595));
	list.add(buildReading(-96.134495,33.283493,-75.572281));
	list.add(buildReading(-96.134578,33.283432,-75.774308));
	list.add(buildReading(-96.134649,33.283359,-75.774308));
	list.add(buildReading(-96.134721,33.283287,-75.774308));
	list.add(buildReading(-96.134792,33.283214,-75.774308));
	list.add(buildReading(-96.134863,33.283142,-75.774308));
	list.add(buildReading(-96.134934,33.283069,-75.774308));
	list.add(buildReading(-96.135005,33.282996,-75.774308));
	list.add(buildReading(-96.135077,33.282924,-75.774308));
	list.add(buildReading(-96.135148,33.282851,-77.603529));
	list.add(buildReading(-96.135219,33.282779,-77.603529));
	list.add(buildReading(-96.135290,33.282706,-77.603529));
	list.add(buildReading(-96.135348,33.282628,-77.603529));
	list.add(buildReading(-96.135373,33.282536,-77.603529));
	list.add(buildReading(-96.135399,33.282445,-77.603529));
	list.add(buildReading(-96.135424,33.282354,-78.798289));
	list.add(buildReading(-96.135450,33.282262,-78.798289));
	list.add(buildReading(-96.135475,33.282171,-78.989497));
	list.add(buildReading(-96.135501,33.282079,-79.120120));
	list.add(buildReading(-96.135527,33.281988,-79.333453));
	list.add(buildReading(-96.135552,33.281897,-79.333453));
	list.add(buildReading(-96.135578,33.281805,-79.643763));
	list.add(buildReading(-96.135574,33.281835,-79.643763));
	list.add(buildReading(-96.135555,33.281826,-79.643763));
	list.add(buildReading(-96.135596,33.281731,-79.643776));
	list.add(buildReading(-96.135637,33.281636,-79.643776));
	list.add(buildReading(-96.135678,33.281540,-79.984138));
	list.add(buildReading(-96.135719,33.281445,-80.308610));
	list.add(buildReading(-96.135760,33.281350,-80.308610));
	list.add(buildReading(-96.135805,33.281291,-80.308610));
	list.add(buildReading(-96.135772,33.281304,-80.671703));
	list.add(buildReading(-96.135790,33.281277,-80.671703));
	list.add(buildReading(-96.135790,33.281277,-80.671703));
	list.add(buildReading(-96.135801,33.281276,-80.671703));
	list.add(buildReading(-96.135873,33.281234,-80.671703));
	list.add(buildReading(-96.135944,33.281193,-80.671703));
	list.add(buildReading(-96.136015,33.281152,-80.671703));
	list.add(buildReading(-96.136087,33.281111,-81.113745));
	list.add(buildReading(-96.136158,33.281069,-81.113745));
	list.add(buildReading(-96.136224,33.281025,-81.113745));
	list.add(buildReading(-96.136243,33.280954,-81.113745));
	list.add(buildReading(-96.136262,33.280884,-81.113745));
	list.add(buildReading(-96.136281,33.280813,-81.660570));
	list.add(buildReading(-96.136299,33.280742,-81.660570));
	list.add(buildReading(-96.136318,33.280671,-81.660570));
	list.add(buildReading(-96.136337,33.280601,-81.660570));
	list.add(buildReading(-96.136356,33.280530,-81.660570));
	list.add(buildReading(-96.136375,33.280459,-82.165485));
	list.add(buildReading(-96.136393,33.280388,-82.165485));
	list.add(buildReading(-96.136412,33.280318,-82.165485));
	list.add(buildReading(-96.136431,33.280247,-82.418250));
	list.add(buildReading(-96.136451,33.280246,-82.418250));
	list.add(buildReading(-96.136388,33.280223,-82.418250));
	list.add(buildReading(-96.136233,33.280195,-82.366385));
	list.add(buildReading(-96.136086,33.280226,-82.366385));
	list.add(buildReading(-96.135943,33.280282,-82.110340));
	list.add(buildReading(-96.135799,33.280338,-82.110340));
	list.add(buildReading(-96.135656,33.280395,-81.746835));
	list.add(buildReading(-96.135512,33.280451,-81.746835));
	list.add(buildReading(-96.135368,33.280507,-81.192064));
	list.add(buildReading(-96.135225,33.280563,-81.192064));
	list.add(buildReading(-96.135081,33.280619,-80.896942));
	list.add(buildReading(-96.134938,33.280675,-80.520596));
	list.add(buildReading(-96.134926,33.280675,-80.520596));
	list.add(buildReading(-96.134785,33.280775,-80.520596));
	list.add(buildReading(-96.134643,33.280904,-79.992997));
	list.add(buildReading(-96.134500,33.281034,-79.992997));
	list.add(buildReading(-96.134358,33.281164,-79.223894));
	list.add(buildReading(-96.134216,33.281294,-78.844645));
	list.add(buildReading(-96.134131,33.281388,-78.410976));
	list.add(buildReading(-96.134066,33.281531,-78.410976));
	list.add(buildReading(-96.134027,33.281728,-77.716269));
	list.add(buildReading(-96.133988,33.281924,-77.476845));
	list.add(buildReading(-96.133950,33.282121,-76.926439));
	list.add(buildReading(-96.133933,33.282212,-76.532659));
	list.add(buildReading(-96.133940,33.282218,-76.532659));
	list.add(buildReading(-96.133892,33.282227,-76.532659));
	list.add(buildReading(-96.133692,33.282287,-76.144411));
	list.add(buildReading(-96.133492,33.282347,-75.882437));
	list.add(buildReading(-96.133291,33.282407,-74.933722));
	list.add(buildReading(-96.133091,33.282468,-74.933722));
	list.add(buildReading(-96.133103,33.282461,-74.334626));
	list.add(buildReading(-96.133081,33.282481,-74.433407));
	list.add(buildReading(-96.133024,33.282562,-74.206803));
	list.add(buildReading(-96.132886,33.282706,-74.206803));
	list.add(buildReading(-96.132748,33.282851,-74.206803));
	list.add(buildReading(-96.132610,33.282996,-71.880035));
	list.add(buildReading(-96.132472,33.283140,-71.585656));
	list.add(buildReading(-96.132334,33.283285,-70.859455));
	list.add(buildReading(-96.132258,33.283357,-70.859455));
	list.add(buildReading(-96.132283,33.283340,-69.583381));
	list.add(buildReading(-96.132256,33.283361,-69.583381));
	list.add(buildReading(-96.132280,33.283341,-69.583381));
	list.add(buildReading(-96.132272,33.283364,-69.583381));
	list.add(buildReading(-96.132249,33.283356,-69.583381));
	list.add(buildReading(-96.132255,33.283360,-69.540606));
	list.add(buildReading(-96.132269,33.283349,-69.583381));
	list.add(buildReading(-96.132300,33.283354,-69.583381));
	list.add(buildReading(-96.132294,33.283337,-69.583381));
	list.add(buildReading(-96.132308,33.283312,-69.583381));
	list.add(buildReading(-96.132368,33.283246,-70.156100));
	list.add(buildReading(-96.132428,33.283179,-70.610974));
	list.add(buildReading(-96.132487,33.283113,-70.610974));
	list.add(buildReading(-96.132547,33.283047,-70.610974));
	list.add(buildReading(-96.132607,33.282981,-70.610974));
	list.add(buildReading(-96.132666,33.282915,-71.880022));
	list.add(buildReading(-96.132726,33.282849,-72.281818));
	list.add(buildReading(-96.132786,33.282783,-72.281818));
	list.add(buildReading(-96.132845,33.282716,-73.115670));
	list.add(buildReading(-96.132905,33.282650,-73.115682));
	list.add(buildReading(-96.132967,33.282585,-73.556680));
	list.add(buildReading(-96.133031,33.282522,-73.556680));
	list.add(buildReading(-96.133095,33.282459,-74.433395));
	list.add(buildReading(-96.133159,33.282396,-74.433407));
	list.add(buildReading(-96.133223,33.282332,-74.433407));
	list.add(buildReading(-96.133287,33.282269,-75.206241));
	list.add(buildReading(-96.133351,33.282206,-75.206241));
	list.add(buildReading(-96.133414,33.282142,-75.206241));
	list.add(buildReading(-96.133478,33.282079,-75.206241));
	list.add(buildReading(-96.133542,33.282016,-76.238937));
	list.add(buildReading(-96.133606,33.281953,-76.238937));
	list.add(buildReading(-96.133670,33.281889,-76.675976));
	list.add(buildReading(-96.133734,33.281826,-77.031019));
	list.add(buildReading(-96.133798,33.281763,-77.031019));
	list.add(buildReading(-96.133862,33.281699,-77.381770));
	list.add(buildReading(-96.133926,33.281636,-77.609275));
	list.add(buildReading(-96.133981,33.281603,-77.609275));
	list.add(buildReading(-96.134106,33.281540,-77.609275));
	list.add(buildReading(-96.134231,33.281478,-77.609275));
	list.add(buildReading(-96.134356,33.281416,-78.445747));
	list.add(buildReading(-96.134480,33.281354,-78.845500));
	list.add(buildReading(-96.134605,33.281292,-79.066892));
	list.add(buildReading(-96.134730,33.281230,-79.175896));
	list.add(buildReading(-96.134779,33.281208,-79.175896));
	list.add(buildReading(-96.134830,33.281194,-79.506109));
	list.add(buildReading(-96.134952,33.281156,-79.622382));
	list.add(buildReading(-96.135073,33.281117,-79.622382));
	list.add(buildReading(-96.135195,33.281078,-80.177140));
	list.add(buildReading(-96.135317,33.281039,-80.177140));
	list.add(buildReading(-96.135438,33.281001,-80.177140));
	list.add(buildReading(-96.135560,33.280962,-80.177140));
	list.add(buildReading(-96.135681,33.280923,-80.177140));
	list.add(buildReading(-96.135777,33.280889,-80.177140));
	list.add(buildReading(-96.135854,33.280789,-80.177140));
	list.add(buildReading(-96.135932,33.280690,-80.177140));
	list.add(buildReading(-96.136009,33.280591,-80.177140));
	list.add(buildReading(-96.136087,33.280491,-81.706397));
	list.add(buildReading(-96.136164,33.280392,-81.706397));
	list.add(buildReading(-96.136167,33.280400,-81.706397));
	list.add(buildReading(-96.136118,33.280579,-81.867705));
	list.add(buildReading(-96.136070,33.280758,-81.761880));
	list.add(buildReading(-96.136021,33.280937,-81.391127));
	list.add(buildReading(-96.135979,33.280980,-81.391127));
	list.add(buildReading(-96.135981,33.280961,-81.230419));
	list.add(buildReading(-96.135851,33.281057,-81.230419));
	list.add(buildReading(-96.135738,33.281165,-81.230419));
	list.add(buildReading(-96.135635,33.281282,-81.230419));
	list.add(buildReading(-96.135533,33.281399,-81.230419));
	list.add(buildReading(-96.135430,33.281516,-81.230419));
	list.add(buildReading(-96.135327,33.281633,-81.230419));
	list.add(buildReading(-96.135267,33.281691,-79.416954));
	list.add(buildReading(-96.135264,33.281726,-79.416954));
	list.add(buildReading(-96.135230,33.281922,-79.155153));
	list.add(buildReading(-96.135197,33.282118,-78.894757));
	list.add(buildReading(-96.135163,33.282314,-78.894757));
	list.add(buildReading(-96.135129,33.282510,-78.894757));
	list.add(buildReading(-96.135106,33.282548,-78.051717));
	list.add(buildReading(-96.135109,33.282541,-78.051717));
	list.add(buildReading(-96.135139,33.282535,-78.051717));
	list.add(buildReading(-96.135111,33.282577,-78.051717));
	list.add(buildReading(-96.135043,33.282728,-77.763678));
	list.add(buildReading(-96.134974,33.282879,-77.763678));
	list.add(buildReading(-96.134906,33.283030,-77.354648));
	list.add(buildReading(-96.134837,33.283182,-76.828841));
	list.add(buildReading(-96.134769,33.283333,-76.828841));
	list.add(buildReading(-96.134730,33.283422,-76.273488));
	list.add(buildReading(-96.134717,33.283423,-76.273488));
	list.add(buildReading(-96.134734,33.283436,-76.273488));
	list.add(buildReading(-96.134649,33.283583,-76.273488));
	list.add(buildReading(-96.134563,33.283731,-76.273488));
	list.add(buildReading(-96.134478,33.283879,-76.273488));
	list.add(buildReading(-96.134392,33.284027,-75.134325));
	list.add(buildReading(-96.134306,33.284175,-75.134325));
	list.add(buildReading(-96.134281,33.284214,-74.798831));
	list.add(buildReading(-96.134256,33.284240,-74.798831));
	list.add(buildReading(-96.134235,33.284247,-74.798831));
	list.add(buildReading(-96.134147,33.284387,-74.553346));
	list.add(buildReading(-96.134060,33.284527,-74.310581));
	list.add(buildReading(-96.133972,33.284667,-74.112099));
	list.add(buildReading(-96.133885,33.284807,-74.067208));
	list.add(buildReading(-96.133798,33.284947,-74.067208));
	list.add(buildReading(-96.133707,33.285086,-74.067208));
	list.add(buildReading(-96.133717,33.285075,-74.030807));
	list.add(buildReading(-96.133722,33.285089,-73.961139));
	list.add(buildReading(-96.133715,33.285095,-73.961139));
	list.add(buildReading(-96.133722,33.285069,-74.030807));
	list.add(buildReading(-96.133715,33.285093,-74.030807));
	list.add(buildReading(-96.133722,33.285068,-73.961139));
	list.add(buildReading(-96.133732,33.285040,-73.961139));
	list.add(buildReading(-96.133743,33.284953,-73.961139));
	list.add(buildReading(-96.133755,33.284865,-73.961139));
	list.add(buildReading(-96.133766,33.284778,-73.961139));
	list.add(buildReading(-96.133778,33.284691,-73.552428));
	list.add(buildReading(-96.133789,33.284604,-73.552403));
	list.add(buildReading(-96.133801,33.284516,-73.552403));
	list.add(buildReading(-96.133812,33.284429,-73.552403));
	list.add(buildReading(-96.133831,33.284343,-73.441787));
	list.add(buildReading(-96.133865,33.284260,-73.441787));
	list.add(buildReading(-96.133899,33.284177,-73.577126));
	list.add(buildReading(-96.133933,33.284094,-73.577126));
	list.add(buildReading(-96.133967,33.284011,-73.675331));
	list.add(buildReading(-96.134001,33.283928,-73.675331));
	list.add(buildReading(-96.134035,33.283845,-74.036190));
	list.add(buildReading(-96.134069,33.283762,-74.036190));
	list.add(buildReading(-96.134103,33.283679,-74.036190));
	list.add(buildReading(-96.134137,33.283596,-74.522220));
	list.add(buildReading(-96.134157,33.283561,-74.522220));
	list.add(buildReading(-96.134148,33.283566,-74.522220));
	list.add(buildReading(-96.134243,33.283476,-74.986960));
	list.add(buildReading(-96.134338,33.283386,-75.261805));
	list.add(buildReading(-96.134433,33.283296,-75.700713));
	list.add(buildReading(-96.134528,33.283206,-75.700713));
	list.add(buildReading(-96.134575,33.283167,-75.700713));
	list.add(buildReading(-96.134564,33.283169,-76.205270));
	list.add(buildReading(-96.134587,33.283128,-76.205270));
	list.add(buildReading(-96.134650,33.283042,-76.310938));
	list.add(buildReading(-96.134713,33.282956,-76.310938));
	list.add(buildReading(-96.134775,33.282871,-76.873329));
	list.add(buildReading(-96.134838,33.282785,-77.124952));
	list.add(buildReading(-96.134901,33.282700,-77.124952));
	list.add(buildReading(-96.134964,33.282614,-77.470373));
	list.add(buildReading(-96.135026,33.282529,-77.827689));
	list.add(buildReading(-96.135089,33.282443,-78.051704));
	list.add(buildReading(-96.135152,33.282357,-78.051704));
	list.add(buildReading(-96.135145,33.282371,-78.343930));
	list.add(buildReading(-96.135223,33.282337,-78.343930));
	list.add(buildReading(-96.135320,33.282256,-78.343930));
	list.add(buildReading(-96.135416,33.282176,-78.343930));
	list.add(buildReading(-96.135513,33.282095,-78.343930));
	list.add(buildReading(-96.135609,33.282014,-79.333453));
	list.add(buildReading(-96.135706,33.281933,-79.605257));
	list.add(buildReading(-96.135802,33.281852,-79.933728));
	list.add(buildReading(-96.135899,33.281772,-80.108597));
	list.add(buildReading(-96.135927,33.281716,-80.215990));
	list.add(buildReading(-96.135996,33.281616,-80.489647));
	list.add(buildReading(-96.136065,33.281516,-80.727179));
	list.add(buildReading(-96.136134,33.281417,-80.872629));
	list.add(buildReading(-96.136203,33.281317,-81.032654));
	list.add(buildReading(-96.136272,33.281217,-81.032654));
	list.add(buildReading(-96.136341,33.281117,-81.032654));
	list.add(buildReading(-96.136194,33.281223,-81.256399));
	list.add(buildReading(-96.136051,33.281338,-80.987293));
	list.add(buildReading(-96.135909,33.281453,-80.768475));
	list.add(buildReading(-96.135767,33.281568,-80.768475));
	list.add(buildReading(-96.135625,33.281682,-80.768475));
	list.add(buildReading(-96.135483,33.281797,-79.605611));
	list.add(buildReading(-96.135460,33.281816,-79.605611));
	list.add(buildReading(-96.135467,33.281831,-79.605611));
	list.add(buildReading(-96.135282,33.281900,-79.328630));
	list.add(buildReading(-96.135098,33.281970,-79.039626));
	list.add(buildReading(-96.134914,33.282040,-78.539621));
	list.add(buildReading(-96.134730,33.282109,-78.539621));

	return list;
    }
}

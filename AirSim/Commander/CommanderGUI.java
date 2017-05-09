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
 * ProxyInterface.java
 *
 * Does not appear that this is being used any more ... should we delete it?
 *
 * Created on August 12, 2005, 11:44 AM
 *
 */

package AirSim.Commander;

import AirSim.Environment.Assets.Asset.Types;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.State;
import AirSim.Environment.Vector3D;
import AirSim.Machinetta.BasicRole;
import AirSim.SARSensorReading;
import AirSim.Machinetta.SensorReading;
import AirSim.Machinetta.Beliefs.AssetStateBelief;
import AirSim.Machinetta.NAI;
import AirSim.Machinetta.NAIList;
import Machinetta.Debugger;
import Machinetta.RAPInterface.InputMessages.InputMessage;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;
import Machinetta.RAPInterface.RAPInterfaceImplementation;
import Machinetta.State.BeliefType.*;
import Machinetta.State.StateChangeListener;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.ProxyState;
import Machinetta.Configuration;
import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Environment.Vector3D;
import AirSim.Machinetta.BasicRole;
import AirSim.Machinetta.Beliefs.Location;
import AirSim.Machinetta.NAI;
import AirSim.Machinetta.NAIList;
import AirSim.Machinetta.Path2D;
import AirSim.Machinetta.Point2D;
import Machinetta.AA.SimpleAA;
import Machinetta.Coordination.MAC.PlanAgent;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.TeamBelief.Constraints.DirectedInformationRequirement;
import Machinetta.State.BeliefType.TeamBelief.Constraints.GeneratedInformationRequirement;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief;
import Util.*;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

// This stuff is for the GUI
import Gui.*;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.*;
import java.text.DecimalFormat;

// @deprecated
public class CommanderGUI  implements StateChangeListener {
    public static boolean SHOW_GUI = true;
    public static int REPAINT_INTERVAL_MS = 1000/10;
    public final static double AUTO_STRIKE_MIN_PROB = .6;
    public final static int DELAY_BEFORE_CONVOY_PLANS = 3000;
    public final static int DELAY_BEFORE_EAS = 2000;
    public static int DEFAULT_PROB_SIGNIFICANT_MIN = 00;
    public double probSignificantMin = ((double)DEFAULT_PROB_SIGNIFICANT_MIN/100.0);
    
    private boolean autoStrikeFlag = false;
    private boolean useDempsterShaferFlag = false;
    private boolean useProbDistGridFlag = false;
    private int convoyPlansNum = 5;

    private final static DecimalFormat fmt = new DecimalFormat("0.00000");

    private Random rand = new Random();

    private ProxyState state = null;

    private DoubleGrid probDistGrid = null;

    private String baseGridFileName = null;
    private String elevationGridFileName = null;
    private String soilGridFileName = null;
    private String xGridFileName=null;
    private String yGridFileName=null;
    private String zGridFileName=null;
    private IntGrid soilGrid = null;
    private DoubleGrid elevationGrid = null;
    // TODO: 5/16/2005 SRO 
    // 
    // These may not be necessary.  I think they may be needed for
    // the terrain gui but I need to check on that, and really
    // it'd be better if we can make them optional because I don't
    // think we're going to need DIS coords for AirSim stuff.
    private IntGrid xGrid = null;
    private IntGrid yGrid = null;
    private IntGrid zGrid = null;

    private int targetCounter = 1;
    private HashMap<String, String> locationToTargetName = new HashMap<String, String>();
    private String targetKey(double posx, double posy) {
	return ((int)posx/100)+"_"+((int)posy/100);
    }

    private String locationToTargetName(double posx, double posy) {
	String key = targetKey(posx, posy);
	String targetName = (String)locationToTargetName.get(key);
	if(null == targetName) {
	    targetName = "T"+targetCounter++;
	    locationToTargetName.put(key, targetName);
	}
	return targetName;
    }

    private HashMap<ProxyID, Long> lastLocationTime = new HashMap<ProxyID, Long>();
    private HashMap<String, PositionMeters> destMap = new HashMap<String, PositionMeters>();
    MapDB mapDB = null;
    Background background = null;
    BackgroundConfig config = null;
    JPanel actionButtonPanel = null;
    TerrainPanel terrainPanel = null;
    RepaintTimer repaintTimer = null;

    HashMap<String,Hashtable<Asset.Types, Double>> combinedBeliefs = new HashMap<String,Hashtable<Asset.Types, Double>>();
    HashMap sentStrikes = new HashMap();

    HashMap<String,Long> nameToTimeArrived = new HashMap<String,Long>();
    long lastTimeExpiredMapObjects = System.currentTimeMillis();
    long EXPIRE_EVERY_N_MS = 5000;    
    long EXPIRATION_TIME_LIMIT = 1000 * 60 * 5;
    double EXPIRATION_CONFIDENCE_LIMIT = .6;
    private void expireOldSensorReadings() {
	long now = System.currentTimeMillis();
	if(now < (lastTimeExpiredMapObjects + EXPIRE_EVERY_N_MS))
	    return;
	lastTimeExpiredMapObjects = now;
	int sizeBefore = nameToTimeArrived.size();
	long oldestTimeAllowed = now - EXPIRATION_TIME_LIMIT;
	String[] targetNames = nameToTimeArrived.keySet().toArray(new String[0]);
	for(String targetName: targetNames) {
	    if(nameToTimeArrived.get(targetName) < oldestTimeAllowed) {
		MapObject mo = mapDB.get(targetName);
		if(null != mo) {
		    if(mo.getProbSignificant() < EXPIRATION_CONFIDENCE_LIMIT) {
			nameToTimeArrived.remove(targetName);
			mapDB.remove(mo);
		    }
		}
	    }
	}
	Debugger.debug("Expired old sensor readings, size before expiring="+sizeBefore+", size after ="+nameToTimeArrived.size(), 5, this);
    }

    private void addEngagementAreasLater(long delayMs) {
	if(true)return;
	final long fDelayMs = delayMs;
	new Thread() {
		public void run() {
		    try {
			sleep(fDelayMs);
		    } catch (InterruptedException e) {}
		    mapDB.add(new MapObject(MapObject.createKey(),ForceIds.OPFOR, "Engage1", MapObject.TYPE_EA_CANDIDATE, 27000.0, 10500.0, 0.0, 4000.0, 3000.0));
		    mapDB.add(new MapObject(MapObject.createKey(),ForceIds.OPFOR, "Engage2", MapObject.TYPE_EA_CANDIDATE, 9500.0, 17500.0, 0.0, 3000.0, 5000.0));
		    mapDB.add(new MapObject(MapObject.createKey(),ForceIds.OPFOR, "Engage3", MapObject.TYPE_EA_CANDIDATE, 18000.0, 27000.0, 0.0, 4000.0, 4000.0));
		    mapDB.add(new MapObject(MapObject.createKey(),ForceIds.OPFOR, "Engage4", MapObject.TYPE_EA_CANDIDATE, 10500.0, 9000.0, 0.0, 5000.0, 4000.0));
		    mapDB.add(new MapObject(MapObject.createKey(),ForceIds.OPFOR, "Engage5", MapObject.TYPE_EA_CANDIDATE, 19000.0, 11000.0, 0.0, 4000.0, 4000.0));
		    mapDB.add(new MapObject(MapObject.createKey(),ForceIds.OPFOR, "Engage6", MapObject.TYPE_EA_CANDIDATE, 19000.0, 2000.0, 0.0, 4000.0, 4000.0));

		}
	    }.start();
    }

    private void createConvoyPlansLater(long delayMs) {
	final long fDelayMs = delayMs;
        new Thread() {
		public void run() {
		    try {
			sleep(fDelayMs);
		    } catch (InterruptedException e) {}
                
		    //		    createConvoyPlan (13119, 34805, 27643, 10575);
		    if(convoyPlansNum >= 1) 
			createConvoyPlan(13119, 44805, 48000, 44805);
		    if(convoyPlansNum >= 2) 
			createConvoyPlan(13119, 34805, 48000, 34805);
		    if(convoyPlansNum >= 3) 
			createConvoyPlan(13119, 24805, 48000, 24805);
		    if(convoyPlansNum >= 4) 
			createConvoyPlan(13119, 14805, 48000, 14805);
		    if(convoyPlansNum >= 5) 
			createConvoyPlan(13119, 4805, 48000, 4805);
		}
	    }.start();
    }

    public CommanderGUI() {
	state = new ProxyState();
	if(null == Configuration.allMap) {
	    Debugger.debug("Configuration.allMap is null, can't read ctdb file name.", 5, this);
	}
	else {
            
            Machinetta.Debugger.debug("Creating.", 1, this);
            
	    String baseGridFileName = (String) Configuration.allMap.get("CTDB_BASE_NAME");
	    if(null == baseGridFileName) {
		Debugger.debug("CTDB_BASE_NAME is not in Configuration.allMap, can't read ctdb file name.", 5, this);
	    }
	    loadCTDB(baseGridFileName);
	    makeTerrainGui();
	    addEngagementAreasLater(DELAY_BEFORE_EAS);


	    String convoyPlansString = (String) Configuration.allMap.get("CONVOY_PLANS_NUM");
	    if(null == convoyPlansString) {
		Debugger.debug("CONVOY_PLANS_NUM is not in Configuration.allMap, can't read CONVOY_PLANS_NUM flag, leaving as default value="+convoyPlansNum, 5, this);
	    }
	    else {
		convoyPlansNum = new Integer(convoyPlansString).intValue();
	    }

	    String autoStrike = (String) Configuration.allMap.get("USE_AUTO_STRIKE");
	    if(null == autoStrike) {
		Debugger.debug("USE_AUTO_STRIKE is not in Configuration.allMap, can't read USE_AUTO_STRIKE flag.", 5, this);
	    }
	    else {
		if(autoStrike.equalsIgnoreCase("yes")
		   || autoStrike.equalsIgnoreCase("true")
		   || autoStrike.equalsIgnoreCase("1"))
		    autoStrikeFlag = true;
		else 
		    autoStrikeFlag = false;
	    }
	    String useDempsterShafer = (String) Configuration.allMap.get("USE_DEMPSTER_SHAFER");
	    if(null == useDempsterShafer) {
		Debugger.debug("USE_DEMPSTER_SHAFER is not in Configuration.allMap, can't read USE_DEMPSTER_SHAFER flag.", 5, this);
	    }
	    else {
		if(useDempsterShafer.equalsIgnoreCase("yes")
		   || useDempsterShafer.equalsIgnoreCase("true")
		   || useDempsterShafer.equalsIgnoreCase("1"))
		    useDempsterShaferFlag = true;
		else
		    useDempsterShaferFlag = false;
	    }

	    String useProbDistGrid = (String) Configuration.allMap.get("USE_PROBDISTGRID");
	    if(null == useProbDistGrid) {
		Debugger.debug("USE_PROBDISTGRID is not in Configuration.allMap, can't read USE_PROBDISTGRID flag.", 5, this);
	    }
	    else {
		if(useProbDistGrid.equalsIgnoreCase("yes")
		   || useProbDistGrid.equalsIgnoreCase("true")
		   || useProbDistGrid.equalsIgnoreCase("1"))
		    useProbDistGridFlag = true;
		else
		    useProbDistGridFlag = false;
	    }
	    String probDistGridFileName = (String) Configuration.allMap.get("PROBDISTGRID_NAME");
	    if(null == probDistGridFileName) {
		Debugger.debug("PROBDISTGRID_NAME is not in Configuration.allMap, can't read probdist grid file name.", 5, this);
	    }
	    else {
		probDistGrid = new DoubleGrid();
		probDistGrid.loadGridFile(probDistGridFileName);
	    }
            
	    String showGuiString = (String) Configuration.allMap.get("SHOW_GUI");
	    if(null == showGuiString) {
		Debugger.debug("SHOW_GUI is not in Configuration.allMap, can't read SHOW_GUI flag, defaults to"+SHOW_GUI, 5, this);
	    }
	    else {
		if(showGuiString.equalsIgnoreCase("yes")
		   || showGuiString.equalsIgnoreCase("true")
		   || showGuiString.equalsIgnoreCase("1"))
		    SHOW_GUI = true;
		else 
		    SHOW_GUI = false;
	    }
	    String repaintInterval = (String) Configuration.allMap.get("REPAINT_INTERVAL_MS");
	    if(null == repaintInterval) {
		Debugger.debug("REPAINT_INTERVAL_MS is not in Configuration.allMap, can't read REPAINT_INTERVAL_MS flag, defaults to"+REPAINT_INTERVAL_MS, 5, this);
	    }
	    else {
		REPAINT_INTERVAL_MS = Integer.parseInt(repaintInterval);
	    }
	    
	    Debugger.debug("USE_AUTO_STRIKE="+autoStrikeFlag+", USE_DEMPSTER_SHAFER="+useDempsterShaferFlag+", USE_PROBDISTGRID="+useProbDistGridFlag+", PROBDISTGRID_NAME="+probDistGridFileName, 5, this);
	}

	state.addChangeListener(this);

        repaintTimer.start();

	createConvoyPlansLater(DELAY_BEFORE_CONVOY_PLANS);
    }

    private void combineBelief(Hashtable<Asset.Types, Double> b1, Hashtable<Asset.Types, Double>b2) {
 	Asset.Types type = null;
 	double conflict = 0.0;
 	double r1 = 0.0;
 	double r2 = 0.0;
 	Iterator iter = b1.keySet().iterator();
 	while(iter.hasNext()) {
 	    type = (Asset.Types)iter.next();
 	    r1 = b1.get(type);
 	    r2 = b2.get(type);
 	    conflict = conflict + r1*(1 - r2 - b2.get(Asset.Types.CLUTTER));
	    System.err.println("CommanderGUI.combineBelief: type="+type+", r1="+r2+", r2="+r2+", clutter="+b2.get(Asset.Types.CLUTTER)+", conflict="+conflict);
 	}
 	Iterator iter2 = b1.keySet().iterator();
 	while(iter2.hasNext()) {
 	    type = (Asset.Types)iter2.next();
 	    r1 = b1.get(type);
 	    r2 = b2.get(type);
	    double newConfidence = 0.0;

	    newConfidence = (r1*r2 + r1*b2.get(Asset.Types.CLUTTER) + r2*b1.get(Asset.Types.CLUTTER))/(1 - conflict);
	    b1.put(type, newConfidence);
	    System.err.println("CommanderGUI.combineBelief: type="+type+", newConfidence="+newConfidence);

	    newConfidence= (b1.get(Asset.Types.CLUTTER)*b2.get(Asset.Types.CLUTTER))/(1-conflict); 
	    b1.put(Asset.Types.CLUTTER, newConfidence) ;
	    System.err.println("CommanderGUI.combineBelief: CLUTTER newConfidence="+newConfidence);
 	}
    }

    private void combineBeliefWithTerrainAnalysis(Hashtable<Asset.Types, Double> b1, int x, int y, DoubleGrid probDist) {
	
	// Get the prob from the probability distribution;
	int gridx = probDist.toGridX(x);
	int gridy = probDist.toGridY(y);
	// The grid should be normalized, so this variable prob should be between 0 and 1 
	double terrainAnalysisProb = probDist.getValue(gridx, gridy);

 	Asset.Types type = null;
 	double prob = 0.0;
	double nonClutterCumulative = 0.0;

 	Iterator iter = b1.keySet().iterator();
 	while(iter.hasNext()) {
 	    type = (Asset.Types)iter.next();
 	    prob = b1.get(type);
	    if(type != Asset.Types.CLUTTER) {
		nonClutterCumulative += prob;;
	    }
	}
	double newClutterProb = 1 - (nonClutterCumulative * (terrainAnalysisProb + 0.5));
	b1.put(Asset.Types.CLUTTER, newClutterProb);
		
	iter = b1.keySet().iterator();
 	while(iter.hasNext()) {
 	    type = (Asset.Types)iter.next();
 	    prob = b1.get(type);
	    if(type != Asset.Types.CLUTTER) {
		prob = prob * (0.5 + terrainAnalysisProb);
		b1.put(type, prob);
	    }
	}
    }

    private double shouldStrike(Hashtable<Asset.Types, Double> belief) {
 	double prob = 0.0;
	double shouldStrikeProb = 0.0;

	for(Asset.Types type: belief.keySet()) {
 	    prob = belief.get(type);
	    if((Types.TWOS6 == type)  
	       || (Types.M1A1 == type)  
	       || (Types.M1A2 == type)  
	       || (Types.M2 == type)  
	       || (Types.T72M == type)  
	       || (Types.T80 == type)  
	       || (Types.M977 == type)  
	       || (Types.M35 == type)  
	       || (Types.AVENGER == type)  
	       || (Types.HMMMV == type)  
	       || (Types.SA9 == type)  
	       || (Types.ZSU23_4M == type)
	       ) 
	       shouldStrikeProb += prob;
	}
	return shouldStrikeProb;
    }

    private int convertType(Types type) {
	if(Types.HMMMV == type) {
	    return UnitTypes.MECH_INFANTRY;
	}
	else if(Types.WASM == type) {
	    return UnitTypes.WASM;
	}
	else if(Types.A10 == type) {
	    return UnitTypes.AIR_FORCES;
	}
	else if(Types.C130 == type) {
	    return UnitTypes.AIR_FORCES;
	}
	else if(Types.TWOS6 == type) {
	    return UnitTypes.AIR_DEFENSE_ARTILLERY;
	}
	else if(Types.F14 == type) {
	    return UnitTypes.AIR_FORCES;
	}
	else if(Types.F15 == type) {
	    return UnitTypes.AIR_FORCES;
	}
	else if(Types.F16 == type) {
	    return UnitTypes.AIR_FORCES;
	}
	else if(Types.F18 == type) {
	    return UnitTypes.AIR_FORCES;
	}
	else if(Types.M1A1 == type) {
	    return UnitTypes.ARMOR;
	}
	else if(Types.M1A2 == type) {
	    return UnitTypes.ARMOR;
	}
	else if(Types.M2 == type) {
	    return UnitTypes.ARMOR;
	}
	else if(Types.MUAV == type) {
	    return UnitTypes.MUAV;
	}
	else if(Types.T72M == type) {
	    return UnitTypes.ARMOR;
	}
	else if(Types.T80 == type) {
	    return UnitTypes.ARMOR;
	}
	else if(Types.TRUCK == type) {
	    return UnitTypes.CIVILIAN_TRUCK;
	}
	else if(Types.M977 == type) {
	    return UnitTypes.MILITARY_TRUCK;
	}
	else if(Types.M35 == type) {
	    return UnitTypes.MILITARY_TRUCK;
	}
	else if(Types.AVENGER == type) {
	    return UnitTypes.WASM;
	}
	else if(Types.HMMMV == type) {
	    return UnitTypes.MECH_INFANTRY;
	}
	else if(Types.SA9 == type) {
	    return UnitTypes.AIR_DEFENSE_ARTILLERY;
	}
	else if(Types.CLUTTER == type) {
	    return UnitTypes.CLUTTER;
	}
	else if(Types.ZSU23_4M == type) {
	    return UnitTypes.AIR_DEFENSE_ARTILLERY;
	}
	else 
	    return UnitTypes.UNKNOWN;
    }
    public void stateChanged (BeliefID [] b) {
// 	if(!SHOW_GUI)
// 	    return;

	expireOldSensorReadings();
	for(int loopi = 0; loopi < b.length; loopi++) {
	    Belief belief = state.getBelief(b[loopi]);
	    try {

		if(belief instanceof AirSim.Machinetta.Beliefs.AssetStateBelief) {
		    AssetStateBelief assetState = (AssetStateBelief)belief;
		    Long lastTimeL = lastLocationTime.get(assetState.pid);
		    long lastTime = 0;
		    if(null != lastTimeL)
			lastTime = lastTimeL.longValue();
		    if(lastTime > assetState.time) {
			Debugger.debug(1,"stateChange:AssetStateBelief:Old Location - ignoring (last location time="+lastTime+"), id='"+assetState.pid+"', time='"+assetState.time+"', x,y="+assetState.xMeters+","+assetState.yMeters);
		    } 
		    else {
			Debugger.debug(1,"stateChange:AssetStateBelief:New Location (last location time="+lastTime+"), id='"+assetState.pid+"', time='"+assetState.time+"', x,y="+assetState.xMeters+","+assetState.yMeters);
			lastLocationTime.put(assetState.pid, assetState.time);
			double posx = assetState.xMeters;
			double posy = assetState.yMeters;
			double heading = assetState.headingDegrees;

			String moId = assetState.pid.toString();
			MapObject mo = mapDB.get(moId);
			if(null == mo) {
			    String moName = moId;
			    int unitType = UnitTypes.WASM;
			    if(moId.startsWith("B-H")) {
				moName = moId;
				unitType = UnitTypes.MECH_INFANTRY;
			    }
			    mo = new MapObject(moId, ForceIds.BLUEFOR, moName, unitType, UnitSizes.SINGLE, posx, posy, 0, 0, 0, 0);
			    mo.setEditable(false);
			    mapDB.add(mo);
			    mapDB.setDirty(true);
			}
			mo.setPos(posx, posy, 0);
			mo.setOrientation(heading);
			PositionMeters destLoc = destMap.get(moId);
			if(null != destLoc) {
			    mo.setDest(destLoc.getX(), destLoc.getY(),0);
			}
			mapDB.setDirty(true);
		    }

		}
		else if(belief instanceof AirSim.Machinetta.SensorReading) {
		    SensorReading reading = (SensorReading)belief;
		    double posx = reading.loc.getX();
		    double posy = reading.loc.getY();
		    double heading = reading.heading;
		    String targetName = locationToTargetName(posx, posy);
		    nameToTimeArrived.put(targetName, System.currentTimeMillis());
		    if(reading.isSAR) {
			Debugger.debug("stateChange:SensorReading:got SAR, id='"+reading.sensor+"', time='"+reading.time+"',  type='"+reading.type+"', loc='"+reading.loc+"', probs='"+reading.SARProbs+"'", 1, this);
			Hashtable<Asset.Types, Double> classificationBelief = null;

			if(useProbDistGridFlag) {
			    combineBeliefWithTerrainAnalysis(reading.SARProbs, (int)posx, (int)posy, probDistGrid);
			}

			classificationBelief = combinedBeliefs.get(targetName);
			if(useDempsterShaferFlag && (null != classificationBelief)) {
			    combineBelief(classificationBelief, reading.SARProbs);
			}
			else {
			    classificationBelief = reading.SARProbs;
			}
			combinedBeliefs.put(targetName,classificationBelief);
			double highestProb = 0.0;
			Types highestProbType = Types.M1A1;
			String commentsTable = "";
			for (Enumeration e = classificationBelief.keys(); e.hasMoreElements(); ) {
			    Types t = (Types)e.nextElement();
			    double prob = classificationBelief.get(t);
			    if(prob > highestProb) {
				highestProb = prob;
				highestProbType = t;
			    }
			    commentsTable += fmt.format(prob) + " : " + t+"\n";
			    Debugger.debug("targetName="+targetName+", highest prob ("+fmt.format(highestProb)+") type ="+highestProbType, 0, this);
			}
			MapObject mo = null;
			mo = mapDB.get(targetName);
			if(null != mo) {
			    Debugger.debug("Old MapObject name='"+targetName+"' reading.type='"+reading.type+"'", 5, this);
			}
			else {
			    Debugger.debug("New MapObject name='"+targetName+"' loc="+posx+","+posy+", reading.type='"+reading.type+"'", 5, this);
			    int unitType = convertType(highestProbType);
			    mo = new MapObject(targetName, ForceIds.OPFOR, targetName, unitType, UnitSizes.SINGLE, posx, posy, 0, 0, 0, 0);
			    mo.setEditable(false);
			    mapDB.add(mo);
			    mapDB.setDirty(true);
			}
			mo.setComments(commentsTable);
			if(null != reading.state) {
			    if(reading.state == State.LIVE) {
				mo.setBda(mo.BDA_ALIVE);
			    }
			    else if(reading.state == State.DESTROYED) {
				mo.setBda(mo.BDA_DEAD);
			    }
			}
			double probSignificant = 0.0;
			for (Enumeration e = classificationBelief.keys(); e.hasMoreElements(); ) {
			    Types t = (Types)e.nextElement();
			    double prob = classificationBelief.get(t);
			    if((t != Types.TRUCK) && (t != Types.CLUTTER))
				probSignificant += prob;
			    mo.addClassification(t.toString(), prob);
			    Debugger.debug("Added classification "+t+" prob "+prob+" to MapObject "+targetName, 0, this);
			}
			mo.setProbSignificant(probSignificant);

			mo.setPos(posx, posy, 0);
			mo.setOrientation(heading);
			mapDB.setDirty(true);
			if(autoStrikeFlag) {
			    double shouldStrikeProb = shouldStrike(classificationBelief);
			    if(shouldStrikeProb < AUTO_STRIKE_MIN_PROB) { 
				Debugger.debug("stateChange: autoStrike: no - shouldStrikeProb="+shouldStrikeProb+", less then min "+AUTO_STRIKE_MIN_PROB, 1, this);
			    }
			    else {
				if(sentStrikes.containsKey(targetKey(posx, posy))) {
				    Debugger.debug("stateChange: autoStrike: already sent strike plan for target "+mo.getKey()+" at point "+(int)posx+","+(int)posy, 1, this);
				}
				else {
				    createStrikePlan((int)posx, (int)posy);
				    sentStrikes.put(targetKey(posx, posy),mo);
				    mapDB.add(new MapObject(MapObject.createKey(),ForceIds.OPFOR, "Strike", MapObject.TYPE_STRIKE, posx, posy, 0.0, 0.0, 0.0));
				    mapDB.setDirty(true);
				    Debugger.debug("stateChange: autoStrike: Strike plan issued for target "+mo.getKey()+" at point "+(int)posx+","+(int)posy, 5, this);
				}
			    }
			}
		    }
		    else {
			Debugger.debug("stateChange:SensorReading: got nonSAR SensorReading, id='"+reading.sensor+"', time='"+reading.time+"',  type='"+reading.type+"', loc='"+reading.loc+"', probs='"+reading.SARProbs+"'", 1, this);
			MapObject mo = null;
			mo = mapDB.get(targetName);
			if(null != mo) {
			    Debugger.debug("Old MapObject name='"+targetName+"' reading.type='"+reading.type+"'", 5, this);
			}
			else {
			    Debugger.debug("New MapObject name='"+targetName+"' reading.type='"+reading.type+"'", 5, this);
			    int unitType = convertType(reading.type);
			    mo = new MapObject(targetName, ForceIds.OPFOR, targetName, unitType, UnitSizes.SINGLE, posx, posy, 0, 0, 0, 0);
			    mo.setEditable(false);
			    mapDB.add(mo);
			    mapDB.setDirty(true);
			}
			if(null != reading.state) {
			    if(reading.state == State.LIVE) {
				mo.setBda(mo.BDA_ALIVE);
			    }
			    else if(reading.state == State.DESTROYED) {
				mo.setBda(mo.BDA_DEAD);
			    }
			}
			mo.setOrientation(heading);
			mo.setPos(posx, posy, 0);
			mapDB.setDirty(true);
		    }
		}
		else if(belief instanceof AirSim.Machinetta.BasicRole) {
		    Debugger.debug("stateChange:BasicRole", 1, this);
		    BasicRole brole = (BasicRole)belief;
		    RAPBelief responsible = brole.getResponsible();
		    PositionMeters destLoc = null;
		    String taskType = "";
		    if(null != responsible) {
			String moId = responsible.getID().toString();
			if(TaskType.patrol == brole.getType()) {
			    NAI nai = (NAI)brole.params.get("NAI");
			    destLoc = new PositionMeters((nai.x1+nai.x2)/2, (nai.y1+nai.y2)/2,0);
			    taskType = "patrol";		    
			    Debugger.debug("stateChange: BasicRole "+taskType+" at "+destLoc.getX()+","+destLoc.getY()+" assigned to "+moId+", added NAI to mapdb.", 1, this);
			}
			else if(TaskType.attack == brole.getType()) {
			    PositionMeters loc = (PositionMeters)brole.params.get("Location");
			    destLoc = loc;
			    taskType = "attack";
			    destMap.put(moId, destLoc);
			    MapObject mo = mapDB.get(moId);
			    if(null != mo) {
				mo.setDest(destLoc.getX(), destLoc.getY(),0);
				mapDB.setDirty(true);
				Debugger.debug("stateChange: BasicRole "+taskType+" at "+destLoc.getX()+","+destLoc.getY()+" assigned to "+moId, 1, this);
			    }
			    else {
				//				Debugger.debug("stateChange: WARNING BasicRole "+taskType+" at "+destLoc.getX()+","+destLoc.getY()+"  assigned to "+moId+" but can't find corresponding map object", 1, this);
			    }
			}
			else {
			    Debugger.debug("stateChange: BasicRole of unfamiliar type: "+brole.getType(), 1, this);
			    
			}
		    }
		}
		else if(belief instanceof AirSim.Machinetta.NAI) {
		    Debugger.debug("stateChange:NAI="+belief, 1, this);
		    NAI nai = (NAI) belief;
		    double xcenter = (nai.x1+nai.x2)/2;
		    double ycenter = (nai.y1+nai.y2)/2;
		    double width = nai.x1 - nai.x2;
		    if(width < 0) width = -width;
		    double height = nai.y1 - nai.y2;
		    if(height < 0) height = -height;
		    mapDB.add(new MapObject(MapObject.createKey(),ForceIds.UNKNOWN, belief.getID().toString(), 
					    MapObject.TYPE_EA_CANDIDATE, xcenter, ycenter, 0.0, width, height));
		}
		else if(belief instanceof AirSim.Machinetta.Path2D) {
		    Debugger.debug("stateChange:Path="+belief, 1, this);
		    Path2D path = (Path2D) belief;
		    //     public MapObject(String key, int type, double posX, double posY, double posZ, double sizeX, double sizeY)
		    Point2D firstPoint = path.wps.getFirst();
		    MapObject mo = new MapObject(MapObject.createKey(), MapObject.TYPE_LINE, firstPoint.getX(), firstPoint.getY(), 0, 0, 0);
		    for(Point2D point: path.wps) {
			mo.addLinePoint(point.getX(), point.getY());
		    }
		    mapDB.add(mo);
		}
		else if(belief instanceof Machinetta.State.BeliefType.RAPBelief) {
		    Debugger.debug("stateChange:RAPBelief", 1, this);
		}
		else if(belief instanceof Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief) {
		    Debugger.debug("stateChange:TeamPlanBelief", 1, this);
		}
		else if(belief instanceof Machinetta.State.BeliefType.TeamBelief.RoleAllocationBelief) {
		    Debugger.debug("stateChange:RoleAllocationBelief", 1, this);
		}
		else {
		    Debugger.debug("stateChange:Unknown class "+belief.getClass().getName()+" for new belief="+belief.toString(), 5, this);		
		}
	    } catch (Exception e) {
		Debugger.debug("stateChange:Exception processing changed belief='"+belief+"', e="+e,5, this);
		e.printStackTrace();
	    }
	}

    }

    private void loadCTDB(String baseGridFileName) {
	Debugger.debug("Loading CTDBs.", 0, this);

	baseGridFileName = baseGridFileName;

	if(null == elevationGridFileName)
	    elevationGridFileName = baseGridFileName+"_e.grd";
	if(null == soilGridFileName)
	    soilGridFileName = baseGridFileName+"_s.grd";
	if(null == xGridFileName)
	    xGridFileName = baseGridFileName+"_x.grd";
	if(null == yGridFileName)
	    yGridFileName = baseGridFileName+"_y.grd";
	if(null == zGridFileName)
	    zGridFileName = baseGridFileName+"_z.grd";

	soilGrid = new IntGrid();
	soilGrid.loadSoilTypeFile(soilGridFileName);
	elevationGrid = new DoubleGrid();
	elevationGrid.loadGridFile(elevationGridFileName);
	xGrid = new IntGrid();
	xGrid.loadGridFile(xGridFileName);
	yGrid = new IntGrid();
	yGrid.loadGridFile(yGridFileName);
	zGrid = new IntGrid();
	zGrid.loadGridFile(zGridFileName);

    }

    private void createConvoyPlan(int sx, int sy, int dx, int dy) {
        
        long planID = rand.nextLong();
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        // Create the roles for WASM patrols
        for (int no = 0; no < 5; no++) {
            BasicRole basic = new BasicRole(TaskType.patrol);
            Hashtable<String, Object> params = new Hashtable<String, Object>();
            params.put("NAIPriority", no);
            params.put("planID", planID);
            basic = (BasicRole)basic.instantiate(params);
            basic.constrainedWait = false;
            
            basic.generatedInfo = new Vector<GeneratedInformationRequirement>();
            try {
                basic.generatedInfo.add(new GeneratedInformationRequirement("NAI", Class.forName("AirSim.Machinetta.NAI"), new BeliefNameID("NAI-"+no+"-"+planID)));
            } catch (ClassNotFoundException e) {
                Machinetta.Debugger.debug("Could not find class for a GeneratedInformationRequirement: " + e, 4, this);
            }
            
            basic.infoSharing = new Vector<DirectedInformationRequirement>();
            try {
                basic.infoSharing.add(new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.Beliefs.Location"), new NamedProxyID("Commander")));
                basic.infoSharing.add(new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.SensorReading"), new NamedProxyID("Commander")));
            } catch (ClassNotFoundException e) {
                Machinetta.Debugger.debug("Could not find class for a DirectedInformationRequirement: " + e, 4, this);
            }
            roles.add(basic);
        }
        
        // Create the roles for moving.
        for (int no = 0; no < 5; no++) {
            BasicRole basic = new BasicRole(TaskType.move);
            Hashtable<String, Object> params = new Hashtable<String, Object>();
            params.put("Destination", new PositionMeters(dx, dy, 0));
            params.put("Follow", no - 1);
            basic = (BasicRole)basic.instantiate(params);
            basic.constrainedWait = false;

            basic.generatedInfo = new Vector<GeneratedInformationRequirement>();
            try {
                basic.generatedInfo.add(new GeneratedInformationRequirement("Path", Class.forName("AirSim.Machinetta.Path"), new BeliefNameID("Path-"+planID)));                
            } catch (ClassNotFoundException e) {
                Machinetta.Debugger.debug("Could not find class for a GeneratedInformationRequirement: " + e, 4, this);
            }
            
            basic.infoSharing = new Vector<DirectedInformationRequirement>();            
            try {
                basic.infoSharing.add(new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.Location"), new NamedProxyID("Commander")));
                basic.infoSharing.add(new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.SensorReading"), new NamedProxyID("Commander")));
            } catch (ClassNotFoundException e) {
                Machinetta.Debugger.debug("Could not find class for a DirectedInformationRequirement: " + e, 4, this);
            }
            roles.add(basic);
        }
        
        // Add the role for determining engagement areas
        BasicRole basic = new BasicRole(TaskType.determineEngagementAreas);
        Hashtable<String, Object> params = new Hashtable<String, Object>();
        params.put("Number", 5);
        params.put("x1", sx);
        params.put("y1", sy);
        params.put("x2", dx);
        params.put("y2", dy);
        basic.infoSharing = new Vector<DirectedInformationRequirement>();
        try {
            // TA must provide information about engagement areas (NAIs)
            basic.infoSharing.add(new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.NAI"), new NamedProxyID("Commander"), new BeliefNameID("NAI-0-"+planID)));
            basic.infoSharing.add(new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.NAI"), new NamedProxyID("Commander"), new BeliefNameID("NAI-1-"+planID)));
            basic.infoSharing.add(new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.NAI"), new NamedProxyID("Commander"), new BeliefNameID("NAI-2-"+planID)));
            basic.infoSharing.add(new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.NAI"), new NamedProxyID("Commander"), new BeliefNameID("NAI-3-"+planID)));
            basic.infoSharing.add(new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.NAI"), new NamedProxyID("Commander"), new BeliefNameID("NAI-4-"+planID)));
	    //            basic.infoSharing.add(new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.NAI"), new NamedProxyID("Commander"), new BeliefNameID("NAI-5-"+planID)));                        
        } catch (ClassNotFoundException e) {
            Machinetta.Debugger.debug("Could not find class for a DirectedInformationRequirement: " + e, 4, this);
        }
        basic = (BasicRole)basic.instantiate(params);
        basic.constrainedWait = false;
        roles.add(basic);

        // Add the role for determining a safe route
        basic = new BasicRole(TaskType.determineLeastResistancePath);
        params = new Hashtable<String, Object>(); 
        params.put("type", "ground");
        params.put("x1", sx);
        params.put("y1", sy);
        params.put("x2", dx);
        params.put("y2", dy);
        basic.infoSharing = new Vector<DirectedInformationRequirement>();
        try {
            // TA must provide information about the best path to take
            basic.infoSharing.add(new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.Path"), new NamedProxyID("Commander"), new BeliefNameID("Path-"+planID)));                                 
        } catch (ClassNotFoundException e) {
            Machinetta.Debugger.debug("Could not find class for a DirectedInformationRequirement: " + e, 4, this);
        }
        basic = (BasicRole)basic.instantiate(params);
        basic.constrainedWait = false;
        roles.add(basic);
        
        // Finally make the plan and PlanAgent
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("Convoy"+sx+":"+sy), "ProtectedConvoy", null, true, new Hashtable<String,Object>(), roles);
        PlanAgent pa = new PlanAgent(tpb);        
    }

    private void createStrikePlan(int x, int y) {
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        // Currently only a single strike on the target.
        BasicRole basic = new BasicRole(TaskType.attack);
        Hashtable<String, Object> params = new Hashtable<String, Object>();
        params.put("Location", new PositionMeters(x, y, 0));
        basic = (BasicRole)basic.instantiate(params);
        basic.constrainedWait = false;
        basic.infoSharing = new Vector<DirectedInformationRequirement>();
        try {
            basic.infoSharing.add(new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.Location"), new NamedProxyID("Commander")));
            basic.infoSharing.add(new DirectedInformationRequirement(Class.forName("AirSim.Machinetta.SensorReading"), new NamedProxyID("Commander")));
        } catch (ClassNotFoundException e) {
            Machinetta.Debugger.debug("Could not find class for a DirectedInformationRequirement: " + e, 4, this);
        }
        roles.add(basic);
	TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("Strike:"+x+":"+y), "StrikePlan", null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
    }
    

    public void strike() {
	Debugger.debug("Strike button clicked", 5, this);

	MapObject mapObjects[] = mapDB.getMapObjects();
	if(null != mapObjects) {
	    for(int loopi = 0; loopi < mapObjects.length; loopi++) {
		MapObject mo = mapObjects[loopi];
		if(null == mo)
		    continue;
		if(mo.TYPE_STRIKE == mo.getType()) {
		    mo.setEditable(false);
		    String strikeKey = targetKey(mo.getPosX(), mo.getPosY());
		    if(sentStrikes.containsKey(strikeKey))
			continue;
		    Debugger.debug("Ordering strike "+strikeKey+" at "+mo.getPosX()+","+mo.getPosY()+","+mo.getPosZ(), 5 ,this);
		    createStrikePlan((int)mo.getPosX(), (int)mo.getPosY());
		    sentStrikes.put(strikeKey,mo);
		}
	    }
	}
    }

    public void makeTerrainGui() {
	JFrame topLevelFrame = null;
	JPanel topPanel = null;
	topLevelFrame = new JFrame("                                                            CommanderGUI");
	topLevelFrame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {System.exit(0);}
	    });
	// For some reason I don't understand, if I just use 0,0 here
	// (the top left of the screen), xwin-32 puts the window
	// slightly off the screen to the top and the left.
 	topLevelFrame.setLocation(5,30);
	topLevelFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
	topPanel = new JPanel();
	topPanel.setLayout( new BorderLayout() );
	topLevelFrame.getContentPane().add( topPanel );

	// ----------------------------------------------------------------------
	// Build the obstacle grids - slope and soil type based.
	// ----------------------------------------------------------------------
	Obstacles obstacles = null;
	IntGrid obstacleGrid = null;
	obstacles = new Obstacles(elevationGrid, soilGrid);
	obstacleGrid = obstacles.getObstacleGrid();

	// ----------------------------------------------------------------------
	// Build the contour lines.
	// ----------------------------------------------------------------------
	IntGrid contourPolygonsGrid = null;
	Contour contours = null;
	contourPolygonsGrid = new IntGrid(obstacleGrid);
 	contourPolygonsGrid.clear();
	contours = new Contour(elevationGrid);
	contours.compute(25.0);

	ViewPort viewPort = new ViewPort(soilGrid);
	mapDB = new MapDB(soilGrid.getGccToTccTransform(), soilGrid.getTccToGccTransform(), xGrid, yGrid, zGrid);

	background = new Background(elevationGrid, soilGrid, contourPolygonsGrid, obstacleGrid, null, contours, null);
	config = new BackgroundConfig(viewPort);

	actionButtonPanel = new JPanel();

	final CommanderGUI commanderGUI = this;
	final JButton strikeButton = new JButton("Strike");
	actionButtonPanel.add(strikeButton);
	strikeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    commanderGUI.strike();
                }
            });
	actionButtonPanel.add(new JLabel("Confidence: "));
	final JSlider confidenceSlider = new JSlider(0, 100, DEFAULT_PROB_SIGNIFICANT_MIN);
	actionButtonPanel.add(confidenceSlider);
	confidenceSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
		    commanderGUI.probSignificantMin = ((double)confidenceSlider.getValue())/100.0;
		    terrainPanel.setProbSignificantMin(commanderGUI.probSignificantMin);
                }
            });

	terrainPanel = new TerrainPanel(mapDB,background,config, actionButtonPanel);
	repaintTimer = new RepaintTimer(terrainPanel, mapDB, REPAINT_INTERVAL_MS);

	commanderGUI.probSignificantMin = ((double)confidenceSlider.getValue())/100.0;
	terrainPanel.setProbSignificantMin(commanderGUI.probSignificantMin);

	topPanel.add(terrainPanel);
	// Note, DON'T use .pack() - or we undo all the
	// setSize/setLocation crap above.

	topLevelFrame.setVisible(SHOW_GUI);
    }
}

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
 * MapUtil.java
 *
 * Created Wed Apr 12 16:19:06 EDT 2006
 *
 */

package AirSim.Commander;

import AirSim.Environment.Assets.Asset.Types;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Environment.Assets.UnattendedGroundSensor;
import AirSim.Environment.Vector3D;
import AirSim.Machinetta.BasicRole;
import AirSim.Machinetta.Beliefs.FlyZone;
import AirSim.Machinetta.Beliefs.GeoLocateRequest;
import AirSim.Machinetta.Beliefs.GeoLocateResult;
import AirSim.Machinetta.Beliefs.ImageData;
import AirSim.Machinetta.Beliefs.TMAScanResult;
import AirSim.Machinetta.Beliefs.UAVLocation;
import AirSim.Machinetta.Beliefs.UGSSensorReading;
import AirSim.Machinetta.Beliefs.VehicleBelief;
import AirSim.Machinetta.Beliefs.AssetStateBelief;
import AirSim.Machinetta.NAI;
import AirSim.Machinetta.NAIList;
import AirSim.Machinetta.Path2D;
import AirSim.Machinetta.Path3D;
import AirSim.Machinetta.PlannedPath;
import AirSim.Machinetta.Point2D;
import AirSim.Machinetta.SensorReading;
import AirSim.SARSensorReading;
import Machinetta.AA.SimpleAA;
import Machinetta.Configuration;
import Machinetta.Coordination.MAC.InformationAgent;
import Machinetta.Coordination.MAC.PlanAgent;
import Machinetta.Coordination.MACoordination;
import Machinetta.Debugger;
import Machinetta.RAPInterface.InputMessages.InputMessage;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;
import Machinetta.RAPInterface.RAPInterfaceImplementation;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.*;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.TeamBelief.Constraints.DirectedInformationRequirement;
import Machinetta.State.BeliefType.TeamBelief.Constraints.GeneratedInformationRequirement;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief;
import Machinetta.State.ProxyState;
import Machinetta.State.StateChangeListener;
import Util.*;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import java.io.IOException;

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

/** 
 *
 * MapUtil is the old class used to maintain world state that the new
 * {@link WorldStateMgr} in the SimUser API is based on. It is not part
 * of the SimUser API, but it may be instructive to review. The most
 * important method is addBelief, which decodes the belief class and
 * hands the belief off to one of many public methods that does the
 * actual world state maintenance based on what kind of belief it is.
 * These other methods are public on the off chance that we need to
 * add such a belief to world state without going through other
 * mechanisms... although we really could (and probably should) simply
 * call addBelief() and make the other methods private.  Anyway,
 * you're going to see a whole bunch of pretty useless docs like
 * "addUGSSensorReading - add a UGSSensorReading" but at least that'll
 * let you know what kind of beliefs we can deal with.  This is a
 * legacy class from an earlier Operator interface.  Most of the code
 * here will be moved into a separate class (probably WorldStateMgr)
 * in order to reduce complexity for the public SimUser API while
 * avoiding breaking legacy applications that use this class.  There's
 * a good chance a lot of these methods will go away in the SimUser
 * API unless we need them.  In particular, MapDB will probably not be
 * part of the SimUser API.  Fuser may possibly remain in the SimUser
 * API.
 *
 * @author owens
 *
 */
public class MapUtil {
    private final static DecimalFormat fmt = new DecimalFormat("0.000");
    private final static double FUSION_DISTANCE=500;

    private final static double EXPIRATION_CONFIDENCE_LIMIT = .6;
    private final static long RUN_SENSOR_READING_EXPIRE_EVERY_N_MS = 1000;    
    private final static long SENSOR_READING_EXPIRATION_LIMIT_MS = 1000 * 20;

    private String iconDir=null;
    private MapDB mapDB;

    private long lastTimeExpiredMapObjects = System.currentTimeMillis();

    private HashMap<ProxyID, Long> lastLocationTime = new HashMap<ProxyID, Long>();
    private HashMap<String, PositionMeters> destMap = new HashMap<String, PositionMeters>();

    private Fuser fuser = new Fuser();

    private HashMap<String,Hashtable<Asset.Types, Double>> combinedBeliefs = new HashMap<String,Hashtable<Asset.Types, Double>>();
    private HashMap sentStrikes = new HashMap();

    private HashMap<Integer, ImageIcon> imageryMap = new HashMap<Integer, ImageIcon>();

    private HashMap<ProxyID,PlannedPath> assetPathMap = new HashMap<ProxyID,PlannedPath>();
    private HashMap<ProxyID,PlannedPath> unusedPathMap = new HashMap<ProxyID,PlannedPath>();
    private HashSet<BeliefID> seenPaths = new HashSet<BeliefID>();

    private HashMap<ProxyID,FlyZone> flyZoneMap = new HashMap<ProxyID,FlyZone>();

    private void loadImagery() {
	String baseDir=iconDir+"/original";
	imageryMap.put(UnitTypes.AIR_DEFENSE_ARTILLERY, new ImageIcon(baseDir+"/SA-9 GASKIN STRELA 1.gif"));
	imageryMap.put(UnitTypes.ARMOR, new ImageIcon(baseDir+"/M1A2 Abrams sand.gif"));
	imageryMap.put(UnitTypes.LIGHT_INFANTRY, new ImageIcon(baseDir+"/HUMVEE CAMO.gif"));
	imageryMap.put(UnitTypes.CIVILIAN_TRUCK, new ImageIcon(baseDir+"/white_pickup.gif"));
    }

    private void loadIcons(String baseDir) {
	UnitTypes.setImageIcon(UnitTypes.AIR_DEFENSE_ARTILLERY, new ImageIcon(baseDir+"/SA-9 GASKIN STRELA 1.gif"));
	UnitTypes.setImageIcon(UnitTypes.ARMOR, new ImageIcon(baseDir+"/M1A2 Abrams sand.gif"));
	UnitTypes.setImageIcon(UnitTypes.LIGHT_INFANTRY, new ImageIcon(baseDir+"/HUMVEE CAMO.gif"));
	UnitTypes.setImageIcon(UnitTypes.CIVILIAN_TRUCK, new ImageIcon(baseDir+"/white_pickup.gif"));
    }

    private void loadIconsOriginal() {
	loadIcons(iconDir+"/original");
    }

    private void loadIcons50() {
	loadIcons(iconDir+"/scale50");
    }

    private void loadIcons25() {
	loadIcons(iconDir);
    }

    /**
     * Constructor.
     *
     * @param mapDB object that this class updates with new world state
     * @param iconDir directory where the icons used on the map GUI reside.
     */
    public MapUtil(MapDB mapDB, String iconDir) {
	this.mapDB = mapDB;
	this.iconDir = iconDir;
	loadImagery();
    }

    /**
     * Accessor to retrieve mapDB.
     * 
     * @return the instance of the mapDB used by the GUI.
     */
    public MapDB getMapDB() {
        return this.mapDB;
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
	    return UnitTypes.AIR_DEFENSE_ARTILLERY;
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

    /**
     * Main interface to the MapUtil class, given a new belief, decode
     * it and update our notion of world state.
     *
     * @param belief The new belief we've received.
     */
    public void addBelief(Belief belief) {
	long startTime = System.currentTimeMillis();
	try {
	expireOldSensorReadings(SENSOR_READING_EXPIRATION_LIMIT_MS);
	
	startTime = System.currentTimeMillis();
	if (belief instanceof UAVLocation) {
	    //	    Debugger.debug("addBelief: Got UAVLocation",1,this);
	    addUAVLocation((UAVLocation)belief);
	}
	else if(belief instanceof AirSim.Machinetta.SensorReading) {
// 	    Debugger.debug("addBelief:SensorReading, ignoring", 0, this);
	}
 	else if (belief instanceof UGSSensorReading) {
// 	    Debugger.debug("addBelief: Got UGSSensorReading",1,this);
	    addUGSSensorReading((UGSSensorReading)belief); 
	}
	else if (belief instanceof VehicleBelief) {
// 	    Debugger.debug("addBelief: Got VehicleBelief",1,this);
	    addVehicleBelief((VehicleBelief)belief);
	}
	else if(belief instanceof AirSim.Machinetta.Beliefs.Location) {
// 	    Debugger.debug("addBelief: Got Location",1,this);
//	    addLocation((Location)belief);
	}
	else if(belief instanceof AirSim.Machinetta.Beliefs.AssetStateBelief) {
// 	    Debugger.debug("addBelief: Got Location",1,this);
	    addAssetState((AssetStateBelief)belief);
	}
	else if(belief instanceof AirSim.Machinetta.Beliefs.FlyZone) { 
// 	    Debugger.debug("addBelief: Got FlyZone",1,this);
	    addFlyZone((FlyZone)belief);
	}
	else if(belief instanceof AirSim.Machinetta.BasicRole) {
// 	    Debugger.debug("addBelief: Got BasicRole",1,this);
	    addBasicRole((BasicRole)belief);
	}
	else if(belief instanceof AirSim.Machinetta.NAI) {
// 	    Debugger.debug("addBelief: Got NAI",1,this);
	    addNAI((NAI)belief);
	}
	else if(belief instanceof AirSim.Machinetta.Path2D) {
// 	    Debugger.debug("addBelief: Got Path2D",1,this);
	    addPath2D((Path2D)belief);
	}
	else if(belief instanceof PlannedPath) {
// 	    Debugger.debug("addBelief: Got PlannedPath",1,this);
	    addPlannedPath((PlannedPath)belief);
	}
	else if (belief instanceof ImageData) {
// 	    Debugger.debug("addBelief: Got ImageData",1,this);
	    addImageData((ImageData)belief);
	}
	else if(belief instanceof AirSim.Machinetta.Beliefs.TMAScanResult) {
// 	    Debugger.debug("stateChange: Got TMAScanResult", 1, this);	    
	    addTMAScanResult((TMAScanResult)belief);
	}
	else if(belief instanceof AirSim.Machinetta.Beliefs.GeoLocateRequest) {
// 	    Debugger.debug("stateChange: Got GeoLocateRequest", 1, this);	    
	    addGeoLocateRequest((GeoLocateRequest)belief);
	}
	else if(belief instanceof AirSim.Machinetta.Beliefs.GeoLocateResult) {
// 	    Debugger.debug("stateChange: Got GeoLocateResult", 1, this);	    
	    addGeoLocateResult((GeoLocateResult)belief);
	}
	else if(belief instanceof Machinetta.State.BeliefType.RAPBelief) {
// 	    Debugger.debug("stateChange:RAPBelief, ignoring", 0, this);
	}
	else if(belief instanceof Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief) {
// 	    Debugger.debug("stateChange:TeamPlanBelief, ignoring", 0, this);
	}
	else if(belief instanceof Machinetta.State.BeliefType.TeamBelief.RoleAllocationBelief) {
// 	    Debugger.debug("stateChange:RoleAllocationBelief, ignoring", 0, this);
	}
	else if(belief instanceof AirSim.Machinetta.Beliefs.RSSIReading) {
// 	    Debugger.debug("stateChange:RSSIReading ignoring", 0, this);
	}
	else if(belief instanceof Machinetta.State.BeliefType.TeamBelief.Associates) {
// 	    Debugger.debug("stateChange:Associates ignoring", 0, this);
	}
	else if(belief instanceof Machinetta.State.BeliefType.TeamBelief.TeamBelief) {
// 	    Debugger.debug("stateChange:TeamBelief ignoring", 0, this);
	}
	else {
	    Debugger.debug("stateChange:Unknown class "+belief.getClass().getName()+" for new belief="+belief.toString(), 3, this);
	}
	}
	catch(Exception e) {
	    Debugger.debug("        Exception processing new belief, e"+e,4,this);
	    e.printStackTrace();
	}
	long elapsed = (System.currentTimeMillis() - startTime);
	if(elapsed > 10)
	    Debugger.debug("        processing new belief, elapsed time="+elapsed,1,this);
    }

    /**
     * Remove any data based on old sensor readings.
     *
     * @param expirationTimeLimitMs the number of milliseconds beyond
     * which we do not wish to keep a sensor reading or other world
     * state derived from that reading.  Also removes from MapDB and
     * GUI.
     */
    public void expireOldSensorReadings(long expirationTimeLimitMs) {
	long now = System.currentTimeMillis();
	if(now < (lastTimeExpiredMapObjects + RUN_SENSOR_READING_EXPIRE_EVERY_N_MS))
	    return;
	lastTimeExpiredMapObjects = now;
	VehicleData[] vehicles = fuser.getVehicleData();
	int sizeBefore = vehicles.length;
	long oldestTimeAllowed = now - SENSOR_READING_EXPIRATION_LIMIT_MS;
	for(int loopi = 0; loopi < vehicles.length; loopi++) {
	    VehicleData vd = vehicles[loopi];
	    if(null == vd)
		continue;
	    Debugger.debug("examining vd="+vd.getId()+", last update="+vd.getLastUpdateTimeMs()+", conf="+vd.getConfidence()+", oldestallowed="+oldestTimeAllowed,0,this);
	    if(vd.getLastUpdateTimeMs() < oldestTimeAllowed) {
		MapObject mo = mapDB.get(vd.getId());
		if(null != mo) {
		    Debugger.debug("expiring vd="+vd.getId()+", last update="+vd.getLastUpdateTimeMs()+", conf="+vd.getConfidence()+", oldestallowed="+oldestTimeAllowed,0,this);
		    if(mo.getProbSignificant() < EXPIRATION_CONFIDENCE_LIMIT) {
			fuser.removeVehicleData(vd);
			mapDB.remove(mo);
		    }
		}
		else {
		    Debugger.debug("couldn't find in mapdb vd="+vd.getId()+", last update="+vd.getLastUpdateTimeMs()+", conf="+vd.getConfidence()+", oldestallowed="+oldestTimeAllowed,0,this);

		}
	    }
	}
	mapDB.setDirty(true);
    }

    /**
     * Add a UAVLocation belief to our world state.
     * 
     * @param location UAV location belief
     */
    public void addUAVLocation(UAVLocation location) {
	ProxyID pid = location.id;	
	Long lastTimeL = lastLocationTime.get(pid);
	long lastTime = 0;
	if(null != lastTimeL)
	    lastTime = lastTimeL.longValue();
	if(lastTime > location.getTime()) {
	    //	    Debugger.debug("stateChange:UAVLocation:Old UAVLocation - ignoring (last location time="+lastTime+"), id='"+pid+"', time='"+location.getTime()+"', lat,lon="+location.latitude+","+location.longtitude, 1, this);
	} 
	else {
	    //	    Debugger.debug("stateChange:UAVLocation:New UAVLocation (last location time="+lastTime+"), id='"+pid+"', time='"+location.getTime()+"', lat,lon="+location.latitude+","+location.longtitude, 1, this);
	    lastLocationTime.put(pid, location.getTime());
	    double posx = location.longtitude;
	    double posy = location.latitude;
	    double heading = location.heading;

	    String moId = pid.toString();
	    MapObject mo = mapDB.get(moId);
	    if(null == mo) {
		String moName = moId;
		int unitType = UnitTypes.MUAV;
		mo = new MapObject(moId, ForceIds.BLUEFOR, moName, unitType, UnitSizes.SINGLE, posx, posy, 0, 0, 0, 0);
		mo.setEditable(false);
		mapDB.add(mo);
	    }
	    mo.setPos(posx, posy, 0);
	    mo.setOrientation(heading);
	    PositionMeters destLoc = destMap.get(moId);
	    if(null != destLoc) {
		mo.setDest(destLoc.getX(), destLoc.getY(),0);
	    }
	    PlannedPath path = unusedPathMap.get(pid);
	    if(null != path)
		addPlannedPath(path);
	    mapDB.setDirty(true);
	}
    }

    /**
     * Add a UGSSensorReading belief to our world state.
     * 
     * @param r UGS sensor reading
     */
    public void addUGSSensorReading(UGSSensorReading r) {
	String moId = "UGS"+(r.getX()/1000)+","+(r.getY()/1000);

	MapObject mo = mapDB.get(moId);
	if(null == mo) {
	    String moName = moId;
	    int unitType = UnitTypes.SENSOR;
	    mo = new MapObject(moId, ForceIds.BLUEFOR, moName, unitType, UnitSizes.SINGLE, r.getX(), r.getY(), 0, 0, 0, 0);
	    mo.setEditable(false);
	    mapDB.add(mo);
	    mapDB.setDirty(true);
	}
	mo.setPos(r.getX(), r.getY(), 0);
	// @todo: range should really be in the message.
	double ugsRange = UnattendedGroundSensor.DEFAULT_RANGE;
	if(r.isPresent())
	    mo.setFlash(true, ugsRange*2, 0.0);
	mapDB.setDirty(true);
    }
        
    /**
     * Add am ImageData belief our world state.
     * 
     * @param imageData Image belief from an EOIR.
     */
    public void addImageData(ImageData imageData) {
	Machinetta.Debugger.debug("Got image data", 1, this);
	BufferedImage img = null;
	double posx = imageData.loc.x;
	double posy = imageData.loc.y;

	try {
	    img = ImageIO.read(new ByteArrayInputStream(imageData.data));
	} catch (IOException ex) {
	    Machinetta.Debugger.debug("Image read failed: " + ex, 3, this);
	}

	VehicleData vd = fuser.addImageData(imageData, img);
	String targetName = vd.getId();

	MapObject mo = null;
	mo = mapDB.get(targetName);
	if(null != mo) {
	}
	else {
	    mo = mapDB.findWithin(posx, posy, 100.0, MapObject.TYPE_PICTURE_ORDER);
	    if(null != mo)
		mapDB.remove(mo);
	    mo = new MapObject(targetName, MapObject.TYPE_PICTURE, posx, posy, 0, 0, 0);
	    mo.setEditable(false);
	    mapDB.add(mo);
	    mapDB.setDirty(true);
	}
	Calendar cal = new GregorianCalendar();
	cal.setTimeInMillis(System.currentTimeMillis());

	String text = "Imagery at time="+cal.get(cal.HOUR_OF_DAY)+":"+cal.get(cal.MINUTE)+":"+cal.get(cal.SECOND);
	
	mo.setMouseOverText(text);
	mo.setMouseOverImage(img);
	Debugger.debug("set ImageData for MapObject='"+targetName+", mouseover text="+text, 1, this);
	mo.setPos(posx, posy, 0);
	mo.setProbSignificant(0.0);
	mo.setFlash(true);
	mapDB.setDirty(true);
    }

    /**
     * Add a TMAScanResult belief to our world state.
     * 
     * @param result the scan
     */
    public void addTMAScanResult(TMAScanResult result) {
	// A UAV has sent a scan result to someone.. 
	Debugger.debug(1, "TDOA TMAScanResult:"
		       +" isDiscriminated="+result.isDiscriminated
		       +" sensorID="+result.sensorID
		       +" readingID="+result.readingID
		       +" blockCounter="+result.blockCounter
		       +" isGeolocateData="+result.isGeolocateData
		       +" requestID="+result.requestID
		       );
	if(result.isGeolocateData) {
	    MapObject mo = mapDB.get(result.sensorID);
	    if(null != mo) {
		mo.setFlash(true);
	    }
	}
    }

    /**
     * Add a GeoLocateRequest belief to our world state.
     * 
     * @param request the geolocate request is received twice, once
     * when the plan is generated, with located field set to false,
     * and then again when the plan is finished, with located field
     * set to true.
     */
    public void addGeoLocateRequest(GeoLocateRequest request) {
	// The DetectorRI has created a geolocate plan
	Debugger.debug(1, "TDOA GeoLocateRequest:"
		       +" frequency="+request.frequency
		       +" bandwidth="+request.bandwidth
		       +" pulseWidth="+request.pulseWidth
		       +" longtitude="+request.longtitude
		       +" latitude="+request.latitude
		       +" emitterID="+request.emitterID
		       +" located="+request.located
		       +" sentToRAPAt="+request.sentToRAPAt
		       );

	// We get this twice - once when the plan is generated, with
	// located=false, and then again when the plan is finished,
	// with located=true
	String idString = "GeoLocateRequest:"+(long)request.longtitude+","+(long)request.latitude;
	if(!request.located) {
	    String moName = idString;
	    MapObject mo = new MapObject(idString, MapObject.TYPE_MAP_GRAPHIC, request.longtitude, request.latitude, 0, 3000, 0);
	    mo.setMapGraphicColor(Color.green);
	    mo.setEditable(false);
	    mapDB.add(mo);
	}
	else {
	    mapDB.remove(idString);
	}
	
    }

    /**
     * Add a GeoLocateResult belief to our world state.
     * 
     * @param result the geolocate result
     */
    public void addGeoLocateResult(GeoLocateResult result) {
	// The DetectorRI has created a geolocate plan
	Debugger.debug(1, "TDOA GeoLocateResult:"
		       +" longtitude="+result.longtitude
		       +" latitude="+result.latitude
		       +" errorElipseLong="+result.errorElipseLong
		       +" errorElipseLat="+result.errorElipseLat
		       +" orientation="+result.orientation
		       +" emitterID="+result.emitterID
		       );
	String id = "Emitter:"+((int)result.longtitude)+","+((int)result.latitude);
	MapObject mo = new MapObject(id, ForceIds.OPFOR, id, UnitTypes.EMITTER, UnitSizes.SINGLE, result.longtitude, result.latitude, 0, 0, 0, 0);
	mo.setEllipse(result.errorElipseLong,result.errorElipseLat, result.orientation);
	mo.setEditable(false);
	mapDB.add(mo);
    }


    /**
     * Add a VehicleBelief belief to our world state.
     * 
     * @param b the vehicleBelief result
     */
    public void addVehicleBelief(VehicleBelief b) {
	double posx = b.getX();
	double posy = b.getY();
	int unitType = convertType(b.getType());

	VehicleData vd = fuser.addBelief(b);
	String targetName = vd.getId();

	MapObject mo = null;
	mo = mapDB.get(targetName);
	if(null != mo) {
	    Debugger.debug("Old MapObject name='"+targetName+"' b.getType()='"+b.getType()+"'", 5, this);
	}
	else {
	    Debugger.debug("New MapObject name='"+targetName+"' b.getType()='"+b.getType()+"'", 1, this);
	    mo = new MapObject(targetName, ForceIds.OPFOR, targetName, unitType, UnitSizes.SINGLE, posx, posy, 0, 0, 0, 0);
	    mo.setEditable(false);
	    ImageIcon icon = imageryMap.get(unitType);
	    if(null != icon)
		mo.setMouseOverImage(icon.getImage());
	    mapDB.add(mo);
	    mapDB.setDirty(true);
	}
	Calendar cal = new GregorianCalendar();
	cal.setTimeInMillis(System.currentTimeMillis());

	String text = "Time="+cal.get(cal.HOUR_OF_DAY)+":"+cal.get(cal.MINUTE)+":"+cal.get(cal.SECOND)+", Type="+b.getType()+", Conf="+fmt.format(b.getConfidence());
	Debugger.debug("addVehicleBelief: setting mouse over text for "+targetName+" to "+text, 0, this);
	
	mo.setMouseOverText(text);
	mo.setProbSignificant(b.getConfidence());
	mo.setThreatToAir(false);
	mo.setThreatToGround(false);
	if((UnitTypes.CIVILIAN_TRUCK != unitType)
	   && (UnitTypes.MILITARY_TRUCK != unitType)
	   && (UnitTypes.CLUTTER != unitType)) {
	    if((UnitTypes.MECH_INFANTRY == unitType)
	       || (UnitTypes.WASM == unitType)
	       || (UnitTypes.MUAV == unitType)
	       || (UnitTypes.ARMOR == unitType))
		mo.setThreatToGround(true);
	    if((UnitTypes.AIR_FORCES == unitType)
	       || (UnitTypes.AIR_DEFENSE_ARTILLERY == unitType))
		mo.setThreatToAir(true);
	}
	//	mo.setOrientation(heading);
	mo.setPos(posx, posy, 0);
	mo.setFlash(true);
	mapDB.setDirty(true);
    }
        
    /**
     * Add an AssetStateBelief to our world state.  These are
     * typically our own assets reporting their current location to
     * us.
     * 
     * @param location the location of the asset
     */
    public void addAssetState(AirSim.Machinetta.Beliefs.AssetStateBelief assetState) {
	Long lastTimeL = lastLocationTime.get(assetState.pid);
	long lastTime = 0;
	if(null != lastTimeL)
	    lastTime = lastTimeL.longValue();
	if(lastTime > assetState.time) {
	    Debugger.debug(0,"stateChange:AssetStateBelief:Old Location - ignoring (last location time="+lastTime+"), id='"+assetState.pid+"', time='"+assetState.time+"', x,y="+assetState.xMeters+","+assetState.yMeters);
	} 
	else {
	    Debugger.debug(0,"stateChange:AssetStateBelief:New Location (last location time="+lastTime+"), id='"+assetState.pid+"', time='"+assetState.time+"', x,y="+assetState.xMeters+","+assetState.yMeters);
	    lastLocationTime.put(assetState.pid, assetState.time);
	    double posx = assetState.xMeters;
	    double posy = assetState.yMeters;
	    double heading = assetState.headingDegrees;

	    String moId = assetState.pid.toString();
	    MapObject mo = mapDB.get(moId);
	    if(null == mo) {
		String moName = moId;
		int unitType = UnitTypes.WASM;
		if((moId.startsWith("B-H")) || (moId.startsWith("H"))) {
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

    /**
     * Add a BasicRole belief to our world state.  This is typically
     * one of our assets reporting to us that it's taken on a role in a plan.
     * 
     * @param brole the role the asset is taking on.
     */
    public void addBasicRole(BasicRole brole) {
	Debugger.debug("stateChange:BasicRole", 1, this);
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

    /**
     * Add a NAI (Named Area of Interest) belief to our world state.
     * NAIs are typically the result of some inferencing process, and
     * here are represented as boxes with IDs.
     * 
     * @param nai the named area of interest
     */
    public void addNAI(NAI nai) {
	Debugger.debug("stateChange:NAI="+nai, 1, this);
	double xcenter = (nai.x1+nai.x2)/2;
	double ycenter = (nai.y1+nai.y2)/2;
	double width = nai.x1 - nai.x2;
	if(width < 0) width = -width;
	double height = nai.y1 - nai.y2;
	if(height < 0) height = -height;
	mapDB.add(new MapObject(MapObject.createKey(),ForceIds.UNKNOWN, nai.getID().toString(), 
				MapObject.TYPE_EA_CANDIDATE, xcenter, ycenter, 0.0, width, height));
	mapDB.setDirty(true);
    }
   
    /**
     * Add a Path2D belief to our world state.  These are typically
     * ground paths that our asset is planning to follow, this belief
     * class should probably be deprecated
     * 
     * @param path The path
     *
     * @see Path3D
     */
    public void addPath2D(Path2D path) {
	Debugger.debug("stateChange:Path="+(Belief)path, 1, this);
	//     public MapObject(String key, int type, double posX, double posY, double posZ, double sizeX, double sizeY)
	Point2D firstPoint = path.wps.getFirst();
	MapObject mo = new MapObject(MapObject.createKey(), MapObject.TYPE_LINE, firstPoint.getX(), firstPoint.getY(), 0, 0, 0);
	for(Point2D point: path.wps) {
	    mo.addLinePoint(point.getX(), point.getY());
	}
	mapDB.add(mo);
	mapDB.setDirty(true);
    }

    /**
     * Add a PlannedPath belief to our world state.  These are
     * typically paths that our asset is planning to follow.
     * 
     * @param plannedPath The planned path
     */
    public void addPlannedPath(PlannedPath plannedPath) {
	BeliefID ppID = plannedPath.getID();
	if(seenPaths.contains(ppID)) {
	    Debugger.debug("addPlannedPath: ignoring repeat of known planned path id="+plannedPath.getID(),0,this);
	    return;
	}

	Debugger.debug("stateChange:plannedPath="+(Belief)plannedPath, 0, this);
	Path3D path = plannedPath.path;
	ProxyID assetID = path.getAssetID();

	if(null == assetID) {
	    Debugger.debug("addPlannedPath: PlannedPath id="+plannedPath.getID()+" path3d.assetID is null, can't do anything useful to display this path - fix whatever generates this path to include the assetID",1,this);
	    return;
	}

	String assetIDString = assetID.toString();
	MapObject mo = mapDB.get(assetIDString);
	if(null == mo) { 
	    Debugger.debug("addPlannedPath: Couldn't find existing map object for assetid = "+assetIDString,1,this);
	    PlannedPath oldPath = unusedPathMap.get(assetID);
	    if(null == oldPath)
		unusedPathMap.put(assetID,plannedPath);
	    else if(oldPath.time < plannedPath.time)
		unusedPathMap.put(assetID,plannedPath);
	    return;
	}

	PlannedPath oldPath = assetPathMap.get(assetID);
	if(null != oldPath) 
	    seenPaths.remove(oldPath.getID());
	seenPaths.add(ppID);
	unusedPathMap.remove(assetID);	// In case it came through before, and was saved here.
	assetPathMap.put(assetID,plannedPath);

	Debugger.debug("addPlannedPath: Found existing map object for assetid = "+assetIDString+", new path="+path.toString() ,1,this);
	Vector3D[] waypoints = path.getWaypointsAry();
	if(waypoints.length <= 0) {
	    Debugger.debug("addPlannedPath: zero waypoints in planned path",0,this);
	    return;
	}
	Debugger.debug("addPlannedPath: "+waypoints.length+" waypoints in planned path",0,this);
	mo.clearPlannedPath();
	for(int loopi = 0; loopi < waypoints.length; loopi++) {
	    if(null != waypoints[loopi]) {
		mo.addPlannedPathPoint((float)waypoints[loopi].x, (float)waypoints[loopi].y);
	    }
	}
	if(null != plannedPath.conflicted) {
	    Debugger.debug("addPlannedPath: received plannedPath conflict notification for path owner "+assetID,0,this);
	    mo.setPlannedPathConflict(true);
	}
	else {
	    mo.setPlannedPathConflict(false);
	}
    }

    /**
     * Check if a requested FlyZone conflicts with an existing
     * FlyZone.  FlyZones are areas (usually 3D boxes) that an asset
     * (usually a UAV) has requested free access to for planning and
     * executing it's paths.  
     * 
     * @param fz the fly zone being requested
     *
     * @return true if the new FlyZone intersects an existing FlyZone,
     * false otherwise.
     */
    private boolean checkForFlyZoneConflicts(FlyZone fz) {
	Iterator<FlyZone> it = flyZoneMap.values().iterator();
	while(it.hasNext()) {
	    FlyZone oldZone = it.next();
	    if(fz.pid.equals(oldZone.pid))
		continue;
	    if(fz.intersects(oldZone)) {
		Debugger.debug("checkForFlyZoneConflicts:FlyZones intersect: old "+oldZone+" new "+fz, 1, this);
		return true;
	    }
	}
	return false;
    }

    /**
     * Add a FlyZone belief to our world state.  We keep track of
     * authorized FlyZone's for UAV so we may check new requests
     * against existing FlyZones.
     * 
     * @param fz the fly zone
     */
    public void addFlyZone(FlyZone fz) {
	Debugger.debug("addFlyZone:FlyZone request="+fz, 1, this);

	boolean conflict = checkForFlyZoneConflicts(fz);

	if(conflict) {
	    fz.approved = false;
	}
	else {
	    fz.approved = true;
	}

	FlyZone reply = new FlyZone(fz);
	InformationAgent agent = new InformationAgent(reply, reply.pid);
	Debugger.debug("addFlyZone:FlyZone request id="+fz.getID(), 1, this);
	Debugger.debug("addFlyZone:FlyZone reply id  ="+reply.getID(), 1, this);

	MACoordination.addAgent(agent);
	agent.stateChanged();	    // Let it act
	Debugger.debug("addFlyZone: Done sending reply="+fz, 1, this);

	double x = (fz.longtitude1+fz.longtitude2)/2;
	double y = (fz.latitude1+fz.latitude2)/2;
	double z = (fz.altitude1+fz.altitude2)/2;
	double width = fz.longtitude2 - fz.longtitude1;
	double length = fz.latitude2 - fz.latitude1;
	
	String flyzoneIDString = "FlyZone."+fz.pid.toString();
	MapObject mo = mapDB.get(flyzoneIDString);
	if(null == mo) {
	    String moName = "FlyZone."+fz.pid.toString()+" "+fz.altitude1+"m to "+fz.altitude2+"m";
	    mo = new MapObject(flyzoneIDString, MapObject.TYPE_MAP_GRAPHIC, x, y, z, width, length);
	    //	    mo.setName(moName);
	    mo.setEditable(false);
	    mapDB.add(mo);
	}
	else {
	    mo.setPos(x,y,z);	
	    mo.setSizeX(width);
	    mo.setSizeY(length);
	}
	if(conflict) {
	    mo.setMapGraphicColor(Color.red);
	    Debugger.debug("addFlyZone: Setting zone color to RED for "+mo.getName(),1,this);
	}
	else {
	    mo.setMapGraphicColor(Color.blue);
	    Debugger.debug("addFlyZone: Setting zone color to BLUE for "+mo.getName(),1,this);
	}

	mapDB.setDirty(true);

	flyZoneMap.put(fz.pid, fz);
    }
}

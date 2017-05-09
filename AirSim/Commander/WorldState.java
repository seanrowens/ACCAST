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
 * WorldState.java
 *
 * Created Wed Apr 12 16:19:06 EDT 2006
 *
 */

package AirSim.Commander;

import AirSim.Environment.Assets.UnattendedGroundSensor;
import AirSim.Environment.Assets.Asset.Types;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Vector3D;
import AirSim.Machinetta.BasicRole;
import AirSim.SARSensorReading;
import AirSim.Machinetta.Beliefs.ImageData;
import AirSim.Machinetta.Beliefs.UAVLocation;
import AirSim.Machinetta.Beliefs.UGSSensorReading;
import AirSim.Machinetta.Beliefs.VehicleBelief;
import AirSim.Machinetta.SensorReading;
import AirSim.Machinetta.NAI;
import AirSim.Machinetta.Path3D;
import AirSim.Machinetta.PlannedPath;
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
import AirSim.Machinetta.Beliefs.AssetStateBelief;
import AirSim.Machinetta.NAI;
import AirSim.Machinetta.NAIList;
import AirSim.Machinetta.Path2D;
import AirSim.Machinetta.Point2D;
import AirSim.Machinetta.SimTime;
import Machinetta.AA.SimpleAA;
import Machinetta.Coordination.MAC.PlanAgent;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.TeamBelief.Constraints.DirectedInformationRequirement;
import Machinetta.State.BeliefType.TeamBelief.Constraints.GeneratedInformationRequirement;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import java.io.IOException;

import Util.*;

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
import java.util.Observable;
import java.util.Observer;

/**
 *
 * @deprecated
 * @author owens
 */
public class WorldState extends Observable {
    private final static DecimalFormat fmt = new DecimalFormat("0.000");
    private final static double FUSION_DISTANCE=500;

    private final static double EXPIRATION_CONFIDENCE_LIMIT = .6;
    private final static long RUN_SENSOR_READING_EXPIRE_EVERY_N_MS = 1000;    
    private final static long SENSOR_READING_EXPIRATION_LIMIT_MS = 1000 * 20;

    private String imageDir=null;

    // Things in our 'world state' 
    //
    // Fuser - takes care of sensor readings and fusing them and
    // expiring them
    //
    // uav/other vehicles
    //		location reports
    //		heading
    // 		trace (i.e. where has it been)
    //		planned paths
    //
    // UGS sensor readings
    // 
    // camera pictures taken
    //
    // Roles?  and their parameters?
    // 
    // NAIs
    //
    // path2d/plannedPath - how are these different?  I'm not sure, it
    // might be that path2d is used just to represent "a line".  Need
    // to figure out what is generating those.
    //

    private long lastTimeExpiredMapObjects = System.currentTimeMillis();
    private Fuser fuser = new Fuser();

    private HashMap<String, VehicleData> entityData= new HashMap<String, VehicleData>();


    HashMap<String,Hashtable<Asset.Types, Double>> combinedBeliefs = new HashMap<String,Hashtable<Asset.Types, Double>>();
    HashMap sentStrikes = new HashMap();

    private HashMap<ProxyID, Long> lastLocationTime = new HashMap<ProxyID, Long>();
    private HashMap<String, PositionMeters> destMap = new HashMap<String, PositionMeters>();
    private HashMap<String, UGSSensorReading> ugsMap = new HashMap<String, UGSSensorReading>();
    private HashMap<Integer, ImageIcon> imageryMap = new HashMap<Integer, ImageIcon>();

    private HashMap<ProxyID,PlannedPath> assetPathMap = new HashMap<ProxyID,PlannedPath>();
    private HashMap<ProxyID,PlannedPath> unusedPathMap = new HashMap<ProxyID,PlannedPath>();
    private HashSet<BeliefID> seenPaths = new HashSet<BeliefID>();

    private void loadImagery() {
	String baseDir=imageDir+"/original";
	imageryMap.put(UnitTypes.AIR_DEFENSE_ARTILLERY, new ImageIcon(baseDir+"/SA-9 GASKIN STRELA 1.gif"));
	imageryMap.put(UnitTypes.ARMOR, new ImageIcon(baseDir+"/M1A2 Abrams sand.gif"));
	imageryMap.put(UnitTypes.LIGHT_INFANTRY, new ImageIcon(baseDir+"/HUMVEE CAMO.gif"));
	imageryMap.put(UnitTypes.CIVILIAN_TRUCK, new ImageIcon(baseDir+"/white_pickup.gif"));
    }

    public WorldState(String imageDir) {
	this.imageDir = imageDir;
	loadImagery();
    }

    private void ignoreBelief(Belief belief, String beliefTypeName) {
	Debugger.debug("addBelief:"+beliefTypeName+", ignoring", 0, this);
    }

    public void addBelief(Belief belief) {
	long startTime = System.currentTimeMillis();
	try {
	    long now = System.currentTimeMillis();
	    if(now >= (lastTimeExpiredMapObjects + RUN_SENSOR_READING_EXPIRE_EVERY_N_MS)) {
		lastTimeExpiredMapObjects = System.currentTimeMillis();
		fuser.expireOldSensorReadings(SENSOR_READING_EXPIRATION_LIMIT_MS,EXPIRATION_CONFIDENCE_LIMIT);
		// NOTE: When we change a single vehicleData instance
		// (or create a new one...)  then we
		// notifyObservers(vd), but when we expire or remove
		// vehicleData instances then we
		// notifyObservers(worldState).
		notifyObservers(this);
	    }
	
	    startTime = System.currentTimeMillis();
	    if (belief instanceof UAVLocation)
		addUAVLocation((UAVLocation)belief);
	    else if(belief instanceof AirSim.Machinetta.SensorReading)
		ignoreBelief(belief, "SensorReading");
	    else if (belief instanceof UGSSensorReading)
		addUGSSensorReading((UGSSensorReading)belief);
	    else if (belief instanceof VehicleBelief)
		addVehicleBelief((VehicleBelief)belief);
 	    else if(belief instanceof AirSim.Machinetta.Beliefs.Location)
		ignoreBelief(belief,"Location");
	    else if(belief instanceof AirSim.Machinetta.Beliefs.AssetStateBelief)
		addAssetStateBelief((AssetStateBelief)belief);
	    else if(belief instanceof AirSim.Machinetta.BasicRole) 
		addBasicRole((BasicRole)belief);
	    else if(belief instanceof AirSim.Machinetta.NAI)
		addNAI((NAI)belief);
	    else if(belief instanceof AirSim.Machinetta.Path2D) 
		addPath2D((Path2D)belief);
	    else if(belief instanceof PlannedPath)
		addPlannedPath((PlannedPath)belief);
	    else if (belief instanceof ImageData) 
		addImageData((ImageData)belief);
	    else if(belief instanceof Machinetta.State.BeliefType.RAPBelief)
		ignoreBelief(belief,"RAPBelief");
	    else if(belief instanceof Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief)
		ignoreBelief(belief,"TeamPlanBelief");
	    else if(belief instanceof Machinetta.State.BeliefType.TeamBelief.RoleAllocationBelief)
		ignoreBelief(belief,"RoleAllocationBelief");
	    else if(belief instanceof AirSim.Machinetta.Beliefs.RSSIReading)
		ignoreBelief(belief,"RSSIReading");
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

    public void addUAVLocation(UAVLocation location) {
	ProxyID pid = location.id;	

	String id = pid.toString();
	VehicleData vd = fuser.getVehicleData(id);

	if(null == vd) {
	    vd = new VehicleData(id, Asset.Types.SMALL_UAV, location.getTime(), location.longtitude, location.latitude, location.altitude, location.heading, location.groundSpeed);
	    fuser.addNewVehicle(vd);
	}
	else {
	    if(vd.getLastUpdateTimeMs() > location.getTime())
		return;
	    double posx = location.longtitude;
	    double posy = location.latitude;
	    double posz = location.altitude;
	    vd.setLocation(new Vector3D(posx, posy, posz), location.getTime());
	    vd.setHeading(location.heading);
	    vd.setSpeed(location.groundSpeed);
	}
	notifyObservers(vd);
    }

    public void addUGSSensorReading(UGSSensorReading r) {
	String id = "UGS"+(r.getX()/1000)+","+(r.getY()/1000);
	
	VehicleData vd = fuser.getVehicleData(id);
	if(null == vd) {
	    vd = new VehicleData(id, Asset.Types.UGS, r.getTime(), r.getX(), r.getY(), 0, 0, 0);
	    fuser.addNewVehicle(vd);
	}
	else {
	    vd.setLocation(new Vector3D(r.getX(), r.getY(), 0), r.getTime());
	}
	// @todo: range should really be in the message.
	vd.setUgsRange(UnattendedGroundSensor.DEFAULT_RANGE);
	vd.setUgsPresent(r.isPresent());
	notifyObservers(vd);
    }
        
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
	vd.setLastUpdateTimeMs(SimTime.getEstimatedTime());
	notifyObservers(vd);
    }

    public void addVehicleBelief(VehicleBelief b) {
	double posx = b.getX();
	double posy = b.getY();
	VehicleData vd = fuser.addBelief(b);
	vd.setLastUpdateTimeMs(SimTime.getEstimatedTime());
	notifyObservers(vd);
    }
        
    public void addAssetStateBelief(AirSim.Machinetta.Beliefs.AssetStateBelief assetState) {
	ProxyID pid = assetState.pid;	
	String id = pid.toString();
	VehicleData vd = fuser.getVehicleData(id);
	if(null == vd) {
	    vd = new VehicleData(id, Asset.Types.UNKNOWN, assetState.time, assetState.xMeters, assetState.yMeters, 0.0, assetState.headingDegrees, 0.0);
	    fuser.addNewVehicle(vd);
	    notifyObservers(this);
	    return;
	}

	if(vd.getLastUpdateTimeMs() > assetState.time)
	    return;

	Debugger.debug("stateChange:AssetState:New AssetState (last assetState time="+vd.getLastUpdateTimeMs()+"), id='"+id+"', time='"+assetState.time+"', x,y="+assetState.xMeters+","+assetState.yMeters, 0, this);
	vd.setLocation(new Vector3D(assetState.xMeters, assetState.yMeters, 0.0),assetState.time);
	vd.setHeading(assetState.headingDegrees);
	notifyObservers(vd);
    }

    public void addBasicRole(BasicRole brole) {
	Debugger.debug("stateChange:BasicRole", 1, this);
	RAPBelief responsible = brole.getResponsible();
	PositionMeters destLoc = null;
	String taskType = "";
	if(null != responsible) {
	    String id = responsible.getID().toString();
	    VehicleData vd = fuser.getVehicleData(id);
	    if(null == vd) {
		vd = new VehicleData(id, Asset.Types.UNKNOWN, SimTime.getEstimatedTime(), -10000,-10000,-10000, 0.0, 0.0);
		fuser.addNewVehicle(vd);
		notifyObservers(this);
	    }
	    if(TaskType.patrol == brole.getType()) {
		NAI nai = (NAI)brole.params.get("NAI");
		destLoc = new PositionMeters((nai.x1+nai.x2)/2, (nai.y1+nai.y2)/2,0);
		vd.setDestination(destLoc);
		notifyObservers(vd);
		Debugger.debug("stateChange: BasicRole 'patrol' at "+destLoc.getX()+","+destLoc.getY()+" assigned to "+id, 1, this);
	    }
	    else if(TaskType.attack == brole.getType()) {
		PositionMeters loc = (PositionMeters)brole.params.get("Location");
		destLoc = loc;
		vd.setDestination(destLoc);
		notifyObservers(vd);
		Debugger.debug("stateChange: BasicRole 'attack' at "+destLoc.getX()+","+destLoc.getY()+" assigned to "+id, 1, this);
	    }
	    else {
		Debugger.debug("stateChange: BasicRole of unfamiliar type: "+brole.getType(), 1, this);
	    }
	}
    }

    public void addNAI(NAI nai) {
	Debugger.debug("stateChange:NAI="+nai, 1, this);
	double xcenter = (nai.x1+nai.x2)/2;
	double ycenter = (nai.y1+nai.y2)/2;
	double width = nai.x1 - nai.x2;
	if(width < 0) width = -width;
	double height = nai.y1 - nai.y2;
	if(height < 0) height = -height;
	// @TODO: add something to vehicleData to represent NAI.  Add
	// code here to add vehicleData NAI.  Or... maybe just use
	// some kind of hashtable of NAIs in this class?
// 	mapDB.add(new MapObject(MapObject.createKey(),ForceIds.UNKNOWN, nai.getID().toString(), 
// 				MapObject.TYPE_EA_CANDIDATE, xcenter, ycenter, 0.0, width, height));
// 	mapDB.setDirty(true);
    }

    public void addPath2D(Path2D path) {
	Debugger.debug("stateChange:Path="+(Belief)path, 1, this);
	//     public MapObject(String key, int type, double posX, double posY, double posZ, double sizeX, double sizeY)
	//	Point2D firstPoint = path.wps.getFirst();
// 	MapObject mo = new MapObject(MapObject.createKey(), MapObject.TYPE_LINE, firstPoint.getX(), firstPoint.getY(), 0, 0, 0);
// 	for(Point2D point: path.wps) {
// 	    mo.addLinePoint(point.getX(), point.getY());
// 	}
// 	mapDB.add(mo);
// 	mapDB.setDirty(true);
    }

    public void addPlannedPath(PlannedPath plannedPath) {
	ProxyID pid = plannedPath.owner;
	String id = pid.toString();
	VehicleData vd = fuser.getVehicleData(id);
	if(null == vd) {
	    vd = new VehicleData(id, Asset.Types.UNKNOWN, plannedPath.time, -1000000, -1000000, 0.0, 0.0, 0.0);
	    fuser.addNewVehicle(vd);
	}
	vd.setPath(plannedPath);
	Debugger.debug("stateChange:plannedPath="+(Belief)plannedPath, 1, this);
    }

}

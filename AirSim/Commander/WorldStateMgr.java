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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package AirSim.Commander;

import AirSim.Environment.Area;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.ForceId;
import AirSim.Environment.Assets.State;
import AirSim.Environment.Assets.Asset.Types;
import AirSim.Environment.Assets.Munition;
import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Environment.Env;
import AirSim.Environment.Vector3D;
import AirSim.Machinetta.BasicRole;
import AirSim.Machinetta.SensorReading;
import AirSim.Machinetta.SimTime;
import AirSim.Machinetta.Beliefs.GeoLocateRequest;
import AirSim.Machinetta.Beliefs.GeoLocateResult;
import AirSim.Machinetta.Beliefs.ImageData;
import AirSim.Machinetta.Beliefs.AssetStateBelief;
import AirSim.Machinetta.Beliefs.TMAScanResult;
import AirSim.Machinetta.Beliefs.UAVLocation;
import AirSim.Machinetta.Beliefs.UGSSensorReading;
import AirSim.Machinetta.Beliefs.VehicleBelief;
import AirSim.Machinetta.MiniWorldState;
import AirSim.Machinetta.NAI;
import AirSim.Machinetta.Path3D;
import AirSim.Machinetta.PlannedPath;
import Gui.*;
import Util.*;
import Machinetta.Coordination.MAC.InformationAgent;
import Machinetta.Coordination.MACoordination;
import Machinetta.Coordination.MAC.PlanAgent;
import Machinetta.Debugger;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.RAPBelief;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanTemplate;
import Machinetta.State.ProxyState;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.HashMap;
import java.util.Hashtable;
import javax.imageio.ImageIO;


/**
 * This class is responsible for keeping track of our 'world state',
 * i.e. everything we know about our guys (location, state, etc),
 * based on them reporting their navigationdata, and opfor guys, based
 * on our guys reporting about them opfor.
 *
 * We need to track;
 *
 * 1) Everything about our own Assets states
 * 
 * 2) Everything about plans we generate?  Maybe not, maybe that will
 * go in SimUserCoordinator.
 *
 * 3) Everything involving sensor readings from our guys, i.e. the
 * opfor units.
 *
 *
 * As of now, everything seems to now be using NavigationDataAP and
 * AssetStateBelief, so we can ignore old stuff (Location, UAVLocation
 * - even though they are still being generated in some places)
 * 
 * StateData seems to have fields for just about everything and
 * getters and setters for same.
 *
 * I created a SimUserFuser, basically a copy of the original Fuser
 * but using StateData and slimmed down a bit.  It ONLY does fusing.
 * That's it.  WorldStateMgr uses SimUserFuser to do fusing and
 * otherwise keeps track of everything.
 *
 *
 * @author owens
 */
public class WorldStateMgr {

    public final static double MAX_FUSION_DISTANCE=2500;
    public final static double MAX_FUSION_DISTANCE_SQD=MAX_FUSION_DISTANCE*MAX_FUSION_DISTANCE;
    public final static double MIN_FUSION_THRESHOLD=0.8;
    private final static double ROUNDING_FACTOR = 500;

    private long lastExpirationTime=0;

    public SimUserSubTeams sust = new  SimUserSubTeams();
    private SimUserFuser suf = new SimUserFuser();

    public StateData get(ProxyID pid) { return suf.get(pid);}

    // This mapDB/mapGUI is REALLY only meant for TESTING purposes -
    // i.e. by visualizing the worldState onto the map the programmer
    // can see that it is working properly.
    private WSMObserver observer=null;

    private ProxyState state;
    /**
     * Creates a instance of WorldStateMgr      
     */
    public WorldStateMgr() {
	state = new ProxyState();
    }

    public WorldStateMgr(WSMObserver observer) {
	this.observer = observer;
	state = new ProxyState();
    }

    public StateData getState(ProxyID pid) {
	return suf.get(pid);
    }

    public StateData[] searchStates(double confidenceRequired, Asset.Types type) {
	StateData[] stateData = suf.getStateData();
	ArrayList<StateData> foundList= new ArrayList<StateData>();
	for(int loopi = 0; loopi < stateData.length; loopi++) {
	    StateData sd = stateData[loopi];

//  	    if(Asset.Types.INFANTRY != type) {
// 		Debugger.debug(1,"searchStates: not Infantry, ignoring sd="+sd);
// 		continue;
// 	    }

	    if(sd.getForceId() == ForceId.BLUEFOR) {
//		if(Asset.Types.SA9 == type) Debugger.debug(1,"searchStates: forceId BlueFor, ignoring sd="+sd);
		continue;
	    }
// 	    if(sd.getForceId() == ForceId.UNKNOWN)
// 		continue;
	    if(sd.getState() == State.DESTROYED) {
//		if(Asset.Types.SA9 == type) Debugger.debug(1,"searchStates: state DESTROYED, ignoring sd="+sd);
		continue;
	    }
	    if(type != sd.getType()) {
//		if(Asset.Types.SA9 == type) Debugger.debug(1,"searchStates: type "+sd.getType()+" wrong, ignoring sd="+sd);
		continue;
	    }
	    if(sd.getConfidence() < confidenceRequired) {
//		Debugger.debug(1,"searchStates: need type "+type+" confidence "+confidenceRequired+", rejecting sd="+sd);
		continue;
	    }
//	    Debugger.debug(1,"searchStates: Accepting need type "+type+" confidence "+confidenceRequired+", sd="+sd);
	    foundList.add(sd);
	}
	return foundList.toArray(new StateData[0]);
    }

    // check if anyone in subteam is still alive.
    public boolean someSubTeamMemberAlive(ArrayList<NamedProxyID> subteam) {
	for(ProxyID pid: subteam) {
	    StateData sd = suf.get(pid);
	    if(!sd.checkAssumedDead())
		return false;
	}
	return true;
    }


    private void ignoreBelief(Belief belief, String beliefTypeName) {
	Debugger.debug(0,"addBelief:"+beliefTypeName+", ignoring");
    }
    
    /**
     * Main interface to the WorldStateMgr class, given a new belief, decode
     * it and update our notion of world state.
     *
     * @param belief The new belief we've received.
     */
    public void addBelief(Belief belief) {
	long startTime=0;
        try {

	    long now = SimTime.getEstimatedTime();
	    long nextExpiration = lastExpirationTime + SimConfiguration.RUN_SENSOR_READING_EXPIRE_EVERY_N_MS;
	    if(now > nextExpiration) {
		lastExpirationTime = now;
		ArrayList<StateData> removed = 
		    suf.expireOld(now, SimConfiguration.SENSOR_READING_EXPIRATION_LIMIT_MS,
				  SimConfiguration.EXPIRATION_CONFIDENCE_LIMIT);
		if(null != observer) observer.remove(removed);	// testing
	    }

	    if(null == belief) 
		return;

            startTime = System.currentTimeMillis();
            if (belief instanceof UAVLocation) {
		ignoreBelief(belief,"UAVLocation");
            } else if (belief instanceof AirSim.Machinetta.SensorReading) {
		addSensorReading((SensorReading)belief);
            } else if (belief instanceof UGSSensorReading) {
                addUGSSensorReading((UGSSensorReading) belief);
            } else if (belief instanceof VehicleBelief) {
                addVehicleBelief((VehicleBelief) belief);
             } else if (belief instanceof AirSim.Machinetta.Beliefs.Location) {
		ignoreBelief(belief,"Location");
            } else if (belief instanceof AirSim.Machinetta.Beliefs.AssetStateBelief) {
                addAssetStateBelief((AssetStateBelief) belief);
            } else if (belief instanceof AirSim.Machinetta.BasicRole) {
                addBasicRole((BasicRole) belief);
            } else if (belief instanceof AirSim.Machinetta.NAI) {
                ignoreBelief(belief,"NAI");
            } else if (belief instanceof AirSim.Machinetta.Path2D) {
                ignoreBelief(belief,"Path2D");
            } else if (belief instanceof PlannedPath) {
                addPlannedPath((PlannedPath) belief);
            } else if (belief instanceof ImageData) {
                addImageData((ImageData) belief);
            } else if (belief instanceof AirSim.Machinetta.Beliefs.TMAScanResult) {
		ignoreBelief(belief,"TMAScanResult");
            } else if (belief instanceof AirSim.Machinetta.Beliefs.GeoLocateRequest) {
                ignoreBelief(belief,"GeoLocateRequest");
            } else if (belief instanceof AirSim.Machinetta.Beliefs.GeoLocateResult) {
		ignoreBelief(belief,"GeoLocateResult");
            } else if (belief instanceof Machinetta.State.BeliefType.RAPBelief) {
		ignoreBelief(belief,"RAPBelief");
            } else if (belief instanceof Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief) {
		ignoreBelief(belief,"TeamPlanBelief");
            } else if (belief instanceof Machinetta.State.BeliefType.TeamBelief.RoleAllocationBelief) {
		ignoreBelief(belief,"RoleAllocationBelief");
            } else if (belief instanceof AirSim.Machinetta.Beliefs.RSSIReading) {
		ignoreBelief(belief,"RSSIReading");
            } else if (belief instanceof Machinetta.State.BeliefType.TeamBelief.Associates) {
		ignoreBelief(belief,"Associates");
            } else if (belief instanceof Machinetta.State.BeliefType.TeamBelief.TeamBelief) {
		ignoreBelief(belief,"TeamBelief");
            } else {
                Debugger.debug(3,"addBelief:Unknown class " + belief.getClass().getName() + " for new belief=" + belief.toString());
            }
        } catch (Exception e) {
            Debugger.debug(4,"        Exception processing new belief, e = " + e);
            e.printStackTrace();
            Debugger.debug(4,"        Ignoring.");
        }
        long elapsed = (System.currentTimeMillis() - startTime);
        if (elapsed > 10) {
            Debugger.debug(0,"        processing new belief, elapsed time=" + elapsed);
        }     
    }
     
    /**
     * Add a SensorReading belief to our world state.  These are typically
     * ground paths that our asset is planning to follow, this belief
     * class should probably be deprecated
     * 
     * @param path The path
     *
     * @see Path3D
     */
    public void addSensorReading(SensorReading sr) {
        
	Vector3D location = new Vector3D(sr.loc.x,sr.loc.y,sr.loc.z);
	Vector3D heading = new Vector3D(1,0,0);
	heading.setXYHeading(sr.heading);
	heading.normalize();
	Asset.Types type = sr.getMostLikely();
	double confidence = sr.SARProbs.get(type);
	Debugger.debug(1,"addSensorReading: sr="+sr+" from proxy "+sr.sensor+" confidence "+confidence );

	StateData sd = suf.findStateData(sr.time,location,heading,type,confidence);
	if(null == sd) {
	    sd = new StateData();
	    suf.addStateData(sd);	// If we have a proxyid, set that before adding to suf
	}
	else {
	    double dist = sd.getLocation().toVector(location).length();
	    double timeElapsed = sr.time - sd.getLastUpdateTimeMs();
	    sd.setSpeedMetersPerSecond((dist/timeElapsed)*1000);
	}
	sd.beliefs.add(sr);
	sd.trace.add(location);

	sd.setLastUpdateTimeMs(sr.time);
	if(confidence >= sd.getConfidence()) {
	    sd.setType(type);
	    sd.setConfidence(confidence);
	}
	sd.setLocation(location);
	sd.setBothHeadings(sr.heading, heading);
	if(sr.state != State.UNKNOWN)
	    sd.setState(sr.state);
	if(sr.forceId != ForceId.UNKNOWN)
	    sd.setForceId(sr.forceId);


	if((sd.getType() == Types.SA9)
	   && (sd.getConfidence() > .5)
	   && (sd.getForceId() != ForceId.BLUEFOR)
	   && (sd.getState() != State.DESTROYED)
	   ) {
	    Debugger.debug(1,"addSensorReading: Likely SA9 target from vb="+sr+" sd = "+sd);
	}

	if((sd.getType() == Types.INFANTRY)
	   && (sd.getConfidence() > .5)
	   && (sd.getForceId() != ForceId.BLUEFOR)
	   && (sd.getState() != State.DESTROYED)
	   ) {
	    Debugger.debug(1,"addSensorReading: Likely INFANTRY target from vb="+sr+" sd = "+sd);
	}

	if(null != observer) observer.update(sd);	// testing
    }

    /**
     * Add a PlannedPath belief to our world state.  These are
     * typically paths that our asset is planning to follow.
     * 
     * @param plannedPath The planned path
     */
    public void addPlannedPath(PlannedPath plannedPath) {
	Debugger.debug(0,"addPlannedPath: received plannedPath for "+plannedPath.owner);
	StateData sd = suf.get(plannedPath.owner);
	if(null == sd) {
	    sd = new StateData();
	    sd.setPid(plannedPath.owner);
	    suf.addStateData(sd);	// If we have a proxyid, set that before adding to suf
	}
	sd.plannedPath = plannedPath;
      
	// Note, there is a time on the plannedPath but it is the time
	// the proxy expects to start executing the planned path, not
	// the current time.

        if(null != plannedPath.conflicted) {
	    Debugger.debug(0,"addPlannedPath: received plannedPath conflict notification for path owner "+plannedPath.owner);
	    sd.setPlannedPathConflict(true);
	}
	else {
	    sd.setPlannedPathConflict(false);
	}
	if(null != observer) observer.update(sd);	// testing
    }

    /**
     * Add an AssetStateBelief to our world state.  These are
     * typically our own assets reporting their current location and
     * state to us.
     * 
     * @param location the location of the asset
     */
    public void addAssetStateBelief(AirSim.Machinetta.Beliefs.AssetStateBelief as) {
	SimTime.updateTime(as.time);
//	Debugger.debug(1,"addAssetStateBelief: belief="+as);
	
	StateData sd = suf.get(as.pid);
	if(null == sd) {
	    // See if maybe this is a ASB matches an earlier sensor
	    // report about one of our guys.
	    Vector3D location = new Vector3D(as.xMeters,as.yMeters,as.zMeters);
	    Vector3D heading = new Vector3D(1,0,0);
	    heading.setXYHeading(as.headingDegrees);
	    heading.normalize();
	    sd = suf.findStateData(as.time,location,heading,as.type,1.0);
	    if(null == sd) {
		sd = new StateData();
		sd.setPid(as.pid);
		suf.addStateData(sd);	// If we have a proxyid, set that before adding to suf
	    }
	}
	sd.setLastReportedTimeMs(as.time);
	sd.setType(as.type);
	sd.setConfidence(1.0);
	sd.setLocation(new Vector3D(as.xMeters,as.yMeters,as.zMeters));
	//	Debugger.debug(1,"addAssetStateBelief: Location for "+sd.getPid()+" set to "+sd.getLocation());
	sd.setHeadingDegrees(as.headingDegrees);
	sd.setSpeedMetersPerSecond(as.groundSpeed);
	sd.isMounted = true;
	sd.armor = as.armor;
	sd.damage = as.damage;
	if(as.state != State.UNKNOWN)
	    sd.state = as.state;
	sd.forceId = ForceId.BLUEFOR;	// perhaps this should be a field in AssetStateBelief?
	sd.beliefs.add(as);
	sd.trace.add(sd.getLocation());
	sd.setLastUpdateTimeMs(as.time);

	if(null != observer) observer.update(sd);	// testing
    }

    /**
     * Add a BasicRole belief to our world state.  This is typically
     * one of our assets reporting to us that it's taken on a role in a plan.
     * 
     * @param brole the role the asset is taking on.
     */
    public void addBasicRole(BasicRole brole) {
	Debugger.debug(1,"addBasicRole: BasicRole="+brole);
	RAPBelief responsible = brole.getResponsible();
	if(null == responsible)
	    return;

	ProxyID pid = responsible.getProxyID();
	StateData sd = suf.get(pid);
	if(null == sd) {
	    sd = new StateData();
	    sd.setPid(pid);
	    suf.addStateData(sd);
	}

	sd.basicRole = brole;

	Vector3D destLoc = null;
	String taskType = "";
	if(null != responsible) {
	    if(TaskType.patrol == brole.getType()) {
		Area patrolArea = (Area)brole.params.get("Area");
		destLoc = new Vector3D((patrolArea.x1+patrolArea.x2)/2, (patrolArea.y1+patrolArea.y2)/2,0);
		taskType = "patrol";		    
		sd.destLocation = destLoc;
		Debugger.debug(1,"addBasicRole: BasicRole "+taskType+" at "+destLoc+" assigned to "+pid+", added NAI to mapdb.");
	    }
	    else if(TaskType.attack == brole.getType()) {
		sd.destLocation = (Vector3D)brole.params.get("Location");
		taskType = "attack";
		Debugger.debug(1,"addBasicRole: BasicRole "+taskType+" at "+sd.destLocation+" assigned to "+pid);
	    }
	    else {
		Debugger.debug(1,"addBasicRole: BasicRole of unfamiliar type: "+brole.getType());
	    }
	}

	if(null != observer) observer.update(sd);	// testing
    }

    /**
     * Add a VehicleBelief belief to our world state.
     * 
     * @param b the vehicleBelief result
     */
    public void addVehicleBelief(VehicleBelief b) {
	if(b.getKey() != null) {
	    return;
	}

	Vector3D location = new Vector3D(b.getX(),b.getY(),b.getZ());
	Asset.Types type = b.getType();
	double confidence = b.getConfidence();
//	Debugger.debug(1,"addVehicleBelief: vb="+b+" from proxy "+b.getSensor()+" confidence "+confidence );
	Vector3D heading = new Vector3D(1,0,0);
	heading.setXYHeading(b.getHeading());
	heading.normalize();

	StateData sd = suf.findStateData(b.getTimeMs(),location,heading,type,confidence);
	if(null == sd) {
	    sd = new StateData();
	    suf.addStateData(sd);	// If we have a proxyid, set that before adding to suf
	    Debugger.debug(1,"addVehicleBelief: vb="+b+" from proxy "+b.getSensor()+" confidence "+confidence +" to NEW stateData id " + sd.getKey());
	}
	else {
	    double dist = sd.getLocation().toVector(location).length();
	    double timeElapsed = b.getTimeMs() - sd.getLastUpdateTimeMs();
	    sd.setSpeedMetersPerSecond((dist/timeElapsed)*1000);
	    Debugger.debug(0,"addVehicleBelief: vb="+b+" from proxy "+b.getSensor()+" confidence "+confidence +" to OLD stateData id "+sd.getKey());
	}

	sd.setLocation(location);
	sd.setBothHeadings(b.getHeading(), heading);
	sd.beliefs.add(b);
	sd.trace.add(location);
	if(confidence >= sd.getConfidence()) {
	    sd.setType(type);
	    sd.setConfidence(confidence);
	}
	sd.setLastUpdateTimeMs(b.getTimeMs());
	if(b.getState() != State.UNKNOWN)
	    sd.setState(b.getState());
	if(b.getForceId() != ForceId.UNKNOWN)
	    sd.setForceId(b.getForceId());

	b.setKey(sd.getKey());

	if((sd.getType() == Types.SA9)
	   && (sd.getConfidence() > .5)
	   && (sd.getForceId() != ForceId.BLUEFOR)
	   && (sd.getState() != State.DESTROYED)
	   ) {
	    Debugger.debug(1,"addVehicleBelief: Likely SA9 target from vb="+b+" sd = "+sd);
	}

	if((sd.getType() == Types.INFANTRY)
	   && (sd.getConfidence() > .5)
	   && (sd.getForceId() != ForceId.BLUEFOR)
	   && (sd.getState() != State.DESTROYED)
	   ) {
	    Debugger.debug(1,"addVehicleBelief: Likely INFANTRY target from vb="+b+" sd = "+sd);
	}

	if(null != observer) observer.update(sd);	// testing
    }

    // @TODO: What about IM's that act kinda like UGS but aren't
    // (hence type would be different) and/or can maybe report range? 
    /**
     * Add a UGSSensorReading belief to our world state.
     * 
     * @param r UGS sensor reading
     */
    public void addUGSSensorReading(UGSSensorReading r) {
	ProxyID pid = r.getPid();
	StateData sd = suf.get(pid);
	if(null == sd) {
	    sd = new StateData();
	    sd.setPid(pid);
	    suf.addStateData(sd);
	}
	sd.setLocation(new Vector3D(r.getX(), r.getY(), 0));
	sd.ugsPresent = r.isPresent();
	sd.type = Asset.Types.UGS;
	sd.forceId = ForceId.BLUEFOR;

	if(null != observer) observer.update(sd);	// testing
    }
        
    /**
     * Add am ImageData belief our world state.
     * 
     * @param imageData Image belief from an EOIR.
     */
    public void addImageData(ImageData imageData) {
	Machinetta.Debugger.debug(1,"addImageData: Got image data");
	BufferedImage img = null;
	double posx = imageData.loc.x;
	double posy = imageData.loc.y;
        double posz = imageData.loc.z;
        
	try {
	    img = ImageIO.read(new ByteArrayInputStream(imageData.data));
	} catch (IOException ex) {
	    Machinetta.Debugger.debug(3,"Image read failed: " + ex);
	}

	StateData sd = new StateData();
	suf.addStateData(sd);
	sd.setLocation(imageData.loc);
	sd.img = img;
	sd.setLastUpdateTimeMs(imageData.time);
	sd.beliefs.add(imageData);

	if(null != observer) observer.update(sd);	// testing
    }

    public static int convertType(Types type) {
	if(Types.HMMMV == type) {
	    return UnitTypes.MECH_INFANTRY;
	}
	else if(Types.WASM == type) {
	    return UnitTypes.WASM;
	}
	else if(Types.SMALL_UAV == type) {
	    return UnitTypes.MUAV;
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
}

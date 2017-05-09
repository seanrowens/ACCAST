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
 * SmallUAV.java
 *
 * Created on February 9, 2006, 4:25 PM
 *
 */

package AirSim.Environment.Assets;

import AirSim.Environment.Assets.Sensors.DirRFSensorReading;
import AirSim.Environment.Assets.Sensors.EOIRSensor;
import AirSim.Environment.Assets.Sensors.EOIRSensorReading;
import AirSim.Environment.Assets.Sensors.RSSISensorReading;
import AirSim.Environment.Assets.Sensors.Sensor;
import AirSim.Environment.Assets.Sensors.TDOASensor;
import AirSim.Environment.Assets.Sensors.TMASensorReading;
import AirSim.Environment.Assets.Tasks.Move;
import AirSim.Environment.Assets.Tasks.MoveToFAARP;
import AirSim.Environment.Assets.Tasks.AskRefuel;
import AirSim.Environment.Assets.Tasks.Land;
import AirSim.Environment.Assets.Tasks.Launch;
import AirSim.Environment.Assets.Tasks.Circle;
import AirSim.Environment.Env;
import AirSim.Environment.Vector3D;
import AirSim.Environment.Waypoint;
import AirSim.Machinetta.Messages.CameraCommandPA;
import AirSim.Machinetta.Messages.EOIRSensorReadingAP;
import AirSim.Machinetta.Messages.GeoLocateDataAP;
import AirSim.Machinetta.Messages.NavigationDataAP;
import AirSim.Machinetta.Messages.NextWaypointPA;
import AirSim.Machinetta.Messages.RPMessage;
import AirSim.Machinetta.Messages.SearchSensorReadingAP;
import AirSim.Machinetta.Messages.FAARPLocationInformPA;
import AirSim.Machinetta.Messages.AskRefuelPA;
import AirSim.Machinetta.Messages.LandPA;
import AirSim.Machinetta.Messages.LaunchPA;
import AirSim.Machinetta.Messages.CirclePA;
import AirSim.SARSensorReading;
import AirSim.SensorReading;
import Machinetta.Communication.Message;
import Gui.*;

import java.util.*;

/**
 *
 * @author pscerri
 */
public class SmallUAV extends Aircraft {
    
    public final boolean INFOTECH_EXPERIMENT_MODE=true;
    public final int INFOTECH_CAMERA_DETECTION_LIMIT_METERS=1000;
    public final int INFOTECH_CAMERA_DETECTION_LIMIT_METERS_SQD=INFOTECH_CAMERA_DETECTION_LIMIT_METERS*INFOTECH_CAMERA_DETECTION_LIMIT_METERS;
    
    TDOASensor tdoaSensor = null;
    
    private static double uavMaxTurnRateDeg = 30; //original: 30
    public static double getUavMaxTurnRateDeg() { return uavMaxTurnRateDeg; }
    public static void setUavMaxTurnRateDeg(double value) { uavMaxTurnRateDeg = value; }

    private static double uavMaxSpeedKph = 150;
    public static double getUavMaxSpeedKph() { return uavMaxSpeedKph; }
    public static void setUavMaxSpeedKph(double value) { uavMaxSpeedKph = value; }

    //    public SmallUAVModel testModel;

    /** Creates a new instance of SmallUAV */
    public SmallUAV(String id, int x, int y, int z) {
        this(id, x, y, z, 39000.0);
    }
    
    public SmallUAV(String id, int x, int y, int z, double threshold) {
        super(id, x, y, z, new Vector3D(1.0, 1.0, 0.0));
	setSpeed(Asset.kphToms(uavMaxSpeedKph));
	setMaxSpeed(Asset.kphToms(uavMaxSpeedKph));
 	MAX_TURN_RATE = uavMaxTurnRateDeg;
	LOITER_TURN_RATE = MAX_TURN_RATE/30;
 	Machinetta.Debugger.debug("SmallUAV speed has been set to "+uavMaxSpeedKph+" kph, MAX_TURN_RATE has been set to "+uavMaxTurnRateDeg+" degrees,", 1, this);
        
        // These things are not so much armored, as hard to hit.
        armor = 3.9;
        
        //@NOTE: if you wanna use fuelFlag option, just remove following comments:
        this.setUseFuelFlag(false);
        this.setFuelConsumingRatio(1.0);
        this.setRefuelThreshold(threshold);
        this.setMaxFuelAmount(40000.0);
        this.setCurFuelAmount(40000.0);
        
	hasProxy = true;
        
        //For test
        //Asset refuelingAsset = env.getAssetByID("FAR1");
        //addTask(new MoveToFAARP(new Vector3D(20000, 25000, this.location.z), refuelingAsset));
        /*
        double radius = 1000.0;
        double angle = 2*Math.PI/8;
        double circleX = 20000;
        double circleY = 20000;
        double circleZ = 1000;
        Circle circleTask = new Circle(new Vector3D(circleX, circleY, circleZ), radius, angle);
        addTask(circleTask);
         */
    }
    
    // Part of above cheat
    static boolean first = true;
    
    public Asset.Types getType() {
        return Asset.Types.SMALL_UAV;
    }
    
    public void msgFromProxy(AirSim.Machinetta.Messages.PRMessage msg) {
        
        switch (msg.type) {
            
            case GEOLOCATE:
                if (tdoaSensor != null) {
                    Machinetta.Debugger.debug(1, "msgFromProxy: UAV Asset asked to geolocate");
                    TMASensorReading sr = tdoaSensor.geolocate(this, env);
                    if (sr != null) {
                        Machinetta.Debugger.debug(1, "msgFromProxy: "+ id + " got geolocation reading: " + sr);
                        GeoLocateDataAP reportMsg = new GeoLocateDataAP();
                        reportMsg.blockCount = 1;
                        reportMsg.blockCounter = 0;
                        reportMsg.data = sr.details;
                        reportMsg.emitterID = 77777; // @fix Don't know?
                        reportMsg.readingID = rand.nextInt();
                        reportMsg.sensorID = id;
                        
                        sendToProxy(reportMsg);
                    } else {
                        Machinetta.Debugger.debug(3, "msgFromProxy: "+id + " asked to geolocate, but failed to find anything!");
                    }
                } else {
                    Machinetta.Debugger.debug(3, "msgFromProxy: SmallUAV without TDOAsensor asked to geolocate!");
                }
                break;
                
            case NEW_WAYPOINT:
                
                NextWaypointPA nwMsg = (NextWaypointPA)msg;
                lastWaypoint = curWaypoint;
                lastWaypointPlanId = curWaypointPlanId;
		lastWaypointRcvdSimTimeMs = curWaypointRcvdSimTimeMs;
		curWaypoint = new Waypoint(nwMsg.longtitude, nwMsg.latitude, nwMsg.altitude,nwMsg.debugExpectedArrivalTime);
		curWaypointPlanId = nwMsg.debugPlanid;
		curWaypointRcvdSimTimeMs = env.getSimTimeMs();
                Vector3D nwp = new Vector3D(nwMsg.longtitude, nwMsg.latitude, nwMsg.altitude);
                addTask(new Move(nwp));

		//		testModel.setWaypoint(curWaypoint, curWaypointRcvdSimTimeMs);
		
		double distFromWaypoint = location.toVector(nwp).length();
		double timeToGetThere = nwMsg.debugExpectedArrivalTime - env.getSimTimeMs();
		double speedExpected = distFromWaypoint/(timeToGetThere/1000);
		Vector3D newHeading = location.toVector(curWaypoint);
		double angleToNewHeading = heading.angleToXY(newHeading);
		double newSpeed = speedExpected;
		if(speedExpected > (Asset.kphToms(uavMaxSpeedKph)*2))
		    newSpeed = (Asset.kphToms(uavMaxSpeedKph)*2);
		else if(speedExpected <0)
		    newSpeed = (Asset.kphToms(uavMaxSpeedKph)/2);
		
                Machinetta.Debugger.debug("msgFromProxy: UAV " + id + " new waypoint from plan "+nwMsg.debugPlanid+" : " + nwp + " received at "+env.getSimTimeMs()+" expected to arrive at "+nwMsg.debugExpectedArrivalTime+", uav is at "+location+", dist from new waypoint "+distFromWaypoint+", time till expectedArrival="+timeToGetThere+", avg speed to get there would be "+speedExpected+" m/s (setting to "+newSpeed+" m/s) angle between cur heading and new heading "+angleToNewHeading, 1, this);
		// @TODO: HACK HACK HACK Is this a hack?  Can we set
		// our speed?  A better question would be, why is our
		// speedExpected different than our maxSpeed?  Why
		// doesn't our planner give us better waypoints?? 
		// Well, truth to tell, the avg speeds expected aren't
		// all that far off from our set speed (41.666).  But
		// maybe they're still too far?  A 1 m/s difference
		// adds up...
		// 
		setSpeed(newSpeed);
                
                break;
                
            case CAMERA_COMMAND:
                
                CameraCommandPA pMsg = (CameraCommandPA)msg;
                Machinetta.Debugger.debug("msgFromProxy: UAV received camera command", 1, this);
                
		if(INFOTECH_EXPERIMENT_MODE) {
		    Vector3D cameraPoint = new Vector3D(pMsg.longtitude, pMsg.latitude, pMsg.altitude);
		    SA9 closestEmitter=null;
		    double bestDistSqd = Double.MAX_VALUE;
		    Object[] assets = env.getAllAssets().toArray(new Object[1]);
		    for(int loopi = 0; loopi < assets.length; loopi++) {
			Asset asset1 = (Asset) assets[loopi];
			if(null == asset1)
			    continue;
			if(asset1 instanceof SA9) {
			    double diffx = (cameraPoint.x - asset1.location.x) ;
			    double diffy = (cameraPoint.y - asset1.location.y);
			    double distSqd = diffx*diffx + diffy*diffy;
			    if(distSqd < bestDistSqd) {
				bestDistSqd = distSqd;
				closestEmitter = (SA9)asset1;
			    }
			}
		    }
		    double dist = Math.sqrt(bestDistSqd);
		    if(dist < INFOTECH_CAMERA_DETECTION_LIMIT_METERS) {
			Machinetta.Debugger.debug("INFOTECH: withinRange YES emitter "+closestEmitter.getID()+" step "+getStep()+" dist "+fmt.format(dist)+" detectedflag "+closestEmitter.detected+" detectedCount "+closestEmitter.detectedCount+" emitterlocation "+closestEmitter.location.toString()+" camera point "+cameraPoint.toString(), 1, this);
			closestEmitter.detected = true;
			closestEmitter.detectedCount++;
		    }
		    else {
			Machinetta.Debugger.debug("INFOTECH: withinRange NO  emitter "+closestEmitter.getID()+" step "+getStep()+" dist "+fmt.format(dist)+" detectedflag "+closestEmitter.detected+" detectedCount "+closestEmitter.detectedCount+" emitterlocation "+closestEmitter.location.toString()+" camera point "+cameraPoint.toString(), 1, this);
		    }
		}

                for (Sensor s: sensors) {
                    if (s instanceof EOIRSensor) {
                        ((EOIRSensor)s).setAltitude(pMsg.altitude);
                        ((EOIRSensor)s).setLatitude(pMsg.latitude);
                        ((EOIRSensor)s).setLongtitude(pMsg.longtitude);
                        ((EOIRSensor)s).setZoom(pMsg.zoom);
                    }
                }                                
                Env.setXPlaneView(this);
                break;
                
            case FAARP_LOCATION_INFORM:
                FAARPLocationInformPA faarpMsg = (FAARPLocationInformPA)msg;
                Machinetta.Debugger.debug(1, "msgFromProxy: SmallUAV " + this.getID() + " received move_to_faarp command!!!");
                    
                int faarpX = (int)faarpMsg.xPos;
                int faarpY = (int)faarpMsg.yPos;
                int faarpZ = (int)faarpMsg.zPos;
                
                Asset refuelingAsset = env.getAssetByID((String)faarpMsg.refuelingAssetID);
                
                if(isCapableToRefuel(faarpX, faarpY, faarpZ)) {                    
                    MoveToFAARP moveTask = new MoveToFAARP(new Vector3D(faarpX, faarpY, 0), refuelingAsset);
                    addTask(moveTask);
                } else {
                    Machinetta.Debugger.debug(1, "msgFromProxy: SmallUAV " + this.getID() + " cannot move to FAARP location because current fuel amount is not enough to get there!");
                    sendOutofFuelToProxy();
                }
                    
                break;
                    
            case ASK_REFUEL:
                AskRefuelPA arMsg = (AskRefuelPA)msg;
                Machinetta.Debugger.debug(1, "msgFromProxy: SmallUAV " + this.getID() + " received ask_refuel command!!!");
                
                Asset refuelingAsset2 = env.getAssetByID((String)arMsg.refuelingAssetID);
                AskRefuel askRefuelTask = new AskRefuel(this, refuelingAsset2);
                addTask(askRefuelTask);
                
                break;

            case LAND:
                LandPA ldMsg = (LandPA)msg;
                Machinetta.Debugger.debug(1, "msgFromProxy: SmallUAV " + this.getID() + " received land command!!!");
                
                DoubleGrid elevationGrid = env.getElevationGrid();
                int gridX = elevationGrid.toGridX(this.location.x);
                int gridY = elevationGrid.toGridY(this.location.y);
                double height = elevationGrid.getValue(gridX, gridY);
                
                Land landTask = new Land(this, new Vector3D(this.location.x, this.location.y, height), null, 0);
                addTask(landTask);
                
                break;

            case LAUNCH:
                LaunchPA lcMsg = (LaunchPA)msg;
                Machinetta.Debugger.debug(1, "msgFromProxy: SmallUAV " + this.getID() + " received launch command!!!");
                
                double launchHeight = lcMsg.zPos;
                Launch launchTask = new Launch(this, new Vector3D(this.location.x, this.location.y, launchHeight));
                addTask(launchTask);
                
                break;
           
            case CIRCLE:
                CirclePA ccMsg = (CirclePA)msg;
                Machinetta.Debugger.debug(1, "msgFromProxy: SmallUAV " + this.getID() + " received circle command!!!");
                
                double circleX = ccMsg.xPos;
                double circleY = ccMsg.yPos;
                double circleZ = ccMsg.zPos;
                double radius = ccMsg.radius;
                double angle = ccMsg.angle;
                
                Circle circleTask = new Circle(new Vector3D(circleX, circleY, circleZ), radius, angle);
                addTask(circleTask);
                
                break;
                
            default:
                super.msgFromProxy(msg);
        }
        
    }
    
    // @TODO: currently we assume that UAV will land after getting FAARP's x&y position.
    //  If it moves more complicated, we need to update this method, too.
    private boolean isCapableToRefuel(int x, int y, int z) {
        //double distance = Math.sqrt(Math.pow(this.location.x - x, 2) + Math.pow(this.location.y - y, 2) + Math.pow(this.location.z - 0, 2));
        double distance = this.location.toVector(x, y, this.location.z).length();
        double zDiff = Math.abs(this.location.z - z);
        distance += zDiff;
        
        if(this.getCurFuelAmount() >= distance*this.getFuelConsumingRatio())
            return true;
        else return false;
    }
    
    // Overriding with empty body to ensure Aircraft's version is
    // ignored - this will soon disappear 
    public void sense() { 
    }
    
    public void sendSensorReadingToProxy(SensorReading sr) {
        Machinetta.Debugger.debug("Sending sensor reading to proxy: " + sr.id+" "+sr, 0, this);
        
        RPMessage msg = null;
        if (sr instanceof TMASensorReading) {
            msg = new SearchSensorReadingAP();
            ((SearchSensorReadingAP)msg).sensorID = sr.sensor.toString();
            ((SearchSensorReadingAP)msg).readingID = 0L;
            ((SearchSensorReadingAP)msg).blockCount = 1;
            ((SearchSensorReadingAP)msg).blockCounter = 0;
            ((SearchSensorReadingAP)msg).data = ((TMASensorReading)sr).details;
        } else if (sr instanceof SARSensorReading) {            
            msg = new RPMessage(RPMessage.MessageTypes.SAR_SENSOR);
            msg.params.add(sr);
        } else if (sr instanceof EOIRSensorReading) {
            msg = new EOIRSensorReadingAP(((EOIRSensorReading)sr).getImage());
            ((EOIRSensorReadingAP)msg).sensorID = sr.sensor.toString();
        } else if (sr instanceof RSSISensorReading) {
            msg = new RPMessage(RPMessage.MessageTypes.RSSI_SENSOR_READING);
            msg.params.add(sr);
        } else if (sr instanceof DirRFSensorReading) {
            msg = new RPMessage(RPMessage.MessageTypes.DIRECTIONAL_RF_SENSOR_READING);
            msg.params.add(sr);
        } else {
            Machinetta.Debugger.debug("Not sending sensor reading of type " + sr.getClass(), 1, this);
        }
        
        if (msg != null) {
            // This is only commented out so that message failure shows up in the GUI
            // msg.setCriticality(Message.Criticality.NONE);
            sendToProxy(msg);
        }
        
    }
    
    /**
     * Overload this so that some sensors can be dealt with specially
     */
    public void addSensor(Sensor s) {
        super.addSensor(s);
        if (s instanceof TDOASensor) {
            tdoaSensor = (TDOASensor)s;
        }
    }
    
    public String toString() {
        return "SmallUAV " + id + " @ " + location.x + " " + location.y;
    }
}

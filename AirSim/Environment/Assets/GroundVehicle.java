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
 * GroundVehicle.java
 *
 * Created on June 20, 2004, 9:11 AM
 */

package AirSim.Environment.Assets;

import Machinetta.Debugger;
import AirSim.Environment.*;
import AirSim.*;
import AirSim.Environment.Buildings.*;
import AirSim.Machinetta.Messages.RPMessage;

import java.util.*;

/**
 *
 * @author  paul
 */
public abstract class GroundVehicle extends Asset {
    
    /** Actually, it isn't really radius it is half of a square column */
    public int SENSE_RADIUS = 3000, SEE_ID_DIST = 3000, SEE_STATE_DIST = 3000, FIRE_DIST = 1400;
    public int getFireDist() { return FIRE_DIST; } 
    
    /**
     * If this is true, this a/c has a SAR on board and should return SARSensorReadings
     *
     * Is currently true by default, but probably should be false
     *
     * @todo Change this so ground vehicles use "proper" sensor stuff.
     */
    protected boolean hasSARSensor = false;
    protected long lastSarTime = 0;
    private final static int TIME_BETWEEN_SAR_SCANS = 20;
    private final static int TIME_BETWEEN_SENSOR_READING_MSGS = 20;

    HashMap<Asset,Long> sentToCommander = new  HashMap<Asset,Long>();

    /** Creates a new instance of GroundVehicle */
    public GroundVehicle(String id, int x, int y, int z, Vector3D heading) {
        super(id, x, y, z, heading);
        setSpeed(Asset.mphToms(60));
	setMaxSpeed(mphToms(60));
	reportLocationRate = 20;
	// Same as infantry
	SEE_ID_DIST = 350;
	SEE_STATE_DIST = 350;
	FIRE_DIST = 200; // original 500
	SENSE_RADIUS = 350; //original 200
    }
    
    public String toString() { return id; }
    
    public void sense() {
        if ((steps + randomStepOffset) % senseRate != 0) {
	    return;
	}

        boolean flag = false;
        
        for (ListIterator li = env.getAssetsInBox((int)(location.x - SENSE_RADIUS), (int)(location.y - SENSE_RADIUS), -10, (int)(location.x + SENSE_RADIUS), (int)(location.y + SENSE_RADIUS), 10000).listIterator(); li.hasNext(); ) {
            Asset a = (Asset)li.next();
                        
	    if(this == a)
		continue;
	    if(a instanceof Munition)
		continue; 
	    if(a.forceId == this.forceId)
		continue;
            if(a.getContainingBuilding() != null) {
                Building b = a.getContainingBuilding();
                if(!b.getContentsVisible())
                    continue;
            }
                        
            for (ListIterator li2 = env.getBuildingsInArea((int)(location.x - SENSE_RADIUS), (int)(location.y - SENSE_RADIUS), (int)(location.x + SENSE_RADIUS), (int)(location.y + SENSE_RADIUS)).listIterator(); li2.hasNext(); ) {
                Building b = (Building)li2.next();
                Vector3D headingToA = this.location.toVector(a.location);
                Vector3D headingToB = this.location.toVector(b.getLocation());
                
                if(headingToA.length() > headingToB.length()) {
                    double diff = 0.0;
                    if(a.location.x == this.location.x) {
                        diff = 0.0001;
                    } else {
                        diff = a.location.x - this.location.x;
                    }
                    double aVal = (a.location.y - this.location.y)/diff;
                    double bVal = this.location.y - aVal*this.location.x;
                
                    double distance = Math.abs(aVal*b.getLocation().x -1*b.getLocation().y + bVal)/Math.sqrt(Math.pow(aVal,2)+1);
                    if(distance < (b.getWidth() + b.getHeight())/2) {
                        flag = true;
                        continue;
                    }
                }
            }            
            if(flag) continue;
            
            // This asset is within the WASMs sense range
            // System.out.println("Wasm " + this + " can sense " + a);
            SensorReading reading = new SensorReading((int)a.location.x, (int)a.location.y, (int)a.location.z, (a.heading != null? a.heading.angle() : 0.0), null, a.getType(), State.UNKNOWN, getPid());
	    reading.asset = a;
	    reading.forceId = ForceId.UNKNOWN;
	    double distSqd =  location.toVectorLengthSqd(a.location);
            if (distSqd < SEE_ID_DIST*SEE_ID_DIST)  {
		reading.id = a.id; 
		reading.forceId = a.getForceId();
	    }
            if ( distSqd < SEE_STATE_DIST*SEE_STATE_DIST) reading.state = a.state;
            
            // @TODO: Ground vehicles are not obscured to other ground vehicles by buildings.
            
            // Now check whether any trees get in the way
            for (Enumeration e = env.getTrees().elements(); e.hasMoreElements(); ) {
                Trees t = (Trees)e.nextElement();
                if (t.area.contains(((GroundVehicle)a).location.x, ((GroundVehicle)a).location.y)) {
                    //System.out.println("Trees obscure vehicle");
                    continue;
                }
            }
            weSeeThem(a);

	    if(reading != null) {
		// Stick it in memory
		memory.addSensorReading(reading);
                    
		if(hasProxy && !dontReportSense) {
		    if (!hasSARSensor) {
			Machinetta.Debugger.debug("Checking if reading.id != null for " + a, 0, this);
			// For now, unless the id can be seen, don't send to the proxy
			if (reading.id != null) {
			    long timeSent = 0;
			    if(sentToCommander.containsKey(a)) {
				timeSent = sentToCommander.get(a);
			    }
			    if(timeSent + TIME_BETWEEN_SENSOR_READING_MSGS > steps) {
				sentToCommander.put(a, steps);
				//				Machinetta.Debugger.debug("Sending sensor reading to  proxy" + a, 1, this);
				RPMessage msg = new RPMessage(reading);
				sendToProxy(msg);
			    }
			}
		    } else {
			if  (steps > (lastSarTime + TIME_BETWEEN_SAR_SCANS)) {
			    // @todo Need to add SAR sensor readings to memory
			    lastSarTime = steps;
			    SARSensorReading ssr = new SARSensorReading(reading.x, reading.y, getPid());
			    ssr.setProbs(a.getType());
			    ssr.setState(reading.state);
			    logSensorReading(ssr, a);
			    RPMessage msg = new RPMessage(ssr);
			    // System.out.println("Created SAR Sensor reading message for proxy:" + msg);
			    sendToProxy(msg);
			}
		    }
		}
	    }
        }
    }

}

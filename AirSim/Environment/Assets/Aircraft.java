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
 * Aircraft.java
 *
 * Created on June 8, 2004, 7:11 PM
 */

package AirSim.Environment.Assets;

import AirSim.SensorReading;
import AirSim.SARSensorReading;
import AirSim.Environment.*;
import AirSim.Machinetta.Messages.RPMessage;
import AirSim.Environment.Buildings.*;

import java.awt.*;
import java.util.*;

/**
 *
 * @author  paul
 */
abstract public class Aircraft extends Asset {
    
    /** Distance at which Aircraft is considered to be at a waypoint */
    protected double AT_WAYPOINT_DIST = 1.0;
    
    public double MAX_CLIMB_RATE = 0.1;
    public double MAX_DESCEND_RATE = 0.4;
    /** Maximum rate of turn per simulation step, in degrees */
    public double MAX_TURN_RATE = 2;
    public double LOITER_TURN_RATE = MAX_TURN_RATE/30.0;

    
    // Sensor field of view
    protected double FOV = 60.0;
    protected double SENSE_BOX_SIZE = 2000;
    /** Altitude at which A/C can detect asset id */
    protected double SEE_ID_DIST = 10000.0;
    /** Altitude at which A/C can detect the state of the asset */
    protected double SEE_STATE_DIST = 10000.0;
    /** Whether or not the aircraft can sense other things in the air */
    protected double SEE_AIRCRAFT_DIST = -1.0;
    
    /** Rate at which A/C will simply fail to see something that is actually there. */
    protected double FALSE_NEGATIVE_SENSE_RATE = 0.2;
    
    /**
     * If this is true, this a/c has a SAR on board and should return SARSensorReadings
     *
     * Is currently true by default, but probably should be false
     */
    protected boolean hasSARSensor = true;
    protected long lastSarTime = 0;
    private final static int TIME_BETWEEN_SAR_SCANS = 20;
    private final static int TIME_BETWEEN_SENSOR_READING_MSGS = 20;
    
    /** Time after which asset makes a sensor reading that is gets the same result with
     * subsequent scans.  E.g., if false positive seen the first time, it is seen (at least) all
     * times until this time.
     */
    private final static int SENSOR_READINGS_CONSTANT = 2000;
    
    /** Mapping between target string ids and the integer ids used by Machinetta.
     *
     * @fix HUGE CHEAT, make this static to save sharing target info, FIX
     */
    protected static Hashtable<Integer, String> targetsToIds = new Hashtable<Integer, String>();
    
    /** Waypoint this aircraft is currently flying to
     *
     * @todo Think about making this into a list for better planning in subclasses
     */
    protected Waypoint currWaypoint = null;
    
    /** Creates a new instance of Aircraft */
    public Aircraft(String id, int x, int y, int z, Vector3D heading) {
        super(id, x,y,z, heading);
	setMaxSpeed(kphToms(500));
    }
    
    public void step(long time) {
	if(tasks.size() <= 0) {
	    // If no task, loiter at current location
	    if(null == heading) {
		// Should really never be null, but just in case.
		heading = new Vector3D(0,0,0);
	    }
	    heading.turn(LOITER_TURN_RATE);
	}
	super.step(time);
    }

    /** @deprecated Use the tasking mechanisms. */
    protected void goToWaypoint(long time) {
        Vector3D toWaypoint = null;
        // This stuff should (but doesn't) take into account the current heading
        toWaypoint = currWaypoint.toVector(location.x, location.y, location.z);
        // Check whether at waypoint, if so, make it null
        if (Math.abs(toWaypoint.x) + Math.abs(toWaypoint.y) + Math.abs(toWaypoint.z) < AT_WAYPOINT_DIST) {
            Machinetta.Debugger.debug("Aircraft " + id + " at waypoint " + currWaypoint , 0, this);
            currWaypoint = null;
            return;
        }
        
        turnToVector(toWaypoint);
        heading.normalize2d();
        
        _step(time);
    }
    
    public Polygon getSensePolygon() {
        double boxLength = 2 * location.z * Math.tan(Math.toRadians(FOV/2.0));
        if (boxLength < SENSE_BOX_SIZE) boxLength = SENSE_BOX_SIZE;
        int [] xs = new int[4], ys = new int[4];
        //System.out.println("Box length : " + boxLength);
        Vector3D v = heading.makeCopy();
        v.length = 1.0;
        v.normalize2d();
        v.turn(90);
        xs[0] = (int)(location.x + (boxLength/2.0) * v.x);
        ys[0] = (int)(location.y + (boxLength/2.0) * v.y);
        v.turn(-90);
        xs[1] = (int)(xs[0] + boxLength * v.x);
        ys[1] = (int)(ys[0] + boxLength * v.y);
        v.turn(-90);
        xs[2] = (int)(xs[1] + boxLength * v.x);
        ys[2] = (int)(ys[1] + boxLength * v.y);
        v.turn(-90);
        xs[3] = (int)(xs[2] + boxLength * v.x);
        ys[3] = (int)(ys[2] + boxLength * v.y);
        /*
        for (int i = 0; i < 4; i++) {
            System.out.print("(" + xs[i] + "," + ys[i] + ") ");
        }
        System.out.println("");
         */
        return new Polygon(xs, ys, 4);
    }
    
    /** These HashMaps contain the FalsePositive object and the time seen (or not) */
    HashMap<Asset,Long> unseenFP = new  HashMap<Asset,Long>();
    HashMap<Asset,Long> seenFP = new  HashMap<Asset,Long>();
    HashMap<Asset,Long> unseenP = new  HashMap<Asset,Long>();
    HashMap<Asset,Long> seenP = new  HashMap<Asset,Long>();
    HashMap<Asset,Long> sentToCommander = new  HashMap<Asset,Long>();
    
    Random rand = new Random();
    /**
     * Handles both false positives and negatives, as well as SAR sensors if isSAR
     */
    public void sense() {

        // First do sensing
        if ((steps + randomStepOffset) % senseRate != 0) {
	    return;
	}

        // System.out.println("New sense cycle");
        Polygon p = getSensePolygon();
        Rectangle r = p.getBounds();
        
        LinkedList possible = env.getAssetsInBox(r.x, r.y, -10, r.x + r.width, r.y + r.height, 10);
        if (SEE_AIRCRAFT_DIST > 0.0) possible.addAll(env.getAssetsInBox((int)(location.x - SEE_AIRCRAFT_DIST), (int)(location.y - SEE_AIRCRAFT_DIST), 10,
                (int)(location.x + SEE_AIRCRAFT_DIST), (int)(location.y + SEE_AIRCRAFT_DIST), 5000));
        // System.out.println("None possible when looking in " + r);
        for (ListIterator li = possible.listIterator(); li.hasNext(); ) {
            Asset a = (Asset)li.next();
	    if(this == a)
		continue;
	    if(a instanceof Munition)
		continue;
            if(a.getContainingBuilding() != null) {
                Building b = a.getContainingBuilding();
                if(!b.getContentsVisible())
                    continue;
            }

            if (a.forceId != ForceId.BLUEFOR) {
                Machinetta.Debugger.debug("Determining whether " + a + " can be seen by " + this + " " + p.contains(a.location.x, a.location.y) + " " + (a != this) + " "  + a.forceId, 0, this);
	    }
            // @todo Notice this only sees red forces - just for testing
            if (p.contains(a.location.x, a.location.y) && a != this && a.forceId != ForceId.BLUEFOR) {
                Machinetta.Debugger.debug(a + " can be seen by " + this, 0, this);
                SensorReading reading = null;
                String type = a.getClass().toString(); // a instanceof GroundVehicle ? "GroundVehicle" : "Aircraft";
                type = type.substring(type.lastIndexOf('.') + 1);
                if (! (a instanceof FalsePositive)) {
                    
                    // Allow for some missed detections
                    boolean senseIt = true;
                    boolean newP = true;
                    // If recently didn't see then still don't
                    if (unseenP.containsKey(a)) {
                        Long time = unseenP.get(a);
                        if (time + SENSOR_READINGS_CONSTANT < steps) {
                            // Miss was so long ago we might see it now
                            unseenP.remove(a);
                        } else {
                            // Still don't see it, done.
                            senseIt = false;
                            newP = false;
                        }
                        // if recently seen it still will
                    } else if (seenP.containsKey(a)) {
                        Long time = seenP.get(a);
                        if (time + SENSOR_READINGS_CONSTANT < steps) {
                            // Saw it so long ago, need to think again
                            seenP.remove(a);
                        } else {
                            newP = false;
                        }
                    }
                    if (newP) {
                        if (rand.nextDouble() < FALSE_NEGATIVE_SENSE_RATE) {
                            // This asset is not going to see it
                            senseIt = false;
                            unseenP.put(a, steps);
                            Machinetta.Debugger.debug("Failing to see : " + a, 1, this);
                        } else {
                            seenP.put(a, steps);
			    weSeeThem(a);
                            Machinetta.Debugger.debug("Correctly seeing: " + a, 1, this);
                        }
                    }
                    
                    if (senseIt) {
                        // System.out.println("Aircraft may see : " + a);
                        reading = new SensorReading((int)a.location.x, (int)a.location.y, (int)a.location.z, a.heading.angle(), null, a.getType(), State.UNKNOWN, getPid());
			reading.asset = a;
                        if (location.z < SEE_ID_DIST) reading.id = a.id;
                        if (location.z < SEE_STATE_DIST) reading.state = a.state;
                        
                        Machinetta.Debugger.debug(this + " sensor reading " + reading, 0, this);
                        
                        if (reading.id != null) {
                            int iCode = reading.id.hashCode();
                            targetsToIds.put(new Integer(iCode), reading.id);
                        }
                    }
                } else {
                    Machinetta.Debugger.debug("Dealing with " + a, 1, this);
                    boolean newFP = true;
                    if (unseenFP.containsKey(a)) {
                        Long time = unseenFP.get(a);
                        if (time + 200 < steps) {
                            unseenFP.remove(a);
                        } else {
                            // Still don't see it, done.
                            newFP = false;
                        }
                    } else if (seenFP.containsKey(a)) {
                        Long time = seenFP.get(a);
                        if (time + 200 < steps) {
                            seenFP.remove(a);
                        } else {
                            // If we saw it recently, we still do
                            Machinetta.Debugger.debug("Still (incorrectly) seeing: " + a, 1, this);
                            reading = new SensorReading((int)a.location.x, (int)a.location.y, (int)a.location.z, a.heading.angle(), null, a.getType(), State.UNKNOWN, getPid());
                            newFP = false;
                        }
                    }
                    if (newFP) {
                        if (rand.nextDouble() < ((FalsePositive)a).falseDetectRate) {
                            // This asset is going to see it
                            reading = new SensorReading((int)a.location.x, (int)a.location.y, (int)a.location.z, a.heading.angle(), null, a.getType(), State.UNKNOWN, getPid());
                            seenFP.put(a, steps);
                            Machinetta.Debugger.debug("Incorrectly seeing: " + a, 1, this);
                        } else {
                            // (Correctly) didn't see it
                            unseenFP.put(a, steps);
                            Machinetta.Debugger.debug("Correctly not seeing: " + a, 1, this);
                        }
                    }
                }
                
                if (hasProxy && reading != null) {
                    
                    if (!hasSARSensor) {
                        // Stick it in memory
                        memory.addSensorReading(reading);
                        
			Machinetta.Debugger.debug("Checking if reading.id != null for " + a, 0, this);
                        // For now, unless the id can be seen, don't send to the proxy
                        if (reading.id != null) {
                            long timeSent = 0;
                            if(sentToCommander.containsKey(a)) {
                                timeSent = sentToCommander.get(a);
                            }
                            if(timeSent + TIME_BETWEEN_SENSOR_READING_MSGS > steps) {
                                sentToCommander.put(a, steps);
                                Machinetta.Debugger.debug("Sending message to the proxy" + a, 5, this);
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
            } else {
                // System.out.println("Wasm " + this + " cannot see " + a);
            }
        }
        // System.out.println("End sense cycle");
    }
    
    /** @Deprecated */
    protected void turnToVector(Vector3D toWaypoint) {
        //System.out.println("To waypoint: " + toWaypoint + " current heading: " + heading.angle());
        // Changing heading up or down
        double nz = 0.0;
        if (toWaypoint.z > heading.z) {
            nz = Math.min(MAX_CLIMB_RATE, toWaypoint.z);
        } else {
            nz = Math.max(-MAX_DESCEND_RATE, toWaypoint.z);
        }
        heading.z = nz;
        //System.out.println("Climb: " + nz);
        // Change heading in x-y plane
        double reqdTurn = heading.angleToXY(toWaypoint);
        //System.out.println("Raw required turn: " + reqdTurn);
        if (reqdTurn > 0) reqdTurn = Math.min(MAX_TURN_RATE, reqdTurn);
        if (reqdTurn < 0) reqdTurn = Math.max(-MAX_TURN_RATE, reqdTurn);
        //System.out.println("Normalized required turn: " + reqdTurn);
        heading.turn(reqdTurn);
        //System.out.println("New heading: " + heading.angle() + " " + heading);
        
        // Need to adjust the speed, based on the turn
    }
}

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
 * Ladar.java
 *
 * 
 *
 * Created on November 22, 2005, 3:00 PM
 *
 */

package AirSim.Environment.Assets.Sensors;

import AirSim.Environment.Assets.State;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.ForceId;
import AirSim.Environment.Assets.FalsePositive;
import AirSim.Environment.Assets.Munition;
import AirSim.Environment.Assets.Tank;
import AirSim.Environment.Env;
import AirSim.Environment.Vector3D;
import AirSim.Machinetta.Messages.RPMessage;
import AirSim.SARSensorReading;
import AirSim.SensorReading;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 *
 * To be honest, I have no idea what Ladar capabilities are, but wanted something more 
 * "reliable" than SARSensor and this sounds good.
 *
 * @author pscerri
 */
public class Ladar extends Sensor {
    
    static java.util.Random rand = new java.util.Random();
    
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
    
    /** These HashMaps contain the FalsePositive object and the time seen (or not) */
    HashMap<Asset,Long> unseenFP = new  HashMap<Asset,Long>();
    HashMap<Asset,Long> seenFP = new  HashMap<Asset,Long>();
    HashMap<Asset,Long> unseenP = new  HashMap<Asset,Long>();
    HashMap<Asset,Long> seenP = new  HashMap<Asset,Long>();
    HashMap<Asset,Long> sentToCommander = new  HashMap<Asset,Long>();
    
    /** A counter. Notice, this is only incremented each time this sensor sensors - not necessarily every simulation step. */
    long steps = 0;
    
    /** Creates a new instance of SARSensor */
    public Ladar(Asset selfAsset) {
        super(100, selfAsset);
    }
    
    public java.util.ArrayList<AirSim.SensorReading> _step(Asset self, Env env) {
        
        java.util.ArrayList<AirSim.SensorReading> ret = null;
        
        steps++;
        
        // System.out.println("New sense cycle");
        Polygon p = getSensePolygon(self);
        Rectangle r = p.getBounds();
        
        // To fix the unchecked conversion here would require playing around with Env (which I don't want to do) - Paul
        LinkedList possible = env.getAssetsInBox(r.x, r.y, -10, r.x + r.width, r.y + r.height, 10);
        if (SEE_AIRCRAFT_DIST > 0.0) possible.addAll(env.getAssetsInBox((int)(self.location.x - SEE_AIRCRAFT_DIST), (int)(self.location.y - SEE_AIRCRAFT_DIST), 10,
                (int)(self.location.x + SEE_AIRCRAFT_DIST), (int)(self.location.y + SEE_AIRCRAFT_DIST), 5000));
        // System.out.println("None possible when looking in " + r);
        for (ListIterator li = possible.listIterator(); li.hasNext(); ) {
            Asset asset = (Asset)li.next();
            if(self == asset)
                continue;
            // I.e., you don't see munitions (which is not good for your health)
            if(asset instanceof Munition)
                continue;
            
//             if (asset.getForceId() != ForceId.BLUEFOR) {
//                 Machinetta.Debugger.debug(asset.getID()+": Determining whether " + asset + " can be seen by " + this + " " + p.contains(asset.location.x, asset.location.y) + " " + (asset != self) + " "  + asset.getForceId(), 0, this);
//             }
            // @todo Notice this only see red forces - just for testing
	    //            if (p.contains(asset.location.x, asset.location.y) && asset != self && asset.getForceId() != ForceId.BLUEFOR) {
            if (p.contains(asset.location.x, asset.location.y) && asset != self) {
                Machinetta.Debugger.debug(self.getID()+": " +asset.getID()+asset + " can be seen by " + this, 0, this);
                SensorReading reading = null;
		Asset.Types type = asset.getType();
		//                String type = asset.getClass().toString(); // a instanceof GroundVehicle ? "GroundVehicle" : "Aircraft";
		//                type = type.substring(type.lastIndexOf('.') + 1);
                if (! (asset instanceof FalsePositive)) {
                    
                    // Allow for some missed detections
                    boolean senseIt = true;
                    boolean newP = true;
                    // If recently didn't see then still don't
                    if (unseenP.containsKey(asset)) {
                        Long time = unseenP.get(asset);
                        if (time + SENSOR_READINGS_CONSTANT < steps) {
                            // Miss was so long ago we might see it now
                            unseenP.remove(asset);
                        } else {
                            // Still don't see it, done.
                            senseIt = false;
                            newP = false;
                        }
                        // if recently seen it still will
                    } else if (seenP.containsKey(asset)) {
                        Long time = seenP.get(asset);
                        if (time + SENSOR_READINGS_CONSTANT < steps) {
                            // Saw it so long ago, need to think again
                            seenP.remove(asset);
                        } else {
                            newP = false;
                        }
                    }
                    if (newP) {
                        if (rand.nextDouble() < FALSE_NEGATIVE_SENSE_RATE) {
                            // This asset is not going to see it
                            senseIt = false;
                            unseenP.put(asset, steps);
                            Machinetta.Debugger.debug(self.getID()+": " +asset.getID()+": Failing to see : " + asset, 1, this);
                        } else {
                            seenP.put(asset, steps);
                            selfAsset.weSeeThem(asset);
                            Machinetta.Debugger.debug(self.getID()+": " +asset.getID()+": Correctly seeing: " + asset, 1, this);
                        }
                    }
                    
                    if (senseIt) {
                        // System.out.println("Aircraft may see : " + a);
                        reading = new SensorReading((int)asset.location.x, (int)asset.location.y, (int)asset.location.z, asset.heading.angle(), null, type, State.UNKNOWN, asset.getPid());
                        reading.asset = asset;

			reading.forceId = ForceId.UNKNOWN;
			double distSqd =  self.location.toVectorLengthSqd(asset.location);
			if (distSqd < SEE_ID_DIST*SEE_ID_DIST)  {
			    reading.id = asset.getID(); 
			    reading.forceId = asset.getForceId();
			}
			if ( distSqd < SEE_STATE_DIST*SEE_STATE_DIST) reading.state = asset.state;
                        Machinetta.Debugger.debug(self.getID()+": " +asset.getID()+this + " sensor reading " + reading, 0, this);
                        
                        if (reading.id != null) {
                            int iCode = reading.id.hashCode();
                            targetsToIds.put(new Integer(iCode), reading.id);
                        }
                    }
                } else {
                    Machinetta.Debugger.debug(self.getID()+": " +asset.getID()+": Dealing with " + asset, 1, this);
                    boolean newFP = true;
                    if (unseenFP.containsKey(asset)) {
                        Long time = unseenFP.get(asset);
                        if (time + 200 < steps) {
                            unseenFP.remove(asset);
                        } else {
                            // Still don't see it, done.
                            newFP = false;
                        }
                    } else if (seenFP.containsKey(asset)) {
                        Long time = seenFP.get(asset);
                        if (time + 200 < steps) {
                            seenFP.remove(asset);
                        } else {
                            // If we saw it recently, we still do
                            Machinetta.Debugger.debug(self.getID()+": " +asset.getID()+": Still (incorrectly) seeing: " + asset, 1, this);
                            reading = new SensorReading((int)asset.location.x, (int)asset.location.y, (int)asset.location.z, asset.heading.angle(), null, type, State.UNKNOWN, asset.getPid());
			    reading.forceId = ForceId.UNKNOWN;
			    double distSqd =  self.location.toVectorLengthSqd(asset.location);
			    if (distSqd < SEE_ID_DIST*SEE_ID_DIST)  {
				reading.id = asset.getID(); 
				reading.forceId = asset.getForceId();
			    }
			    if ( distSqd < SEE_STATE_DIST*SEE_STATE_DIST) reading.state = asset.state;

                            newFP = false;
                        }
                    }
                    if (newFP) {
                        if (rand.nextDouble() < ((FalsePositive)asset).falseDetectRate) {
                            // This asset is going to see it
                            reading = new SensorReading((int)asset.location.x, (int)asset.location.y, (int)asset.location.z, asset.heading.angle(), null, type, State.UNKNOWN, asset.getPid());
			    reading.forceId = ForceId.UNKNOWN;
                            seenFP.put(asset, steps);
                            Machinetta.Debugger.debug(self.getID()+": " +asset.getID()+": Incorrectly seeing: " + asset, 1, this);
                        } else {
                            // (Correctly) didn't see it
                            unseenFP.put(asset, steps);
                            Machinetta.Debugger.debug(self.getID()+": " +asset.getID()+": Correctly not seeing: " + asset, 1, this);
                        }
                    }
                }
                
                if (reading != null) {
                    
		    // @TODO: This is deranged, why do we have
		    // if(!hasSARSensor) when we KNOW _we_ are a Ladar
		    // sensor??
                    if (!hasSARSensor) {
                        // Stick it in memory
                        asset.getMemory().addSensorReading(reading);
                        
                        Machinetta.Debugger.debug(self.getID()+": " +asset.getID()+": Checking if reading.id != null for " + asset, 0, this);
                        // For now, unless the id can be seen, don't send to the proxy
                        if (reading.id != null) {
                            long timeSent = 0;
                            if(sentToCommander.containsKey(asset)) {
                                timeSent = sentToCommander.get(asset);
                            }
                            // @todo This whole loop probably goes.
                            if(timeSent + TIME_BETWEEN_SENSOR_READING_MSGS > steps) {
                                sentToCommander.put(asset, steps);
                                // Machinetta.Debugger.debug(self.getID()+": " +asset.getID()+": Sending message to the proxy" + asset, 1, this);
                                // RPMessage msg = new RPMessage(reading);
                                // @todo Change this.
                                // sendToProxy(msg);
                                if (ret == null) {
                                    ret = new java.util.ArrayList<AirSim.SensorReading>();
                                }
                                ret.add(reading);
                            }
                        }
                    } else {
                        if  (steps > (lastSarTime + TIME_BETWEEN_SAR_SCANS)) {
                            // @todo Need to add SAR sensor readings to memory
                            lastSarTime = steps;
                            SARSensorReading ssr = new SARSensorReading(reading.x, reading.y, asset.getPid());
                            ssr.setProbs(asset.getType());
                            ssr.setState(reading.state);
			    ssr.heading = reading.heading;
			    ssr.forceId = reading.forceId;
                            self.logSensorReading(ssr, asset);
                            // RPMessage msg = new RPMessage(ssr);
                            // System.out.println("Created SAR Sensor reading message for proxy:" + msg);
                            // @todo Change this.
                            // sendToProxy(msg);
                            if (ret == null) {
                                ret = new java.util.ArrayList<AirSim.SensorReading>();
                            }
			    Machinetta.Debugger.debug(1,self.getID()+": adding SARSensorReading to ret list, ssr="+ssr);
                            ret.add(ssr);
                        }
                    }
                }
            } else {
                // System.out.println("Wasm " + this + " cannot see " + a);
            }
        }
        // System.out.println("End sense cycle");
        return ret;
    }
    
    
    public Polygon getSensePolygon(Asset self) {
        double boxLength = 2 * self.location.z * Math.tan(Math.toRadians(FOV/2.0));
        if (boxLength < SENSE_BOX_SIZE) boxLength = SENSE_BOX_SIZE;
        int [] xs = new int[4], ys = new int[4];
        //System.out.println("Box length : " + boxLength);
        Vector3D v = self.heading.makeCopy();
        v.length = 1.0;
        v.normalize2d();
        v.turn(90);
        xs[0] = (int)(self.location.x + (boxLength/2.0) * v.x);
        ys[0] = (int)(self.location.y + (boxLength/2.0) * v.y);
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
}

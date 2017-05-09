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
 * Sensor.java
 *
 * Created on November 22, 2005, 2:59 PM
 *
 */

package AirSim.Environment.Assets.Sensors;

import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.State;
import AirSim.Environment.Assets.ForceId;
import AirSim.Environment.Assets.FalsePositive;
import AirSim.Environment.Assets.Tank;
import AirSim.Environment.Env;
import AirSim.SensorReading;
import Machinetta.Debugger;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author pscerri
 */
public abstract class Sensor {
    
    static protected java.util.Random rand = new java.util.Random();
    
    private int rate, count = 0;
    /** @deprecated I don't know when this got in here - Paul */
    protected Asset selfAsset = null;
    
    /** These HashMaps are for managing false positive and false negative sensing
     */
    private HashMap<Asset,Long> unseenFP = new  HashMap<Asset,Long>();
    private HashMap<Asset,Long> seenFP = new  HashMap<Asset,Long>();
    private HashMap<Asset,Long> unseenP = new  HashMap<Asset,Long>();
    private HashMap<Asset,Long> seenP = new  HashMap<Asset,Long>();
    
    /** Creates a new instance of Sensor
     *
     * @param rate How many steps between scans by this sensor
     * @param asset The asset this sensor is mounted on.
     */
    public Sensor(int rate, Asset selfAsset) {
        this.rate = rate;
        this.selfAsset = selfAsset;
    }
    
    /** Creates a new instance of Sensor
     *
     * @param rate How many steps between scans by this sensor
     * @param asset The asset this sensor is mounted on.
     */
    public Sensor(int rate) {
        this.rate = rate;
    }
    
    public abstract ArrayList<SensorReading> _step(Asset a, Env env);
    
    public ArrayList<SensorReading> step(Asset a, Env env) {
        if (++count >= rate) {
            count = 0;
            return _step(a, env);
        }
        return null;
    }
    
    /**
     * This method returns a SensorReading if target is seen.
     *
     * This method <b> does not </b> take into account:
     * Relative locations of self and target
     * Line of sight
     *
     * This method <b> does </b> take into account:
     * False positive and negative rates
     * Previous, recent sensing of the same object.
     *
     * Basically, after a sensor has determined that an asset (false positive or real)
     * is within the viewing area of self, it calls this for false positives and false
     * negatives to be taken into account.
     *
     * No confusion is applied.
     *
     * @param asset The asset that is seen or unseeen in this reading.
     * @param steps The counter of number of times the sensor has been called
     * @param SENSOR_READINGS_CONSTANT For how many steps the asset should get the same false positive/negative
     * @param FALSE_NEGATIVE_SENSE_RATE The rate at which the sensor misses things (the false positive rate is on the FalsePositive asset).
     * @return A sensor reading with exact asset details or nothing, depending on whether seen or not
     */
    protected SensorReading generateFPFNExactReading(Asset self, Asset asset, long steps, long SENSOR_READINGS_CONSTANT, double FALSE_NEGATIVE_SENSE_RATE) {
        
        SensorReading reading = null;
        
        // Notice the change here in what "type" is returned.  Hopefully values are the same, but I haven't checked carefully
        // Likely that "CLUTTER" doesn't work ...
        // Paul 23/11/05
        Asset.Types type = asset.getType();
        // String type = asset.getClass().toString(); // a instanceof GroundVehicle ? "GroundVehicle" : "Aircraft";
        // type = type.substring(type.lastIndexOf('.') + 1);
        
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
		    Debugger.debug(0,"Failing to see : " + asset);
                } else {
                    seenP.put(asset, steps);
                    // @todo: SRO Thu Mar 30 17:06:46 EST 2006 - Paul,
                    // this was the original problem, before you
                    // copied this code in here it did ;
                    // 'sensingAsset.weSeeThem(targetAsset)' but in
                    // the copying process it got changed to;
                    // 'targetAsset.weSeeThem(targetAsset)'
                    if(null != selfAsset)
                        selfAsset.weSeeThem(asset);
		    Debugger.debug(0,"Correctly seeing: " + asset);
                }
            }
            
            if (senseIt) {
                // System.out.println("Aircraft may see : " + a);
                if(null == asset) {
                    Debugger.debug(5,"ERROR: asset is null");
                }
                if(null == asset.heading) {
                    Debugger.debug(5,"ERROR: asset.heading is null");
                }
                reading = new SensorReading((int)asset.location.x, 
					    (int)asset.location.y, 
					    (int)asset.location.z, 
					    (null == asset.heading) ? 0.0: asset.heading.angle(),
					    null, 
					    asset.getType(), 
					    State.UNKNOWN,
					    self.getPid());
                reading.asset = asset;
		reading.forceId = ForceId.UNKNOWN;

                reading.id = asset.getID();
                reading.state = asset.state;
                
                Debugger.debug(0,this + " sensor reading " + reading);
            }
        } else {
            // False positive case
            Debugger.debug(1,"Dealing with " + asset);
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
                    Debugger.debug(1,"Still (incorrectly) seeing: " + asset);
                    reading = new SensorReading((int)asset.location.x, (int)asset.location.y, (int)asset.location.z, asset.heading.angle(), null, type, asset.state, self.getPid());
		    reading.forceId = ForceId.UNKNOWN;
                    newFP = false;
                }
            }
            if (newFP) {
                if (rand.nextDouble() < ((FalsePositive)asset).falseDetectRate) {
                    // This asset is going to see it
                    reading = new SensorReading((int)asset.location.x, (int)asset.location.y, (int)asset.location.z, asset.heading.angle(), null, type, State.UNKNOWN, self.getPid());
                    seenFP.put(asset, steps);
		    reading.forceId = ForceId.UNKNOWN;
                    Debugger.debug(1,"Incorrectly seeing: " + asset);
                } else {
                    // (Correctly) didn't see it
                    unseenFP.put(asset, steps);
                    Debugger.debug(1,"Correctly not seeing: " + asset);
                }
            }
        }
        
        return reading;
    }
    
    protected int getRate() {
        return rate;
    }
    
    protected void setRate(int rate) {
        this.rate = rate;
    }
}

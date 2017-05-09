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
 * MarkOneEyeball.java
 *
 * Created on November 22, 2005, 3:00 PM
 *
 */

package AirSim.Environment.Assets.Sensors;

import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.State;
import AirSim.Environment.Assets.ForceId;
import AirSim.Environment.Assets.FalsePositive;
import AirSim.Environment.Assets.Munition;
import AirSim.Environment.Assets.Tank;
import AirSim.Environment.Assets.UnattendedGroundSensor;
import AirSim.Environment.Env;
import AirSim.Environment.Vector3D;
import AirSim.Machinetta.Messages.RPMessage;
import AirSim.MarkOneEyeballReading;
import AirSim.SensorReading;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Supposed to represent a human eye, more or less, i.e. some human
 * flying a plane or running around on the ground.  Pretty much a
 * direct steal from SAR sensor at this point.  Really need to
 * refactor some of the code here (and from Ladar which should
 * probably be Lidar) out somewhere else.
 *
 * @author owens
 */
public class MarkOneEyeball extends Sensor {    
    private final static int DEFAULT_STEPS_BETWEEN_SCANS = 100;
    
    // @TODO: Change these to be more appropriate for the human eye

    // Sensor field of view - random guess that the human fov is 180
    // degrees.  Need to find some documentation to back this up or
    // give the correct number.
    // @TODO: Since we're going more with the "an infantry asset
    // represents a squad (or platoon) of infantry) should FOV maybe
    // be 360?
    protected double FOV = 180.0;
    protected double SENSE_BOX_SIZE = 800;
    /** Altitude at which sensor can detect asset id */
    protected double SEE_ID_DIST = 400.0;
    /** Altitude at which sensor can detect the state of the asset */
    protected double SEE_STATE_DIST = 400.0;
    /** Whether or not the sensor can sense other things in the air */
    protected double SEE_AIRCRAFT_DIST = -1.0;
    
    /** Rate at which sensor will simply fail to see something that is actually there. */
    protected double FALSE_NEGATIVE_SENSE_RATE = 0.2;
    
    /** Time after which asset makes a sensor reading that is gets the same result with
     * subsequent scans.  E.g., if false positive seen the first time, it is seen (at least) all
     * times until this time.
     */
    private final static int SENSOR_READINGS_CONSTANT = 2000;
    
    /** These HashMaps contain the FalsePositive object and the time seen (or not)
     *
     * @todo Could these be moved to a super class?  A whole chunk of code would need to move with it .... _step
     */
    private HashMap<Asset,Long> unseenFP = new  HashMap<Asset,Long>();
    private HashMap<Asset,Long> seenFP = new  HashMap<Asset,Long>();
    private HashMap<Asset,Long> unseenP = new  HashMap<Asset,Long>();
    private HashMap<Asset,Long> seenP = new  HashMap<Asset,Long>();
    
    /** A counter. Notice, this is only incremented each time this
     * sensor sensors - not necessarily every simulation step. */
    private long steps = 0;
    
    /** Creates a new instance of MarkOneEyeball
     */
    public MarkOneEyeball(int stepsBetweenScans, Asset selfAsset) {
        super(stepsBetweenScans, selfAsset);
    }

    public MarkOneEyeball(Asset selfAsset) {
        super(DEFAULT_STEPS_BETWEEN_SCANS, selfAsset);
    }
    
    /**
     * The main sense loop
     *
     */
    public java.util.ArrayList<AirSim.SensorReading> _step(Asset self, Env env) {
        
        java.util.ArrayList<AirSim.SensorReading> ret = null;
        
        steps++;
        
        Polygon p = getSensePolygon(self);
        Rectangle r = p.getBounds();
        
        // Get the assets on the ground
        LinkedList possible = env.getAssetsInBox(r.x, r.y, -10, r.x + r.width, r.y + r.height, 10);
                
        if (possible != null && possible.size() > 0) {
            
            for (ListIterator li = possible.listIterator(); li.hasNext(); ) {
                Asset asset = (Asset)li.next();
                
                // I.e., you don't see yourself or tiny sensors on the ground
                if(self == asset || (asset instanceof UnattendedGroundSensor))
                    continue;
                
                // I.e., you don't see munitions (which is not good for your health)
                if(asset instanceof Munition)
                    continue;
                                
		// If we can see a unit with the same forceid as ours
		// well enough to tell that they are the same forceid
		// as us and that they are not dead - then don't
		// report it.
		if(asset.getForceId() == self.getForceId()) {
		    double distSqd =  self.location.toVectorLengthSqd(asset.location);
		    if ((distSqd < SEE_ID_DIST*SEE_ID_DIST) 
			&& ( distSqd < SEE_STATE_DIST*SEE_STATE_DIST)
			) {
			if ( asset.state != State.DESTROYED)
			    continue;
		    }

		}

                Machinetta.Debugger.debug("Determining whether " + asset + " can be seen by " + this + " " + p.contains(asset.location.x, asset.location.y) + " " + (asset != self) + " "  + asset.getForceId(), 0, this);
                
                if (p.contains(asset.location.x, asset.location.y)) {
                    SensorReading reading = generateFPFNExactReading(self, asset, steps, SENSOR_READINGS_CONSTANT, FALSE_NEGATIVE_SENSE_RATE);
                                        
                    if (reading != null) {
                        
                        // @todo Need to add SAR sensor readings to memory
                        MarkOneEyeballReading ssr = new MarkOneEyeballReading(reading.x, reading.y, self.getPid());
                        // @todo MarkOneEyeball Sensors are currently working perfectly ... 

			// @TODO: since we don't have Infantry in our
			// confusion matrix, we can never see them -
			// which is bad, especially for the human eye.
			// So instead of going through the confusion
			// calculation (in setProbs()), just set it
			// here.
                        ssr.addProb(asset.getType(), 1.0);
			// Instead of the original code (stolen from Ladar) here;
			//                        ssr.setProbs(asset.getType(), 1.0);

			//                        Machinetta.Debugger.debug("For " + asset.getType() + " most likely is " + ssr.getMostLikelyExClutter() + " at " + ssr.getHighestProbExClutter(), 1, this);

                        ssr.setState(State.UNKNOWN);
			ssr.forceId = ForceId.UNKNOWN;
			double distSqd =  self.location.toVectorLengthSqd(asset.location);
			if (distSqd < SEE_ID_DIST*SEE_ID_DIST)  {
			    ssr.id = asset.getID(); 
			    ssr.forceId = asset.getForceId();
			}
			if ( distSqd < SEE_STATE_DIST*SEE_STATE_DIST) ssr.state = asset.state;

                        self.logSensorReading(ssr, asset);                        
                        if (ret == null) {
                            ret = new java.util.ArrayList<AirSim.SensorReading>();
                        }
                        // @todo Something is really strange here, creating ssr, using reading ... 
                        // ret.add(reading);
                        ret.add(ssr);
                        
                        Machinetta.Debugger.debug("Adding: " + reading, 0, this);
                    }
                } else {
                    // System.out.println(this + " cannot see " + a);
                }
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

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
 * SSMissile.java
 *
 * Created on May 16, 2005, 11:13 AM
 */

package AirSim.Environment.Assets;

import Machinetta.Debugger;
import AirSim.Environment.*;

/**
 *
 * @author pscerri
 */
public class SSMissile extends Asset implements Munition {
    private final static double SPEED_DEFAULT_M_PER_S = mphToms(800);
    
    private GroundVehicle target;
    double g = 0.02, vz;
    //    final private double SPEED = 6.0;
    final private double SPEED = 1.0; // so i can see it - really it should be 6.0
    protected double fuel = 10.0;
    private long deadTime = 0;
    
    protected Waypoint targetLoc = null;
    
    /** Creates a new instance of SSMissile */
    public SSMissile(String id, int x, int y, Waypoint targetLoc) {
	this(id,x,y,targetLoc,SPEED_DEFAULT_M_PER_S);
    }
    
    public SSMissile(String id, int x, int y, Waypoint targetLoc, GroundVehicle target) {
	this(id, x, y, targetLoc, target, SPEED_DEFAULT_M_PER_S);
    }
    
    /** Creates a new instance of SSMissile */
    public SSMissile(String id, int x, int y, Waypoint targetLoc, double speedMetersPerSecond) {
        super(id, x, y, 0, new Vector3D(0.0, 0.0, 1.0), 0.0);
        init(id, x, y, targetLoc,speedMetersPerSecond);
    }
    
    public SSMissile(String id, int x, int y, Waypoint targetLoc, GroundVehicle target, double speedMetersPerSecond) {
        super(id, x, y, 0, new Vector3D(0.0, 0.0, 1.0), 0.0);
        
        this.target = target;
        init(id, x, y, targetLoc,speedMetersPerSecond);
    }
    
    private void init(String id, int x, int y, Waypoint targetLoc, double speedMetersPerSecond) {
        setSpeed(speedMetersPerSecond);
	setMaxSpeed(speedMetersPerSecond);
        this.targetLoc = targetLoc;
        state = State.LIVE;
        
        heading = targetLoc.toVector(location.x, location.y, location.z);
        double totalDist = heading.length();
        double arg = (g*totalDist)/(SPEED*SPEED);
        if (arg > 1.0) arg = 1.0;
        else if (arg < 0.0) arg = 0.0;
        double theta = Math.asin(arg)/2.0;
        //        setSpeed(SPEED*Math.cos(theta));
        vz = SPEED*Math.sin(theta);
        heading.length = moveDistance();
        heading.normalize2d();
        heading.z = vz;
	armor = 3.9;	// These things are not so much armored, as hard to hit.
    }
    
    public Asset.Types getType() { return Asset.Types.CLUTTER; }
    
    protected void detonate() {
	double dist = location.toVectorLength(target.location);
        Debugger.debug(1,"missile "+getID()+" detonating at "+location+" dist "+dist+" from target "+target.getID()+" at"+target.location+" on step "+env.getStep());
        causeExplosion(getMaxDestroyDist());
        suicide(true, "missile detonated, hopefully near target.");
    }

    public double getMaxDestroyDist() {
        return 50.0;
    }  
    
    public void sense() {}
    
    /*
    public void step(long t) {
        heading.z -= g;
        if (state == S_DESTROYED) {
        System.out.println("step called on destroyed missile");
        env.removeAsset(this);
        return;
        }
        else if (location.z < -0.1 && heading.z <= 0.0) {
            detonate();
            return;
        } else {
            _step(t);
        }
    }
     */
    public void step(long t) {
        if(this.getIsMunitionRemove()) {
            deadTime++;
            if(deadTime > 100) {
                env.removeAsset(this);
            }
        } else {
            if(null != target) {
                if (fuel > 0.0) heading = target.location.toVector(location);
                double xdiff = target.location.x - location.x;
                double ydiff = target.location.y - location.y;
                double zdiff = target.location.z - location.z;
                double dist = Math.sqrt((xdiff * xdiff) + (ydiff * ydiff) + (zdiff * zdiff));
                if(dist < getMaxDestroyDist()/3) {
                    detonate();
                }
                if(dist < moveDistance()) {
                    location.x = target.location.x;
                    location.y = target.location.y;
                    location.z = target.location.z;
                    detonate();
                }
            }

            double xdiff = targetLoc.x - location.x;
            double ydiff = targetLoc.y - location.y;
            double zdiff = targetLoc.z - location.z;
            double dist = Math.sqrt((xdiff * xdiff) + (ydiff * ydiff) + (zdiff * zdiff));
            if(dist < getMaxDestroyDist()/3) {
                detonate();
            }

            if(dist < moveDistance()) {
                location.x = targetLoc.x;
                location.y = targetLoc.y;
                location.z = targetLoc.z;
                detonate();
            }

	    if(null != heading) {
		heading.length = SPEED;
		heading.normalize();
		fuel -= (0.01 + heading.z/10);
		if (fuel < 0.0) {
		    suicide(true, "missile out of fuel");
		}
	    }
            _step(t);
        }
    }
    
    public void msgFromProxy(AirSim.Machinetta.Messages.PRMessage msg) {
    }
    
    
}

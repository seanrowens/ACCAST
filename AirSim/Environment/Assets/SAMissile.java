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
 * SAMissile.java
 *
 * Created on May 20, 2005, 6:00 PM
 */

package AirSim.Environment.Assets;


import Machinetta.Debugger;
import AirSim.Environment.*;

/**
 *
 * @author pscerri
 */
public class SAMissile extends Asset implements Munition {
    
    private final static double SPEED_DEFAULT_M_PER_S = mphToms(800);
    private Aircraft target;
    protected double fuel = 100.0;
    private long deadTime = 0;
    
    public SAMissile(String id, int x, int y, Aircraft target) {
	this(id,x,y,target,SPEED_DEFAULT_M_PER_S);
    }

    /** Creates a new instance of SAMissile */
    public SAMissile(String id, int x, int y, Aircraft target, double speedMetersPerSecond) {
        super(id, x, y, 0, new Vector3D(0.0, 0.0, 1.0), 0.0);
        setSpeed(speedMetersPerSecond);
	setMaxSpeed(speedMetersPerSecond);
	setAtLocationDistance(speedMetersPerSecond);
        this.target = target;
        state = State.LIVE;
        // TODO: Mon Jun  6 15:46:40 EDT 2005 SRO
        //
        // Does it make more sense for the missile to just destroy
        // itself if it has no target?
        // 	if(null == target) {
        //             state = S_DESTROYED;
        // 	    Debugger.debug(getID()+": SA created with null target, setting state to S_DESTROYED", 1, this);
        // 	}
        Debugger.debug(getID()+": SA created", 1, this);
        
        // @fix to a more realistic number
        // MAX_DESTROY_DIST = 1000.0;
	armor = 3.9;	// These things are not so much armored, as hard to hit.
    }
    
    public Asset.Types getType() { return Asset.Types.SA9; }
    
    public void sense() {}
    
    protected void detonate() {
	double dist = location.toVectorLength(target.location);
        Debugger.debug(1,"missile "+getID()+" detonating at "+location+" dist "+dist+" from target "+target.getID()+" at"+target.location+" on step "+env.getStep());
        causeExplosion(getMaxDestroyDist());
        suicide(true, "missile detonated, hopefully near target.");
    }
    
    public double getMaxDestroyDist() {
        return 100.0;
    }  

    
    public void step(long t) {        
        if(this.getIsMunitionRemove()) {
            deadTime++;
            //if(deadTime > 100) {
                env.removeAsset(this);
            //}
        } else {        
            if(null == target) {
                java.util.Random r = new java.util.Random();
                heading.turn(r.nextInt(20));
                fuel -= (0.01 + heading.z/10);
                if (fuel < 0.0) heading = new Vector3D(0.0, 0.0, -1.0);
                _step(t);
            } else {
                Debugger.debug(getID()+": SA missile from " + location + " to " + target.location, 0, this);
                if (fuel > 0.0) heading = target.location.toVector(location);
            }

            if(null != target) {
                double xdiff = target.location.x - location.x;
                double ydiff = target.location.y - location.y;
                double zdiff = target.location.z - location.z;
                double dist = Math.sqrt((xdiff * xdiff) + (ydiff * ydiff) + (zdiff * zdiff));
                if(dist < (getMaxDestroyDist()/3)) {
                    detonate();
                }
                if(dist < moveDistance()) {
                    location.x = target.location.x;
                    location.y = target.location.y;
                    location.z = target.location.z;
                    detonate();
                }
            }

            if (heading.length() < moveDistance() || (location.z < -0.1 && heading.z <= 0.0))  {
                detonate();
            } else {
                heading.length = moveDistance();
                heading.normalize();
                fuel -= (0.01 + heading.z/10);
                if (fuel < 0.0) {
                    suicide(true, "missile out of fuel");
                }
                _step(t);
            }        
        }
    }
}

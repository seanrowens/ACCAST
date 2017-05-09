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
 * ASMissile.java
 *
 * Created on March 22, 2006, 7:32 PM
 *
 */

package AirSim.Environment.Assets;

import AirSim.Environment.Vector3D;
import AirSim.Environment.Waypoint;
import Machinetta.Debugger;

/**
 *
 * @author pscerri
 */
public class ASMissile extends Asset implements Munition {
    private final static double SPEED_DEFAULT_M_PER_S = mphToms(800);
    
    private GroundVehicle target;
    private Waypoint targetLoc;
    private long deadTime = 0;
    
    protected double fuel = 5000.0;
    
    /** Creates a new instance of ASMissile */
    public ASMissile(String id, int x, int y, int z, GroundVehicle target) {
	this(id,x,y,z,target, SPEED_DEFAULT_M_PER_S);
    }

    public ASMissile(String id, int x, int y, int z, GroundVehicle target, double speedMetersPerSecond) {
        super(id, x, y, z, new Vector3D(0.0, 0.0, 1.0), 6.0);
        
        setSpeed(speedMetersPerSecond);
	setMaxSpeed(speedMetersPerSecond);
	setAtLocationDistance(speedMetersPerSecond);

        this.target = target;
        targetLoc = new Waypoint(target.location.x, target.location.y, 0.0);
        
	armor = 3.9;	// These things are not so much armored, as hard to hit.
    }
    
    public Asset.Types getType() { return Asset.Types.CLUTTER; }
    
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
    
    // @TODO: There's a problem that shows up under heavy load,with
    // ASMissile and probably with other assets as well.  We call
    // super() in the constructor which eventually calls the
    // constructor in Asset, which adds 'this' to Env.  So what can
    // happen is that Env calls this.step() before this is done being
    // constructed.  In this particular case it shows up as accessing
    // targetLoc throwing a null exception error because it isn't set
    // yet.  Need to fix this properly but not sure how other than
    // removing the Env.add() in Asset and adding it in to every
    // single Asset descendant constructor, which would almost
    // certainly result in a bunch of time tracking down places I
    // missed.  And really we shouldn't add to env in constructor
    // because there's no guarantee that the lines in constructor will
    // be exceuted in the order we have written them.  (Same as the
    // problem with calling mythread.start() in constructor.)
    public void step(long t) {
	if(null == targetLoc)
	    return;

        if(this.getIsMunitionRemove()) {
            deadTime++;
            if(deadTime > 100) {
                env.removeAsset(this);
            }
        } else {

            if(null != target) {
                // Machinetta.Debugger.debug("ASMissile revectoring", 1, this);
                if (fuel > 0.0) heading = target.location.toVector(location);
                double xdiff = target.location.x - location.x;
                double ydiff = target.location.y - location.y;
                double zdiff = target.location.z - location.z;
                double dist = Math.sqrt((xdiff * xdiff) + (ydiff * ydiff) + (zdiff * zdiff));
                if(dist < (getMaxDestroyDist())/3) {
                    detonate();
                } else if(dist < moveDistance()) {
                    location.x = target.location.x;
                    location.y = target.location.y;
                    location.z = target.location.z;
                    detonate();
                } else if (location.z < -10.0) {
                    detonate();
                }
            }

            double xdiff = targetLoc.x - location.x;
            double ydiff = targetLoc.y - location.y;
            double zdiff = targetLoc.z - location.z;
            double dist = Math.sqrt((xdiff * xdiff) + (ydiff * ydiff) + (zdiff * zdiff));

//             if (dist < 5000.0) {
//                 heading.z = - (location.z / dist);
//             } else {
//                 heading.z = 0.0;
//             }

            heading.length = getSpeed();
            heading.normalize();

	    double distToTarget = location.toVectorLength(target.location);
	    Debugger.debug(1,"missile "+getID()+" at "+location+" distToTarget "+distToTarget+" from target "+target.getID()+" at"+target.location+" on step "+env.getStep()+" heading="+heading);

            Machinetta.Debugger.debug("New missile heading: " + heading, 0, this);
            fuel -= (0.01 + heading.z/10);
            if (fuel < 0.0) {
                suicide(true, "missile out of fuel");
            }
            _step(t);
        }
    }
}

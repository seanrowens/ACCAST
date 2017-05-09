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
 * MissileFire.java
 *
 * Created on September 28, 2007, 11:28 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Environment.Assets.Tasks;

import AirSim.Environment.*;
import AirSim.Environment.Assets.*;

/**
 *
 * @author junyounk
 */
public class MissileFire extends Attack {
    
    protected double MAX_DESTROY_DIST = 40.0;
    protected double MAX_DESTROY_DIST_SQD = MAX_DESTROY_DIST*MAX_DESTROY_DIST;
    boolean strikeMode = false;
    protected int STRIKE_RADIUS = 2000;
    int EXPLOSION_SIZE = 400;
    
    public MissileFire(Vector3D destination) {
        super(destination);
    }
    
    public MissileFire(Vector3D destination, double speed) {
        super(destination, speed);
    }
    
    public void step(Asset asset, long time) {
        Vector3D toWaypoint = destination.toVector(asset.location.x, asset.location.y, asset.location.z);
        
        // if (!isFinished) System.out.println("Attacking toWaypoint: " + toWaypoint + ", dest " + destination + ", asset " + asset.location);
        
        if (isFinished) {
            System.out.println("Dead WASMs shouldn't talk ... ");
        } else if (asset.location.z <= 0) {
            attack(asset, destination, EXPLOSION_SIZE);
            System.out.println("Boom");
            // @todo Need to indicate that this is dead ...
            isFinished = true;
            speed = 0.0;
        } else if (toWaypoint.length() < STRIKE_RADIUS) {
            strikeMode = true;
            destination.z =  -3 + ((toWaypoint.length()-destination.z)/30);
        } else {
            // still trying to get there ....
        }
        
        if (isFinished) {
            // Nada, shouldn't really be getting called
        } else if (strikeMode) {
            if (asset instanceof Aircraft) turnToVector((Aircraft)asset, toWaypoint);
            else asset.heading = toWaypoint;
            asset.heading.normalize2d();
	    //            System.out.println("Attacking on final approach ... " + toWaypoint.length() + " altitude " + asset.location.z + " to.z: " + asset.heading.z );
        } else {
            // System.out.println("Attacking on way to target ... " + toWaypoint.length());
            super.step(asset, time);
        }

	double xdiff = destination.x - asset.location.x;
	double ydiff = destination.y - asset.location.y;
	double zdiff = destination.z - asset.location.z;
	double distSqd = (xdiff * xdiff) + (ydiff * ydiff) + (zdiff * zdiff);
	if(distSqd < MAX_DESTROY_DIST_SQD) {
            attack(asset, destination, EXPLOSION_SIZE);
	}
	if(distSqd < (asset.moveDistance()*asset.moveDistance())) {
	    asset.location.x = destination.x;
	    asset.location.y = destination.y;
	    asset.location.z = destination.z;
            attack(asset, destination, EXPLOSION_SIZE);
	}
    }

    protected void attack(Asset asset, Vector3D destination, double dist) {
        /* Set weapon type, name, properties
        Weapon wp = new Weapon();
        wp.setWeaponType(~~~);
        asset.setWeapon(wp);
        */
        
        //env.setDamageMode(false);
        
        asset.causeExplosion(dist);
    }
    
    public String toString() { return "MissileFire:" + destination; }
    
}

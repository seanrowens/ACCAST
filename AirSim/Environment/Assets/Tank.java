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
 * Tank.java
 *
 * Created on June 20, 2004, 9:14 AM
 */

package AirSim.Environment.Assets;
import AirSim.Environment.*;
import AirSim.Environment.Assets.Tasks.DefendAir;
import AirSim.Environment.Assets.Tasks.RandomMove;

/**
 *
 * @author  paul
 */
public abstract class Tank extends GroundVehicle {
    
    
    public double SPEED = 0.01;
    public final double CONVOY_SPACING = 3.0;
    
    /** If this is not null, then the tank is part of a convoy */
    public Tank follow = null;
    public Road road = null;
    
    public boolean forward = true;
    
    /** Creates a new instance of Tank 
     * @Deprecated Use version without z
     */
    public Tank(String id, int x, int y, int z) {
        super(id, x, y, z, new Vector3D(0.0, 0.0, 0.0));
	SEE_STATE_DIST = 3000;
	FIRE_DIST = 2500;
	SENSE_RADIUS = 3000;
        setSpeed(SPEED);
        
        state = State.LIVE;
	fireAtSurfaceCapable = true;
	fireAtAirCapable = false;

	// Tanks default behavior is to defend the air
	DefendAir task = new DefendAir(false, true, x, y, 1000, 1000);
	addTask(task);
    }
    
    public Tank(String id, int x, int y) {
        this (id, x, y, 0);
    }
 
    public String toString() { return "Tank " + id; }



}

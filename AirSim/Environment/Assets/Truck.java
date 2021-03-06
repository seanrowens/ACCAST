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
 * Truck.java
 *
 * Created on May 13, 2005, 4:11 PM
 */

package AirSim.Environment.Assets;

import AirSim.Environment.*; 
import AirSim.Environment.Assets.Tasks.RandomMove;

/**
 *
 * @author pscerri
 */
public class Truck extends GroundVehicle {
    
    /** Creates a new instance of Truck */
    public Truck(String id, int x, int y) {
        super(id, x, y, 0, new Vector3D(1.0, 0.0, 0.0));
	// Move around randomly in a box 5km by 5km, centered on our
	// starting location
	Machinetta.Debugger.debug("Creating randomMove task for "+this, 5, this);	
	addTask(new RandomMove(x, y, 5000, 5000));
        state = State.LIVE;
        setSpeed(Asset.mphToms(30)); // SPEED_30MPH);
	//        setSpeed(0.00000001);
	fireAtSurfaceCapable = false;
	fireAtAirCapable = false;
    }

    // Trucks don't sense.  Save some CPU time.
    public void sense() { }
    
    public Asset.Types getType() { return Asset.Types.TRUCK; }
    
}

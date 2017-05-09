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
 * Munition.java
 *
 * Created on May 16, 2005, 11:07 AM
 */

package AirSim.Environment.Assets;

import Machinetta.Debugger;
import AirSim.Environment.*;

/**
 *
 * @author pscerri
 */
//public abstract class Munition extends Asset {
public interface Munition {
    
    public double getMaxDestroyDist();
    
    // Maximum distance at which this munition can harm assets
    // public double MAX_DESTROY_DIST = 40.0;
    
    /** Creates a new instance of Munition 
    public Munition(String id, int x, int y, int z, double speed, Vector3D heading) {
        super(id, x, y, z, heading, speed);
    }
    */
    
    /*
    public void sense() {}
    
    public void explosion(Asset a) {
        // Munitions are not affected by other munitions ... 
    }
    
    protected void detonate() {
	Debugger.debug(getID()+" detonating at "+location.x+","+location.y+","+location.z+" on step "+env.getStep(),5,this);
	causeExplosion(MAX_DESTROY_DIST);
	suicide(true, "missile detonated, hopefully near target.");
    }
    
    public void msgFromProxy(AirSim.Machinetta.Messages.PRMessage msg) {
        Machinetta.Debugger.debug("Proxy connected to munition, munition ignoring: " + msg, 2, this);
    }
    
    public String toString() { return id; }
     */
}

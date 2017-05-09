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
 * RandomMove.java
 *
 *
 */

package AirSim.Environment.Assets.Tasks;

import AirSim.Environment.Area;
import AirSim.Environment.Assets.Aircraft;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Vector3D;

import Machinetta.Debugger;

import java.util.*;

/**
 *
 * @author seanowens
 */
public class RandomMove extends Task {
    private static Random rand = new java.util.Random();

    public final static double DEFAULT_AIRCRAFT_ALT = 100.0;
    public final static double DEFAULT_GROUND_ALT = 0.0;

    private double width = 5000;
    private double height = 5000;

    private double xcenter = 0;
    private double ycenter = 0;

    Move move = null;

    /** Creates a new instance of RandomMove */
    public RandomMove(int xcenter, int ycenter, int width, int height) {
	this.xcenter = xcenter;
	this.ycenter = ycenter;
	this.width = width;
	this.height = height;
    }
    
    public void step(Asset asset, long time) {  
	if(asset.getSpeed() <= 0)
	    asset.setSpeed(asset.getMaxSpeed());
	if (move == null || move.finished(asset)) {
	    double x, y, z;
	    if (asset instanceof Aircraft) {
		z = DEFAULT_AIRCRAFT_ALT;
	    } else {
		z = DEFAULT_GROUND_ALT;
	    }
	    x = rand.nextInt((int)width) - (width/2) + xcenter;
	    y = rand.nextInt((int)height) - (height/2) + ycenter;
	    move = new Move(new Vector3D(x, y, z));
	} 
	move.step(asset, time);
    }
     
    /** RandomMove is a never ending task ... */
    public boolean finished(Asset asset) { return false; }
}

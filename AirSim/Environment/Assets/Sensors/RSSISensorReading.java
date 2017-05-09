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
 * RSSISensorReading.java
 *
 * Created on May 19, 2006, 3:16 PM
 *
 */

package AirSim.Environment.Assets.Sensors;

import AirSim.SensorReading;

import java.util.*;

/**
 *
 * @author pscerri
 */
public class RSSISensorReading extends SensorReading {

    public double x, y, z;
    public int[] channels;
    public double[] strengths;
    public long time = System.currentTimeMillis();
    
    /** Creates a new instance of RSSISensorReading */
    public RSSISensorReading(double x, double y, double z, int channels[], double strengths[]) {
        this.x = x;
        this.y = y;
        this.z = z;
	this.channels = channels;
	this.strengths = strengths;
    }
    
    public String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append("RSSI @ <").append(x).append(", ").append(y).append(", ").append(z).append(">, channel/strengths = ");
	for(int loopi = 0; loopi < channels.length; loopi++) {
	    buf.append("(").append(channels[loopi]).append(",").append(strengths[loopi]).append("), ");
	}
	return buf.toString();
    }
    
}

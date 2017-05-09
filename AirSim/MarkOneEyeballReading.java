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
 * MarkOneEyeballReading.java
 *
 * Created on September 12, 2005, 5:27 PM
 *
 */

package AirSim;

import AirSim.Environment.Assets.Asset;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.Debugger;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;

/**
 *
 * @author pscerri
 */
public class MarkOneEyeballReading extends ConfusionMatrixReading {
    
    /** Creates a new instance of MarkOneEyeballReading */
    public MarkOneEyeballReading(int x, int y, ProxyID sensor) {
	super(x,y,sensor);
    }

    public String toString() { 
	calculateMostLikely();

	return "MarkOneEyeballReading: mostlikely="+mostLikely+" ("+fmt.format(mostLikelyProb)+")"
	    +" mostlikelyExClutter="+mostLikelyExClutter+" ("+fmt.format(mostLikelyExClutterProb)+")"
	    + " id: " + id 
	    + " loc: ("+x+","+y+","+z+")"
	    + " state: " + state 
	    + " forceId: " +forceId
	    +" sensor: " +sensor
	    +" heading " +heading
	    +" type: "+ probsToString()
	    ; 
    }

}

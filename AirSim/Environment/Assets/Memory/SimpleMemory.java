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
 * SimpleMemory.java
 *
 * Created on May 27, 2005, 3:16 PM
 */

package AirSim.Environment.Assets.Memory;

import Machinetta.Debugger;
import AirSim.Environment.Assets.*;
import AirSim.Environment.Env;
import AirSim.SensorReading;

import java.util.*;

/**
 *
 * @author pscerri
 */
public class SimpleMemory implements Memory { 
    private static final long MAX_MEMORY_TIME_MS = 30000;
    private Env env = null;
    private Asset owner;
    protected HashMap<String,SensorReading> knownAssets = new HashMap<String,SensorReading>();
    
    /** Creates a new instance of SimpleMemory */
    public SimpleMemory(Asset owner) {
	env = new Env();
	this.owner = owner;
    }

    public void addSensorReading(SensorReading reading) {
	if(null == reading.asset)
	    throw new NullPointerException("SensorReading.asset should not be null in SensorReading="+reading);
        if (reading.id == null) {
            // SimpleMemory doesn't remember these .... 
        } else {
	    if(!(owner instanceof Munition)
	       && !(owner instanceof Hummer)
	       ) {
//  		if((null != owner) && (null != reading.asset))
//  		    Debugger.debug(owner.getID()+": Remembering reading (id "+reading.id+") for "+reading.asset.getID()+" at "+reading.x+","+reading.y+" state "+reading.state, 5, this);
//  		else
//  		    Debugger.debug(owner.getID()+": Remembering reading (id "+reading.id+") at "+reading.x+","+reading.y+" state "+reading.state, 5, this);
	    }
	    knownAssets.put(reading.id, reading);
        }
    }

    public SensorReading[] getAllKnownAssets() {        
	ArrayList<SensorReading> readings = new ArrayList<SensorReading>();
	long oldestTime = env.getSimTimeMs() - MAX_MEMORY_TIME_MS;
	Iterator iter = knownAssets.values().iterator();
	while(iter.hasNext()) {
	    SensorReading r = (SensorReading)iter.next();
	    if(r.time >= oldestTime)
		readings.add(r);
		
	}

	SensorReading [] readingAry = readings.toArray(new SensorReading[1]);

        return readingAry;
    }
        
}

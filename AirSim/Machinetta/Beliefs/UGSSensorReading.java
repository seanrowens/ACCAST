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
 * UGSSensorReading.java
 *
 * Created on March 13, 2006, 7:00 PM
 *
 */

package AirSim.Machinetta.Beliefs;

import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;

/**
 * UGSSensorReading is used by the proxies for Unattended Ground
 * Sensors to share beliefs about detections.  Note that these are
 * very simple sensors, that simply report whether or not they are
 * hearing anything, and their current location.
 *
 * @author pscerri
 */
public class UGSSensorReading extends Belief {
    
    private ProxyID pid = null;
    public ProxyID getPid() { return pid; }
    public void setPid(ProxyID value) { pid = value; }
    private long time = 0l;
    private int x = 0;
    private int y = 0;
    /** If true this is a postive reading, otherwise it is a negative reading */
    private boolean present = true;
    
    /** Creates a new instance of UGSSensorReading */
    public UGSSensorReading(ProxyID pid, int x, int y, long time) {   
	this.pid = pid;
        this.x = x;
        this.y = y;
        this.time = time;
    }

    /** Creates a new instance of UGSSensorReading */
    public UGSSensorReading(ProxyID pid, int x, int y, long time, boolean present) {   
	this.pid = pid;
        this.x = x;
        this.y = y;
        this.time = time;
        this.present = present;
    }
    
    public BeliefID makeID() {
        return new BeliefNameID("UGSSR:" + time);
    }

    public long getTime() {
        return time;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isPresent() {
        return present;
    }
    
}

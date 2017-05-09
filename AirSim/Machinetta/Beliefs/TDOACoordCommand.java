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
 * TDOACoordCommand.java
 *
 * Created on November 29, 2007, 2:00 PM
 *
 */

package AirSim.Machinetta.Beliefs;

import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.ProxyID;
import Util.*;

/**
 * TDOACoordCommand is used for TDOA (Time Difference Of Arrival)
 * emitter geolocation coordination.
 *
 * @author pscerri
 */
public class TDOACoordCommand extends Belief {        
    
    /** Time at which a reading should be taken, in ms since Jan 1, 1970 UTC
     * i.e., System.currentTimeMillis();
     * 
     */
    private long senseTime = -0L;
    
    /** 
     * Non-null if this refers to a specific proxy
     */
    private ProxyID target = null;
    
    /**
     * Location to move to
     */
    private PositionMeters loc = null;
    
    private boolean complete = false;
    
    /** Creates a new instance of TDOACoordCommand */
    public TDOACoordCommand() {              
    }

    private static int counter = 0;
    
    public BeliefID makeID() {
        return new BeliefNameID("TDOACoordCommand:" + counter++);
    }

    public PositionMeters getLoc() {
        return loc;
    }

    public long getSenseTime() {
        return senseTime;
    }

    public ProxyID getTarget() {
        return target;
    }

    public void setLoc(PositionMeters loc) {
        this.loc = loc;
    }

    public void setSenseTime(long senseTime) {
        this.senseTime = senseTime;
    }

    public void setTarget(ProxyID target) {
        this.target = target;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

}

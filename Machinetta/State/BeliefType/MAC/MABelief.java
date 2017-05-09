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
 * MABelief.java
 *
 * Created on November 5, 2003, 12:23 PM
 */

package Machinetta.State.BeliefType.MAC;

import Machinetta.Coordination.MAC.Agent;
import Machinetta.State.*;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.ProxyID;

/**
 *
 * @author  pscerri
 */
public abstract class MABelief extends Belief {
        
    public java.util.Vector<ProxyID> visited;
    public int uniqueID = 0;
    
    /** Needed for XML */
    public MABelief() {}        
    
    /** Creates a new instance of MABelief */
    public MABelief(Agent a) {
        super(new BeliefNameID(a.makeComparisonString(a.getDefiningBeliefs())));
        visited = a.visitedAgents;
        uniqueID = a.uniqueID;
    }
    
    /** Works on the assumption that id is always set, either when agent moves or 
     * when created using constructor with agent. I agree this is not desirable!
     */
    public BeliefID makeID() {
        return id;
    }
}

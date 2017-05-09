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
 * PlannedPath.java
 *
 * Created on July 26, 2006, 3:59 PM
 *
 */

package AirSim.Machinetta;

import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.ProxyID;

/**
 * A path that a proxy is planning to start in the near future, being
 * shared with teammates typically to enable deconfliction, i.e. to
 * avoid coming too near or colliding with other assets that are also
 * planning paths.
 *
 * @author pscerri
 */
public class PlannedPath extends Belief {
    
    public Path3D path = null;
    public long time = 0L;
    public ProxyID owner = null;
    
    /** This will be non-null iff some other proxy finds a conflict with this
     * path and sends it back to the owner.
     */
    public BeliefID originalPlannedPathID = null;
    public Path3D conflicted = null;
    public ProxyID conflictDetectedBy = null;    
    
    /** Creates a new instance of PlannedPath */
    public PlannedPath(Path3D path, ProxyID owner) {
        this.path = path;
        this.owner = owner;
        
        // @fix When time is added to Path3D use that time instead
        time = System.currentTimeMillis();
    }

    public BeliefID makeID() {
        return new BeliefNameID("Path:" + owner + "@" + time + ( (null != conflicted) ? " CONFLICT_WITH:"+conflicted.getAssetID()+" DETECTEDBY:"+conflictDetectedBy : ""));
    }
    
    
}

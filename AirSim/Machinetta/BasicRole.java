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
 * BasicRole.java
 *
 * Created on August 17, 2005, 1:27 PM
 *
 */

package AirSim.Machinetta;

import AirSim.Environment.Assets.Tasks.TaskType;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import java.util.Hashtable;

/** 
 * BasicRole is the RoleBelief base class for all roles allocated by
 * Sanjaya proxies.  It's a fairly straightforward task type plus hash
 * table of parameters specific to the task type.  Task types are
 * enumerated in AirSim.Environment.Assets.Tasks.TaskType.  The proxy
 * AA classes evaluate the capability of the RAP to perform a
 * particular BasicRole and use that to decide whether or not to take
 * on the role.  The proxy RI classes implement the actual performance
 * of the role.
 *
 * @author pscerri
 */
public class BasicRole extends RoleBelief {
    
    // @TODO: We shouldn't be using TaskType here, we should create a
    // RoleType enum or something.  Then we make each RI that
    // implements a Role break the Role down into concrete actions
    // equating to Tasks in Sanjaya, i.e. the proxy is 'driving' the
    // Asset/RAP in Sanjaya.
    public TaskType type = null;
    public Hashtable params = new Hashtable();
    
    transient static int idCounter = 0;
    
    /** For create from XML */
    public BasicRole() {
    }
    
    /** Creates a new instance of BasicRole */
    public BasicRole(TaskType type) {
        this.type = type;
        roleName = type.toString();
        
        makeID();
    }
    
    public RoleBelief instantiate(Hashtable params) {
        BasicRole nr = new BasicRole(type);
	if(null != this.responsibleRAP) 
	    nr.responsibleRAP = this.responsibleRAP;
        Machinetta.Debugger.debug(1, type + " initial params: " + nr.params);
        nr.params = new Hashtable();
        if (params != null) {
            nr.params.putAll(params);
        }
        if (this.params != null) {
            nr.params.putAll(this.params);
        }
        Machinetta.Debugger.debug(1, type + " instantiated params: " + nr.params);
        nr.id = null;
        // @todo Might want to clone this ... actually it is already done in PlanAgent
        // Might want to remove this?
        nr.infoSharing = infoSharing;
        nr.makeID();
        return nr;
    }
    
    public BeliefID makeID() {
        if (id == null) {
            if (params != null) {
                id = new BeliefNameID(type + ":" + params+" "+idCounter++);
            } else {
                id = new BeliefNameID(type + ":" + idCounter++);
            }
        }
        return id;
    }
    
    private final static long serialVersionUID = 1L;
    
    public Object getParams() {
        return params;
    }
    
    public TaskType getType() {
        return type;
    }
    
    public String toString() {
        return type + ":" + params + " "+super.toString();
    }

}

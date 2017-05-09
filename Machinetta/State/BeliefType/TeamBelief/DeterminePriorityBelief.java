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
 * DeterminePriorityBelief.java
 *
 * Created on 13 Octember 2002, 12:11
 */

package Machinetta.State.BeliefType.TeamBelief;

import Machinetta.Configuration;
import Machinetta.State.*;
import Machinetta.State.BeliefType.*;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import Machinetta.State.BeliefType.TeamBelief.Priority.PriorityBelief;

import java.util.Hashtable;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 * A role to determine the priority of a plan
 *
 * @author  pynadath
 */
public class DeterminePriorityBelief extends RoleBelief {
    
    /** The priority to be determined */
    public PriorityBelief priority = null;
    
    /** For auto XML */
    public DeterminePriorityBelief() {}
    
    public BeliefID makeID() {
        return new Machinetta.State.BeliefNameID("DeterminePriority"+priority.getID());
    }
    
    /** Creates a new instance of DeterminePriorityBelief */
    public DeterminePriorityBelief(BeliefID id, PriorityBelief priority) {
        super(id, "DeterminePriority");
        this.priority = priority;
    }
    
    /** Creates a standard BeliefID */
    public DeterminePriorityBelief(PriorityBelief priority) {
        this(new BeliefNameID("Determine "+priority.getID()),priority);
    }
    
    /****************** Access */
    public PriorityBelief getPriority() { return priority; }
    
    public String toString() { return "Determine " + priority; }
        
    /** Fills in instance-specific values of this belief by lookup in the provided table
     */
    public RoleBelief instantiate(Hashtable params) {
        Machinetta.Debugger.debug(1,"Instantiating with "+params);
        if (params.containsKey("Priority")) {
	    PriorityBelief newPriority = null;
	    Object parameter = params.get("Priority");
	    if (parameter instanceof PriorityBelief)
		newPriority = (PriorityBelief)parameter;
	    else if (parameter instanceof BeliefID) {
		BeliefID priorityID = (BeliefID)parameter;
		newPriority = (PriorityBelief)state.getBelief(priorityID);
	    }
	    if (newPriority != null)
		return new DeterminePriorityBelief(newPriority);
        }
        /** Can't instantiate a DeterminePriorityBelief without a priority */
        return null;
    }
    
     public static final long serialVersionUID = 1L;
}

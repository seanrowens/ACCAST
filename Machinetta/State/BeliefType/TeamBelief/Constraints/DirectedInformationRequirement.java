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
 * DirectedInformationRequirement.java
 *
 * Created on August 25, 2005, 12:31 PM
 *
 */

package Machinetta.State.BeliefType.TeamBelief.Constraints;

import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.ProxyID;

/**
 *
 * @author pscerri
 */
public class DirectedInformationRequirement extends Belief  {
    
    /* Either type must be not null or params must be true */
    public Class type = null;
    
    public boolean sendParams = false;
    public String paramName = null;
    
    public ProxyID to = null;
    public BeliefID performer = null;
    
    // True iff added to information factory
    public boolean added = false;
    
    /** Some label for distinguishing between DirectedInformationRequirements.
     *
     * This name should be in the meta data, with key "Label" of the belief.
     */
    public String name = null;
    
    /**
     * If this is not null, when sending information, the agent should attach this
     * beliefID to the belief.
     */
    public BeliefID proscribedID = null;
    
    /**
     * If this is not null, the sender should only send a belief if it has exactly
     * this BeliefID.
     * public BeliefID specificID = null;
     */
    
    /** For XML */
    public DirectedInformationRequirement() {}
    
    /**In this case the Role with which this is associated has a responsibility
     * to send any information it gets of class type to "to"
     */
    public DirectedInformationRequirement(Class type, ProxyID to) {
        this.type = type;
        this.to = to;
    }
    
    /**In this case the Role with which this is associated has a responsibility
     * to send any information it gets of class type to "to"
     */
    public DirectedInformationRequirement(String name, Class type, ProxyID to) {
        this.type = type;
        this.to = to;
        this.name = name;
    }
    
    /** In this case the Role with which this is associated has a responsibility
     * to send any information it gets of class type to whoever is performing the
     * role "other".   The PlanAgent's informs the proxy
     * with this role who is performing "other".
     */
    public DirectedInformationRequirement(String name, Class type, BeliefID other) {
        this.type = type;
        this.performer = other;
        this.name = name;
    }
    
    /** In this case the Role with which this is associated has a responsibility
     * to send any information it gets of class type to whoever is performing the
     * role "other".   The PlanAgent's informs the proxy
     * with this role who is performing "other".
     */
    public DirectedInformationRequirement(Class type, BeliefID other) {
        this.type = type;
        this.performer = other;
    }
    
    /** In this case the Role with which this is associated has a responsibility
     * to send any information it gets of class type to whoever is performing the
     * role "other".   The PlanAgent's informs the proxy
     * with this role who is performing "other".
     */
    public DirectedInformationRequirement(Class type, ProxyID to, BeliefID proscribed) {
        this.type = type;
        this.to = to;
        this.proscribedID = proscribed;
    }
    
    /** In this case the Role with which this is associated has a responsibility
     * to send any information it gets of class type to whoever is performing the
     * role "other".   The PlanAgent's informs the proxy
     * with this role who is performing "other".
     */
    public DirectedInformationRequirement(Class type, BeliefID other, BeliefID proscribed) {
        this.type = type;
        this.performer = other;
        this.proscribedID = proscribed;
    }
    
    /**
     * Creates a DIR requiring that the plan parameters be sent to the proxy which accepts
     * role other.
     *
     * @fix This is really redundant once the planParams field moves with the RoleAgent (which it currently does not.)
     */
    public DirectedInformationRequirement(String paramName, BeliefID other) {
        sendParams = true;
        this.paramName = paramName;
        this.performer = other;
    }
    
    /**
     * Creates a DIR requiring that a specific belief be sent
     *
     * public DirectedInformationRequirement(BeliefID specificID, BeliefID other) {
     * this.specificID = specificID;
     * this.performer = other;
     * }
     */
    
    public DirectedInformationRequirement clone() {
        
        DirectedInformationRequirement ret = new DirectedInformationRequirement();
        ret.type = type;
        ret.sendParams = sendParams;
        ret.paramName = paramName;
        ret.to = to;
        ret.performer = performer;
        ret.name = name;
        ret.proscribedID = proscribedID;
        
        return ret;
    }
    
    public Machinetta.State.BeliefID makeID() {
        if (id == null) {
            id = new BeliefNameID(type + " TO:" + to + " PERF:" + performer ); // (to != null? to.toString() : performer.toString()));
        }
        return id;
    }
    
    public static final long serialVersionUID = 1L;
    
    public Class getType() {
        return type;
    }
    
    public void setType(Class type) {
        this.type = type;
    }
    
    public ProxyID getTo() {
        return to;
    }
    
    public void setTo(ProxyID to) {
        this.to = to;
    }
    
    public BeliefID getPerformer() {
        return performer;
    }
    
    public void setPerformer(BeliefID performer) {
        this.performer = performer;
    }
    
    public String toString() { return makeID().toString(); }
}

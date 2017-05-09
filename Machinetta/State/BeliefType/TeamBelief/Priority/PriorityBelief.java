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
 * PriorityBelief.java
 *
 * Created on October 11, 2002, 4:41 PM
 */

package Machinetta.State.BeliefType.TeamBelief.Priority;

import Machinetta.State.ProxyState;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.Match.*;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief;
import Machinetta.State.BeliefID;
import Machinetta.Configuration;

import java.util.Enumeration;
import java.util.Vector;
/**
 *
 * @author  pynadath
 */
public abstract class PriorityBelief extends Belief implements Matchable {

    /** The plan with which this priority is associated */
    public BeliefID plan = null;
    
    /** State is true iff the priority value has been determined (to any extent at all)
     */
    public boolean state = false;

    /** For auto XML */
    public PriorityBelief() {}
    
    /** Creates a new instance of PriorityBelief */
    public PriorityBelief(BeliefID id) {
        this(id,null);
    }
    /** Creates a new instance of PriorityBelief
     * @param id The ID of this belief
     * @param plan The Belief ID of the team plan with which this priority is associated
     */
    public PriorityBelief(BeliefID id,BeliefID plan) {
        super(id);
        this.plan = plan;
    }
    
    /** Creates a priority belief according to the configured information level
     * @param id The Belief ID to be used with the newly created belief
     * @param plan The team plan with which the new priority is associated
     * @return A new priority belief
     */
    public static PriorityBelief createPriority(BeliefID id,BeliefID plan) {
        switch(Configuration.PLAN_PRIORITY_TYPE) {
            case BINARY:
                return new BinaryPriority(id, plan);
            case STATIC_QUANTITATIVE:
                return new StaticQuantitativePriority(id, plan);
            case DYNAMIC_QUANTITATIVE:
                return new DynamicQuantitativePriority(id, plan);
        }
        
        // Strange that compiler does not pick up that we cannot get here.
        return null;
        
        /*
        if (Configuration.INFO_LEVEL_PRIORITY.equalsIgnoreCase("binary"))
            return new BinaryPriority(id,plan);
        else if (Configuration.INFO_LEVEL_PRIORITY.equalsIgnoreCase("static quantitative"))
            return new StaticQuantitativePriority(id,plan);
        else if (Configuration.INFO_LEVEL_PRIORITY.equalsIgnoreCase("dynamic quantitative"))
            return new DynamicQuantitativePriority(id,plan);
        else
            return null;
         */
    }
    
    /** @return true iff this priority has been through an initial determination
     */
    public boolean isDetermined() { return state; }
    
    /** Default method for setting priority to some value (isDetermined now returns T)
     */
    protected void _setPriority(Object value) { state = true; }
    
    /** Abstract method for setting priority to some value
     */
    public abstract void setPriority(Object value);
    
    /** Incorporates the new beliefs into a reassessed priority value
     * (this could potentially return a new priority value, but
     * why bother when you have such a nice getPriority method?)
     */
    public void updatePriority(Belief belief) {
        /** By default, I stubbornly stick to my initial priority */
    }
    
    /** Returns a single scalar value that represents the current priority estimate
     * @return An integer whose magnitude indicates a priority along some total order
     */
    public abstract Integer getPriority();
    
    /** @return The plan with which this priority belief is associated */
    public TeamPlanBelief getPlan() { return (TeamPlanBelief)new ProxyState().getBelief(plan); }
    
    /** Generates a belief represenation according to the specified template
     * @param keys An array of attribute keys that the belief should fill in when generating the string
     * @return A string with the specified keys filled in
     */
    public String matchString(Vector keys) {
        String myStr = MatchableBelief.matchString(this,keys);
        for (Enumeration keyList = keys.elements(); keyList.hasMoreElements(); ) {
            String key = (String)keyList.nextElement();
            if (key.equalsIgnoreCase("plan"))
                    myStr += " " + plan;
            else if (key.equalsIgnoreCase("ID"))
                myStr = myStr = " " + getID();
            else if (key.equalsIgnoreCase("isPriority"))
                myStr = myStr + " isPriority";
            else if (key.equalsIgnoreCase("isDetermined"))
                myStr = myStr + " " + state;
        }
        return myStr;
    }
    
    public abstract String toString();    
}

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
 * TeamPlanBelief.java
 *
 * Created on August 22, 2002, 3:16 PM
 */

package Machinetta.State.BeliefType.TeamBelief;

import Machinetta.State.ProxyState;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefsXML;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.Match.*;
import Machinetta.State.BeliefType.TeamBelief.Priority.PriorityBelief;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.w3c.dom.*;

/**
 *
 * @author  pynadath
 */
public class TeamPlanBelief extends Belief {
    
    /** Name of the plan class */
    public String planName = null;
    
    /** Belief ID of the team executing this plan */
    public BeliefID team = null;
    
    /** A hashtable of objects that are the parameters of this particular plan instantation */
    public Hashtable<String,Object> params = null;
    
    /** A vector of roles within this team plan */
    public Vector<RoleBelief> roles = new Vector<RoleBelief>();
    
    /** A vector of constraints between the roles */
    public Vector roleConstraints = new Vector();
    
    /** Indicates whether this plan is currently active or not */
    public boolean state = false;
    
    /** Maximum length of time the plan might be expected to run, before it is reasonable to think there
     * might be a problem. <br>
     *
     * If < 0, then n/a
     */
    public int maximumReasonableCompletionTime = -1;
    
    /** The overall priority (in some form) of this plan */
    public PriorityBelief priority = null;
    
    /** @fix Need some better method for describing required schedule.
     *
     * For now, indicates that plan agent should do scheduling.
     */
    public boolean schedulable = false;
    
    /** The usual proxy state convenience */
    private transient ProxyState allBeliefs = new ProxyState();
    
    /** For auto-XML */
    public TeamPlanBelief() {}
    
    /** Creates a new instance of TeamPlanBelief
     * @param id ID for this belief
     * @param planName Identifying name for the class of the plan
     * @param team Belief about the team associated with this plan instance
     * @param state Flag indicating whether plan is active or not
     * @param params Parameters specific to this plan instantiation
     */
    public TeamPlanBelief(BeliefID id, String planName, BeliefID team, boolean state, Hashtable<String,Object> params, Vector<RoleBelief> roles) {
        super(id);
        this.planName = planName;
        this.team = team;
        this.params = params;
        /** By default, plan is inactive */
        this.state = state;
        this.roles = roles;
    }
    
    public TeamPlanBelief(BeliefID id, String planName, BeliefID team, boolean state, Hashtable<String,Object> params, Vector<RoleBelief> roles, Vector roleConstraints) {
        super(id);
        this.planName = planName;
        this.team = team;
        this.params = params;
        /** By default, plan is inactive */
        this.state = state;
        this.roles = roles;
        this.roleConstraints = roleConstraints;
    }
    
    public  String getName() { return planName; }
    
    public  BeliefID getTeam() { return team; }
    
    /** Creates a brand new priority on this plan (and adds priority belief to state)
     * @return The newly created priority belief
     */
    public PriorityBelief prioritize() {
        if (priority == null) {
            Machinetta.Debugger.debug( 2,"Creating new priority belief for " + getID());
            this.priority = PriorityBelief.createPriority(new BeliefNameID(getID().toString()+" Priority"),getID());
            if (allBeliefs == null) {
                Machinetta.Debugger.debug( 5,"Why is allBeliefs null?");
                allBeliefs = new ProxyState();
            }
            allBeliefs.addBelief(priority);
            allBeliefs.notifyListeners();
        }
        return getPriority();
    }
    
    /** Associates a pre-existing priority with this plan (does not add priority belief to state)
     * @return The newly associated priority belief
     */
    public PriorityBelief prioritize(PriorityBelief priority) {
        this.priority = priority;
        return getPriority();
    }
    
    /** Priority accessor
     * @return The priority (belief) of this plan
     */
    public PriorityBelief getPriority() {
        // I don't really like this, because if the tpb moves it may end up with an old belief,
        // however, there is a problem without it (and I don't know why! - same problem
        // of changing a belief and old links no longer work.)
        if (priority != null) {
            if (allBeliefs == null) {
                allBeliefs = new ProxyState();
            }
            PriorityBelief newPB = (PriorityBelief)allBeliefs.getBelief(priority.getID());
            if (newPB != null) priority = newPB;
        }
        return priority;
    }
    
    /** Plan state accessor
     * @return
     * True iff plan is currently active
     */
    public boolean isActive() { return state; }
    
    /** Makes plan active */
    public void activate() { state = true; }
    
    /** Makes plan inactive */
    public void deactivate() { this.deactivate(new Hashtable()); }
    
    /** Makes plan inactive */
    public void deactivate(Hashtable causes) {
        // In the future, we'll do something with the causes
        if (state) {
            state = false;
            Machinetta.Debugger.debug( 1,"Deactivating plan: "+getID());
        }
    }
    
    public int getMaxCompletionTime() { return maximumReasonableCompletionTime; }
    
    /** Access specific parameters of this plan instance
     * @param key
     * The attribute name of the desired parameter
     * @return
     * The object that is the value of the specified parameter
     */
    public  Object getParam(String key) { return params.get(key); }
    
    public void setParam(String key,Object value) { params.put(key,value); }
    
    public Enumeration getAllParamKeys() { return params.keys(); }
    
    public Vector getAllRoles() { return roles; }
    
    public Vector getPreconditions() {
        Object preconditions = getParam("Preconditions");
        if (preconditions == null)
            return new Vector();
        else
            return (Vector)preconditions;
    }
    
    public Vector getPostconditions() {
        Object postconditions = getParam("Postconditions");
        if (postconditions == null)
            return new Vector();
        else
            return (Vector)postconditions;
    }
    
    public static Hashtable staticMatchPostconditions(Hashtable beliefs, Vector postConditions, Hashtable<String,Object>params) {
        /** Go through each possible pre/post-condition to see whether any are matched in their entirety */
        for (Enumeration enumVectors = postConditions.elements(); enumVectors.hasMoreElements(); ) {
            Vector conditionList = (Vector)enumVectors.nextElement();
            Machinetta.Debugger.debug(0,"Checking against all of "+conditionList);
            /** Go through each condition and check whether there is a belief that matches it */
            Hashtable<String,Matchable> matches = new Hashtable<String,Matchable>();
            boolean matchExists = false;
            for (Enumeration enumConditions = conditionList.elements(); enumConditions.hasMoreElements(); ) {
                MatchCondition condition = (MatchCondition)enumConditions.nextElement();
                Machinetta.Debugger.debug(0,"Checking against condition "+condition);
                String label = condition.getLabel();
                if (label == null) {
                    for (Enumeration enumBeliefs=beliefs.elements(); enumBeliefs.hasMoreElements();) {
                        Matchable potentialMatch = (Matchable)enumBeliefs.nextElement();
                        Machinetta.Debugger.debug(0,"Checking belief "+potentialMatch);
                        /** Check this belief against the condition template */
                        if (MatchableBelief.matches(condition.getString(), potentialMatch,condition.inputKeys)) {
                            Machinetta.Debugger.debug(0,"Match!");
                            if (condition.getLabel() != null)
                                matches.put(condition.getLabel(),potentialMatch);
                            matchExists = true;
                        }
                    }
                } else {
                    BeliefID matchID = (BeliefID)params.get(label);
                    Matchable potentialMatch = (Matchable)(new ProxyState()).getBelief(matchID);
                    if (potentialMatch != null) {
                        /** Check this belief against the condition template */
                        Machinetta.Debugger.debug(0,"Checking belief "+potentialMatch);
                        if (MatchableBelief.matches(condition.getString(), potentialMatch,condition.inputKeys)) {
                            Machinetta.Debugger.debug(0,"Match!");
                            if (condition.getLabel() != null)
                                matches.put(condition.getLabel(),potentialMatch);
                            matchExists = true;
                        }
                    } else {
                        Machinetta.Debugger.debug( 0,"Could not match: " + matchID + " from " + label);
                    }
                }
                if (!matchExists) {
                    /** Mismatch, no need to examine any more conditions */
                    Machinetta.Debugger.debug(0,"Mismatch");
                    break;
                }
            }
            if (matchExists) {
                /** We've gone through each condition and found a matching belief */
                return matches;
            }
        }
        return null;
    }
    
    public Hashtable matchPostconditions(Hashtable beliefs) {
        /** Go through each possible pre/post-condition to see whether any are matched in their entirety */
        for (Enumeration enumVectors = getPostconditions().elements(); enumVectors.hasMoreElements(); ) {
            Vector conditionList = (Vector)enumVectors.nextElement();
            Machinetta.Debugger.debug(1,"Checking against all of "+conditionList);
            /** Go through each condition and check whether there is a belief that matches it */
            Hashtable<String,Matchable> matches = new Hashtable<String,Matchable>();
            boolean matchExists = false;
            for (Enumeration enumConditions = conditionList.elements(); enumConditions.hasMoreElements(); ) {
                MatchCondition condition = (MatchCondition)enumConditions.nextElement();
                Machinetta.Debugger.debug(1,"Checking against condition "+condition);
                String label = condition.getLabel();
                if (label == null) {
                    for (Enumeration enumBeliefs=beliefs.elements(); enumBeliefs.hasMoreElements();) {
                        Matchable potentialMatch = (Matchable)enumBeliefs.nextElement();
                        Machinetta.Debugger.debug(1,"Checking belief "+potentialMatch);
                        /** Check this belief against the condition template */
                        if (MatchableBelief.matches(condition.getString(), potentialMatch,condition.inputKeys)) {
                            Machinetta.Debugger.debug(1,"Match!");
                            if (condition.getLabel() != null)
                                matches.put(condition.getLabel(),potentialMatch);
                            matchExists = true;
                        }
                    }
                } else {
                    BeliefID matchID = (BeliefID)getParam(label);
                    Matchable potentialMatch = (Matchable)allBeliefs.getBelief(matchID);
                    if (potentialMatch != null) {
                        /** Check this belief against the condition template */
                        if (MatchableBelief.matches(condition.getString(), potentialMatch,condition.inputKeys)) {
                            Machinetta.Debugger.debug(1,"Match!2");
                            if (condition.getLabel() != null)
                                matches.put(condition.getLabel(),potentialMatch);
                            matchExists = true;
                        }
                    } else {
                        Machinetta.Debugger.debug(1,"Nothing for " + matchID);
                    }
                }
                if (!matchExists) {
                    /** Mismatch, no need to examine any more conditions */
                    Machinetta.Debugger.debug(1,"Mismatch");
                    break;
                }
            }
            if (matchExists) {
                /** We've gone through each condition and found a matching belief */
		Machinetta.Debugger.debug(1,"Found matching belief(s)="+matches);
                return matches;
            }
        }
	Machinetta.Debugger.debug(0,"No matching belief(s) found");
        return null;
    }
    
    public String toString() {
        String result = super.toString();
        if (state)
            result = result + "(+";
        else
            result = result + "(-";
        result = result + planName + ")";
        result = result + "@" + team;
        result += roles;
        if (params.isEmpty())
            return result;
        else
            return result + params.toString();
    }
    
    public boolean matches(String matchStr, String[] keys) {
        String myStr = planName;
        for (int keyIndex=0; keyIndex < keys.length; keyIndex++) {
            String key = keys[keyIndex];
            if (key.equalsIgnoreCase("team"))
                myStr = myStr + " : " + team;
            else
                myStr = myStr + " : " + (String)getParam(key);
        }
        return myStr.equalsIgnoreCase(matchStr);
    }
    
    public BeliefID makeID() {
        Machinetta.Debugger.debug( 5,"Calling makeID on TeamPlanBelief is dangerous ... ");
        return new BeliefNameID("TeamPlan"+planName+team.toString());
    }
    
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) return false;
        TeamPlanBelief tp = (TeamPlanBelief)obj;
        if (tp.planName.equalsIgnoreCase(planName)) {
            for (Enumeration e = params.keys(); e.hasMoreElements(); ) {
                String key = (String) e.nextElement();
                if (tp.getParam(key) != getParam(key)) {
                    Machinetta.Debugger.debug( 1,"Plans " + tp + " and " + this + " do not match because " + key + " is different");
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }
    
    public static final long serialVersionUID = 1L;
}

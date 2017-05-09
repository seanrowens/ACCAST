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
 * TeamPlanTemplate.java
 *
 * Created on October 2, 2002, 4:48 PM
 */

package Machinetta.State.BeliefType.TeamBelief;

import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.TeamBelief.Constraints.DirectedInformationRequirement;
import Machinetta.State.BeliefsXML;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.Match.*;
import Machinetta.State.BeliefType.TeamBelief.Constraints.*;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Attr;

/**
 *
 * @author  pynadath
 */
public class TeamPlanTemplate extends Belief {
    
    /** Name of the plan class */
    public String planName = null;
    
    /** Belief ID of the team executing this plan */
    public BeliefID team = null;
    
    /** Table of default parameter values for this template (inherited by all instances) */
    public Hashtable<String,Object> params = null;
    
    /** Lists of conditions that trigger initiation/termination of this plan class */
    public Vector preconditions = null;
    public Vector postconditions = null;
    
    /** List of roles to instantiate with the plan */
    public Vector<RoleBelief> roles = null;
    
    /** Constraints between roles in the plan */
    public Vector roleConstraints = null;
    
    /** Lookup table to facilitate finding (pre)conditions */
    public Hashtable<String,MatchCondition> conditionTable = null;
    
    /** Maximum length of time the plan might be expected to run, before it is reasonable to think there
     * might be a problem. <br>
     *
     * If < 0, then n/a
     */
    public int maximumReasonableCompletionTime = -1;
    
    /** For auto XML */
    public TeamPlanTemplate() {
        preconditions = new Vector();
        postconditions = new Vector();
        params = new Hashtable<String,Object>();
    }    
    
    /** Creates a new instance of TeamPlanTemplate
     * @param planName
     * Identifying name for the class of the plan
     * @param team
     * Belief about the team associated with this plan template
     * @param keys
     * Keys indicating template slots to be filled in upon instantiation
     * @param params
     * Parameters specific to this plan instantiation
     */
    public TeamPlanTemplate(String planName,BeliefID team,Hashtable<String,Object> params,Vector preconditions,Vector postconditions,Vector<RoleBelief> roles) {
        this(new BeliefNameID("Plan Template "+planName),planName,team,params,preconditions,postconditions,roles);
    }
    public TeamPlanTemplate(String planName,BeliefID team,Hashtable<String,Object> params,Vector preconditions,Vector postconditions,Vector<RoleBelief> roles,Vector constraints) {
        this(new BeliefNameID("Plan Template "+planName),planName,team,params,preconditions,postconditions,roles,constraints);
    }
    /** Creates a new instance of TeamPlanTemplate
     * @param id
     * ID for this belief
     * @param planName
     * Identifying name for the class of the plan
     * @param team
     * Belief about the team associated with this plan template
     * @param keys
     * Keys indicating template slots to be filled in upon instantiation
     * @param params
     * Parameters specific to this plan instantiation
     */
    public TeamPlanTemplate(BeliefID id, String planName,BeliefID team,Hashtable<String,Object> params,Vector preconditions,Vector postconditions,Vector roles) {
        this(id, planName, team, params, preconditions, postconditions, roles, new Vector());
    }
    
    public TeamPlanTemplate(BeliefID id, String planName,BeliefID team,Hashtable<String,Object> params,Vector preconditions,Vector postconditions,Vector roles,Vector roleConstraints) {
        super(id);
        this.planName = planName;
        this.team = team;
        this.params = params;
        this.preconditions = preconditions;
        this.roles = roles;
        this.roleConstraints = roleConstraints;
        
        createCondTable();
        this.postconditions = postconditions;
    }
    
    /** Store the preconditions in a lookup table for easy access */
    private void createCondTable () {
        conditionTable = new Hashtable<String,MatchCondition>();
        for (Enumeration preList = preconditions.elements(); preList.hasMoreElements(); ) {
            /** Each precondition is a list of conditions */
            Vector conditions = (Vector)preList.nextElement();
            for (Enumeration conditionList = conditions.elements(); conditionList.hasMoreElements(); ) {
                MatchCondition condition = (MatchCondition)conditionList.nextElement();
                if (condition.getLabel() != null)
                    conditionTable.put(condition.getLabel(),condition);
            }
        }
    }
    
    /** Determines whether a set of beliefs should trigger instantiation of this template
     * @param beliefs A hashtable of potential triggers
     * @return A vector containing all possible hash tables containing a set of matching triggers
     */
    public Vector<Hashtable<String,Matchable>> matchPreconditions(Hashtable beliefs) {
        Vector conditions = getPreconditions();
        Vector<Hashtable<String,Matchable>> matches = new Vector<Hashtable<String,Matchable>>();
        Machinetta.Debugger.debug( 0,"Plan has " + conditions.size() + " preconditions: " + conditions);
        /** Go through each possible pre/post-condition to see whether any are matched in their entirety */
        for (Enumeration enumVectors = conditions.elements(); enumVectors.hasMoreElements(); ) {
            Vector conditionList = (Vector)enumVectors.nextElement();
            Machinetta.Debugger.debug(0,"Checking against all of "+conditionList);
            /** Go through each condition and check whether there is a belief that matches it */
            Vector<Hashtable<String,Matchable>> currentMatch = new Vector<Hashtable<String,Matchable>>();
            currentMatch.add(new Hashtable<String,Matchable>());
            currentMatch = generateMatches(beliefs,currentMatch,conditionList.elements());
            matches.addAll(currentMatch);
        }
        Machinetta.Debugger.debug( 0,"Returning " + matches.size() + " matches");
        return matches;
    }
    
    @SuppressWarnings("unchecked")
    public Vector<Hashtable<String,Matchable>> generateMatches(Hashtable beliefs,Vector<Hashtable<String,Matchable>> matchList,Enumeration conditions) {
        if (conditions.hasMoreElements()) {
            MatchCondition condition = (MatchCondition)conditions.nextElement();
            Machinetta.Debugger.debug(0,"Checking " + beliefs.size() + " matchables against condition "+condition);
            Vector<Matchable> matches = new Vector<Matchable>();
            /** Find all beliefs that match this particular condition */
            for (Enumeration enumBeliefs=beliefs.elements(); enumBeliefs.hasMoreElements();) {
                Matchable potentialMatch = (Matchable)enumBeliefs.nextElement();
                Machinetta.Debugger.debug(0,"Checking belief "+potentialMatch);
                if (MatchableBelief.matches(condition.getString(), potentialMatch,condition.inputKeys)) {
                    /** Match, no need to examine any more beliefs */
                    Machinetta.Debugger.debug(0,"Match!");
                    matches.add(potentialMatch);
                } else {
                    Machinetta.Debugger.debug(0,"No match");
                }
            }
            /** Check whether any matches were found... */
            if (matches.size() == 0) {
                /** No matches were found for this condition, so give up */
                Machinetta.Debugger.debug(0,"Mismatch");
                return new Vector<Hashtable<String,Matchable>>();
            }
            Vector<Hashtable<String,Matchable>> newMatches = matchList;
            /** Check whether we have to save the matches in our vector of hashtables */
            if (condition.getLabel() != null) {
                /** Combine current list of hashtable matches with these new matches */
                newMatches = new Vector<Hashtable<String,Matchable>>();
                for (Enumeration<Hashtable<String,Matchable>> oldMatches = matchList.elements(); oldMatches.hasMoreElements(); ) {
                    Hashtable<String,Matchable> oldMatch = (Hashtable<String,Matchable>)oldMatches.nextElement();
                    for (Enumeration newEntries = matches.elements(); newEntries.hasMoreElements(); ) {
                        Matchable newEntry = (Matchable)newEntries.nextElement();
                        Hashtable<String,Matchable> newMatch = (Hashtable<String,Matchable>)(oldMatch.clone());
                        newMatch.put(condition.getLabel(),newEntry);
                        newMatches.add(newMatch);
                    }
                }
            }
            return generateMatches(beliefs,newMatches,conditions);
        } else {
            Machinetta.Debugger.debug( 0,"Conditions had no more elements!");
            return matchList;
        }
    }
    
    /** Creates instance of plan and instances of roles within that plan. */
    @SuppressWarnings("unchecked")
    public TeamPlanBelief instantiatePlan(Hashtable<String,Matchable> triggers) {
        if (conditionTable == null) createCondTable();
        Hashtable<String,Object> newParams = (Hashtable<String,Object>)(params.clone()); // Plans need IDs
	Hashtable<String,Matchable> roleParams = new Hashtable<String,Matchable>(); // Roles need actual objects
        newParams.put("Preconditions", preconditions);
        newParams.put("Postconditions",postconditions);
        // Grab all of the specifics of the match that has triggered the instantiation 
        for (Enumeration keyList = triggers.keys(); keyList.hasMoreElements(); ) {
            String label = (String)keyList.nextElement();
            Matchable match = (Matchable)triggers.get(label);
            MatchCondition condition = (MatchCondition)conditionTable.get(label);
            // Store this match within the parameters of the soon-to-be-instantiated plan 
            newParams.put(label,((Belief)match).getID());
	    roleParams.put(label,match);
        }
        // Find all the roles (templates actually) and instantiate them 
        Vector<RoleBelief> newRoles = new Vector<RoleBelief>(); 
        Vector<RoleConstraint> newConstraints = new Vector<RoleConstraint>();
        try {
            // Instantiate the roles
            for (Enumeration allRoles = roles.elements(); allRoles.hasMoreElements(); ) {
                RoleBelief roleTemplate = (RoleBelief)allRoles.nextElement();
                Machinetta.Debugger.debug(1, "Really Instantiating role: "+roleTemplate + " with id " + roleTemplate.getID() + " and params " + roleParams);
                RoleBelief roleInstance = roleTemplate.instantiate(roleParams);
                Machinetta.Debugger.debug(1, "Instantiating role done: "+roleInstance);
                newRoles.addElement(roleInstance);
            }
                                
            
            //
            //
            // Problem here that generic role definitions are not properly instantiated to specific role definitions.
            //
            //            
            
            // Now instantiate any DirectedInformationRequirements on these roles
            // Assumes newRoles and roles have roles in the same order ... 
            for (RoleBelief roleInst: newRoles) {
                if (roleInst.infoSharing != null) {
                    Vector<DirectedInformationRequirement> ninfoShare = new Vector<DirectedInformationRequirement>(roleInst.infoSharing.size());
                    for (DirectedInformationRequirement dir: roleInst.infoSharing) {
                        DirectedInformationRequirement ndir = dir.clone();
                        if (ndir.performer != null) {
                            boolean found = false;
                            int index = 0;
                            for (; !found && index < roles.size(); index++) {
                                found = roles.get(index).id.equals(ndir.performer);                                
                            }
                            if (found) {                                
                                ndir.performer = newRoles.get(--index).getID();
                                Machinetta.Debugger.debug(3, "Instantiated dir: " + dir + " -> " + ndir);
                            } else {
                                Machinetta.Debugger.debug(4, "Instantiated DIR failed, could not find old performer: " + ndir.performer + " choices: " + roles);
                            }
                        }
                        ninfoShare.add(ndir);
                    }
                    roleInst.infoSharing = ninfoShare;
                }
            }
            
            
            // @fix Need to do instantiated for GeneratedInformationRequirement
            
            // Need to change the references to templates in the constraints to match the instances
            for (Enumeration allConstraints = roleConstraints.elements(); allConstraints.hasMoreElements(); ) {
                RoleConstraint rc = (RoleConstraint)allConstraints.nextElement();
                Machinetta.Debugger.debug( 0,"Instantiating constraint: " + rc);
                RoleConstraint nrc = rc.instantiate(roles, newRoles);
                newConstraints.addElement(nrc);
            }
        } catch (NullPointerException e) {
            Machinetta.Debugger.debug( 5,"Plan " + planName + " has no roles!!");
        } 
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID(instanceName(triggers)),planName,team,true,newParams,newRoles,newConstraints);
        tpb.maximumReasonableCompletionTime = maximumReasonableCompletionTime;
        return tpb;
    }
    
    /** Generates the name of the plan instantiated by the specified precondition match
     * @param triggers A hashtable of Matchable beliefs that triggered the instantiation
     * @return The string name (that shows up in the Belief ID) of the instnatiated plan
     */
    public String instanceName(Hashtable triggers) {
        if (conditionTable == null) createCondTable();
        String instanceName = planName;
        for (Enumeration keyList = triggers.keys(); keyList.hasMoreElements(); ) {
            String label = (String)keyList.nextElement();
            Matchable match = (Matchable)triggers.get(label);
            MatchCondition condition = (MatchCondition)conditionTable.get(label);
            /** The template specifies extracting this particular trigger */
            instanceName = instanceName + " " + match.matchString(condition.outputKeys);
        }
        return instanceName;
    }
    
    public  String getName() { return planName; }
    
    public  BeliefID getTeam() { return team; }
    
    /** Access specific parameters of this plan template
     * @param key
     * The attribute name of the desired parameter
     * @return
     * The object that is the value of the specified parameter
     */
    public  Object getParam(String key) {
        return params.get(key);
    }
    
    public Vector getPreconditions() { return preconditions; }
    public Vector getPostconditions() { return postconditions; }
        
    public String toString() {
        String result = super.toString();
        result = result + "(" + planName + ")";
        result = result + "@" + team;
        if (preconditions.size() > 0)
            result = result + " PRE: " + preconditions.toString();
        if (postconditions.size() > 0)
            result = result + " POST: " + postconditions.toString();
        if (params.isEmpty())
            return result;
        else
            return result + params.toString();
    }
        
    public BeliefID makeID() {
        return new Machinetta.State.BeliefNameID("PlanTemplate"+planName+team);
    }    
    
    public static final long serialVersionUID = 1L;
}

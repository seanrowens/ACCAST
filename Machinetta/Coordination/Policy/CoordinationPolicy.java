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
 * CoordinationPolicy.java
 *
 * Created on August 29, 2002, 2:14 PM
 */

package Machinetta.Coordination.Policy;

import Machinetta.Communication.Message;
import Machinetta.Communication.KQMLMessage;
import Machinetta.Communication.TextMessage;
import Machinetta.State.ProxyState;
import Machinetta.State.BeliefsXML;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.RAPBelief;
import Machinetta.State.BeliefType.Match.*;
import Machinetta.State.BeliefType.TeamBelief.Priority.PriorityBelief;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanTemplate;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import Machinetta.State.BeliefType.TeamBelief.RoleAllocationBelief;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import org.w3c.dom.Document;

/**
 *
 * @author  pynadath
 */
public abstract class CoordinationPolicy {
    
    /** Creates a new instance of CoordinationPolicy */
    public CoordinationPolicy() {
        /** We have default policies that update state relevant to teams and team plans */
        addHandler("Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief",new TeamPlanBeliefHandler());
        addHandler("Machinetta.State.BeliefType.TeamBelief.TeamPlanTemplate",new TeamPlanBeliefHandler());
        addHandler("Machinetta.State.BeliefType.TeamBelief.TeamBelief",new TeamBeliefHandler());
        /** This handler adds RoleAllocationBeliefs for any unassigned RoleBeliefs...
         * this is handled by the role-allocation reasoning */
        addHandler("Machinetta.State.BeliefType.TeamBelief.RoleBelief",new RoleBeliefHandler());
        addHandler("Machinetta.State.BeliefType.Match.Matchable",new MatchableHandler());
    }
    
    /** Processes a belief and returns a set of actions to perform in response
     * @param belief The new (or modified) belief that has triggered the policy
     * @param isCoordination Flag that's true iff belief was triggered by coordination
     * @return A list of actions (e.g., messages, new beliefs)
     */
    public Vector processBelief(Belief belief,boolean isCoordination) {
        Vector result = new Vector();
        for (Enumeration e = table.keys(); e.hasMoreElements(); ) {
            Class keyClass = (Class)e.nextElement();
            if (keyClass.isInstance(belief)) {
                BeliefHandler handler = (BeliefHandler)table.get(keyClass);
                Machinetta.Debugger.debug(0,"Invoking handler for "+keyClass.getName());
                result.addAll(handler.handleBelief(belief,isCoordination));
            }
        }
        return result;
    }
    
    /** Processes an incoming message and returns a set of actions to perform in response
     * @param message The incoming message
     * @return A list of actions (e.g., messages, new beliefs)
     */
    public Vector interpretMessage(Message message) {
        Vector actions = new Vector();
        if (message instanceof KQMLMessage) {
            // For now, treat all of these messages as belief messages
            String content = ((KQMLMessage)message).get(":content");
            content = content.substring(1,content.length()-1);
            actions.addAll(interpretMessage(content));
        } else if (message instanceof TextMessage) {
            String content = ((TextMessage)message).getText();
            actions.addAll(interpretMessage(content));
        } else {
            // Some other kind of message that I should be able to handle,
            //  but I ashamedly must admit that I can't
            Machinetta.Debugger.debug(3,"Unable to interpret message of type: "+message.getClass().getName()+ " content: " + message);
        }
        return actions;
    }
    
    /** Processes an incoming message string and returns a set of actions to perform in response
     * @param message The incoming message string
     * @return A list of actions (e.g., messages, new beliefs)
     */
    public Vector interpretMessage(String message) {
        Document doc = BeliefsXML.getDocumentFromString(message);
        if (doc == null)
            return new Vector();
        else
            return interpretMessage(doc);
    }
    
    /** Processes an incoming message document and returns a set of actions to perform in response
     * @param message The incoming message document
     * @return A list of actions (e.g., messages, new beliefs)
     */
    public Vector interpretMessage(Document message) {
        Vector actions = new Vector();
        // Extract beliefs from XML document and add them to ProxyState
        Belief newBeliefs [] = BeliefsXML.getBeliefs(message);
        for (int beliefIndex=0; beliefIndex < newBeliefs.length; beliefIndex++) {
            Belief newBelief = newBeliefs[beliefIndex];
            Machinetta.Debugger.debug(0,"New belief "+newBelief);
            actions.addAll(interpretMessage(newBelief));
        }
        return actions;
    }
    
    /** Processes an incoming message belief and returns a set of actions to perform in response
     * @param message The incoming message belief
     * @return A list of actions (e.g., messages, new beliefs)
     */
    public Vector interpretMessage(Belief message) {
        Vector actions = new Vector();
        actions.add(message);
        // Apply handlers to this new belief
        actions.addAll(processBelief(message,true));
        return actions;
    }
    
    /** Registers a new belief handler */
    protected void addHandler(String className,BeliefHandler handler) {
        try { table.put(Class.forName(className),handler); }
        catch (ClassNotFoundException e) { Machinetta.Debugger.debug(2,e.toString()); }
    }
    
    /** Default class that "handles" beliefs by doing nothing */
    protected class BeliefHandler {
        public Vector handleBelief(Belief belief,boolean isCoordination) {
            Machinetta.Debugger.debug(0,"Invoking default behavior");
            return new Vector();
        }
    }
    
    /** Role belief handler that adds a new role allocation belief when unassigned */
    protected class RoleBeliefHandler extends BeliefHandler {
        public Vector handleBelief(Belief belief,boolean isCoordination) {
            Machinetta.Debugger.debug(0,"Processing " + belief);
            Vector actions = new Vector();                        
            
            return actions;
        }
    }
    
    /** Default team belief handler that updates our list of known teams */
    protected class TeamBeliefHandler extends BeliefHandler {
        public Vector handleBelief(Belief belief,boolean isCoordination) {
            // Update list of teams if appropriate
            BeliefID teamID = belief.getID();
            if (!teams.contains(teamID)) {
                Machinetta.Debugger.debug(1,"New team: "+belief);
                teams.add(teamID);
            }
            return new Vector();
        }
    }
    
    /** Default team plan belief handler that updates our list of known team plans */
    protected class TeamPlanBeliefHandler extends BeliefHandler {
        public Vector handleBelief(Belief belief,boolean isCoordination) {
            // Update list of plans if appropriate
            BeliefID planID = belief.getID();
            if (!plans.contains(planID)) {
                Machinetta.Debugger.debug(2,"New plan/template: "+planID);
                plans.add(planID);
            }
            return new Vector();
        }
    }
    
    /** Default belief handler that matches new belief against known conditions */
    protected class MatchableHandler extends BeliefHandler {
        public Vector handleBelief(Belief belief,boolean isCoordination) {
            Vector result = new Vector();
            /** Store any newly created plans for later addition to our "currently known plans" list */
            Vector newPlans = new Vector();
            Matchable matchBelief = (Matchable)belief;
            /** Store this trigger for matching against plan conditions */
            triggers.put(belief.getID(),matchBelief);
            Machinetta.Debugger.debug(0,"Current triggers "+triggers);
            for (Enumeration enumPlans = plans.elements(); enumPlans.hasMoreElements(); ) {
                /** Check whether any known plans have pre/post-conditions matched */
                BeliefID planID = (BeliefID)enumPlans.nextElement();
                Belief genericPlan = (Belief)state.getBelief(planID);
                Machinetta.Debugger.debug(0,"Checking against plan "+genericPlan);
                if (genericPlan instanceof TeamPlanBelief) {
                    /** We check postconditions of active plans */
                    TeamPlanBelief plan = (TeamPlanBelief)genericPlan;
                    if (plan.isActive()) {
                        Hashtable matches = plan.matchPostconditions(triggers);
                        if (matches != null) {
                            /** We match the post-conditions of this plan, so deactivate plan */
                            Machinetta.Debugger.debug(1,"Postconditions match for "+plan);
                            result.add(plan);
                            result.addAll(terminatePlan(plan,matches));
                        }
                    } else {
                        /** Plan is already inactive, so don't check postconditions
                         * (maybe we should check preconditions?)
                         */
                    }
                } else if (genericPlan instanceof TeamPlanTemplate) {
                    /** Must be instance of TeamPlanTemplate, so check preconditions */
                    TeamPlanTemplate template = (TeamPlanTemplate)genericPlan;
                    Vector matchList = template.matchPreconditions(triggers);
                    for (Enumeration matchEnum = matchList.elements(); matchEnum.hasMoreElements(); ) {
                        Hashtable matches = (Hashtable)matchEnum.nextElement();
                        /** We match the pre-conditions of this plan, so first
                         * check whether a plan instance already exists for these triggers */
                        Machinetta.Debugger.debug(0,"Preconditions match for "+template);
                        Machinetta.Debugger.debug(0,"Triggers are: "+matches);
                        BeliefNameID newPlanID = new BeliefNameID(template.instanceName(matches));
                        Machinetta.Debugger.debug(0,"Instantiating new plan: "+newPlanID);
                        if (state.getBelief(newPlanID) == null && !newPlans.contains(newPlanID)) {
                            /** Don't have a plan instance yet, so instantiate one now */
                            TeamPlanBelief newPlan = template.instantiatePlan(matches);
                            Machinetta.Debugger.debug(1,"New plan: "+newPlan + " with ID: " + newPlan.getID());
                            result.add(newPlan);
                            
                            // This is the piece we are going to turn into a role ...
                            result.addAll(initiatePlan(newPlan,matches));
                            
                            /** Store for later addition to list of known plans */
                            newPlans.add(newPlan.getID());
                        } else {
                            Machinetta.Debugger.debug(0,"Plan already exists: "+newPlanID);
                        }
                    }
                } else {
                    Machinetta.Debugger.debug(1,"Unknown plan type: "+genericPlan);
                }
            }
            // Add plan to list of known plans
            plans.addAll(newPlans);
            return result;
        }
    }
    
    /** Method for handling the initiation of a new plan and generate any necessary actions
     * @param plan The new plan
     * @param triggers The conditions which triggered the initiation of this plan
     * @return A vector of actions to perform in response to this initiation
     */
    public Vector initiatePlan(TeamPlanBelief plan,Hashtable triggers) {
        Vector result = new Vector();
        // Add associated role beliefs as well
        Vector roleList = new Vector();
        for (Enumeration allRoles = plan.getAllRoles().elements(); allRoles.hasMoreElements(); ) {
            RoleBelief role = (RoleBelief)allRoles.nextElement();
            /** Add role only if there isn't one there already */
            if (state.getBelief(role.getID()) == null) {
                Machinetta.Debugger.debug( 0,"Adding role: "+role);
                roleList.add(role);
                role.setPlan(plan);
                result.add(role);
            } else {
                // Should we hook up this pre-existing role in some way?
            }
        }
        
        // Check whether there are any necessary role allocations
        for (Enumeration allRoles = roleList.elements(); allRoles.hasMoreElements(); ) {
            RoleBelief newRole = (RoleBelief)allRoles.nextElement();
            Machinetta.Debugger.debug( 1,"Checking responsibility on new role: "+newRole.getID() + "( " + newRole + " )");
            if (newRole.getResponsible() == null && state.getBelief(RoleAllocationBelief.makeID(newRole)) == null) {
                RoleAllocationBelief newAllocation = new RoleAllocationBelief(newRole);
                Machinetta.Debugger.debug( 1,"Creating new role allocation: "+newAllocation.getID());
                result.add(newAllocation);
            }
        }
        
        /** Add any newly generated priority belief */
        PriorityBelief priority = plan.getPriority();
        if (priority != null)
            result.add(priority);
        return result;
    }
    
    /** Method for handling the termination of a plan and generate any necessary actions
     * @param plan The completed plan
     * @param triggers The conditions which triggered the termination of this plan
     * @return A vector of actions to perform in response to this termination
     */
    public Vector terminatePlan(TeamPlanBelief plan,Hashtable triggers) {
        Vector actions = new Vector();
        plan.deactivate(triggers);
        /** Check whether there are any roles to remove from their assigned RAPs*/
        for (Enumeration allRoles = plan.getAllRoles().elements(); allRoles.hasMoreElements(); ) {
            RoleBelief role = (RoleBelief)allRoles.nextElement();
            RAPBelief responsible = role.getResponsible();
            if (responsible != null)
                responsible.removeRole(role);
            role.deactivate();
            actions.add(role);
        }
        return actions;
    }
    
    /** Proxy State object that policies can use to access beliefs.
     */
    protected ProxyState state = new ProxyState();
    /** List of known teams */
    protected Vector teams = new Vector();
    
    /** List of known team plans */
    protected Vector plans = new Vector();
    
    /** List of known possible plan initiation/termination triggers (indexed by BeliefID) */
    protected Hashtable triggers = new Hashtable();
    
    /** Default handler that we will use many times */
    private BeliefHandler defaultHandler = new BeliefHandler();
    
    /** Poorly named table of registered belief handlers */
    protected Hashtable table = new Hashtable();
}

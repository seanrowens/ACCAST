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
 * PlanAgentFactory.java
 *
 * Created on November 6, 2003, 11:39 AM
 */

package Machinetta.Coordination.MAC;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.Match.Matchable;

import java.util.*;
import java.lang.reflect.*;

/**
 *
 * @author  pscerri
 */
public class PlanAgentFactory extends AgentFactory {
    
    private java.util.Hashtable<BeliefID,Belief> templates = new java.util.Hashtable<BeliefID,Belief>();
    private static java.util.Hashtable<BeliefID,Belief> triggers = new java.util.Hashtable<BeliefID,Belief>();
    
    private Constructor planAgentConstructor = null, planAgentReconstructor = null;
    
    /** Creates a new instance of PlanAgentFactory */
    public PlanAgentFactory() {
        try {
            Class planAgentClass = Class.forName(Machinetta.Configuration.PLAN_AGENT_CLASS);
            
            // Create the constructor object for creating plan agents from scratch
            Class [] constructorParams = new Class[1];
            constructorParams[0] = Class.forName("Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief");
            planAgentConstructor = planAgentClass.getConstructor(constructorParams);
            
            // Create the constructor object for creating plan agent from PlanAgent Beliefs
            constructorParams[0] = Class.forName("Machinetta.State.BeliefType.MAC.PlanAgentBelief");
            planAgentReconstructor = planAgentClass.getConstructor(constructorParams);
            
        } catch (ClassNotFoundException e1) {
            Machinetta.Debugger.debug( 5,"Exiting: Could not find plan agent class:" + Machinetta.Configuration.PLAN_AGENT_CLASS);
            System.exit(1);
        } catch (NoSuchMethodException e2) {
            Machinetta.Debugger.debug( 5,"Exiting: Specified Plan Agent Class has no constructor for TeamPlanBelief");
            System.exit(1);
        }
    }
    
    /**
     * If a new planAgent arrives, add to agents if agent not already there.
     * Add belief to state. <p>
     *
     * RoleAgent is more complicated ... if role has no responsibleRAP then
     * agent is here to find owner and RoleAllocation should be created.
     * However, if there if the role is already filled then this agent should "die".<br>
     * If responsibleRAP is set, then agent is informing and looking for conflicts. <p>
     *
     * See PlanAgent and RoleAgent for details.
     */
    public void createAgent(Machinetta.State.BeliefType.MAC.MABelief bel) {
        if (bel instanceof Machinetta.State.BeliefType.MAC.PlanAgentBelief) {
            // Below is untested, since at time of writing, plan agents did not move
            createAgent((Machinetta.State.BeliefType.MAC.PlanAgentBelief)bel);
        } else if (bel instanceof Machinetta.State.BeliefType.MAC.RoleAgentBelief) {
            RoleAgent a = new RoleAgent((Machinetta.State.BeliefType.MAC.RoleAgentBelief)bel, true);
        } else if (bel instanceof Machinetta.State.BeliefType.MAC.AssociateInformBelief) {
            AssociateInformAgent a = new AssociateInformAgent((Machinetta.State.BeliefType.MAC.AssociateInformBelief)bel);
        } else if (bel instanceof Machinetta.State.BeliefType.MAC.ConflictResolutionBelief) {
            ConflictResolutionAgent a = new ConflictResolutionAgent((Machinetta.State.BeliefType.MAC.ConflictResolutionBelief)bel);
        } else if (bel instanceof Machinetta.State.BeliefType.MAC.JIAgentBelief) {
            JIAgent a = new JIAgent((Machinetta.State.BeliefType.MAC.JIAgentBelief)bel);
        } else {
            Machinetta.Debugger.debug( 3,"Plan factory asked to create unknown agent type: " + bel);
        }
    }
    
    
    public void stateChange(Machinetta.State.BeliefID[] b) {
    
        // Instantiate plans only if this proxy has the authority.
        if (!Machinetta.Configuration.PLAN_INSTANTIATION_POLICY.equalsIgnoreCase("NONE")) {
            for (int i = 0; i < b.length; i++) {
                Machinetta.State.BeliefType.Belief bel = state.getBelief(b[i]);
                if (bel instanceof Machinetta.State.BeliefType.Match.Matchable) triggers.put(b[i], bel);
                // else Machinetta.Debugger.debug( 1,bel + " is not Matchable");
                if (bel instanceof Machinetta.State.BeliefType.TeamBelief.TeamPlanTemplate) {
                    Machinetta.Debugger.debug( 1,"Adding template to PlanAgentFactory: " + bel);
                    templates.put(bel.getID(), bel);
                }
            }
            for (java.util.Enumeration e = templates.elements(); e.hasMoreElements(); ) {
                Machinetta.State.BeliefType.TeamBelief.TeamPlanTemplate template = (Machinetta.State.BeliefType.TeamBelief.TeamPlanTemplate)e.nextElement();
                
                Machinetta.Debugger.debug( 0,"Matching against: " + template);
                Vector<Hashtable<String,Matchable>> matchList = template.matchPreconditions(triggers);
                for (Enumeration<Hashtable<String,Matchable>> matchEnum = matchList.elements(); matchEnum.hasMoreElements(); ) {
                    Hashtable<String,Matchable> matches = (Hashtable<String,Matchable>)matchEnum.nextElement();
                    
                    /** We match the pre-conditions of this plan, so first
                     * check whether a plan instance already exists for these triggers */
                    Machinetta.Debugger.debug(0,"Preconditions match for "+template);
                    Machinetta.Debugger.debug(0,"Triggers are: "+matches);
                    Machinetta.State.BeliefNameID newPlanID = new Machinetta.State.BeliefNameID(template.instanceName(matches));
                    
                    if (state.getBelief(newPlanID) == null) {
                        // Don't have a plan instance yet, so instantiate one now
                        // Notice that the Plan agent will be responsible for putting in state and
                        // for instantiating, if required.
                        Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief newPlan = template.instantiatePlan(matches);
                        Machinetta.Debugger.debug(1,"New plan created locally: "+newPlan + " with ID: " + newPlan.getID() + " using " + matches);
                        
                        // Now create a PlanAgent
                        Agent a = createAgent(newPlan);
                        //MACoordination.addAgent(a);
                    } else {
                        Machinetta.Debugger.debug(0,"Plan already exists: "+newPlanID);
                    }
                }
            }
        }
    }
    
    /**
     * Creates whichever type of plan agent is specified in the config file.
     */
    private Agent createAgent(Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief newPlan) {
        Object [] args = new Object[1];
        args[0] = newPlan;
        try {
            return (Agent) planAgentConstructor.newInstance(args);
        } catch (InstantiationException e1) {
            Machinetta.Debugger.debug( 5,e1 + ", could not create plan agent for : " + newPlan);
        } catch (IllegalAccessException e2) {
            Machinetta.Debugger.debug( 5,e2 + ", could not create plan agent for : " + newPlan);
        } catch (InvocationTargetException e3) {
            Machinetta.Debugger.debug( 5,e3 + ": " + e3.getCause() + " meant could not create plan agent for : " + newPlan);
        }
        
        return null;
        //return new PlanAgent(newPlan);
    }
    
    public void createAgent(Machinetta.State.BeliefType.MAC.PlanAgentBelief bel) {
        Object [] args = new Object[1];
        args[0] = bel;
        try {
            planAgentReconstructor.newInstance(args);
        } catch (InstantiationException e1) {
            Machinetta.Debugger.debug( 4,e1 + ", could not create plan agent for : " + bel);
        } catch (IllegalAccessException e2) {
            Machinetta.Debugger.debug( 4,e2 + ", could not create plan agent for : " + bel);
        } catch (InvocationTargetException e3) {
            Machinetta.Debugger.debug( 4,e3 + ": " + e3.getCause() + " meant could not create plan agent for : " + bel);
        }
    }
    
    public static java.util.Hashtable<BeliefID,Belief> getTriggers() { return triggers; }
}

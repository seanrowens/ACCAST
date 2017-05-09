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
 * MetaReasoningAgent.java
 *
 * Created on May 24, 2004, 5:33 PM
 */

package Machinetta.Coordination.MAC;

import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.MAC.*;
import Machinetta.State.BeliefType.*;
import Machinetta.State.BeliefType.TeamBelief.Priority.*;
import Machinetta.State.ProxyState;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.TeamBelief.DecisionMakingRole;
import Machinetta.Coordination.MAC.*;
import Machinetta.Coordination.MACoordination;
import java.util.*;

/**
 * Potentially, get rid of the "agent" variable all together and keep only as a belief.
 *
 * Sub-classing could be used for different meta-reasoning decisions, currently this only
 * does missing role allocations.
 *
 * @author  pscerri
 */
public class MetaReasoningAgent extends Agent {
    
    /** If this is not null, then this is meta-reasoning for some other agent */
    private Agent agent = null;
    private MABelief agentB = null;
    
    public Machinetta.State.BeliefID[] ids = new Machinetta.State.BeliefID[1];
    
    /** True iff the meta reasoning agent should move the underlying agent when it moves. <br>
     *
     * RoleAllocation meta-reasoning is currently the only one that does this.
     */
    public boolean moveAgent = false;
    
    /** This is the strategy that the agent will be following */
    private Vector<MetaReasoningAgent.TOCAction> strategy = null;
    /** The step at which the agent is up to in the strategy */
    private int currentStep = 0;
    
    /** True if a decision has been made and the agent is working to get it executed. */
    private boolean executing = false;
    
    /** The decision made */
    private String decision = null;
    
    /*=== The decision the meta-reasoning agent could reach are represented by
     * a list of strings, while the information used to make that decision are
     * represented with a hashtable (key is info name, value is value of info. ==*/
    /** Possible decisions the agent could make */
    private Vector<String> decisions = null;
    private Hashtable<String, Object> info = null;
    
    /** A decision making role for this meta-reasoning, if created. */
    DecisionMakingRole role = null;
    
    /** Creates a new instance of MetaReasoningAgent */
    public MetaReasoningAgent(Agent a) {
        ids[0] = new BeliefNameID("MetaAgent:"+Agent.makeComparisonString(a.getDefiningBeliefs()));
        this.agent = a;
        
        populateDecision();
        if (moveAgent && agent != null)
            MACoordination.removeAgent(agent);
        uniqueID = a.uniqueID;
        
        createStrategy();
    }
    
    /** Creates a new instance of MetaReasoningAgent */
    public MetaReasoningAgent(RAPBelief rap) {
        ids[0] = new BeliefNameID("MetaAgent:"+rap.getID());
        populateRAPAvailDecision(rap);
        createStrategy();
    }
    
    public MetaReasoningAgent(PriorityBelief priorityBelief) {
        ids = new Machinetta.State.BeliefID[2];
        ids[0] = new BeliefNameID("MetaAgent:"+priorityBelief.getID());
        ids[1] = state.getSelf().getProxyID().getID();
        
        decisions = new Vector<String>();
        decisions.add("Low");
        decisions.add("Normal");
        decisions.add("High");
        
        info = new Hashtable<String,Object>();
        info.put("Type", "Prioritize");
        info.put("PriorityBelief", priorityBelief);
        info.put("Plan", priorityBelief.getPlan());     
        info.put("RequestingAgent", state.getSelf().getProxyID());
        createStrategy();
    }
    
    /** This version allows creation from belief. <br>
     *
     * Any underlying agent is intentionally not recreated, because
     * when it is, it will act.
     */
    public MetaReasoningAgent(MABelief self, MABelief mab) {
        super(self);
        this.agentB = mab;
    }
    
    public void _merge(Agent a) {
        Machinetta.Debugger.debug( 3,"MetaReasoningAgent being merged?  I don't understand! " + this + " and " + a);
    }
    
    public Machinetta.State.BeliefType.Belief getAgentAsBelief() {
        return new Machinetta.State.BeliefType.MAC.MetaReasoningBelief(this);
    }
    
    public Machinetta.State.BeliefID[] getDefiningBeliefs() {
        return ids;
    }
    
    /** Only of interest if there is a decision making role that may have changed. */
    public void stateChanged() {
        if (role != null) {
            role = (DecisionMakingRole)state.getBelief(role.getID());
            Machinetta.Debugger.debug( 0,"Checking status of role: " + role);
            if (role != null && role.getDecision() != null) {
                Machinetta.Debugger.debug( 3,"Decision made: " + role.getDecision());
                // Once the decision has been made, switch agent to "executing" mode
                executing = true;
                // Start execution
                decision = role.getDecision();
                executeDecision();
                // After execution set the decision to null so that it is not re-executed - better to remove?
                state.removeBelief(role.getID());
            } else if (role == null) {
                Machinetta.Debugger.debug( 1,"Warning: role was not null, now is!");
            }
        }
    }
    
    public Vector<String> getDecisionOptions() { return decisions; }
    public Hashtable<String,Object> getDecisionInformation() { return info; }
    public void setDecisionOptions(Vector<String> decisions) { this.decisions = decisions; }
    public void setDecisionInfo(Hashtable<String,Object> info) { this.info = info; }
    
    /** Access only for XML to reset, otherwise this is manipulated internally. */
    public void setExecuting(boolean ex) {
        executing = ex;
    }
    
    /** True iff a decision has been made and agent is attempting to get it implemented. */
    public boolean isExecuting() { return executing; }
    
    public String getDecision() { return decision; }
    public void setDecision(String dec) { decision = dec; }
    
    /** Creates a TOC Strategy that this agent will execute to make a decision. <br>
     *
     * The strategy is stored in local variable "strategy". <br>
     *
     * Currently implements simplest strategy of always transferring control to some
     * expert.
     */
    public void createStrategy() {
        ProxyState state = new ProxyState();
        RAPBelief [] raps = state.getAllRAPBeliefs();
        ProxyID expert = null;
        for (int i = 0; expert == null && i < raps.length; i++) {
            CapabilityBelief cap = raps[i].getCapability("META_REASON");
            if (cap != null) {
                Machinetta.Debugger.debug( 0,"Expert found: " + raps[i]);
                expert = raps[i].getProxyID();
            }
        }
        
        if (expert != null) {
            if (state.getSelf().getProxyID().matches(expert)) {
                _createStrategy();
            } else {
                if (moveAgent)
                    Machinetta.Coordination.MACoordination.removeAgent(agent);
                // Move to an expert so that models are available to make a decision
                Machinetta.Debugger.debug( 1,"Moving meta-agent to " + expert);
                MACoordination.moveAgent(this, expert);
            }
        }
    }
    
    /** Executes the next step in the strategy */
    public void executeStrategy() {
        Machinetta.Debugger.debug( 1,"Executing strategy");
        // This happens when a strategy has not been created because the agent has moved
        // to an expert to get required models before making the strategy.  The object
        // still exists, even though the agent "has left" - its messy, but it works.
        if (strategy == null) return;
        
        final TOCAction currentAction = (TOCAction)strategy.get(currentStep);
        if (currentAction == null) {
            Machinetta.Debugger.debug( 3,"Strategy Complete?");
        } else if (currentAction.type.equalsIgnoreCase("Transfer")) {
            Machinetta.Debugger.debug( 1,"Moving to " + currentAction.id);
            final ProxyState state = new ProxyState();
            // Need to ensure that any underlying agent does not stay here
            if (agent != null && moveAgent)
                Machinetta.Coordination.MACoordination.removeAgent(agent);
            if (state.getSelf().getProxyID().matches(currentAction.id)) {
                // Arrived at "RAP" to whom this has been transferred
                Machinetta.Debugger.debug( 1,this + " has arrived at destination");
                
                // Now need to transfer control - create a role, put in state
                role = new Machinetta.State.BeliefType.TeamBelief.DecisionMakingRole(getDefiningBeliefs()[0], decisions, info);
                state.addBelief(role);
                state.notifyListeners();
                
                Machinetta.Debugger.debug( 3,"Transferring control for : " + role.getID());
                
                // if the transfer was a timed one, wait then deactivate role
                if (currentAction.time > 0) {
                    Machinetta.Debugger.debug( 3,"Will take back control in " + currentAction.time);
                    (new Thread() {
                        public void run() {
                            try {
                                sleep(currentAction.time * 1000);
                            } catch (InterruptedException e) {}
                            Machinetta.Debugger.debug( 3,"Transferring back control for : " + role.getID());
                            role.deactivate();
                            state.addBelief(role);
                            state.notifyListeners();
                            currentStep++;
                            executeStrategy();
                        }
                    }).start();
                } else {
                    Machinetta.Debugger.debug( 3,"Control given permanently for " + role);
                }
            } else {
                // Move this agent to the agent to which control has been transferred
                Machinetta.Debugger.debug( 1,"Moving meta-agent to " + currentAction.id);
                MACoordination.moveAgent(this, currentAction.id);
            }
        } else if (currentAction.type.equalsIgnoreCase("Autonomous")) {
            Machinetta.Debugger.debug( 1,"Autonomous decision");
            decision = (String)info.get("Autonomous");
            if (decision == null) {
                Machinetta.Debugger.debug( 3,"No autonomous default in strategy???");
            } else {
                Machinetta.Debugger.debug( 3,"Making autonomous meta-reasoning decision: " + decision);
                // Once the decision has been made, switch agent to "executing" mode
                executing = true;
                // Start execution
                executeDecision();
                // This agent is now complete
                MACoordination.removeAgent(this);
            }
        }
    }
    
    /** Returns any agent this agent is doing reasoning for */
    public Agent getUnderlyingAgent() { return agent; }
    
    public MABelief getUnderlyingAgentAsBelief() {
        if (agentB != null) return agentB;
        else if (agent != null) return (MABelief)agent.getAgentAsBelief();
        else return null;
    }
    
    public String toString() {
        return "MRA for " + info + ", executing? " + executing;
    }
    
    /*------
     * Strategy stuff
     *-----*/
    
    /** Returns a vector containing the strategy this agent is following. */
    public Vector getStrategy() { return strategy; }
    public void setStrategy(Vector<MetaReasoningAgent.TOCAction> s) { strategy = s; }
    public int getCurrentStrategyStep() { return currentStep; }
    public void setCurrentStrategyStep(int i) { currentStep = i; }
    
    public TOCAction createTOCAction(String type, ProxyID id, int time) {
        return new TOCAction(type, id, time);
    }
    
    private int countRoles(RAPBelief rap) {
        Vector roles = rap.getRoles();
        int no = 0;
        for (int i = 0; i < roles.size(); i++) {
            if (!(roles.elementAt(i) instanceof Machinetta.State.BeliefType.TeamBelief.RoleAllocationBelief)) {
                no++;
            }
        }
        return no;
    }
    
    /**
     * Try to find an expert currently performing less than maxRoles roles.
     *
     * if maxRoles < 0, then any expert will do.
     */
    private ProxyID getExpert(int maxRoles) {        
        ProxyState state = new ProxyState();
        RAPBelief [] raps = state.getAllRAPBeliefs();
        ProxyID expert = null;
        for (int i = 0; expert == null && i < raps.length; i++) {
            CapabilityBelief cap = raps[i].getCapability("META_REASON");
            if (cap != null) {
                if (maxRoles < 0 || countRoles(raps[i]) < maxRoles) {
                    Machinetta.Debugger.debug( 1,"Expert found: " + raps[i]);
                    expert = raps[i].getProxyID();
                }
            }
        }
        return expert;
    }
    
    /**
     * Notice that this may require the meta-reasoning agent leaves this proxy.
     *
     */
    private void terminatePlan(ProxyID planAgentRAP) {
        if (state.getSelf().getProxyID().equals(planAgentRAP)) {
            // At plan agent, get it to terminate the plan
            java.util.LinkedList others = MACoordination.getAgentsForPlan(uniqueID);
            
            if (others != null) {
                PlanAgent pa = null;
                for (java.util.ListIterator li = others.listIterator(); pa == null && li.hasNext(); ) {
                    Agent a = (Agent)li.next();
                    if (a instanceof PlanAgent)  pa = (PlanAgent)a;
                }
                if (pa != null) {
                    // Found what we were looking for, inform it then done
                    Machinetta.Debugger.debug( 3,"Found PlanAgent, terminating plan: " + this);
                    pa.terminate();
                    // Remove self, job well done ..
                    Machinetta.Coordination.MACoordination.removeAgent(this);
                } else {
                    Machinetta.Debugger.debug( 3,"Problem: MetaReasoning agent cannot find PlanAgent");
                }
            } else {
                Machinetta.Debugger.debug( 3,"Problem: MetaReasoning agent cannot find PlanAgent (or any agent related to plan)");
            }
        } else {
            Machinetta.Debugger.debug( 2,"Moving to plan agent to terminate plan: " + planAgentRAP);
            MACoordination.moveAgent(this, planAgentRAP);
        }
    }
    
   /*===================
    * Decision dependant stuff
    *===================*/
    
    /** Populates the decision information .... <br>
     *
     * whatever that means
     */
    protected void populateDecision() {
        if (agent != null) {
            if (agent instanceof RoleAgent) {
                Machinetta.Debugger.debug( 3,"New Role Allocation MRA");
                moveAgent = true;
                decisions = new Vector<String>();
                decisions.add("Sleep");
                decisions.add("Cancel");
                decisions.add("Continue");
                
                info = new Hashtable<String, Object>();
                info.put("Type", "RoleAllocation");
                info.put("Autonomous", "Continue");
                info.put("Role", ((RoleAgent)agent).role.getID());
                info.put("Plan", ((RoleAgent)agent).planID);
                info.put("UniqueID", new Integer(((RoleAgent)agent).uniqueID));
            } else if (agent instanceof PlanAgent) {
                Machinetta.Debugger.debug( 3,"New Plan Agent MRA");
                moveAgent = false;
                decisions = new Vector<String>();
                decisions.add("Cancel");
                decisions.add("Continue");
                
                info = new Hashtable<String, Object>();
                info.put("Type", "LongRunningPlan");
                info.put("Autonomous", "Cancel");
                info.put("Plan", ((PlanAgent)agent).getPlan().getID());
                info.put("Controlling RAP", state.getSelf().getProxyID());
            } else {
                moveAgent = false;
            }
        } else {
            Machinetta.Debugger.debug( 3,"MetaReasoning agent confused about decision type");
        }
    }
    
    protected void populateRAPAvailDecision(RAPBelief rap) {
        
        Machinetta.Debugger.debug( 3,"New RAP Available MRA");
        
        moveAgent = false;
        decisions = new Vector<String>();
        decisions.add("Leave");
        decisions.add("Move");
        
        info = new Hashtable<String,Object>();
        info.put("Type", "AvailableRAP");
        info.put("RAP", rap.getProxyID());
        info.put("Autonomous", "Leave");
        info.putAll(rap.CapabilityBeliefH);
    }
    
    private void _createStrategy() {
        String type = (String)info.get("Type");
        if (type.equalsIgnoreCase("RoleAllocation")) {
            
            ProxyID expert = getExpert(-1);
            // Choose strategy depending on whether we found an available expert
            if (expert != null) {
                /*
                strategy = new Vector<MetaReasoningAgent.TOCAction>(2);
                strategy.add(new MetaReasoningAgent.TOCAction("Transfer", expert, 10));
                // "Dud" is required due to hack in belief
                strategy.add(new MetaReasoningAgent.TOCAction("Autonomous",  new NamedProxyID("Dud"), -1));
                 */
                strategy = new Vector<MetaReasoningAgent.TOCAction>(1);
                strategy.add(new MetaReasoningAgent.TOCAction("Transfer", expert, -1));
            } else {
                strategy = new Vector<MetaReasoningAgent.TOCAction>(1);
                strategy.add(new MetaReasoningAgent.TOCAction("Autonomous", /* Required due to hack in belief */ new NamedProxyID("Dud"), -1));
            }
        } else if (type.equalsIgnoreCase("LongRunningPlan")) {
            
            ProxyID expert = getExpert(3);
            // Choose strategy depending on whether we found an available expert
            if (expert != null) {
                strategy = new Vector<MetaReasoningAgent.TOCAction>(1);
                strategy.add(new MetaReasoningAgent.TOCAction("Transfer", expert, -1));
            } else {
                strategy = new Vector<MetaReasoningAgent.TOCAction>(1);
                strategy.add(new MetaReasoningAgent.TOCAction("Autonomous", /* Required due to hack in belief */ new NamedProxyID("Dud"), -1));
            }
            
        } else if (type.equalsIgnoreCase("AvailableRAP")) {
            
            ProxyID expert = getExpert(2);
            // Choose strategy depending on whether we found an available expert
            if (expert != null) {
                strategy = new Vector<MetaReasoningAgent.TOCAction>(1);
                strategy.add(new MetaReasoningAgent.TOCAction("Transfer", expert, -1));
            } else {
                strategy = new Vector<MetaReasoningAgent.TOCAction>(1);
                strategy.add(new MetaReasoningAgent.TOCAction("Autonomous", /* Required due to hack in belief */ new NamedProxyID("Dud"), -1));
            }
        } else if (type.equalsIgnoreCase("Prioritize")) {
            ProxyID expert = getExpert(-1);
            if (expert != null) {
                strategy = new Vector<MetaReasoningAgent.TOCAction>(1);
                strategy.add(new MetaReasoningAgent.TOCAction("Transfer", expert, -1));
            } else {
                strategy = new Vector<MetaReasoningAgent.TOCAction>(1);
                strategy.add(new MetaReasoningAgent.TOCAction("Autonomous", /* Required due to hack in belief */ new NamedProxyID("Dud"), -1));
            }
        } else {
            Machinetta.Debugger.debug( 3,"Do not know how to make a strategy for " + info.get("Type"));
        }
    }
    
    /**
     * A decision has been made, implement the decision
     */
    public void executeDecision() {
        
        String type = (String)info.get("Type");
        
        // Execution for decisions about role meta-reasoning
        if (type.equalsIgnoreCase("RoleAllocation")) {
            if (agentB != null && agentB instanceof RoleAgentBelief) {
                agent = new RoleAgent((RoleAgentBelief)agentB, false);
            }
            if (decision.compareTo("Cancel") == 0) {
                ProxyID planAgentRAP = ((RoleAgent)agent).planAgentRAP;
                // If we are cancelling, the role agent should definitely go ...
                MACoordination.removeAgent(agent);
                
                // This will take care of getting rid of the RAP
                terminatePlan(planAgentRAP);
                
            } else if (decision.compareTo("Sleep") == 0) {
                // Sleep for standard time and then allow to continue
                (new Thread() {
                    public void run() {
                        try {
                            Machinetta.Debugger.debug( 2,"Meta reasoning letting agent sleep : " + agent);
                            sleep(Machinetta.Configuration.UNFILLED_ROLE_SLEEP_TIME);
                        } catch (InterruptedException e) {}
                        Machinetta.Debugger.debug( 2,"Meta reasoning waking agent : " + agent);
                        if (!MACoordination.agentExists(agent.getDefiningBeliefs())) {
                            MACoordination.addAgent(agent);
                        }
                        agent.visitedAgents.clear();
                        ((RoleAgent)agent).initAct();
                    }
                }).start();
                // This agent is now finished and can be removed ...
                Machinetta.Coordination.MACoordination.removeAgent(this);
                
            } else if (decision.compareTo("Continue") == 0) {
                // Just let the agent loose.
                if (!MACoordination.agentExists(agent.getDefiningBeliefs())) {
                    MACoordination.addAgent(agent);
                }
                agent.visitedAgents.clear();
                ((RoleAgent)agent).initAct();
                // This agent is now finished and can be removed ...
                Machinetta.Coordination.MACoordination.removeAgent(this);
            } else {
                Machinetta.Debugger.debug( 3,"Unknown decision for role meta reasoning : " + role.getDecision());
            }
        } else if (type.equalsIgnoreCase("LongRunningPlan")) {
            if (decision.equalsIgnoreCase("Cancel")) {
                ProxyID planAgentRAP = (ProxyID)info.get("Controlling RAP");
                // This will get rid of meta-reasoning agent
                terminatePlan(planAgentRAP);
            } else if (decision.equalsIgnoreCase("Continue")) {
                // Get rid of this agent, nothing to do.
                MACoordination.removeAgent(this);
            } else {
                Machinetta.Debugger.debug( 3,"Unknown long running plan decision");
            }
        } else if (type.equalsIgnoreCase("AvailableRAP")) {
            Machinetta.Debugger.debug( 3,"BEWARE: Unimplemented available rap decision called");
        } else if (type.equalsIgnoreCase("Prioritize")) {
            ProxyID requestor = (ProxyID)info.get("RequestingAgent");            
            if (requestor.equals(state.getSelf().getProxyID())) {
                // Back to requestor, let it know the priority
                DynamicQuantitativePriority bel = (DynamicQuantitativePriority)info.get("PriorityBelief");
                Machinetta.Debugger.debug( 3,"Setting priority on " + bel + " to " + decision);
                if (bel != null) {
                    if (decision.equalsIgnoreCase("Low")) {
                        bel.setPriority(0);
                    } else if (decision.equalsIgnoreCase("Normal")) {
                        bel.setPriority(1);
                    } else if (decision.equalsIgnoreCase("High")) {
                        bel.setPriority(2);
                    }
                    state.addBelief(bel);
                    state.notifyListeners();
                } else {
                    Machinetta.Debugger.debug( 3,"Somehow lost priority belief, cannot set priority!");
                }
                
            } else {
                // Head to the requestor, to let it know priority
                Machinetta.Debugger.debug( 1,"Heading back to requestor: " + requestor + " for " + role.getID());
                MACoordination.moveAgent(this, requestor);
            } 
                
        } else {
            Machinetta.Debugger.debug( 3,"Do not know how to execute decision type : " + info.get("Type"));
        }
        
    }
    
    public static final long serialVersionUID = 1L;
    
   /*===================
    * End decision dependant stuff
    *===================*/
    
    /** Represents a single action in a TOC
     *
     * BEWARE: This is not sent very messily via the message passing, so look carefully
     * at the belief when making any changes.  Specifically, probably doesn't handle null in any
     * id ....
     */
    public class TOCAction {
        public TOCAction(String type, ProxyID id, int time) {
            this.type = type;
            this.id = id;
            this.time = time;
        }
        
        public String type;
        public ProxyID id;
        public int time;
    }
}

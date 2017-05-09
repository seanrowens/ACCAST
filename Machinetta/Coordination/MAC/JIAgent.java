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
 * JointIntentionsAgent.java
 *
 * Created on March 2, 2004, 2:24 PM
 */

package Machinetta.Coordination.MAC;

import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.ProxyID;

/**
 *
 * @author  pscerri
 */
public class JIAgent extends Agent {
    
    private JI_STATE state = null;
    
    /** The Plan BeliefID for the plan on who's behalf this agent is acting. <br>
     * Note that only the id is required, since only involved agents are visited.
     */
    private Machinetta.State.BeliefID planID = null;
    
    /** BeliefID of the role, if any, to which this agent is relevant */
    private Machinetta.State.BeliefID roleID = null;
    
    /** The value of the capability, for role allocated messages */
    private int capability = 0;
    
    /** A time for scheduling messages */
    private int scheduleTime = 0;
    
    /** In the case of the DIR_UPDATE need to inform which role and
     * (see next, DIRPerformer) which Proxy
     */
    private BeliefID DIRRole = null;
    
    private ProxyID DIRPerformer = null;
    
    /** In the case of TEAMMATE_* tells which teammate */
    private ProxyID teammate = null;
    
    /** The proxy sending this message */
    private Machinetta.State.BeliefType.ProxyID informer = null;
    
    /** The (current) type of this JISAgent */
    public static enum JI_STATE { ROLE_ALLOCATED, PLAN_TERMINATED, ROLE_DEALLOCATED, ROLE_START, ROLE_SUSPEND,
    ROLE_COMPLETE, AUTHORIZATION_REQUEST, AUTHORIZATION_GRANTED,
    SCHEDULING_REQUEST, SCHEDULING_RESPONSE, DIR_UPDATE, TEAMMATE_ADD, TEAMMATE_REMOVE};
    
    
    // These are all deprecated
    
    /** RoleAgent informing PlanAgent that a proxy has been found for the role. */
    public final static int JIS_ROLE_ALLOCATED = 1;
    
    /** PlanAgent telling RoleAgent all roles should be terminated */
    public final static int JIS_PLAN_TERMINATED = 2;
    
    /** RoleAgent telling PlanAgent that role is no longer allocated to this agent */
    public final static int JIS_ROLE_DEALLOCATED = 3;
    
    /** PlanAgent informing RoleAgent that it should begin role execution */
    public final static int JIS_ROLE_START = 4;
    
    /** PlanAgent informing RoleAgent that it should suspend role execution */
    public final static int JIS_ROLE_SUSPEND = 5;
    
    /** RoleAgent informing PlanAgent that RAP has completed execution of the role */
    public final static int JIS_ROLE_COMPLETE = 6;
    
    /** RoleAgent requesting authorization */
    public final static int JIS_AUTHORIZATION_REQUEST = 7;
    
    /** PlanAgent giving authorization */
    public final static int JIS_AUTHORIZATION_GRANTED = 8;
    
    /** PlanAgent checking ability to schedule plan at time */
    public final static int JIS_SCHEDULING_REQUEST = 9;
    
    /** RoleAgent sending schedule (only) */
    public final static int JIS_SCHEDULING_RESPONSE = 10;
    
    /** Informing who some information should be delivered to */
    public final static int JIS_DIR_UPDATE = 11;
    
    /** Creates a new instance of JointIntentionsAgent
     */
    public JIAgent(Machinetta.State.BeliefID planID, Machinetta.State.BeliefID roleID, JI_STATE state) {
        this.planID = planID;
        this.roleID = roleID;
        this.state = state;
        informer = super.state.getSelf().getProxyID();
    }
    
    /** Creates a new instance of JointIntentionsAgent
     *
     * @Deprecated
     */
    public JIAgent(Machinetta.State.BeliefID planID, Machinetta.State.BeliefID roleID, int state) {
        Machinetta.Debugger.debug(5, "Using deprecated JIAgent constructor ... will fail");
        this.planID = planID;
        this.roleID = roleID;
        informer = super.state.getSelf().getProxyID();
    }
    
    /** Creates a new instance of a JIAgent from a belief */
    public JIAgent(Machinetta.State.BeliefType.MAC.JIAgentBelief jib) {
        super(jib);
        this.state = jib.JIstate;
        this.planID = jib.planID;
        this.roleID = jib.roleID;
        this.informer = jib.informer;
        this.capability = jib.capability;
        this.scheduleTime = jib.time;
        this.DIRPerformer = jib.DIRPerformer;
        this.DIRRole = jib.DIRRole;
        this.teammate = jib.teammate;
        act();
    }
    
    public void _merge(Agent a) {
        // Machinetta.Debugger.debug("_merge called in JI agent, but ignored .. ", 3, this);
        // Assume that incoming agent has more up to date information
        if (((JIAgent)a).state != state) {
            Machinetta.Debugger.debug(1, "Local JIAgent " + this + " changing from state " + state + " to " + ((JIAgent)a).state);
            this.state = ((JIAgent)a).state;
        }
    }
    
    public Machinetta.State.BeliefType.Belief getAgentAsBelief() {
        return new Machinetta.State.BeliefType.MAC.JIAgentBelief(this);
    }
    
    public Machinetta.State.BeliefID[] getDefiningBeliefs() {
        Machinetta.State.BeliefID[] ids = new Machinetta.State.BeliefID[2];
        ids[0] = planID;
        ids[1] = roleID;
        //ids[2] = new Machinetta.State.BeliefNameID("JI"+state);
        return ids;
    }
    
    public void stateChanged() {
    }
    
    /** The activities of this agent <br>
     *
     * @todo Clean this up a bit, at some stage - lots of cut and paste code. <br>
     *
     * When the JIAgent fails to find a RoleAgent it is likely due to a race condition. Specifically
     * the proxy had given up the role and tried to inform the plan agent, but the JIAgent was already
     * on route.  The plan agent handles this case OK (in theory) so the JIAgent should be safe just
     * "giving up" but maybe eventually want to warn the plan agent in case something else has happened. <br>
     */
    public void act() {
        Machinetta.Debugger.debug(1, "JIAgent " + this + " allowed to act in state: " + state);
        PlanAgent pa = null;
        RoleAgent ra = null;
        switch(state) {
            case ROLE_ALLOCATED:
                Machinetta.Debugger.debug(1, "Informing role allocated");
                // Hopefully we are at plan agent
                for (java.util.ListIterator li = Machinetta.Coordination.MACoordination.getAgentsForPlan(uniqueID).listIterator(); pa == null && li.hasNext(); ) {
                    Agent a = (Agent)li.next();
                    if (a instanceof PlanAgent) pa = (PlanAgent)a;
                }
                if (pa == null) {
                    Machinetta.Debugger.debug(4, "When allocating could not find plan agent for : " + this + " with id " + uniqueID);
                    Machinetta.Coordination.MACoordination.printAgents(4);
                } else {
                    pa.roleAllocated(this);
                }
                break;
                
            case ROLE_DEALLOCATED:
                Machinetta.Debugger.debug(1, "Informing role deallocated");
                // Hopefully we are at plan agent
                for (java.util.ListIterator li = Machinetta.Coordination.MACoordination.getAgentsForPlan(uniqueID).listIterator(); pa == null && li.hasNext(); ) {
                    Agent a = (Agent)li.next();
                    if (a instanceof PlanAgent) pa = (PlanAgent)a;
                }
                if (pa == null) {
                    Machinetta.Debugger.debug(4, "When deallocating could not find plan agent for : " + this + " with id " + uniqueID);
                    Machinetta.Coordination.MACoordination.printAgents(4);
                } else {
                    pa.roleDeallocated(this);
                }
                break;
                
            case PLAN_TERMINATED:
                Machinetta.Debugger.debug(1, "Informing plan terminated");
                // Hopefully we are at role agent
                ra = getRoleAgent();
                if (ra == null) {
                    Machinetta.Debugger.debug(0, "When terminating could not find role agent for : " + this);
                    Machinetta.Coordination.MACoordination.printAgents(0);
                } else {
                    ra.informPlanTerminated(this);
                }
                break;
                
            case ROLE_START:
                Machinetta.Debugger.debug(1, "Informing role start: " + this);
                // Hopefully we are at role agent
                ra = getRoleAgent();
                if (ra == null) {
                    Machinetta.Debugger.debug(0, "When starting could not find role agent for : " + this);
                    Machinetta.Coordination.MACoordination.printAgents(0);
                } else {
                    ra.informRoleStart(this);
                }
                break;
                
            case ROLE_SUSPEND:
                Machinetta.Debugger.debug(1, "Informing role suspended: " + this);
                ra = getRoleAgent();
                if (ra == null) {
                    Machinetta.Debugger.debug(4, "When suspending could not find role agent for : " + this);
                    Machinetta.Coordination.MACoordination.printAgents(4);
                } else {
                    ra.informRoleSuspended(this);
                }
                break;
                
            case ROLE_COMPLETE:
                Machinetta.Debugger.debug(1, "Informing role complete: " + this);
                // Hopefully we are at plan agent
                for (java.util.ListIterator li = Machinetta.Coordination.MACoordination.getAgentsForPlan(uniqueID).listIterator(); pa == null && li.hasNext(); ) {
                    Agent a = (Agent)li.next();
                    if (a instanceof PlanAgent) pa = (PlanAgent)a;
                }
                if (pa == null) {
                    Machinetta.Debugger.debug(4, "When informing complete could not find plan agent for : " + this + " with id " + uniqueID);
                    Machinetta.Coordination.MACoordination.printAgents(4);
                } else {
                    pa.roleComplete(this);
                }
                break;
                
            case AUTHORIZATION_GRANTED:
                Machinetta.Debugger.debug(1, "Informing authorization granted: " + this);
                ra = getRoleAgent();
                if (ra == null) {
                    Machinetta.Debugger.debug(0, "When starting could not find role agent for : " + this);
                    Machinetta.Coordination.MACoordination.printAgents(0);
                } else {
                    ra.informRoleAuthorized(this);
                }
                break;
            case SCHEDULING_REQUEST:
                Machinetta.Debugger.debug(2, "JIAgent asked to change scheduling");
                ra = getRoleAgent();
                if (ra == null) {
                    Machinetta.Debugger.debug(0, "When rescheduling could not find role agent for : " + this);
                    Machinetta.Coordination.MACoordination.printAgents(0);
                } else {
                    ra.requestReschedule(this);
                }
                break;
            case SCHEDULING_RESPONSE:
                Machinetta.Debugger.debug(4, "Scheduling actions for JIA unimplemented .... ");
                break;
            case DIR_UPDATE:
                Machinetta.Debugger.debug(1, "JIAgent acting on DIR update with : " + DIRRole + " and " + DIRPerformer);
                ra = getRoleAgent();
                if (ra == null) {
                    Machinetta.Debugger.debug(1, "When doing DIR update could not find role agent for : " + this);
                    Machinetta.Coordination.MACoordination.printAgents(0);
                } else {
                    ra.updateDirectedInfoInformation(DIRRole, DIRPerformer);
                }
                break;
                
            case TEAMMATE_ADD:
                Machinetta.Debugger.debug(1, "Informing team mate added");
                // Hopefully we are at role agent
                ra = getRoleAgent();
                if (ra == null) {
                    Machinetta.Debugger.debug(3, "When informing team mate add could not find role agent for : " + this);
                    Machinetta.Coordination.MACoordination.printAgents(1);
                } else {
                    ra.teammateAdd(this);
                }
                break;
                
            case TEAMMATE_REMOVE:
                Machinetta.Debugger.debug(1, "Informing team mate removed");
                // Hopefully we are at role agent
                ra = getRoleAgent();
                if (ra == null) {
                    Machinetta.Debugger.debug(3, "When informing team mate remove could not find role agent for : " + this);
                    Machinetta.Coordination.MACoordination.printAgents(1);
                } else {
                    ra.teammateRemove(this);
                }
                break;
                
            default:
                Machinetta.Debugger.debug(4, "JIAgent in unknown state : " + state);
        }
    }
    
    /** Tries to find the RoleAgent corresponding to this JIAgent */
    private RoleAgent getRoleAgent() {
        RoleAgent ra = null;
        for (java.util.ListIterator li = Machinetta.Coordination.MACoordination.getAgentsForPlan(uniqueID).listIterator(); ra == null && li.hasNext(); ) {
            Agent a = (Agent)li.next();
            if (a instanceof RoleAgent) {
                if (((RoleAgent)a).role.getID().equals(roleID)) {
                    ra = (RoleAgent)a;
                }
            }
        }
        return ra;
    }
    
    public JI_STATE getJIState() { return state; }
    public void setState(JI_STATE newState) {this.state = newState; }
    
    public Machinetta.State.BeliefID getPlanID() { return planID; }
    public Machinetta.State.BeliefID getRoleID() { return roleID; }
    public Machinetta.State.BeliefType.ProxyID getInformer() { return informer; }
    public void setInformer(Machinetta.State.BeliefType.ProxyID informer) { this.informer = informer; }
    public int getCapability() { return capability; }
    public void setCapability(int cap) { capability = cap; }
    public int getScheduleTime() { return scheduleTime; }
    public void setScheduleTime(int time) { scheduleTime = time; }
    public BeliefID getDIRRole() { return DIRRole; }
    public void setDIRRole(BeliefID role) { DIRRole = role; }
    public ProxyID getDIRPerformer() { return DIRPerformer; }
    public void setDIRPerformer(ProxyID id) { DIRPerformer = id; }
    public void setTeammate(ProxyID teammate) { this.teammate = teammate; }
    public ProxyID getTeammate() { return teammate; }
    
    
    /** Create an exact copy of this JIAgent */
    public Object clone() {
        JIAgent clone = new JIAgent(planID, roleID, state);
        clone.uniqueID = uniqueID;
        clone.visitedAgents = visitedAgents;
        clone.informer = informer;
        
        return clone;
    }
    
    public String toString() { return "JIA: " + planID + ":" + roleID + " in State: " + state + " from " + informer + ", id " + uniqueID; }
    
    public boolean equals(Object o) {
        if (o == null || !(o instanceof JIAgent)) return false;
        JIAgent test = (JIAgent)o;
        return (test.getPlanID().equals(planID) && test.getRoleID().equals(roleID) && test.getInformer().equals(informer));
    }
    
    private final static long serialVersionUID = 1L;
}

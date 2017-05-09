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
 * InformationAgent.java
 *
 * Created on November 4, 2003, 10:28 AM
 */

package Machinetta.Coordination.MAC;
import Machinetta.Coordination.MACoordination;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import java.util.Random;

/**
 *
 * @author  pscerri
 */
public class InformationAgent extends Agent {
    
    private Machinetta.State.BeliefType.Belief b = null;
    private ProxyID toRAP = null;
    private Machinetta.State.BeliefType.TeamBelief.RoleBelief forRole = null;
    /** Notice that this does not move with agent, indicates that agent is done <b>here</b> */
    private Boolean done = false;
    private int TTL = -1;
    
    /** Creates a new instance of InformationAgent
     *
     * With this instantiation, the information agent just goes a wandering
     */
    public InformationAgent(Machinetta.State.BeliefType.Belief b) {
        this.b = b;
        Machinetta.Debugger.debug( 0,"Information agent created for: " + b);
    }
    
    /** Creates a new instance of InformationAgent
     *
     * With this instantiation, the information agent travels a fixed distance
     */
    public InformationAgent(Machinetta.State.BeliefType.Belief b, int ttl) {
        this.b = b;
        TTL = ttl;
        Machinetta.Debugger.debug( 0,"Information agent created for: " + b + " : " + ttl);
    }
    
    /** Creates a new instance of InformationAgent
     *
     * With this instantiation, the information agent goes to a specific RAP
     */
    public InformationAgent(Machinetta.State.BeliefType.Belief b, ProxyID toRAP) {
        this.b = b;
        this.toRAP = toRAP;
        Machinetta.Debugger.debug( 0,"Information agent created for: " + b + " : " + toRAP);
    }
    
    /** Creates a new instance of InformationAgent
     *
     * Not strictly useful (at least one of toRAP and forRole must be null)
     * but makes things easier for the factory.
     */
    public InformationAgent(Machinetta.State.BeliefType.Belief b, ProxyID toRAP, RoleBelief forRole) {
        this.b = b;
        this.toRAP = toRAP;
        this.forRole = forRole;
        Machinetta.Debugger.debug( 0,"Information agent created for: " + b);
    }
    
    /** This function returns the belief that this agent is carrying around the team */
    public Machinetta.State.BeliefType.Belief getBelief() { return b; }
    
    public Machinetta.State.BeliefID[] getDefiningBeliefs() {
        Machinetta.State.BeliefID [] bids = new Machinetta.State.BeliefID[1];
        bids[0] = b.getID();
        return bids;
    }
    
    public Machinetta.State.BeliefType.Belief getAgentAsBelief() {
        return new Machinetta.State.BeliefType.MAC.InformationAgentBelief(this);
    }
    
    public String toString() {
        return "Information agent for : " + b + " TTL=" + TTL + " toRAP= " + toRAP;
    }
    
    /**
     * This is called by the factory when the agent is first created.
     */
    public void stateChanged() { 
        
        synchronized(done) {
            // The agent will only act once here
            if (done)
                return;
            else
                done = true;
        }
        
        if (toRAP != null) {
            if (state.getSelf().getProxyID().equals(toRAP)) {
                Machinetta.Debugger.debug( 0,this + " job is done ... at dest");
                Machinetta.Coordination.MACoordination.removeAgent(this);
            } else {
                Machinetta.Coordination.MACoordination.moveAgent(this, toRAP);
            }
        } else if (forRole != null) {
            // I don't this this occurs any more, since InformationAgentFactory should set
            // toRAP before sending it off.
            Machinetta.Debugger.debug( 3,"Unimplemented version of InformationAgent");
        } else if (TTL >= 0) {                        
            if (TTL == 0) {
                Machinetta.Debugger.debug( 0,this + " job is done ... TTL == 0");
                Machinetta.Coordination.MACoordination.removeAgent(this);
            } else {
                TTL--;
		//                Machinetta.Debugger.debug( 1,"Changed TTL to " + TTL + " on " + this);
                Machinetta.Coordination.MACoordination.moveAgentRandomly(this, true);
            }
        } else {            
            if (!Machinetta.Coordination.MACoordination.moveAgentRandomly(this, false)) {
                // Visited all, just go away
                Machinetta.Coordination.MACoordination.removeAgent(this);
            }
        }
    }
    
    /**
     * When a send fails, wait a while and then try resending randomly.
     */
    public void sendFailed() {
        final InformationAgent agent = this;
        
        MACoordination.pendingFailedInfoAgents--; 
        
        synchronized(done) {
            // The agent will only act once here
            if (done)
                return;
            else
                done = true;
        }
        
        (new Thread() {
            public void run() {                
                try {                    
                    long sleepTime = (long)((new Random()).nextDouble() * 500);
                    Machinetta.Debugger.debug(0,"Waiting " + sleepTime + " to resend agent: " + agent);
                    sleep(sleepTime);
                } catch (Exception e) {}
                
                // Even if supposed to go somewhere specific, since last failed, just move anywhere for now.
                Machinetta.Debugger.debug(3,"Resending failed agent: " + agent);
                Machinetta.Coordination.MACoordination.moveAgentRandomly(agent, true);
            }            
        }).start();
    }
    
    public void _merge(Agent a) {
        // Do nothing?
    }
    
    public final static long serialVersionUID = 1L;
    
    public ProxyID getToRAP() {
        return toRAP;
    }
    
    public void setToRAP(ProxyID toRAP) {
        this.toRAP = toRAP;
    }
    
    public Machinetta.State.BeliefType.TeamBelief.RoleBelief getForRole() {
        return forRole;
    }
    
    public void setForRole(Machinetta.State.BeliefType.TeamBelief.RoleBelief forRole) {
        this.forRole = forRole;
    }
    
    public int getTTL() {
        return TTL;
    }
    
    public void setTTL(int TTL) {
        this.TTL = TTL;
    }
    
    /**
     * Call when the agent should try to act here again.
     */
    public void reset() {
        done = false;
        TTL++;
    }
}

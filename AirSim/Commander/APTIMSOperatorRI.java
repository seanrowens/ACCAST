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
 * SimOperatorRI.java
 *
 * Created on March 20, 2006, 9:34 AM
 *
 */

package AirSim.Commander;

import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Environment.Area;
import AirSim.Machinetta.BasicRole;
import Machinetta.Coordination.MAC.PlanAgent;
import Machinetta.RAPInterface.InputMessages.InputMessage;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;
import Machinetta.RAPInterface.RAPInterfaceImplementation;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief;
import Machinetta.State.ProxyState;
import java.awt.Rectangle;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author pscerri
 */
public class APTIMSOperatorRI extends RAPInterfaceImplementation {
    
    private ProxyState state = new ProxyState();
    {
        Machinetta.Debugger.debug("APTIMS operator got a state change message", 1, this);
    }
    
    /** Creates a new instance of SimOperatorRI */
    public APTIMSOperatorRI() {
        Machinetta.Debugger.debug("APTIMS operator is running ... ", 1, this);
    }
    
    /**
     * Sends a message to the RAP
     *
     *
     * @param msg The message to send
     */
    public void sendMessage(OutputMessage msg) {
        
    }
    
    /**
     * Needed to implement Thread
     */
    public void run() {
        
        while (state.getSelf() == null) {
            try {
                sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        
        try {
            sleep(10000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        PlanAgent pa = createSwarmPlan(15000,15000,1000,3);
        
        try {
            sleep(180000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        Machinetta.Debugger.debug("Plan terminated", 1, this);
        pa.terminate();
        
        
        try {
            sleep(200);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        Machinetta.Debugger.debug("Changed swarm", 1, this);
        pa = createSwarmPlan(25000,25000,1000,3);
        
        try {
            sleep(120000);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        Machinetta.Debugger.debug("Plan terminated", 1, this);
        pa.terminate();
                
        try {
            sleep(200);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        Machinetta.Debugger.debug("Bridge plan started", 1, this);
        pa = createBridgePlan(10000, 10000, 30000, 30000, 3);
        
    }
    
    private PlanAgent createSwarmPlan(int x, int y, int r, int noUAVs) {
        Machinetta.Debugger.debug("Creating Swarm Plan: loc="+x+", "+y, 3, this);
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        for (int i = 0; i < noUAVs; i++) {
            BasicRole basic = new BasicRole(TaskType.patrol);
            Hashtable<String, Object> params = new Hashtable<String, Object>();
            Rectangle area = new Rectangle(x-r,y-r,2*r, 2*r);
            params.put("Area", area);
            params.put("Label", i);
            basic = (BasicRole)basic.instantiate(params);
            basic.constrainedWait = false;
            roles.add(basic);
        }
        
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("Swarm"+x+":"+y+":"+r), "Swarm", null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
    }
    
    private PlanAgent createBridgePlan(int x1, int y1, int x2, int y2, int noUAVs) {
        Machinetta.Debugger.debug("Creating Bridge Plan: loc="+x1+", "+y1 + " to " + x2 + ", " + y2, 3, this);
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        int dx = (x2 - x1)/(noUAVs-1);
        int dy = (y2 - y1)/(noUAVs-1);
        
        int x = x1, y = y1;
        
        for (int i = 0; i < noUAVs; i++) {
            BasicRole basic = new BasicRole(TaskType.patrol);
            Hashtable<String, Object> params = new Hashtable<String, Object>();
            Rectangle area = new Rectangle(x-100,y-100,200, 200);
            params.put("Area", area);
            params.put("Label", i);
            basic = (BasicRole)basic.instantiate(params);
            basic.constrainedWait = false;
            roles.add(basic);
            
            x += dx;
            y += dy;
        }
        
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("Bridge"+x1+":"+y1 + "-" + x2 + ":" + y2), "Swarm", null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
    }
    
    private PlanAgent createMovePlan() {
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        BasicRole basic = new BasicRole(TaskType.move);
        Hashtable<String, Object> params = new Hashtable<String, Object>();
        params.put("Destination", new java.awt.Point(47500,47500));
        basic = (BasicRole)basic.instantiate(params);
        basic.constrainedWait = false;
        roles.add(basic);
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("MoveToDest"), "Move", null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
    }
    
    
    
    private PlanAgent createPatrolPlan(int x, int y) {
        Machinetta.Debugger.debug("createPatrolPlan: loc="+x+", "+y, 3, this);
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        // Currently only a single strike on the target.
        BasicRole basic = new BasicRole(TaskType.patrol);
        Hashtable<String, Object> params = new Hashtable<String, Object>();
        Area area = new Area(x-500,y-500,x+500, y+500);
        params.put("Area", area);
        basic = (BasicRole)basic.instantiate(params);
        basic.constrainedWait = false;
        roles.add(basic);
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("Patrol"+x+":"+y), "Patrol", null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
    }
    
    
    /**
     * Called to get list of new messages
     * Should return only those messages received since last called.
     *
     * @return List of InputMessage objects received from RAP since last called.
     */
    public InputMessage[] getMessages() {
        // Not used.
        return null;
    }
    
}

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
 * ResourceAgent.java
 *
 * Created on June 12, 2007, 5:09 PM
 *
 */

package Machinetta.Coordination.MAC;

import Machinetta.Coordination.MACoordination;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.MAC.ResourceAgentBelief;
import Machinetta.State.BeliefType.ResourceNeedBelief;
import Machinetta.State.BeliefType.TeamBelief.ResourceBelief;
import Machinetta.State.ProxyState;
import java.util.Random;

/**
 *
 * @author pscerri
 */
public class ResourceAgent extends Agent {
    
    private ResourceBelief resource = null;
    private int threshold = 50;
    
    // @todo Shift these to config
    private int inc = 2, dec = 4;
    private long incTimeStep = 500;
    IncThread thread = new IncThread();
    {
        thread.agent = this;
    }
    // Notice that this does not move with agent
    ResourceNeedBelief rnb = null;
    // Notice that this does not move with agent
    ProxyState state = new ProxyState();
    
    /** Creates a new instance of ResourceAgent */
    public ResourceAgent(ResourceBelief resource) {
        this.resource = resource;
        MACoordination.addAgent(this);
        initAct();
    }
    
    public ResourceAgent(ResourceAgentBelief rab) {
        this(rab, false);
    }
    
    public ResourceAgent(ResourceAgentBelief rab, boolean isFailed) {
        
        Machinetta.Debugger.debug( 1,"Creating from belief");
        
        this.resource = rab.getResource();
        this.threshold = rab.getThreshold();
        
        MACoordination.addAgent(this);
        
        if (isFailed) {
            sendFailed();
        } else {
            initAct();
        }
    }
    
    
    public void stateChanged() {
        // rnb will be null when agent failed to move and is waiting to move.
        if (rnb != null) {
            
            rnb = (ResourceNeedBelief)state.getBelief(rnb.getID());
            
            if (rnb.isConsidered()) {
                Machinetta.Debugger.debug( 1,"Proxy deserves resource: " + rnb.getNeed()  + " : " + threshold);
                if (rnb.getNeed() > threshold) {
                    if (!thread.isStarted()) {
                        Machinetta.Debugger.debug( 1,"Resource given to proxy: " + resource);
                        rnb.setTaken(false);
                        rnb.setGiven(true);
                        thread.start();
                        state.addBelief(rnb);
                        state.notifyListeners();
                    }
                } else {
                    if (thread.isStarted()) {
                        Machinetta.Debugger.debug( 1,"Resource taken from proxy: " + resource);
                        rnb.setTaken(true);
                        rnb.setGiven(false);
                        try { thread.interrupt(); } catch (SecurityException e) {}
                        thread.softStop();
                        threshold = rnb.getNeed(); // Reset down to threshold we want to leave at
                        state.addBelief(rnb);
                        state.notifyListeners();
                    }
                    threshold -= dec;
                    MACoordination.moveAgentRandomly(this, true);
                }
            } // else do nothing
        }
    }
    
    private void initAct() {
        Machinetta.Debugger.debug( 1,"Resource agent initializing: " + resource);
        rnb = new ResourceNeedBelief(resource);
        state.addBelief(rnb);
        state.notifyListeners();
    }
    
    public void sendFailed() {
        final ResourceAgent agent = this;
        (new Thread() {
            public void run() {
                try {
                    long sleepTime = (long)((new Random()).nextDouble() * 5000);
                    Machinetta.Debugger.debug( 1,"Waiting " + sleepTime + " to resend agent: " + agent);
                    sleep(sleepTime);
                } catch (Exception e) {}
                
                // Even if supposed to go somewhere specific, since last failed, just move anywhere for now.
                Machinetta.Debugger.debug( 1,"Resending failed agent: " + agent);
                Machinetta.Coordination.MACoordination.moveAgentRandomly(agent, true);
            }
        }).start();
        
    }
    
    // TODO: (20m, minor) Clean this up, because I hacked it to synchronize it
    private class IncThread extends Thread {
        
        private boolean started = false;
        private boolean stop = false;
        public ResourceAgent agent = null;
        
        public void run() {
            Machinetta.Debugger.debug( 1,"Started agent: " + agent);
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            while (!stop) {
                try {
                    sleep(incTimeStep);
                } catch (Exception e) {}
                
                threshold += inc;
                rnb.setConsidered(false);
                state.addBelief(rnb);
                state.notifyListeners();
                Machinetta.Debugger.debug( 1,"Agent Threshold is : " + threshold);
            }
            Machinetta.Debugger.debug( 1,"Ended agent: " + agent);
        }
        
        public synchronized boolean isStarted() {
            return started;
        }
        
        public synchronized void start() {
            super.start();
            started = true;
        }
        
        public synchronized void softStop() {
            stop = true;
            try { this.interrupt(); } catch(SecurityException e) {};
        }
    }
    
    public BeliefID[] getDefiningBeliefs() {
        BeliefID[] ids = new BeliefID[1];
        ids[0] = resource.getID();
        return ids;
    }
    
    public Belief getAgentAsBelief() {
        
        return new ResourceAgentBelief(this);
        
    }
    
    public ResourceBelief getResource() {
        return resource;
    }
    
    public int getThreshold() {
        return threshold;
    }
    
    public void _merge(Agent a) {
        Machinetta.Debugger.debug( 4,"Something has gone wrong, merged resource agents .... ");
    }
    
    
}

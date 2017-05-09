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
 * CoordModule.java
 *
 * Created on 4 July 2002, 13:01
 */

package Machinetta.Coordination;

import Machinetta.IntelligentModule;
import Machinetta.State.BeliefID;
import Machinetta.State.ProxyState;
import Machinetta.State.BeliefType.TeamBelief.TeamBelief;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanTemplate;
import Machinetta.Communication.Communication;
import Machinetta.Communication.Message;
import Machinetta.Configuration;
import Machinetta.Proxy;

/**
 * Handles coordination between proxy and other proxies.
 *
 * @author  scerri
 */
public class CoordModule extends IntelligentModule {
    
    /** Creates a new instance of CoordModule
     */
    public CoordModule() {
        this(null);
    }
    
    public CoordModule(Proxy proxy) {
        super(proxy);
        if (impl == null)
            init();
    }
    
    /** Implements StateChangeListener
     * @param b List of beliefs changed since last call
     */
    protected void proxyStateChanged(BeliefID[] b) {
        impl.proxyStateChange(b);
    }
    
    /** Incoming messages
     * @param msgs List of Message objects, arrived since last called
     */
    public void incomingMessages(Message [] msgs) {
        impl.incomingMessages(msgs);
    }
    
    /** Creates the implementation of the coordination, if not done      
     *
     */
    private void init() {
                
        if (impl == null) {
            String className = Configuration.COORD_IMPLEMENTATION_TYPE;
            
            try {
                Class type = Class.forName(className);
                impl = (CoordImplementation)type.newInstance();
                Machinetta.Debugger.debug(1,"CoordImplementation " + type + " created");
            } catch (ClassNotFoundException e) {
                Machinetta.Debugger.debug(5,"Could not find CoordImplementation : " + Configuration.COORD_IMPLEMENTATION_TYPE);
            } catch (InstantiationException e2) {
                Machinetta.Debugger.debug(5,"Could not instantiate : " + e2);
            } catch (IllegalAccessException e3) {
                Machinetta.Debugger.debug(5,"Could not instantiate : " + e3);
            } catch (ClassCastException e4) {
                Machinetta.Debugger.debug(5,"CoordImplementation specified was not a CoordImplementation");
            }
            
            if (impl == null) {
                Machinetta.Debugger.debug(1,"Using default AAImplementation");
                impl = new TestCoordination();
            }
        }
        
        /** Process all of the current beliefs */
        /** First, let's process team(plan) beliefs, so that we know who's who */
        BeliefID [] allBeliefs = state.getAllBeliefs();
        int teamCount = 0;
        for (int i = 0; i < allBeliefs.length; i++) {
            Belief belief = state.getBelief(allBeliefs[i]);
            if ((belief instanceof TeamBelief) || (belief instanceof TeamPlanTemplate))
                teamCount++;
        }
        BeliefID [] teams = new BeliefID[teamCount];
        BeliefID [] nonTeams = new BeliefID[allBeliefs.length-teamCount];
        int teamIndex =  0;
        int nonTeamIndex = 0;
        for (int i = 0; i < allBeliefs.length; i++) {
            Belief belief = state.getBelief(allBeliefs[i]);
            if ((belief instanceof TeamBelief) || (belief instanceof TeamPlanTemplate))
                teams[teamIndex++] = allBeliefs[i];
            else
                nonTeams[nonTeamIndex++] = allBeliefs[i];
        }
        this.proxyStateChanged(teams);
        this.proxyStateChanged(nonTeams);
    }        
    
    /** Implementation object for coordination */
    private static CoordImplementation impl = null;
    
    /** Beliefs */
    private ProxyState state = new ProxyState();
}

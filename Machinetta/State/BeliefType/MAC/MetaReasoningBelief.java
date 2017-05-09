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
 * MetaReasoningBelief.java
 *
 * Created on May 24, 2004, 5:43 PM
 */

package Machinetta.State.BeliefType.MAC;

import Machinetta.Coordination.MAC.*; 
import Machinetta.State.BeliefType.ProxyID;

import java.util.*;

/**
 * This class is really ugly and I would really like to change it
 *
 * Maybe one day when I am bored ....
 *
 * @author  pscerri
 */
public class MetaReasoningBelief extends MABelief  {
    
    // The underlying agent, if any
    public MABelief agentB = null;
    public boolean moveAgent = false;
    
    public Machinetta.State.BeliefID bid = null;
    
    // The TOC strategy    
    public Vector<String> strategyStrings = null;
    public Vector<ProxyID> strategyProxyIDs = null;
    public Vector<Integer> strategyTimes = null;
    public int currentStep = 0;
    
    public boolean executing = true;
    
    // The decision to be made
    public Vector<String> decisionOptions = null;
    public Hashtable<String,Object> decisionInfo = null;
    
    public String decision = null;
    
    /** For create from XML only */
    public MetaReasoningBelief() {}
    
    /** Creates a new instance of MetaReasoningBelief */
    public MetaReasoningBelief(MetaReasoningAgent a) {
        super(a);
        try {
            agentB = (MABelief)a.getUnderlyingAgentAsBelief();
        } catch (NullPointerException e) {
            // Lazy .... this happens when there is no underlying agent ... no big deal
            Machinetta.Debugger.debug( 3,"Maybe this is a big deal ... Meta");
        }
        moveAgent = a.moveAgent;
        
        // Translate the strategy across via several vectors, ugly but simple ...
        Vector strategy = a.getStrategy();
        if (strategy != null) {
            strategyStrings = new Vector<String>();
            strategyProxyIDs = new Vector<ProxyID>();
            strategyTimes = new Vector<Integer>();
            for (int i = 0; i < strategy.size(); i++) {
                MetaReasoningAgent.TOCAction action = (MetaReasoningAgent.TOCAction)strategy.elementAt(i);
                if (action.type == null || action.id == null) {
                    Machinetta.Debugger.debug( 3,"Null in action ... ");
                }
                strategyStrings.add(action.type);
                strategyProxyIDs.add(action.id);
                strategyTimes.add(new Integer(action.time));
            }
            currentStep = a.getCurrentStrategyStep();
        } else {
            Machinetta.Debugger.debug( 0,"Meta-reasoning agent has no strategy ... ");
        }
        
        this.decisionOptions = a.getDecisionOptions();
        this.decisionInfo = a.getDecisionInformation();
        
        executing = a.isExecuting();
        decision = a.getDecision();
        
        bid = a.ids[0];
    }
    
    /**
     * Recreates the agent from the belief. <br>
     *
     * The underlying agent (if any) is kept as a belief.
     */
    public MetaReasoningAgent getAsAgent() {
        MetaReasoningAgent ma = new MetaReasoningAgent(this, agentB);
        
        ma.ids[0] = bid;

        // Recreate decision information
        ma.setDecisionInfo(decisionInfo);        
        ma.setDecisionOptions(decisionOptions);
        ma.setExecuting(executing); 
        ma.setDecision(decision);
        
        // Recreate strategy
        if (strategyStrings != null) {
            Vector<MetaReasoningAgent.TOCAction> strategy = new Vector<MetaReasoningAgent.TOCAction>();
            for (int i = 0; i < strategyStrings.size(); i++) {
                MetaReasoningAgent.TOCAction action = ma.createTOCAction(
                (String)strategyStrings.elementAt(i),
                (ProxyID)strategyProxyIDs.elementAt(i),
                ((Integer)strategyTimes.elementAt(i)).intValue());
                strategy.add(action);
            }
            ma.setStrategy(strategy);
            ma.setCurrentStrategyStep(currentStep);
        } else {
            Machinetta.Debugger.debug( 0,"No strategy received ... ");
            ma.createStrategy();
        }       
        
        ma.moveAgent = moveAgent;
                
        return ma;
    }
    
    public static final long serialVersionUID = 1L;
}

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
 * STEAMPolicy.java
 *
 * Created on August 29, 2002, 2:45 PM
 */

package Machinetta.Coordination.Policy;

import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.TeamBelief.*;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.Communication.TextMessage;
import Machinetta.Communication.Message;
import java.util.Vector;
import java.util.Iterator;
import java.util.Hashtable;

/**
 *
 * @author  pynadath
 */
public class STEAMPolicy extends CoordinationPolicy {
    
    /** Creates a new instance of STEAMPolicy */
    public STEAMPolicy() {
        super();
    }
    
    /** Method for handling the initiation of a new plan and generate any necessary actions
     * @param plan The new plan
     * @param triggers The conditions which triggered the initiation of this plan
     * @return A vector of actions to perform in response to this initiation
     */
    public Vector initiatePlan(TeamPlanBelief plan,Hashtable triggers) {
        Vector result = super.initiatePlan(plan,triggers);
        // First cut...just send this plan to everyone
        //String content = "<Beliefs><Belief>"+plan.toXML()+"</Belief></Beliefs>";
        String content = "<Beliefs><Belief>"+Machinetta.State.BeliefsXML.toXML(plan)+"</Belief></Beliefs>";
        for (Iterator teamIterator = teams.iterator(); teamIterator.hasNext(); ) {
            BeliefID teamID = (BeliefID)teamIterator.next();
            TeamBelief team = (TeamBelief)state.getBelief(teamID);
            for (Iterator memberIterator = team.getMembers(); memberIterator.hasNext(); ) {
                ProxyID member = (ProxyID)memberIterator.next();
                result.addElement(new TextMessage(member,content));
            }
        }
        return result;
    }
    
    /** Method for handling the termination of a plan and generate any necessary actions
     * @param plan The completed plan
     * @param triggers The conditions which triggered the termination of this plan
     * @return A vector of actions to perform in response to this termination
     */
    public Vector terminatePlan(TeamPlanBelief plan,Hashtable triggers) {
        Vector result = super.terminatePlan(plan,triggers);
        Machinetta.Debugger.debug(1,"Terminating plan...");
        /** Determine source of this new belief */
        String source = (String)plan.getParam("Source");
        /** Access team */
        TeamBelief team = (TeamBelief)state.getBelief(plan.getTeam());
        /** Determine team's approach to coordination */
        String approach = team.getApproach();
        /** Determine whether termination of this plan is already common knowledge among team members */
        String possibleNotCommonKnowledge = null;
        if (team.size() <= 1)
            possibleNotCommonKnowledge = VALUE_LOW;
        else if (approach.equalsIgnoreCase(CAUTIOUS_APPROACH))
            possibleNotCommonKnowledge = VALUE_HIGH;
        else if (approach.equalsIgnoreCase(NORMAL_APPROACH)) {
            if (source == null)
                /** If no source, then by default assume not common knowledge */
                possibleNotCommonKnowledge = VALUE_HIGH;
            else if (source.equalsIgnoreCase(PRIVATE_SOURCE))
                possibleNotCommonKnowledge = VALUE_HIGH;
            else if (source.equalsIgnoreCase(PUBLIC_SOURCE))
                possibleNotCommonKnowledge = VALUE_LOW;
            else
                Machinetta.Debugger.debug(3,"Unknown belief source: "+source);
        }
        else if (approach.equalsIgnoreCase(RECKLESS_APPRAOCH))
            possibleNotCommonKnowledge = VALUE_LOW;
        else
            Machinetta.Debugger.debug(3,"Unknown style of coordination: "+approach);
        /** If we haven't determined whether common knowledge or not, then there's a problem */
        if (possibleNotCommonKnowledge == null) {
            Machinetta.Debugger.debug(2,"Unable to determine whether common knowledge. Using default.");
            possibleNotCommonKnowledge = VALUE_HIGH;
        }
        Machinetta.Debugger.debug(1,"Prob(not common knowledge)="+possibleNotCommonKnowledge);
        /** Determine cost of miscoordinating in terminating this plan (by default, assume HIGH) */
        String costMiscoordination = (String)plan.getParam("CostOfMiscoordinatedTermination");
        if (costMiscoordination == null)
            costMiscoordination = VALUE_HIGH;
        /** Determine value of communication (alternatively, cost of not communicating) */
        String valueCommunication = null;
        if (possibleNotCommonKnowledge.equalsIgnoreCase(VALUE_HIGH))
            if (costMiscoordination.equalsIgnoreCase(VALUE_LOW))
                valueCommunication = VALUE_MEDIUM;
            else /** costMiscoordination is VALUE_MEDIUM or VALUE_HIGH */
                valueCommunication = VALUE_HIGH;
        else /** possibleNotCommonKnowledge == VALUE_LOW */
            if (costMiscoordination.equalsIgnoreCase(VALUE_HIGH))
                valueCommunication = VALUE_MEDIUM;
            else /** costMiscoordination is VALUE_MEDIUM or VALUE_LOW */
                valueCommunication = VALUE_LOW;
        Machinetta.Debugger.debug(1,"EU[Comm]="+valueCommunication);
        /** Look up cost of communication for this team */
        String costCommunication = (String)team.getParam("CommunicationCost");
        if (costCommunication == null)
            /** By default, assume low communication cost */
            costCommunication = VALUE_LOW;
        /** Finally, determine whether I should communicate or not */
        if ((valueCommunication.equalsIgnoreCase(VALUE_HIGH)) || (compareValues(valueCommunication,costCommunication) > 0))
            /** Communicate termination of plan */
            result.addAll(generateTeamMessages(plan.getTeam(),Machinetta.State.BeliefsXML.toXML(plan)));
        return result;
    }
    
    public Vector generateTeamMessages(BeliefID teamID,String content) {
        Vector messages = new Vector();
        TeamBelief team = (TeamBelief)state.getBelief(teamID);
        for (Iterator memberIterator = team.getMembers(); memberIterator.hasNext(); ) {
            ProxyID member = (ProxyID)memberIterator.next();
            messages.addElement(new TextMessage(member,content));
        }
        return messages;
    }
    
    /** Comparison function for qualitative values
     * @param value1 First string value for comparison
     * @param value2 Second string value for comparison
     * @return 0 if value1 == value2, 1 if value1 > value2, -1 if value1 < value2
     */
    public static final int compareValues(String value1,String value2) {
        if (value1.equalsIgnoreCase(VALUE_HIGH))
            if (value2.equalsIgnoreCase(VALUE_HIGH))
                return 0;
            else if (value2.equalsIgnoreCase(VALUE_MEDIUM))
                return 1;
            else
                return -1;
        else if (value1.equalsIgnoreCase(VALUE_MEDIUM))
            if (value2.equalsIgnoreCase(VALUE_HIGH))
                return -1;
            else if (value2.equalsIgnoreCase(VALUE_MEDIUM))
                return 0;
            else
                return 1;
        else if (value1.equalsIgnoreCase(VALUE_LOW))
            if (value2.equalsIgnoreCase(VALUE_LOW))
                return 0;
            else
                return -1;
        else
            /** Shouldn't reach this point */
            return 0;
    }
    
    public static final String VALUE_HIGH = "high";
    public static final String VALUE_MEDIUM = "medium";
    public static final String VALUE_LOW = "low";
    
    public static final String CAUTIOUS_APPROACH = "cautious";
    public static final String NORMAL_APPROACH = "normal";
    public static final String RECKLESS_APPRAOCH = "reckless";
    
    public static final String PRIVATE_SOURCE = "private";
    public static final String PUBLIC_SOURCE = "public";
}

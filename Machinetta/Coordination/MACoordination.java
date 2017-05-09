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
 * MACoordination.java
 *
 * Created on November 4, 2003, 9:41 AM
 */
package Machinetta.Coordination;

import Machinetta.Communication.*;
import Machinetta.Coordination.MAC.*;
import Machinetta.State.*;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.MAC.InformationAgentBelief;
import Machinetta.State.BeliefType.MAC.ResourceAgentBelief;
import Machinetta.State.BeliefType.MAC.RoleAgentBelief;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.RAPBelief;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import Machinetta.State.BeliefType.TeamBelief.FailedCommunication;
import Machinetta.State.BeliefType.TeamBelief.FailedProxy;
import Machinetta.State.BeliefType.TeamBelief.TeamBelief;
import java.util.ArrayList;

import org.w3c.dom.Document;
import java.util.Hashtable;
import java.io.*;
import java.util.Random;

/**
 *
 * @author  pscerri
 */
public class MACoordination extends CoordImplementation {

    /** This hashtable holds the agent that are currently at this proxy
     * Key is agent.makeComparisonString(..)
     * Value is the agent
     */
    public static Hashtable<String, Agent> agents = new Hashtable<String, Agent>();
    /** This hashtable records where agents that have visited have departed to
     * Key is agent.makeComparisonString(..)
     * Value is a DepartureRecord
     */
    public static Hashtable<String, DepartureRecord> leftAgents = new Hashtable<String, DepartureRecord>();
    /**
     * This is a hack for ensuring that the number of pending information agents is limited
     */
    public static int pendingFailedInfoAgents = 0;
    /** This is special storage for associate information agents
     */
    private static Hashtable<BeliefID, java.util.LinkedList<AssociateInformAgent>> associateInformAgents = new Hashtable<BeliefID, java.util.LinkedList<AssociateInformAgent>>();
    /** Agents are also stored in a sorted linked list, if their
     * uniqueID is > 0, to allow easy identification of agents
     * for the same plan.
     */
    private static java.util.LinkedList<Agent> agentList = new java.util.LinkedList<Agent>();
    private static ObjectOutputStream logFile = null;
    AgentFactory[] agentFactories = new AgentFactory[3];
    final int INFO_AGENT_FACTORY = 0;
    final int PLAN_AGENT_FACTORY = 1;
    final int META_REASONING_AGENT_FACTORY = 2;

    {
        agentFactories[INFO_AGENT_FACTORY] = new InformationAgentFactory();
        agentFactories[PLAN_AGENT_FACTORY] = new PlanAgentFactory();
        agentFactories[META_REASONING_AGENT_FACTORY] = new MetaReasoningAgentFactory();
    }
    /** This is for messages that clients tried to send before instantiation. */
    private static ArrayList<Machinetta.Communication.ObjectMessage> pending = null;
    private static Machinetta.Communication.Communication scomms;

    /** Creates a new instance of MACoordination */
    public MACoordination() {
        scomms = comms;

        // Just a test to see whether anyone tried to send messages before MACoordination was 
        // instantiated.  If so, send those messages now.
        if (pending != null) {
            for (Machinetta.Communication.ObjectMessage msg : pending) {
                Machinetta.Debugger.debug(1, "Sending message delayed");
                scomms.sendMessage(msg);
            }
            pending = null;
        }

        if (Machinetta.Configuration.MACOORDINATION_LOG_FILE != null) {
            try {
                logFile = new ObjectOutputStream(new FileOutputStream(Machinetta.Configuration.MACOORDINATION_LOG_FILE));
            } catch (IOException e) {
                Machinetta.Debugger.debug(3, "Failed to open mobile agent log file: " + e);
            }
        }
    }

    /** For MAC all incoming messages should actually be agents */
    public void incomingMessages(Message[] msgs) {
        for (int i = 0; i < msgs.length; i++) {

            // @todo: hack to test message dropping, this should be
            // Paul: This should be done in the switch, because more overall control can be exercised there.
            // configured by the .cnf file or something.
// 	    double MESSAGE_DROP_RATE = .00;
// 	    if(rand.nextDouble() < MESSAGE_DROP_RATE)
// 		continue;

            Belief[] agents = null;
            if (msgs[i] instanceof TextMessage) {
                TextMessage msg = (TextMessage) msgs[i];
                agents = interpretMessage(msg.getText());
            } else if (msgs[i] instanceof ObjectMessage) {
                agents = new Belief[1];
                agents[0] = (Belief) ((ObjectMessage) msgs[i]).o;
            }

            if (agents == null) {
                Machinetta.Debugger.debug(3, "Strange belief received: " + msgs[i]);
            } else {
                for (int j = 0; j < agents.length; j++) {
                    if (agents[j] != null) {
                        Machinetta.State.BeliefType.MAC.MABelief agent = (Machinetta.State.BeliefType.MAC.MABelief) agents[j];
                        try {
                            Machinetta.Debugger.debug(0, "Agent arriving : " + agent);
                        } catch (NullPointerException e) {
                            Machinetta.Debugger.debug(3, "Problem recreating an agent ... " + agent.getClass());
                            e.printStackTrace();
                        }
                        if (agent instanceof Machinetta.State.BeliefType.MAC.InformationAgentBelief) {
                            agentFactories[INFO_AGENT_FACTORY].createAgent(agent);
                        } else if (agents[j] instanceof Machinetta.State.BeliefType.MAC.PlanAgentBelief ||
                                agents[j] instanceof Machinetta.State.BeliefType.MAC.RoleAgentBelief ||
                                agents[j] instanceof Machinetta.State.BeliefType.MAC.AssociateInformBelief ||
                                agents[j] instanceof Machinetta.State.BeliefType.MAC.ConflictResolutionBelief ||
                                agents[j] instanceof Machinetta.State.BeliefType.MAC.JIAgentBelief) {
                            agentFactories[PLAN_AGENT_FACTORY].createAgent(agent);
                        } else if (agents[j] instanceof Machinetta.State.BeliefType.MAC.MetaReasoningBelief) {
                            agentFactories[META_REASONING_AGENT_FACTORY].createAgent(agent);
                        } else if (agents[j] instanceof Machinetta.State.BeliefType.MAC.ResourceAgentBelief) {
                            ResourceAgent ra = new ResourceAgent((ResourceAgentBelief) agents[j]);
                        }
                    }
                }
            }
        }
    }

    /** Tells both factories and individual agents about changes to state */
    public void proxyStateChange(BeliefID[] b) {

        for (int i = 0; i < agentFactories.length; i++) {
            agentFactories[i].stateChange(b);
        }

        for (java.util.Enumeration e = agents.elements(); e.hasMoreElements();) {
            Agent a = (Agent) e.nextElement();
            // Don't actually know why this happens.
            if (a != null) {
                a.stateChanged();
            }
        }

        // Deal with FailedCommunication
        for (BeliefID bid : b) {
            Belief bel = state.getBelief(bid);
            if (bel instanceof FailedCommunication) {
                Machinetta.Debugger.debug(1, "Dealing with FailedCommunication");

                Message msg = ((FailedCommunication) bel).getFailedMessage();
                // Who should have responsibility for resending various message types?
                // Now need to decide whether to send the message to someone else
                ObjectMessage omsg = (ObjectMessage) msg;

                if (((FailedCommunication) bel).isAddressed()) {
                    // Remove it, just to avoid hearing about it again ...
                    Machinetta.Debugger.debug(1, "Failed communication already dealt with");
                    state.removeBelief(bid);

                } else if (omsg.o.getClass().toString().equalsIgnoreCase("class Machinetta.State.BeliefType.MAC.InformationAgentBelief")) {

                    ((FailedCommunication) bel).setAddressed(true);
                    state.removeBelief(bid);                                        

                    // @todo Make this a configuration value
                    if (rand.nextDouble() > ((double) pendingFailedInfoAgents) / 100.0) {
                        // There is no particular reason this is handled here rather than in the InformationAgentFactory
                        // but then there would be no good reason to handle it there ...
                        // If I ever decide to move it, it is a simple cut and paste
                        Machinetta.Debugger.debug(1, "Handling failed information belief propogation, by resending");
                        InformationAgentBelief iBel = (InformationAgentBelief) omsg.o;
                        InformationAgent a = new InformationAgent(iBel.bel, iBel.toRAP, iBel.forRole);
                        a.setTTL(iBel.TTL);
                        a.reset();
                        addAgent(a);
                        
                        pendingFailedInfoAgents++;
                        
                        a.sendFailed();

                    } else {
                        Machinetta.Debugger.debug(4, "Dropping failed info agent send.");
                    }


                } else if (omsg.o.getClass().toString().equalsIgnoreCase("class Machinetta.State.BeliefType.MAC.RoleAgentBelief")) {

                    ((FailedCommunication) bel).setAddressed(true);

                    Machinetta.Debugger.debug(1, "Handling role agent move failure by resending");
                    RoleAgent ra = new RoleAgent((RoleAgentBelief) omsg.o, false);
                    addAgent(ra);
                    moveAgentRandomly(ra, true);

                } else if (omsg.o.getClass().toString().equalsIgnoreCase("class Machinetta.State.BeliefType.MAC.ResourceAgentBelief")) {

                    ((FailedCommunication) bel).setAddressed(true);

                    Machinetta.Debugger.debug(1, "Handing Resource agent move failure by resending");
                    ResourceAgentBelief rBel = (ResourceAgentBelief) omsg.o;
                    ResourceAgent ra = new ResourceAgent(rBel, true);

                } else {

                    ((FailedCommunication) bel).setAddressed(true);

                    Machinetta.Debugger.debug(3, "No action ... Failed message object is of type: " + omsg.o.getClass());
                }

                // Now decide whether to post a FailedProxy belief
                // For now, be extremely aggressive and assume this guy has failed
                FailedProxy nbel = new FailedProxy(msg.getDest());
                state.addBelief(nbel);
                state.notifyListeners();

            // Dealing with failed proxies is different and needs to be dealt with elsewhere
            // This is going to be very tricky, in general, because the proxy might have been
            // responsible for plans, etc.

            }
        }

    }

    public static void printAgents(int level) {
        if (Machinetta.Debugger.getDebugLevel() <= level) {
            Machinetta.Debugger.debug(level, "**** Mobile agents at proxy:");
            for (java.util.Enumeration e = agents.elements(); e.hasMoreElements();) {
                Machinetta.Debugger.debug(level, e.nextElement().toString());
            }
            Machinetta.Debugger.debug(level, "****");
        }
    }

    /** Processes an incoming message string and returns a set of actions to perform in response
     * @param message The incoming message string
     * @return A list of actions (e.g., messages, new beliefs)
     */
    protected Belief[] interpretMessage(String message) {
        Document doc = BeliefsXML.getDocumentFromString(message);
        if (doc == null) {
            return null;
        } else {
            Belief newBeliefs[] = BeliefsXML.getBeliefs(doc);
            return newBeliefs;
        }
    }
    private static java.util.Random rand = new java.util.Random();

    /** Moves agent to another proxy, as yet unvisited, if that exists
     *
     *
     */
    public static boolean moveAgentRandomlyOld(Agent a, boolean revisitOK) {

        // Check whether we have sent this agent anywhere before
        DepartureRecord record = leftAgents.get(a.makeComparisonString(a.getDefiningBeliefs()));

        ProxyState state = new ProxyState();

        // Some (OK, now only RoleAgents) might be intelligently routed
        ProxyID intelligentDest = null;
        if (a instanceof RoleAgent) {
            intelligentDest = intelligentRoute((RoleAgent) a, state, record);
        }

        if (intelligentDest != null) {
            Machinetta.Coordination.MACoordination.moveAgent(a, intelligentDest);
            return true;
        }

        // If intelligent routing failed, try random routing

        /** @fix Need to find a better way to get team belief from state,
         * since potentially there will be multiple team beliefs.
         */
        TeamBelief team = (TeamBelief) state.getBelief(new BeliefNameID("TeamAll"));
        if (team != null && team.size() > 0) {

            ProxyID selfID = state.getSelf().getProxyID();

            int c = 0, i = rand.nextInt(team.members.size());
            while (c < team.members.size() &&
                    (team.members.get(i).matches(selfID) || (record != null && record.haveDepartedTo(team.members.get(i))))) {
                c++;
                i = (i + 1) % team.members.size();
            }            
            
            if (c < team.members.size()) {
                Machinetta.Coordination.MACoordination.moveAgent(a, team.members.get(i));
                return true;
            } else if (revisitOK && !(team.members.size() == 1 && team.members.contains(selfID))) {
                do {
                    i = rand.nextInt(team.members.size());
                } while (team.members.get(i).matches(selfID));
                Machinetta.Coordination.MACoordination.moveAgent(a, team.members.get(i));
                return true;
            } else {
                Machinetta.Debugger.debug(1, "Agent has visited all RAPs" + a);
                return false;
            }

        } else {
            Machinetta.Debugger.debug(4, "Cannot work out team, therefore cannot move agent!! ");
            state.printState();
        }


        return false;
    }

    private static int [] checkOrder = null;
    
    public static boolean moveAgentRandomly(Agent a, boolean revisitOK) {

        // Check whether we have sent this agent anywhere before
        DepartureRecord record = leftAgents.get(a.makeComparisonString(a.getDefiningBeliefs()));

        ProxyState state = new ProxyState();

        // Some (OK, now only RoleAgents) might be intelligently routed
        ProxyID intelligentDest = null;
        if (a instanceof RoleAgent) {
            intelligentDest = intelligentRoute((RoleAgent) a, state, record);
        }

        if (intelligentDest != null) {
            Machinetta.Coordination.MACoordination.moveAgent(a, intelligentDest);
            return true;
        }

        // If intelligent routing failed, try random routing

        /** @fix Need to find a better way to get team belief from state,
         * since potentially there will be multiple team beliefs.
         */
        TeamBelief team = (TeamBelief) state.getBelief(new BeliefNameID("TeamAll"));
        if (team != null && team.size() > 0) {

            ProxyID selfID = state.getSelf().getProxyID();

            if (checkOrder == null || checkOrder.length != team.members.size()) {
                checkOrder = new int[team.members.size()];
                for (int i = 0; i < checkOrder.length; i++) {
                    checkOrder[i] = -1;
                }
                for (int i = 0; i < checkOrder.length; i++) {
                    int j = rand.nextInt(checkOrder.length);
                    while (checkOrder[j] >= 0) {
                        j = (++j) % checkOrder.length;
                    }
                    checkOrder[j] = i;
                }
                Machinetta.Debugger.debug(0, "New check order created");
            }
            
            int possibleFailed = -1, possibleRevisit = -1;
            int toSend = -1;
            boolean found = false;
            
            for (int i = 0; !found && i < checkOrder.length; i++) {
                int j = checkOrder[i];

                // Don't send to self
                if (team.members.get(j).matches(selfID)) continue;
                // Try not to send to someone we have already sent to
                if (record != null && record.haveDepartedTo(team.members.get(j))) {
                    possibleRevisit = j;
                    continue;
                }
                // Check whether this proxy has failed
                FailedProxy fBel = (FailedProxy)state.getBelief(FailedProxy.makeID(team.members.get(j)));
                if (fBel != null) {
                    long time = System.currentTimeMillis();
                    long age = time - fBel.time;
                    
                    // If the failed proxy message is really old, ignore it, otherwise
                    // probabilistically send to this proxy
                    if (age < 10000) {
                        if (rand.nextInt(10000) < age) {
                            possibleFailed = j;
                            Machinetta.Debugger.debug(1, "RANDOMROUTE Possibly sending failed: " + team.members.get(j) + " age: " + age);                            
                        } else {
                            Machinetta.Debugger.debug(1, "RANDOMROUTE Probabilistically not sending to failed: " + team.members.get(j) + " age: " + age);                            
                        }
                        continue;
                    } else {
                        Machinetta.Debugger.debug(1, "RANDOMROUTE Ignoring old failed belief: " + age);                        
                    }
                } else {
		    //                    Machinetta.Debugger.debug(1, "RANDOMROUTE No failed belief for: " + team.members.get(j));
                }
                
                toSend = j;
                found = true;
            }
            
            // @todo Mix the order up a bit
            
            boolean preferRevisit = rand.nextBoolean();
            
            if (found) {
                Machinetta.Coordination.MACoordination.moveAgent(a, team.members.get(toSend));
		//                Machinetta.Debugger.debug(1, "RANDOMROUTE Sent to available team mate: " + team.members.get(toSend));
                return true;
            } else if (possibleFailed >= 0 && !(preferRevisit && possibleRevisit >= 0 && revisitOK)) {
                Machinetta.Coordination.MACoordination.moveAgent(a, team.members.get(possibleFailed));
                Machinetta.Debugger.debug(1, "RANDOMROUTE Sent to known failing: " + team.members.get(possibleFailed));
                return true;
            } else if (revisitOK && possibleRevisit >= 0) {                
                Machinetta.Coordination.MACoordination.moveAgent(a, team.members.get(possibleRevisit));
                Machinetta.Debugger.debug(1, "RANDOMROUTE revisiting: " + team.members.get(possibleRevisit));
                return true;            
            } else {
                Machinetta.Debugger.debug(1, "Agent has visited all RAPs" + a);
                return false;
            }

        } else {
            Machinetta.Debugger.debug(4, "Cannot work out team, therefore cannot move agent!!");
	    if(team == null)
		Machinetta.Debugger.debug(4, "        team == null");
	    else
		Machinetta.Debugger.debug(4, "        team.size == "+team.size());
            Machinetta.Debugger.debug(4, "        agent="+a);
	    if(a instanceof InformationAgent) {
		InformationAgent ia = (InformationAgent)a;
		RoleBelief rb = ia.getForRole();
		String rbString = "null";
		if(null != rb)
		    rbString = rb.toString();
		Machinetta.Debugger.debug(4, "        agent is informationAgent, Belief='"+ia.getBelief()+"', forRole='"+rbString+"'");
	    }
            Machinetta.Debugger.debug(4, "PRINTING STACK TRACE.");
	    (new Exception()).printStackTrace();
            Machinetta.Debugger.debug(4, "DONE PRINTING STACK TRACE.");
            state.printState();
        }


        return false;
    }
    
    /**
     * RoleAgents could be intelligently routed to only those team mates
     * that might have the required capability. If such information is known.
     *
     * @return ProxyID to route to or null if not helpful
     */
    private static ProxyID intelligentRoute(RoleAgent ra, ProxyState state, DepartureRecord record) {

        RAPBelief[] raps = state.getAllRAPBeliefs();
        ProxyID selfID = state.getSelf().getProxyID();

        int c = 0, i = rand.nextInt(raps.length);
        while (c < raps.length) {
            if (!(raps[i].getProxyID().matches(selfID) || (record != null && record.haveDepartedTo(raps[i].getProxyID()))) &&
                    raps[i].canPerformRole(ra.role)) {
                Machinetta.Debugger.debug(1, "intelligentRoute decided to send " + ra + " to " + raps[i]);
                return raps[i].getProxyID();
            } else {
                Machinetta.Debugger.debug(0, "intelligentRoute decided against " + raps[i] + " " + c + " of " + raps.length);
            }
            c++;
            i = (i + 1) % raps.length;
        }

        return null;
    }

    /**
     * This method allows an Agent to move from one proxy
     * to another.
     */
    public static void moveAgent(Agent a, ProxyID dest) {
        Machinetta.Debugger.debug((a instanceof InformationAgent ? 0 : 1), "Agent Leaving for " + dest + ": " + a);
        if (dest.equals(state.getSelf().getProxyID())) {
            // Moving to self ... doesn't make much sense for any agent type except JIAgent (where it is for simplicity)
            if (a instanceof JIAgent) {
                ((JIAgent) a).act();
            } else {
                Machinetta.Debugger.debug(4, "Why is " + a + " trying to move to self?");
            }

        } else {
            // Add the visit to the agents internal information
            a.proxyVisited();

            // Remove from list of agents currently at this proxy
            //agents.remove(idStr);
            removeAgent(a);

            String idStr = Agent.makeComparisonString(a.getDefiningBeliefs());

            /*
            Machinetta.Debugger.debug(1,"After move");
            printAgents(1);
             */

            // Record the departure in departure records to help future agents
            DepartureRecord dep = (DepartureRecord) leftAgents.get(idStr);
            if (dep == null) {
                dep = new DepartureRecord();
                leftAgents.put(idStr, dep);
            }
            dep.addDeparture(dest);

            // Use serialization to get message to destination quickly
            Machinetta.Communication.ObjectMessage msg = new Machinetta.Communication.ObjectMessage(dest, a.getAgentAsBelief());

            // @fix Need to choose message criticalities better than this ...
            if (a instanceof InformationAgent) {
                msg.setCriticality(Message.Criticality.NONE);
            } else if (a instanceof RoleAgent) {
                msg.setCriticality(Message.Criticality.HIGH);
            }

            if (scomms != null) {
                scomms.sendMessage(msg);
                Machinetta.Debugger.debug(0, "Sending " + msg + " to " + dest);
            } else {
                Machinetta.Debugger.debug(1, "Comms not ready");
                if (pending == null) {
                    pending = new ArrayList<Machinetta.Communication.ObjectMessage>();
                }
                pending.add(msg);
            }
        }
    }

    /** Add an agent to the list of those currently at this proxy
     *
     * returns true iff the added agent was merged with another.
     */
    public static boolean addAgent(Agent a) {

        boolean merged = false;
        String idStr = Agent.makeComparisonString(a.getDefiningBeliefs());

        synchronized (agents) {
            Machinetta.Debugger.debug(0, "Adding : " + a + " : " + idStr);
            if (agents.get(idStr) != null) {
                Machinetta.Debugger.debug(4, "WARNING Identical agent arrived at proxy: " + a + ", merging");
                Agent old = (Agent) agents.get(idStr);
                old.merge(a);
                merged = true;
            } else {
                agents.put(idStr, a);
            }
        }

        // Also add the agent to the sorted list
        if (!merged && a.uniqueID > 0) {
            synchronized (agentList) {
                int i = -1, curr = 0;
                for (java.util.ListIterator li = agentList.listIterator(); curr < a.uniqueID;) {
                    if (li.hasNext()) {
                        curr = ((Agent) li.next()).uniqueID;
                    } else {
                        curr = Integer.MAX_VALUE;
                    }
                    i++;
                }
                if (i == -1) {
                    i = 0;
                }
                agentList.add(i, a);
            }
        }

        // Store associate agents separately, with plan id as key
        if (!merged && a instanceof AssociateInformAgent) {
            AssociateInformAgent ai = (AssociateInformAgent) a;
            java.util.LinkedList<AssociateInformAgent> list = (java.util.LinkedList<AssociateInformAgent>) associateInformAgents.get(ai.getPlan().getID());
            if (list == null) {
                list = new java.util.LinkedList<AssociateInformAgent>();
                associateInformAgents.put(ai.getPlan().getID(), list);
            }
            list.add(ai);
        }


        // Logging
        if (logFile != null) {
            Agent toWrite = a;
            if (merged) {
                toWrite = (Agent) agents.get(idStr);
            }
            try {
                logFile.writeObject(toWrite);
            } catch (IOException e) {
                Machinetta.Debugger.debug(3, "Failed to write agent to logfile: " + e);
            }
        }

        return merged;
    }

    /** Remove an agent, call only when agent is finished its work */
    public static void removeAgent(Agent a) {
        String idStr = Agent.makeComparisonString(a.getDefiningBeliefs());

        Machinetta.Debugger.debug(0, "Removing : " + a + "  " + idStr);

        synchronized (agents) {
            // System.out.println("Keys are: " + agents.keySet());
            if (agents.remove(idStr) == null) {
                Machinetta.Debugger.debug(3, "Attempted to remove agent not at proxy: " + a + "  " + idStr);
                System.out.println("Keys were: " + agents.keySet());
            }
        }
        if (a instanceof AssociateInformAgent) {
            AssociateInformAgent ai = (AssociateInformAgent) a;
            java.util.LinkedList list = (java.util.LinkedList) associateInformAgents.get(ai.getPlan().getID());
            if (list == null || !list.remove(ai)) {
                Machinetta.Debugger.debug(3, "Attempted to remove associate inform agent not at proxy: " + a);
            }
        }
        synchronized (agentList) {
            if (a.uniqueID > 0) {
                agentList.remove(a);
            }
        }
    }

    /** Get an agent, if it exists with the given comparison string */
    public static Agent getAgent(String comparisonString) {
        return (Agent) agents.get(comparisonString);
    }

    /** Gets agents with same uniqueID, meaning that they are related to the same plan. */
    public static java.util.LinkedList<Agent> getAgentsForPlan(int uniqueID) {
        java.util.LinkedList<Agent> list = new java.util.LinkedList<Agent>();
        //Machinetta.Debugger.debug(3,"Looking for uniqueID: " + uniqueID);
        int curr = 0;
        Agent a = null;
        synchronized (agentList) {
            java.util.ListIterator li = agentList.listIterator();
            for (; curr < uniqueID && li.hasNext();) {
                //Machinetta.Debugger.debug(3,"skipping : " + a + " with " + curr);
                a = (Agent) li.next();
                curr = a.uniqueID;
            }
            while (curr == uniqueID) {
                //Machinetta.Debugger.debug(3,"adding :" + a + " with " + curr);
                list.add(a);
                if (li.hasNext()) {
                    a = (Agent) li.next();
                    curr = a.uniqueID;
                } else {
                    curr = -1;
                }
            }
        }

        //Machinetta.Debugger.debug(3,"Did not include: " + a + " with " + curr)
        //Machinetta.Debugger.debug(3,"Done");
        return list;
    }

    /** Check whether an agent defined by these beliefs is currently at this
     * proxy
     */
    public static boolean agentExists(Machinetta.State.BeliefID[] bids) {
        return (agents.get(Agent.makeComparisonString(bids)) != null);
    }

    /** Check whether an agent defined by these beliefs was ever here */
    public static boolean agentWasHere(Machinetta.State.BeliefID[] bids) {
        return (leftAgents.get(Agent.makeComparisonString(bids)) != null);
    }

    /** Check whether this agent has left for dest before. */
    public static boolean agentHasLeftFor(Machinetta.State.BeliefID[] bids, ProxyID dest) {
        DepartureRecord rec = (DepartureRecord) leftAgents.get(Agent.makeComparisonString(bids));
        if (rec == null) {
            return false;
        } else {
            return rec.haveDepartedTo(dest);
        }
    }

    /** Returns a linked list of conflicting associate inform agents (or null) */
    public static java.util.LinkedList getConflicting(AssociateInformAgent a) {
        java.util.LinkedList list = (java.util.LinkedList) associateInformAgents.get(a.getPlan().getID());
        if (list == null) {
            return null;
        }
        java.util.LinkedList ret = (java.util.LinkedList) list.clone();
        for (int i = 0; i < ret.size(); i++) {
            AssociateInformAgent ai = (AssociateInformAgent) ret.get(i);
            if (ai.uniqueID == a.uniqueID) {
                ret.remove(i);
            }
        }
        if (ret.size() == 0) {
            return null;
        } else {
            return ret;
        }
    }
}

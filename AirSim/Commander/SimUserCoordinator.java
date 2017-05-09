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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package AirSim.Commander;

import AirSim.UnitIDs;

import AirSim.Machinetta.SimTime;
import Machinetta.Debugger;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanTemplate;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.RAPBelief;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import Machinetta.State.BeliefNameID;
import AirSim.Machinetta.Beliefs.ProxyEventData;
import AirSim.Machinetta.Beliefs.ProxyEventData.EventType;
import AirSim.Machinetta.BasicRole;
import AirSim.Machinetta.Point2D;
import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Vector3D;
import Machinetta.State.BeliefType.Match.MatchCondition;
import Machinetta.Coordination.MAC.PlanAgent;
import AirSim.Configs.TPTFactory;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * 
 * This class mainly deals with the adaptive intelligence for SimUser.
 * Specifically, this is monitoring the world states, invoking the
 * subplans of given scenario, and adding the intelligence - reinvoke
 * the plans or generate the alternative plans to compensate the
 * failure - to the system based on the rules.
 * @author junyounk
 */
public class SimUserCoordinator extends Thread {

    private SimUserUtils suu;
    private SimUserSubTeams sust;

    private boolean isFinished = false;    
    private WorldStateMgr wsm = null;
    private int currentStepNum = 0;

    boolean uavIsrPhaseOver = false;
    boolean attackPhaseStarted = false;
    private class PlanData {
	String key;
	long timeStarted = System.currentTimeMillis();
	PlanAgent agent = null;
	boolean dropped = false;
	public String toString() {
	    return "PlanData: key="+key
		+" started="+timeStarted
		+" dropped="+dropped
		+" planagent="+((null == agent) ? "null" : agent)
		;
	}
    }
    private static final long PLAN_EXPIRATION_MS = 90 * 1000;

    private HashMap<String, PlanData> planMap = null;
            
    private ArrayList<ScenarioPhase> unrunPhases = new ArrayList<ScenarioPhase>();
    private ArrayList<ScenarioPhase> runningPhases = new ArrayList<ScenarioPhase>();
    private ArrayList<ScenarioPhase> readyForNextPhases = new ArrayList<ScenarioPhase>();
    private ArrayList<ScenarioPhase> donePhases = new ArrayList<ScenarioPhase>();

    private long timeStart = System.currentTimeMillis();

    private Random rand = new Random();

    /**
     * Initialize SimUserCoordinator
     * 
     * 
     * @param wsm To monitor the world state
     */
    public SimUserCoordinator(WorldStateMgr wsm, SimUserSubTeams sust) {
        this.wsm = wsm;
	this.sust = sust;

        //anything to initialize?
        planMap = new HashMap<String, PlanData>();
        suu = new SimUserUtils(wsm);

	makePhases();
    }
    
    public void makePhases() {
//  	// @TODO: HACK HACK HACK  Just to test DIAssautl without waiting forever.
//  	unrunPhases.add(new PhaseDIAirliftAndConvoy(6,wsm,suu,sust));
//  	unrunPhases.add(new PhaseDIAssault(7,wsm,suu,sust));

	unrunPhases.add(new PhaseUavIsrAndPrepFires(1,wsm,suu,sust));
	unrunPhases.add(new PhaseUavOverwatchAndCas(2,wsm,suu,sust));
	if(!SimConfiguration.SMALL_ACCAST_SCENARIO) {
	    unrunPhases.add(new PhaseIMAirdrop(3,wsm,suu,sust));
	    unrunPhases.add(new PhaseIMDeploy(4,wsm,suu,sust));
	}
	unrunPhases.add(new PhaseAUGVDeploy(5,wsm,suu,sust));
	unrunPhases.add(new PhaseDIAirliftAndConvoy(6,wsm,suu,sust));
	unrunPhases.add(new PhaseDIAssault(7,wsm,suu,sust));
    }
    
    /**
     * Returns the number of the group or set of subplans
     * 
     * 
     * @return mode To indiciate the group or set of subplans to invoke
     * 
     */
    public int getCurrentStepNum() {
        return currentStepNum;
    }
    
    /**
     * Sets the number of the group or set of subplans
     * 
     * 
     * @param mode To indiciate the group or set of subplans to invoke
     */
    public void setCurrentStepNum(int mode) {
        this.currentStepNum = mode;
    }
    
    /**
     * Needed to implement to Thread
     */
    public void run() {
	try {
	    sleep (10000);
	} catch (InterruptedException e) {}

	// ----------------------------------------------------------------------
	// TESTING CODE FOR MetaTeamPlanTemplates!  
	boolean testMTPT = false;
	if(testMTPT) {
	    TPTFactory tptFactory = new AirSim.Configs.TPTFactory();;

	    ArrayList areaList = new ArrayList<AirSim.Environment.Area>();
	    areaList.add(new AirSim.Environment.Area(0,0,1000,1000));
	    areaList.add(new AirSim.Environment.Area(1000,0,2000,1000));
	    areaList.add(new AirSim.Environment.Area(2000,0,3000,1000));
	    areaList.add(new AirSim.Environment.Area(3000,0,4000,1000));
	    areaList.add(new AirSim.Environment.Area(4000,0,5000,1000));
	    //	    AirSim.Configs.TPTFactory tptFactory = new AirSim.Configs.TPTFactory();
	    tptFactory.test5(areaList);

	    // Wait for 300 more seconds to see if test works
	    try {
		sleep(300* 1000);
	    } catch (InterruptedException ex) {
		//            ex.printStackTrace();
	    }
        
	}
	// ----------------------------------------------------------------------


	// Phases can be;
	//
	// 1) unrun - residing in an ordered list are all of hte
	// phases yet to run
	// 
	// 2) running - residing in an list (ordered list, but it
	// shouldn't matter), the running phases are what we watch to
	// see when we're ready to run another one.
	// 
	// 3) running and ready for next phase - it's still running
	// but we can go ahead and start the next unrun phase.  We
	// move the running phase to the readyForNextPhase list so
	// that we only start another phase once in response to it
	// being ready.
	//
	// 4) done - we can forget about it at this point other than
	// calling endPhase() to do any cleanup.
	//
	// 5) if at any time runningPhases is empty, start the next
	// available phase in unrunPhases.
	//
	// multiple phases may run concurrently - i.e. after we start
	// the UAV overwatch phase, that phase will simply stay
	// running the entire scenario,

	Debugger.debug(1,"run: Running...");
	if(!SimConfiguration.PLAN_CREATION_AUAV_ATTACK_DI )
	    Debugger.debug(1,"run: PLAN_CREATION_AUAV_ATTACK_DI is false");

	if(!SimConfiguration.PLAN_CREATION_AUGV_ATTACK_DI )
	    Debugger.debug(1,"run: PLAN_CREATION_AUGV_ATTACK_DI is false");

	if(!SimConfiguration.PLAN_CREATION_AUGV_OR_AUAV_ATTACK_DI )
	    Debugger.debug(1,"run: PLAN_CREATION_AUGV_OR_AUAV_ATTACK_DI is false");

        while(!isFinished) {
            try {

 		checkForPlansToCreate();

		int runNext = 0;
		
		// if(runningPhases) is empty, then we need to start a
		// phase.
		if(0 == runningPhases.size()) {
		    Debugger.debug(1,"run: runningPhases is empty...");
		    runNext++;
		}

		// Check if any of the phases in runningPhases are
		// readyForNextPhase, if so, move it to
		// readyForNextPhase and get the next phase from the
		// head of unrunPhases and start it.
		for (Iterator<ScenarioPhase> i = runningPhases.iterator(); i.hasNext(); ) {
		    ScenarioPhase phase = i.next();
		    if(phase.readyForNextPhase()) {
			if(phase instanceof PhaseUavIsrAndPrepFires) {
			    uavIsrPhaseOver = true;
			}
			i.remove();
			runNext++;
			Debugger.debug(1,"run: Phase "+phase.getClass().toString()+" is ready for next phase, removed from runningPhases and added to readyForNextPhases.");
			readyForNextPhases.add(phase);
		    }
		}

		// Check if any of the phases in readyForNextPhase is
		// done, if so, move it to donePhases and call
		// endPhase() on it.
		for (Iterator<ScenarioPhase> i = readyForNextPhases.iterator(); i.hasNext(); ) {
		    ScenarioPhase phase = i.next();
		    if(phase.phaseDone()) {
			i.remove();
			donePhases.add(phase);
			Debugger.debug(1,"run: Phase "+phase.getClass().toString()+" is done, ending.");
			phase.endPhase();
		    }
		}

		if(runNext > 0) {
		    Debugger.debug(1,"run: Starting next unrun phase because of prior phase ready for next phase.");
		    startNextUnrunPhase();
		}


                //do we need to sleep here?
                sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(SimUserCoordinator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void startNextUnrunPhase() {
	if(0 == unrunPhases.size())
	    return;
	ScenarioPhase phase = unrunPhases.get(0);
	unrunPhases.remove(0);
	if(phase instanceof PhaseDIAssault) {
	    attackPhaseStarted = true;
	}

	long elapsed = System.currentTimeMillis() - timeStart;
	Debugger.debug(1,"startNextUnrunPhase: Starting phase "+phase.getClass().toString()+" time since start "+elapsed+" simtime "+SimTime.getEstimatedTime());
	phase.instantiatePhasePlans();
	runningPhases.add(phase);
    }

    private String makePlanKey(double x, double y) {
	return ((int)x)/10 + ","+((int)y)/10;
    }
         
    private void makeStrikePlan(StateData sd) {
	String key = makePlanKey(sd.getLocation().x,sd.getLocation().y);
	boolean foundKey;
	synchronized(planMap) {
	    foundKey = planMap.containsKey(key);
	}
	if(foundKey) {
	    Debugger.debug(1,"makeStrikePlan: Already have plan "+key+" for target "+sd);
	    return;
	}

	PlanData pd = new PlanData();
	pd.key = key;
	synchronized(planMap) {
	    planMap.put(key, pd);
	}

	double roll = rand.nextDouble();
        if(roll < SimConfiguration.PLAN_CREATION_DROP_PROBABILITY) {
            Machinetta.Debugger.debug(1,"makeStrikePlan: dropped plan probability is "+SimConfiguration.PLAN_CREATION_DROP_PROBABILITY+", we dropped (failed to run) a UAV Strike plan on sd="+sd);
	    pd.dropped = true;
            return;
        }

	final PlanData fpd = pd;
	final StateData fsd = sd;
	new Thread() {
	    public void run() {
		Machinetta.Debugger.debug("makeStrikePlan: Running strike plan, delayed for "+SimConfiguration.PLAN_CREATION_REACTION_TIME_MS,3,this);
		if(SimConfiguration.PLAN_CREATION_REACTION_TIME_MS > 0) {
		    try {Thread.sleep((long)SimConfiguration.PLAN_CREATION_REACTION_TIME_MS);} catch (InterruptedException e) {}
		}
		fpd.timeStarted = System.currentTimeMillis();
		fpd.agent = suu.createStrikePlan((int)fsd.getLocation().x,(int)fsd.getLocation().y);
	    }
	}.start();
    }

    private void makeUGVAttackPlan(StateData sd) {
	String key = makePlanKey(sd.getLocation().x,sd.getLocation().y);
	boolean foundKey;
	synchronized(planMap) {
	    foundKey = planMap.containsKey(key);
	}
	if(foundKey) {
	    Debugger.debug(1,"makeUGVAttackPlan: Already have plan "+key+" for target "+sd);
	    return;
	}

	PlanData pd = new PlanData();
	pd.key = key;
	synchronized(planMap) {
	    planMap.put(key, pd);
	}

	double roll = rand.nextDouble();
        if(roll < SimConfiguration.PLAN_CREATION_DROP_PROBABILITY) {
            Machinetta.Debugger.debug(1,"makeUGVAttackPlan: dropped plan probability is "+SimConfiguration.PLAN_CREATION_DROP_PROBABILITY+", we dropped (failed to run) a UGV Attack plan on sd="+sd);
            pd.dropped = true;
	    return;
        }

	final PlanData fpd = pd;
	final StateData fsd = sd;
	new Thread() {
	    public void run() {
		Machinetta.Debugger.debug("makeUGVAttackPlan: Running strike plan, delayed for "+SimConfiguration.PLAN_CREATION_REACTION_TIME_MS,3,this);
		if(SimConfiguration.PLAN_CREATION_REACTION_TIME_MS > 0) {
		    try {Thread.sleep((long)SimConfiguration.PLAN_CREATION_REACTION_TIME_MS);} catch (InterruptedException e) {}
		}
		fpd.timeStarted = System.currentTimeMillis();
		fpd.agent = suu.createUGVAttackPlan((int)fsd.getLocation().x,(int)fsd.getLocation().y);
	    }
	}.start();

    }

    private void makeAttackFromAirOrGroundPlan(StateData sd) {
	String key = makePlanKey(sd.getLocation().x,sd.getLocation().y);
	boolean foundKey;
	synchronized(planMap) {
	    foundKey = planMap.containsKey(key);
	}
	if(foundKey) {
	    Debugger.debug(1,"makeAttackFromAirOrGroundPlan: Already have plan "+key+" for target "+sd);
	    return;
	}

	PlanData pd = new PlanData();
	pd.key = key;
	synchronized(planMap) {
	    planMap.put(key, pd);
	}

	double roll = rand.nextDouble();
        if(roll < SimConfiguration.PLAN_CREATION_DROP_PROBABILITY) {
            Machinetta.Debugger.debug(1,"makeAttackFromAirOrGroundPlan: dropped plan probability is "+SimConfiguration.PLAN_CREATION_DROP_PROBABILITY+", we dropped (failed to run) a UAV/UGV Attack plan on sd="+sd);
            pd.dropped = true;
	    return;
        }

	final PlanData fpd = pd;
	final StateData fsd = sd;
	new Thread() {
	    public void run() {
		Machinetta.Debugger.debug("makeAttackFromAirOrGroundPlan: Running strike plan, delayed for "+SimConfiguration.PLAN_CREATION_REACTION_TIME_MS,3,this);
		if(SimConfiguration.PLAN_CREATION_REACTION_TIME_MS > 0) {
		    try {Thread.sleep((long)SimConfiguration.PLAN_CREATION_REACTION_TIME_MS);} catch (InterruptedException e) {}
		}
		fpd.timeStarted = System.currentTimeMillis();
		fpd.agent = suu.createAttackFromAirOrGroundPlan((int)fsd.getLocation().x,(int)fsd.getLocation().y);
	    }
	}.start();

    }

    // Go through our states looking for anything to trigger creation
    // of a plan...  really this would be better off being reactive,
    // i.e .in reaction to new beliefs coming in.  Oh well.
    private void checkForPlansToCreate() {
	StateData[] stateData = null;

	stateData = wsm.searchStates(.60, Asset.Types.SA9);
	Debugger.debug(1,"checkForPlansToCreate: Checking for strike plans to create, found "+stateData.length+" targets");
	if(stateData.length > 0) {
	    for(int loopi = 0; loopi < stateData.length; loopi++) {
		makeStrikePlan(stateData[loopi]);
	    }
	}
	
	// WE want to hold off on issuing UGVAttacks until our UGV's
	// are in the right neighborhood.
	if(SimConfiguration.PLAN_CREATION_AUAV_ATTACK_DI 
	   || (attackPhaseStarted && SimConfiguration.PLAN_CREATION_AUGV_ATTACK_DI)
	   || (attackPhaseStarted && SimConfiguration.PLAN_CREATION_AUGV_OR_AUAV_ATTACK_DI)
	   ) {
	    stateData = wsm.searchStates(.80, Asset.Types.INFANTRY);
	    if(stateData.length > 0) {
		Debugger.debug(1,"checkForPlansToCreate: Checking for DI strike/UGV attack plans to create, found "+stateData.length+" targets");
		for(int loopi = 0; loopi < stateData.length; loopi++) {
		    if(SimConfiguration.PLAN_CREATION_AUAV_ATTACK_DI) {
			makeStrikePlan(stateData[loopi]);
		    }
		    else if(attackPhaseStarted && SimConfiguration.PLAN_CREATION_AUGV_ATTACK_DI) {
			makeUGVAttackPlan(stateData[loopi]);
		    }
		    else if(attackPhaseStarted && SimConfiguration.PLAN_CREATION_AUGV_OR_AUAV_ATTACK_DI) {
			makeAttackFromAirOrGroundPlan(stateData[loopi]);
		    }
		}
	    }
	    else {
		Debugger.debug(1,"checkForPlansToCreate: Found 0 DI with confidence high enough to order attack.");
		
	    }
	}

	// Now clean up old plans?
	if(planMap.size() > 0) {
	    String[] keys;
	    synchronized(planMap) {
		keys = planMap.keySet().toArray(new String[0]);
	    }
	    for(String key: keys) {
		PlanData pd = planMap.get(key);;
		if(pd.agent != null) {
		    if(pd.agent.terminated) {
			Debugger.debug(1,"checkForPlansToCreate: Removing terminated plan "+pd.agent);
			planMap.remove(key);
		    }
		    else if((System.currentTimeMillis() - pd.timeStarted)> PLAN_EXPIRATION_MS) {
			Debugger.debug(1,"checkForPlansToCreate: plan expired, removing/terminating plan "+pd);
			planMap.remove(key);
			pd.agent.terminate();
		    }
		}
	    }
	}
    }

    /**
     * Deals with the uncertainties or reacts to any changes of a state around SimUser module
     * 
     * 
     * @param belief Receive this belief from SimUser main module, and pass to the SimUserUtils module
     */
    public void manageSimUser(Belief belief) {
        suu.manageSimUser(belief);
	if(belief instanceof ProxyEventData) {
	    ProxyEventData ped = (ProxyEventData) belief;
	    if(ped.type == EventType.ATTACK_FROM_AIR_EXECUTED) {
		String key = makePlanKey(ped.location3D.x, ped.location3D.y);
		synchronized(planMap) {
		    PlanData pd = planMap.remove(key);
		    Debugger.debug(1,"manageSimUser: received ProxyEventData "+ped+" Removing terminated plan "+pd.agent);
		}
	    }
	}
    }

}



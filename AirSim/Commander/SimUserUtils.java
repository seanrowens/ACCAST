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
import AirSim.Environment.Area;
import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.State;
import AirSim.Environment.Assets.Asset.Types;
import AirSim.Environment.Assets.Munition;
import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Environment.Env;
import AirSim.Environment.Vector3D;
import AirSim.Machinetta.BasicRole;
import AirSim.Machinetta.Beliefs.GeoLocateRequest;
import AirSim.Machinetta.Beliefs.GeoLocateResult;
import AirSim.Machinetta.Beliefs.ImageData;
import AirSim.Machinetta.Beliefs.Location;
import AirSim.Machinetta.Beliefs.TMAScanResult;
import AirSim.Machinetta.Beliefs.UAVLocation;
import AirSim.Machinetta.Beliefs.UGSSensorReading;
import AirSim.Machinetta.Beliefs.VehicleBelief;
import AirSim.Machinetta.NAI;
import AirSim.Machinetta.Path2D;
import AirSim.Machinetta.Path3D;
import AirSim.Machinetta.PlannedPath;

import Machinetta.Coordination.MAC.InformationAgent;
import Machinetta.Coordination.MACoordination;
import Machinetta.Coordination.MAC.PlanAgent;
import Machinetta.Debugger;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.RAPBelief;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanTemplate;
import Machinetta.State.ProxyState;

import Gui.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;


/**
 *
 * 
 * This class contains all kinds of utility methods for SimUser modules.
 * We can add more specific methods when we augment the capabilities of SimUser.
 *
 * @author junyounk
 */
public class SimUserUtils {
    private Random rand = new Random();
    private HashMap<Point,Object> strikeLocsOrdered = new HashMap<Point,Object>();
    private HashMap<Point,Object> patrolLocsOrdered = new HashMap<Point,Object>();
    static protected Env env = new Env();
    private WorldStateMgr wsm = null;

    private ProxyState state = new ProxyState();
    
    /**
     * Creates an instance of SimUserUtils
     */
    public SimUserUtils(WorldStateMgr wsm) {
	this.wsm = wsm;
    }
    
    /**
     * Main module to deal with beliefs
     *
     * When beliefs are added to ProxyState our stateChangeListener in
     * SimUserRI updates our world state (WorldStateMgr) and then
     * calls SimUserCoordinator.manageSimUser(belief) which then calls
     * here, so we can react intelligently as necessary to changing
     * conditions.
     * 
     * @param belief Any kind of Belief
     */
    public void manageSimUser(Belief belief) {
        // Here is the "intelligence of the human commander"
        if (belief instanceof VehicleBelief) {
            VehicleBelief vb = (VehicleBelief)belief;
	    handleVehicleBelief(vb);
	} else if (belief instanceof UGSSensorReading) {
            UGSSensorReading ub = (UGSSensorReading) belief;
	    handleUGSSensorReading(ub);
        }
        
        // any other beliefs?
        // we can add new beliefs or use the existing plans.
    }

    public void considerStrikePlan(VehicleBelief vb) {
	Debugger.debug(1, "Deciding to hit: " + vb);
                                
	boolean drop = dropPlan("strike, veh type="+vb.getType()+", confidence="+vb.getConfidence()+", loc="+vb.getX()+","+vb.getY()+", beliefid="+vb.id);
	if(drop)
	    return;
	strikeLocsOrdered.put(new Point(vb.getX()/100, vb.getY()/100), null);
	final int strikex = vb.getX();
	final int strikey = vb.getY();
	final long delay = reactionDelay();
	new Thread() {
	    
	    public void run() {
		Machinetta.Debugger.debug("Running strike plan, delayed for "+delay,3,this);
		if(delay > 0) {
		    try {Thread.sleep(delay);} catch (InterruptedException e) {}
		}
		createStrikePlan(strikex, strikey);
	    }
	}.start();
    }

    // @TODO: It looks like Jun intended that we respond to a
    // VehicleBelief (i.e. one of our proxies saw something - possibly
    // one our own guys) by possibly creating a plan to do something
    // (attack iet, etc) However this looks pretty broken as is.
    public void handleVehicleBelief(VehicleBelief vb) {
// 	VehicleData vd = wsm.addBeliefToFuser(vb);
// 	Asset curAsset = getAssetFromVehicleData(vd);
            
// 	if (vb.getType() == Asset.Types.SA9 && vb.getConfidence() > 0.0 &&
// 	    !strikeLocsOrdered.containsKey(new Point(vb.getX()/100, vb.getY()/100))) {
// 	    considerStrikePlan(vb);
// 	} else if(vb.getType() == Asset.Types.INFANTRY && vb.getConfidence() > 0.0) {

// 	    Asset targetAsset = getTargetAsset(vd);

// 	    //DirectFire, Defend, Flee, Move, RandomMove, Patrol, Mount/Dismount, and?
// 	    //When, where, with what kind of parameters?
// 	    //check tag with WorldStateMgr
// 	    int tag = 0;
                
// 	    if(targetAsset != null && tag == 0) { //if DirectFire is needed for infantry
// 		final String beliefNameID = "DirectFireToDest";
// 		final String planName = "DirectFire";
// 		final Hashtable<String, Object> params = new Hashtable<String, Object>();
                    
// 		//For test, just random point
// 		params.put("Destination", targetAsset.location);
                    
// 		new Thread() {
// 		    public void run() {
// 			Machinetta.Debugger.debug(1, "Running directFire plan, delayed for "+SimConfiguration.REACTION_DELAY_MS);
// 			if(SimConfiguration.REACTION_DELAY_MS > 0) {
// 			    try {Thread.sleep(SimConfiguration.REACTION_DELAY_MS);} catch (InterruptedException e) {}
// 			}
// 			createGeneralPlan(TaskType.attack, beliefNameID, planName, params);
// 		    }
// 		}.start();
                    
// 	    } else if (targetAsset != null && tag == 1) { // simple move
// 		final String beliefNameID = "MoveToDest";
// 		final String planName = "Move";
// 		final Hashtable<String, Object> params = new Hashtable<String, Object>();
                    
// 		//For test, just random point
// 		params.put("Destination", targetAsset.location);
                    
// 		new Thread() {
// 		    public void run() {
// 			Machinetta.Debugger.debug(1, "Running move plan, delayed for "+SimConfiguration.REACTION_DELAY_MS);
// 			if(SimConfiguration.REACTION_DELAY_MS > 0) {
// 			    try {Thread.sleep(SimConfiguration.REACTION_DELAY_MS);} catch (InterruptedException e) {}
// 			}
// 			createGeneralPlan(TaskType.move, beliefNameID, planName, params);
// 		    }
// 		}.start();
                    
// 	    } else if (curAsset != null && tag == 2) {
// 		final String beliefNameID = "MountAsset";
// 		final String planName = "Mount";
// 		final Hashtable<String, Object> params = new Hashtable<String, Object>();
                    
// 		//For test, just random point
// 		boolean isMount = true;
// 		params.put("isMount", isMount);
// 		params.put("mountedAsset", curAsset);
                    
// 		new Thread() {
// 		    public void run() {
// 			Machinetta.Debugger.debug(1, "Running mount plan, delayed for "+SimConfiguration.REACTION_DELAY_MS);
// 			if(SimConfiguration.REACTION_DELAY_MS > 0) {
// 			    try {Thread.sleep(SimConfiguration.REACTION_DELAY_MS);} catch (InterruptedException e) {}
// 			}
// 			createGeneralPlan(TaskType.mount, beliefNameID, planName, params);
// 		    }
// 		}.start();
                    
// 	    } else if (curAsset != null && tag == 3) {
// 		final String beliefNameID = "DismountAsset";
// 		final String planName = "Dismount";
// 		final Hashtable<String, Object> params = new Hashtable<String, Object>();
                    
// 		//For test, just random point
// 		boolean isMount = false;
// 		params.put("isMount", isMount);
// 		params.put("dismountedAsset", curAsset);
                    
// 		new Thread() {
// 		    public void run() {
// 			Machinetta.Debugger.debug(1, "Running dismount plan, delayed for "+SimConfiguration.REACTION_DELAY_MS);
// 			if(SimConfiguration.REACTION_DELAY_MS > 0) {
// 			    try {Thread.sleep(SimConfiguration.REACTION_DELAY_MS);} catch (InterruptedException e) {}
// 			}
// 			createGeneralPlan(TaskType.dismount, beliefNameID, planName, params);
// 		    }
// 		}.start();
                    
// 	    } // we can add more!
                
                
// 	} else if(vb.getType() == Asset.Types.SMALL_UAV && vb.getConfidence() > 0.0) {
// 	    //Circle or DirectMissile, Move, Land, Launch, Mount/DisMount, Refuel, and?, sensing?
// 	    int tag = 0;
                
// 	    if(tag == 0) { //if DirectFire is needed for infantry
// 		final String beliefNameID = "CircleAround";
// 		final String planName = "Circle";
// 		final Hashtable<String, Object> params = new Hashtable<String, Object>();
                    
// 		//For test, just random point
// 		final int posX = vb.getX();
// 		final int posY = vb.getY();
// 		params.put("Destination", new Point(posX, posY));
                    
// 		new Thread() {
// 		    public void run() {
// 			Machinetta.Debugger.debug(1, "Running circle plan, delayed for "+SimConfiguration.REACTION_DELAY_MS);
// 			if(SimConfiguration.REACTION_DELAY_MS > 0) {
// 			    try {Thread.sleep(SimConfiguration.REACTION_DELAY_MS);} catch (InterruptedException e) {}
// 			}
// 			createGeneralPlan(TaskType.circle, beliefNameID, planName, params);
// 		    }
// 		}.start();
                    
// 	    } // anything else
// 	    //else if {}
                
// 	}
// 	else if(vb.getType() == Asset.Types.UGV && vb.getConfidence() > 0.0) {
// 	    //DefendAir, Move, Patrol, Mount/Dismount, Refuel, and?
// 	    //add more
                
// 	} else if(vb.getType() == Asset.Types.C130 && vb.getConfidence() > 0.0) {
// 	    //Transport, Deploy, Refuel?
// 	    //add more
                
// 	} else if(vb.getType() == Asset.Types.IM && vb.getConfidence() > 0.0) {
// 	    //SpreadOut, Detonate, ?
// 	    //add more
                
// 	}
            
// 	//any other assets?
            
            
    }
 
    public void handleUGSSensorReading(UGSSensorReading ub) {
	if(ub.isPresent()) {
	    if(!patrolLocsOrdered.containsKey(new Point(ub.getX()/100, ub.getY()/100))) {
		boolean drop = dropPlan("patrol, time="+ub.getTime()+", loc="+ub.getX()+","+ub.getY()+", beliefid="+ub.id);
		if(!drop) {
		    patrolLocsOrdered.put(new Point(ub.getX()/100, ub.getY()/100), null);
		    final int patrolx = ub.getX();
		    final int patroly = ub.getY();
		    new Thread() {
			public void run() {
			    Machinetta.Debugger.debug("Running patrol plan, delayed for "+SimConfiguration.REACTION_DELAY_MS,3,this);
			    if(SimConfiguration.REACTION_DELAY_MS > 0) {
				try {Thread.sleep(SimConfiguration.REACTION_DELAY_MS);} catch (InterruptedException e) {}
			    }
			    createPatrolPlan(patrolx, patroly);
			}
		    }.start();
		}
	    }
	}

    }

    /**
     * Drops plans if DROPPED_PLAN_PROB is bigger than the randomly picked threshold value
     * Currently, this method is used only for logging purpose.
     * 
     * 
     * @param planDesc Plan description
     * @return The flag indicating whether the given plan is dropped
     */
    public boolean dropPlan(String planDesc) {
        
        Machinetta.Debugger.debug(1, "dropPlan!!!!!!1 in SimOperator!!!     " + planDesc);
        
        double roll = rand.nextDouble();
        if(roll < SimConfiguration.DROPPED_PLAN_PROB) {
            Machinetta.Debugger.debug("dropped plan "+planDesc, 3, this);
            return true;
        }
        Machinetta.Debugger.debug("creating plan "+planDesc, 3, this);
        return false;
    }
    
    // Make this a method in case later we want to do something more
    // clever with it, like make it a gaussian distribution.
    public long reactionDelay() {
	return SimConfiguration.REACTION_DELAY_MS;
    }


    /**
     * Generates PlanAgentTemplates with a vector of active plans
     * Given plans include pre & post condition, role, constraints, and parameter information.
     * Once a planAgentTemplate is generated, it is added to SimUser proxy state, and automatically allocated
     * by Machinetta system based on their roles.
     * 
     * 
     * @param newPlans Vector containing new plans that will be generate
     */
    public void generateNextTeamPlans(Vector newPlans) {
        Hashtable <String, Object> params = new Hashtable <String, Object>();
        Vector  preconditions = new Vector(),
                postconditions = new Vector(),
                roles = new Vector(),
                constraints = new Vector();
        
        for (int i=0; i<newPlans.size(); i++) {
            TeamPlanTemplate tpt = (TeamPlanTemplate) newPlans.get(i);
            
            //@NOTE: We do not have to think about checking pre-conditions of subplans.
            //       This work is done by Machinetta system automatically.  
            
            Machinetta.Debugger.debug(1, "SimUser: new plan is added to ProxyState");
            tpt.setLocallySensed(true);
            state.addBelief(tpt);
            state.notifyListeners();
        }
       
    }
  
    /**
     * Creates a hold plan using PlanAgent
     * 
     * 
     * @return Generated PlanAgent
     */
    public PlanAgent createHoldPlan() {
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        // Currently only a single strike on the target.
        BasicRole basic = new BasicRole(TaskType.hold);
        Hashtable<String, Object> params = new Hashtable<String, Object>();
        basic = (BasicRole)basic.instantiate(params);
        basic.constrainedWait = false;
        roles.add(basic);
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("WaitAtStart"), "Wait", null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
    }
    
    /**
     * Creates a move plan with a given destination value
     * 
     * 
     * @param x X value of the destination
     * @param y Y value of the destination
     * @return Generated PlanAgent
     */
    public PlanAgent createMovePlan(int x, int y) {
        Machinetta.Debugger.debug(1, "createMovePlan!!!!!! in SimOperator!!!");
        
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        BasicRole basic = new BasicRole(TaskType.move);
        Hashtable<String, Object> params = new Hashtable<String, Object>();
        params.put("Destination", new java.awt.Point(x,y));
        basic = (BasicRole)basic.instantiate(params);
        basic.constrainedWait = false;
        roles.add(basic);
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("MoveToDest"), "Move", null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
    }
    
    /**
     * This method for creating any kind of plan.
     *
     * To do this, we need the specific information including a task
     * type, id, plan name, parameters, and etc...
     * 
     * @param type Task type
     * @param beliefNameID ID
     * @param planName Plan name
     * @param params Parameters to instantiate the plan
     * @return Generated PlanAgent
     */
    public PlanAgent createGeneralPlan(TaskType type, String beliefNameID, String planName, Hashtable<String, Object> params) {       
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        BasicRole basic = new BasicRole(type);
        basic = (BasicRole)basic.instantiate(params);
        basic.constrainedWait = false;
        roles.add(basic);
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID(beliefNameID), planName, null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
    }
    
    /**
     * Creates a strike plan with a given destination
     * 
     * 
     * @param x X value of the destination
     * @param y Y value of the destination
     * @return Generated PlanAgent
     */
    public PlanAgent createStrikePlan(int x, int y) {
        Machinetta.Debugger.debug(1,"createStrikePlan: loc="+x+", "+y);
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        // Currently only a single strike on the target.
        BasicRole basic = new BasicRole(TaskType.attackFromAir);
        Hashtable<String, Object> params = new Hashtable<String, Object>();
        params.put("TargetLocation", new Vector3D(x,y,0));
        basic = (BasicRole)basic.instantiate(params);
        basic.constrainedWait = false;
        roles.add(basic);
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("KillSAM"+x+":"+y), "KillSAM", null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
        
        Machinetta.Debugger.debug(1,"createStrikePlan: for loc="+x+", "+y+" created planagent ="+pa);
        return pa;
    }

    /**
     * Creates a UGV attack plan with a given destination
     * 
     * 
     * @param x X value of the destination
     * @param y Y value of the destination
     * @return Generated PlanAgent
     */
    public PlanAgent createUGVAttackPlan(int x, int y) {
        Machinetta.Debugger.debug(1,"createUGVAttackPlan: loc="+x+", "+y);
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        // Currently only a single strike on the target.
        BasicRole basic = new BasicRole(TaskType.UGVAttack);
        Hashtable<String, Object> params = new Hashtable<String, Object>();
        params.put("Location", new Vector3D(x,y,0));
        basic = (BasicRole)basic.instantiate(params);
        basic.constrainedWait = false;
        roles.add(basic);
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("UGVAttack"+x+":"+y), "UGVAttack", null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
    }

    /**
     * Creates a UGV or UAV attack plan with a given destination
     * 
     * 
     * @param x X value of the destination
     * @param y Y value of the destination
     * @return Generated PlanAgent
     */
    public PlanAgent createAttackFromAirOrGroundPlan(int x, int y) {
        Machinetta.Debugger.debug(1,"createAttackFromAirOrGroundPlan: loc="+x+", "+y);
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        // Currently only a single strike on the target.
        BasicRole basic = new BasicRole(TaskType.attackFromAirOrGround);
        Hashtable<String, Object> params = new Hashtable<String, Object>();
        params.put("Location", new Vector3D(x,y,0));
        basic = (BasicRole)basic.instantiate(params);
        basic.constrainedWait = false;
        roles.add(basic);
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("attackFromAirOrGround"+x+":"+y), "attackFromAirOrGround", null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
    }
    
    /**
     * Creates a patrol plan with a given destination
     * 
     * 
     * @param x X value of the destination
     * @param y Y value of the destination
     * @return Generated PlanAgent
     */
    public PlanAgent createPatrolPlan(int x, int y) {
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
    
    public PlanAgent createPatrolPlan(ArrayList<NamedProxyID> subteam, java.awt.Rectangle patrolArea) {
	Area newArea = new Area(patrolArea.getX(), patrolArea.getY(), patrolArea.getX()+patrolArea.getWidth(), patrolArea.getY()+patrolArea.getHeight());
	return createPatrolPlan(subteam, newArea);
    }

    public PlanAgent createPatrolPlan(ArrayList<NamedProxyID> subteam, Area patrolArea) {
        Machinetta.Debugger.debug(1, "createPatrolPlan: area = "+patrolArea.toString());

        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
	BasicRole basic;
	Hashtable<String, Object> params;
	ProxyID proxyId;
	
	for(int loopi = 0; loopi < subteam.size(); loopi++) {
	    proxyId = subteam.get(loopi);
	    basic = new BasicRole(TaskType.patrol);
	    params = new Hashtable<String, Object>();
	    params.put("Area", patrolArea);
	    basic = (BasicRole)basic.instantiate(params);
	    basic.constrainedWait = false;
	    basic.setResponsible(new RAPBelief(proxyId));
	    Debugger.debug(1, "patrolArea role setResponsible to "+proxyId);
	    roles.add(basic);
	}

        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("Patrol"+patrolArea), "Patrol", null, true, new Hashtable(), roles);
        PlanAgent planAgent = new PlanAgent(tpb);
	Debugger.debug(1, "Created plan agent "+planAgent.toString());
	return planAgent;
    }

    public PlanAgent createAttackFromAirPlan(ArrayList<NamedProxyID> subteam, double x, double y) {
        Machinetta.Debugger.debug(1, "createAttackFromAirPlan: target "+x+","+y);

        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
	BasicRole basic;
	Hashtable<String, Object> params;
	ProxyID proxyId;
	
	for(int loopi = 0; loopi < subteam.size(); loopi++) {
	    proxyId = subteam.get(loopi);
	    basic = new BasicRole(TaskType.attackFromAir);
	    params = new Hashtable<String, Object>();
	    params.put("TargetLocation", new Vector3D(x,y,0));
	    basic = (BasicRole)basic.instantiate(params);
	    basic.constrainedWait = false;
	    basic.setResponsible(new RAPBelief(proxyId));
	    Debugger.debug(1, "patrolArea role setResponsible to "+proxyId);
	    roles.add(basic);
	}

        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("KillSAM"+x+":"+y), "KillSAM", null, true, new Hashtable(), roles);
        PlanAgent planAgent = new PlanAgent(tpb);
	Debugger.debug(1, "Created plan agent "+planAgent.toString());
	return planAgent;
    }


    public PlanAgent createISRPlan(ArrayList<NamedProxyID> subteam, Area isrArea) {
        Machinetta.Debugger.debug(1, "createIsrPlan: area = "+isrArea.toString());

        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
	BasicRole basic;
	Hashtable<String, Object> params;
	ProxyID proxyId;
	
	for(int loopi = 0; loopi < subteam.size(); loopi++) {
	    proxyId = subteam.get(loopi);
	    basic = new BasicRole(TaskType.intelSurveilRecon);
	    params = new Hashtable<String, Object>();
	    params.put("Area", isrArea);
	    basic = (BasicRole)basic.instantiate(params);
	    basic.constrainedWait = false;
	    basic.setResponsible(new RAPBelief(proxyId));
	    Debugger.debug(1, "isrArea role setResponsible to "+proxyId);
	    roles.add(basic);
	}

        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("Isr"+isrArea), "Isr", null, true, new Hashtable(), roles);
        PlanAgent planAgent = new PlanAgent(tpb);
	Debugger.debug(1, "Created plan agent "+planAgent.toString());
	return planAgent;
    }

    // for testing we create a set of plans with hardcoded separate
    // isr areas for each UAV role
    public PlanAgent createFakeIsrPlan(ArrayList<NamedProxyID> subteam) {
        Machinetta.Debugger.debug(1, "createIsrPlan2");

        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
	BasicRole basic;
	Hashtable<String, Object> params;
	ProxyID proxyId;
	
	Area isrArea;

	isrArea = new Area(0,0,1000,1000);

	proxyId = subteam.get(0);
	basic = new BasicRole(TaskType.intelSurveilRecon);
	params = new Hashtable<String, Object>();
	params.put("Area", isrArea);
	basic = (BasicRole)basic.instantiate(params);
	basic.constrainedWait = false;
	basic.setResponsible(new RAPBelief(proxyId));
	Debugger.debug(1, "isrArea role setResponsible to "+proxyId);
	roles.add(basic);

	isrArea = new Area(1000,0,2000,1000);
	proxyId = subteam.get(1);
	basic = new BasicRole(TaskType.intelSurveilRecon);
	params = new Hashtable<String, Object>();
	params.put("Area", isrArea);
	basic = (BasicRole)basic.instantiate(params);
	basic.constrainedWait = false;
	basic.setResponsible(new RAPBelief(proxyId));
	Debugger.debug(1, "isrArea role setResponsible to "+proxyId);
	roles.add(basic);

	isrArea = new Area(2000,0,3000,1000);
	proxyId = subteam.get(2);
	basic = new BasicRole(TaskType.intelSurveilRecon);
	params = new Hashtable<String, Object>();
	params.put("Area", isrArea);
	basic = (BasicRole)basic.instantiate(params);
	basic.constrainedWait = false;
	basic.setResponsible(new RAPBelief(proxyId));
	Debugger.debug(1, "isrArea role setResponsible to "+proxyId);
	roles.add(basic);

	isrArea = new Area(3000,0,4000,1000);
	proxyId = subteam.get(3);
	basic = new BasicRole(TaskType.intelSurveilRecon);
	params = new Hashtable<String, Object>();
	params.put("Area", isrArea);
	basic = (BasicRole)basic.instantiate(params);
	basic.constrainedWait = false;
	basic.setResponsible(new RAPBelief(proxyId));
	Debugger.debug(1, "isrArea role setResponsible to "+proxyId);
	roles.add(basic);

	isrArea = new Area(4000,0,5000,1000);
	proxyId = subteam.get(4);
	basic = new BasicRole(TaskType.intelSurveilRecon);
	params = new Hashtable<String, Object>();
	params.put("Area", isrArea);
	basic = (BasicRole)basic.instantiate(params);
	basic.constrainedWait = false;
	basic.setResponsible(new RAPBelief(proxyId));
	Debugger.debug(1, "isrArea role setResponsible to "+proxyId);
	roles.add(basic);

        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("Isr"+isrArea), "Isr", null, true, new Hashtable(), roles);
        PlanAgent planAgent = new PlanAgent(tpb);
	Debugger.debug(1, "Created plan agent "+planAgent.toString());
	return planAgent;
    }

    public PlanAgent createAirdropPlan(ArrayList<NamedProxyID> subteam, double atDestRange, double  baseXPos, double baseYPos, double xPos, double yPos) {
        Machinetta.Debugger.debug(1, "createAirdropPlan: atDestRange "+atDestRange+", dest "+xPos+", "+yPos+" return to base at "+baseXPos+", "+baseYPos);

        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
	BasicRole basic;
	Hashtable<String, Object> params;
	ProxyID proxyId;
	
	for(int loopi = 0; loopi < subteam.size(); loopi++) {
	    proxyId = subteam.get(loopi);
	    basic = new BasicRole(TaskType.airdrop);
	    params = new Hashtable<String, Object>();
	    params.put("Location",new Vector3D(xPos,yPos,0));
	    params.put("Base",new Vector3D(baseXPos,baseYPos,0));
	    params.put("AtDestRange", atDestRange);
	    basic = (BasicRole)basic.instantiate(params);
	    basic.constrainedWait = false;
	    basic.setResponsible(new RAPBelief(proxyId));
	    roles.add(basic);
	}

        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("Airdrop_"+xPos+"_"+yPos), "AirDrop", null, true, new Hashtable(), roles);
        PlanAgent planAgent = new PlanAgent(tpb);
	return planAgent;
    }

    public PlanAgent createTransportPlan(ArrayList<NamedProxyID> subteam, double xPos, double yPos) {
        Machinetta.Debugger.debug(1, "createTransportPlan: dest "+xPos+", "+yPos);

        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
	BasicRole basic;
	Hashtable<String, Object> params;
	ProxyID proxyId;
	
	for(int loopi = 0; loopi < subteam.size(); loopi++) {
	    proxyId = subteam.get(loopi);
	    basic = new BasicRole(TaskType.transport);
	    params = new Hashtable<String, Object>();
	    params.put("Location",new Vector3D(xPos,yPos,0));
	    basic = (BasicRole)basic.instantiate(params);
	    basic.constrainedWait = false;
	    basic.setResponsible(new RAPBelief(proxyId));
	    roles.add(basic);
	}

        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("Transport_"+xPos+"_"+yPos), "Transport", null, true, new Hashtable(), roles);
        PlanAgent planAgent = new PlanAgent(tpb);
	return planAgent;
    }


    public PlanAgent createAssaultPlan(ArrayList<NamedProxyID> subteam, double xPos, double yPos) {
        Machinetta.Debugger.debug(1, "createAssaultPlan: dest "+xPos+", "+yPos);

        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
	BasicRole basic;
	Hashtable<String, Object> params;
	ProxyID proxyId;
	
	for(int loopi = 0; loopi < subteam.size(); loopi++) {
	    proxyId = subteam.get(loopi);
	    basic = new BasicRole(TaskType.attack);
	    params = new Hashtable<String, Object>();
	    params.put("Location",new Vector3D(xPos,yPos,0));
	    basic = (BasicRole)basic.instantiate(params);
	    basic.constrainedWait = false;
	    basic.setResponsible(new RAPBelief(proxyId));
	    roles.add(basic);
	}

        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("Assault_"+xPos+"_"+yPos), "Assault", null, true, new Hashtable(), roles);
        PlanAgent planAgent = new PlanAgent(tpb);
	return planAgent;
    }


    // @TODO: AAAAAAAAAAGGGGGGGGGGGGGGHHHHHHHHHHH!
    //
    // He's using env.  And as far as I can tel he's not populating
    // env.  So did this _ever_ work?  This all has to go, has to be
    // replaced with something that actually works.
    //
    // Ok, we can re-use the DataStructure3D object from Env and hence
    // replace most of this fairly easily I think.

    /**
     * Gets an actual asset object using a given VehicleData to attack
     * This method mainly compares the location information, and if the difference is smaller than
     * the threshold value in the configuration, IDENTICAL_RADIUS, we assumes the selected asset is the target we want.
     * @NOTE: we need to improve this.
     * 
     * @param vd Vehicle data
     * @return Selected target asset
     */
    private Asset getTargetAsset(VehicleData vd) {        
        
        Vector3D location = vd.getLocation();
        Asset targetAsset = null;
        
        for (ListIterator li = env.getAssetsInBox((int)(location.x - SimConfiguration.SENSE_RADIUS), (int)(location.y - SimConfiguration.SENSE_RADIUS), -10, (int)(location.x + SimConfiguration.SENSE_RADIUS), (int)(location.y + SimConfiguration.SENSE_RADIUS), 10000).listIterator(); li.hasNext(); ) {
            Asset a = (Asset)li.next();
                        
            double diff = location.toVector(a.location).length();
      	    if(diff < SimConfiguration.IDENTICAL_RADIUS)
		continue;
	    if(a instanceof Munition)
		continue;
            if(a.getType() == Asset.Types.CIVILIAN)
                continue;
	    if(a.getType() == Asset.Types.C130)
		continue;
	    if(a.getType() == Asset.Types.SMALL_UAV)
		continue;
            if(a.state == State.DESTROYED)
                continue;

            targetAsset = a;
            break;
        }
        
        return targetAsset;
    }
    
    /**
     * Gets an actual asset object using a given VehicleData for general purposes
     * This method mainly compares the location information, and if the difference is smaller than
     * the threshold value in the configuration, IDENTICAL_RADIUS, we assumes the selected asset is the target we want.
     * @NOTE: we need to improve this.
     * 
     * @param vd Vehicle data
     * @return Selected target asset
     */
    private Asset getAssetFromVehicleData(VehicleData vd) {        
        
        Vector3D location = vd.getLocation();
        Asset retAsset = null;
        
        for (ListIterator li = env.getAssetsInBox((int)(location.x - SimConfiguration.SENSE_RADIUS), (int)(location.y - SimConfiguration.SENSE_RADIUS), -10, (int)(location.x + SimConfiguration.SENSE_RADIUS), (int)(location.y + SimConfiguration.SENSE_RADIUS), 10000).listIterator(); li.hasNext(); ) {
            Asset a = (Asset)li.next();
                        
            double diff = location.toVector(a.location).length();
      	    if(diff < SimConfiguration.IDENTICAL_RADIUS) {
                retAsset = a;
            }
        }
        
        return retAsset;
    }
    
    /**
     * Finds a C130 asset object using a given VehicleData
     * This method mainly compares the location information, and if the difference is smaller than
     * the threshold value in the configuration, IDENTICAL_RADIUS, we assumes the selected asset is the target we want.
     * @NOTE: we need to improve this.
     * 
     * @param vd Vehicle data
     * @return Selected target asset
     */
    private Asset getMountingAsset(VehicleData vd) {        
        
        Vector3D location = vd.getLocation();
        Asset targetAsset = null;
        
        for (ListIterator li = env.getAssetsInBox((int)(location.x - SimConfiguration.SENSE_RADIUS), (int)(location.y - SimConfiguration.SENSE_RADIUS), -10, (int)(location.x + SimConfiguration.SENSE_RADIUS), (int)(location.y + SimConfiguration.SENSE_RADIUS), 10000).listIterator(); li.hasNext(); ) {
            Asset a = (Asset)li.next();
                        
            double diff = location.toVector(a.location).length();
      	    if(diff < SimConfiguration.IDENTICAL_RADIUS)
		continue;	    
	    if(a.getType() == Asset.Types.C130) {
                targetAsset = a;
                break;
            }
        }
        
        return targetAsset;
    }
    

    

}

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
 * DynamicFlyZones.java
 *
 * Created on June 17, 2005, 9:51 AM
 *
 */

package AirSim.Machinetta;

import AirSim.Machinetta.Beliefs.UAVLocation;
import AirSim.Machinetta.Beliefs.VehicleBelief;
import AirSim.Machinetta.Beliefs.FlyZone;
import AirSim.Machinetta.CostMaps.BeliefCostMap;
import AirSim.Machinetta.CostMaps.CostMap;
import AirSim.Machinetta.CostMaps.MixGaussiansCostMap;
import AirSim.Machinetta.CostMaps.MixGaussiansCostMap;
import AirSim.Machinetta.CostMaps.OtherVehicleCostMap;
import AirSim.Machinetta.CostMaps.RandomGaussiansCostMap;
import AirSim.Machinetta.CostMaps.SimpleStaticCostMap;
import AirSim.Machinetta.CostMaps.SimpleStaticCostMap3D;
import AirSim.Machinetta.Messages.*;
import AirSim.Machinetta.SimTime;
import AirSim.Environment.*;
import Machinetta.Communication.*;
import Machinetta.ConfigReader;
import Machinetta.Coordination.MAC.BeliefShareRequirement;
import Machinetta.Coordination.MAC.InformationAgentFactory;
import Machinetta.Coordination.MAC.InformationAgent;
import Machinetta.Coordination.MACoordination;
import Machinetta.RAPInterface.OutputMessages.NewRoleMessage;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;
import Machinetta.RAPInterface.OutputMessages.RoleCancelMessage;
import Machinetta.RAPInterface.RAPInterfaceImplementation;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.TeamBelief.Constraints.DirectedInformationRequirement;
import Machinetta.State.ProxyState;

import java.net.*;
import java.awt.Rectangle;
import java.util.*;

/**
 *
 * @author owens
 */
public class DynamicFlyZones {
    private final static double FLYZONE_REWARD = -5.0;
    private final static double NOFLYZONE_COST = 5.0;

    private PathPlanner pathPlanner = null;
    private RAPInterfaceImplementation rapInt = null;
    private ArrayList<CostMap> costMaps;

    private SimpleStaticCostMap3D cm = null;
    private FlyZone flyZone = null;
    private FlyZone request = null;
    private PlannedPath plannedPath = null;

    private ProxyState state=null;

    // @TODO: Remove costmaps as param?
    public DynamicFlyZones(PathPlanner pathPlanner, RAPInterfaceImplementation rapInt, ArrayList<CostMap> costMaps) {
	this.pathPlanner = pathPlanner;
	this.rapInt = rapInt;
	this.costMaps = costMaps;
	cm = new SimpleStaticCostMap3D();
	pathPlanner.addCostMap(cm);
	state = new ProxyState();
        
    }

    private boolean contains(Vector3D point) {
	if((point.x >= flyZone.longtitude1) 
	   && (point.x <= flyZone.longtitude2)
	   && (point.y >= flyZone.latitude1) 
	   && (point.y <= flyZone.latitude2)
	   && (point.z >= flyZone.altitude1) 
	   && (point.z <= flyZone.altitude2))
	    return true;
	else
	    return false;
    }

    private FlyZone buildFlyZoneRequest(Waypoint[] waypoints) {
	FlyZone req = new FlyZone();
	req.pid = state.getSelf().getProxyID();

	for(int loopi = 0; loopi < waypoints.length; loopi++) {
	    if(req.longtitude1 == -Double.MAX_VALUE)
		req.longtitude1 = waypoints[loopi].x;
	    else if(waypoints[loopi].x <= req.longtitude1) 
		req.longtitude1 = waypoints[loopi].x;
	    if(req.longtitude2 == -Double.MAX_VALUE)
		req.longtitude2 = waypoints[loopi].x;
	    else if(waypoints[loopi].x >= req.longtitude2)
		req.longtitude2 = waypoints[loopi].x;

	    if(req.latitude1 == -Double.MAX_VALUE)
		req.latitude1 = waypoints[loopi].y;
	    else if(waypoints[loopi].y <= req.latitude1) 
		req.latitude1 = waypoints[loopi].y;
	    if(req.latitude2 == -Double.MAX_VALUE)
		req.latitude2 = waypoints[loopi].y;
	    else if(waypoints[loopi].y >= req.latitude2)
		req.latitude2 = waypoints[loopi].y;

	    if(req.altitude1 == -Double.MAX_VALUE)
		req.altitude1 = waypoints[loopi].z;
	    else if(waypoints[loopi].z <= req.altitude1) 
		req.altitude1 = waypoints[loopi].z;
	    if(req.altitude2 == -Double.MAX_VALUE)
		req.altitude2 = waypoints[loopi].z;
	    else if(waypoints[loopi].z >= req.altitude2)
		req.altitude2 = waypoints[loopi].z;
	}
	return req;
    }

    public boolean checkFlyZone(PlannedPath pp) {
	boolean insideFlyZone = true;
	Path3D path = pp.path;
	Waypoint[] waypoints = path.getWaypointsAry();

	if(null == flyZone) {
	    insideFlyZone = false;
	}
	else {
	    for(int loopi = 0; loopi < waypoints.length; loopi++) {
		if(!contains(waypoints[loopi])) {
		    insideFlyZone = false;
		    break;
		}
	    }
	}
	// if insideFlyZone is false, stash this planned path
	// and send a FlyZone to TrafficController
	// 
	// @TODO: 
	// Hmm, MAC.InformationAgent's hold a belief and have a
	// constructor that includes a 'toRap' proxyid.  When the
	// agent is created the factory calls 'stateChanged()' on the
	// informationAgent, which then detects if toRap is set, and
	// if so, calls moveAgnet to move itself to toRap.  So thats
	// PROBABLY how we should do this.  
	//
	// So we really need to create a class in
	// AirSim/Machinetta/Beliefs that describes the requested zone
	// and another to describe the response, use an
	// informationAgent to send the message to the traffic
	// controller.
	//
	// So, when the InformationAgent gets to the operator, the
	// belief about the request will be added to operator
	// ProxyState, so operator's stateChanged should catch it and
	// send an informationAgent back with the same belief and
	// approval set true, or false.... 
	// 
	// @TODO: need to watch out for the reply belief getting
	// merged with existing belief when it gets back.  Shouldn't
	// happen as long as we don't add that belief to our OWN
	// proxyState.
	if(!insideFlyZone) {
	    Machinetta.Debugger.debug("Path goes outside current flyzone (or flyzone is null), sending flyzone request to opeator0.",1,this);
	    plannedPath = pp;
	    request = buildFlyZoneRequest(waypoints);
	    Machinetta.Debugger.debug("Requested flyzone="+request.toString(),1,this);

// 	    state.addBelief(request);

	    // @TODO: Bit of a hack.  Maybe should do this via
	    // directedInformationRequirement instead, in UAVRI?
 	    InformationAgent agent = new InformationAgent(request, new NamedProxyID("TrafficController0"));
 	    MACoordination.addAgent(agent);
 	    agent.stateChanged();	    // Let it act
	}

	return insideFlyZone;
    }

    public boolean checkFlyZoneResponse(FlyZone fz) {
	if(!fz.sameSpace(request)) {
	    Machinetta.Debugger.debug("Got a Flyzone response, but it doesn't match our existing request - ignoring it. requestID="+request.getID()+", responseID="+fz.getID(),1,this);
	    return false;
	}

	if(fz.approved) {
	    Machinetta.Debugger.debug("Flyzone request was approved, marking plan approved, fz="+fz,1,this);
	    // if approved, then we add the flyzone to our costmap as
	    // a reward and start the path
	    
	    // remove old flyzone if any from costmaps - including any
	    // that the operator denied.
	    cm.clear();

	    // add new flyzone to costmaps
	    cm.addCostBox(fz.longtitude1, fz.latitude1, fz.altitude1,
			  fz.longtitude2, fz.latitude2, fz.altitude2,
			  FLYZONE_REWARD);		   
	    flyZone = fz;

	    // start path
	    pathPlanner.pathApproved(plannedPath);

	    plannedPath = null;
	    request = null;

	    return true;
	}
	else {
	    Machinetta.Debugger.debug("Flyzone request was denied, forcing a replan, fz="+fz,1,this);

	    // if not approved then we add the flyzone to our costmap
	    // as a cost and force a replan

	    // add new flyzone to costmaps as cost not reward, so we
	    // hopefully plan elsewhere
	    cm.addCostBox(fz.longtitude1, fz.latitude1, fz.altitude1,
			  fz.longtitude2, fz.latitude2, fz.altitude2,
			  NOFLYZONE_COST);

	    plannedPath = null;
	    request = null;
	    pathPlanner.forceReplan("DynamicFlyZones: flyzone request not approved");

	    return false;
	}

    }
    
    
}

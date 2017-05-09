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
 * AA.java
 *
 * Created on June 17, 2005, 9:47 AM
 *
 */

package AirSim.Machinetta;

import AirSim.Environment.Area;
import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Environment.Vector3D;
import AirSim.Machinetta.Beliefs.AssetStateBelief;
import AirSim.Machinetta.Messages.PRMessage;
import AirSim.Machinetta.Messages.PRMessage.MessageType;
import Machinetta.State.BeliefType.CapabilityBelief;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import Util.*;
import java.util.Hashtable;

/**
 * This is AA for a WASM?
 *
 * @author pscerri
 */
public class AA extends Machinetta.AA.SimpleAA {
    // TODO:  Fri Oct 14 23:15:13 EDT 2005 SRO 
    //
    // Per Paul's suggestion, this is a quick hack to avoid proxy's of
    // dead WASMs from taking on new attack roles.  It will be set to
    // true the first time we accept an attack role.
    private boolean doingAttackRole = false;

    /**
     * Creates a new instance of AA
     */
    public AA() {
	Machinetta.Debugger.debug(5, state.getSelf().getProxyID() + " DO NOT USE THIS CLASS.  From what I can tell nothing uses this class and none of our configs do, so I have no idea if it even works. - SRO Thu Feb 26 23:15:42 EST 2009");
    }
    
    /**
     * The capability to do some task is calculated here and returned as required.
     *
     * Obviously this is pretty complex/messy (well will be) because we are calculating
     * capabilities for all role/entity types.
     *
     * Notice that attack capability is always higher than patrol so that WASMs give up patrol
     * to attack.  We need a better solution than this ....
     */
    public void requestCapability(RoleBelief role) {
        
        if (role instanceof BasicRole) {
            BasicRole basic = (BasicRole)role;
            CapabilityBelief cap = state.getSelf().getCapability(role.roleName);
            
            // Typically need to know where we are now
	    AssetStateBelief assetState = (AssetStateBelief)state.getSelf().miscInfo.get("AssetState");
            
            if (assetState == null) {
                Machinetta.Debugger.debug(state.getSelf().getProxyID() + " does not know own location for cap computation, assuming (0,0)", 3, this);
                assetState = new AssetStateBelief();
		assetState.xMeters = 0;
		assetState.yMeters = 0;
		assetState.zMeters = 0;
		assetState.time = System.currentTimeMillis();
            }
            
            switch(basic.getType()) {
                case patrol:
                    try {
                        
                        // This assumes that your capability to patrol is inversely proportional to
                        // your ability to get to the centre of the patrol area
                        
                        NAI nai = (NAI)((Hashtable)role.getParams()).get("NAI");
                        int dist = (int)Util.MathHelpers.dist(assetState.xMeters, assetState.yMeters, nai.getCenter().x, nai.getCenter().y);
                        int capValue = 100 - (dist/UAVRI.MAP_WIDTH_METERS*100);
                        if (role.getParams() != null) cap.setCapability(role.getParams(), new Integer(capValue));
                        Machinetta.Debugger.debug("Set patrol capability to " + capValue + " with params " + role.getParams(), 1, this);
                    } catch (Exception e) {
                        Machinetta.Debugger.debug("Something went wrong getting patrol cap: " + e, 5, this);
                    }
                    break;
                    
	    case attack: {
                    Vector3D loc = (Vector3D)((Hashtable)role.getParams()).get("Location");
                    int dist = (int)Util.MathHelpers.dist(assetState.xMeters, assetState.yMeters, loc.getX(), loc.getY());
		    int capValue;
		    if(doingAttackRole) {
			capValue = 0;
			Machinetta.Debugger.debug("Calculating capability for attack role, ALREADY HAVE attack role so capability is set to " + capValue, 1, this);
		    }
		    else if(dist > UAVRI.AA_MAX_DIST_CAP_METERS) {
			capValue = 0;
		    }
		    else {
			    capValue = 200 - (dist/UAVRI.MAP_WIDTH_METERS*100);
		    }
		    
                    if (role.getParams() != null) cap.setCapability(role.getParams(), new Integer(capValue));
                    Machinetta.Debugger.debug("Set attack capability to " + capValue + " with params " + role.getParams(), 1, this);
                    break;
	    }
	    case attackFromAirOrGround:
	    case UGVAttack: {
                    Vector3D loc = (Vector3D)((Hashtable)role.getParams()).get("Location");
                    int dist = (int)Util.MathHelpers.dist(assetState.xMeters, assetState.yMeters, loc.getX(), loc.getY());
		    int capValue;
		    if(dist > UAVRI.AA_MAX_DIST_CAP_METERS) {
			capValue = 0;
		    }
		    else {
			capValue = 200 - (dist/UAVRI.MAP_WIDTH_METERS*100);
		    }
		    
                    if (role.getParams() != null) cap.setCapability(role.getParams(), new Integer(capValue));
                    Machinetta.Debugger.debug("Set attack capability to " + capValue + " with params " + role.getParams(), 1, this);
                    break;
	    }
                case move:
                    
                    // This one is a bit tricky.  The actual capability to move is constant.
                    Machinetta.Debugger.debug("Set move capability to 100 with params " + role.getParams(), 1, this);
                    if (role.getParams() != null) cap.setCapability(role.getParams(), new Integer(100));
                    break;
                    
                default:
                    Machinetta.Debugger.debug("Don't know how to compute capability for " + role + " assuming 0", 1, this);
                    cap.setCapability(role.getParams(), new Integer(0));
            }
            state.addBelief(cap);
            state.notifyListeners();
        } else {
            Machinetta.Debugger.debug("Don't know how to get capability for " + role, 5, this);
        }
    }
    
    public void removeRole(Machinetta.State.BeliefType.TeamBelief.RoleBelief r) {
        Machinetta.Debugger.debug("RAP being told to stop role: " + r, 1, this);
    }
    
    public void addRole(Machinetta.State.BeliefType.TeamBelief.RoleBelief r) {
        Machinetta.Debugger.debug("RAP being asked to perform role: " + r, 1, this);
        if (r instanceof BasicRole) {
            BasicRole basic = (BasicRole)r;
            switch(basic.getType()) {
                
                case patrol:
                    NAI nai = (NAI)basic.params.get("NAI");
                    if (nai != null) {
                        Area a = new Area(nai.x1, nai.y1, nai.x2, nai.y2);
                        PRMessage msg = new PRMessage(MessageType.NEW_TASK, TaskType.patrol);
                        msg.params.put("Area", a);
                        messageToRAP(msg);
                    } else {
                        Machinetta.Debugger.debug("No NAI given with patrol: " + basic.params, 3, this);
                    }
                    break;
                    
                case move:
                    Vector3D loc = (Vector3D)basic.params.get("Destination");
                    Path2D path = (Path2D)basic.params.get("Path");
                    if (path == null) Machinetta.Debugger.debug("No path provided for move!! ", 3, this);
                    
                    if (loc != null) {
                        PRMessage msg = new PRMessage(MessageType.NEW_TASK, TaskType.move);
                        // @todo Assumes that z will always be 0 for a move.
                        msg.params.put("Location", new Vector3D(loc.getX(), loc.getY(), 0));
                        msg.params.put("Path", path);
                        messageToRAP(msg);
                    } else {
                        Machinetta.Debugger.debug("No location given with move: " + basic.params, 3, this);
                    }
                    break;
                    
                case attack:
		    doingAttackRole = true;
                    loc = (Vector3D)basic.params.get("Location");
                    if (loc != null) {
                        PRMessage msg = new PRMessage(MessageType.NEW_TASK, TaskType.attack);
                        // @todo Assumes that z will always be 0 for an attack
                        msg.params.put("Location", new Vector3D(loc.getX(), loc.getY(), 100));
                        messageToRAP(msg);
                    } else {
                        Machinetta.Debugger.debug("No location given with attack: " + basic.params, 3, this);
                    }
                    break;

                case UGVAttack:
                    loc = (Vector3D)basic.params.get("Location");
                    if (loc != null) {
                        PRMessage msg = new PRMessage(MessageType.NEW_TASK, TaskType.UGVAttack);
                        // @todo Assumes that z will always be 0 for an attack
                        msg.params.put("Location", new Vector3D(loc.getX(), loc.getY(), 100));
                        messageToRAP(msg);
                    } else {
                        Machinetta.Debugger.debug("No location given with attack: " + basic.params, 3, this);
                    }
                    break;
                default:
                    Machinetta.Debugger.debug("Unimplemented/Unknown basic role type: " + basic.getType(), 4, this);
            }
        }
    }
    
}

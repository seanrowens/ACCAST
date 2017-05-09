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
 * GeoLocateAA.java
 *
 * Created on February 7, 2006, 5:00 PM
 *
 */

package AirSim.Machinetta;

import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Machinetta.Beliefs.GeoLocateRequest;
import AirSim.Machinetta.Beliefs.GeoLocateResult;
import AirSim.Machinetta.Beliefs.TDOACoordCommand;
import AirSim.Machinetta.Beliefs.TMAScanResult;
import AirSim.Machinetta.Beliefs.UAVLocation;
import Machinetta.State.BeliefID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.CapabilityBelief;
import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import Machinetta.State.StateChangeListener;
import Util.*;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;

/**
 *
 * @author pscerri
 */
public class GeoLocateAA extends Machinetta.AA.SimpleAA implements StateChangeListener {
    
    Hashtable<GeoLocateRequest,TMAScanResult[]> pendingRequests = new Hashtable<GeoLocateRequest,TMAScanResult[]>();
    
    ArrayList<RoleBelief> roles = new ArrayList<RoleBelief>();
    
    /** Creates a new instance of GeoLocateAA */
    public GeoLocateAA() {
        state.addChangeListener(this);
    }
    
    /**
     * Tell the RAP that it is no longer performing this role.
     * Note: this is not supposed to be "intelligent", AA reasoning
     * for deciding to get rid of role should have been performed already
     *
     * @todo Implement
     */
    protected void removeRole(RoleBelief r) {
        Machinetta.Debugger.debug(3, "Geolocator removed role - ignoring");
    }
    
    /**
     * Tell the RAP that it is to perform this role.
     * Note: this is not supposed to be "intelligent", AA reasoning
     * for deciding to accept role should have been performed already
     *
     * @todo Implement
     */
    protected void addRole(RoleBelief r) {
        Machinetta.Debugger.debug(2, "Geolocator given role " + r);
        
        BasicRole basic = (BasicRole)r;
        if (basic.getType() == TaskType.geolocateCompute) {
            GeoLocateRequest request = (GeoLocateRequest)((Hashtable)basic.getParams()).get("request");
            Machinetta.Debugger.debug(1, "Need to deal with " + request);
            pendingRequests.put(request, new TMAScanResult[3]);
        } else {
            Machinetta.Debugger.debug(1, "Don't know how to handle role of type: " + basic.getType());
        }
        
        roles.add(r);
    }
    
    
    /**
     *
     * @todo This is just an example.
     *
     */
    public void requestCapability(RoleBelief role) {
        
        if (role instanceof BasicRole) {
            
            BasicRole basic = (BasicRole)role;
            CapabilityBelief cap = state.getSelf().getCapability(role.roleName);
            
            switch(basic.getType()) {
                
                default:
                    Machinetta.Debugger.debug(1, "Don't know how to compute capability for " + role + " assuming 0");
                    cap.setCapability(role.getParams(), new Integer(0));
            }
            state.addBelief(cap);
            state.notifyListeners();
        } else {
            Machinetta.Debugger.debug(5, "Don't know how to get capability for " + role);
        }
    }
    
    private Hashtable<ProxyID,TDOACoordCommand> uavToCCMap = new Hashtable<ProxyID,TDOACoordCommand>();
    private Hashtable<RoleBelief,GeoLocateResult> roleToResult = new Hashtable<RoleBelief,GeoLocateResult>();
    private boolean sensedSinceCommand = false;
    
    public void stateChanged(BeliefID[] b) {
        for (int i = 0; i < b.length; i++) {
            Belief bel = state.getBelief(b[i]);
            if (bel instanceof TMAScanResult) {
                
                TMAScanResult tmasr = (TMAScanResult)bel;
                
                // This is not good, relies on sensor id being proxy id (which it should be, but still)
                RoleBelief role = getRoleFor(new NamedProxyID(tmasr.sensorID));
                
                if (role == null) {
                    Machinetta.Debugger.debug(5, "Cannot work out which role " + tmasr.sensorID + " is sending info about");
                } else {
                    Machinetta.Debugger.debug(1, "Sensor data for role: " + role);
                }
                
                for (GeoLocateRequest request: pendingRequests.keySet()) {
                    if (tmasr.requestID != null && tmasr.requestID.equals(request.getID())) {
                        Machinetta.Debugger.debug(1, "Have new data for geolocation: " + tmasr.sensorID);
                        TMAScanResult [] data = pendingRequests.get(request);
                        int index = 0;
                        while (index < data.length && data[index] != null) index++;
                        if (index < data.length) data[index] = tmasr;
                        if (index == 2) {
                            Machinetta.Debugger.debug(1, "Have all required data for geolocation!!!! ");
                            
                            GeoLocateResult glr = computeGLR(data);
                            glr.emitterID = request.emitterID;
                            
                            Machinetta.Debugger.debug(1,"Adding GeoLocateResult belief to state, result="+glr);
                            glr.setLocallySensed(true);
                            state.addBelief(glr);
                            
                            // Also mark the request as complete and put back in the state
                            // @todo Make these distances configurable
                            if (glr.errorElipseLong < 1000 && glr.errorElipseLat < 1000) {
                                Machinetta.Debugger.debug(1,"Marking request as complete and adding back to state, result="+glr);
                                request.located = true;
                                request.setLocallySensed(true);
                                state.addBelief(request);
                            }
                            
                            // Clear the data from pendingRequests, so it can be reused
                            pendingRequests.put(request, new TMAScanResult[3]);
                            
                            // Put the result in hashtable, so new locations can be suggested
                            roleToResult.put(role,glr);
                            
                            // Indicate that next locations should be sent
                            sensedSinceCommand = true;
                            
                            state.notifyListeners();
                        }
                    }
                }
            } else if (bel instanceof UAVLocation) {
                
                
                // Might need to store elipse somewhere accessible to determine where to send
                
                ProxyID uavID = ((UAVLocation)bel).id;
                
                TDOACoordCommand com = uavToCCMap.get(uavID);
                
                // Work out which request this is for
                RoleBelief role = getRoleFor(uavID);
                
                if (role == null) {
                    Machinetta.Debugger.debug(5, "Cannot work out which role " + uavID + " is sending info about");
                } else {
                    Machinetta.Debugger.debug(0, "Information for role: " + role);
                }
                
                if (com != null) {
                    // Check if at locations
                    PositionMeters destLoc = com.getLoc();
                    if (destLoc != null) {
                        
                        double dist = dist((UAVLocation)bel, destLoc);
                        Machinetta.Debugger.debug(1, "Comparing : " + destLoc + " and " + (UAVLocation)bel + " -> " + dist);
                        
                        // @todo Parameterize
                        if (dist < 5000) {
                            Machinetta.Debugger.debug(1, "At dest!");
                            com.setComplete(true);
                            
                            // Now check if others are complete and, if so, send sense requests.
                            boolean allOK = (uavToCCMap.size() >= 3);
                            if (allOK) {
                                Iterator<TDOACoordCommand> it = uavToCCMap.values().iterator();
                                while (it.hasNext()) {
                                    allOK = it.next().isComplete() && allOK;
                                }
                            }
                            
                            if (allOK) {
                                Machinetta.Debugger.debug(1, "Ready to go!!!!!  ");
                               
                                for (ProxyID uav: role.getTeammates()) {
                                    // Need to send sense commands here
                                    com = new TDOACoordCommand();
                                    com.setSenseTime(System.currentTimeMillis() + 500);
                                    com.setLocallySensed(true);
                                    com.setTarget(uav);
                                    uavToCCMap.put(uav, com);
                                    
                                    Machinetta.Debugger.debug(1, "Inserting sense command into state for " + uav);
                                    
                                    state.addBelief(com);
                                    state.notifyListeners();
                                }
                                
                                sensedSinceCommand = false;
                            }
                            
                        } else {
                            com.setComplete(false);
                        }
                    } else {
                        
                        Machinetta.Debugger.debug(1, "Last com was a sense ... ");
                        // Decide whether to send a new location
                        if (sensedSinceCommand) {
                            // should send new locations
                            GeoLocateResult glr = roleToResult.get(role);
                            if (glr == null) {
                                Machinetta.Debugger.debug(5, "Problem getting glr for role");
                            } else {
                                int count = 0;
                                for (ProxyID uav: role.getTeammates()) {
                                    com = new TDOACoordCommand();
                                    
                                    double ang = 120.0 * count;
                                    double spread = Math.min(10000.0, Math.max(glr.errorElipseLat, glr.errorElipseLong));                                 
                                    
                                    double x = glr.longtitude + spread * Math.cos(Math.toRadians(ang));
                                    if (x < 0) x = 0.0;
                                    if (x > 50000) x = 50000.0;
                                    double y = glr.latitude + spread * Math.sin(Math.toRadians(ang));
                                    if (y < 0) y = 0.0;
                                    if (y > 50000) y = 50000.0;
                                    
                                    com.setLoc(new PositionMeters(x, y, 0));
                                    com.setLocallySensed(true);
                                    Machinetta.Debugger.debug(1, "Inserting first TDOA command into state for " + uavID + " : " + x + ", " + y);
                                    com.setTarget(uav);
                                                                        
                                    uavToCCMap.put(uav, com);
                                    
                                    state.addBelief(com);
                                    state.notifyListeners();
                                    
                                    count ++;
                                }
                            }
                        }
                        
                        
                    }
                } else if (role != null) {
                    
                    // Decide on the first place to go, basically spread around the request
                    GeoLocateRequest request = (GeoLocateRequest)((Hashtable)((BasicRole)role).getParams()).get("request");
                    
                    double ang = uavToCCMap.size() * 120.0;
                    
                    com = new TDOACoordCommand();
                    
                    double x = request.longtitude + 10000.0 * Math.cos(Math.toRadians(ang));
                    if (x < 0) x = 0.0;
                    if (x > 50000) x = 50000.0;
                    double y = request.latitude + 10000.0 * Math.sin(Math.toRadians(ang));
                    if (y < 0) y = 0.0;
                    if (y > 50000) y = 50000.0;
                    
                    com.setLoc(new PositionMeters(x, y, 0));
                    com.setLocallySensed(true);
                    Machinetta.Debugger.debug(1, "Inserting first TDOA command into state for " + uavID + " : " + x + ", " + y);
                    com.setTarget(uavID);
                    
                    state.addBelief(com);
                    state.notifyListeners();
                    
                    // Record in map, so we know later
                    uavToCCMap.put(uavID, com);
                }
                
            }
        }
    }
    
    private RoleBelief getRoleFor(ProxyID uavID) {
        RoleBelief role = null;
        for (RoleBelief r: roles) {
            ArrayList<ProxyID> team = r.getTeammates();
            for (ProxyID t: team) {
                if (t.equals(uavID)) {
                    role = r;
                    break;
                }
            }
        }
        return role;
    }
    
    private double dist(UAVLocation loc, PositionMeters l2) {
        return Math.sqrt((loc.latitude-l2.y)*(loc.latitude-l2.y) + (loc.longtitude-l2.x)*(loc.longtitude-l2.x));
    }
    
    private GeoLocateResult computeGLR(TMAScanResult [] data) {
        GeoLocateResult glr = new GeoLocateResult();
        
        int sensorx[] = new int[data.length];
        int sensory[] = new int[data.length];
        int sensorz[] = new int[data.length];
        short type = -1;
        int emitterx = -1;
        int emittery = -1;
        int emitterz = 0;
        
        for (int i = 0; i < data.length; i++) {
            
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data[i].data));
            
            try {
                type = dis.readShort();
                emitterx = dis.readInt();
                emittery = dis.readInt();
                emitterz = dis.readInt();
                sensorx[i] = dis.readInt();
                sensory[i] = dis.readInt();
                sensorz[i] = dis.readInt();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        double optDist = 2000;
        double angles [] = new double[3], diffs [] = new double[3], dists [] = new double[3];
        for (int i = 0; i < sensorx.length; i++) {
            angles[i] = Math.toDegrees(Math.atan2((double)(emitterx - sensorx[i]), (double)(emittery - sensory[i])));
        }
        
        double tDiff = 0.0, tDist = 0.0;
        for (int i = 0; i < sensorx.length; i++) {
            for (int j = 0; j < sensorx.length; j++) {
                if (i != j) {
                    double d = Math.abs(angles[i] - angles[j]);
                    while (d > 360.0) d -= 360.0;
                    if (d > 180.0) d = 360 - d;
                    // System.out.println("d for " + angles[i] + " " + angles[j] + " is " + d);
                    diffs[i] += d;
                }                          }
            dists[i] = Math.sqrt((sensorx[i]-emitterx)*(sensorx[i]-emitterx) + (sensory[i]-emittery)*(sensory[i]-emittery));
            tDist += Math.abs(dists[i] - optDist);
            tDiff += Math.abs(240.0 - diffs[i]);
        }
        
        Machinetta.Debugger.debug(1, "S1: " + sensorx[0] + " " + sensory[0] + " -> " + angles[0] + " " + diffs[0] + " " + dists[0]);
        Machinetta.Debugger.debug(1, "S2: " + sensorx[1] + " " + sensory[1] + " -> " + angles[1] + " " + diffs[1] + " " + dists[1]);
        Machinetta.Debugger.debug(1, "S3: " + sensorx[2] + " " + sensory[2] + " -> " + angles[2] + " " + diffs[2] + " " + dists[2]);
        
        double elipse = tDiff * 10.0 + tDist / 10.0;
        
        Machinetta.Debugger.debug(1, "tDiff: " + tDiff + " tDist: " + tDist + " Elipse: " + elipse);
        
        glr.longtitude = emitterx;
        glr.latitude = emittery;
        glr.errorElipseLat = elipse;
        glr.errorElipseLong = elipse;
        
        glr.orientation = 0.0;
        // glr.orientation = (new java.util.Random()).nextDouble() * 360.0;
        
        return glr;
    }
    
    private GeoLocateResult TEST_computeGLR(TMAScanResult [] data) {
        GeoLocateResult glr = new GeoLocateResult();
        
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data[0].data));
        short type = -1;
        int emitterx = -1;
        int emittery = -1;
        int emitterz = 0;
        int sensorx = -1;
        int sensory = -1;
        int sensorz = 0;
        try {
            type = dis.readShort();
            emitterx = dis.readInt();
            emittery = dis.readInt();
            emitterz = dis.readInt();
            sensorx = dis.readInt();
            sensory = dis.readInt();
            sensorz = dis.readInt();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        glr.longtitude = emitterx;
        glr.latitude = emittery;
//                             glr.errorElipseLat = (new java.util.Random()).nextDouble() * 200.0;
//                             glr.errorElipseLong = (new java.util.Random()).nextDouble() * 200.0;
        glr.errorElipseLat = 2000;
        glr.errorElipseLong = 3000;
        glr.orientation = (new java.util.Random()).nextDouble() * 360.0;
        return glr;
    }
    
    public static void main(String argv[]) {
        System.out.println("Test");
        
        Random rand = new Random();
        int sensorx[] = new int[3];
        int sensory[] = new int[3];
        int sensorz[] = new int[3];
        short type = -1;
        int emitterx = 500;
        int emittery = 500;
        int emitterz = 0;
        double optDist = 200;
        sensorx[0] = 500;
        sensory[0] = 200;
        sensorx[1] = 330;
        sensory[1] = 670;
        sensorx[2] = 670;
        sensory[2] = 670;
        double angles [] = new double[3], diffs [] = new double[3], dists [] = new double[3];
        for (int i = 0; i < sensorx.length; i++) {
            angles[i] = Math.toDegrees(Math.atan2((double)(emitterx - sensorx[i]), (double)(emittery - sensory[i])));
        }
        double tDiff = 0.0, tDist = 0.0;
        for (int i = 0; i < sensorx.length; i++) {
            for (int j = 0; j < sensorx.length; j++) {
                if (i != j) {
                    double d = Math.abs(angles[i] - angles[j]);
                    while (d > 360.0) d -= 360.0;
                    if (d > 180.0) d = 360 - d;
                    // System.out.println("d for " + angles[i] + " " + angles[j] + " is " + d);
                    diffs[i] += d;
                }                          }
            dists[i] = Math.sqrt((sensorx[i]-emitterx)*(sensorx[i]-emitterx) + (sensory[i]-emittery)*(sensory[i]-emittery));
            tDist += Math.abs(dists[i] - optDist);
            tDiff += Math.abs(240.0 - diffs[i]);
        }                            System.out.println("S1: " + sensorx[0] + " " + sensory[0] + " -> " + angles[0] + " " + diffs[0] + " " + dists[0]);
        System.out.println("S2: " + sensorx[1] + " " + sensory[1] + " -> " + angles[1] + " " + diffs[1] + " " + dists[1]);
        System.out.println("S3: " + sensorx[2] + " " + sensory[2] + " -> " + angles[2] + " " + diffs[2] + " " + dists[2]);
        double elipse = tDiff * 10.0 + tDist / 10.0;
        System.out.println("tDiff: " + tDiff + " tDist: " + tDist + " Elipse: " + elipse);
        
    }
    
}

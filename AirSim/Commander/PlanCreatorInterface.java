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
package AirSim.Commander;

 /* PlanCreatorInterface.java
  *
  * Created on Mon Apr 17 16:55:21 EDT 2006
  *
  * @author owens
  */

import AirSim.Environment.Assets.Tasks.TaskType;
import AirSim.Machinetta.BasicRole;
import AirSim.Machinetta.Beliefs.NoFlyZone;
import AirSim.Machinetta.Point2D;
import Machinetta.State.BeliefType.TeamBelief.Constraints.ANDConstraint;
import java.util.*;

import Machinetta.Coordination.MAC.PlanAgent;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.BeliefType.TeamBelief.RoleBelief;
import Machinetta.State.BeliefType.TeamBelief.TeamPlanBelief;
import Machinetta.State.ProxyState;
import java.awt.Point;

/* A set of utility methods for creating plans.
 *
 * @author pscerri/owens
 */

public class PlanCreatorInterface {
    
    // Attack a location from the air.
    //
    // Paul - I can probably get this from CommanderGUI or SimOperatorRI.
    //
    // @param targetPoint	The location of the target to strike
    // @return A @link{PlanAgent} is returned for the created plan.
    public PlanAgent attackFromAir(AirSim.Machinetta.Point2D targetPoint) {
        Machinetta.Debugger.debug("creating attackFromAir plan with target="+targetPoint, 3, this);
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        // Currently only a single strike on the target.
        BasicRole basic = new BasicRole(TaskType.attackFromAir);
        Hashtable<String, Object> params = new Hashtable<String, Object>();
        params.put("TargetLocation", new java.awt.Point(targetPoint.x,targetPoint.y));
        basic = (BasicRole)basic.instantiate(params);
        basic.constrainedWait = false;
        roles.add(basic);
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("KillSAM"+targetPoint.x+":"+targetPoint.y), "KillSAM", null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
    }
    
    // Move to a location specified by a point.
    //
    // Notice that this does not say *who* should move, any old asset with
    // the move capability will move. I will add another version that allows
    // specification of who should do the moving.
    //
    // @param destination	The location to move to.
    // @return A @link{PlanAgent} is returned for the created plan.
    public PlanAgent move(AirSim.Machinetta.Point2D destination){
        Machinetta.Debugger.debug("creating move plan with dest=" + destination, 1, this);
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        BasicRole basic = new BasicRole(TaskType.move);
        Hashtable<String, Object> params = new Hashtable<String, Object>();
        params.put("Destination", new java.awt.Point(destination.x,destination.y));
        basic = (BasicRole)basic.instantiate(params);
        basic.constrainedWait = false;
        roles.add(basic);
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("MoveToDest"), "Move", null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
    }
    
    // Move to along a route.
    //
    // I don't have the roles for this yet
    //
    // @param route	The route to move along.
    // @return A @link{PlanAgent} is returned for the created plan.
    public PlanAgent move(AirSim.Machinetta.Path2D route){
        return null;
    }
    
    // Patrol an area.
    //
    // Paul - I can probably get this from CommanderGUI or SimOperatorRI.
    //
    // @param area	The area to patrol.
    // @return A @link{PlanAgent} is returned for the created plan.
    public PlanAgent patrol(java.awt.Rectangle rect){
        Machinetta.Debugger.debug("creating patrol plan with loc=" + rect, 1, this);
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        // Currently only a single strike on the target.
        BasicRole basic = new BasicRole(TaskType.patrol);
        Hashtable<String, Object> params = new Hashtable<String, Object>();
        params.put("Area", rect);
        basic = (BasicRole)basic.instantiate(params);
        basic.constrainedWait = false;
        roles.add(basic);
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("Patrol"+rect), "Patrol", null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
    }
    
    // Nogo area
    //
    // Doesn't return anything, because NoGo areas do
    // not create plans, they add a belief to relevant UAVs
    //
    // @param area	The nogo area
    // @return void
    public void nogo(AirSim.Environment.Area area){
        Machinetta.Debugger.debug("Creating nogo zone", 1, this);
        Belief nfz = new NoFlyZone(area);
        ProxyState state = new ProxyState();
        state.addBelief(nfz);
        state.notifyListeners();
    }
    
    
    // Simultaenously attack multiple targets from air.
    //
    // Not simultaneous, but only done if all can be done.
    // (Major difficulty doing simultaneous is being able to fly paths that take a fixed length
    // of time.  Basic scheduling code is there, but with the route planner, it is tough.)
    //
    // @param targets	A list of targets to attack simultaneously.
    // @return A @link{PlanAgent} is returned for the created plan.
    public PlanAgent simultaneousAttackFromAir(List<AirSim.Machinetta.Point2D> targets){
        Machinetta.Debugger.debug("Creating simultaneous attackfromair plan", 1, this);
        Vector<RoleBelief> roles = new Vector<RoleBelief>(), constrainedRoles = new Vector<RoleBelief>();
        
        for (Point2D targetPoint: targets) {
            BasicRole strikeRole = new BasicRole(TaskType.attackFromAir);
            Hashtable<String, Object> params = new Hashtable<String, Object>();
            params.put("TargetLocation", new java.awt.Point(targetPoint.x,targetPoint.y));
            strikeRole = (BasicRole)strikeRole.instantiate(params);
            strikeRole.constrainedWait = true;
            roles.add(strikeRole);
            constrainedRoles.add(strikeRole);
        }
        ANDConstraint constraint = new ANDConstraint(constrainedRoles);
        Vector constraints = new Vector();
        constraints.add(constraint);
        
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("KillSAMS"+simultaneousStrikeIDCounter++), "KillSAMS", null, true, new Hashtable(), roles, constraints);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
    }
    static private int simultaneousStrikeIDCounter = 0;
    
    // Attack multiple targets from air, and perform BDA on them after
    // the attacks.
    //
    // Not simultaneous, but only done if all can be done.
    // (Major difficulty doing simultaneous is being able to fly paths that take a fixed length
    // of time.  Basic scheduling code is there, but with the route planner, it is tough.)
    //
    // Also notice that no Asset has the BDA capability, so those roles will run around indefinitely.
    //
    // @param targets	A list of targets to attack simultaneously.
    // @return A @link{PlanAgent} is returned for the created plan.
    public PlanAgent attackFromAirWithBDA(List<AirSim.Machinetta.Point2D> targets){
        Machinetta.Debugger.debug("Creating attackFromAirWithBDA plan", 1, this);
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        for (Point2D targetPoint: targets) {
            BasicRole strikeRole = new BasicRole(TaskType.attackFromAir);
            Hashtable<String, Object> params = new Hashtable<String, Object>();
            params.put("TargetLocation", new java.awt.Point(targetPoint.x,targetPoint.y));
            strikeRole = (BasicRole)strikeRole.instantiate(params);
            strikeRole.constrainedWait = true;
            roles.add(strikeRole);
            
            BasicRole BDARole = new BasicRole(TaskType.BDAFromAir);
            Hashtable<String, Object> BDAparams = new Hashtable<String, Object>();
            params.put("TargetLocation", new java.awt.Point(targetPoint.x,targetPoint.y));
            BDARole = (BasicRole)BDARole.instantiate(params);
            BDARole.constrainedWait = true;
            roles.add(BDARole);
        }
        
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("KillSAMSBDA"+multipleStrikeBDAIDCounter++), "KillSAMSBDA", null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
    }
    static private int multipleStrikeBDAIDCounter = 0;
    
    // Attack multiple targets from air, and perform BDA on them after
    // the attacks, simultaneously.
    //
    // Not simultaneous, but only done if all can be done.
    // (Major difficulty doing simultaneous is being able to fly paths that take a fixed length
    // of time.  Basic scheduling code is there, but with the route planner, it is tough.)
    //
    // Also notice that no Asset has the BDA capability, so those roles will run around indefinitely.
    //
    // @param targets	A list of targets to attack simultaneously.
    // @return A @link{PlanAgent} is returned for the created plan.
    public PlanAgent simultaneousAttackFromAirWithBDA(List<AirSim.Machinetta.Point2D> targets){
        Machinetta.Debugger.debug("Creating simultaneousAttackFromAirWithBDA plan", 1, this);
        Vector<RoleBelief> roles = new Vector<RoleBelief>(), constrainedRoles = new Vector<RoleBelief>();
        
        for (Point2D targetPoint: targets) {
            BasicRole strikeRole = new BasicRole(TaskType.attackFromAir);
            Hashtable<String, Object> params = new Hashtable<String, Object>();
            params.put("TargetLocation", new java.awt.Point(targetPoint.x,targetPoint.y));
            strikeRole = (BasicRole)strikeRole.instantiate(params);
            strikeRole.constrainedWait = true;
            roles.add(strikeRole);
            constrainedRoles.add(strikeRole);
            
            BasicRole BDARole = new BasicRole(TaskType.BDAFromAir);
            Hashtable<String, Object> BDAparams = new Hashtable<String, Object>();
            params.put("TargetLocation", new java.awt.Point(targetPoint.x,targetPoint.y));
            BDARole = (BasicRole)BDARole.instantiate(params);
            BDARole.constrainedWait = true;
            roles.add(BDARole);
            // Not adding to the AND constraint ...
            // (a) because probably should strike anyway
            // (b) since no one will take BDA roles, including this will stifle plan
            // constrainedRoles.add(strikeRole);
        }
        ANDConstraint constraint = new ANDConstraint(constrainedRoles);
        Vector constraints = new Vector();
        constraints.add(constraint);
        
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("KillSAMSBDA"+simultaneousStrikeBDAIDCounter++), "KillSAMSBDA", null, true, new Hashtable(), roles, constraints);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
    }
    static private int simultaneousStrikeBDAIDCounter = 0;
    
    // I am going to have to think a bit about how to implement this.
    //
    // Provide moving guard on one or more groundvehicles.
    //
    // @param pids	The proxy IDs of the ground vehicle(s) to guard.
    // @return A @link{PlanAgent} is returned for the created plan.
    public PlanAgent guardGroundVehicles(List<ProxyID> pids){
        Machinetta.Debugger.debug("NOT Creating guardGroundVehicles plan - this method not implemented", 4, this);
        return null;
    }
    
    /**
     * This plan requests that a particular point be photographed.
     */
    public PlanAgent EOSenseLocation(Point p) {
        
        Machinetta.Debugger.debug("creating EOSenseLocation plan with location="+p, 3, this);
        Vector<RoleBelief> roles = new Vector<RoleBelief>();
        
        // Currently only a single strike on the target.
        BasicRole basic = new BasicRole(TaskType.EOImage);
        Hashtable<String, Object> params = new Hashtable<String, Object>();
        params.put("ImageLocation", p);
        basic = (BasicRole)basic.instantiate(params);
        basic.constrainedWait = false;
        roles.add(basic);
        TeamPlanBelief tpb = new TeamPlanBelief(new BeliefNameID("EOSense"+p.x+":"+p.y), "EOSense", null, true, new Hashtable(), roles);
        PlanAgent pa = new PlanAgent(tpb);
        
        return pa;
    }

}

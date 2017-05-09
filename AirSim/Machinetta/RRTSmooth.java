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
 * RRTSmooth.java
 *
 * Created on October 19, 2006, 6:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Machinetta;
import AirSim.Environment.*;
import AirSim.Machinetta.*;
import AirSim.Machinetta.CostMaps.*;
import java.util.*;

/**
 * This class provides static path smoothing methods that operate on the outputs
 * of other planning modules.
 * @author pkv
 */
public class RRTSmooth {
    /**
     * Takes a path and optimizes it by removing nodes that minimally affect cost,
     * resulting in less redundant paths.  However, resulting paths are not guaranteed 
     * to preserve any vehicle movement constraints.
     * 
     * A smoothness constant is used to determine the tradeoff between path cost and
     * optimality.  A higher value means that reducing the number of nodes in the path
     * is emphasized over maintaining the cost of the path.  A good nominal value is 1.
     * @param rawPath the source path that this function will smooth
     * @param costMaps the set of cost maps used to evaluate path cost
     * @param smoothness constant determining tradeoff between path cost and optimality
     * @return an optimized path object
     */
    public static Path3D smoothNodeCull (Path3D rawPath, 
            ArrayList<CostMap> costMaps, double smoothness) {
        
        Path3D reducedPath = new Path3D();
        Waypoint[] pointArray = rawPath.getWaypointsAry();
        Waypoint wp;
        Waypoint nextwp;
        
        /* If the array has less than three points, we can't reduce it */
        if (pointArray.length < 3)
            return rawPath;
              
        for (int i = 0; i < pointArray.length;) {
            wp = pointArray[i];
            double costmin = Double.MAX_VALUE;
            int nexti = i+1;
            
            /* add the current point to the path */
            reducedPath.addPoint(wp);
            
            /* look through all remaining points */
            for (int j = pointArray.length-1; j > i; j--) {
                nextwp = pointArray[j];
                
                /* get cost of point */
                double cost = 0;
                for (CostMap cm: costMaps) { 
                    cost += cm.getCost(wp.x, wp.y, wp.z, 0, 
                            nextwp.x, nextwp.y, nextwp.z, 0);
                }
                
                 /* if we are within epsilon of improving the cost, use node */
                if (cost < costmin + smoothness*(j-i)) {   
                    costmin = cost;
                    nexti = j;
                }   
            }
            /* add the min node to the path next iteration */
            i = nexti;
        }

        return reducedPath;
    }
    
    /**
     * Takes a path and optimizes it by attempting to move nodes perpendicular to the
     * path such that the change in angle over consecutive nodes is minimized.  In
     * concept, this should produce smooth curves in the overall path. Resulting paths 
     * should implicitly preserve vehicle movement constraints on turning.
     * 
     * A smoothness constant is used to determine the tradeoff between path cost and
     * optimality.  A higher value means that reducing the change in angle over the 
     * path is emphasized over maintaining the cost of the path.  A good nominal value 
     * is 1.
     * @param rawPath the source path that this function will smooth
     * @param costMaps the set of cost maps used to evaluate path cost
     * @param smoothness constant determining tradeoff between path cost and optimality
     * @return an optimized path object
     */
    public static Path3D smoothCurvShift (Path3D rawPath, 
            ArrayList<CostMap> costMaps, double smoothness) {

        Path3D reducedPath = new Path3D();
        Waypoint[] pointArrayOld, pointArray;
        Waypoint perpvect = new Waypoint(0,0,0);
        Waypoint leftPoint = new Waypoint(0,0,0);
        Waypoint rightPoint = new Waypoint(0,0,0);
        double stepsize = 5;
        double oldcost = 0, leftcost = 0, rightcost = 0;
        
        double leftanglecost, rightanglecost;
        double angleweight = 1000;

        Machinetta.Debugger.debug("Warning: This path smoothing method is not fully implemented!", 5, "smoothCurvShift");
        
        /* Get the original path */
        pointArrayOld = rawPath.getWaypointsAry();
        
        /* If the array has less than three points, we can't reduce it */
        if (pointArrayOld.length < 3)
            return rawPath;
        
        /* Deep copy the data from the original path */
        for (int pt_ctr = 0; pt_ctr < pointArrayOld.length; pt_ctr++) {
            reducedPath.addPoint(new Waypoint(pointArrayOld[pt_ctr].x,
                                            pointArrayOld[pt_ctr].y,
                                            pointArrayOld[pt_ctr].z));
        }
        
        /* Get the copied path */
        pointArray = reducedPath.getWaypointsAry();
        
        /* For each set of two points, take the point in the middle and shift it
         * until its cost is minimized along the gradient available */
        for (int iter_cnt = 0; iter_cnt < 200; iter_cnt++) {    
            for (int i = 0; i < pointArray.length - 2; i++) {
                //calculate perpendicular vector
                perpvect.x = pointArray[i+2].y - pointArray[i].y;
                perpvect.y = pointArray[i].x - pointArray[i+2].x;
                perpvect.z = 0;
                perpvect.normalize2d();
                
                //move a little either way in the direction of the gradient
                leftPoint.x = pointArray[i+1].x + stepsize*perpvect.x;
                leftPoint.y = pointArray[i+1].y + stepsize*perpvect.y;
                leftPoint.z = pointArray[i+1].z + stepsize*perpvect.z;
                
                rightPoint.x = pointArray[i+1].x - stepsize*perpvect.x;
                rightPoint.y = pointArray[i+1].y - stepsize*perpvect.y;
                rightPoint.z = pointArray[i+1].z - stepsize*perpvect.z;
                
                //get gradient
                oldcost = 0;
                for (CostMap cm: costMaps) { 
                    oldcost += cm.getCost(
                            pointArray[i].x, 
                            pointArray[i].y, 
                            pointArray[i].z, 0, 
                            pointArray[i+1].x,
                            pointArray[i+1].y,
                            pointArray[i+1].z, 0);
                    oldcost += cm.getCost(
                            pointArray[i+1].x, 
                            pointArray[i+1].y, 
                            pointArray[i+1].z, 0, 
                            pointArray[i+2].x,
                            pointArray[i+2].y,
                            pointArray[i+2].z, 0);
                }
                
                //get gradient
                leftcost = 0;
                for (CostMap cm: costMaps) { 
                    leftcost += cm.getCost(
                            pointArray[i].x, 
                            pointArray[i].y, 
                            pointArray[i].z, 0, 
                            leftPoint.x,
                            leftPoint.y,
                            leftPoint.z, 0);
                    leftcost += cm.getCost(
                            leftPoint.x,
                            leftPoint.y,
                            leftPoint.z, 0,
                            pointArray[i+2].x, 
                            pointArray[i+2].y, 
                            pointArray[i+2].z, 0);
                }
                
                //add cost of changes in angle
                leftanglecost = 0;
                leftanglecost += getAngleCost(pointArray[i], leftPoint, pointArray[i+2]);
                /*if (i > 0) {
                    leftanglecost += getAngleCost(pointArray[i-1], pointArray[i], leftPoint);
                }
                if (i < pointArray.length-3) {
                    leftanglecost += getAngleCost(leftPoint, pointArray[i+2], pointArray[i+3]);
                }*/
                
                //get gradient
                rightcost = 0;
                for (CostMap cm: costMaps) { 
                    rightcost += cm.getCost(
                            pointArray[i].x, 
                            pointArray[i].y, 
                            pointArray[i].z, 0, 
                            rightPoint.x,
                            rightPoint.y,
                            rightPoint.z, 0);
                    rightcost += cm.getCost(
                            rightPoint.x,
                            rightPoint.y,
                            rightPoint.z, 0,
                            pointArray[i+2].x, 
                            pointArray[i+2].y, 
                            pointArray[i+2].z, 0);
                }
                
                //add cost of changes in angle
                rightanglecost = 0;
                rightanglecost += getAngleCost(pointArray[i], rightPoint, pointArray[i+2]);
                /*if (i > 0) {
                    rightanglecost += getAngleCost(pointArray[i-1], pointArray[i], rightPoint);  
                }
                if (i < pointArray.length-3) {
                    rightanglecost += getAngleCost(rightPoint, pointArray[i+2], pointArray[i+3]);
                }*/
                
                
                if (leftanglecost < rightanglecost)
                    if (leftcost < oldcost) {
                        pointArray[i+1].x = leftPoint.x;
                        pointArray[i+1].y = leftPoint.y;
                        pointArray[i+1].z = leftPoint.z;
                    }
                else
                    if (rightcost < oldcost) {
                        pointArray[i+1].x = rightPoint.x;
                        pointArray[i+1].y = rightPoint.y;
                        pointArray[i+1].z = rightPoint.z;
                    }
                /*Machinetta.Debugger.debug("old cost:" + String.valueOf(oldcost) 
                                    + ", left cost:" + String.valueOf(leftcost) 
                                    + ", right cost:" + String.valueOf(rightcost) 
                                    + ", rac:" + String.valueOf(rightanglecost) 
                                    + ", lac:" + String.valueOf(leftanglecost),5,"Smoother");*/
                Machinetta.Debugger.debug("plane:" + String.valueOf(getAngleCost(pointArray[i], pointArray[i+1], pointArray[i+2])), 10, "Smoother");
                Machinetta.Debugger.debug("left:" + String.valueOf(leftanglecost), 1, "smoothCurvShift");
                Machinetta.Debugger.debug("right:" + String.valueOf(rightanglecost), 1, "smoothCurvShift");
            }
            Machinetta.Debugger.debug("iterend", 1, "smoothCurvShift");
        }
        return reducedPath;
    }
    
    /**
     * Takes three points and returns a cost function of the X-Y planar angle of the
     * three points.
     * @param p1 start point of path
     * @param p2 mid point of path
     * @param p3 end point of path
     * @return the cost of the change in X-Y path angle at the mid-point
     */
    private static double getAngleCost(Waypoint p1, Waypoint p2, Waypoint p3) {
        return (Math.abs(getPlaneAngle(p1, p2, p3)));
    }
        
    /**
     * Takes three points and returns the X-Y planar angle between the vectors formed
     * from the first to the second and the second to the third points.  This works
     * out to be the change in angle of the path in the X-Y plane at the midpoint.
     * @param p1 start point of path
     * @param p2 mid point of path
     * @param p3 end point of path
     * @return the change in angle of the path at the midpoint
     */
    private static double getPlaneAngle(Waypoint p1, Waypoint p2, Waypoint p3) {
        double dprod;
        double cprod;
        
        // A . B = |A|*|B|*cosO
        dprod = (p2.x - p1.x) * (p3.x - p2.x) + (p2.y - p1.y) * (p3.y - p2.y);
        
        // A x B = |A|*|B|*sinO
        cprod = (p2.x - p1.x) * (p3.y - p2.y) - (p3.x - p2.x) * (p2.y - p1.y);
        
        return Math.atan2(cprod, dprod);
    }
    
    /**
     * This is just the tan function remapped to -pi to +pi, and limited at +/- inf., 
     * so that it can be used as a cost function without problems with asymptotes.
     * @param angle the angle to evaluate
     * @return tan of (angle/2)
     */
    private static double tanLimit (double angle) {
        angle /= 2;
        return Math.tan(angle);
    }
    
    /**
     * Takes a path and optimizes it by attempting to move nodes perpendicular to the
     * path such that the straightness over consecutive nodes is maximized.  In
     * concept, this should remove unnecessary curves in the overall path. Resulting 
     * paths usually preserve vehicle movement constraints on turning, given a feasible
     * starting path, but this is not guaranteed.
     * 
     * A smoothness constant is used to determine the tradeoff between path cost and
     * optimality.  A higher value means that increasing the straightness of the path 
     * is emphasized over maintaining the cost of the path.  A good nominal value is 1.
     * @param rawPath the source path that this function will smooth
     * @param costMaps the set of cost maps used to evaluate path cost
     * @param smoothness constant determining tradeoff between path cost and optimality
     * @return an optimized path object
     */
    public static Path3D smoothPerpShift (Path3D rawPath, 
            ArrayList<CostMap> costMaps, double smoothness) {

        Path3D reducedPath = new Path3D();
        Waypoint[] pointArrayOld, pointArray;
        Waypoint perpvect = new Waypoint(0,0,0);
        Waypoint centerPoint = new Waypoint(0,0,0);
        double stepsize = 30;
        double oldcost = 0, newcost = 0;
        
        /* Get the original path */
        pointArrayOld = rawPath.getWaypointsAry();
        
        /* If the array has less than three points, we can't reduce it */
        if (pointArrayOld.length < 3)
            return rawPath;
        
        /* Deep copy the data from the original path */
        for (int pt_ctr = 0; pt_ctr < pointArrayOld.length; pt_ctr++) {
            reducedPath.addPoint(new Waypoint(pointArrayOld[pt_ctr].x,
                                            pointArrayOld[pt_ctr].y,
                                            pointArrayOld[pt_ctr].z));
        }
        
        /* Get the copied path */
        pointArray = reducedPath.getWaypointsAry();
        
        /* For each set of two points, take the point in the middle and shift it
         * until its cost is minimized along the gradient available */
        for (int iter_cnt = 0; iter_cnt < 50; iter_cnt++) {    
            for (int i = 0; i < pointArray.length - 2; i++) {
                /* calculate perpendicular vector to path */
                perpvect.x = pointArray[i+2].y - pointArray[i].y;
                perpvect.y = pointArray[i].x - pointArray[i+2].x;
                perpvect.z = 0;
                perpvect.normalize2d();
                
                /* determine straightening direction */
                double dir = Math.signum((pointArray[i+2].x - pointArray[i].x)
                                *(pointArray[i+1].y - pointArray[i].y) 
                            - (pointArray[i+2].y - pointArray[i].y)
                                *(pointArray[i+1].x - pointArray[i].x));
                
                /* move a little in the straightening direction */
                centerPoint.x = pointArray[i+1].x + dir*stepsize*perpvect.x;
                centerPoint.y = pointArray[i+1].y + dir*stepsize*perpvect.y;
                centerPoint.z = pointArray[i+1].z + dir*stepsize*perpvect.z;
                
                /* get cost of path through original point */
                oldcost = 0;
                for (CostMap cm: costMaps) { 
                    oldcost += cm.getCost(
                            pointArray[i].x, 
                            pointArray[i].y, 
                            pointArray[i].z, 0, 
                            pointArray[i+1].x,
                            pointArray[i+1].y,
                            pointArray[i+1].z, 0);
                    oldcost += cm.getCost(
                            pointArray[i+1].x, 
                            pointArray[i+1].y, 
                            pointArray[i+1].z, 0, 
                            pointArray[i+2].x,
                            pointArray[i+2].y,
                            pointArray[i+2].z, 0);
                }
                
                /* get cost of path through straightened point */
                newcost = 0;
                for (CostMap cm: costMaps) { 
                    newcost += cm.getCost(
                            pointArray[i].x, 
                            pointArray[i].y, 
                            pointArray[i].z, 0, 
                            centerPoint.x,
                            centerPoint.y,
                            centerPoint.z, 0);
                    newcost += cm.getCost(
                            centerPoint.x,
                            centerPoint.y,
                            centerPoint.z, 0,
                            pointArray[i+2].x, 
                            pointArray[i+2].y, 
                            pointArray[i+2].z, 0);
                }
                
                /* if new cost is reduced to within epsilon of original cost, 
                   use new point */
                if (oldcost > newcost - smoothness*stepsize) {
                    pointArray[i+1].x = centerPoint.x;
                    pointArray[i+1].y = centerPoint.y;
                    pointArray[i+1].z = centerPoint.z;
                }
            }
        }
        
        return reducedPath;
    }
    
    /**
     * Takes a path and optimizes it using optimal control theory and gradient descent.
     * Resulting paths are guaranteed to return a feasible path, as defined in this
     * function by control constraints.
     * 
     * A smoothness constant is used to determine the tradeoff between path cost and
     * optimality.  A higher value means that increasing the control optimality of the 
     * path is emphasized over maintaining the cost of the path.  A good nominal value
     * is 1.
     * @param rawPath the source path that this function will smooth
     * @param costMaps the set of cost maps used to evaluate path cost
     * @param smoothness constant determining tradeoff between path cost and optimality
     * @return an optimized path object
     */
    public static Path3D smoothGradDesc (Path3D rawPath, 
            ArrayList<CostMap> costMaps, double smoothness) {
        Path3D reducedPath = new Path3D();
        Waypoint[] pointArrayOld;
        
        /* Get the original path */
        pointArrayOld = rawPath.getWaypointsAry();
        
        /* If the array has less than three points, we can't reduce it */
        if (pointArrayOld.length < 3)
            return rawPath;
        
        /* Deep copy the data from the original path */
        for (int pt_ctr = 0; pt_ctr < pointArrayOld.length; pt_ctr++) {
            reducedPath.addPoint(new Waypoint(pointArrayOld[pt_ctr].x,
                                            pointArrayOld[pt_ctr].y,
                                            pointArrayOld[pt_ctr].z));
        }
        
        Waypoint[] state, control, lambda, grad, ctrlgrad, parhamil;
        Waypoint endpt;
        CostMap[] cms = costMaps.toArray(new CostMap[0]);
        double stepsize;
        
        /* put the current info into a point array */
        state = reducedPath.getWaypointsAry();
        endpt = new Waypoint(state[state.length - 1].x, state[state.length - 1].y, state[state.length - 1].z);
        
        /* initialize intermediate calculation arrays */
        lambda = new Waypoint[state.length];
        grad = new Waypoint[state.length];
        
        control = new Waypoint[state.length-1];
        ctrlgrad = new Waypoint[state.length-1];
        parhamil = new Waypoint[state.length-1];
        
        /* initialize all these arrays */
        for (int i = 0; i < state.length - 1; i++) {
            lambda[i] = new Waypoint(0, 0, 0);
            grad[i] = new Waypoint(0, 0, 0);
            
            control[i] = new Waypoint(0, 0, 0);
            ctrlgrad[i] = new Waypoint(0, 0, 0);
            parhamil[i] = new Waypoint(0, 0, 0);
        }
        lambda[state.length-1] = new Waypoint(0, 0, 0);
        grad[state.length-1] = new Waypoint(0, 0, 0);
        
        stepsize = 0.00000001;
        
        /* generate angle information */
        state[0].z = 0;
        for (int i_cnt = 1; i_cnt < state.length; i_cnt++) {
            state[i_cnt].z = Math.atan2(state[i_cnt].y - state[i_cnt-1].y,
                                        state[i_cnt].x - state[i_cnt-1].x);
        }
        
        /* find the control parameters for the state path */
        control[0].y = state[0].angle();
        for (int i_cnt = 0; i_cnt < state.length-1; i_cnt++) {
            control[i_cnt].x = Math.sqrt(state[i_cnt+1].toVectorLengthSqd(state[i_cnt]));
            control[i_cnt].y = state[i_cnt+1].z - state[i_cnt].z;
        }
        
        /* begin gradient descent iteration */
        for (int iter = 0; iter < 50000; iter++) {
            /* generate path from control parameters */
            state = updatePath(control, state);
            
            /* get new gradients from the costmaps */
            grad = updateGradient(state, cms, grad);
            ctrlgrad = updateControlGradient(control, grad, ctrlgrad);
            
            /* compute costate sequence */
            lambda = updateCostate(control, state, endpt, grad, lambda);
            
            /* compute partial Hamiltonian */
            parhamil = updatePartialHamiltonian(control, state, ctrlgrad, lambda, parhamil);
            
            /* update controls */
            for (int i_cnt = 0; i_cnt < state.length - 1; i_cnt++) {
                control[i_cnt].x -= stepsize*parhamil[i_cnt].x;
                control[i_cnt].y -= stepsize*parhamil[i_cnt].y;
                control[i_cnt].z -= stepsize*parhamil[i_cnt].z;
            }
            
            /* project controls into valid control space */
        }
        
        return reducedPath;
    }
    
    /* create a new path from the control sequence */
    private static Waypoint[] updatePath (Waypoint[] control, Waypoint[] state) {
        for (int i_cnt = 1; i_cnt < state.length; i_cnt++) {
            state[i_cnt].x = state[i_cnt-1].x + control[i_cnt-1].x 
                    * Math.cos(control[i_cnt-1].y + state[i_cnt-1].z);
            state[i_cnt].y = state[i_cnt-1].y + control[i_cnt-1].x 
                    * Math.sin(control[i_cnt-1].y + state[i_cnt-1].z);
        }
        return state;
    }
    
    /* calculate the costate sequence using the control, state, and gradient */
    private static Waypoint[] updateCostate (Waypoint[] control, Waypoint[] state, Waypoint endpt,
                                            Waypoint[] grad, Waypoint[] lambda) {
        double W = 0.1;
        lambda[state.length-1].x = 2*W*(state[state.length-1].x - endpt.x) + grad[state.length-1].x;
        lambda[state.length-1].y = 2*W*(state[state.length-1].y - endpt.y) + grad[state.length-1].y;
        lambda[state.length-1].z = grad[state.length-1].z;
        
        for (int i_cnt = state.length-2; i_cnt > 0; i_cnt--) {
            lambda[i_cnt].x = grad[i_cnt].x + lambda[i_cnt+1].x;
            lambda[i_cnt].y = grad[i_cnt].y + lambda[i_cnt+1].y;
            lambda[i_cnt].z = grad[i_cnt].z 
                    - lambda[i_cnt+1].x * control[i_cnt].x * Math.sin(control[i_cnt].y + state[i_cnt].z)
                    + lambda[i_cnt+1].y * control[i_cnt].x * Math.cos(control[i_cnt].y + state[i_cnt].z) 
                    + lambda[i_cnt+1].z;
        }
        return lambda;
    }
    
    /* use the costate sequence to create the partial derivatives of the Hamiltonian */
    private static Waypoint[] updatePartialHamiltonian(Waypoint[] control, Waypoint[] state, 
                                                    Waypoint[] ctrlgrad, Waypoint[] lambda, Waypoint[] parhamil) {
        for (int i_cnt = 0; i_cnt < control.length; i_cnt++) {
            parhamil[i_cnt].x = ctrlgrad[i_cnt].x
                    + lambda[i_cnt+1].x * control[i_cnt].x * Math.cos(control[i_cnt].y + state[i_cnt].z)
                    + lambda[i_cnt+1].y * control[i_cnt].x * Math.sin(control[i_cnt].y + state[i_cnt].z);
            parhamil[i_cnt].y = ctrlgrad[i_cnt].y
                    - lambda[i_cnt+1].x * control[i_cnt].x * Math.sin(control[i_cnt].y + state[i_cnt].z)
                    + lambda[i_cnt+1].y * control[i_cnt].x * Math.cos(control[i_cnt].y + state[i_cnt].z) 
                    + lambda[i_cnt+1].z;
            parhamil[i_cnt].z = ctrlgrad[i_cnt].z;
        }
        return parhamil;
    }
    
    /* calculate the gradient from the cost map */
    private static Waypoint[] updateGradient(Waypoint[] state, CostMap[] cms, Waypoint[] grad) {
        double stepsize = 10;
        double scale = 600;
        for (int i_cnt = 1; i_cnt < state.length; i_cnt++) {
            grad[i_cnt].x = 0;
            grad[i_cnt].y = 0;
            grad[i_cnt].z = 0;
            
            for (int map_cnt = 0; map_cnt < cms.length; map_cnt++) {
                grad[i_cnt].x += (cms[map_cnt].getCost(state[i_cnt-1].x, state[i_cnt-1].y, 0,
                                                    state[i_cnt].x + stepsize, state[i_cnt].y, 0)
                                    - cms[map_cnt].getCost(state[i_cnt-1].x, state[i_cnt-1].y, 0,
                                                    state[i_cnt].x - stepsize, state[i_cnt].y, 0))
                                    /(2*stepsize*scale);
                
                grad[i_cnt].y += (cms[map_cnt].getCost(state[i_cnt-1].x, state[i_cnt-1].y, 0,
                                                    state[i_cnt].x, state[i_cnt].y + stepsize, 0)
                                    - cms[map_cnt].getCost(state[i_cnt-1].x, state[i_cnt-1].y, 0,
                                                    state[i_cnt].x, state[i_cnt].y - stepsize, 0))
                                    /(2*stepsize*scale);
            }
        }
        return grad;
    }
    
    /* create a control gradient to promote well-formed paths */
    private static Waypoint[] updateControlGradient(Waypoint[] control, Waypoint[] grad, Waypoint[] ctrlgrad) {
        double stepsize = 10;
        double gradCoeff = 500;
        
        for (int i_cnt = 1; i_cnt < control.length; i_cnt++) {
            ctrlgrad[i_cnt].x = 0.01*control[i_cnt].x*control[i_cnt].x*stepsize
                                /(gradCoeff*grad[i_cnt].length()+1);
            ctrlgrad[i_cnt].y = 10*control[i_cnt].y*stepsize
                                /(gradCoeff*grad[i_cnt].length()+1);
            ctrlgrad[i_cnt].z = 0;
        }
        return ctrlgrad;
    }
}

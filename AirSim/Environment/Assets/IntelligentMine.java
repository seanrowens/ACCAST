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
 * IntelligentMine.java
 *
 * Created on May 18, 2006, 1:19 PM
 *
 */

package AirSim.Environment.Assets;

import AirSim.Environment.Assets.Tasks.Task;
import AirSim.Environment.Assets.Tasks.SpreadOut;
import AirSim.Environment.Assets.Tasks.Detonate;
import AirSim.Environment.Assets.Tasks.Move;
import AirSim.Environment.Buildings.Building;
import AirSim.Machinetta.Messages.DetonatePA;
import AirSim.Machinetta.Messages.MovePA;
import AirSim.Machinetta.Messages.ActivatePA;
import AirSim.Machinetta.Messages.DeactivatePA;
import AirSim.Machinetta.Messages.NavigationDataAP;
import AirSim.Environment.Vector3D;
import AirSim.Environment.Waypoint;
import Machinetta.Communication.Message;

import java.util.*;

/**
 *
 * @author pscerri, junyounk, owens
 */
public class IntelligentMine extends UnattendedGroundSensor implements Munition {
//public class IntelligentMine extends GroundVehicle {
    private final static double MAX_DESTROY_DIST = 300;
    private final static double SENSOR_RANGE = 300;

    private static Random rand = new java.util.Random();
    
    private boolean exploded = false;
    private double range = 500.0;
    public boolean isActivate = false;

    /** Creates a new instance of IntelligentMine */
    public IntelligentMine(String name, int x, int y) {
        super(name, x, y, SENSOR_RANGE);
        //super(name, x, y, 0, new Vector3D(0.0, 0.0, 0.0));
        setSpeed(Asset.mphToms(200));
        state = State.LIVE;
	
	armor = 3.99;	// hard to see/find
    }
        
    public double getMaxDestroyDist() {
	return MAX_DESTROY_DIST;
    }

    /**
     * Intelligent mines can only sense before they explode.
     */
    public void sense() {
        //@NOTE: we can easily change this part to sense only this is activated.
        if (!exploded) {
            super.sense();
        
            if(possible != null) {
                for (ListIterator li = possible.listIterator(); li.hasNext(); ) {
                    Asset a = (Asset)li.next();
                    
                    if(a == this)
                        continue;
                    if(a.forceId == this.forceId)
                        continue;
                    if(a.getType() == Asset.Types.CIVILIAN)
                        continue;
                    if(a.state == State.DESTROYED)
                        continue;
                    if(a.getContainingBuilding() != null) {
                        Building b = a.getContainingBuilding();
                        if(!b.getContentsVisible())
                            continue;
                    }
                    
                    if(isActivate) {
                        Detonate detonateTask = new Detonate(MAX_DESTROY_DIST);
                        addTask(detonateTask);
			exploded = true;
                    }
                }
            }
            
            if(this.getDroppedFlag()) {
                double randomVal = 2*Math.PI*rand.nextDouble();            
                double x = this.location.x + range*Math.cos(randomVal);
                double y = this.location.y + range*Math.sin(randomVal);
                Machinetta.Debugger.debug(1, this.getID()+"==> spread out position: " + x + ", " + y);
                SpreadOut spreadTask = new SpreadOut(new Vector3D(x, y, 0.0));
                this.addTask(spreadTask);
                this.setDroppedFlag(false);
            }       
        }
    }
    
    /**
     * Use the basic Task mechanism, but need to know when exploding.
     */
    protected void addTask(Task t) {
        super.addTask(t);        
    }
    
    public void msgFromProxy(AirSim.Machinetta.Messages.PRMessage msg) {
        
    	switch (msg.type) {
            
        	case DETONATE:
                    if(isActivate) {
                        DetonatePA dtMsg = (DetonatePA)msg;
                        Machinetta.Debugger.debug(1, "msgFromProxy: Intelligent Mine " + this.getID() + " received detonate command!!!");
                        
                        double dist = dtMsg.dist;
                        
                        Detonate detonateTask = new Detonate(dist);
                        addTask(detonateTask);
                    }
        		
                    break;
        		
        	case MOVE:
                    MovePA mvMsg = (MovePA)msg;
                    Machinetta.Debugger.debug(1, "msgFromProxy: Intelligent Mine " + this.getID() + " received move command!!!");
                    
                    Move moveTask = new Move(new Vector3D(mvMsg.xPos, mvMsg.yPos, mvMsg.zPos));
                    addTask(moveTask);
                
                    break;
                
                case ACTIVATE:
                    ActivatePA actMsg = (ActivatePA)msg;
                    Machinetta.Debugger.debug(1, "msgFromProxy: Intelligent Mine " + this.getID() + " received activate command!!!");
        		
                    isActivate = true;
                    break;
                
                case DEACTIVATE:
                    DeactivatePA dactMsg = (DeactivatePA)msg;
                    Machinetta.Debugger.debug(1, "msgFromProxy: Intelligent Mine " + this.getID() + " received deactivate command!!!");
        		
                    isActivate = false;
                    break;
                    
                default:
                    super.msgFromProxy(msg);
    	}
    }
}

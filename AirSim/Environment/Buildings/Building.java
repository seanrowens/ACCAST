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
 * Building.java
 *
 * Created on December 3, 2007, 1:31 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Environment.Buildings;

import AirSim.Environment.*;
import AirSim.Environment.Assets.*;
import java.util.*;

/**
 *
 * @author junyounk
 */
abstract public class Building {
    
    static protected Env env = new Env();
    
    private Vector3D location = null;
    public Vector3D getLocation() { return location; }
    public void setLocation(Vector3D location) { this.location = location; }
    
    private int width = 0;
    private int height = 0;
    private int depth = 0;
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }
    public boolean contains(double x, double y) {
	if((x > (location.x - width/2))
	   && (x < (location.x + width/2))
	   && (y > (location.y - height/2))
	   && (y < (location.y + height/2)))
	    return true;
	return false;
    }
    
    private State state = State.LIVE;
    public State getState() { return state; }
    public void setState(State value) { state = value; }
    
    private double armor = 0.0;
    public double getArmor() { return armor; }
    public void setArmor(double armor) { this.armor = armor; }
    
    protected String id = "X";
    public String getID() { return id; }
    
    protected static Random rand = new Random();
    public HashMap containedAssets = null;
    
    //@NOTE: we can simply more complicated mode with this field
    // default option is not visible.
    private boolean contentsVisible = false;
    public boolean getContentsVisible() { return contentsVisible; }
    public void setContentsVisible(boolean contentsVisible) { this.contentsVisible = contentsVisible; }
    
    /** Creates a new instance of Building */
    public Building(String id, int cx, int cy, int width, int height) {
        this(id, cx, cy, width, height, 30.0);
    }
    
    public Building(String id, int cx, int cy, int width, int height, double armor) {
        this.id = id;
        location = new Vector3D(cx, cy, 0.0);
        this.width = width;
        this.height = height;
        this.armor = armor;
        this.state = State.LIVE;
        
        this.containedAssets = new HashMap();
        
        env.addBuilding(this);
    }
    
    private boolean directFiring = false;
    public boolean isDirectFiring() { return directFiring; }
    public void setDirectFiring(boolean value) { directFiring = value; }
    private Vector3D directFireTarget = null;
    public Vector3D getDirectFireTarget() { return directFireTarget; }
    
    /** This asset is fired by a. */
    public void directFiredBy(Asset a) {
	directFiring = true;
	directFireTarget = new Vector3D(a.location);
        if (env.getCumulativeDamageMode() == true) {
            double threshold = 0.5;
        
            if(armor <= 0) {
                killedBy(a);
                //return true;
            } else { // if armor > 0
                if(rand.nextGaussian() > threshold) {
                    armor -= a.getWeapon().getDamage();
                    //Machinetta.Debugger.debug(1, getID()+"'s armor: " + armor + " & weapon's damage: " + a.getWeapon().getDamage());
                }
                //if(armor <= 0) return true;
                //else return false;
           }
        } else {
            if(rand.nextGaussian() > armor) {
                killedBy(a);
                //return true;
            }
            //return false;
        }
    }
    
    /** The asset a has exploded, update as required. */
    public void explosion(Asset a) {
        // killedBy(a);
        if(rand.nextGaussian() > armor) {
            killedBy(a);
        }
    }
    
    private void died() {
	if(State.DESTROYED != state) {
	    if(!(this instanceof Munition)) {
		//env.amDead(this);
            }
	}
        state = State.DESTROYED;
	Machinetta.Debugger.debug(getID()+" has destroyed.", 2, this);
    }
        
    public void killedBy(Asset killer) {
        if(State.DESTROYED != state) {
            died();
            killer.kills++;
            Machinetta.Debugger.debug(getID()+" has been destroyed by "+killer.getID(), 2, this);
            
            Iterator iterator = this.containedAssets.keySet().iterator();
            // kill all!
            if(env.getContainedKillOpt() == 0) {                
                while( iterator. hasNext() ) {
                    Asset a = env.getAssetByID((String)iterator.next());
		    if(a.state == State.DESTROYED)
			continue;
                    a.died();
                    killer.kills++;
                    Machinetta.Debugger.debug(a.getID()+" has been killed by "+killer.getID(), 2, this);
                }
            } else if(env.getContainedKillOpt() == 1) {
                //Do nothing (because mode 1 means do not kill any contained asset.                
                while( iterator. hasNext() ) {
                    Asset a = env.getAssetByID((String)iterator.next());
                    a.setContainingBuilding(null);
                    //Machinetta.Debugger.debug(a.getID()+" has been killed by "+killer.getID(), 2, this);
                }
            } else if(env.getContainedKillOpt() == 2) {
                // Kill contained assets based on some probability distributions.                
                while( iterator. hasNext() ) {
                    Asset a = env.getAssetByID((String)iterator.next());
		    if(a.state == State.DESTROYED)
			continue;
                    if(rand.nextGaussian() > 0.5) {                        
                        a.died();
                        killer.kills++;
                        Machinetta.Debugger.debug(a.getID()+" has been killed by "+killer.getID(), 2, this);
                    } else {
                        a.setContainingBuilding(null);
                    }
                }
            }
            
        } else {
	    //            Machinetta.Debugger.debug(getID()+" is already destroyed, attempted to be destroyed by "+killer.getID(), 2, this);
        }
    }
    
    // add 'a' asset to building
    public boolean addAsset(Asset a) {
        if(this.containedAssets.containsKey(a.getID())) {
            Machinetta.Debugger.debug(1, "Add Error: " + getID() + " is already containing given asset "+ a.getID());
            return false;
        } else {
            Machinetta.Debugger.debug(1, getID()+ " adding asset "+a.getID());
            this.containedAssets.put(a.getID(), a);
            a.setContainingBuilding(this);
            return true;
        }
    }
    
    // remove 'a' asset from building
    public boolean removeAsset(Asset a) {
        if(!this.containedAssets.containsKey(a.getID())) {
            Machinetta.Debugger.debug(1, "Remove Error: " + getID() + " isn't containing given asset " + a.getID());
            return false;
        } else {
            Machinetta.Debugger.debug(1, getID()+ " removing asset "+a.getID());
            this.containedAssets.remove(a.getID());
            a.setContainingBuilding(null);
            return true;
        }
    }
}

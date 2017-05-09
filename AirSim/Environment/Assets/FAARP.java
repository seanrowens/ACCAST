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
 * FAARP.java
 *
 * Created on October 23, 2007, 6:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Environment.Assets;

import AirSim.Environment.*;
import Machinetta.Debugger;
import AirSim.Environment.Assets.Tasks.Refuel;

import java.util.*;
import java.text.DecimalFormat;

/**
 *
 * @author junyounk
 */
public class FAARP extends Asset {
    private final static DecimalFormat fmt = new DecimalFormat("0.00");
    private HashMap refuelAssets = null;
    public HashMap getRefuelAssets() { return refuelAssets; }
    public void setRefuelAssets(HashMap refuelAssets) { this.refuelAssets = refuelAssets; }
    
    //@NOTE: do we additionally need to add some parameter value to activate this field?
    int maxCapability = -1;

    // liter/time_step?
    private double fuelRechargeAmount = 0.0;
    
    /** Creates a new instance of FAARP */
    public FAARP(String id, int x, int y, int z) {
        super(id, x, y, z, new Vector3D(0.0, 0.0, 0.0));
        
        state = State.LIVE;
	fireAtSurfaceCapable = false;
	fireAtAirCapable = false;

        this.fuelRechargeAmount = 500;
        this.maxCapability = 5;
        this.refuelAssets = new HashMap();
        
	// FAARP's default behavior is refueling!
	Refuel task = new Refuel();
	addTask(task);
        
        dontReportSense = true;
    }
    
    public FAARP(String id, int x, int y, int z, double refuelRechargeAmount, int maxCapability) {
        super(id, x, y, z, new Vector3D(0.0, 0.0, 0.0));
        
        state = State.LIVE;
	fireAtSurfaceCapable = false;
	fireAtAirCapable = false;

        this.fuelRechargeAmount = refuelRechargeAmount;
        this.maxCapability = maxCapability;
        this.refuelAssets = new HashMap();
        
	// FAARP's default behavior is refueling!
	Refuel task = new Refuel();
	addTask(task);
        
        dontReportSense = true;
    }
    
    // Add this asset to a asset's refuel queue. (Ask refuel to a asset)
    public boolean addToRefuelQueue(Asset a) {
        if(this.refuelAssets.containsKey(a.getID()) == true) {
            Machinetta.Debugger.debug(1, "Add Refuel Queue Error: " + this.getID() + "'s refuel queue is already containing given asset "+ a.getID());
            return false;
        } else if(this.refuelAssets.size() >= this.maxCapability) {
            Machinetta.Debugger.debug(1, "Add Refuel Queue Error: " + this.getID() + "'s maximum capability was exceeded!");
            return false;
        } else {
            Machinetta.Debugger.debug(1, "Adding asset "+a.getID()+" to refuel queue, now have queue size "+refuelAssets.size());
            this.refuelAssets.put(a.getID(), this);
            a.setRefuelingAsset(this);
            return true;
        }
    }
    
    /**
     * Refuel first asset at FAARP
     */
    public void refuelOneAsset() {
        Iterator iterator = this.refuelAssets.keySet().iterator();
        if(iterator.hasNext()) {
            Asset a = env.getAssetByID((String)iterator.next());

            //@TODO: add code if a.getFuelDone is true for any reason, it just pass that, and try to refuel next asset.
            double curAmount = a.getCurFuelAmount()+this.fuelRechargeAmount;
	    int percent = (int)((curAmount/a.getMaxFuelAmount())*100);
	    Machinetta.Debugger.debug(1, a.getID() + " refueling at "+fmt.format(curAmount)+" of max "+fmt.format(a.getMaxFuelAmount())+ " ("+percent+" %)");
            if(curAmount > a.getMaxFuelAmount()) {
                a.setCurFuelAmount(a.getMaxFuelAmount());
		Machinetta.Debugger.debug(1, a.getID() + " done refueling, current Fuel Amount= " + fmt.format(curAmount));
                a.setRefuelDone(true);
                a.setRefuelingAsset(null);
                refuelAssets.remove(a.getID());
            } else a.setCurFuelAmount(curAmount);
        }
    }
    
    public Asset.Types getType() {
        return Asset.Types.FAARP;
    }
    
    public void sense() {
        
    }
    
    public String toString() { return "FAARP " + id; }
}

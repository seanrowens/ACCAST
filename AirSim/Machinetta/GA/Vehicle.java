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
 * Vehicle.java
 *
 * Created on May 19, 2007, 3:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Machinetta.GA;

/**
 *
 * @author pscerri
 */
public class Vehicle {        
    
    private double speed;
    //unit: rad
    public double MAX_THETA_CHANGE;
    public double MAX_PSI_CHANGE;
    
    public String type = null;
    
    // This coefficient is used to alter the expected speed of the
    // UAV.  Even though the UAV speed might be set to say 141.6 m/s,
    // in practice it runs slower than that, because of limitations on
    // turning, etc, when following a path, the autopilot, etc.  It
    // would be better if we'd just model the uav better, but for now
    // we fudge it with this.
    public double SPEED_MODIFIER = .999;
    
    /** Creates a new instance of Vehicle */
    public Vehicle(String type, double speed, double theta, double psi) {
        this.type = type;
        this.speed = speed;
        this.MAX_THETA_CHANGE = theta;
        this.MAX_PSI_CHANGE = psi;
    }
    
    public Vehicle(String type) {
        this(type, 5.0, 30.0, 3.0);
    }
    
    public Vehicle() {
        this("UGV", 5.0, 30.0, 3.0);
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
}

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
 * UnattendedGroundSensor.java
 *
 * Created on March 8, 2006, 3:50 PM
 *
 */

package AirSim.Environment.Assets;

import AirSim.Machinetta.Messages.RPMessage;
import java.util.LinkedList;

/**
 *
 * @author pscerri
 */
public class UnattendedGroundSensor extends Asset {
    public final static double DEFAULT_RANGE = 3000.0;

    private boolean detectAir = false;
    private boolean directional = false;
    private boolean strengthIndicator = false;
    private boolean typeIndicator = false;
    private double range = DEFAULT_RANGE;
    protected LinkedList possible;
            
    /** Creates a new instance of UnattendedGroundSensor */
    public UnattendedGroundSensor(String name, int x, int y, double range) {
        super(name, x, y, 0, null, 0.0);
        super.reportLocationRate = -1;
        this.range = range;
	hasProxy = true;
    }
    
    public UnattendedGroundSensor(String name, int x, int y) {
        this(name, x, y, DEFAULT_RANGE);
    }
    
    private long senseCounter = 0, notFound = 0;
    public void sense() {
        if (++senseCounter % 300 == 0) {
            possible = null;
            
            if (detectAir) {
                possible =  env.getAssetsInBox((int)(location.x-range), (int)(location.y-range), 0,
                        (int)(location.x+range), (int)(location.y+range), 10000);
            } else {
                possible =  env.getAssetsInBox((int)(location.x-range), (int)(location.y-range), -50,
                        (int)(location.x+range), (int)(location.y+range), 50);
            }
            
            // @todo Handle type, direction, etc. indicators
            boolean found = false;
            for (Object o: possible) {
                Asset a = (Asset)o;
                if (a != this) {
		    //                    System.out.println("Sensing : " + a);
                    found = true;
                }
            }
            
            if (found) {
                // Send up a sensor reading
                RPMessage msg = new RPMessage(RPMessage.MessageTypes.SEARCH_SENSOR_READING);
                msg.params.add((int)location.x);
                msg.params.add((int)location.y);
                msg.params.add(true);
                sendToProxy(msg);
                notFound = 0;
            } else {
                notFound++;
                if (notFound % 10 == 0) {
                    RPMessage msg = new RPMessage(RPMessage.MessageTypes.SEARCH_SENSOR_READING);
                    msg.params.add((int)location.x);
                    msg.params.add((int)location.y);
                    msg.params.add(false);
                    sendToProxy(msg);
                }
            }
        }
    }
    
    public Asset.Types getType() {
        return Asset.Types.UGS;
    }
    
    public boolean isDetectAir() {
        return detectAir;
    }
    
    public void setDetectAir(boolean detectAir) {
        this.detectAir = detectAir;
    }
    
    public boolean isDirectional() {
        return directional;
    }
    
    public void setDirectional(boolean directional) {
        this.directional = directional;
    }
    
    public boolean isStrengthIndicator() {
        return strengthIndicator;
    }
    
    public void setStrengthIndicator(boolean strengthIndicator) {
        this.strengthIndicator = strengthIndicator;
    }
    
    public boolean isTypeIndicator() {
        return typeIndicator;
    }
    
    public void setTypeIndicator(boolean typeIndicator) {
        this.typeIndicator = typeIndicator;
    }
    
    public double getRange() {
        return range;
    }
    
    public void setRange(double range) {
        this.range = range;
    }
    
}

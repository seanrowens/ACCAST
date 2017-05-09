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
 * RSSISearch.java
 *
 * Created on February 27, 2006, 4:30 PM
 *
 */

package AirSim.Environment.Assets.Tasks;

import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.Sensors.RSSISensor;
import AirSim.Environment.Area;

/**
 *
 * @author pscerri
 */
public class RSSISearch extends Task {
    
    private RSSISensor sensor = null;
    private Patrol patrol = null;
    double bestToDate = 0.0, turnRate = 0.4;
    
    /** Creates a new instance of RSSISearch */
    public RSSISearch(RSSISensor sensor) {
        this.sensor = sensor;        
    }
    
    public void step(Asset a, long time) {
        // System.out.println("Performing RSSI Search");                
        
        double recent = sensor.getLastReading();
        
        if (bestToDate == 0.0 && recent == 0.0) {
            if (patrol == null) {
                patrol = new Patrol(new Area(0.0, 0.0, 50000.0, 50000.0), a);
            }
            patrol.step(a, time);
        } else if (bestToDate > recent) {            
            a.heading.turn(turnRate);
            turnRate *= 0.9999;
            turnRate = Math.max(turnRate, 0.01);
            //if (bestToDate > 0.0) System.out.println(recent + " < " + bestToDate + " so turning @ " + turnRate);
        } else {
            // Go straight
            //System.out.println(recent + " >= " + bestToDate + " so going straight");
            turnRate = 10.0;
            bestToDate = recent;
        }
        
    }
    
    public boolean finished(Asset a) {
        return false;
    }
    
}

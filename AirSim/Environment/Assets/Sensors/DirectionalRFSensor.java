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
 * DirectionalRFSensor.java
 *
 * Created on January 12, 2007, 5:56 PM
 *
 */

package AirSim.Environment.Assets.Sensors;

import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.Emitter;
import AirSim.Environment.Env;
import AirSim.SensorReading;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 *
 * @author pscerri
 */
public class DirectionalRFSensor extends Sensor {
    
    public final static int DEFAULT_BEAM_RADIUS = 100000;
    private final static boolean USE_BEAM_RADIUS = false;
    private Area beam = new Area(new Rectangle(new Dimension(beamRadius*2,beamRadius*2)));

    private static int beamRadius = DEFAULT_BEAM_RADIUS;
    
    /** Creates a new instance of DirectionalRFSensor */
    public DirectionalRFSensor() {
        super(100);
    }

    public DirectionalRFSensor(int beamRadius) {
        super(100);
	this.beamRadius = beamRadius;
    }
    
    /**
     * Implements the actual sensor
     */
    public ArrayList<SensorReading> _step(Asset self, Env env) {
        
        java.util.ArrayList<AirSim.SensorReading> ret = null;
        
        Rectangle r = beam.getBounds();
        r.setLocation((int)(self.location.x - r.width/2), (int)(self.location.y - r.height/2));
        
        Machinetta.Debugger.debug("DirectionalRFSensor being called: " + r, 1, this);
        
        // Resolution is in degrees
        double [] values = new double[360];
        
        // Get the assets on the ground
        LinkedList possible = null;
        if(USE_BEAM_RADIUS)
            possible = env.getAssetsInBox(r.x, r.y, -10, r.x + r.width, r.y + r.height, 10);
        else
            possible = new LinkedList(env.getAllAssets());
        
        DirRFSensorReading sr = null;
        if (possible != null && possible.size() > 0) {
            
            //            Machinetta.Debugger.debug("Beam sees " + possible.size() + " assets: " + possible, 1, this);
            for (ListIterator li = possible.listIterator(); li.hasNext(); ) {
                Asset asset = (Asset)li.next();
                
                if (asset instanceof Emitter) {
                    Emitter emitter = (Emitter)asset;
                    if(emitter.currentlyEmitting()) {
                        // Contributions from multiple emitters accumulate
                        Machinetta.Debugger.debug("DRFSensor Beam sees emitter " + emitter, 1, this);
                        
                        double dx = self.location.x - asset.location.x;
                        double dy = self.location.y - asset.location.y;
                        
                        double angle = Math.atan2(dy, dx);
                        
                        int iAngle = (int)Math.toDegrees(angle);
                        
                        Machinetta.Debugger.debug("Angle to " + asset.location.x + ", " + asset.location.y + " is " + self.location.x + ", " + self.location.y + " is " + iAngle, 1, this);
                        
                        // Throw in some noise
                        iAngle = (iAngle + (rand.nextInt(20) - 10)) % 360;
                        
                        // Fill in the values
                        // Notice that multiple signals overlap
                        double value = 1.0;
                        values[iAngle] += 1.0;
                        for (int i = 1; i < 15; i++) {
                            value *= 0.9;
                            values[(iAngle + i)%360] += value;
                            int a = (iAngle - i);
                            if (a < 0) a = 360+a;
                            values[a] += value;
                        }
                        
                    }
                    
                    if (sr == null)
                        sr = new DirRFSensorReading(self.location.x, self.location.y, self.location.z, values);
                    
                }
            }
            
        }
        
        if (sr != null) {
            ret = new ArrayList<SensorReading>();
            ret.add(sr);
        }
        
        if (ret != null && ret.size() > 0) {
            Machinetta.Debugger.debug("SENDING!", 1, this);
        }
        
        return ret;
    }
    
    
}

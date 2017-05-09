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
 * TMASensor.java
 *
 * Created on February 10, 2006, 6:38 PM
 *
 */

package AirSim.Environment.Assets.Sensors;

import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.Emitter;
import AirSim.Environment.Env;
import AirSim.Environment.Vector3D;
import AirSim.SensorReading;
import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * @deprecated
 * @author pscerri
 */
public class TMASensor extends Sensor {
    
    /**
     *
     * Here is a big CHEAT to avoid multiple sensor readings of the same object.
     *
     * Obviously, we need to get rid of this at some point.
     *
     */
    private static HashMap<Asset, Object> sensedAssets = new HashMap<Asset,Object>();
    
    /** Creates a new instance of TMASensor */
    public TMASensor() {
        super(5000, null);
    }
    
    private int steps = 0;
    
    private Area beam = new Area(new Rectangle(new Dimension(3000,3000)));
    
    public ArrayList<SensorReading> _step(Asset self, Env env) {
        java.util.ArrayList<AirSim.SensorReading> ret = null;
        
        Rectangle r = beam.getBounds();
        r.setLocation((int)(self.location.x - r.width/2), (int)(self.location.y - r.height/2));
        
        Machinetta.Debugger.debug("TMA Sensor being called: " + r, 0, this);
        
        // Get the assets on the ground
        LinkedList possible = env.getAssetsInBox(r.x, r.y, -10, r.x + r.width, r.y + r.height, 10);
        
        if (possible != null && possible.size() > 0) {
            
            for (ListIterator li = possible.listIterator(); li.hasNext(); ) {
                Asset asset = (Asset)li.next();
                
                if (asset instanceof Emitter && !sensedAssets.containsKey(asset)) {
                    // @todo Check frequencies, etc.
                    Machinetta.Debugger.debug("TMA Sensor sees " + asset + " @ " + asset.location.x + ", " + asset.location.y, 1, this);
                    TMASensorReading sr = new TMASensorReading();
                    sr.x = (int)self.location.x;
                    sr.y = (int)self.location.y;
                    sr.z = (int)self.location.z;
                    sr.sensor = asset.getPid();
                    
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    DataOutputStream dout = new DataOutputStream(bout);
                    try {
                        // Indication that it is an emitter
                        dout.writeShort(1);
                        // Location
                        dout.writeInt((int)self.location.x);
                        dout.writeInt((int)self.location.y);
                        dout.writeInt((int)self.location.z);
                        
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    
                    
                    sr.details = bout.toByteArray();
                    if (ret == null) ret = new java.util.ArrayList<AirSim.SensorReading>();
                    ret.add(sr);
                    
                    // Cheating and remembering that we sensed this
                    sensedAssets.put(asset, null);
                    
                }
            }
        }
        // System.out.println("End sense cycle");
        return ret;
    }
    
    /**
     * This is exactly the same as _step, except the "reverse" cheat
     * on the sensedAssets - we must have seen it before.
     */
    public TMASensorReading geolocate(Asset self, Env env) {
        
        Rectangle r = beam.getBounds();
        r.setLocation((int)(self.location.x - r.width/2), (int)(self.location.y - r.height/2));
        
        Machinetta.Debugger.debug("TMA Sensor being called: " + r, 0, this);
        
        // Get the assets on the ground
        LinkedList possible = env.getAssetsInBox(r.x, r.y, -10, r.x + r.width, r.y + r.height, 10);
        
        for (ListIterator li = possible.listIterator(); li.hasNext(); ) {
            Asset asset = (Asset)li.next();
            
            if (asset instanceof Emitter && sensedAssets.containsKey(asset)) {
                // @todo Check frequencies, etc.
                Machinetta.Debugger.debug("TMA Sensor sees " + asset + " @ " + asset.location.x + ", " + asset.location.y, 1, this);
                TMASensorReading sr = new TMASensorReading();
                sr.x = (int)self.location.x;
                sr.y = (int)self.location.y;
                sr.z = (int)self.location.z;
                sr.sensor = asset.getPid();
                
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                DataOutputStream dout = new DataOutputStream(bout);
                try {
                    // Indication that it is an emitter
                    dout.writeShort(1);
                    // Location
                    dout.writeInt((int)self.location.x);
                    dout.writeInt((int)self.location.y);
                    dout.writeInt((int)self.location.z);
                    
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                
                
                sr.details = bout.toByteArray();
                
                // Assume this is it
                return sr;                
            }
        }
        return null;
    }
    
}

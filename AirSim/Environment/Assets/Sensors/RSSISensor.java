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
 * RSSISensor.java
 *
 * Created on February 27, 2006, 4:24 PM
 *
 */

package AirSim.Environment.Assets.Sensors;

import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Assets.Emitter;
import AirSim.Environment.Assets.SA9;
import AirSim.Environment.Assets.SmallUAV;
import AirSim.Environment.Env;
import AirSim.Environment.Vector3D;
import AirSim.SensorReading;
import Util.GUI.MutableDoublePanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Area;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author pscerri
 */
public class RSSISensor extends Sensor {
    
    // This is used as a param to the emitter model to 'stretch' the
    // model from its original size to the size of the map.
    public static double MAP_SCALE=1.0;
    private final static boolean DO_LANDSCAPE_BASED_ATTENUATION = false;
    private final static boolean USE_BEAM_RADIUS = false;

    private EmitterModel emitterModel;
    private double lastReading = 0.0;
    private RSSILandscapeSpecification landscape = null;
    private static boolean errorShown = false;
    
    private static int beamRadius = 10000;
    
    /** Creates a new instance of RSSISensor
     *
     * @todo Make the landscape file a parameter.
     * @todo Make the sensing rate a parameter.
     */
    public RSSISensor() {
        super(10,null);

        emitterModel = new EmitterModel(0,0,MAP_SCALE);

        // Load a specification of the RSSI landscape from file
        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream("/tmp/L3/MultipathOnlyRSSIlandscape.rssi"));
            //ObjectInputStream is = new ObjectInputStream(new FileInputStream("/tmp/L3/RandomRSSIlandscape.rssi"));
            //ObjectInputStream is = new ObjectInputStream(new FileInputStream("/tmp/L3/StructuredRSSIlandscape.rssi"));
            landscape = (RSSILandscapeSpecification)is.readObject();
            is.close();
        } catch (Exception ex) {
            if (!errorShown) {
                // JOptionPane.showMessageDialog(null, "Failed to load RSSI Landscape: " + ex);
                Machinetta.Debugger.debug("No RSSI Specification found", 4, this);
                errorShown = true;
            }
        }
        
    }
    
    private Area beam = new Area(new Rectangle(new Dimension(beamRadius*2,beamRadius*2)));

    // Need to rethink a bit.  
    //
    // Keep track of what channels we have sent in the past
    // Every sense step
    // 		calcualte all signal strengths from all emitters/channels
    // 		If any of them differ enough from previous, send them all
    // 		If it has been more than n steps since a send, send them all
    
    private class LastReading {
	int channel;
	double lastStrength = 0.0;
	double strength = 0.0;
	boolean updated = true;

	public LastReading(int channel) {
	    this.channel = channel;
	}
	
	public void prep() {
	    strength = 0.0;
	    updated = false;
	}

	public void addSignal(double signal) {
	    strength += signal;
	    updated = true;
	}

	public void addNoise(EmitterModel emitter) {
	    if(!updated)
		strength += emitter.getNoise();
	}

 	public boolean shouldSend() {
 	    if (Math.abs(lastStrength - strength)/strength > 0.05)
 		return true;
 	    return false;
 	}
    }
    
    long totalSentCount = 0;
    long senseCount = 0;
    long lastSend=0;
    int notSentCount = 0;
    HashMap<Integer,LastReading> channelsSent = new HashMap<Integer,LastReading>();
    LastReading[] channelsSentAry = null;

    private void prepChannelsSent() {
	if(channelsSentAry != null) {
	    for(int loopi = 0; loopi < channelsSentAry.length; loopi++) {
		channelsSentAry[loopi].prep();
	    }
	}
    }
    private void addNoiseToChannels() {
	if(channelsSentAry != null) {
	    for(int loopi = 0; loopi < channelsSentAry.length; loopi++) {
		channelsSentAry[loopi].addNoise(emitterModel);
	    }
	}
    }
    private void updateChannel(int channel, double strength) {
	LastReading reading = channelsSent.get(channel);
	if(null == reading) {
	    reading = new LastReading(channel);
	    channelsSent.put(channel, reading);
	    channelsSentAry = channelsSent.values().toArray(new LastReading[1]);
	}
	reading.addSignal(strength);
    }
    
    // if we should send any of them, might as well send all of them.
    // Also, once we send one reading for a channel, we have to always
    // send readings for that channel.  I.e. if an intermittent
    // transmitter turned off, or if a spurious reading was generated,
    // we need to keep on sending readings that are pure noise, so the
    // filter can process them.
    private boolean shouldSend() {
	if(null == channelsSentAry) 
	    return false;
	if((senseCount - lastSend) >= 10)
	    return true;
	for(int loopi = 0; loopi < channelsSentAry.length; loopi++) {
	    if(channelsSentAry[loopi].shouldSend())
		return true;
	}
	return false;
    }

    public ArrayList<SensorReading> _step(Asset self, Env env) {
	senseCount++;

        java.util.ArrayList<AirSim.SensorReading> ret = null;
	
        Rectangle r = beam.getBounds();
        r.setLocation((int)(self.location.x - r.width/2), (int)(self.location.y - r.height/2));
        
        Machinetta.Debugger.debug("RSSISensor being called: " + r, -1, this);
        
        // Get the assets on the ground
	LinkedList possible = null;
	if(USE_BEAM_RADIUS) 
	    possible = env.getAssetsInBox(r.x, r.y, -10, r.x + r.width, r.y + r.height, 10);
	else
	    possible = new LinkedList(env.getAllAssets());
        
        
        if (possible != null && possible.size() > 0) {
	    prepChannelsSent();
            for (ListIterator li = possible.listIterator(); li.hasNext(); ) {
                Asset asset = (Asset)li.next();
                if (!(asset instanceof Emitter))
		    continue;
		Emitter emitter = (Emitter)asset;
		if(!emitter.currentlyEmitting()) 
		    continue;
		int channels[] = emitter.getChannels();
		for(int loopi = 0; loopi < channels.length; loopi++) {
		    updateChannel(channels[loopi], computeSignal(self, asset));
		}
            }
        }
        
	addNoiseToChannels();

	// @TODO: roll some dice and decide if we should add some
	// spurious noise readings here - i.e. if some bit of noise on
	// some random channel was so loud it looks like a reading.

	if(shouldSend()) {
	    lastSend = senseCount;
	    int[] channels = new int[channelsSentAry.length];
	    double[] strengths = new double[channelsSentAry.length];
	    for(int loopi = 0; loopi < channelsSentAry.length; loopi++) {
		channels[loopi] = channelsSentAry[loopi].channel;
		strengths[loopi] = channelsSentAry[loopi].strength;
		channelsSentAry[loopi].lastStrength = channelsSentAry[loopi].strength;
	    }
	    RSSISensorReading reading = new RSSISensorReading(self.location.x, self.location.y, self.location.z, channels, strengths);
	    if(null == ret) {
		ret = new java.util.ArrayList<AirSim.SensorReading>();
	    }
	    //	    Machinetta.Debugger.debug("Sending from asset "+self.getID()+" RSSISensorReading="+reading, 1, this);
	    ret.add(reading);
	    totalSentCount++;
	}
	else {
	    notSentCount++;
	}
	return ret;
    }
    
    /**
     * RSSI signals are distorted by any available "landscape" (as specified in the
     * RSSILandscapeSpecification)
     *
     */
    private double computeSignal(Asset self, Asset a) {
        double distX = self.location.x - a.location.x;
        double distY = self.location.y - a.location.y;
	//        double distZ = self.location.z - a.location.z;
	//	double distSqd = distX*distX + distY*distY + distZ*distZ;
	double distSqd = distX*distX + distY*distY;
	//	double dist = Math.sqrt(distX*distX + distY*distY + distZ*distZ);

	double strength = emitterModel.getSignal2(distSqd);


	//	Machinetta.Debugger.debug("computeSignal: asset "+self.getID()+" returning "+strength+" for distSqd "+distSqd+" from self at "+self.location.x+","+self.location.y+","+self.location.z+" to target location "+a.location.x+","+a.location.y+","+a.location.z, 2, this);

        if(!DO_LANDSCAPE_BASED_ATTENUATION) return strength;

        Machinetta.Debugger.debug("Landscape based attentuation being performed", 1, this);
        
        if (strength > 0.0 && landscape != null) {
            //System.out.print("Init signal strength: " + strength + " ");
            // This assumes that all direction arrays are same size
            double dx = distX/100.0;
            double dy = distY/100.0;
            
            double mapX = AirSim.Configuration.width/landscape.east.length;
            double mapY = AirSim.Configuration.length/landscape.east[0].length;
            
            double cx = a.location.x;
            double cy = a.location.y;
            int prevX = -1, prevY = -1;
            
            while (Math.abs(cx - self.location.x) > Math.abs(2*dx) || Math.abs(cy - self.location.y) > Math.abs(2*dy)) {
                cx += dx;
                cy += dy;
                // System.out.println("Testing : " + cx + " " + self.location.x + " " + cy + " " + self.location.y);
                // System.out.println("Testing : " + Math.abs(cx - self.location.x) + " " + Math.abs(2*dx) + " " + Math.abs(cy - self.location.y) + " " + Math.abs(2*dy));
                
                double NWAttenuation = 1.0, EWAttenuation = 1.0;
                int x = (int)(cx/mapX), y = (int)(cy/mapY);
                try {
                    if (!(x == prevX && y == prevY)) {
                        if (dx > 0) {
                            // Signal coming from west
                            EWAttenuation = landscape.west[x][y];
                        } else {
                            // Signal coming from east
                            EWAttenuation = landscape.east[x][y];
                        }
                        
                        if (dy > 0) {
                            // Signal coming from south
                            NWAttenuation = landscape.south[x][y];
                        } else {
                            // Signal coming from north
                            NWAttenuation = landscape.north[x][y];
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Array index problem " + x + " " + y + " " +
                            AirSim.Configuration.width + " " + landscape.east.length + " " + AirSim.Configuration.length + " " + landscape.east[0].length +
                            " " + cx + " " + cy);
                }
                
                // Weight the attenuation by the relative degree we are going NS/EW
                double attenuation = NWAttenuation * Math.abs(dy)/(Math.abs(dx)+Math.abs(dy)) + EWAttenuation * Math.abs(dx)/(Math.abs(dx)+Math.abs(dy));
                
                if (attenuation < 0.0 || attenuation > 1.00001) System.out.println("Computed attenuation for location as " + attenuation);
                else strength *= attenuation;
                
                prevX = x; prevY = y;
            }
            
            //System.out.print("Before multipath " + strength);
            
            // Finally, factor in multipath
            if (prevX > 0 && prevY > 0) strength *= landscape.multipath[prevX][prevY];
            //System.out.println(" finally " + strength);
        }
        // System.out.println("Signal is : " + strength);
        return Math.max(Math.min(strength, 1.0), 0.0);
    }
    
    public double getLastReading() {
        return lastReading;
    }
    
    public static double[][] computeSignalGridVerySlowly(int resolution) {
	Env env = new Env();
	ArrayList<Asset> ems = new ArrayList<Asset>();
	for (ListIterator li = env.getAllAssets().listIterator(); li.hasNext(); ) {
	    Asset a = (Asset)li.next();
	    if(a instanceof Emitter) {
		ems.add(a);
	    }
	}

        RSSISensor sensor = new RSSISensor();
        SmallUAV uav = new SmallUAV("signalGrid", 0, 0, 0);
	env.removeAsset(uav);

        double [][] signals = new double[resolution][resolution];
        double max = 0.0;
        
        for (int loopy = 0; loopy < resolution; loopy++) {
            for (int loopx = 0; loopx < resolution; loopx++) {
                uav.location = new Vector3D(AirSim.Configuration.width/resolution*loopx, AirSim.Configuration.height/resolution*loopy, 500);
                // System.out.println("UAV: " + uav.location);
                for (int loopi = 0; loopi < ems.size(); loopi++) {
                    signals[loopx][loopy] += sensor.computeSignal(uav, ems.get(loopi));
                }
                max = Math.max(max, signals[loopx][loopy]);
            }
        }
	return signals;
    }

    public static void main(String argv []) {
        
        AirSim.Configuration.height = 50000;
        System.out.println(AirSim.Configuration.width + " " + AirSim.Configuration.height);
        
        RSSISensor sensor = new RSSISensor();
        SmallUAV uav = new SmallUAV("test", 0, 0, 0);
        
        SA9 [] ems = {
            /*new SA9("em", 5000, 5000),
            new SA9("em", 10000, 10000),
            new SA9("em", 15000, 15000),
            new SA9("em", 20000, 20000),*/
            new SA9("em", 25000, 25000) //,
            /* new SA9("em", 30000, 30000),
            new SA9("em", 35000, 35000),
            new SA9("em", 40000, 40000),
            new SA9("em", 45000, 45000), */
        };
        
        int resolution = 500;
        
        double [][] signals = new double[resolution][resolution];
        double max = 0.0;
        
        for (int y = 0; y < resolution; y++) {
            for (int x = 0; x < resolution; x++) {
                uav.location = new Vector3D(AirSim.Configuration.width/resolution*x, AirSim.Configuration.height/resolution*y, 500);
                // System.out.println("UAV: " + uav.location);
                for (int i = 0; i < ems.length; i++) {
                    signals[x][y] += sensor.computeSignal(uav, ems[i]);
                }
                max = Math.max(max, signals[x][y]);
            }
        }
        
        MutableDoublePanel panel = new MutableDoublePanel();
        panel.values = signals;
        panel.setMinValue(0.0);
        panel.setMaxValue(max);
        
        JFrame sf = new JFrame("test");
        sf.getContentPane().setLayout(new BorderLayout());
        sf.getContentPane().add(panel);
        sf.pack();
        
        sf.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
        sf.setVisible(true);
    }
    
}

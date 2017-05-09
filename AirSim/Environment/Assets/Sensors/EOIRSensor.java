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
 * EOIRSensor.java
 *
 * Created on April 11, 2006, 4:20 PM
 *
 */

package AirSim.Environment.Assets.Sensors;

import AirSim.Environment.Assets.Asset;
import AirSim.Environment.Env;
import AirSim.SensorReading;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import javax.swing.ImageIcon;

/**
 * This sensor is going to be a little different, because it should be configured
 * and used only only demand by the proxy.
 *
 * To achieve this it checks whether current latitude, longtitude and altitude
 * are (approximately) matched by the asset.
 *
 * @author pscerri
 */
public class EOIRSensor extends Sensor {
    
    private double latitude = Double.NEGATIVE_INFINITY,
            longtitude = Double.NEGATIVE_INFINITY,
            altitude = Double.NEGATIVE_INFINITY,
            zoom = Double.NEGATIVE_INFINITY;
    /**
     * This probably should be part of the protocol, but currently
     * the proxy is sending the camera command when it wants the picture 
     * taken.
     */
    private double distanceTolerance = 1000.0;
    private int imageWidth = 200, imageHeight = 200;
    private int taken = 0;
    /** The UAV will take upto this number of pictures per command. */
    private final int maxPics = 30;
    
    /** Creates a new instance of EOIRSensor */
    public EOIRSensor() {
        super(5000);
        
        // new ImageIcon("AirSim/Images/Humvee.gif");
        
    }
    
    public ArrayList<SensorReading> _step(Asset a, Env env) {
        
        /*  Just temporary (and lazy) code for creating a cost map for the terrain
        env.writeTerrainCostMap("/usr1/pscerri/TerrainCM", 1000, 50000, 50000);
        System.exit(0);
         */
        
        ArrayList<SensorReading> ret = null;
        
        if (latitude < 0.0 || longtitude < 0.0 || altitude < 0.0) return null;
        
        double xyDist = Util.MathHelpers.dist(a.location.x, a.location.y, longtitude,latitude);
        // @fix Take into account altitude
        
        if (xyDist < distanceTolerance && taken < maxPics) {
            // Take picture
            // @fix Ignoring zoom
            Machinetta.Debugger.debug("Taking a picture ... ", 1, this);
            EOIRSensorReading sr = new EOIRSensorReading(makeImage(a, env), a.getPid());
            ret = new ArrayList<SensorReading>();
            ret.add(sr);
            taken++;
        } else {
            Machinetta.Debugger.debug("Not in position to take EO image, distance is " + xyDist +" min distance required is "+distanceTolerance, 1, this);
        }
        
        return ret;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
        taken = 0;
        if (latitude > 0.0) setRate(25);
        else setRate(5000);
    }
    
    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
        taken = 0;
        if (longtitude > 0.0) setRate(25);
        else setRate(5000);
        
    }
    
    public void setAltitude(double altitude) {
        this.altitude = altitude;
        taken = 0;
        if (altitude > 0.0) setRate(25);
        else setRate(5000);
    }
    
    public void setZoom(double zoom) {
        taken = 0;
        this.zoom = zoom;
    }
    
    
    /**
     * This needs a lot of work ....
     * @todo Elavation taken into account in images.
     * @todo Orientation taken into account in images.
     * @todo Make images look realistic, not just colored squares
     * @todo Flip images up the other way
     */
    private BufferedImage makeImage(Asset a, Env env) {
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D)image.getGraphics();
        
        double footprintSize = 1000, stepSize = 50;
        int fx = (int)(a.location.x - footprintSize/2);
        int fy = (int)(a.location.y - footprintSize/2);
        double dx = imageWidth / footprintSize * stepSize, dy = imageHeight / footprintSize * stepSize;
        double cx = 0, cy = 0;
        // Only draw the soilTypes that would not obscure a vehicle
        for (int i = fx; i < fx + footprintSize; i+=stepSize) {
            for (int j = fy; j < fy + footprintSize; j+=stepSize) {
                int soilType = env.getSoilType(i,j);
                // if (soilType != Gui.SoilTypes.CANOPY_FOREST) {
		g2.setColor(new Color(Gui.SoilTypes.getSoilColors()[soilType]));
                    g2.fillRect((int)Math.floor(cx), (int)Math.floor(cy), (int)Math.ceil(dx), (int)Math.ceil(dy));
                // }
                cy += dy;
            }
            cx += dx;
            cy = 0.0;
        }
        
        LinkedList possible = env.getAssetsInBox(fx, fy, -10, (int)(fx + footprintSize), (int)(fy + footprintSize), (int)(a.location.z - 50));
        g2.setColor(Color.black);
        for (Object o: possible) {
            Asset seenAsset = (Asset)o;
            Machinetta.Debugger.debug("Asset in image, drawing @ " + (int)((seenAsset.location.x - fx)*(dx/stepSize)) + " " + (seenAsset.location.x - fx), 1, this);
            g2.fillOval((int)((seenAsset.location.x - fx)*(dx/stepSize)), (int)((seenAsset.location.y - fy)*(dy/stepSize)), (int)dx, (int)dy);
            //g2.drawImage(UnitTypes.getImageIcon(2).getImage(), (int)((seenAsset.location.x - fx)*(dx/stepSize)), (int)((seenAsset.location.y - fy)*(dy/stepSize)), null );
        }
        
        return image;
    }
}

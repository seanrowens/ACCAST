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
 * MapView.java
 *
 * Created on June 19, 2004, 12:54 PM
 */

package AirSim.Environment.GUI;

import AirSim.Environment.*;
import AirSim.Environment.Assets.State;
import AirSim.Environment.Assets.*;
import AirSim.Environment.Buildings.*;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;

/**
 * TODO: Make the background a constant
 *
 * @author  paul
 */
public class MapView extends JPanel {
    
    public static int UPDATE_RATE = 200;
    public static Color BACKGROUND_COLOR = new Color(0.5f, 0.7f, 0.5f);
    public static Color TREE_COLOR = new Color(0.1f, 0.4f, 0.1f);
    public boolean labelsOn = false;
    
    // Extent of the current view
    public static int ZOOM = 500;
    public static int SX = 500, SY = 500;
    int x1, y1, z1, x2, y2, z2;
    
    // Access to what is going on
    Env env = new Env();
    Map map = new Map();
    Controls controls = null;
    Locator locator = null;
    
    /** Creates a new instance of MapView */
    public MapView() {
        
        setLayout(new BorderLayout());
        
        add(map, BorderLayout.CENTER);
        
        JPanel bottomP = new JPanel(new GridLayout(1,0));
        locator = new Locator(this);
        controls = new Controls(this);
        bottomP.add(locator);
        bottomP.add(controls);
        add(bottomP, BorderLayout.SOUTH);
        updateThread.start();
        
        x1 = SX;
        y1 = SY;
        z1 = 0;
        x2 = SX + ZOOM;
        y2 = SY + ZOOM;
        z2 = 300;
    }
    
    Thread updateThread = new Thread() {
        public void run() {
            while (true) {
                try {
                    sleep(UPDATE_RATE);
                } catch (InterruptedException e) {}
                // System.out.println("Updating map view");
                if (!MainFrame.paused)
                    map.repaint();
            }
        }
    };
    
    public static int calcUpdateRate(int value) {
        return (int)(1000.0 * Math.pow(Math.E, -value/10.0));
    }
    
    public void setBounds() {
        x1 = SX;
        y1 = SY;
        z1 = 0;
        x2 = SX + ZOOM;
        y2 = SY + ZOOM;
        z2 = 300;
        map.resetBuffer();
        if (MainFrame.paused) repaint();
    }
    
    JSlider speedC = new JSlider(1, 100, 30);
    JSlider zoomC = new JSlider(1, 1000, ZOOM);
    JCheckBox runC = new JCheckBox("Run", !MainFrame.paused);
    JSlider xC = new JSlider(0, 1000, SX);
    JSlider yC = new JSlider(0, 1000, SY);
    JCheckBox cameraView = new JCheckBox("3DView", false);
    JCheckBox labelsOnCB = new JCheckBox("Labels", false);
    
    class Locator extends JPanel {
        JComboBox allForces;
        
        public Locator(final MapView view) {
            Env env = new Env();
            allForces = new JComboBox(env.getAllAssets());
            add(allForces);
            
            allForces.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Asset a = (Asset)allForces.getSelectedItem();
                    SX = (int)a.location.x - (ZOOM / 2);
                    SY = (int)a.location.y - (ZOOM / 2);
                    xC.setValue(SX);
                    yC.setValue(SY);
                    view.setBounds();
                }
            });
            
            add(runC);
            runC.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MainFrame.paused = !runC.isSelected();
                }
            });
            
            add(cameraView);
            cameraView.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (cameraView.isSelected()) {
                        //   new CameraView();
                        System.out.println("Camera view not implemented");
                    }
                }
            });
            
            add(labelsOnCB);
            labelsOnCB.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    labelsOn = labelsOnCB.isSelected();
                }
            });
        }
        
    }
    
    class Controls extends JPanel {
        
        public Controls(final MapView view) {
            setLayout(new GridLayout(0, 2, 5, 1));
            JLabel speedL = new JLabel("Sim Speed:");
            speedL.setHorizontalAlignment(JLabel.RIGHT);
            add(speedL);
            add(speedC);
            speedC.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    MainFrame.UPDATE_RATE = calcUpdateRate(speedC.getValue());
                    if (MainFrame.UPDATE_RATE < 100)
                        UPDATE_RATE = 100;
                    else
                        UPDATE_RATE = MainFrame.UPDATE_RATE;
                    // System.out.println("Set update rate to : " + MainFrame.UPDATE_RATE);
                }
            });
            
            JLabel zoomL = new JLabel("Zoom:");
            zoomL.setHorizontalAlignment(JLabel.RIGHT);
            add(zoomL);
            add(zoomC);
            zoomC.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    int oldZOOM = ZOOM;
                    ZOOM = zoomC.getValue();
                    SX += (oldZOOM - ZOOM) / 2;
                    SY += (oldZOOM - ZOOM) / 2;
                    SX = Math.max(SX, 0);
                    SY = Math.max(SY, 0);
                    xC.setValue(SX);
                    yC.setValue(SY);
                    view.setBounds();
                }
            });
            
            JLabel xL = new JLabel("X Coord:"), yL = new JLabel("Y Coord:");
            xL.setHorizontalAlignment(JLabel.RIGHT);
            yL.setHorizontalAlignment(JLabel.RIGHT);
            add(xL); add(xC);
            add(yL); add(yC);
            xC.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    SX = xC.getValue();
                    view.setBounds();
                }
            });
            yC.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    SY = yC.getValue();
                    view.setBounds();
                }
            });
            
            
        }
        
    }
    
    class Map extends JPanel {
        public void paintBuffer(Graphics g){
            
//             // testing
//             GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
//             /*
//              String[] fonts = genv.getAvailableFontFamilyNames();
//             for (int i = 0; i < fonts.length; i++) {
//                 System.out.println("Font: " + fonts[i]);
//             }*/
//             Font f = Font.decode("Military-PLAIN-36");
//             //Font f = Font.decode("Harrington-PLAIN-36");
//             System.out.println("Font: " + f);
//             g.setFont(f);
//             // End testing
            
//             g.drawString("\u00A6", 20, 90);
//             if (true) return;
            
            g.setColor(BACKGROUND_COLOR);
            g.fillRect(0,0, bufferWidth, bufferHeight);
            
            // Draw buildings
            g.setColor(Color.GRAY);
            LinkedList buildings = env.getBuildingsInArea(x1, y1, x2, y2);
            for (ListIterator li = buildings.listIterator(); li.hasNext(); ) {
                Building b = (Building)li.next();
                g.fillRect(drawX(b.getLocation().x-(double)b.getWidth()/2.0), drawY(b.getLocation().y+(double)b.getHeight()/2.0), (int)(scaleW * (b.getWidth())), (int)(scaleH * (b.getHeight())));
                if (labelsOn) g.drawString(b.getID(), drawX(b.getLocation().x-(double)b.getWidth()/2.0), drawY(b.getLocation().y+(double)b.getHeight()/2.0));
            }
            
            // Draw tanks
            for (ListIterator li = env.getAssetsInBox(x1, y1, z1, x2, y2, z2).listIterator(); li.hasNext(); ) {
                Asset a = (Asset)li.next();
                if (a instanceof Tank) {
                    drawTank((Tank)a, g);
                }
            }
            
            // Draw the road map
            Road [] roads = env.getRoadMap();
            g.setColor(Color.BLACK);
            for (int i = 0; i < roads.length; i++) {
                g.drawLine(drawX(roads[i].x1), drawY(roads[i].y1), drawX(roads[i].x2), drawY(roads[i].y2));
                if (labelsOn) g.drawString(roads[i].name, drawX((roads[i].x1 + roads[i].x2)/2.0), drawY((roads[i].y1 + roads[i].y2)/2.0));
            }
            
            // Draw the trees - this is pretty inefficient
            Vector trees = env.getTrees();
            g.setColor(TREE_COLOR);
            for (Enumeration e = trees.elements(); e.hasMoreElements(); ) {
                Trees t = (Trees)e.nextElement();
                Polygon area = t.area;
                int [] xpoints = new int[t.area.npoints], ypoints = new int[t.area.npoints];
                for (int i = 0; i < area.npoints; i++) {
                    xpoints[i] = drawX(area.xpoints[i]);
                    ypoints[i] = drawY(area.ypoints[i]);
                }
                g.fillPolygon(xpoints, ypoints, t.area.npoints);
            }
            
            // Draw WASMS
            for (ListIterator li = env.getAssetsInBox(x1, y1, z1, x2, y2, z2).listIterator(); li.hasNext(); ) {
                Asset a = (Asset)li.next();
                if (a instanceof WASM) {
                    drawWasm((WASM)a, g);
                }
            }
        }
        
        private void drawWasm(WASM w, Graphics g) {
            if (w.state == State.LIVE)
                g.setColor(Color.blue);
            else
                g.setColor(Color.DARK_GRAY);
            
            Vector3D dir = w.heading.makeCopy();
            int x = drawX(w.location.x), y = drawY(w.location.y);
            int [] xs = new int[4];
            xs [0] = x;
            dir.turn(150);
            xs [1] = (int)(x + 10 * Math.cos(Math.toRadians(dir.angle())));
            dir.turn(30);
            xs [2] = (int)(x + 5 * Math.cos(Math.toRadians(dir.angle())));
            dir.turn(30);
            xs [3] = (int)(x + 10 * Math.cos(Math.toRadians(dir.angle())));
            int [] ys = new int[4];
            ys[0] = y;
            dir.turn(-60);
            ys[1] = (int)(y - 10 * Math.sin(Math.toRadians(dir.angle())));
            dir.turn(30);
            ys[2] = (int)(y - 5 * Math.sin(Math.toRadians(dir.angle())));
            dir.turn(30);
            ys[3] = (int)(y - 10 * Math.sin(Math.toRadians(dir.angle())));
            Polygon p = new Polygon(xs, ys, 4);
            g.drawPolygon(p);
            
            if (w.state == State.LIVE) {
                Polygon sense = w.getSensePolygon();
                for (int i = 0; i < sense.npoints; i++) {
                    sense.xpoints[i] = drawX(sense.xpoints[i]);
                    sense.ypoints[i] = drawY(sense.ypoints[i]);
                }
                g.drawPolygon(sense);
            }
            if (labelsOn) g.drawString(w.getID(), x, y);
        }
        
        private void drawTank(Tank t, Graphics g) {
            if (t.state == State.LIVE)
                g.setColor(Color.red);
            else
                g.setColor(Color.DARK_GRAY);
            Vector3D dir = t.heading.makeCopy();
            if (dir.length() == 0.0) dir = new Vector3D(1.0, 0.0, 0.0);
            int x = drawX(t.location.x), y = drawY(t.location.y);
            int [] xs = new int[4];
            xs [0] = x;
            dir.turn(150);
            xs [1] = (int)(x + 10 * Math.cos(Math.toRadians(dir.angle())));
            dir.turn(30);
            xs [2] = (int)(x + 5 * Math.cos(Math.toRadians(dir.angle())));
            dir.turn(30);
            xs [3] = (int)(x + 10 * Math.cos(Math.toRadians(dir.angle())));
            int [] ys = new int[4];
            ys[0] = y;
            dir.turn(-60);
            ys[1] = (int)(y - 10 * Math.sin(Math.toRadians(dir.angle())));
            dir.turn(30);
            ys[2] = (int)(y - 5 * Math.sin(Math.toRadians(dir.angle())));
            dir.turn(30);
            ys[3] = (int)(y - 10 * Math.sin(Math.toRadians(dir.angle())));
            Polygon p = new Polygon(xs, ys, 4);
            g.drawPolygon(p);
            
            if (labelsOn && t.follow == null) g.drawString(t.getID(), x, y);
        }
        
        private int drawX(double x) { return (int)((x-x1) * scaleW); }
        private int drawY(double y) { return bufferHeight - (int)((y-y1) * scaleH); }
        public void setCenter(double x, double y) {
            
        }
        private int bufferWidth, bufferHeight;
        private double scaleW, scaleH;
        private Image bufferImage;
        private Graphics bufferGraphics;
        
        public void paint(Graphics g){
            //    checks the buffersize with the current panelsize
            //    or initialises the image with the first paint
            if(bufferWidth!=getSize().width ||
                    bufferHeight!=getSize().height ||
                    bufferImage==null || bufferGraphics==null)
                resetBuffer();
            
            if(bufferGraphics!=null){
                //this clears the offscreen image, not the onscreen one
                bufferGraphics.clearRect(0,0,bufferWidth,bufferHeight);
                
                //calls the paintbuffer method with
                //the offscreen graphics as a param
                paintBuffer(bufferGraphics);
                
                //we finaly paint the offscreen image onto the onscreen image
                g.drawImage(bufferImage,0,0,this);
            }
        }
        
        public void calcScales() {
            scaleW = (double)bufferWidth/(x2 - x1);
            scaleH = (double)bufferHeight/(y2 - y1);
            // System.out.println("Set scaleW " + scaleW + " scaleH " + scaleH);
        }
        
        private void resetBuffer(){
            // always keep track of the image size
            bufferWidth=getSize().width;
            bufferHeight=getSize().height;
            
            //    clean up the previous image
            if(bufferGraphics!=null){
                bufferGraphics.dispose();
                bufferGraphics=null;
            }
            if(bufferImage!=null){
                bufferImage.flush();
                bufferImage=null;
            }
            
            //    create the new image with the size of the panel
            bufferImage=createImage(bufferWidth,bufferHeight);
            bufferGraphics=bufferImage.getGraphics();
            
            calcScales();
        }
    }
}

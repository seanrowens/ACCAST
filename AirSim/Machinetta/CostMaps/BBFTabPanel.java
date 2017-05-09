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
 * BBFTabPanel.java
 *
 */
package AirSim.Machinetta.CostMaps;

import AirSim.Environment.Vector3D;
import AirSim.Machinetta.Path3D;
import AirSim.Machinetta.UAVRI;
import AirSim.Machinetta.RRTPlanner;
import AirSim.Machinetta.MiniWorldState;
import Machinetta.Debugger;
import Gui.DoubleGrid;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.*;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.JTabbedPane;
import java.text.DecimalFormat;

/**
 * This class holds a collection of sub tabs, each tab displaying a
 * single BinaryBayesFilter.  Blah.  This should really be an
 * Observable model using MVC somehow...
 *
 *
 * @author sean
 */
public class BBFTabPanel extends JTabbedPane {
    
    private final static Debugger d = new Debugger();

    public final static DecimalFormat fmt = new DecimalFormat(".00000");

    private MiniWorldState miniWorldState = null;
    
    double[][] beliefs = null;
    
    boolean showEntropy = false;

    public BBFTabPanel(MiniWorldState miniWorldState) {
	this.miniWorldState = miniWorldState;
    }

    ArrayList<Point> uavs = new ArrayList<Point>();
    public void setUavs(ArrayList<Point> newUavs) {
        uavs = newUavs;
    }
    
    ArrayList<Point> emitters = new ArrayList<Point>();
    public void setEmitters(ArrayList<Point> newEmitters) {
        emitters = newEmitters;
    }
    
    public ArrayList<ArrayList<Clust>> listOfClusterLists;
    public void setListOfClustersLists(ArrayList<ArrayList<Clust>> listOfClusterLists) {
        this.listOfClusterLists = listOfClusterLists;
    }

    HashMap<String,BinaryBayesDisplay> bbfDisplays = new HashMap<String,BinaryBayesDisplay>();
    public void addPanel(String name, BinaryBayesDisplay bbfDisplay) {
	bbfDisplays.put(name,bbfDisplay);
	this.add(name, bbfDisplay);
	updateAllDisplays();
    }

    /** Creates a new instance of BBFTabPanel */
    public BBFTabPanel(boolean showEntropy) {
	this.showEntropy = showEntropy;
    }

    public void updateAllDisplays() {
	for(String key: bbfDisplays.keySet()) {
	    BinaryBayesDisplay bbfDisplay = bbfDisplays.get(key);
	    if(null == bbfDisplay) {
		d.debug(3, "Updating display "+key+" got null filter for key.");
	    }
	    double x = miniWorldState.getCurrx();
	    double y = miniWorldState.getCurry();
	    Path3D path = miniWorldState.getPath();
	    d.debug(0, "Updating display "+key+" x,y to "+x+","+y+" and path to"+path);
	    bbfDisplay.setData(x,y,null,path);
	    bbfDisplay.repaint();
	}
    }
    
    
}

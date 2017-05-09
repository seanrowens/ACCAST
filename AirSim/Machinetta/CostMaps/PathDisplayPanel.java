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
 * PathDisplayPanel.java
 *
 * Created on March 23, 2006, 9:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package AirSim.Machinetta.CostMaps;

import AirSim.Machinetta.SimTime;
import AirSim.Machinetta.UAVRI;
import AirSim.Environment.Vector3D;
import AirSim.Machinetta.Path3D;
import AirSim.Machinetta.RRTPlanner;
import Gui.DoubleGrid;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.*;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.text.DecimalFormat;

/**
 *
 * @author paul
 */
public class PathDisplayPanel extends JPanel {

    private final static Color ZERO_COST_COLOR = Color.black;
    private final static Color PATH_COLOR = Color.yellow;
    private final static Color OTHER_PATH_COLOR = Color.cyan;

    private final static boolean PRINT_COSTS = true;
    private final static DecimalFormat fmt = new DecimalFormat("0.00");

    double mapWidth = 0;
    double mapHeight = 0;

    Path3D path = null;
    ArrayList<CostMap> cms = null;
    OtherVehicleCostMap ovcmap = null;
    double currX = 0;
    double currY = 0;
    
    /** Creates a new instance of PathDisplayPanel */
    public PathDisplayPanel (double mapWidth, double mapHeight, ArrayList<CostMap> cms, Path3D path) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.path = path;
        this.cms = cms;
    }
    
    public void setData(double newCurrX, double newCurrY, ArrayList<CostMap> newCms, Path3D newPath) {
 	final ArrayList<CostMap> newCmsF = newCms;
 	final Path3D newPathF = new Path3D(newPath);
 	final double newCurrXF = newCurrX;
 	final double newCurrYF = newCurrY;
	try {
	EventQueue.invokeAndWait(new Runnable() { 
		public void run() { 
		    if(null != newPathF)
			path = newPathF;
		    if(null != newCmsF)
			cms = newCmsF;
		    currX = newCurrXF;
		    currY = newCurrYF;
		}
	    }
				 );
	}
	catch(InterruptedException e) {
	    System.err.println("Update of costmap panel was interrupted e="+e);
	    e.printStackTrace();
	}
	catch(java.lang.reflect.InvocationTargetException e) {
	    System.err.println("e="+e);
	    e.printStackTrace();
	}
    }

    private int toScreenY(int height, double y) {
        double dy = mapHeight/(double)height;
        return (int)(((mapHeight - y) / dy)+.5);
    }
    
    private int toScreenX(int width, double x) {
        double dx = mapWidth/(double)width;
        return (int)((x/dx)+.5);
    }

    private void buildCostMapRGBs(int width, int height, int rgbValues[]) {
	int redVal = 0;
	int greenVal = 0;
	int blueVal =  0;

	double dx = mapWidth/(double)width;
	double dy = mapHeight/(double)height;
	
	CostMap[] costMaps = cms.toArray(new CostMap[1]);

	if(PRINT_COSTS)System.err.println("COSTS COSTS COSTS COSTS COSTS COSTS COSTS COSTS COSTS COSTS COSTS COSTS COSTS COSTS COSTS COSTS COSTS COSTS COSTS COSTS ");
	if(PRINT_COSTS)System.err.println("----------------------------------------------------------------------------------------------------------------------------------");

	// NOTE: in the main loop we're looping by +3, +3, i.e. we're
	// only getting every 9th value - so when we calc stats on
	// costData we need to loop the same way.  We're only wasting
	// a little space here (well, 9 times as much as we need, but
	// ehn.)
	double[][][] costData = new double[width][height][costMaps.length+1];
	
	long simTime = SimTime.getEstimatedTime();
	
	for (int loopy = 0; loopy < (height-3); loopy += 3) {
	    StringBuffer line = new StringBuffer(loopy+": ");
	    for (int loopx = 0; loopx < (width-3); loopx += 3) {
		redVal = 0;
		greenVal = 0;
		blueVal =  0;

		double costx = loopx * dx;
		double costy = mapHeight - (loopy * dy);

		double cost = 0.0;
		for (int loopc = 0; loopc < costMaps.length; loopc++) {
		    costData[loopx][loopy][loopc+1] = costMaps[loopc].getCost (costx, costy, simTime, costx+1, costy+1, simTime);
		    if(costData[loopx][loopy][loopc+1] == Double.MAX_VALUE)
			costData[loopx][loopy][loopc+1] = 0.0;
		    cost += costData[loopx][loopy][loopc+1];
		}
		costData[loopx][loopy][0] = cost;

		if(PRINT_COSTS)line.append(" "+fmt.format(cost));
		cost = Math.min(255,cost);
		cost = Math.max(-255,cost);
		if (cost > 0) {
		    redVal = (int)cost;
		} else if (cost < 0) {
		    greenVal = -(int)cost;
		} else {
		    redVal = ZERO_COST_COLOR.getRed();
		    greenVal = ZERO_COST_COLOR.getGreen();
		    blueVal = ZERO_COST_COLOR.getBlue();
		}
		int rgbVal = (redVal << 16) | (greenVal << 8) | blueVal;
		//		rgbValues[loopx + (loopy*width)] = rgbVal;
		rgbValues[loopx + 0 + ((loopy+0)*width)] = rgbVal;
		rgbValues[loopx + 1 + ((loopy+0)*width)] = rgbVal;
		rgbValues[loopx + 2 + ((loopy+0)*width)] = rgbVal;
		rgbValues[loopx + 0 + ((loopy+1)*width)] = rgbVal;
		rgbValues[loopx + 1 + ((loopy+1)*width)] = rgbVal;
		rgbValues[loopx + 2 + ((loopy+1)*width)] = rgbVal;
		rgbValues[loopx + 0 + ((loopy+2)*width)] = rgbVal;
		rgbValues[loopx + 1 + ((loopy+2)*width)] = rgbVal;
		rgbValues[loopx + 2 + ((loopy+2)*width)] = rgbVal;
	    }
	    	    if(PRINT_COSTS)System.err.println(line);
	}
	if(PRINT_COSTS)System.err.println("----------------------------------------------------------------------------------------------------------------------------------");
	double[] totals = new double[costMaps.length+1];
	double[] means = new double[costMaps.length+1];
	double[] variances = new double[costMaps.length+1];
	double[] stddevs = new double[costMaps.length+1];
	int countCosts = 0;
	for (int loopy = 0; loopy < (height-3); loopy += 3) {
	    for (int loopx = 0; loopx < (width-3); loopx += 3) {
		for (int loopc = -1; loopc < costMaps.length; loopc++) {
		    totals[loopc+1] += costData[loopx][loopy][loopc+1];
		}
		countCosts++;
	    }
	}
	for (int loopc = -1; loopc < costMaps.length; loopc++) {
	    means[loopc+1] = totals[loopc+1]/countCosts;
	}
	for (int loopy = 0; loopy < (height-3); loopy += 3) {
	    for (int loopx = 0; loopx < (width-3); loopx += 3) {
		for (int loopc = -1; loopc < costMaps.length; loopc++) {
		    double diff = means[loopc+1] - costData[loopx][loopy][loopc+1];
		    variances[loopc+1] +=  (diff *diff);
		}
	    }
	}
	for (int loopc = -1; loopc < costMaps.length; loopc++) {
	    variances[loopc+1] = variances[loopc+1]/countCosts;
	}
	for (int loopc = -1; loopc < costMaps.length; loopc++) {
	    stddevs[loopc+1] = Math.sqrt(variances[loopc+1]);
	}
	
        /*
        for (int loopc = 0; loopc < costMaps.length; loopc++) {
	    System.err.println("PathDisplayPanel: costmap "+costMaps[loopc].getClass().getName()+" mean="+fmt.format(means[loopc+1])+" stddev="+fmt.format(stddevs[loopc+1]));
	}
	System.err.println("PathDisplayPanel: totals mean="+fmt.format(means[0])+" stddev="+fmt.format(stddevs[0]));
	
	System.err.println("----------------------------------------------------------------------------------------------------------------------------------");
        */
    }

    public void paint(Graphics g) {
	long startTime = System.currentTimeMillis();

        Graphics2D g2 = (Graphics2D)g;
	g2.setColor(ZERO_COST_COLOR);
	g2.clearRect(0,0,(int)getSize().getWidth(),(int)getSize().getHeight());

     	double screenW = getSize().getWidth();
     	double screenH = getSize().getHeight();
     	int iscreenW = (int)screenW;
     	int iscreenH = (int)screenH;
   	int rgbValues[] = new int[iscreenH * iscreenW];
    	buildCostMapRGBs(iscreenW, iscreenH, rgbValues);

	long rgbTime = System.currentTimeMillis();   

     	BufferedImage tempImage = new BufferedImage(iscreenW, iscreenH, BufferedImage.TYPE_INT_RGB);
     	tempImage.setRGB (0, 0, iscreenW, iscreenH, rgbValues, 0, iscreenW);
     	g2.drawImage(tempImage,0,0,null);

	long rgbToImageTime = System.currentTimeMillis();   

        // FIXME Need higher contrast so I can see the map & path
        g2.setColor (PATH_COLOR);
        g2.setStroke (new BasicStroke(1));
        
	if(null != path) {
	    Vector3D curr = path.getNext ();
        
	    if(null != curr) 
		g2.drawOval(toScreenX(iscreenW, curr.x)-5, toScreenY(iscreenH, curr.y), 10, 10);
	    //	g2.drawOval((int)(curr.x/RRTPlanner.mapStep)-5, getHeight() - (int)(curr.y/RRTPlanner.mapStep)-5, 10, 10);

	    if(path.size() > 0) {
		Path3D toDraw = new Path3D(path);
        
		toDraw.removeFirst ();
		while (toDraw.getNext () != null) {
		    Vector3D next = toDraw.getNext ();            
		    g2.drawLine(toScreenX(iscreenW, curr.x), toScreenY(iscreenH, curr.y), toScreenX(iscreenW, next.x), toScreenY(iscreenH, next.y));
		    //            g2.drawLine ((int)(curr.x/RRTPlanner.mapStep),  (int)((mapHeight - curr.y)/RRTPlanner.mapStep), (int)(next.x/RRTPlanner.mapStep), (int)((mapHeight - next.y)/RRTPlanner.mapStep));
		    //            g2.drawLine ((int)(curr.x/RRTPlanner.mapStep), getHeight() - (int)(curr.y/RRTPlanner.mapStep), (int)(next.x/RRTPlanner.mapStep), getHeight() - (int)(next.y/RRTPlanner.mapStep));
		    toDraw.removeFirst ();
		    curr = next;
		}
	    }
	}

	ovcmap = null;
	for (CostMap cm: cms) {
	    if(cm instanceof OtherVehicleCostMap)
		ovcmap = (OtherVehicleCostMap)cm;            
	}

	if( null != ovcmap) {
	    g2.setColor (OTHER_PATH_COLOR);
	    Iterator<Path3D> it = ovcmap.getIterator();
	    while(it.hasNext()) {
		Path3D otherPath = it.next();
		Path3D toDraw = new Path3D(otherPath);
		Vector3D curr = toDraw.getNext ();
		while (toDraw.getNext () != null) {
		    Vector3D next = toDraw.getNext ();            
		    g2.drawLine(toScreenX(iscreenW, curr.x), toScreenY(iscreenH, curr.y), toScreenX(iscreenW, next.x), toScreenY(iscreenH, next.y));
		    //		    g2.drawLine ((int)(curr.x/RRTPlanner.mapStep),  (int)((mapHeight - curr.y)/RRTPlanner.mapStep), (int)(next.x/RRTPlanner.mapStep), (int)((mapHeight - next.y)/RRTPlanner.mapStep));
		    toDraw.removeFirst ();
		    curr = next;
		}
	    }
	}

        long endTime = System.currentTimeMillis();
        long rgbTotal = rgbToImageTime - startTime;
        long rgbToImageTotal = rgbToImageTime - rgbTime;
        long totalTime = endTime - startTime;
      	Machinetta.Debugger.debug("paint: total="+totalTime+", rgb building="+rgbTotal+", rgbToImage="+rgbToImageTotal,0,this);
    }
}

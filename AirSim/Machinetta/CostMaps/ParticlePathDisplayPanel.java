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
 * ParticlePathDisplayPanel.java
 *
 */

package AirSim.Machinetta.CostMaps;

import AirSim.Environment.Vector3D;
import AirSim.Machinetta.Path3D;
import AirSim.Machinetta.RRTPlanner;
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
 * This class was meant to be a debug panel only and never meant to be
 * pushed as far as it has been... so the code really should be
 * cleaned up.  A lot.
 *
 * @author sean (based on PathDisplayPanel by Paul)
 */
public class ParticlePathDisplayPanel extends JPanel implements MouseMotionListener {
    public final static Color PARTICLE_COLOR = Color.red;
    public final static Color PATH_COLOR = Color.yellow;
    public final static DecimalFormat fmt = new DecimalFormat(".00000");


    double currX = 0;
    double currY = 0;
    ArrayList<CostMap> cms = null;
    Path3D path = null;
    boolean displayParticles = false;
    double mapWidth = 0;
    double mapHeight = 0;

    Point[] particles = null;
    ArrayList<Point> emitters = new ArrayList<Point>();
    public void setEmitters(ArrayList<Point> newEmitters) {
	emitters = newEmitters;
    }

    ParticleFilterCostMap pfcMap = null;    

    boolean showEntropy = false;

    /** Creates a new instance of ParticlePathDisplayPanel */

    public ParticlePathDisplayPanel(double currX, double currY, ArrayList<CostMap> cms, Path3D path, boolean showEntropy, boolean displayParticles, double mapWidth, double mapHeight) {
	this.currX = currX;
	this.currY = currY;
        this.path = new Path3D(path);
        this.cms = cms;
	this.showEntropy = showEntropy;
	this.displayParticles = displayParticles;
	this.mapWidth = mapWidth;
	this.mapHeight = mapHeight;
	for(int loopi = 0; loopi < cms.size(); loopi++) {
	    CostMap cm = cms.get(loopi);
	    if(cm instanceof ParticleFilterCostMap) {
		pfcMap = (ParticleFilterCostMap)cm;
		particles = (pfcMap).getParticlePoints();
	    }
	}
//  	emitters.add(new Point(45144, 45275));
//  	emitters.add(new Point(5344, 35275));
//  	emitters.add(new Point(25344, 25275));
	addMouseMotionListener(this);
    }
    
    public void setData(double newCurrX, double newCurrY, ArrayList<CostMap> newCms, Path3D newPath) {
 	final ArrayList<CostMap> newCmsF = newCms;
 	final Path3D newPathF = new Path3D(newPath);
 	final double newCurrXF = newCurrX;
 	final double newCurrYF = newCurrY;
	try {
	EventQueue.invokeAndWait(new Runnable() { 
		public void run() { 
		    path = newPathF;
		    cms = newCmsF;
		    currX = newCurrXF;
		    currY = newCurrYF;
		    for(int loopi = 0; loopi < newCmsF.size(); loopi++) {
			CostMap cm = newCmsF.get(loopi);
			if(cm instanceof ParticleFilterCostMap)
			    particles = ((ParticleFilterCostMap)cm).getParticlePoints();
		    }
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
    
    private int toScreenY(double y) {
	return (int)((mapHeight - y)/RRTPlanner.mapStep);
    }
    
    private int toScreenX(double x) {
	return (int)(x/RRTPlanner.mapStep);
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
	if(!showEntropy) {
	    for (int x = 0; x < RRTPlanner.mapSize; x+=RRTPlanner.mapStep) {
		for (int y = 0; y < RRTPlanner.mapSize; y+=RRTPlanner.mapStep) {
		    double cost = 0.0;
		    for (CostMap m: cms) {
			// FIXME visibility helpers
			cost += m.getCost (x, y, 0, x+1, y+1, 0);
			//cost += m.getCost (x, y, 0, x+10, y+10, 0);
		    }
		    if (cost > 0) {
			g2.setColor (new Color(Math.min(255, (int)cost*5), 0, 0));
		    } else if (cost < 0) {
			g2.setColor (new Color(0,Math.min(255, (int)-cost*5), 0));
		    } else {
			g2.setColor (Color.white);
		    }
		    // FIXME dropping vertical inflection about the x-axis: 
		    // g2.fillRect (x,500-y,1,1);
		    g2.fillRect (toScreenX(x),toScreenY(y),1,1);

		}
	    }
	}
	else {
	    long over1count = 0;
	    long under0count = 0;
	    double max = -10000;
	    double min = 10000;
	    for (int x = 0; x < RRTPlanner.mapSize; x+=RRTPlanner.mapStep) {
		for (int y = 0; y < RRTPlanner.mapSize; y+=RRTPlanner.mapStep) {
		    double entropy = pfcMap.getEntropyValue(x,y);
		    if(entropy > 1.0) over1count++;
		    else if(entropy < 0.0)	under0count++;
		    if(entropy < min) min = entropy;
		    if(entropy > max) max = entropy;
		    int redVal = 0;
		    int greenVal = 0;
		    int blueVal =  0;

		    if(Double.isNaN(entropy) || Double.isInfinite(entropy)) {
			g2.setColor(Color.cyan);
		    }
 		    else if(entropy == .0) {
			g2.setColor (Color.white);
		    }
 		    else if(entropy == 1.0) {
			g2.setColor (Color.pink);
		    }
 		    else if((entropy < .0001) && (entropy > -.0001)) {
			g2.setColor (Color.gray);
		    }
 		    else if(entropy < .0) {
			g2.setColor(Color.red);
		    }
 		    else if(entropy > 1.0) {
			g2.setColor(Color.magenta);
		    }
 		    else {
			blueVal = (int)(255 - entropy*192.0);
			if(blueVal < 0) blueVal = 0;
			if(blueVal > 255) blueVal = 255;
			g2.setColor (new Color(redVal, greenVal, blueVal));
		    }

		    g2.fillRect (toScreenX(x),toScreenY(y),1,1);

		}
	    }
	    Machinetta.Debugger.debug("minEntropy = "+min+", maxEntropy="+max+", over1count="+over1count+", under0count="+under0count,1,this);
	}

	if(displayParticles) {
	    if(null != particles) {
		g2.setColor(PARTICLE_COLOR);
		for(int loopi = 0; loopi < particles.length; loopi++) {
		    Point p = particles[loopi];
		    g2.fillOval(toScreenX(p.x)-1,toScreenY(p.y)-1,2,2);  
		}
	    }
	}
        
        // FIXME Need higher contrast so I can see the map & path
        g2.setColor (PATH_COLOR);
        g2.setStroke (new BasicStroke(2));
	g2.drawOval(toScreenX(currX)-4, toScreenY(currY) - 4, 9, 9);
	Path3D drawPath = new Path3D(path);
        Vector3D curr = drawPath.getNext ();
        drawPath.removeFirst ();
        while (drawPath.getNext () != null) {
            Vector3D next = drawPath.getNext ();
            g2.drawLine(toScreenX(curr.x), toScreenY(curr.y), toScreenX(next.x), toScreenY(next.y));
            drawPath.removeFirst ();
            curr = next;
        }

	g2.setColor(PATH_COLOR);
	for(int loopi = 0; loopi < emitters.size(); loopi++) {
	    Point emitter = emitters.get(loopi);
	    
	    g2.drawLine(toScreenX(emitter.x)-10,toScreenY(emitter.y),toScreenX(emitter.x)+10,toScreenY(emitter.y));
	    g2.drawLine(toScreenX(emitter.x),toScreenY(emitter.y)-10,toScreenX(emitter.x),toScreenY(emitter.y)+10);
	    
	}
    }

    // Invoked when a mouse button is pressed on a component and then
    // dragged.
    public void mouseDragged(MouseEvent e) {
	
    }

    //  Invoked when the mouse cursor has been moved onto a
    //  component but no buttons have been pushed.
    public void mouseMoved(MouseEvent e) {
	int costX = e.getX() * RRTPlanner.mapStep;
	int costY = (getHeight() - e.getY()) * RRTPlanner.mapStep;
	double cost = 0.0;
	if(!showEntropy) {
	    for (CostMap m: cms) {
		cost += m.getCost (costX, costY, 0, costX+1, costY+1, 0);
	    }
	}
	else {
	    cost = pfcMap.getEntropyValue(costX,costY);
	}
	setToolTipText("Cost = "+fmt.format(cost));
	setName("Cost = "+fmt.format(cost));
	Machinetta.Debugger.debug("Cost at "+costX+","+costY+" = "+fmt.format(cost),1,this);
    }

}

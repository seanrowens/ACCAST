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
 * BinaryBayesDisplay.java
 *
 */
package AirSim.Machinetta.CostMaps;

import AirSim.Environment.Vector3D;
import AirSim.Machinetta.Path3D;
import AirSim.Machinetta.UAVRI;
import AirSim.Machinetta.RRTPlanner;
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
import java.text.DecimalFormat;

/**
 * This class was meant to be a debug panel only and never meant to be
 * pushed as far as it has been... so the code really should be
 * cleaned up.  A lot.  @TODO: Change references to RRTPlanner.mapStep
 * to be a constructor arg.
 *
 * @author sean (based on PathDisplayPanel by Paul)
 */
public class BinaryBayesDisplay extends JPanel {
    public final static boolean LOG_BELIEF_VALUES = false;
    public final static boolean LOG_ENTROPY_VALUES = false;
    public final static boolean LOG_BELIEFS_NEAR_EMITTERS = false;

    public final static int RGB_BELIEF_EQ_ONE = Color.white.getRGB();
    public final static int RGB_BELIEF_GT_ONE = Color.green.getRGB();
    public final static int RGB_BELIEF_EQ_ZERO = Color.darkGray.getRGB();
    public final static int RGB_BELIEF_LT_ZERO = Color.magenta.getRGB();
    public final static boolean USE_SPECIAL_BELIEF_COLORS = true;
    
    public final static Color PATH_COLOR = Color.yellow;
    
    public final static Color[] CLUSTER_LIST_COLORS = {Color.magenta,Color.orange,Color.green,Color.blue,Color.pink};
    public final static DecimalFormat fmt = new DecimalFormat(".00000");
    
    private final static Debugger d = new Debugger();

    double prior;
    boolean showEntropy = false;
    boolean showBeliefs = false;
    double mapWidth = 0;
    double mapHeight = 0;
    double lowerLeftXmeters = 0;
    double lowerLeftYmeters = 0;
    
    double currX = 0;
    double currY = 0;
    BinaryBayesFilter bbfFilter;
    Path3D path = null;

    double[][] beliefs = null;
    DoubleGrid entropyGrid = null;
    
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
    
    public ArrayList<Clust> clustList;
    public void setClustList(ArrayList<Clust> clustList) {
        this.clustList = clustList;
    }
    
    /** Creates a new instance of BinaryBayesDisplay */
    public BinaryBayesDisplay(double currX, double currY, BinaryBayesFilter bbfFilter, Path3D path, boolean showEntropy, boolean showBeliefs, double mapWidth, double mapHeight, double lowerLeftXmeters,double lowerLeftYmeters, double prior) {
	d.debug("Constructor: curr X,Y="+currX+", "+currY+" showEntropy="+showEntropy+", showBeliefs="+showBeliefs+", mapWidth="+mapWidth+", mapHeight="+mapHeight,1,this);	
        this.currX = currX;
        this.currY = currY;
        this.path = new Path3D(path);
        this.bbfFilter = bbfFilter;
        this.showEntropy = showEntropy;
        this.showBeliefs = showBeliefs;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
	this.lowerLeftXmeters = lowerLeftXmeters;
	this.lowerLeftYmeters = lowerLeftYmeters;
	this.prior = prior;

	if(null != bbfFilter) {
	    int gridSize = bbfFilter.getSize();
	    if(showBeliefs) {
		beliefs = new double[gridSize][gridSize];
	    }
	    else if(showEntropy) {
		double scaleFactor = bbfFilter.getMapToProbArrayScaleFactor();
		entropyGrid = new DoubleGrid(gridSize,gridSize, (int)scaleFactor);	    
	    }
	}
    }

    public BinaryBayesDisplay(double currX, double currY, BinaryBayesFilter bbfFilter, Path3D path, boolean showEntropy, boolean showBeliefs, double mapWidth, double mapHeight) {
	this(currX, currY, bbfFilter, path, showEntropy, showBeliefs, mapWidth, mapHeight,0,0,UAVRI.BBF_UNIFORM_PRIOR );
    }
    
    public void setData(final double[][] newBeliefs, final ArrayList<Point> newUavs) {
        try {
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    beliefs = newBeliefs;
                    uavs = newUavs;
                }
            }
            );
        } catch(InterruptedException e) {
            System.err.println("Update of binary bayes panel was interrupted e="+e);
            e.printStackTrace();
        } catch(java.lang.reflect.InvocationTargetException e) {
            System.err.println("e="+e);
            e.printStackTrace();
        }
    }
    
    public void setData(final double newCurrX, final double newCurrY, final ArrayList<CostMap> newCms, final Path3D newPath) {
        try {
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    path = newPath;
                    currX = newCurrX;
                    currY = newCurrY;
                }
            }
            );
        } catch(InterruptedException e) {
            System.err.println("Update of costmap panel was interrupted e="+e);
            e.printStackTrace();
        } catch(java.lang.reflect.InvocationTargetException e) {
            System.err.println("e="+e);
            e.printStackTrace();
        }
    }

    private int toScreenY(int height, double y) {
        double dy = mapHeight/(double)height;
        return (int)(((mapHeight - (y-lowerLeftYmeters)) / dy)+.5);
    }
    
    private int toScreenX(int width, double x) {
        double dx = mapWidth/(double)width;
        return (int)(((x-lowerLeftXmeters)/dx)+.5);
    }
    
    private void buildEntropyRGBs(int width, int height, int rgbValues[]) {
        if(null == bbfFilter)
            return;
        
        long over1count = 0;
        long under0count = 0;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        
        bbfFilter.getEntropyGrid(entropyGrid);
        
        double dx = (double)entropyGrid.getWidth()/(double)width;
        double dy = (double)entropyGrid.getHeight()/(double)height;
        StringBuffer buf = new StringBuffer();
        if(LOG_ENTROPY_VALUES)
            d.debug("ENTROPY",1,this);
        
        for(int loopx = 0; loopx < width; loopx++ ) {
            if(LOG_ENTROPY_VALUES && (0 == (loopx%10))) {
                buf.setLength(0);
                buf.append(loopx+": " );
            }
            for(int loopy = 0; loopy < height; loopy++ ) {
                int entropyx = (int)(loopx * dx);
                int entropyy = (int)(loopy * dy);
                double entropy = entropyGrid.getValue(entropyx, entropyy);
                
                if(LOG_ENTROPY_VALUES && (0 == (loopx%10)) && (0 == (loopy%10))) {
                    buf.append(" "+fmt.format(entropy));
                }
                if(entropy > 1.0) over1count++;
                else if(entropy < 0.0)	under0count++;
                if(entropy < min) min = entropy;
                if(entropy > max) max = entropy;
                int redVal = 0;
                int greenVal = 0;
                int blueVal =  0;
                
                blueVal = (int)(63+ entropy*192.0);
                if(blueVal < 0) blueVal = 0;
                if(blueVal > 255) blueVal = 255;
                
                int rgbVal = (redVal <<16) | (greenVal << 8) | blueVal;                
                rgbValues[loopx+ ((height - 1 - loopy)*width)] = rgbVal;
            }
            if(LOG_ENTROPY_VALUES && 0 == (loopx%10)) {
                d.debug(buf.toString(),1,this);
            }
            
        }
        
        double total = 0;
        double mean = 0;
        double variance = 0;
        double stddev = 0;
        int countCosts = 0;
        for(int loopx = 0; loopx < width; loopx++ ) {
            for(int loopy = 0; loopy < height; loopy++ ) {
                int entropyx = (int)(loopx * dx);
                int entropyy = (int)(loopy * dy);
                double entropy = entropyGrid.getValue(entropyx, entropyy);
                total += entropy;
                countCosts++;
            }
        }
        mean = total/countCosts;
        for(int loopx = 0; loopx < width; loopx++ ) {
            for(int loopy = 0; loopy < height; loopy++ ) {
                int entropyx = (int)(loopx * dx);
                int entropyy = (int)(loopy * dy);
                double entropy = entropyGrid.getValue(entropyx, entropyy);
                double diff = mean - entropy;
                variance +=  (diff *diff);
            }
        }
        variance = variance/countCosts;
        stddev = Math.sqrt(variance);
        
        d.debug("entropy mean="+fmt.format(mean)+" stddev="+fmt.format(stddev),1,this);
        d.debug("minEntropy = "+min+", maxEntropy="+max+", over1count="+over1count+", under0count="+under0count,1,this);
    }
    
    private void buildBeliefRGBs(int screenWidth, int screenHeight, int rgbValues[]) {
        if(null == beliefs) {
	    d.debug(1,"buildBeliefRGBs: null beliefs, can't build belief RGBs");
	    return;
	}
	bbfFilter.copyBeliefs(beliefs);

	double belWidth=beliefs.length;
	double belHeight=beliefs[0].length;

	double dx = belWidth/(double)screenWidth;
	double dy = belHeight/(double)screenHeight;
	StringBuffer buf = new StringBuffer();
	int lastRgbVal = 0;
	double lastbelief = 0;
	int hitcount = 0;

	if(LOG_BELIEF_VALUES)
	    d.debug("BELIEFS",1,this);        
	for (int loopx = 0; loopx < screenWidth; loopx++) {
	    if(LOG_BELIEF_VALUES && (0 == (loopx%10))) {
		buf.setLength(0);
		buf.append(loopx+": " );
	    }
	    for (int loopy = 0; loopy < screenHeight; loopy++) {
		int beliefx = (int)(loopx * dx);
		int beliefy = (int)(loopy * dy);
		
		double belief = beliefs[beliefx][beliefy];
		if(LOG_BELIEF_VALUES && (0 == (loopx%10)) && (0 == (loopy%10))) {
		    buf.append(" "+fmt.format(belief));
		}

		int rgbVal = lastRgbVal;
		int redVal = 0;
		int greenVal = 0;
		int blueVal = 0;
		if(lastbelief != belief) {
		    redVal = (int)Math.min(255, (255.0 * (belief + .25)));
		    if(belief < prior)
			redVal = 0;

		    rgbVal = (redVal << 16) | (greenVal << 8) | blueVal;

		    if(USE_SPECIAL_BELIEF_COLORS) {
			if(belief == 1.0) {
			    rgbVal = RGB_BELIEF_EQ_ONE;
			}
			if(belief > 1.0) {
			    rgbVal = RGB_BELIEF_GT_ONE;
			}
			if(belief == 0.0) {
			    rgbVal = RGB_BELIEF_EQ_ZERO;
			}
			if(belief < 0.0) {
			    rgbVal = RGB_BELIEF_LT_ZERO;
			}
		    }
		}
		else {
		    hitcount++;
		}
		lastbelief = belief;
		lastRgbVal = rgbVal;

		rgbValues[loopx+ ((screenHeight - 1 - loopy)*screenWidth)] = rgbVal;
	    }
	    if(LOG_BELIEF_VALUES && 0 == (loopx%10)) {
		d.debug(buf.toString(),1,this);
	    }
	}
    }
    
    private void printBeliefs(Point emitter) {
        if(null == beliefs)
            return;
        
        int emitterx = emitter.x / RRTPlanner.mapStep * 2;
        int emittery = emitter.y / RRTPlanner.mapStep * 2;
        
        int startx = emitterx - 40;
        int endx = emitterx + 40;
        int starty = emittery - 40;
        int endy = emittery + 40;
        if(startx < 0) startx = 0;
        if(starty < 0) starty = 0;
        if(endx < 0) endx = 0;
        if(endy < 0) endy = 0;
        if(startx >= beliefs.length) startx = beliefs.length - 1;
        if(starty >= beliefs[0].length) starty = beliefs[0].length -1;
        if(endx >= beliefs.length) endx = beliefs.length - 1;
        if(endy >= beliefs[0].length) endy = beliefs[0].length -1;
        
        double highest = -10000000000.0;
        StringBuffer buf = new StringBuffer();
        d.debug("BELIEFS",1,this);
        for(int loopy = starty; loopy <= endy; loopy++) {
            buf.setLength(0);
            buf.append("BELIEF["+loopy+"]: " );
            for(int loopx = startx; loopx <= endx; loopx++) {
                double belief = beliefs[loopx][loopy];
                if(belief > highest)
                    highest = belief;
                if(0.0 == belief)
                    buf.append(" 0     ");
                else if(1.0 == belief)
                    buf.append(" 1     ");
                else
                    buf.append(" "+fmt.format(belief));
            }
            d.debug(buf.toString(),1,this);
        }
        d.debug("Highest="+highest,1,this);
    }
    
    private void drawClusters(Graphics g, Graphics2D g2, ArrayList<Clust> clusters, Color clusterColor, int iscreenW, int iscreenH) {
	if(null == clusters)
	    return;
        g2.setStroke(new BasicStroke(2));
        for(int loopi = 0; loopi < clusters.size(); loopi++) {
            Clust cluster = clusters.get(loopi);
	    int y = toScreenY(iscreenH, cluster.y);
	    int x = toScreenX(iscreenW, cluster.x);
	    int meanSize = (int)(iscreenW/mapWidth * cluster.memberDistMean);
	    int maxSize = (int)(iscreenW/mapWidth * cluster.memberDistMax);
	    int fourkm = (int)(iscreenW/mapWidth * 4000);

	    g2.setColor(clusterColor);
            g.drawOval(x-meanSize,y-meanSize,meanSize*2,meanSize*2);
	    if(!cluster.failed) {
		g2.setColor(Color.red);
		g.drawOval(x-fourkm/2,y-fourkm/2,fourkm,fourkm);
		g.drawString(cluster.getKey(), x-fourkm/2, y);
	    }
        }
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        double screenW = getSize().getWidth();
        double screenH = getSize().getHeight();
        int iscreenW = (int)screenW;
        int iscreenH = (int)screenH;
        
        int rgbValues[] = new int[iscreenH * iscreenW];
        
        long startTime = System.currentTimeMillis();
        if(showEntropy) {
            buildEntropyRGBs(iscreenW, iscreenH, rgbValues);
        }
        
        long entropyTime = System.currentTimeMillis();
        if(showBeliefs) {
            buildBeliefRGBs(iscreenW, iscreenH, rgbValues);
            
            if(LOG_BELIEFS_NEAR_EMITTERS) {
                for(int loopi = 0; loopi < emitters.size(); loopi++) {
                    Point emitter = emitters.get(loopi);
                    printBeliefs(emitter);
                }
            }
        }
        long beliefTime = System.currentTimeMillis();

        BufferedImage tempImage = new BufferedImage(iscreenW, iscreenH, BufferedImage.TYPE_INT_RGB);
        tempImage.setRGB(0, 0, iscreenW, iscreenH, rgbValues, 0, iscreenW);
        g2.drawImage(tempImage,0,0,null);
        
        long rgbToImageTime = System.currentTimeMillis();
        
        // FIXME Need higher contrast so I can see the map & path
        g2.setColor(PATH_COLOR);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(toScreenX(iscreenW, currX)-4, toScreenY(iscreenH, currY) - 4, 9, 9);
        if(null != uavs) {
            for(int loopi = 0; loopi < uavs.size(); loopi++) {
                Point uav = uavs.get(loopi);
                g.drawOval(toScreenX(iscreenW, uav.x)-4,toScreenY(iscreenH, uav.y)-4,9,9);
            }
        }
        
        if(null != path && path.getSize() > 0) {
            Path3D drawPath = new Path3D(path);
            Vector3D curr = drawPath.getNext();
            drawPath.removeFirst();
            while (drawPath.getNext() != null) {
                Vector3D next = drawPath.getNext();
                g2.drawLine(toScreenX(iscreenW, curr.x), toScreenY(iscreenH, curr.y), toScreenX(iscreenW, next.x), toScreenY(iscreenH, next.y));
                drawPath.removeFirst();
                curr = next;
            }
        }
        
        g2.setColor(PATH_COLOR);
        g2.setStroke(new BasicStroke(1));
        for(int loopi = 0; loopi < emitters.size(); loopi++) {
            Point emitter = emitters.get(loopi);
            g.drawOval(toScreenX(iscreenW, emitter.x)-20,toScreenY(iscreenH, emitter.y)-20,40,40);
        }

	if(null != listOfClusterLists) {
	    for(int loopi = 0; loopi < listOfClusterLists.size(); loopi++) {
		ArrayList<Clust> clist = listOfClusterLists.get(loopi);
		Color cColor = null;
		if((clist.size()-1) < CLUSTER_LIST_COLORS.length) {
		    cColor = CLUSTER_LIST_COLORS[(clist.size()-1)];
		}
		else {
		    cColor = CLUSTER_LIST_COLORS[CLUSTER_LIST_COLORS.length - 1];
		}
		drawClusters(g, g2, clist, cColor, iscreenW, iscreenH);
	    }
	}

	if(null != clustList) {
	    drawClusters(g, g2, clustList, Color.magenta, iscreenW, iscreenH);
	}

        long endTime = System.currentTimeMillis();
        long entropyTotal = entropyTime - startTime;
        long beliefTotal = beliefTime - entropyTime;
        long rgbToImageTotal = rgbToImageTime - beliefTime;
        long pathTotal = endTime - rgbToImageTime;
        long totalTime = endTime - startTime;
	//        d.debug("paint: total="+totalTime+", entropy="+entropyTotal+", beliefs="+beliefTotal+", rgbToImage="+rgbToImageTotal+", path="+pathTotal,1,this);
        
    }
    
}

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
package Gui;

// @author      Sean R. Owens
// @version     $Id: TerrainCanvas.java,v 1.8 2007/12/12 21:09:24 owens Exp $ 

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.text.DecimalFormat;



////Test
//import com.sun.j3d.loaders.objectfile.ObjectFile;
//import com.sun.j3d.loaders.ParsingErrorException;
//import com.sun.j3d.loaders.IncorrectFormatException;
//import com.sun.j3d.loaders.Scene;
//
//import com.sun.j3d.utils.universe.*;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import javax.media.j3d.*;
//import javax.vecmath.*;
//import java.awt.GraphicsConfiguration;
//import java.net.MalformedURLException;
//import java.net.URL;
//import com.sun.j3d.utils.behaviors.vp.*;
//import java.io.FileNotFoundException;



// The plan is, we have a subclass of Canvas that will take care of
// all the view port crap, and converting from world coords to display
// (canvas, really) coords.  The canvas takes care of mapping from
// world coords in general to a view port that is displayed on the
// canvas, calls the various model objects (IntGrid, etc) to create
// the background image, then calls each of the objects on the map to
// draw onto the canvas.  When it calls IntGrid, it specifies what
// part of the IntGrid to display, and how large an image to use to
// display it.
//
// The canvas also maintains configuration state, i.e. which options
// the user has selected for how the map background image is
// displayed.  These options are altered by radio buttons, etc, that
// are on a separate sub panel.

// @TODO: This originally inherited from Canvas, but Canvases aren't
// double buffered, so I changed to inherit from JPanel.  But I
// already have a class named TerrainPanel.  Need to rename it to
// something proper.
public class TerrainCanvas extends JPanel implements Observer {
    public final static boolean SHOW_FPS = true;
    public final static boolean SHOW_MSG_COUNTS = true;
    public final static float LINE_STROKE = 1.5f;

    private final static DecimalFormat fmt = new DecimalFormat("0.00");
    private final static DecimalFormat fmt2 = new DecimalFormat("0");
    private final static Font FPS_FONT = new Font("Monospaced", Font.PLAIN, 8);
    private final static int FPS_X = 10;
    private final static int FPS_Y = 20;
    private final static Font MSG_STAT_FONT = new Font("Monospaced", Font.BOLD, 18);
    private final static int MSG_STAT_X = 10;
    private final static int MSG_STAT_Y = 40;

    private double sentMsgs=0;
    public void setSentMsgs(double val) { sentMsgs = val;}
    private double resentMsgs=0;
    public void setResentMsgs(double val) { resentMsgs = val;}
    private double failedMsgs=0;
    public void setFailedMsgs(double val) { failedMsgs = val;}

    long redrawTimes[] = new long[15];
    long lastRedrawTime = System.currentTimeMillis();
    int redrawTimesCounter = 0;

    private DebugInterface debug = null;
    private MapDB mapDB = null;
    private BackgroundConfig config = null;
    private Background background = null;
    private double probSignificantMin = 0.0;
    public void setProbSignificantMin(double value) { probSignificantMin = value;}

    private ViewPort viewPort = null;

    private BufferedImage img = null;

    private BufferedImage backImg = null;
    private boolean backInvalid = false;

    private BufferedImage foreImg = null;
    private boolean foreInvalid = false;

    private boolean selectionBoxOn = false;
    private int selectX;
    private int selectY;
    private int selectWidth;
    private int selectHeight;
    
    private JPanel drawingPanel;
    
    public synchronized void setSelectionRect(int x1, int y1, int x2, int y2) {
	if(x1 > x2) {
	    int temp = x1; 
	    x1 = x2;
	    x2 = temp;
	}
	if(y1 > y2) {
	    int temp = y1; 
	    y1 = y2;
	    y2 = temp;
	}
	synchronized(this) {
	    selectX = x1;
	    selectY = y1;
	    selectWidth = x2 - x1;
	    selectHeight = y2 - y1;
	}
	selectionBoxOn = true;
	foreInvalid = true;
	mapDB.setDirty(true);
	repaint();
    }
    public synchronized void setSelectionOff() {
	selectionBoxOn = false;
	mapDB.setDirty(true);
	repaint();
    }

//        //Test
//    private boolean spin = false;
//    private boolean noTriangulate = false;
//    private boolean noStripify = false;
//    private double creaseAngle = 60.0;
//    private URL filename = null;
//
//    private SimpleUniverse univ = null;
//    private BranchGroup scene = null;
//
//    public BranchGroup createSceneGraph() {
//	// Create the root of the branch graph
//	BranchGroup objRoot = new BranchGroup();
//
//        // Create a Transformgroup to scale all objects so they
//        // appear in the scene.
//        TransformGroup objScale = new TransformGroup();
//        Transform3D t3d = new Transform3D();
//        t3d.setScale(0.9); // 0.7
//        objScale.setTransform(t3d);
//        objRoot.addChild(objScale);
//
//	// Create the transform group node and initialize it to the
//	// identity.  Enable the TRANSFORM_WRITE capability so that
//	// our behavior code can modify it at runtime.  Add it to the
//	// root of the subgraph.
//	TransformGroup objTrans = new TransformGroup();
//	objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
//	objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
//	objScale.addChild(objTrans);
//
//	int flags = ObjectFile.RESIZE;
//	if (!noTriangulate) flags |= ObjectFile.TRIANGULATE;
//	if (!noStripify) flags |= ObjectFile.STRIPIFY;
//	ObjectFile f = new ObjectFile(flags, 
//	  (float)(creaseAngle * Math.PI / 180.0));
//	Scene s = null;
//	try {
//	  s = f.load(filename);            
//	}
//	catch (FileNotFoundException e) {
//	  System.err.println(e);
//	  System.exit(1);
//	}
//	catch (ParsingErrorException e) {
//	  System.err.println(e);
//	  System.exit(1);
//	}
//	catch (IncorrectFormatException e) {
//	  System.err.println(e);
//	  System.exit(1);
//	}
//	  
//	objTrans.addChild(s.getSceneGroup());
//
//	BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
//
//        if (spin) {
//	  Transform3D yAxis = new Transform3D();
//	  Alpha rotationAlpha = new Alpha(-1, Alpha.INCREASING_ENABLE,
//					  0, 0,
//					  4000, 0, 0,
//					  0, 0, 0);
//
//	  RotationInterpolator rotator =
//	      new RotationInterpolator(rotationAlpha, objTrans, yAxis,
//				       0.0f, (float) Math.PI*2.0f);
//	  rotator.setSchedulingBounds(bounds);
//	  objTrans.addChild(rotator);
//	} 
//
//        // Set up the background
//        Color3f bgColor = new Color3f(0.05f, 0.05f, 0.5f);
//        javax.media.j3d.Background bgNode = new javax.media.j3d.Background(bgColor);
//        bgNode.setApplicationBounds(bounds);
//        objRoot.addChild(bgNode);
//
//	return objRoot;
//    }
//    
//    private Canvas3D createUniverse() {
//	// Get the preferred graphics configuration for the default screen
//	GraphicsConfiguration config =
//	    SimpleUniverse.getPreferredConfiguration();
//
//	// Create a Canvas3D using the preferred configuration
//	Canvas3D canvas3d = new Canvas3D(config);
//
//	// Create simple universe with view branch
//	univ = new SimpleUniverse(canvas3d);
//        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
//
//	// add mouse behaviors to the ViewingPlatform
//	ViewingPlatform viewingPlatform = univ.getViewingPlatform();
//
//	PlatformGeometry pg = new PlatformGeometry();
//
//	// Set up the ambient light
//	Color3f ambientColor = new Color3f(0.1f, 0.1f, 0.1f);
//	AmbientLight ambientLightNode = new AmbientLight(ambientColor);
//	ambientLightNode.setInfluencingBounds(bounds);
//	pg.addChild(ambientLightNode);
//
//	// Set up the directional lights
//	Color3f light1Color = new Color3f(1.0f, 1.0f, 0.9f);
//	Vector3f light1Direction  = new Vector3f(1.0f, 1.0f, 1.0f);
//	Color3f light2Color = new Color3f(1.0f, 1.0f, 1.0f);
//	Vector3f light2Direction  = new Vector3f(-1.0f, -1.0f, -1.0f);
//
//	DirectionalLight light1
//	    = new DirectionalLight(light1Color, light1Direction);
//	light1.setInfluencingBounds(bounds);
//	pg.addChild(light1);
//
//	DirectionalLight light2
//	    = new DirectionalLight(light2Color, light2Direction);
//	light2.setInfluencingBounds(bounds);
//	pg.addChild(light2);
//
//	viewingPlatform.setPlatformGeometry( pg );
//      
//	// This will move the ViewPlatform back a bit so the
//	// objects in the scene can be viewed.
//	viewingPlatform.setNominalViewingTransform();
//
//	if (!spin) {
//            OrbitBehavior orbit = new OrbitBehavior(canvas3d,
//						    OrbitBehavior.REVERSE_ALL);
//            orbit.setSchedulingBounds(bounds);
//            viewingPlatform.setViewPlatformBehavior(orbit);	    
//	}        
//        
//        // Ensure at least 5 msec per frame (i.e., < 200Hz)
//	univ.getViewer().getView().setMinimumFrameCycleTime(5);
//
//	return canvas3d;
//    }
    
    
    
    
    public TerrainCanvas(MapDB mapDB, BackgroundConfig config, Background background) {
	debug = new DebugFacade(this);
	this.mapDB = mapDB;
	this.config = config;
	this.background = background;
// 	if(null == config.viewPort) {
// 	    config.viewPort = new ViewPort(background.getSourceBottomLeftX(), 
// 					   background.getSourceBottomLeftY(),
// 					   background.getSourceWidth(), 
// 					   background.getSourceHeight(), 
// 					   1000.0, 1000.0);
// 	    background.setViewPort(config.viewPort);
// //	    config.viewPort.requestSourceView(background.getSourceBottomLeftX(), 
// // 					      background.getSourceBottomLeftY(),
// // 					      background.getSourceBottomLeftX() + background.getSourceWidth(), 
// // 					      background.getSourceBottomLeftY() + background.getSourceHeight());
// 	    config.viewPort.requestSourceViewFitToScreen(background.getSoilGrid());
// 	}
// 	background.emptyCache();

	background.setViewPort(config.viewPort);
	config.viewPort.setTccToGccTransform(background.getTccToGccTransform());
	mapDB.addObserver(this);
	config.addObserver(this);
	background.addObserver(this);
	//	setSize(600,600);
        

        
        /*
        //Test
        try {
                String name = "/home/junyounk/galleon.obj";
                filename = new URL("file:" + name);
                if (filename == null) {
                    System.err.println(name + " not found");
                    System.exit(1);
                }
            } catch (MalformedURLException ex) {
                //Logger.getLogger(ObjLoad.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        drawingPanel = new javax.swing.JPanel();

        //setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        //setTitle("ObjLoad");
        //GridBagLayout layout = new GridBagLayout();        
        //drawingPanel.setLayout(layout);
        
        drawingPanel.setLayout(new java.awt.BorderLayout());

        drawingPanel.setPreferredSize(new java.awt.Dimension(100, 100));
        //drawingPanel.setLocation(10000,20000);
        
        //getContentPane().add(drawingPanel, java.awt.BorderLayout.CENTER);

        //pack();
        // Create Canvas3D and SimpleUniverse; add canvas to drawing panel
	Canvas3D c = createUniverse();        
        
       
        drawingPanel.add(c, java.awt.BorderLayout.CENTER);
        //drawingPanel.add(c, new Dimension(100, 200));
        //drawingPanel.createImage()
        
	//drawingPanel.add(c, cons);        
        //drawingPanel.setBounds(100, 100, 50, 50);
        
	// Create the content branch and add it to the universe
	scene = createSceneGraph();
	univ.addBranchGraph(scene);
        
        this.add(drawingPanel);
        //this.add(drawingPanel, )
        */
        
    }

    // Observer implementation - when delete is clicked, delete
    // currently selected map object.
    public void update(Observable  o, Object  arg) {
	if(o == config) {
	    debug.debug("Got an update from config panel");
	    backInvalid = true;
	    repaint();
	}
	else if(o == background) {
	    debug.debug("Got an update from config panel");
	    backInvalid = true;
	    repaint();
	}
	else if (o == mapDB) {
	    foreInvalid = true;
	    repaint();
	}
    }
    
    public void draw(BufferedImage drawingImg) {
	Graphics drawingGraphics = drawingImg.createGraphics();
	int drawingWidth  = drawingImg.getWidth();
	int drawingHeight = drawingImg.getHeight();

	config.viewPort.setDest((double)drawingWidth, (double)drawingHeight);
	if(null == foreImg)
	    foreInvalid = true;
	if(null == backImg)
	    backInvalid = true;
	else if((drawingWidth != backImg.getWidth()) || (drawingHeight != backImg.getHeight())) {
	    foreInvalid = true;
	    backInvalid = true;
	}

	if(backInvalid) {
	    if((null == backImg)
	       || (backImg.getWidth() != drawingWidth)
	       || (backImg.getHeight() != drawingHeight)) {
		backImg = ((Graphics2D)drawingGraphics).getDeviceConfiguration().createCompatibleImage(drawingWidth, drawingHeight);
	    }
	    background.drawBackground(backImg, config);
	    backInvalid = false;
	}
	if(foreInvalid) {
	    long startTime = System.currentTimeMillis();
	    
	    if((null == foreImg)
	       || (foreImg.getWidth() != drawingWidth)
	       || (foreImg.getHeight() != drawingHeight)) {
		foreImg = ((Graphics2D)drawingGraphics).getDeviceConfiguration().createCompatibleImage(drawingWidth, drawingHeight);
	    }
	    Graphics foreGraphics = foreImg.createGraphics();
	    foreGraphics.drawImage(backImg,0,0,null);

	    MapObject mapObjects[] = mapDB.getMapObjects();
	    if(null != mapObjects) {
		for(int loopLayer = MapObject.LOWEST_LAYER_INCLUSIVE; loopLayer <= MapObject.HIGHEST_LAYER_INCLUSIVE; loopLayer++) {
		    for(int loopi = 0; loopi < mapObjects.length; loopi++) {
			MapObject mo = mapObjects[loopi];
			if(null == mo)
			    continue;
			if(loopLayer != mo.getLayer())
			    continue;
			if(config.showTraces) {
			    if(mo.getTracePointSize() >= 2) {
				mo.drawTraceLine(foreGraphics, config.viewPort);
			    }
			    if(mo.getPlannedPathPointSize() >= 2) {
				mo.drawPlannedPathLine(foreGraphics, config.viewPort);
			    }
			}
		    }
		    for(int loopi = 0; loopi < mapObjects.length; loopi++) {
			MapObject mo = mapObjects[loopi];
			if(null == mo)
			    continue;
			if(loopLayer != mo.getLayer())
			    continue;
			if(mo.getProbSignificant() < probSignificantMin) {
			    //			    debug.debug("Skipping mo="+mo.getKey()+" because probSignificantMin ("+probSignificantMin+") > mo.getProbSignificant() ("+mo.getProbSignificant()+")");
			    continue;
			}
			synchronized(mo) {
			    mo.draw(foreGraphics, config.viewPort, config.showMapObjectTypes, config.showMapObjectNames, config.showTraces);
			}
		    }
		}
	    }
	    foreGraphics.dispose();
	    foreGraphics = null;

	    long elapsed = (System.currentTimeMillis() - startTime);
	    //	    debug.debug("        redrawing foreImage, elapsed time="+elapsed);
	}

	drawingGraphics.drawImage(foreImg,0,0,null);
	drawingGraphics.dispose();
    }

    private int drawWithDropShadow(Graphics g, String text, int x, int y) {
	g.setColor(Color.black);
	g.drawString(text,x,y);
	g.setColor(Color.white);
	g.drawString(text,x-1,y-1);

	FontRenderContext frc = ((Graphics2D)g).getFontRenderContext();
	Font f = g.getFont();
	Rectangle2D stringBounds = f.getStringBounds(text,frc);
	return (int)stringBounds.getHeight();
    }

    long lastRepaint = System.currentTimeMillis();
    public void paint(Graphics g) {
	long startTime = System.currentTimeMillis();
	super.paint(g);
	Dimension d = getSize();
	if(backInvalid || foreInvalid || (null == img) || (null == backImg) || (null == foreImg) || (d.width != img.getWidth()) || (d.height != img.getHeight())) {
	    if((null == img)
	       || (img.getWidth() != d.getWidth())
	       || (img.getHeight() != d.getHeight())) {
		img = ((Graphics2D)g).getDeviceConfiguration().createCompatibleImage(d.width, d.height);
	    }
	    draw(img);
	}
	g.clearRect(0,0,(int)d.getWidth(), (int)d.getHeight());
	g.setColor(SoilTypes.DEFAULT_SOIL_COLOR);
	g.fillRect(0,0,(int)d.getWidth(), (int)d.getHeight());
	g.drawImage(img,0,0,null);
	((Graphics2D)g).setStroke(new BasicStroke(LINE_STROKE));
	g.setColor(Color.white);
	synchronized(this) {
	    if(selectionBoxOn) 
		g.drawRect(selectX, selectY, selectWidth, selectHeight);
 	}
	Font curFont = g.getFont();
	if(SHOW_FPS) {
	    long now = System.currentTimeMillis();
	    redrawTimes[redrawTimesCounter++] = now - lastRedrawTime;
	    lastRedrawTime = now;
	    if(redrawTimesCounter >= redrawTimes.length)
		redrawTimesCounter = 0;
	    long totalRedrawTime = 0;
	    for(int loopi = 0; loopi < redrawTimes.length; loopi++) {
		totalRedrawTime += redrawTimes[loopi];
	    }
	    double avgRedrawTime = ((double)redrawTimes.length)/((double)totalRedrawTime) * 1000;

	    g.setFont(FPS_FONT);
	    g.drawString("FPS="+fmt.format(avgRedrawTime),FPS_X,FPS_Y);
	}
	if((SHOW_MSG_COUNTS) && (sentMsgs != 0)) {
	    int posx = MSG_STAT_X;
	    int posy = MSG_STAT_Y;

	    g.setFont(MSG_STAT_FONT);

	    posy += drawWithDropShadow(g,"  Msgs Sent: "+fmt2.format(sentMsgs),posx,posy);
	    posy += drawWithDropShadow(g,"Msgs Resent: "+fmt2.format(resentMsgs)+" ("+fmt.format(100 * (resentMsgs/sentMsgs))+"%)",posx,posy);
	    posy += drawWithDropShadow(g,"Msgs Failed: "+fmt2.format(failedMsgs)+" ("+fmt.format(100 * (failedMsgs/sentMsgs))+"%)",posx,posy);
	}
	g.setFont(curFont);
	long now = System.currentTimeMillis();
	long elapsed = now - startTime;
	//	debug.debug("        repainting after "+(now - lastRepaint)+"ms, elapsed time="+elapsed);
	lastRepaint = now;
    }
}


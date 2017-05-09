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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
// @author      Sean R. Owens
// @version     $Id: Background.java,v 1.9 2007/12/14 04:05:20 junyounk Exp $ 
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.imageio.ImageIO;

import com.georgeandabe.tigerline.model.TLData;
import com.georgeandabe.tigerline.renderer.TLRenderer;

// This class keeps references to all of the objects used to generate
// the background image, and redraws background images for a variety
// of panels, on command.  It also implements Observer to be notified
// when background object state changes and extends Observerable to
// notify any such panels of changes, i.e. it multiplexes any
// background object changes out to all panels.
public class Background extends Observable implements Observer {
    public final static float SCREEN_GRID_LINE_ONE_KM_STROKE = 1.0f;
    public final static float SCREEN_GRID_LINE_TEN_KM_STROKE = 3.0f;
    public final static float PRINTABLE_GRID_LINE_ONE_KM_STROKE = 3.0f;
    public final static float PRINTABLE_GRID_LINE_TEN_KM_STROKE = 3.0f;

    public final static float GRID_LINE_ONE_KM_STROKE = SCREEN_GRID_LINE_ONE_KM_STROKE;
    public final static float GRID_LINE_TEN_KM_STROKE = SCREEN_GRID_LINE_TEN_KM_STROKE;
    private final static Color GRID_LINE_COLOR = Color.black;
    private DebugInterface debug = null;
    private DoubleGrid elevationGrid = null;
    public double getSourceBottomLeftX() { return (double) soilGrid.getBottomLeftX();}
    public double getSourceBottomLeftY() { return (double) soilGrid.getBottomLeftY();}
    public double getSourceWidth() { return soilGrid.getWidthMeters();}
    public double getSourceHeight() { return soilGrid.getHeightMeters();}
    public double[][] getTccToGccTransform() { return soilGrid.getTccToGccTransform();}

    // TODO: SRO Thu Jul 20 20:26:16 EDT 2006
    //
    // sometime down the line I want to chagne this to use an Overlay
    // class that stores a Grid (parent class of IntGrid/DoubleGrid)
    // and also can store a color RGBA array and/or flags for drawing
    // with DoubleGrid.  But not right now.
    private ArrayList<DoubleGrid> overlayList = null;
    public ArrayList<DoubleGrid> getOverlayList() { return overlayList; }
    public void setOverlayList(ArrayList<DoubleGrid> list) { overlayList = list; }
    private BufferedImage overlayImg = null;
    private boolean overlayInvalid = false;
    public void overlayListDirty() { overlayInvalid = true; }

    private IntGrid soilGrid = null;
    public IntGrid getSoilGrid() { return soilGrid;}
    private IntGrid contourGrid = null;
    private IntGrid obstacleGrid = null;
    private IntGrid configSpaceGrid = null;
    private Contour contours = null;
    private ViewPort viewPort = null;
    public void setViewPort(ViewPort port) {
	this.viewPort = port;
	viewPort.addObserver(this);
    }
    private Image soilCacheImage = null;
    private Image contourCacheImage = null;
    private int contourCacheMultiples = 0;
    private Image obstacleCacheImage = null;
    private Image overlayCacheImage = null;
    private Image configSpaceCacheImage = null;
    private Image roadCacheImage = null;
    private Image satCacheImage = null;
    private Image scaledSoilCacheImage = null;
    private Image scaledContourCacheImage = null;
    private Image scaledObstacleCacheImage = null;
    private Image scaledOverlayCacheImage = null;
    private Image scaledConfigSpaceCacheImage = null;
    private Image scaledRoadCacheImage = null;
    private Image scaledSatCacheImage = null;

    private TLRenderer roadRenderer = null;
    
    // @todo: one of things it'd be nice to do one of these days is to
    // revamp this to use a more dynamic approach - i.e. lets
    // define/find some kind of image factory interface, then we get
    // handed a list of name/imagefactory pairs, and we can
    // dynamically turn them on/off.  Then we can write one (or really
    // two, one for IntGrid and one for DoubleGrid) grid imagefactory
    // and we can overlay any number of grids in whatever order we
    // want them.
    
    // This class holds all of the components that make up the
    // background image and overlays for the TerrainCanvas. 
    //
    // Note that other than soilGrid, all other parameters may be null
    // and will then simply be ignored.
    //
    // @param elevationGrid The elevations on the map - this parameter may be null.
    // @param soilGrid The soil types of the map - this parameter may not be null.
    // @param contourGrid A grid to hold the contour lines on the map - this parameter may be null. 
    // @param obstacleGrid The obstacles on the map - this parameter may be null.
    // @param configSpaceGrid The config spaces on the map - this parameter may be null.
    // @param contours The contours on the map - this parameter may be null.  

    public Background(DoubleGrid elevationGrid, IntGrid soilGrid, IntGrid contourGrid, IntGrid obstacleGrid, IntGrid configSpaceGrid, Contour contours, TLData roadData) {
	debug = new DebugFacade(this);
	this.elevationGrid = elevationGrid;
	this.soilGrid = soilGrid;
	this.contourGrid = contourGrid;
	this.obstacleGrid = obstacleGrid;
	this.configSpaceGrid = configSpaceGrid;
	this.contours = contours;

	int colorRGBs[] = null;
    	colorRGBs = new int[2];
     	colorRGBs[0] = Color.red.getRGB() & 0x80FFFFFF;
     	colorRGBs[1] = Color.black.getRGB() & 0x00FFFFFF;
	if(null != obstacleGrid)
	    obstacleCacheImage = obstacleGrid.drawImage(colorRGBs);

    	colorRGBs = new int[2];
     	colorRGBs[0] = SoilTypes.DEFAULT_SOIL_COLOR.getRGB() & 0x80FFFFFF;
     	colorRGBs[1] = SoilTypes.DEFAULT_SOIL_COLOR.getRGB() & 0x00FFFFFF;
	if(null != contourGrid)
	    contourCacheImage = contourGrid.drawImage(colorRGBs);

	colorRGBs = SoilTypes.getSoilColors();
	soilCacheImage = soilGrid.drawSoilImage(colorRGBs);
	
	if(null != configSpaceGrid) {
	    colorRGBs =  UnitSizes.getUnitColors(configSpaceGrid.getGridCellSize());
	    for(int loopi = 0; loopi < colorRGBs.length; loopi++) {
		colorRGBs[loopi] = (colorRGBs[loopi] & 0x00FFFFFF) | 0x30000000;
	    }
	    configSpaceCacheImage = configSpaceGrid.drawImage(colorRGBs);
	}

	if(null != roadData)
	    roadRenderer = new TLRenderer(roadData);
                
    }
       
    public void emptyCache() {
	soilCacheImage = null;
	contourCacheImage = null;
	contourCacheMultiples = 0;
	obstacleCacheImage = null;
	configSpaceCacheImage = null;
	scaledSoilCacheImage = null;
	scaledContourCacheImage = null;
	scaledObstacleCacheImage = null;
	scaledConfigSpaceCacheImage = null;
        roadCacheImage = null;
        scaledRoadCacheImage = null;
        satCacheImage = null;
        scaledSatCacheImage = null;
        
	changed();
    }

    // Observer implementation
    public void update(Observable  o, Object  arg) {
	//	if(o == viewPort) {
	//	    debug.debug("Viewport changed, emptying image cache.");
	soilCacheImage = null;
	contourCacheImage = null;
	contourCacheMultiples = 0;
	obstacleCacheImage = null;
	configSpaceCacheImage = null;
	scaledSoilCacheImage = null;
	scaledContourCacheImage = null;
	scaledObstacleCacheImage = null;
	scaledConfigSpaceCacheImage = null;
        roadCacheImage = null;
        scaledRoadCacheImage = null;
        satCacheImage = null;
        scaledSatCacheImage = null;
	    //	}
	changed();
    }
    
    public void changed() {
	setChanged();
	notifyObservers(this);
    }

    private void drawSoilTypes(BufferedImage img, ViewPort viewPort) {
	// Blah, scaling code.  Should this be elsewhere?

	int[] colorRGBs = SoilTypes.getSoilColors();
	soilCacheImage = soilGrid.drawSoilImage(colorRGBs, viewPort);

	int tempWidth = soilCacheImage.getWidth(null);
	int tempHeight = soilCacheImage.getHeight(null);
	double xscale = (double)img.getWidth() / (double)tempWidth;
	double yscale = (double)img.getHeight() / (double)tempHeight;
	double scale = 0.0;
	if(xscale < yscale)
	    scale = xscale;
	else
	    scale = yscale;

	int scaledWidth = (int)(tempWidth * scale);
	int scaledHeight = (int)(tempHeight * scale);

	Image scaledTempImage = null;
	if((scaledSoilCacheImage != null)
	   && (scaledSoilCacheImage.getHeight(null) == scaledHeight)
	   && (scaledSoilCacheImage.getWidth(null) == scaledWidth)) {
		scaledTempImage = scaledSoilCacheImage;
	}
	else {	
	    //	    debug.info("                Regenerating scaled soil types image.");

	    scaledTempImage = soilCacheImage.getScaledInstance(scaledWidth,
							       scaledHeight,
							       Image.SCALE_SMOOTH);
	    //	    debug.info("                Done regenerating scaled soil types image.");
	    scaledSoilCacheImage = scaledTempImage;
	}
	
	Graphics ig = img.createGraphics();
	ig.drawImage(scaledTempImage,0,0,null);
	ig.dispose();
	ig = null;
    }

    private void drawRoads(BufferedImage img, ViewPort viewPort) {
	if(null == roadRenderer)
	    return;

	roadRenderer.buildRecords();
        debug.info("      ***Road data drawing!!!");
	long timeStart = System.currentTimeMillis();

	roadCacheImage = roadRenderer.getCenteredFitImage(img,
							  (int)viewPort.getDestWidth(),
							  (int)viewPort.getDestHeight(),
							  viewPort, 
							  soilGrid);

	//        roadCacheImage = soilGrid.drawRoads(img, roadRenderer, viewPort);
	long timeEnd = System.currentTimeMillis();
	long timeElapsed = timeEnd - timeStart;
        debug.info("      ***Done Road data drawing, elapsed="+timeElapsed);
        
	// Blah, scaling code.  Should this be elsewhere?
	int tempWidth = roadCacheImage.getWidth(null);
	int tempHeight = roadCacheImage.getHeight(null);
	double xscale = (double)img.getWidth() / (double)tempWidth;
	double yscale = (double)img.getHeight() / (double)tempHeight;
	double scale = 0.0;
	if(xscale < yscale)
	    scale = xscale;
	else
	    scale = yscale;

	int scaledWidth = (int)(tempWidth * scale);
	int scaledHeight = (int)(tempHeight * scale);

	Image scaledTempImage = null;
	if((scaledRoadCacheImage != null)
	   && (scaledRoadCacheImage.getHeight(null) == scaledHeight)
	   && (scaledRoadCacheImage.getWidth(null) == scaledWidth)) {
		scaledTempImage = scaledRoadCacheImage;
	}
	else {	
	    //	    debug.info("                Regenerating scaled soil types image.");

	    scaledTempImage = roadCacheImage.getScaledInstance(scaledWidth,
							       scaledHeight,
							       Image.SCALE_SMOOTH);
	    //	    debug.info("                Done regenerating scaled soil types image.");
	    scaledRoadCacheImage = scaledTempImage;
	}
	
	Graphics ig = img.createGraphics();
	ig.drawImage(scaledTempImage,0,0,null);
	ig.dispose();
	ig = null;
    }
    
    private void drawSatImages(BufferedImage img, ViewPort viewPort) {
	debug.info("      ***Sat Images drawing!!!");
	long timeStart = System.currentTimeMillis();
        
        //satCacheImage = Toolkit.getDefaultToolkit().getImage("/home/junyounk/test/s25000_15000.jpg");
        File f = new File ("/home/junyounk/test/s_all2.jpg");
        try {
            satCacheImage = ImageIO.read(f);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

	long timeEnd = System.currentTimeMillis();
	long timeElapsed = timeEnd - timeStart;
        debug.info("      ***Done Sat Images drawing, elapsed="+timeElapsed);
        
	// Blah, scaling code.  Should this be elsewhere?
	int tempWidth = satCacheImage.getWidth(null);
	int tempHeight = satCacheImage.getHeight(null);
	double xscale = (double)img.getWidth() / (double)tempWidth;
	double yscale = (double)img.getHeight() / (double)tempHeight;
	double scale = 0.0;
	if(xscale < yscale) // original: <
	    scale = xscale;
	else
	    scale = yscale;

	int scaledWidth = (int)(tempWidth * scale);
	int scaledHeight = (int)(tempHeight * scale);

	Image scaledTempImage = null;
	if((scaledSatCacheImage != null)
	   && (scaledSatCacheImage.getHeight(null) == scaledHeight)
	   && (scaledSatCacheImage.getWidth(null) == scaledWidth)) {
		scaledTempImage = scaledSatCacheImage;
	}
	else {	
	    //	    debug.info("                Regenerating scaled soil types image.");

	    scaledTempImage = satCacheImage.getScaledInstance(scaledWidth,
							       scaledHeight,
							       Image.SCALE_SMOOTH);            
	    //	    debug.info("                Done regenerating scaled soil types image.");
	    scaledSatCacheImage = scaledTempImage;
	}
	
	Graphics ig = img.createGraphics();          
        int leftCoord = (int) viewPort.sourceToDestX(0);
        int topCoord = (int) viewPort.sourceToDestY(0);
        ig.drawImage(scaledTempImage,leftCoord,0,null);
        //ig.drawImage(satCacheImage, 0, 0, null);
        
        /*
        File f2 = new File ("/home/junyounk/test.jpg");
        try {
            ImageIO.write ((BufferedImage)scaledTempImage, "jpeg", f2);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
         */
        
	ig.dispose();
	ig = null;
         
    }
        
    private void drawContours(BufferedImage img, int contourMultiples, ViewPort viewPort) {
	if(null == contourGrid)
	    return;
	if(null == contours)
	    return;

	if(contourMultiples != contourCacheMultiples) {
	    contourGrid.clear();
	    // TODO: This is hackish, should have some array or
	    // something in BackgroundConfig.  As it is, this is based
	    // on the options we added to the combobox in ConfigPanel.
	    int skipContours = 0;
	    if(BackgroundConfig.CONTOUR_MENU_NONE == contourMultiples) // NONE
		skipContours = BackgroundConfig.CONTOUR_MULT_NONE;
	    else if(BackgroundConfig.CONTOUR_MENU_25 == contourMultiples) // 25 meters
		skipContours = BackgroundConfig.CONTOUR_MULT_25;
	    else if(BackgroundConfig.CONTOUR_MENU_50 == contourMultiples) // 50 meters
		skipContours = BackgroundConfig.CONTOUR_MULT_50;
	    else if(BackgroundConfig.CONTOUR_MENU_100 == contourMultiples) // 100 meters
		skipContours = BackgroundConfig.CONTOUR_MULT_100;
	    else if(BackgroundConfig.CONTOUR_MENU_250 == contourMultiples) // 250 meters
		skipContours = BackgroundConfig.CONTOUR_MULT_250;
	    else if(BackgroundConfig.CONTOUR_MENU_500 == contourMultiples) // 500 meters
		skipContours = BackgroundConfig.CONTOUR_MULT_500;
	    else if(BackgroundConfig.CONTOUR_MENU_1000 == contourMultiples) // 1000 meters
		skipContours = BackgroundConfig.CONTOUR_MULT_1000;

	    if(skipContours == BackgroundConfig.CONTOUR_MULT_NONE)
		return;
	    contours.draw(skipContours, contourGrid);
	    contourCacheImage = null;
	}

	if(null == contourCacheImage) {
	    int[] colorRGBs = new int[2];
	    colorRGBs[0] = Color.black.getRGB() &0xFFFFFFFF;
	    colorRGBs[1] = SoilTypes.DEFAULT_SOIL_COLOR.getRGB() & 0x00FFFFFF;
	    contourCacheImage = contourGrid.drawImage(colorRGBs, viewPort);
	    scaledContourCacheImage = null;
	}
	contourCacheMultiples = contourMultiples;

	long startTime = System.currentTimeMillis();

	// Blah, scaling code.  Should this be elsewhere?
	int tempWidth = contourCacheImage.getWidth(null);
	int tempHeight = contourCacheImage.getHeight(null);
	double xscale = (double)img.getWidth() / (double)tempWidth;
	double yscale = (double)img.getHeight() / (double)tempHeight;
	double scale = 0.0;
	if(xscale < yscale)
	    scale = xscale;
	else
	    scale = yscale;

	int scaledWidth = (int)(tempWidth * scale);
	int scaledHeight = (int)(tempHeight * scale);

	Image scaledTempImage = null;
	if((scaledContourCacheImage != null)
	   && (scaledContourCacheImage.getHeight(null) == scaledHeight)
	   && (scaledContourCacheImage.getWidth(null) == scaledWidth)) {
		scaledTempImage = scaledContourCacheImage;
	}
	else {	
	    //	    debug.info("                Regenerating scaled contour image.");
	    scaledTempImage = contourCacheImage.getScaledInstance(scaledWidth,
								  scaledHeight,
								  Image.SCALE_SMOOTH);
	    //	    debug.info("                Done regenerating scaled contour image.");
	    scaledContourCacheImage = scaledTempImage;
	}
	debug.info("\trescaling scaledContourCacheImage, elapsed time="+(System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();

	Graphics ig = img.createGraphics();
	ig.drawImage(scaledTempImage,0,0,null);

	debug.info("\tredrawing scaledContourCacheImage, elapsed time="+(System.currentTimeMillis() - startTime));

	ig.dispose();
	ig = null;
    }

    private void drawObstacle(BufferedImage img) {
	
    	int[] colorRGBs = new int[2];
     	colorRGBs[0] = Color.red.getRGB() & 0x80FFFFFF;
     	colorRGBs[1] = Color.black.getRGB() & 0x00FFFFFF;
	if(null != obstacleGrid)
	    obstacleCacheImage = obstacleGrid.drawImage(colorRGBs, viewPort);

	// Blah, scaling code.  Should this be elsewhere?
	int tempWidth = obstacleCacheImage.getWidth(null);
	int tempHeight = obstacleCacheImage.getHeight(null);
	double xscale = (double)img.getWidth() / (double)tempWidth;
	double yscale = (double)img.getHeight() / (double)tempHeight;
	double scale = 0.0;
	if(xscale < yscale)
	    scale = xscale;
	else
	    scale = yscale;

	int scaledWidth = (int)(tempWidth * scale);
	int scaledHeight = (int)(tempHeight * scale);

	Image scaledTempImage = null;

	if((scaledObstacleCacheImage != null)
	   && (scaledObstacleCacheImage.getHeight(null) == scaledHeight)
	   && (scaledObstacleCacheImage.getWidth(null) == scaledWidth)) {
		scaledTempImage = scaledObstacleCacheImage;
	}
	else {	
	    //	    debug.info("Regenerating scaled obstacle image.");
	    scaledTempImage = obstacleCacheImage.getScaledInstance(scaledWidth,
								    scaledHeight,
								    Image.SCALE_SMOOTH);
	    //	    debug.info("Done regenerating scaled obstacle image.");
	    scaledObstacleCacheImage = scaledTempImage;
	}


	Graphics ig = img.createGraphics();
	ig.drawImage(scaledTempImage,0,0,null);
	ig.dispose();
	ig = null;
    }

    private void drawConfigSpace(BufferedImage img) {
	if(null == configSpaceCacheImage)
	    return;

	// Blah, scaling code.  Should this be elsewhere?
	int tempWidth = configSpaceCacheImage.getWidth(null);
	int tempHeight = configSpaceCacheImage.getHeight(null);
	double xscale = (double)img.getWidth() / (double)tempWidth;
	double yscale = (double)img.getHeight() / (double)tempHeight;
	double scale = 0.0;
	if(xscale < yscale)
	    scale = xscale;
	else
	    scale = yscale;

	int scaledWidth = (int)(tempWidth * scale);
	int scaledHeight = (int)(tempHeight * scale);

	Image scaledTempImage = null;

	if((scaledConfigSpaceCacheImage != null)
	   && (scaledConfigSpaceCacheImage.getHeight(null) == scaledHeight)
	   && (scaledConfigSpaceCacheImage.getWidth(null) == scaledWidth)) {
		scaledTempImage = scaledConfigSpaceCacheImage;
	}
	else {	
	    //	    debug.info("Regenerating scaled configSpace image.");
	    scaledTempImage = configSpaceCacheImage.getScaledInstance(scaledWidth,
								    scaledHeight,
								    Image.SCALE_SMOOTH);
	    //	    debug.info("Done regenerating scaled configSpace image.");
	    scaledConfigSpaceCacheImage = scaledTempImage;
	}


	Graphics ig = img.createGraphics();
	ig.drawImage(scaledTempImage,0,0,null);
	//	ig.drawImage(configSpaceCacheImage,0,0,null);
	ig.dispose();
	ig = null;
    }

    private void drawOverlay(BufferedImage img, DoubleGrid overlayGrid) {
	
    	int[] colorRGBs = new int[2];
     	colorRGBs[0] = Color.red.getRGB() & 0x80FFFFFF;
     	colorRGBs[1] = Color.black.getRGB() & 0x00FFFFFF;


	boolean showValuesWithHue = false;
	boolean showValuesWithBrightness = true;
	double defaultHue = .5;
	double defaultSaturation = .8;
	double defaultBrightness = .8;
	double contourFactor = 40.0;
	double contourGranularity = 4.0;
	int alpha = 128;
	    
	//    public synchronized Image drawImage(Graphics panelG, boolean showElevationWithHue, boolean showElevationWithBrightness, double defaultHue, double defaultSaturation, double defaultBrightness, double contourFactor, double contourGranularity, int alpha)
	if(null != overlayGrid)
	    overlayCacheImage = overlayGrid.drawImage(null, showValuesWithHue, showValuesWithBrightness, defaultHue, defaultSaturation, defaultBrightness, contourFactor, contourGranularity,alpha);

	// Blah, scaling code.  Should this be elsewhere?
	int tempWidth = overlayCacheImage.getWidth(null);
	int tempHeight = overlayCacheImage.getHeight(null);
	double xscale = (double)img.getWidth() / (double)tempWidth;
	double yscale = (double)img.getHeight() / (double)tempHeight;
	double scale = 0.0;
	if(xscale < yscale)
	    scale = xscale;
	else
	    scale = yscale;

	int scaledWidth = (int)(tempWidth * scale);
	int scaledHeight = (int)(tempHeight * scale);

	Image scaledTempImage = null;

	if((scaledOverlayCacheImage != null)
	   && (scaledOverlayCacheImage.getHeight(null) == scaledHeight)
	   && (scaledOverlayCacheImage.getWidth(null) == scaledWidth)) {
		scaledTempImage = scaledOverlayCacheImage;
	}
	else {	
	    //	    debug.info("Regenerating scaled overlay image.");
	    scaledTempImage = overlayCacheImage.getScaledInstance(scaledWidth,
								    scaledHeight,
								    Image.SCALE_SMOOTH);
	    //	    debug.info("Done regenerating scaled overlay image.");
	    scaledOverlayCacheImage = scaledTempImage;
	}


	Graphics ig = img.createGraphics();
	ig.drawImage(scaledTempImage,0,0,null);
	ig.dispose();
	ig = null;
    }

    private void drawOverlays(BufferedImage img) {
	if(null != overlayList) {
	    for(int loopi = 0; loopi < overlayList.size(); loopi++) {
		DoubleGrid overlayGrid = overlayList.get(loopi);
		drawOverlay(img, overlayGrid);
	    }
	}
    }

    public void drawBackground(BufferedImage img, BackgroundConfig config) {
	double sourceWidth = soilGrid.getWidthMeters() / soilGrid.getCellSizeMeters();
	double sourceHeight = soilGrid.getHeightMeters() / soilGrid.getCellSizeMeters();
	double xscale = (double)img.getWidth() / sourceWidth;
	double yscale = (double)img.getHeight() / sourceHeight;
	double scale = 0.0;
	if(xscale < yscale)
	    scale = xscale;
	else
	    scale = yscale;

	BufferedImage tempImg  = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
	
	Graphics ig = tempImg.createGraphics();
	ig.setColor(SoilTypes.DEFAULT_SOIL_COLOR);
	ig.fillRect(0, 0, img.getWidth(),img.getHeight());

	//	debug.info("                ----------Drawing background.----------");
	if(config.soilTypes) {
	    	    debug.info("                Drawing soil types.");
	    long startTime = System.currentTimeMillis();
	    drawSoilTypes(tempImg,config.viewPort);
	    	    debug.info("                        elapsed time="+(System.currentTimeMillis() - startTime));
	}
      	if(config.showRoads) {
	    	    debug.info("                Drawing roads.");
	    long startTime = System.currentTimeMillis();
	    drawRoads(tempImg, config.viewPort);
	    	    debug.info("                        elapsed time="+(System.currentTimeMillis() - startTime));
	}
        if(config.showSatImages) {
	    	    debug.info("                Drawing sat images.");
	    long startTime = System.currentTimeMillis();
	    if(viewPort.destToSourceX(0) < 0)
                drawSatImages(tempImg, config.viewPort);
	    	    debug.info("                        elapsed time="+(System.currentTimeMillis() - startTime));
	}
	if(config.configSpace) {
	    	    debug.info("                Drawing config space.");
	    long startTime = System.currentTimeMillis();
	    drawConfigSpace(tempImg);
	    	    debug.info("                        elapsed time="+(System.currentTimeMillis() - startTime));
	}
	if(config.voronoiGraph) {
	    	    debug.info("                Drawing voronoi graph. (not really.)");
	    long startTime = System.currentTimeMillis();
	    	    debug.info("                        elapsed time="+(System.currentTimeMillis() - startTime));
	    
	}
	if(config.elevation) {
	    	    debug.info("                Drawing elevation. (not really.)");
	    long startTime = System.currentTimeMillis();
	    	    debug.info("                        elapsed time="+(System.currentTimeMillis() - startTime));
	}
	if(config.contourMultiples > 0) {
	    	    debug.info("                Drawing contours.");
	    long startTime = System.currentTimeMillis();
	    drawContours(tempImg, config.contourMultiples, config.viewPort);
	    	    debug.info("                        elapsed time="+(System.currentTimeMillis() - startTime));
	}
	if(config.obstacles) {
	    	    debug.info("                Drawing obstacle.");
	    long startTime = System.currentTimeMillis();
	    drawObstacle(tempImg);
	    	    debug.info("                        elapsed time="+(System.currentTimeMillis() - startTime));
	}

	drawOverlays(tempImg);

	double gridLineInc = 0.0;
	if(config.gridLinesOneKm) {
	    debug.debug("setting gridLineInc to 1,000");
	    gridLineInc = 1000.0;
	}
	else if(config.gridLinesTenKm) {
	    debug.debug("setting gridLineInc to 10,000");
	    gridLineInc = 10000.0;
	}
	else {
	    debug.debug("not drawing grid lines.");
	    gridLineInc = 0;
	}

	// @todo: this is to make sure the grid lines cover a large
	// area - we rely on swing clipping to get rid of the extra
	// ones.  This really should be done in a more intelligent
	// manner using our ViewPort class.
	double localLeftX = soilGrid.toLocalX(0) - 50000;
	double localBottomY = soilGrid.toLocalY(0) + 50000;
	double localRightX = soilGrid.toLocalX(soilGrid.getWidth()-1) + 50000;
	double localTopY = soilGrid.toLocalY(soilGrid.getHeight()-1) - 50000;
	int screenLeftX = (int)config.viewPort.sourceToDestX(localLeftX);
	int screenBottomY = (int)config.viewPort.sourceToDestY(localBottomY);
	int screenRightX = (int)config.viewPort.sourceToDestX(localRightX);
	int screenTopY = (int)config.viewPort.sourceToDestY(localTopY);

	if(gridLineInc > 0) {
	    Graphics2D ig2 = ((Graphics2D)ig);
	    BasicStroke stroke1 = new BasicStroke(GRID_LINE_ONE_KM_STROKE);
	    BasicStroke stroke10 = new BasicStroke(GRID_LINE_TEN_KM_STROKE);
	    ig.setColor(GRID_LINE_COLOR);
	    int counter = 0;
	    for(double loopd = localLeftX; loopd < localRightX; loopd += gridLineInc) {
		if(counter >= 10) {
		    counter = 0;
		    ig2.setStroke(stroke10);
		}
		else
		    ig2.setStroke(stroke1);
		int foox = (int)config.viewPort.sourceToDestX(loopd);
		ig.drawLine(foox, screenBottomY, foox, screenTopY);
		counter++; 
	    }
	    counter = 0;
	    for(double loopd = localBottomY; loopd > localTopY; loopd -= gridLineInc) {
		if(counter >= 10) {
		    counter = 0;
		    ig2.setStroke(stroke10);
		}
		else
		    ig2.setStroke(stroke1);
		int fooy = (int)config.viewPort.sourceToDestY(loopd);
		ig.drawLine(screenLeftX, fooy, screenRightX, fooy);
		counter++; 
	    }
	}

	ig.dispose();
	ig = null;

	debug.info("                drawing tempimg to img.");
	long startTime = System.currentTimeMillis();
	ig = img.createGraphics();
	ig.drawImage(tempImg,0,0,null);
	ig.dispose();
	ig = null;
	debug.info("                        elapsed time="+(System.currentTimeMillis() - startTime));

	//	debug.info("                ----------Done drawing background.----------");
	
    }

}

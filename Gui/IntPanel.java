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

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.awt.event.*;

    
public class IntPanel extends JPanel implements Observer {
    private final static Color GRID_LINE_COLOR = Color.white;

    public final static int COLOR_TYPE_BANDS = 0;
    public final static int COLOR_TYPE_BRIGHTNESS = 1;
    public final static int COLOR_TYPE_HUE = 2;

    private final static float DEFAULT_HUE = 0.5f;
    private final static float DEFAULT_SATURATION = 0.8f;
    private final static float DEFAULT_BRIGHTNESS = 0.8f;

    private boolean displayOneKmGrid = false;
    private boolean displayTenKmGrid = false;
    private IntGrid grid = null;
    private DebugInterface debug = null;
    private BufferedImage img = null;
    private boolean imageInvalid = false;
    public void setImageInvalid(boolean val) { imageInvalid = val;}
    private int colorRGBs[] = null;

    public void update(Observable  o, Object  arg)
    {
	imageInvalid = true;
	repaint();
    }
    
    private void initColors(int colorType, int numColors, double startValue) {
	if(numColors <= 0) { 
	    grid.computeHighestLowest();
	    numColors = grid.getHighest() + 1;
	    if((grid.getHighest()+1) < 0) {
		debug.error("The IntGrid we're trying to display has a 'highest+1' value of < 0 - either this grid has no positive numbers, or (more likely) has a cell set to Integer.MAX_VALUE.  Creating a color array size 100.");
		numColors = 100;
	    }
	    else if(numColors > 200) {
		debug.error("The IntGrid we're trying to display has a highest of > 200, limit colors to 0 to 199.");
		numColors = 200;
	    }
	}

	double range = 1.0 - startValue;
	if(COLOR_TYPE_BRIGHTNESS == colorType) {
	    colorRGBs = new int[numColors];
	    for(int loopi = 0; loopi < colorRGBs.length; loopi++) {
		double percentage = ((double)loopi)/(double)colorRGBs.length;
		float brightness = (float)((percentage * range) + startValue);
		colorRGBs[loopi] = Color.getHSBColor(DEFAULT_HUE, DEFAULT_SATURATION, brightness).getRGB();
	    }
	}
	else if(COLOR_TYPE_HUE == colorType) {
	    colorRGBs = new int[numColors];
	    for(int loopi = 0; loopi < colorRGBs.length; loopi++) {
		double percentage = ((double)loopi)/(double)colorRGBs.length;
		float hue = (float)((percentage * range) + startValue);
		colorRGBs[loopi] = (Color.getHSBColor(hue, DEFAULT_SATURATION, DEFAULT_BRIGHTNESS)).getRGB();
	    }
	}
	else if(COLOR_TYPE_BANDS == colorType) {

	    colorRGBs = new int[numColors + 1];

	    //    	colorRGBs = new int[8];
	    //     	colorRGBs[0] = Color.white.getRGB();
	    //     	colorRGBs[1] = Color.yellow.getRGB();
	    //     	colorRGBs[2] = Color.orange.getRGB();
	    //     	colorRGBs[3] = Color.green.getRGB();
	    //     	colorRGBs[4] = Color.magenta.getRGB();
	    //     	colorRGBs[5] = Color.blue.getRGB();
	    //     	colorRGBs[6] = Color.gray.getRGB();
	    //     	colorRGBs[7] = Color.black.getRGB();

	    colorRGBs = new int[300];
	    colorRGBs[ 0] = Color.red.getRGB();
	    colorRGBs[ 1] = Color.white.getRGB();
	    for(int loopi = 2; loopi < 6; loopi++) 
		colorRGBs[loopi] = Color.green.getRGB();
	    for(int loopi = 6; loopi < 10; loopi++) 
		colorRGBs[ 6] = Color.magenta.getRGB();
	    for(int loopi = 10; loopi < 25; loopi++) 
		colorRGBs[loopi] = Color.blue.getRGB();
	    for(int loopi = 26; loopi < 72; loopi++) 
		colorRGBs[loopi] = Color.gray.getRGB();
	    for(int loopi = 72; loopi < 75; loopi++) 
		colorRGBs[72] = Color.black.getRGB();
	    for(int loopi = 75; loopi < 100; loopi++) 
		colorRGBs[loopi] = Color.black.getRGB();
	    for(int loopi = 100; loopi < 125; loopi++) 
		colorRGBs[loopi] = Color.blue.getRGB();
	    for(int loopi = 125; loopi < 150; loopi++) 
		colorRGBs[loopi] = Color.cyan.getRGB();
	    for(int loopi = 150; loopi < 175; loopi++) 
		colorRGBs[loopi] = Color.pink.getRGB();
	    for(int loopi = 175; loopi < 200; loopi++) 
		colorRGBs[loopi] = Color.yellow.getRGB();
	    for(int loopi = 200; loopi < 300; loopi++) 
		colorRGBs[loopi] = Color.orange.getRGB();
	} 
	
    }

    // Constructor for int panel, using these colors  to map integer values to colors.
    public IntPanel(IntGrid grid, int[] colorRGBs) {
	debug = new DebugFacade(this);
	this.grid = grid;
	this.colorRGBs = colorRGBs;
	grid.addObserver(this);
    }

    // Constructor for int panel, use a range of brightness from 0 to
    // 1.0, covering the range of values in the IntGrid.
    public IntPanel(IntGrid grid) {
	debug = new DebugFacade(this);
	this.grid = grid;
	initColors(COLOR_TYPE_BRIGHTNESS, 0, 0.0);
	grid.addObserver(this);
    }

    // Constructor for int panel, using the colorType specified (see
    // constants above), with default values.
    public IntPanel(IntGrid grid, int colorType) {
	debug = new DebugFacade(this);
	this.grid = grid;
	initColors(colorType, 0, 0.0);
	grid.addObserver(this);
    }

    // Constructor for int panel, using the colorType specified (see
    // constants above), with the number of colors specified.  (Note,
    // all grid values higher than numColors will be mapped to the
    // highest color in the color array.)
    public IntPanel(IntGrid grid, int colorType, int numColors) {
	debug = new DebugFacade(this);
	this.grid = grid;
	initColors(colorType, numColors, 0.0);
	grid.addObserver(this);
    }

    // Constructor for int panel, using the colorType specified (see
    // constants above), with starting value specified.
    public IntPanel(IntGrid grid, int colorType, double startValue) {
	debug = new DebugFacade(this);
	this.grid = grid;
	initColors(colorType, 0, startValue);
	grid.addObserver(this);
    }

    // Constructor for int panel, using the colorType specified (see
    // constants above), with the number of colors specified.  (Note,
    // all grid values higher than numColors will be mapped to the
    // highest color in the color array.)  Start lowest/darkest color
    // at startValue.
    public IntPanel(IntGrid grid, int colorType, int numColors, double startValue) {
	debug = new DebugFacade(this);
	this.grid = grid;
	initColors(colorType, numColors, startValue);
	grid.addObserver(this);
    }

    public void draw(BufferedImage drawingImg) {
	Graphics drawingGraphics = drawingImg.createGraphics();
	int drawingWidth  = drawingImg.getWidth();
	int drawingHeight = drawingImg.getHeight();
	Image tempImage = grid.drawImage(colorRGBs);

	// Blah, scaling code.  Should this be elsewhere?
	int tempWidth = tempImage.getWidth(null);
	int tempHeight = tempImage.getHeight(null);
	double xscale = (double)drawingImg.getWidth() / (double)tempWidth;
	double yscale = (double)drawingImg.getHeight() / (double)tempHeight;
	double scale = 0.0;
	if(xscale < yscale)
	    scale = xscale;
	else
	    scale = yscale;
	//	    debug.info("                unscaled image is "+tempWidth+" wide by "+tempHeight+" high, on screen image is "+d.width+" by "+d.height+", xscale="+xscale+", yscale="+yscale+", final scale="+scale);
	Image scaledTempImage = tempImage.getScaledInstance((int)(tempWidth * scale),
							    (int)(tempHeight * scale),
							    Image.SCALE_AREA_AVERAGING);
 	drawingGraphics.drawImage(scaledTempImage,0,0,null);

	// Draw 1 km and 10 km grid.
	double imageSize = tempWidth * scale;
	double gridLineInc = (1000.0/(((double)grid.getWidthMeters())/imageSize));
	int counter = 0;
	float strokeSize = 1.0f;
	drawingGraphics.setColor(GRID_LINE_COLOR);
	for(double loopd = 0; loopd < drawingWidth; loopd += gridLineInc) {
	    if(counter >= 10)
		counter = 0;
	    if((counter != 0) && (grid.getWidthMeters() > 30000))
		continue;
	    strokeSize = (counter == 0) ? 3.0f : 1.0f;
	    ((Graphics2D)drawingGraphics).setStroke(new BasicStroke(strokeSize));
	    drawingGraphics.drawLine((int)loopd, 0, (int)loopd, drawingHeight - 1);
	    drawingGraphics.drawLine(0,(int)loopd, drawingWidth - 1, (int)loopd);
	    counter++; 
	}

	drawingGraphics.dispose();
	drawingGraphics = null;
	imageInvalid = false;
    }

    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	Dimension d = getSize();
	if(imageInvalid || (null == img) || (d.width != img.getWidth()) || (d.height != img.getHeight())) {
	    img = ((Graphics2D)g).getDeviceConfiguration().createCompatibleImage(d.width, d.height);

	    draw(img);
	}
	g.drawImage(img,0,0,null);
    }
    
}

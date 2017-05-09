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

public class DoublePanel extends JPanel implements Observer {
    private DoubleGrid grid = null;
    private DebugInterface debug = null;
    private BufferedImage img = null;
    private boolean imageInvalid = false;

    private boolean showValuesWithHue = false;
    private boolean showValuesWithBrightness = true;
    private double defaultHue = .5;
    private double defaultSaturation = .8;
    private double defaultBrightness = .8;
    private double contourFactor = 40.0;
    private double contourGranularity = 4.0;

    public DoublePanel(DoubleGrid grid, boolean showValuesWithHue, boolean showValuesWithBrightness) {
	debug = new DebugFacade(this);
	this.grid = grid;
	this.showValuesWithHue = showValuesWithHue;
	this.showValuesWithBrightness = showValuesWithBrightness;
	grid.addObserver(this);
    }

    public void update(Observable  o, Object  arg)
    {
	imageInvalid = true;
	repaint();
    }

    public void draw(BufferedImage drawingImg) {
	Graphics drawingGraphics = drawingImg.createGraphics();
	int drawingWidth  = drawingImg.getWidth();
	int drawingHeight = drawingImg.getHeight();

	Image tempImage = grid.drawImage(drawingGraphics, showValuesWithHue, showValuesWithBrightness, defaultHue, defaultSaturation, defaultBrightness, contourFactor, contourGranularity);
	if(null == tempImage) {
	    debug.error("Why did DoubleGrid.drawImage return null?");
	}
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
	debug.info("unscaled image is "+tempWidth+" wide by "+tempHeight+" high, on screen image is "+drawingWidth+" by "+drawingHeight+", xscale="+xscale+", yscale="+yscale+", final scale="+scale);
	Image scaledTempImage = tempImage.getScaledInstance((int)(tempWidth * scale),
							    (int)(tempHeight * scale),
							    Image.SCALE_AREA_AVERAGING);
	drawingGraphics.drawImage(scaledTempImage,0,0,null);

	// Draw 1 km and 10 km grid.
	double imageSize = tempWidth * scale;
	double gridLineInc = (1000.0/(((double)grid.getWidthMeters())/imageSize));
	int counter = 0;
	float strokeSize = 1.0f;
	drawingGraphics.setColor(Color.white);
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

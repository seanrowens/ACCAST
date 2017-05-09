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
// @version     $Id: ViewPort.java,v 1.4 2007/11/29 21:10:24 junyounk Exp $ 

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

// SRO Wed Apr  5 17:21:53 EDT 2006
//
// This class translates coordinates from a source coordinate system
// to a destination coordinate system - only its not really general
// enough.  The idea here is to have one place where we control what
// part of a map/grid is displayed in a panel, as well as the drawing from the 
public class ViewPort extends Observable {
    private DebugInterface debug = null;
    
    private double localSrcLeftX = 0.0;
    private double localSrcBottomY = 0.0;
    private double localSrcWidth = 0.0;
    public double getSourceWidth() { return localSrcWidth; }
    private double localSrcHeight = 0.0;
    public double getSourceHeight() { return localSrcHeight; }
    public double getLocalSrcLeftX() { return localSrcLeftX; }
    public double getLocalSrcBottomY() { return localSrcBottomY; }

    private double destX = 0.0;
    private double destY = 0.0;
    private double destWidth = 0.0;
    public double getDestWidth() { return destWidth; }
    private double destHeight = 0.0;
    public double getDestHeight() { return destHeight; }
    public double getDestX() { return destX; }
    public double getDestY() { return destY; }
    
    // These are the 'requested' source coordinates - when a request
    // is made, it is automatically centered and expanded to fill the
    // dest panel.
    private double localReqSrcLeftX = 0.0;
    private double localReqSrcBottomY = 0.0;
    private double localReqSrcWidth = 0.0;
    public double getReqSrcWidth() { return localReqSrcWidth; }
    private double localReqSrcHeight = 0.0;
    public double getReqSrcHeight() { return localReqSrcHeight; }

    private double scale = 0.0;
    public double getScale() { return scale;}
    private double initScale = 0.0;
    public double getInitScale() { return initScale; }
    public void setInitScale(double initScale) { this.initScale = initScale; }

    private IntGrid intGrid = null;
    public IntGrid getIntGrid() { return intGrid; }
    
    public void changed() {
	setChanged();
	notifyObservers(this);
    }

    private void calcScale() {
	double xscale = destWidth / localSrcWidth;
	double yscale = destHeight / localSrcHeight;
	if(xscale < yscale)
	    scale = xscale;
	else
	    scale = yscale;
    }

    public ViewPort(double localSrcLeftX, double localSrcBottomY,
		    double localSrcWidth, double localSrcHeight,
		    double destWidth, double destHeight) {
	debug = new DebugFacade(this);
	this.localSrcLeftX = localSrcLeftX;
	this.localSrcBottomY = localSrcBottomY;
	this.localSrcWidth = localSrcWidth;
	this.localSrcHeight = localSrcHeight;
	this.destWidth = destWidth;
	this.destHeight = destHeight;
	calcScale();
   }

    public ViewPort(IntGrid grid) {
        this.intGrid = intGrid;
	debug = new DebugFacade(this);
	this.localSrcLeftX = grid.getBottomLeftX();
	this.localSrcBottomY = grid.getBottomLeftY();
	this.localSrcWidth = grid.getWidthMeters();
	this.localSrcHeight = grid.getHeightMeters();
	this.destWidth = 1000.0;
	this.destHeight = 1000.0;
	calcScale();
   }

    // @todo need to add code here so when the panel is resized, and
    // we call setDest, we recalculate the source based on the
    // REQUESTED source.
    public void setAll(double localSrcLeftX, double localSrcBottomY,
		       double localSrcWidth, double localSrcHeight,
		       double destWidth, double destHeight) {
	this.localSrcLeftX = localSrcLeftX;
	this.localSrcBottomY = localSrcBottomY;
	this.localSrcWidth = localSrcWidth;
	this.localSrcHeight = localSrcHeight;
	this.destWidth = destWidth;
	this.destHeight = destHeight;
	calcScale();
    }

    public void setSource(double localSrcLeftX, double localSrcBottomY,
			  double localSrcWidth, double localSrcHeight) {
	this.localSrcLeftX = localSrcLeftX;
	this.localSrcBottomY = localSrcBottomY;
	this.localSrcWidth = localSrcWidth;
	this.localSrcHeight = localSrcHeight;
	calcScale();
    }

    // @todo need to add code here so when the panel is resized, and
    // we call setDest, we recalculate the source based on the
    // REQUESTED source.
    public void setDest(double destWidth, double destHeight) {
	if((destWidth != this.destWidth) || (destHeight != this.destHeight)) {
	    this.destWidth = destWidth;
	    this.destHeight = destHeight;
	    calcScale();
	    calcSourceView();
	}
    }

    public double sourceToDestX(double localSrcX) {
	return (localSrcX - localSrcLeftX) * scale;
    }

    public double sourceToDestY(double localSrcY) {
	return (localSrcHeight - (localSrcY - localSrcBottomY)) * scale;
    }

    public double destToSourceX(double destX) {
	return (destX / scale) + localSrcLeftX;
    }

    public double destToSourceY(double destY) {
	return (localSrcHeight - (destY / scale)) + localSrcBottomY;
    }
    
    public double destToSourceWidth(double width) {
        return width/scale;
    }
    
    
    // Ok, this is going to get interesting...
    //
    // viewPort has our source lower left corner position, and
    // height/width, and also our dest (screen) width and height.
    // I think this is in local coords. (@todo verify that
    // viewport works with local coords)
    //
    // (Theoretically we also have the dest lower left corner, but
    // so far its always been in its own panel/image so it has
    // always been 0,0.)
    //
    // For the sake of argument, let us specify that source
    // (width, height) is (4000, 5000) in local coords. (Which
    // amounts to 100 x 100 grid cells for a 50 meter
    // gridcellsize).  And also that lower left corner is at
    // (1000, 2000).
    // 
    // Let us further specify that the dest (width, height) is
    // (800, 600) pixels.
    // 
    // So first of all lets calculate the center of the source
    // we're going to draw, since we'll need that later.  x =
    // (1000 + 4000/2) = 3000, and y = (2000 + 5000/2) = 4500, so
    // our center is at 3000, 4500.
    // 
    // We know the dest width/height, we want to maintain aspect
    // ratio.  So we first compare the ratio of dest width to
    // source width (800/4000 = 0.20), and dest height to source
    // height (600/5000 = 0.12), and scale by the smaller of the
    // ratios (0.12).  We do this so we can fit everything
    // specified into the screen from the smaller ratio.  OK, so
    // we're going to scale by 0.12, so 0.12 * 4000 = 480, and .12
    // * 5000 = 600, so our final image is going to be size
    // 480x600.
    //
    // Now obviously 480x600 is not going to fill an 800x600
    // panel, so we have to center that and expand it to the edges
    // of the panel.  So, 800/.12 = 6667 and 600/.12 = 5000, so
    // the rectangle of the source we're drawing is (6667, 5000),
    // centered on the original center (told you we'd need this
    // later) which is (3000, 4500).
    // 
    // So, the extents of our source rectangle are;
    // x1 = 3000 - (6667/2) = -333.5000
    // x2 = 3000 + (6667/2) = 6333.5000
    // y1 = 4500 - (5000/2) = 2000.0000
    // y2 = 4500 + (5000/2) = 7000.0000
    //
    // So we're drawing a rectangle of source from lower left
    // corner (-333.5, 2000) to upper right corner (6333.5, 7000).
    // Don't worry just yet that we're into the negative x on the
    // left.
    // 
    // So now all we really need to do is loop over the dest,
    // grabbing color data from the corresponding source points.
    // Sounds too easy.  Well, I guess we'll see.
    public void calcSourceView() {
	synchronized(this) {
	    debug.debug("calcSourceView: requested corner="+localReqSrcLeftX+", "+localReqSrcBottomY+", size = "+localReqSrcWidth+", "+localReqSrcHeight);

	    double localReqSrcCenterX = localReqSrcLeftX + localReqSrcWidth/2;
	    double localReqSrcCenterY = localReqSrcBottomY + localReqSrcHeight/2;
	    debug.debug("calcSourceView: requested center="+localReqSrcCenterX+", "+localReqSrcCenterY);

	    debug.debug("calcSourceView: dest corner="+destX+", "+destY+", size = "+destWidth+", "+destHeight);

	    double reqWidthRatio = destWidth / localReqSrcWidth;
	    double reqHeightRatio = destHeight / localReqSrcHeight;
	    double reqRatio;
	    if(reqWidthRatio < reqHeightRatio) 
		reqRatio = reqWidthRatio;
	    else
		reqRatio = reqHeightRatio;
	    debug.debug("calcSourceView: widthRatio="+reqWidthRatio+", heightRatio="+reqHeightRatio+", using ratio="+reqRatio);

	    localSrcWidth = destWidth / reqRatio;
	    debug.debug("calcSourceView: localSrcWidth = destWidth / reqRatio ; "+localSrcWidth+" = "+destWidth+" / "+reqRatio);
	    localSrcHeight = destHeight / reqRatio;
	    debug.debug("calcSourceView: localSrcHeight = destHeight / reqRatio ; "+localSrcHeight+" = "+destHeight+" / "+reqRatio);
	    localSrcLeftX = localReqSrcCenterX - localSrcWidth/2;
	    debug.debug("calcSourceView: localSrcLeftX = localReqSrcCenterX - localSrcWidth/2 - "+localSrcLeftX+" = "+localReqSrcCenterX+" - "+localSrcWidth/2);
	    localSrcBottomY = localReqSrcCenterY - localSrcHeight/2;
	    debug.debug("calcSourceView: localSrcBottomY = localReqSrcCenterY - localSrcHeight/2 ; "+localSrcBottomY+" = "+localReqSrcCenterY+" - "+localSrcHeight/2);
	    debug.debug("calcSourceView: actual corner="+localSrcLeftX+", "+localSrcBottomY+", size = "+localSrcWidth+", "+localSrcHeight);

	    calcScale();
	    changed();
	}
    }

    // Given a requested 'box' to view, fit it to the screen.  It is
    // important to note that the actual viewable space will be
    // somewhat larger than requested, depending primarily on the size
    // of the destination panel.  For instance, if the box requested
    // is square (equal height and width) and the width of the dest
    // panel is larger than the height of the dest panel, the
    // resulting source view will be the same as requested, but the
    // width will be somewhat larger to fit in the panel.
    //
    // @param localSrcX1	the X coordinate (in local coords) of the first corner
    // @param localSrcY1	the Y coordinate (in local coords) of the first corner
    // @param localSrcX2	the X coordinate (in local coords) of the second corner
    // @param localSrcY2	the Y coordinate (in local coords) of the second corner
    public void requestSourceView(double localSrcX1, double localSrcY1, double localSrcX2, double localSrcY2) {
	
	// First convert that into lower left corner and width,height
	if(localSrcX1 > localSrcX2) {
	    double temp = localSrcX2;
	    localSrcX2 = localSrcX1;
	    localSrcX1 = temp;
	}
	if(localSrcY1 > localSrcY2) {
	    double temp = localSrcY2;
	    localSrcY2 = localSrcY1;
	    localSrcY1 = temp;
	}
	synchronized(this) {
	    localReqSrcWidth = localSrcX2 - localSrcX1;
	    localReqSrcHeight = localSrcY2 - localSrcY1;
	    localReqSrcLeftX = localSrcX1;
	    localReqSrcBottomY = localSrcY1;
	}
	calcSourceView();
    }

    public void requestSourceViewFitToScreen(IntGrid grid) {
	debug.debug("requestSourceViewFitToScreen: requesting fit to screen.");
	requestSourceView(grid.getBottomLeftX(),
			  grid.getBottomLeftY(),
			  grid.getBottomLeftX()+grid.getWidthMeters(),
			  grid.getBottomLeftY()+grid.getHeightMeters());
    }

    public void requestZoomOut() {
	synchronized(this) {
	    localReqSrcLeftX -= localReqSrcWidth/2;
	    localReqSrcBottomY -= localReqSrcHeight/2;
	    localReqSrcHeight *= 2;
	    localReqSrcWidth *= 2;
	}
	calcSourceView();
    }
    
    // OK, this is the transform from Geo Centric Coordinates to Topo
    // Centric Coordinates - but it is not enough.  Even after we do
    // this, in order to draw it we need to flip it (i.e. it will in a
    // coordinate system that assumes the lower left is 0, and that as
    // you go up the screen y values go up, whereas java believes the
    // upper left is 0 and that as you go down the screen the y values
    // go up), and also scale it to the screen.  Also note that this
    // will only work when the lower left of the map IS zero, zero,
    // i.e. it assumes we haven't cut a chunk out of the map.
    //
    // Also note that this particular method throws away the z value,
    // since we don't need it since we're drawing on screen.  Probably
    // not the best way to do it.
    // @deprecated I'm not sure this ever worked, or if anything still uses it.
    public Point2D.Double transformGccToTcc(double[] point) {
	double[] pointArray = new double[4];
	double[] resultArray = new double[4];
	Point2D.Double result = new Point2D.Double();
	pointArray[0] = point[0];
	pointArray[1] = point[1];
	pointArray[2] = point[2];
	pointArray[3] = 1.0;
	multiply(pointArray, gccToTccTransform, resultArray);
	//	debug.info("Transformed gcc="+point[0]+", "+point[1]+", "+point[2]+" to "+resultArray[0]+", "+resultArray[1]+", "+resultArray[2]+", "+resultArray[3]);
	result.x = resultArray[0];
	result.y = resultArray[1];
	return result;
    }


    // @deprecated I'm not sure this ever worked, or if anything still uses it.
    private double[][] gccToTccTransform = new double[4][4]; 
    // @deprecated I'm not sure this ever worked, or if anything still uses it.
    private double[][] tccToGccTransform = new double[4][4]; 
    // @deprecated I'm not sure this ever worked, or if anything still uses it.
    public void setTccToGccTransform(double[][] tccToGccTransform) {
	this.tccToGccTransform = tccToGccTransform;
	// this is tcc2gcc, gcc2tcc is the transpose of tcc2gcc
	for(int loopy = 0; loopy <= 3; loopy++) 
	    for(int loopx = 0; loopx <= 3; loopx++)
		this.gccToTccTransform[loopy][loopx] = this.tccToGccTransform[loopx][loopy];
    }
    
    // This is used by IntGrid so we can test our transformation
    // process on the sample points we load from the file.
    // @deprecated I'm not sure this ever worked, or if anything still uses it.
    public static void transform(double point[], double transform[][], double result[]) {
	result[0] = (point[0] * transform[0][0])
	    + (point[1] * transform[1][0])
	    + (point[2] * transform[2][0])
	    + (point[3] * transform[3][0]);

	result[1] = (point[0] * transform[0][1])
	    + (point[1] * transform[1][1])
	    + (point[2] * transform[2][1])
	    + (point[3] * transform[3][1]);

	result[2] = (point[0] * transform[0][2])
	    + (point[1] * transform[1][2])
	    + (point[2] * transform[2][2])
	    + (point[3] * transform[3][2]);

	result[3] = (point[0] * transform[0][3])
	    + (point[1] * transform[1][3])
	    + (point[2] * transform[2][3])
	    + (point[3] * transform[3][3]);
    }


    // @deprecated I'm not sure this ever worked, or if anything still uses it.
    private void multiply(double point[], double transform[][], double result[]) {
	result[0] = (point[0] * transform[0][0])
	    + (point[1] * transform[1][0])
	    + (point[2] * transform[2][0])
	    + (point[3] * transform[3][0]);

	result[1] = (point[0] * transform[0][1])
	    + (point[1] * transform[1][1])
	    + (point[2] * transform[2][1])
	    + (point[3] * transform[3][1]);

	result[2] = (point[0] * transform[0][2])
	    + (point[1] * transform[1][2])
	    + (point[2] * transform[2][2])
	    + (point[3] * transform[3][2]);

	result[3] = (point[0] * transform[0][3])
	    + (point[1] * transform[1][3])
	    + (point[2] * transform[2][3])
	    + (point[3] * transform[3][3]);
    }
}

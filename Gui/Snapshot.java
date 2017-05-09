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
// @version     $Id: Snapshot.java,v 1.2 2006/07/10 21:54:22 owens Exp $ 

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.imageio.stream.*;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;


import java.util.*;
import java.awt.Point;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;




public class Snapshot {
    private DebugInterface debug = null;    
    private int sizex = 0;
    private int sizey = 0;
    private BufferedImage img = null;

    private float compressionQuality = 0.90f;

    public Snapshot(int sizex, int sizey) {
	debug = new DebugFacade(this);
	this.sizex = sizex;
	this.sizey = sizey;
    }

    public void snap(String filename, JComponent component) {
	img = new BufferedImage(sizex, sizey, BufferedImage.TYPE_INT_ARGB);
	// TODO: Should create some kind of 'snapshotable' interface.
// 	if(component instanceof JComponent) {
// 	    debug.debug("snapshotting jcomponent.");
// 	    Graphics drawingGraphics = img.createGraphics();
// 	    component.paintAll(drawingGraphics);
// 	}
// 	else 
//        if(component instanceof DoublePanel)
// 	    ((DoublePanel)component).draw(img);
// 	else if(component instanceof IntPanel)
// 	    ((IntPanel)component).draw(img);
// 	else if(component instanceof VectorPanel)
// 	    ((VectorPanel)component).draw(img);
// 	else if(component instanceof ResistorPanel)
// 	    ((ResistorPanel)component).draw(img);
// 	else
	    if(component instanceof TerrainCanvas)
	    ((TerrainCanvas)component).draw(img);
// 	else if(component instanceof ImagePanel)
// 	    ((ImagePanel)component).draw(img);

	try {
	    File outfile = new File(filename+".png");
	    ImageIO.write(img, "png", outfile);

// 	    outfile = new File(filename+".jpg");
// 	    ImageIO.write(img, "jpg", outfile);


	}
	catch(IOException e) {
	    debug.error("Exception trying to write image to "+filename+", e="+e);
	    e.printStackTrace();
	}
	img.flush();
	img = null;
    }
}

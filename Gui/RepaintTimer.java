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
// @version     $Id: RepaintTimer.java,v 1.2 2006/07/10 21:54:22 owens Exp $ 

import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

public class RepaintTimer implements Runnable {
    private JPanel panel = null;
    private MapDB mapDB = null;
    private long repaintPeriodMs = 0;
    private long lastRepaintTime = 0;
    private DebugInterface debug = null;

    private Thread myThread = null;
    private boolean keepRunning = true;

    public RepaintTimer(JPanel panel, MapDB mapDB, long repaintPeriodMs) {
	this.panel = panel;
	this.mapDB = mapDB;
	this.repaintPeriodMs = repaintPeriodMs;
	debug = new DebugFacade(this);
    }

    public void run() {
	
	long lastRepaint = System.currentTimeMillis();
	long timeLeft = repaintPeriodMs;
	while(keepRunning) {
	    try {
		Thread.sleep(timeLeft);
	    } catch (InterruptedException e) {
		break;
	    }
	    long timeNow = System.currentTimeMillis();
	    if((timeNow - lastRepaintTime)  > repaintPeriodMs) {
		if(mapDB.getDirty()) {
		    //		    debug.debug("Issuing a repaint after "+(timeNow - lastRepaint)+" at time="+timeNow);
		    panel.repaint();
		    mapDB.setDirty(false);
		    lastRepaint = timeNow;
		}
		else {
		    //		    debug.debug("Not issuing a repaint at time="+timeNow);
		}
		timeLeft = repaintPeriodMs;
	    }
	    else {
		timeLeft = repaintPeriodMs - (timeNow - lastRepaintTime);
	    }
	}
    }


    public void start() {
	keepRunning = true;
	myThread = new Thread(this);
	myThread.setPriority(Thread.MAX_PRIORITY);
	myThread.start();
    }

    public void stop() {
	keepRunning = false;
	myThread = null;
    }

}

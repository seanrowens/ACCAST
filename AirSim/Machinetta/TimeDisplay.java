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
 * TimeDisplay.java
 *
 *
 */

package AirSim.Machinetta;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import java.text.DecimalFormat;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 *
 * @author owens
 */
public class TimeDisplay {

    private static DecimalFormat fmt = new DecimalFormat("#,###");
    private static DateFormat dfm1 = new SimpleDateFormat("yyyy-MM-dd");
    private static DateFormat dfm2 = new SimpleDateFormat("HH:mm:ss.SSS");

    private static long UPDATE_INTERVAL_MS = 25;
    private static long SLEEP_BETWEEN_UPDATE_MS = 5;
    private static int FONT_SIZE = (int)(36 * 2.5);
    private static JFrame topFrame = null;
    private static JPanel topPanel = null;
    private static JLabel unixTimeLabel = null;
    private static JLabel dateLabel = null;
    private static JLabel hmsSLabel = null;

    public static void main(String argv[]) {
	topFrame = new JFrame("TIME");
	topFrame.addWindowListener(new WindowAdapter(){
		public void windowClosing(WindowEvent we){
		    topFrame.setVisible(false);
		    topFrame.dispose();
		    System.exit(0);
		}
	    });
	topFrame.setLocation(0,23);
	topFrame.setSize(800,600);
	topPanel = new JPanel();

	unixTimeLabel = new JLabel("");
	unixTimeLabel.setFont(new Font("TimeDisplay", Font.BOLD, FONT_SIZE));
	topPanel.add(unixTimeLabel);

	dateLabel = new JLabel("");
	dateLabel.setFont(new Font("TimeDisplay", Font.BOLD, FONT_SIZE));
	topPanel.add(dateLabel);

	hmsSLabel = new JLabel("");
	hmsSLabel.setFont(new Font("TimeDisplay", Font.BOLD, FONT_SIZE));
	topPanel.add(hmsSLabel);

	topFrame.getContentPane().add(topPanel);
	topFrame.show();

	long now = 0;
	Date nowDate = new Date();
	long nextUpdateTimeMs = 0;
	long lastUpdateTimeMs = 0;
	long diff=0;
	int loopIterationsBetweenUpdates = 0;
	now = System.currentTimeMillis();
	nextUpdateTimeMs = now + UPDATE_INTERVAL_MS;
	lastUpdateTimeMs = now;
	while(true) {
	    loopIterationsBetweenUpdates++;
	    now = System.currentTimeMillis();
	    if(now >= nextUpdateTimeMs) {
		diff  = now - lastUpdateTimeMs;
		//		System.err.println("Now= "+now+" diff="+diff+" loopIterationsBetweenUpdates="+loopIterationsBetweenUpdates);
		nextUpdateTimeMs += UPDATE_INTERVAL_MS;
		lastUpdateTimeMs = now;
		loopIterationsBetweenUpdates = 0;

		nowDate.setTime(now);
		final String dateString = dfm1.format(nowDate);
		final String hmsSString = dfm2.format(nowDate);
		final long fnow = now;
                try {
                    EventQueue.invokeAndWait(new Runnable() {
			    public void run() {
				unixTimeLabel.setText(fmt.format(fnow));
				dateLabel.setText(dateString);
				hmsSLabel.setText(hmsSString);
				topFrame.repaint();
			    }
			}
					     );
                } catch(InterruptedException e) {
                    System.err.println("Update of time panel data was interrupted e="+e);
                    e.printStackTrace();
                } catch(java.lang.reflect.InvocationTargetException e) {
                    System.err.println("e="+e);
                    e.printStackTrace();
                }
		
	    }
	    try { Thread.sleep(SLEEP_BETWEEN_UPDATE_MS); } catch(InterruptedException e) {}
	}
    }
}

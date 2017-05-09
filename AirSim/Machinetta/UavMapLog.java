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
package AirSim.Machinetta;

import AirSim.Environment.Waypoint;
import Gui.StringUtils;
import Gui.LatLonUtil;
import Util.CSV;
import java.awt.geom.Point2D;

import java.awt.image.*;
import java.awt.event.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.util.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.awt.Point;
import java.awt.geom.Point2D;
import javax.swing.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import javax.imageio.*;

public class UavMapLog {

    private FileOutputStream logStream = null;
    private PrintStream logPrint = null;

    // Given a time, get a string representing the current time in
    // format YYYY_MM_DD_HH_MM_SS, i.e. 2005_02_18_23_16_24.  Using
    // this format results in timestamp strings (or filenames) that
    // will sort to date order.  If 'time' is null it will be
    // filled in with the current time.
    private static String getTimeStamp(Calendar time) {
        String timestamp = "";
        String part = null;
        if(null == time)
            time = Calendar.getInstance();
        
        timestamp = Integer.toString(time.get(time.YEAR));
        
        part = Integer.toString((time.get(time.MONTH)+1));
        if(1 == part.length())
            part = "0"+part;
        timestamp += part;
        
        part = Integer.toString(time.get(time.DAY_OF_MONTH));
        if(1 == part.length())
            part = "0"+part;
        timestamp += part;
        
        part = Integer.toString(time.get(time.HOUR_OF_DAY));
        if(1 == part.length())
            part = "0"+part;
        timestamp += part;
        
        part = Integer.toString(time.get(time.MINUTE));
        if(1 == part.length())
            part = "0"+part;
        timestamp += part;
        
        part = Integer.toString(time.get(time.SECOND));
        if(1 == part.length())
            part = "0"+part;
        timestamp += part;
        
        return timestamp;
    }

    public UavMapLog() {
	String logFileName = "uavmap"+getTimeStamp(null)+".log";
	try {
	    logStream = new FileOutputStream(logFileName);
	}
	catch(FileNotFoundException e) {
	    Machinetta.Debugger.debug(3, "IOException opening log file ("+logFileName+") for writing, e="+e);
	}
	logPrint = new PrintStream( logStream );
	synchronized(logPrint) {
	    logPrint.println(System.currentTimeMillis()+" start");
	}
    }

    public void click(String clicktype, UavMapTarget target) {
	synchronized(logPrint) {
	    logPrint.println(System.currentTimeMillis()+" "+clicktype+" "+target.id+" "+target.lat+" "+target.lon+" "+target.localx+" "+target.localy+" "+target.screenx+" "+target.screeny+" "+target.screenw+" "+target.screenh);
	}
    }

    public void navData(int uavid, double latitude, double longitude, double altitude, double localx, double localy, double yaw, double pitch, double roll) {
	synchronized(logPrint) {
	    logPrint.println(System.currentTimeMillis()+" navdata "+uavid+" "+latitude+" "+longitude+" "+altitude+" "+localx+" "+localy+" "+yaw+" "+pitch+" "+roll);
	}
    }

    private LogLine parseLine(ArrayList<String> fieldList) {
	String[] fields = fieldList.toArray(new String[1]);
	LogLine logLine = new LogLine();
	logLine.time = Long.parseLong(fields[0]);

	if(fields[1].equalsIgnoreCase("navdata")) {
	    logLine.navData = true;
	    logLine.uavid = Integer.parseInt(fields[2]);
	    logLine.lat = Double.parseDouble(fields[3]);
	    logLine.lon = Double.parseDouble(fields[4]);
	    logLine.alt =  Double.parseDouble(fields[5]);
	    logLine.localx = Double.parseDouble(fields[6]);
	    logLine.localy =  Double.parseDouble(fields[7]);
	    logLine.yaw = Double.parseDouble(fields[8]);
	    logLine.pitch = Double.parseDouble(fields[9]);
	    logLine.roll =  Double.parseDouble(fields[10]);
	}
	// Original log format, soon to be obsolete
	else if(fields[1].equalsIgnoreCase("UAV")) {
	    logLine.navData = true;
	    logLine.uavid = Integer.parseInt(fields[2]);
	    logLine.lat = Double.parseDouble(fields[3]);
	    logLine.lon = Double.parseDouble(fields[4]);
	    logLine.alt =  Double.parseDouble(fields[5]);
	    logLine.localx = Double.parseDouble(fields[6]);
	    logLine.localy =  Double.parseDouble(fields[7]);
	}
	else if(fields[1].equalsIgnoreCase("click") 
		|| fields[1].equalsIgnoreCase("clickadd")
		|| fields[1].startsWith("click")) {
	    logLine.clickAdd = true;
	    logLine.clickType = fields[1];
	    logLine.clickId = Integer.parseInt(fields[2]);
	    logLine.lat = Double.parseDouble(fields[3]);
	    logLine.lon = Double.parseDouble(fields[4]);
	    logLine.localx = Double.parseDouble(fields[5]);
	    logLine.localy =  Double.parseDouble(fields[6]);
	    logLine.screenx =  Double.parseDouble(fields[7]);
	    logLine.screeny = Double.parseDouble(fields[8]);
	    logLine.screenW = Double.parseDouble(fields[9]);
	    logLine.screenH =  Double.parseDouble(fields[10]);
	}
	else if(fields[1].equalsIgnoreCase("start")) {
	    logLine.start = true;
	}
	return logLine;
    }

    // @TODO: This is borked... if parseLine() is static it can't use
    // the private inner class LogLine;
    //
    // AirSim-src/AirSim/Machinetta/UavMapLog.java:108: non-static variable this cannot be referenced from a static context
    //	LogLine logLine = new LogLine();
    //	                  ^
    //
    // So I made playbackNavDataFromFile() and parseLine() both non
    // static... but that means to playback a log file you have to be
    // writing a log file which is kinda inane.  Although generally
    // we'll be playing back the nav data so we can have users click
    // on the map and we want to log their clicks, so that's ok, we
    // will be writing a log file, but I can imagine cases where you
    // wouldn't.  Oh well.

    public void playbackNavDataFromFile(UavMapDisplay mapDisplay, String filename, double speedup) {
	// @TODO: Change this to read a line at a time instead of the
	// entire file at once.
	ArrayList<ArrayList<String>> lineList = CSV.parseFile(filename, ' ');
	long playbackStartTime = System.currentTimeMillis();
	long logStartTime = -1;
	for(int loopi = 0; loopi < lineList.size(); loopi++) {
	    LogLine logLine = parseLine(lineList.get(loopi));
	    // The first logs we recorded don't have a start time.  
	    if(-1 == logStartTime)
		logStartTime = logLine.time;

	    if(logLine.navData) {

		long logLineTimeSinceStart = logLine.time - logStartTime;
		logLineTimeSinceStart = (long)((double)logLineTimeSinceStart/speedup);
		long timeToUpdate = playbackStartTime + logLineTimeSinceStart;
		long timeToSleep = timeToUpdate - System.currentTimeMillis();

		if(timeToSleep > 0) {
		    Machinetta.Debugger.debug(1, "logtime "+logLine.time+" logLineTimeSinceStart "+logLineTimeSinceStart+" sleeping "+timeToSleep+" until "+timeToUpdate);
		    try {Thread.sleep(timeToSleep); } catch (Exception e) {}		    
		}
		mapDisplay.updateUav(logLine.uavid, logLine.lat, logLine.lon, logLine.alt, logLine.yaw, logLine.pitch, logLine.roll);
	    }
	    else if(logLine.start) {
		logStartTime = logLine.time;
	    }
	}

    }

    private class LogLine {
	long time;
	boolean start = false;
	boolean navData = false;
	boolean clickAdd = false;
	String clickType = null;
	int uavid;
	int clickId = -1;
	double lat = -1;
	double lon = -1;
	double alt = -1;
	double localx = -1;
	double localy = -1;
	double yaw = 0;
	double pitch = 0;
	double roll = 0;
	double screenx = -1;
	double screeny = -1;
	double screenW = -1;
	double screenH = -1;
    }
    
}

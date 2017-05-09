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
 * AirportGrep.java
 *
 * Created on November 12, 2007, 1:02 PM
 */

package AirSim.Commander;

import Gui.LatLonUtil;

import java.text.DecimalFormat;
import java.util.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.swing.*;

/**
 *
 * @author  junyounk
 */
public class AirportGrep extends javax.swing.JFrame {
    private final static DecimalFormat fmt = new DecimalFormat("0.00000");

    double latN;
    double lonW;
    double latS;
    double lonE;
    private class Location {
	double lat;
	double lon;
	double alt;
	public Location(double lat, double lon, double alt) {
	    this.lat = lat;
	    this.lon = lon;
	    this.alt = alt;
	}
    }
    private HashMap<String,Location> latLonMap = null;
    private ArrayList<String> latLonAry = null;
    private String[] latLonNames = null;
    
    public static String[] parseList(char delim, String list) {
	if(list == null)
	    return null;
	if(list.equals(""))
	    return null;

	Vector returnVec = new Vector();
	String[] returnArray = null;

	// Copy list into a char array.
	char listChars[];
	listChars = new char[list.length()];
	list.getChars(0, list.length(), listChars, 0);

	int count = 0;
	int itemStart = 0;
	int itemEnd = 0;
	String newItem = null;

	while(count < listChars.length) {
	    count = itemEnd;
	    if(count >= listChars.length)
		break;
	    itemStart = count;
	    itemEnd = itemStart;
	    while(itemEnd < listChars.length) {
		if(delim != listChars[itemEnd]) {
		    itemEnd++;
		}
		else
		    break;
	    }
	    newItem = new String(listChars, itemStart, itemEnd - itemStart);
	    itemEnd++;
	    count = itemEnd;
	    returnVec.add(newItem);
	}
	// Convert from vector to array, and return it.
	returnArray = new String[1];
	returnArray = (String[])returnVec.toArray((Object[])returnArray);
	return returnArray;
    }
    private void loadLatLons(String latLonFileName) {
	try {
	    FileReader fileReader = new FileReader(latLonFileName);
	    BufferedReader bufferedReader = new BufferedReader(fileReader);

	    latLonMap = new HashMap<String,Location>();
	    latLonAry = new ArrayList<String>();
    
	    while(true) {
		String line = bufferedReader.readLine();
		if(null == line)
		    break;
		if(line.startsWith("#$"))
		    continue;
// 		String[] fields = parseList('\t', line);
// 		if(fields.length != 3) {
// 		    Machinetta.Debugger.debug(3,"Unable to parse line in lat lon file, ignoring, line='"+line+"'");
// 		    continue;
// 		}
// 		double lat = Double.parseDouble(fields[0]);
// 		double lon = Double.parseDouble(fields[1]);
// 		String name = fields[2];
 		String[] fields = parseList(':', line);
 		if(fields.length != 14) {
 		    Machinetta.Debugger.debug(3,"Unable to parse line in lat lon file, got "+fields.length+" colon (:) separated fields, expecting 14, ignoring, line='"+line+"'");
 		    continue;
 		}
		String code = fields[0];
		String name = fields[2];
		String country = fields[4];
		double degrees, minutes, seconds;
		degrees = Integer.parseInt(fields[05]);
		minutes = Integer.parseInt(fields[06]);
		seconds = Integer.parseInt(fields[07]);
		double lat = degrees + minutes/60 + seconds/3600;
		if(fields[8].equals("S"))
		    lat = -lat;
		
		degrees = Integer.parseInt(fields[9]);
		minutes = Integer.parseInt(fields[10]);
		seconds = Integer.parseInt(fields[11]);
		double lon = degrees + minutes/60 + seconds/3600;
		if(fields[12].equals("U"))
		    lon = -lon;
		
		double alt = Double.parseDouble(fields[13]);

		if(lat > latS && lat < latN && lon > lonW && lon < lonE) {
		    latLonMap.put(name,new Location(lat,lon, alt));
		    latLonAry.add(name);
		    System.out.println("FOUND:"+line);
		}
	    }
	}
	catch(Exception e){
	    Machinetta.Debugger.debug(3,"Unable to read lat/lon file='"+latLonFileName+"', e=" + e);
	    e.printStackTrace();
	    return;
	}

    }

    /** Creates new form AirportGrep */
    public AirportGrep(String latLonFileName, double latN, double lonW, double latS, double lonE) {
	System.err.println("Grepping nw corner lat/lon="+latN+","+lonW+", sw corner lat/lon="+latS+","+lonE);
	this.latN = latN;
	this.lonW = lonW;
	this.latS = latS;
	this.lonE = lonE;
	if(null != latLonFileName) 
	    loadLatLons(latLonFileName);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
	String latLonFileName = null;
	if(null != args) {
	    if(args.length >= 1)
		latLonFileName = args[0];
	}
	double latPittsburgh=40.440;
	double lonPittsburgh=-79.996;
	double latn = latPittsburgh  +  LatLonUtil.kmToDegreesLat(latPittsburgh, 800);
	double lats = latPittsburgh  -  LatLonUtil.kmToDegreesLat(latPittsburgh, 800);
	double lonw = lonPittsburgh  -  LatLonUtil.kmToDegreesLon(latPittsburgh, 800);
	double lone = lonPittsburgh  +  LatLonUtil.kmToDegreesLat(latPittsburgh, 800);

	AirportGrep ag = new AirportGrep(latLonFileName,latn, lonw, lats, lone);
    }



}

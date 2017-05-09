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
import java.awt.geom.Point2D;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.*;
import java.net.*;
import java.util.*;

public class VCBridgeClient implements Runnable {

    private String serverHostname = null;
    private int serverPort = 0;
    private String name = null;
    private int uavid = NavData.INVALID_UAV_ADDRESS;
    
    private Thread myThread = null;
    private Socket sock = null;
    private BufferedReader in = null;
    private PrintWriter out = null;

    private NavData latestNavData = null;

    public NavData updateLocation(){
	return latestNavData;
    }

    public void sendCurrent(Waypoint[] path, int uavAddress){
	Waypoint wp = path[0];
	Machinetta.Debugger.debug(1,"Sending waypoint to UAV = "+wp.x+" "+wp.y+" "+wp.z+" "+wp.time);
	out.println("WAYPOINT "+wp.x+" "+wp.y+" "+wp.z+" "+wp.time);
	out.flush();
    }

    public VCBridgeClient(String serverHostname, int serverPort, int uavid, String name){
	this.serverHostname =  serverHostname;
	this.serverPort = serverPort;
	this.name = name;
	this.uavid = uavid;
	myThread = new Thread(this);
	latestNavData = new NavData();
    }
    
    public void start() {
	myThread.start();
    }

    private  void processInputLine(String line) {
	String[] words = StringUtils.parseList(' ',line);
	if(words.length <= 0)
	    return;
	if(words[0].equalsIgnoreCase("ERROR")) {
	    Machinetta.Debugger.debug(3," Got an error back from VCBridgeServer = "+line);
	    return;
	}
	if(words[0].equalsIgnoreCase("NAVDATA")) {
	    if(words.length < 9) {
		Machinetta.Debugger.debug(3," Got a NAVDATA line from VCBridgeServer, but not enough fields, should be 9, only "+words.length+", original line="+line);
		return;
	    }
	    NavData data = new NavData();
	    data.procerusUAVAddress = Integer.parseInt(words[2]);
	    data.latitude = Double.parseDouble(words[4]);
	    data.longitude = Double.parseDouble(words[6]);
	    data.altitude =  Double.parseDouble(words[8]);
	    latestNavData = data;
	    return;
	}
	Machinetta.Debugger.debug(1,"Unknown line from VCBridgeServer="+line);
    }
    

    public void run() {
	while(true) {
	    Machinetta.Debugger.debug(1,"Opening connection to "+serverHostname+" port "+serverPort);

	    try {
		sock = new Socket(serverHostname, serverPort);
		in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		out = new PrintWriter(sock.getOutputStream());
	    }
	    catch (IOException e){
		e.printStackTrace(System.err);
		try { Thread.sleep(1000); } catch(Exception e2) {}
		continue;
	    }

	    Machinetta.Debugger.debug(1,"About to start reading/writing to/from socket.");

	    out.println("myname "+name);
	    out.println("uavid "+uavid);
	    out.flush();
	    while(true) {
		try {
		    String line = in.readLine();
		    if(null == line) {
			Machinetta.Debugger.debug(3,"Socket to VCBridgeServer has closed on us!");
			break;
		    }
		    processInputLine(line);
		}
		catch (Exception e){
		    Machinetta.Debugger.debug(3,"Exception reading socket from VCBridgeServer, e="+e);
		    e.printStackTrace();
		    break;
		}
	    }

	    // 	byte[] buf = new byte[data.length];
	    // 	int bytes_read = 0;
	    // 	for(int loopi = 1; loopi <= iterations; loopi++) {
	    // 	    try {
	    // 		sockOutput.write(data, 0, data.length);	
	    // 		bytes_read = sockInput.read(buf, 0, buf.length);
	    // 	    }
	    // 	    catch (IOException e){
	    // 		e.printStackTrace(System.err);
	    // 	    }
	    // 	    if(bytes_read < data.length) {
	    // 		System.err.println("run: Sent "+data.length+" bytes, server should have sent them back, read "+bytes_read+" bytes, not the same number of bytes.");
	    // 	    }
	    // 	    else {
	    // 		System.err.println("Sent "+bytes_read+" bytes to server and received them back again, msg = "+(new String(data)));
	    // 	    }

	    // 	    // Sleep for a bit so the action doesn't happen to fast - this is purely for reasons of demonstration, and not required technically.
	    // 	    try { Thread.sleep(50);} catch (Exception e) {}; 
	    // 	}
	    // 	System.err.println("Done reading/writing to/from socket, closing socket.");

	    try {
		sock.close();
	    }
	    catch (IOException e){
		System.err.println("Exception closing socket.");
		e.printStackTrace(System.err);
	    }

	    try { Thread.sleep(1000); } catch(Exception e) {}
	    // loop back to top of while and try to reopen socket!
	} 
    }

}

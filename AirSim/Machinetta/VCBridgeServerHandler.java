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
import java.util.*;
import java.util.concurrent.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.*;
import java.net.*;

public class VCBridgeServerHandler implements Runnable {
    private int POLL_TIMEOUT_MS = 1000;

    private VCBridgeServer server = null;
    private Socket sock = null;
    private BufferedReader in = null;
    private PrintWriter out = null;

    private Thread myThread = null;

    private String myName = "anonymousHandler";
    private boolean wantAllUAVData = false;
    private int myUavID = NavData.INVALID_UAV_ADDRESS;

    private ArrayBlockingQueue<NavData> navDataQueue = new ArrayBlockingQueue<NavData>(100);

    public VCBridgeServerHandler(VCBridgeServer server, Socket sock) {
	this.server = server;
	this.sock = sock;
	try {
	    in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
	    out = new PrintWriter(sock.getOutputStream());
	}
	catch(java.io.IOException e) {
	    Machinetta.Debugger.debug(4,myName+": Exception creating reader/writer for socket, e="+e);
	    e.printStackTrace();
	    close = true;
	}
	this.myThread = new Thread(this);
	// Note that if we call myThread.start() now, we run the risk
	// of this new thread calling run before we're finished
	// constructing.  We can't count on the fact that we call
	// .start() last - javac or the jvm might have reordered the
	// above lines.  The class constructing us must wait for the
	// constructor to return and then call start() on us.
	Machinetta.Debugger.debug(1,myName+": New handler created.");
    }

    public void start() {
	myThread.start();
    }
    
    public void pushNavData(NavData data) { 
	//	Machinetta.Debugger.debug(1,myName+": Pushing navaData on queue.");
	// note, we 'offer' here, so if the queue is backed up it may
	// drop some of these navData packets - which is preferable to
	// the entire system blocking and locking up.
	navDataQueue.offer(data);
    }

    private boolean close = false;
    private Thread outputThread = null;

    private void runOutput() {
	while(true) {
	    NavData data = null;
	    try {
	     data = navDataQueue.poll(POLL_TIMEOUT_MS,TimeUnit.MILLISECONDS);
	    }
	    catch(java.lang.InterruptedException e) {}
	    if(close) {
		try {
		    Machinetta.Debugger.debug(1,myName+": Closing socket.");
		    sock.close();
		}
		catch (Exception e){
		    Machinetta.Debugger.debug(3,myName+": Exception while closing socket, e="+e);
		    e.printStackTrace(System.err);
		}
		return;
	    }
	    if(null == data) 
		continue;
	    //	    Machinetta.Debugger.debug(1,myName+": processing navData="+data);
	    if(wantAllUAVData || 
	       ((myUavID != data.INVALID_UAV_ADDRESS) && (myUavID == data.procerusUAVAddress))) {
		//		Machinetta.Debugger.debug(1,myName+": sending navData to socket.");
		out.println("NAVDATA uav "+data.procerusUAVAddress+" lat "+data.latitude+" lon "+data.longitude+" alt "+data.altitude+" distToTargetMets "+data.distToTargetMets);
		out.flush();
	    }
	}
    }

    private void shutdownOutput() {
	synchronized(this) { close = true; }
	outputThread.interrupt();
    }

    private  void processInputLine(String line) {
	Machinetta.Debugger.debug(1,myName+": processing input from client="+line);
	String[] words = StringUtils.parseList(' ',line);
	if(words.length <= 0)
	    return;
	if(words[0].equalsIgnoreCase("WAYPOINT")) {
	    if(words.length < 5) {
		out.println("ERROR 'Command WAYPOINT lacks sufficient params, need x,y,z,t' input '"+line+"'");
		
		Machinetta.Debugger.debug(1,myName+": waypoint command has < 5 commands");
		for(int loopi = 0; loopi < words.length; loopi++) {
		    Machinetta.Debugger.debug(1,myName+":         words["+loopi+"] = "+words[loopi]);
		}
		return;
	    }
	    double x = Double.parseDouble(words[1]);
	    double y = Double.parseDouble(words[2]);
	    double z = Double.parseDouble(words[3]);
	    long t = Long.parseLong(words[4]);
	    Waypoint wp = new Waypoint(x,y,z,t);
	    Machinetta.Debugger.debug(1,myName+": Seidng waypoint to server="+wp.toString());
	    server.sendWaypoint(myUavID, wp);
	    return;
	}
	if(words[0].equalsIgnoreCase("UAVID")) {
	    if(words[1].equalsIgnoreCase("all")) {
		wantAllUAVData = true;
		return;
	    }
	    myUavID = Integer.parseInt(words[1]);
	    return;
	}
	if(words[0].equalsIgnoreCase("MYNAME")) {
	    myName = words[1];
	    return;
	}
	out.println("ERROR 'unknown command' input '"+line+"'");
	out.flush();
    }
    
    // All this method does is wait for some bytes from the
    // connection, read them, then write them back again, until the
    // socket is closed from the other side.
    public void run() {
	Machinetta.Debugger.debug(1,myName+": run() starting.");

	// In case an exception happened when we were creating
	// reader/writer
	if(close) {
	    server.removeHandler(this);
	    return;
	}

	outputThread = new Thread() { 
		public void run() {
		    runOutput();
		}
	    };
	outputThread.start();

	while(true) {
	    try {
		String line = in.readLine();
		if(null == line) {
		    server.removeHandler(this);
		    shutdownOutput();
		    Machinetta.Debugger.debug(1,myName+": got null from reading input socket, handler is returning from run.");
		    return;
		}
		processInputLine(line);
	    }
	    catch (Exception e){
		e.printStackTrace();
		break;
	    }
	}
    }
}

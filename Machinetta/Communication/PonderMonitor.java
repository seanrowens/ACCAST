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
package Machinetta.Communication;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.*;
import java.util.*;
import java.net.*;

public class PonderMonitor implements Runnable {
    private String hostname = null;
    private int port = 0;
    private PonderInterface ponderInterface;
    private Socket sock = null;
    private InputStream sockInput = null;
    private OutputStream sockOutput = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private Thread myThread;
    private boolean stopping = false;
    private InetAddress hostAddress = null;

    public PonderMonitor(String hostname, int port, PonderInterface ponderInterface) {
	Machinetta.Debugger.debug(1,"Constructing with hostname "+hostname+" port="+port);
	this.hostname = hostname;
	this.port = port;
	this.ponderInterface = ponderInterface;

	try {
	    hostAddress = InetAddress.getByName(hostname);
	} catch (UnknownHostException e) {
	    Machinetta.Debugger.debug(3,e.getMessage());
	}

	Machinetta.Debugger.debug(1,"Done constructing.");
    }

    public void start() { 
	myThread = new Thread(this);
	myThread.start();
    }

    public void stop() { 
	stopping = true;
	myThread.interrupt();
    }
    
    public void run() {
	Machinetta.Debugger.debug(1,"Entering run.");

	sock = null;
	int sleepTime = 2000;

	while (!stopping) {
	    while(null == sock) {
		try {
		    sock = new Socket(hostAddress, port);
		    sockInput = sock.getInputStream();
		    sockOutput = sock.getOutputStream();
		    out = new PrintWriter(sockOutput, true);
		    in = new BufferedReader(new InputStreamReader(sockInput));
		}
		catch (IOException e){
		    Machinetta.Debugger.debug(1,"Exception opening connection to host "+hostname+" port "+port+", e="+e);
		    e.printStackTrace();
		    Machinetta.Debugger.debug(1,"Ignoring exception and sleeping for "+sleepTime+" ms");
		    try { Thread.sleep(sleepTime); } catch (InterruptedException ie) {};
		}
	    }
	    
	    try {
		String inputLine;

		inputLine = in.readLine();
		if(null == inputLine) {
		    Machinetta.Debugger.debug(3,"Input line was null, closing socket and ignoring.");
		    sockInput.close();
		    sockOutput.close();
		    sock.close();
		    sock = null;
		    continue;
		}
		ponderInterface.ponder(inputLine);
	    }
	    catch (Exception e){
		Machinetta.Debugger.debug(4,"Exception e="+e);
		e.printStackTrace();
		Machinetta.Debugger.debug(4,"Closing socket and otherwise ignoring exception");
		try {
		    sock.close();
		}
		catch (Exception e2){
		    Machinetta.Debugger.debug(4,"Ignoring exception while closing socket="+e2);
		    e2.printStackTrace();
		}
		sock = null;
	    }
	}
	Machinetta.Debugger.debug(1,"Returning from run.");
    }
}

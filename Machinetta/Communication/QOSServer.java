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

public class QOSServer implements Runnable {
    private int serverPort = 0;
    private QOSInterface qos;
    private ServerSocket serverSock = null;
    private Socket sock = null;
    private InputStream sockInput = null;
    private OutputStream sockOutput = null;
    private Thread myThread;
    private boolean stopping = false;

    public QOSServer(int serverPort, QOSInterface qos) {
	Machinetta.Debugger.debug(1,"Constructing with server port="+serverPort);

	this.serverPort = serverPort;
	this.qos = qos;

 	try {
	    serverSock = new ServerSocket(serverPort);
 	}
 	catch (IOException e){
 	    Machinetta.Debugger.debug(4,"Exception opening server socket for listeing, e="+e);
	    e.printStackTrace();
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

	while (!stopping) {
	    try {
		Machinetta.Debugger.debug(1,"Waiting for accept.");
		sock = serverSock.accept();
		Machinetta.Debugger.debug(1,"Have accepted new socket.");

		// If you are very concerned about message latency (i.e. 1ms
		// instead of 15ms is important), uncomment the following line
		// to make the socket a bit faster.  Note that setting
		// TcpNoDelay to true in some sense makes your application a
		// 'bad internet citizen'.
		//
		// sock.setTcpNoDelay(true);

		sockInput = sock.getInputStream();
		sockOutput = sock.getOutputStream();
		PrintWriter out = new PrintWriter(sockOutput, true);
		BufferedReader in = new BufferedReader(new InputStreamReader(sockInput));

		String inputLine, outputLine;

		inputLine = in.readLine();
		if(null == inputLine) {
		    Machinetta.Debugger.debug(3,"Input line was null, closing socket and ignoring.");
		    sockInput.close();
		    sockOutput.close();
		    continue;
		}

		StringTokenizer tok = new StringTokenizer(inputLine);
		if(!tok.hasMoreTokens()) {
		    Machinetta.Debugger.debug(3,"Tried to tokenize inputline, got no tokens, closing socket and ignoring.");
		    sockInput.close();
		    sockOutput.close();
		    continue;
		}
		String command = tok.nextToken();
		if(null == command) {
		    Machinetta.Debugger.debug(3,"Tried to tokenize inputline, first token was null, closing socket and ignoring.");
		    sockInput.close();
		    sockOutput.close();
		    continue;
		}
		
		if(command.equalsIgnoreCase("setqos")) {
		    Machinetta.Debugger.debug(1,"Got command setqos.");
		    if(!tok.hasMoreTokens()) {
			out.println("Format of setqos command is 'setqos nn.nn id1 id2' where nn.nn is a real number.");
			sockOutput.flush();
			Machinetta.Debugger.debug(3,"Got command setqos, but missing parameter (i.e. setqos 1.0 id1 id2), closing socket and ignoring.");
			sockInput.close();
			sockOutput.close();
			continue;
		    }
		    String qosLevelString = tok.nextToken();
		    if(null == qosLevelString) {
			out.println("Format of setqos command is 'setqos nn.nn id1 id2' where nn.nn is a real number.");
			sockOutput.flush();
			Machinetta.Debugger.debug(3,"Got command setqos, but null parameter (i.e. setqos 1.0 id1 id2), closing socket and ignoring.");
			sockInput.close();
			sockOutput.close();
			continue;
		    }
		    double qosLevel = 0.0;
		    try {
			qosLevel = Double.parseDouble(qosLevelString);
		    }
		    catch(Exception e) {
			out.println("Format of setqos command is 'setqos nn.nn id1 id2' where nn.nn is a real number.");
			sockOutput.flush();
			Machinetta.Debugger.debug(3,"Got command setqos, but parameter ('"+qosLevelString+"') was not a parseable as a double, closing socket and ignoring.");
			sockInput.close();
			sockOutput.close();
			continue;
		    }
		    if(!tok.hasMoreTokens()) {
			out.println("Format of setqos command is 'setqos nn.nn id1 id2' where nn.nn is a real number.");
			sockOutput.flush();
			Machinetta.Debugger.debug(3,"Got command setqos, but missing parameter (i.e. setqos 1.0 id1 id2), closing socket and ignoring.");
			sockInput.close();
			sockOutput.close();
			continue;
		    }
		    String firstid = tok.nextToken();
		    if(!tok.hasMoreTokens()) {
			out.println("Format of setqos command is 'setqos nn.nn id1 id2' where nn.nn is a real number.");
			sockOutput.flush();
			Machinetta.Debugger.debug(3,"Got command setqos, but missing parameter (i.e. setqos 1.0 id1 id2), closing socket and ignoring.");
			sockInput.close();
			sockOutput.close();
			continue;
		    }
		    String secondid = tok.nextToken();

		    out.println("Setting QOS = "+qosLevel);
		    qos.setQOS(qosLevel, firstid, secondid);
		    sockOutput.flush();
		    sockOutput.close();
		}
		else {
		    out.println("Unknown command='"+command+"', closing socket.");
		    sockOutput.flush();
		    Machinetta.Debugger.debug(1,"Tried to tokenize inputline, first token was null, closing socket and ignoring.");
		    sockInput.close();
		    sockOutput.close();
		}
		Machinetta.Debugger.debug(1,"Finished with socket, waiting for next connection.");
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
	    }
	}
	Machinetta.Debugger.debug(1,"Returning from run.");
    }
}

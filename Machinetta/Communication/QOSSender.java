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

import java.io.*;
import java.net.*;

public class QOSSender {
    private final static int QOS_SERVER_PORT_DEFAULT = 4433;

    public static void send(double qosLevel, String firstID, String secondID) {
	Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;

	String command = "setqos "+qosLevel+" "+firstID+" "+secondID;

	String hostname="localhost";
        try {
            socket = new Socket(hostname,QOS_SERVER_PORT_DEFAULT);
            out = new PrintWriter(socket.getOutputStream(), true);
	    out.println(command);
	    out.flush();
	    out.close();
        }
	catch (UnknownHostException e) {
            Machinetta.Debugger.debug(3,"UnknownHost exception opening socket to host "+hostname+" port "+QOS_SERVER_PORT_DEFAULT+", e="+e);
	    e.printStackTrace();
	    return;
        }
	catch (IOException e) {
            Machinetta.Debugger.debug(3,"IO exception opening socket to host "+hostname+" port "+QOS_SERVER_PORT_DEFAULT+", e="+e);
	    e.printStackTrace();
	    return;
        }
    }

    public static void main(String[] argv) {
	double qosLevel = 1.0;
	String firstID = null;
	String secondID = null;

	if(argv.length != 3) {
	    System.err.println("Error, need 3 args, only got "+argv.length);
	    System.err.println("Usage: setqos nn.nn firstid secondid");
	    System.err.println("    nn.nn should be a real number");
	    System.exit(1);
	}
	
	qosLevel = Double.parseDouble(argv[0]);
	firstID = argv[1];
	secondID = argv[2];
	System.err.println("Sending setqos command with qosLevel="+qosLevel+" firstID="+firstID+" secondID="+secondID);
	QOSSender.send(qosLevel, firstID, secondID);
    }
}

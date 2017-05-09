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
import java.util.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class VCBridgeServer {
    private String virtualCockpitIPAddress = null;

    private int serverPort = 0;
    private ServerSocket serverSock = null;
    private Socket sock = null;

    VCAutopilot autopilot = null;

    ArrayList<VCBridgeServerHandler> handlers = new ArrayList<VCBridgeServerHandler>();

    public void createHandler(Socket sock) {
	VCBridgeServerHandler handler = new VCBridgeServerHandler(this, sock);
	synchronized (handlers) {
	    handlers.add(handler);
	}
	handler.start();
    }

    public void removeHandler(VCBridgeServerHandler handler) {
	synchronized (handlers) {
	    handlers.remove(handler);
	}
    }

    public void pushNavData(NavData data) {
	synchronized (handlers) {
	    //	    Machinetta.Debugger.debug(1,"Handlers size="+handlers.size());
	    for(int loopi = 0; loopi < handlers.size(); loopi++) {
		VCBridgeServerHandler handler = handlers.get(loopi);
		handler.pushNavData(data);
	    }
	}
    }

    public void sendWaypoint(int uavid, Waypoint wp) {
	Waypoint[] path = new Waypoint[1];
	path[0] = wp;
	autopilot.sendCurrent(path, uavid);
    }

    public VCBridgeServer(String virtualCockpitIPAddress, int serverPort) throws IOException {
	this.virtualCockpitIPAddress = virtualCockpitIPAddress;
	this.serverPort = serverPort;

	autopilot = new VCAutopilot(virtualCockpitIPAddress);
	serverSock = new ServerSocket(serverPort);

 	(new Thread() { 
 	    public void run() {
 		while(true) {
		    NavData data = autopilot.waitForNavData();
		    if(null != data) {
			pushNavData(data);
		    }
 		}
 	    }
 	    }).start();
    }
    
    public void waitForConnections() {
	while (true) {
	    try {
		sock = serverSock.accept();
		Machinetta.Debugger.debug(1,"Accepted new socket, creating new handler for it.");
		createHandler(sock);
		Machinetta.Debugger.debug(1,"Finished with socket, waiting for next connection.");
	    }
	    catch (IOException e){
		e.printStackTrace();
	    }
	}
    }

    public static void main(String argv[]) {
	int port = 54321;
	String virtualCockpitIPAddress = null;
	boolean useTestData = false;

	for(int loopi=0; loopi < argv.length; loopi++) {
	    if(argv[loopi].equals("-vcip") && ((loopi+1) < argv.length)) {
		virtualCockpitIPAddress = argv[++loopi];
	    }
	    else if(argv[loopi].equals("--test") || argv[loopi].equals("-test")) {
		useTestData = true;
	    }
	    else { 
		Machinetta.Debugger.debug(5,"Unknown command line arg["+loopi+"]="+argv[loopi]);
	    }
        }
        
	VCBridgeServer server = null;
	try {
	    server = new VCBridgeServer(virtualCockpitIPAddress, port);
 	}
 	catch (IOException e){
	    e.printStackTrace();
 	}
	TestLatLonData testData = null;
	if(useTestData) {
	    testData = new TestLatLonData();
	    testData.generateTestUpdates(server);
	}
	server.waitForConnections();
    }

}

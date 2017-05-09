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
 * Proxy.java
 *
 * Created on 4 July 2002, 12:09
 */

package Machinetta;

import Machinetta.State.ProxyState;
import Machinetta.Communication.Communication;
import Machinetta.Communication.Message;
import Machinetta.RAPInterface.RAPInterface;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;
import Machinetta.AA.AAModule;
import Machinetta.Coordination.CoordModule;
import Machinetta.State.BeliefType.ProxyID;

import java.util.Hashtable;
import java.util.HashMap;
import java.util.Date;
import java.io.File;

/**
 * This is the main class of a proxy
 *
 * @author  scerri
 */
public class Proxy {
    
    // SRO Tue Nov 30 23:58:51 EST 2004 - Note, this should really be
    // done some other way but this is a quick hack.  Possibly through
    // the configuration file, but I don't have time right now to run
    // around modifying several thousand fire brigade configuration
    // files.  We can't really pass it IN to
    // Machinetta.Communications.LocalComms because different comms
    // implementations might not need the port.
    public static int serverPort = 7045;

    public static void main(String argv[]) {
	HashMap<String, String> configValues = new HashMap<String, String>();
        // System.out.println("Starting test: " + (new Date()));
        String config = null;
        
	for(int loopi=0; loopi < argv.length; loopi++) {
	    String configVarName = null;
	    String configVarValue = null;
	    if(argv[loopi].startsWith("-")) {
		if(argv[loopi].equals("-config") && ((loopi+2) < argv.length)) {
		    configVarName = argv[++loopi];
		    configVarValue = argv[++loopi];
		    configValues.put(configVarName, configVarValue);
		}
		else { 
		    Machinetta.Debugger.debug(5,"Unknown command line arg["+loopi+"]="+argv[loopi]);
		    Machinetta.Debugger.debug(5,"Usage: Proxy configfile [-config configvaname value] [-config configvaname value] ...");
		}
	    }
	    else {
		File f = new File(argv[0]);
		if (!f.exists()) {
		    Machinetta.Debugger.debug(5,"ERROR: Could not find configuration file : " + argv[0]);
		} else {
		    config = argv[0];
		}
	    }
        }
        
        if (config != null) {
            System.out.println("Using config " + config);
            
            Proxy p = new Proxy(config, configValues);
            
            System.out.println("Initialization Complete\n\n");
        }
	else {
	    Machinetta.Debugger.debug(5,"ERROR: No config file, can't run, exiting.");
	}
    }    
    
    public Proxy(String configFile, HashMap<String, String> configValues) {
        init(configFile, configValues);
    }
    
    public Proxy(String configFile) {
        init(configFile, null);
    }
    
    private void init(String configFile, HashMap<String, String> configValues) {
        // Load configuration
	if(null != configValues) {
	    new Configuration(configFile, configValues);
	}
	else {
	    new Configuration(configFile);
	}

        // Call this method so as to set debug level from Configuration
        Debugger.setDebugLevel(Configuration.DEBUG_LEVEL);
        // Set up comms
        comms = new Communication();
        comms.registerForEvents(this);
        // Moved to after state is loaded: comms.setProxyID(id);
        
        // Set up RAP interface
        rap = new RAPInterface();
        rap.registerForEvents(this);
        
        // Set up ProxyState
        proxyState = new ProxyState(Configuration.DEFAULT_BELIEFS_FILE);
        try {
            ProxyID id = proxyState.getSelf().getProxyID();
            comms.setProxyID(id);
        } catch (NullPointerException e) {
            // Possible bug here, if constructor with ProxyID is used ...
            Machinetta.Debugger.debug(5,"No self belief in beliefs file!! Exiting!");
            proxyState.printState();
            System.exit(0);
        }
        
        // Set up Coord
        coord = new CoordModule(this);
        
        // Set up AA
        aa = new AAModule(this);
        
        // Let all modules process initial beliefs
        proxyState.ready();
        proxyState.notifyListeners();
    }
    
    /** Called when new messages arrive at communication module */
    public void incomingCommunicationMessages() {
        try {
            if (coord != null && proxyState.isReady()) {
                Message [] msgs = comms.recvMessages();
                coord.incomingMessages(msgs);
            }
        } catch (NullPointerException e) {
            Machinetta.Debugger.debug(3,"Null pointer receiving incoming messages.");
            System.err.println("Stack trace for : Null pointer receiving incoming messages from Proxy " + proxyState.getSelf().getProxyID() + " :");
            e.printStackTrace();
        }
    }
    
    /** Called when new messages arrive at RAP interface */
    public void incomingRAPMessages() {
        if (aa != null)
            aa.messagesFromRap(rap.getMessages());  
        else 
            Machinetta.Debugger.debug(0,"Messages from RAP before AA set, waiting");
    }
    
    /** Called when messages should be sent to RAP () */
    public void outgoingRAPMessage(OutputMessage msg) {
        rap.sendMessage(msg);
    }
    
    /** Get ProxyID for this proxy */
    public static ProxyID getProxyID() { return id;}
    
    private ProxyState proxyState = null;
    private RAPInterface rap = null;
    private AAModule aa = null;
    private CoordModule coord = null;
    private Communication comms = null;
    
    private static ProxyID id = null;
}

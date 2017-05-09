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
 * ClientThread.java
 *
 * Created on July 15, 2004, 9:29 PM
 */

package AirSim;

import AirSim.Environment.*;
import AirSim.Environment.Assets.*;
//import Machinetta.DomainSpecific.AS2.*;
import Machinetta.RAPInterface.OutputMessages.OutputMessage;

import java.io.*;
import java.net.*;

/**
 * @deprecated
 * @author  pscerri
 */
public class ClientThread extends Thread {
    private Env env = null;
    
    private Asset asset = null;
    
    public ClientThread(Socket s) {
        env = new Env();
        this.s = s;
        try {
            out = new ObjectOutputStream(s.getOutputStream());
            in = new ObjectInputStream(s.getInputStream());
        } catch (IOException e) {
            Machinetta.Debugger.debug("Client exception " + e, 5, this);
        }
        
        Machinetta.Debugger.debug("Connection to client open.", 0, this);
        
        start();
    }
    
    public synchronized void send(Object o) {
        /*
        try {
            //out.writeObject(o);
            RIPDU pdu = new RIPDU((Machinetta.RAPInterface.InputMessages.InputMessage)o);
            if (pdu != null)
                out.writeObject(pdu);
        } catch (IOException e) {
            Machinetta.Debugger.debug("Problem writing object: " + o, 3, this);
        }
         */
    }
    
    public void run() {
        
        /*
        Object o = null;
        boolean done = false;
        int errors = 0;
        do {
            try  {
                o = in.readObject();
                if (o instanceof SimIDMessage) {
                    SimIDMessage msg = (SimIDMessage)o;
                    asset = env.getAssetByID(msg.id);
                    if (asset != null) {
                        Machinetta.Debugger.debug("Proxy has connected to : " + asset, 1, this);
                        //asset.setProxy(this);
                    } else {
                        Machinetta.Debugger.debug("ID message for unknown asset: " + asset, 3, this);
                    }
                } else if (asset != null && o instanceof OutputMessage) {
                    asset.msgFromProxy((OutputMessage)o);
                } else if (asset != null && o instanceof RIPDU) {
                    asset.PDUFromProxy((RIPDU)o);
                } else {
                    Machinetta.Debugger.debug("Unknown message received by sim: " + o, 3, this);
                }
                
            } catch (ClassNotFoundException e) {
                Machinetta.Debugger.debug("Unknown class " + e, 1, this);
                errors++;
            } catch (EOFException e) {
                Machinetta.Debugger.debug("End of file, server closed", 1, this);
                done = true;
            } catch (SocketException e) {
                Machinetta.Debugger.debug("Socket exception: " + e, 3, this);
                //done = true;
                errors++;
            } catch (IOException e) {
                Machinetta.Debugger.debug("Read failed : " + e, 5, this);
                errors++;
            } catch (ClassCastException e) {
                Machinetta.Debugger.debug("Class cast problem with " + " : " + e, 5, this);
                errors++;
            } catch (Exception e) {
                Machinetta.Debugger.debug("Client error : " + e, 5, this);
                e.printStackTrace();
                errors++;
            }
        } while (o != null && !done && errors < 10);
        
        
        try {
            // clientThreads.remove(this);
            out.close();
            in.close();
        } catch (IOException e) {
            Machinetta.Debugger.debug("Problem closing client socket " + e, 1, this);
        }
*/
    }
    private Socket s = null;
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;
}

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
 * VCBridge.java
 *
 * Created on September 5, 2007, 12:04 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package AirSim.Machinetta;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author Owner
 */
public class VCBridge implements Runnable{
    ConcurrentLinkedQueue<sCommandPacket[]> commandList;
    //SocketConnector sC = new SocketConnector("127.0.0.1");
    //SocketConnector sC = new SocketConnector("128.237.254.202");
    //    SocketConnector sC = new SocketConnector("128.237.231.183");
    SocketConnector sC = null;
    VCCommands vC = null;
    Thread writeThread;
    Thread readThread;
    DataReadThread dataRead = null;
    /** Creates a new instance of VCBridge */
    public VCBridge(String virtualCockpitIPString) {
	sC = new SocketConnector(virtualCockpitIPString);
	vC = new VCCommands(sC);
	dataRead =new DataReadThread(sC);
        commandList = new ConcurrentLinkedQueue<sCommandPacket[]>();
        //sC = new SocketConnector("127.0.0.1");
        writeThread = new Thread(this);
        writeThread.start();
        readThread = new Thread(dataRead);
        readThread.start();
    }
    public NavData waitForNavData() {
	return sC.takeNavData();
    }
    public void addCommand(sCommandPacket[] commands){
        System.err.println("VCBridge.addCommand: entering");
        commandList.add(commands);
    }
    public NavData getNavData(){
        return sC.GetNavData();
    }
    public boolean packetsAvailable(){
        return sC.packetsAvailable();
    }
    public void run() {
        
        //vC.sendAcksStdTelemetry();
        //vC.requestAllVCPackets();
        vC.requestNavigationDataOnly();
	//	vC.request248and249();
	while (sC.IsConnected()){
           
// 	    // @TODO: HACK - using 5 for uav id, this should be
// 	    // configurable somehow.  Probably should just send it for
// 	    // all uavs (1 to 5) that we have.
// 	    vC.sendPassthrough26and27(1);
// 	    vC.sendPassthrough26and27(2);
// 	    vC.sendPassthrough26and27(3);
// 	    vC.sendPassthrough26and27(4);
// 	    vC.sendPassthrough26and27(5);

            if(!commandList.isEmpty()){
                //System.err.println("There are commands on the queue");
                try{
                    sCommandPacket[] scp = commandList.poll();
                    vC.SendCmdsToVc(scp);
                }
                catch(Exception e){
		    System.err.println("Exception e="+e);
		    e.printStackTrace();
		}
            }
            try{Thread.sleep(100);} catch(Exception e){
		System.err.println("Thread was interrupted");
	    }
        }
	System.err.println("VCBridge.run: exiting (presumably sC.IsConnected() returned false.");
    }
    
}









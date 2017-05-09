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
 * Sim.java
 *
 * Created on July 22, 2003, 3:18 PM
 */

package LocalComms;

//import LocalComms.SimpleSim.AirSim.Objects.ControlAgent.ControlAgent;
import Machinetta.RAPInterface.OutputMessages.*;

import java.io.*;

/**
 *
 * @author  pscerri
 */
public class SimServer extends LocalComms.Server {
        
    //private ControlAgent [] agents;
    
    Process [] procs = null;
            
    public SimServer() { }
    
    /** 
     * Creates a bunch of Machinetta agents with appropriate config and belief files 
     */
    public SimServer(int noAgents, String logLoc, String infoPrefix, String classpath) {
        // Start the server
        super.start();     
        
/*
        // Run the proxies
        Runtime runtime = Runtime.getRuntime();
        procs = new Process[noAgents];        
        //String [] cmdarray = {"ssh", "athiri", "/usr0/pscerri/bin/java", "-classpath", classpath, "Machinetta.Test.Test", "Dummy"};
        String [] cmdarray = {"java", "-classpath", classpath, "Machinetta.Test.Test", "Dummy"};
        for (int i = 0; i < noAgents; i++) {
            try {
                //cmdarray[6] = infoPrefix+(i+1)+".cfg";
                cmdarray[4] = infoPrefix+(i+1)+".cfg";
                procs[i] = runtime.exec(cmdarray);
                (new ProcessMonitor(procs[i], logLoc+ File.separator+"proxy"+(i+1))).start();
                Thread.sleep(500);
            } catch (IOException e) {
            } catch (InterruptedException e2) {}
        } 
*/
                     
    }
    
    /** Accept messages from proxies */
    protected void RAPMessage(OutputMessage m, int threadID) {
    }
    
    /** Start up the game */
    public static void main(String argv []) {
        new SimServer(1, "C:\\Temp", "beha", "C:\\Code");
    }
    
    protected void done () {
        if (procs == null) return;
        for (int i = 0; i < procs.length; i++) {
            procs[i].destroy();
        }
        
        try {
            for (int i = 0; i < procs.length; i++) {
                procs[i].waitFor();
            }
            Machinetta.Debugger.debug(1, "All proxies killed");    
        } catch (Exception e) {
            Machinetta.Debugger.debug(5, "Problem waiting for dead processes, may be live proxies.");
        }        
    }
    
    class ProcessMonitor extends Thread {
        
        Process p1 = null;
        String outFile = null;
        FileWriter out = null; 
        public ProcessMonitor (Process p1, String outFileName) {
            this.p1 = p1;
            try {
                out = new FileWriter(outFileName);
            } catch (IOException e) {
                Machinetta.Debugger.debug(5, "Could not open log file for writing " + outFileName);
            }
            outFile = outFileName;
        }
        
        public void run () {
            InputStreamReader inr = new InputStreamReader (p1.getInputStream());
            BufferedReader breader = new BufferedReader(inr);
            
            InputStreamReader inrErr = new InputStreamReader (p1.getErrorStream());
            BufferedReader breaderErr = new BufferedReader(inrErr);
        
            int lc = 0;
            try {            
                String line = null;
                while ((line = breader.readLine()) != null) {
                    //System.out.println(outFile + " " + line);
                    out.write(line + "\n");
                    out.flush();
                    if (inrErr.ready()) {
                        while ((line = breaderErr.readLine()) != null) {
                            //System.out.println(outFile + " " + line);
                            out.write("Err:" + line + "\n");
                            out.flush();
                        }
                    }
                }
            } catch (IOException e) { System.out.println(e);}
        }
    }
}

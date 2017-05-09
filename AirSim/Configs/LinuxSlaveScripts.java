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
package AirSim.Configs;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class LinuxSlaveScripts {

    static public void makeAllKillScript(String loc, String [] SLAVE_MACHINES) {
	String locs = loc+File.separator;

        // Make scripts for all machines
        try {
            String filename = locs + "allkill.sh";
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));
            
            out.writeBytes("#!/bin/tcsh\n\n");
            
            //out.writeBytes("setenv CONFIG_DIR "+loc+"\n");
            out.writeBytes("setenv CONFIG_DIR "+"."+"\n");
            out.writeBytes("echo \"Killing\"\n");
            out.writeBytes("ps auxwwww|fgrep $USER|fgrep java|fgrep -v NetBeans | fgrep -v fgreg | cut -c 17-23 | xargs kill\n");
            out.writeBytes("sleep 1\n");
            /*
            out.writeBytes("ps auxwwww|fgrep $USER|fgrep java| fgrep -v javac |egrep \"UDPSwitch|MainFrame|Proxy\"| cut -c 8-15|xargs kill >& /dev/null\n");
            out.writeBytes("ps auxwwww|fgrep $USER|fgrep jamvm |egrep \"UDPSwitch|MainFrame|Proxy\"| cut -c 8-15|xargs kill >& /dev/null\n");
            out.writeBytes("sleep 1\n");
            out.writeBytes("ps auxwwww|fgrep $USER|fgrep java| fgrep -v javac |egrep \"UDPSwitch|MainFrame|Proxy\"| cut -c 8-15|xargs kill -9 >& /dev/null\n");
            out.writeBytes("ps auxwwww|fgrep $USER|fgrep jamvm |egrep \"UDPSwitch|MainFrame|Proxy\"| cut -c 8-15|xargs kill >& /dev/null\n");
            out.writeBytes("\n");
                    */
            out.writeBytes("echo \"Remaining java processes\"\n");
            out.writeBytes("echo \"________________________________________\"\n");
            out.writeBytes("ps auxwwww|head -1\n");
            out.writeBytes("ps auxwwww|fgrep $USER|fgrep java| fgrep -v javac |fgrep -v fgrep\n");
            out.writeBytes("ps auxwwww|fgrep $USER|fgrep jamvm|fgrep -v fgrep\n");
            out.writeBytes("\n");
	    if(SLAVE_MACHINES != null) {
		for (int i = 0; i < SLAVE_MACHINES.length; i++) {
		    out.writeBytes("echo \"Killing things on "+SLAVE_MACHINES[i]+"\"\n");
		    out.writeBytes("ssh "+SLAVE_MACHINES[i]+" $CONFIG_DIR/kill.sh\n");
		    out.writeBytes("\n");
		}
	    }
            
            out.writeBytes("ps auxwwww|fgrep memforeverall.sh | cut -c 8-16|xargs kill\n");
            out.writeBytes("ps auxwwww|fgrep topforeverall.sh | cut -c 8-16|xargs kill\n");

            out.flush();
            out.close();
            Runtime.getRuntime().exec("chmod 744 "+filename);
        } catch (Exception e) {
            System.out.println("Failed to write script file: " + e);
            e.printStackTrace();
        }
    }
    
    static public void makeUptimesScript(String loc, String [] SLAVE_MACHINES) {
	String locs = loc+File.separator;
        // Make scripts for all machines
        String filename = locs+"uptimeall.sh";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));
            
            out.writeBytes("#!/bin/bash\n");
            out.writeBytes("\n");
            //out.writeBytes("export CONFIG_DIR="+loc+"\n");
            out.writeBytes("export CONFIG_DIR="+"."+"\n");
            out.writeBytes("\n");
            out.writeBytes("$CONFIG_DIR/uptime.sh\n");
            out.writeBytes("\n");
	    if(SLAVE_MACHINES != null) {
		for (int i = 0; i < SLAVE_MACHINES.length; i++) {
		    out.writeBytes("ssh "+SLAVE_MACHINES[i]+" $CONFIG_DIR/uptime.sh &\n");
		    out.writeBytes("\n");
		}
	    }
            
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '"+filename+"' : " + e);
            e.printStackTrace();
        }
    }
    
    static public void makeTopsScript(String loc, String [] SLAVE_MACHINES) {
	String locs = loc+File.separator;

        // Make scripts for all machines
        String filename = locs+"topsall.sh";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));
            
            out.writeBytes("#!/bin/bash\n");
            out.writeBytes("\n");
            //out.writeBytes("export CONFIG_DIR="+loc+"\n");
            out.writeBytes("export CONFIG_DIR="+"."+"\n");
            out.writeBytes("\n");
            out.writeBytes("$CONFIG_DIR/top.sh\n");
            out.writeBytes("\n");
	    if(SLAVE_MACHINES != null) {
		for (int i = 0; i < SLAVE_MACHINES.length; i++) {
		    out.writeBytes("ssh "+SLAVE_MACHINES[i]+" $CONFIG_DIR/top.sh\n");
		    out.writeBytes("\n");
		}
	    }
            
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '"+filename+"' : " + e);
            e.printStackTrace();
        }
    }
    
    static public void makeMemAllScript(String loc, String [] SLAVE_MACHINES) {
	String locs = loc+File.separator;

        // Make scripts for all machines
        String filename = locs+"memall.sh";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));
            
            out.writeBytes("#!/bin/bash\n");
            out.writeBytes("\n");
            //out.writeBytes("export CONFIG_DIR="+loc+"\n");
            out.writeBytes("export CONFIG_DIR="+"."+"\n");
            out.writeBytes("\n");
            out.writeBytes("$CONFIG_DIR/mem.sh\n");
            out.writeBytes("\n");
	    if(SLAVE_MACHINES != null) {
		for (int i = 0; i < SLAVE_MACHINES.length; i++) {
		    out.writeBytes("ssh "+SLAVE_MACHINES[i]+" $CONFIG_DIR/mem.sh\n");
		    out.writeBytes("\n");
		}
	    }
            
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '"+filename+"' : " + e);
            e.printStackTrace();
        }
    }

    static public void makeDeleteLogsScript(String loc, String [] SLAVE_MACHINES, String OUTPUT_DIRS) {
	String locs = loc+File.separator;

        // Make scripts for all machines
        String filename = locs+"deletelogsall.sh";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));
            
            out.writeBytes("#!/bin/bash\n");
            out.writeBytes("\n");
            out.writeBytes("rm -rf "+OUTPUT_DIRS+"*\n");
            out.writeBytes("\n");
	    
	    if(SLAVE_MACHINES != null) {
		for (int i = 0; i < SLAVE_MACHINES.length; i++) {
		    out.writeBytes("ssh "+SLAVE_MACHINES[i]+" rm -rf "+OUTPUT_DIRS+"*\n");
		    out.writeBytes("\n");
		}
            }
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '"+filename+"' : " + e);
            e.printStackTrace();
        }
    }

    static public void makeFetchLogsScript(String loc, String [] SLAVE_MACHINES, String OUTPUT_DIRS) {
	String locs = loc+File.separator;

        // Make scripts for all machines
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(locs + "fetchlogsall.sh"));
            
            out.writeBytes("#!/bin/bash\n\n");
            
	    if(SLAVE_MACHINES != null) {
		for (int i = 0; i < SLAVE_MACHINES.length; i++) {
		    out.writeBytes("echo \"Fetching logs from "+SLAVE_MACHINES[i]+"\"\n");
		    out.writeBytes("scp -r "+SLAVE_MACHINES[i]+":"+OUTPUT_DIRS+"* "+OUTPUT_DIRS+"\n");
		}
	    }
            
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file: " + e);
            e.printStackTrace();
        }
        
    }
}

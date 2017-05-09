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

public class LinuxStaticScripts {

    static public void makeUptimeScript(String loc) {
	String locs = loc+File.separator;
        String filename = locs+"uptime.sh";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));
            
            out.writeBytes("#!/bin/bash\n");
            out.writeBytes("\n");
            out.writeBytes("UPTIME=`uptime`\n");
            out.writeBytes("echo $HOSTNAME $UPTIME\n");
            
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '"+filename+"' : " + e);
            e.printStackTrace();
        }
    }
    
    static public void makeTopScript(String loc) {
	String locs = loc+File.separator;
        String filename = locs+"top.sh";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));
            
            out.writeBytes("#!/bin/bash\n");
            out.writeBytes("\n");
            out.writeBytes("echo $HOSTNAME\n");
            out.writeBytes("top -b -n 1 -u $USER\n");
            
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '"+filename+"' : " + e);
            e.printStackTrace();
        }
    }
    
    static public void makeTopForeverAllScript(String loc) {
	String locs = loc+File.separator;

        // Make scripts for all machines
        String filename = locs+"topforeverall.sh";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));
            
            out.writeBytes("#!/bin/bash\n");
            out.writeBytes("\n");
            //out.writeBytes("export CONFIG_DIR="+loc+"\n");
            out.writeBytes("export CONFIG_DIR="+"."+"\n");
            out.writeBytes("\n");
	    out.writeBytes("while [ true ] ; do\n");
	    out.writeBytes("date \n");
	    out.writeBytes("$CONFIG_DIR/topsall.sh\n");
	    out.writeBytes("sleep 30\n");
	    out.writeBytes("done\n");
            
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '"+filename+"' : " + e);
            e.printStackTrace();
        }
    }

    static public void makeMemScript(String loc) {
	String locs = loc+File.separator;
        String filename = locs+"mem.sh";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));
            
            out.writeBytes("#!/bin/bash\n");
            out.writeBytes("\n");
            out.writeBytes("echo $HOSTNAME\n");
	    out.writeBytes("ps -ww -u $USER -o \"%cpu,%mem,vsz,rss,args\" | fgrep java | fgrep -v fgrep\n");
            
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '"+filename+"' : " + e);
            e.printStackTrace();
        }
    }
    
    static public void makeMemForeverAllScript(String loc) {
	String locs = loc+File.separator;

        // Make scripts for all machines
        String filename = locs+"memforeverall.sh";
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));
            
            out.writeBytes("#!/bin/bash\n");
            out.writeBytes("\n");
            //out.writeBytes("export CONFIG_DIR="+loc+"\n");
            out.writeBytes("export CONFIG_DIR="+"."+"\n");
            out.writeBytes("\n");
	    out.writeBytes("while [ true ] ; do\n");
	    out.writeBytes("echo SAMPLE\n");
	    out.writeBytes("date \n");
	    out.writeBytes("$CONFIG_DIR/memall.sh\n");
	    out.writeBytes("sleep 30\n");
	    out.writeBytes("done\n");
            
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '"+filename+"' : " + e);
            e.printStackTrace();
        }
    }
    
    static public void makeKillScript(String loc) {
	String locs = loc+File.separator;
        String filename = locs+"kill.sh";
        // Make scripts for all machines
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(filename));
            
            out.writeBytes("#!/bin/tcsh\n");
            out.writeBytes("\n");
            //out.writeBytes("ps auxwwww|fgrep $USER|fgrep java| fgrep -v javac |egrep \"UDPSwitch|MainFrame|Proxy\"| cut -c 18-23|xargs kill >& /dev/null\n");
            out.writeBytes("ps auxwwww|fgrep $USER|fgrep java| fgrep -v javac | cut -c 13-23 | xargs kill >& /dev/null\n");
            //out.writeBytes("ps auxwwww|fgrep $USER|fgrep jamvm |egrep \"UDPSwitch|MainFrame|Proxy\"| cut -c 18-23|xargs kill >& /dev/null\n");
            out.writeBytes("ps auxwwww|fgrep $USER|fgrep jamvm | cut -c 18-23|xargs kill >& /dev/null\n");
            out.writeBytes("sleep 1\n");
            //out.writeBytes("ps auxwwww|fgrep $USER|fgrep java| fgrep -v javac |egrep \"UDPSwitch|MainFrame|Proxy\"| cut -c 18-23|xargs kill -9 >& /dev/null\n");
            //out.writeBytes("ps auxwwww|fgrep $USER|fgrep jamvm |egrep \"UDPSwitch|MainFrame|Proxy\"| cut -c 18-23|xargs kill >& /dev/null\n");
            out.writeBytes("ps auxwwww|fgrep $USER|fgrep java| fgrep -v javac | cut -c 18-23|xargs kill -9 >& /dev/null\n");
            out.writeBytes("ps auxwwww|fgrep $USER|fgrep jamvm | cut -c 18-23|xargs kill >& /dev/null\n");
            out.writeBytes("\n");
            out.writeBytes("echo \"Remaining java processes on \" $HOST\n");
            out.writeBytes("echo \"________________________________________\"\n");
            out.writeBytes("ps auxwwww|fgrep $USER|fgrep java| fgrep -v javac |fgrep -v fgrep\n");
            out.writeBytes("ps auxwwww|fgrep $USER|fgrep jamvm |fgrep -v fgrep\n");
            
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Failed to write script file '"+filename+"' : " + e);
            e.printStackTrace();
        }
    }
    

}

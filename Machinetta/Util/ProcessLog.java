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
 * ProcessLog.java
 *
 * Created on December 19, 2004, 1:49 PM
 */

package Machinetta.Util;

import java.io.*;
import java.util.*;

/**
 *
 * @author  pscerri
 */
public class ProcessLog {
    
    /** Creates a new instance of ProcessLog */
    public ProcessLog(String fileName) {
        
        BufferedReader buf = null;
        LinkedList<Record> records = new LinkedList<Record>();
        
        try {
            File f = new File(fileName);
            FileReader is = new FileReader(f);
            buf = new BufferedReader(is);
        } catch (Exception e) {
            Machinetta.Debugger.debug( 3,"Problem getting input stream: " + e);
        }
        
        try {
            while (buf.ready()) {
                try {
                    String line = buf.readLine();
                    StringTokenizer st = new StringTokenizer(line);
                    String file = st.nextToken();
                    long time = Long.parseLong(st.nextToken());
                    st.nextToken();
                    int level = Integer.parseInt(st.nextToken());
                    String msg = line.substring(line.indexOf('"')+1); // ,line.lastIndexOf('"'));
                    // System.out.println("From: " + file + " @ " + time + " " + level + " ^^^^ " + msg);
                    
                    Record r = new Record(time, file, level, msg);
                    int index = 0;
                    boolean found = false;
                    for (ListIterator li = records.listIterator(); !found && li.hasNext(); ) {
                        Record old = (Record)li.next();
                        if (old.time > r.time) found = true;
                        else index++;
                    }
                    records.add(index, r);
                } catch (Exception e) {
                    Machinetta.Debugger.debug( 3,"Problem reading : " + e);
                }
            }
        } catch (Exception e) {
            Machinetta.Debugger.debug( 3,"Problem reading : " + e);
        }
        
        System.out.println("Sorted list:");
        for (ListIterator li = records.listIterator(); li.hasNext(); ) {
            System.out.println(li.next());
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            Machinetta.Debugger.debug( 1,"Usage: java Machinetta.Util.ProcessLog logfile");
        } else {
            new ProcessLog(args[0]);
        }
    }
    
    class Record {
        
        long time;
        int level;
        String from, msg;
        
        public Record(long time, String from, int level, String msg) {
            this.time = time;
            this.from = from;
            this.level = level;
            this.msg = msg;
        }
        
        public String toString() {
            return time + " : " + from + " " + msg;
        }
    }
}

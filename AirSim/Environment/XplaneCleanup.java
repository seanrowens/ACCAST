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
 * MainFrame.java
 *
 * Created on June 19, 2004, 12:38 PM
 */

package AirSim.Environment;

import AirSim.Environment.*;
import AirSim.ProxyServer;
import AirSim.Environment.Assets.*;
import AirSim.Environment.Assets.Tasks.*;

import Gui.*;

import javax.swing.JOptionPane;
import java.util.*;
import java.io.*;

public class XplaneCleanup {

    public static void clean(String filename) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String str;
            while ((str = in.readLine()) != null) {
                process(str);
            }
            in.close();
        } catch (IOException e) {
            System.out.println("Error reading config file: " + e);
        }
    }

    static double lastheading = 0.0;
    public static void process(String line) {
	String[] fields = StringUtils.parseList('\t', line);
	double heading = Double.parseDouble(fields[0]);
	double lat = Double.parseDouble(fields[0]);
	double lon = Double.parseDouble(fields[0]);
	double distance = Double.parseDouble(fields[0]);
	double headdiff = heading - lastheading;

	lastheading = heading;
	if(headdiff < 0) headdiff  = -headdiff;
	if(headdiff > 29)
	    return;
	System.out.println(heading+"\t"+lat+"\t"+lon+"\t"+distance);
    }

    public static void main(String[] args) {
	XplaneCleanup.clean(args[0]);
    }

}

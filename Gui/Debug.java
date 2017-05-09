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
package Gui;

// @author      Sean R. Owens
// @version     $Id: Debug.java,v 1.2 2006/07/10 21:54:22 owens Exp $ 

import Machinetta.Debugger;

public class Debug {

    // debugging out put - for when you want EVERYTHING
    public static void debug(String message) {
	Debugger.debug(message, 0, "Debug");
    }
    
    // info output to aid in impromptu debugging - i.e. stuff thats
    // more important than debug() level, but not a warning or error.
    public static void info(String message) {
	Debugger.debug(message, 1, "Debug");
    }

    // Nonfatal problems.
    public static void warn(String message) {
	Debugger.debug(message, 3, "Debug");

    }

    // fatal or near fatal problems
    public static void error(String message) {
	Debugger.debug(message, 4, "Debug");
    }

    public static void error(Throwable t) {
	Debugger.debug(t.toString(), 5, "Debug");
    }

    // Stuff you just want to output.
    public static void output(String message) {
	Debugger.debug(message, 2, "Debug");
    }



    // debugging out put - for when you want EVERYTHING
    public static void debug(Object obj, String message) {
	Debugger.debug(message, 0, obj);
    }
    
    // info output to aid in impromptu debugging - i.e. stuff thats
    // more important than debug() level, but not a warning or error.
    public static void info(Object obj, String message) {
	Debugger.debug(message, 1, obj);
    }

    // Nonfatal problems.
    public static void warn(Object obj, String message) {
	Debugger.debug(message, 3, obj);

    }

    // fatal or near fatal problems
    public static void error(Object obj, String message) {
	Debugger.debug(message, 4, obj);
    }

    public static void error(Object obj, Throwable t) {
	Debugger.debug(t.toString(), 5, obj);
    }

    // Stuff you just want to output.
    public static void output(Object obj, String message) {
	Debugger.debug(message, 2, obj);
    }

}

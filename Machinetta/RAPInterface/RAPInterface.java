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

 * RAPInterface.java

 *

 * Created on 9 July 2002, 13:58

 */



package Machinetta.RAPInterface;



import Machinetta.RAPInterface.InputMessages.InputMessage;

import Machinetta.RAPInterface.OutputMessages.OutputMessage;

import Machinetta.Configuration;

import Machinetta.Proxy;



/**

 * The interface to the R or A or P

 *

 * Implements singleton pattern, so anyone can create objects

 *

 * @author  scerri

 */

public class RAPInterface {

    

    /** Creates a new instance of RAPInterface */

    public RAPInterface() {

        if (impl == null) { init(); }

    }



    /** Register proxy so it can recieve incoming message notifications 

     *

     * @param proxy The main proxy object

     */

    public void registerForEvents (Proxy proxy) {

        impl.registerProxy(proxy);

    }

    

    /** Get messages received since last called 

     * 

     * @return List of InputMessage objects

     */

    public InputMessage [] getMessages () { return impl.getMessages(); }

    

    /** Send a message to the RAP 

     *

     * @param msg The message to be sent

     */

    public void sendMessage (OutputMessage msg) { impl.sendMessage(msg); }

    

    /** Creates singleton 

     *

     */

    private void init () {



        String className = Configuration.RAP_INTERFACE_IMPLEMENTATION_TYPE;

        

        try {

            Class type = Class.forName(className);

            impl = (RAPInterfaceImplementation)type.newInstance();

            Machinetta.Debugger.debug( 1,"RAPInterface " + type + " created");

        } catch (ClassNotFoundException e) {

            Machinetta.Debugger.debug( 5,"Could not find RAPInterface : " + Configuration.RAP_INTERFACE_IMPLEMENTATION_TYPE);

        } catch (InstantiationException e2) {

            Machinetta.Debugger.debug( 5,"Could not instantiate : " + e2);

        } catch (IllegalAccessException e3) {

            Machinetta.Debugger.debug( 5,"Could not instantiate : " + e3);

        } catch (ClassCastException e4) {

            Machinetta.Debugger.debug( 5,"RAPInterface specified (" + className + ") was not a RAPInterfaceImplementation");

        }



        if (impl == null) {

            Machinetta.Debugger.debug( 1,"Using default RAPInterface");

            impl = new TerminalInterface();

        }



        // Start the RAPInterface off

        impl.start();

    }



    /** Implementation of the interface */

    private static RAPInterfaceImplementation impl = null;

}


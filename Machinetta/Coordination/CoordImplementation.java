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
 * CoordImplementation.java
 *
 * Created on 28 July 2002, 14:45
 */

package Machinetta.Coordination;

import Machinetta.Communication.*;
import Machinetta.Communication.Message;
import Machinetta.State.*;

/**
 *
 * @author  scerri
 */
public abstract class CoordImplementation {
    
    /** Creates a new instance of CoordImplementation */
    public CoordImplementation() {
    }
 
    /** Handle incoming messages from other Proxies. 
     *
     * @param msgs Array of message objects that have been recieved since last call
     */
    public abstract void incomingMessages (Message [] msgs);
    
    /** Handle changes to proxy state
     *
     * @param b An array of ids of beliefs that have changed.
     */
    public abstract void proxyStateChange (BeliefID [] b);

    /** Proxy State object that implementations can use to access beliefs.
     */
    protected static ProxyState state = new ProxyState();
    
    /** Communication object that implementations can use to send message.
     */
    protected Communication comms = new Communication();
}

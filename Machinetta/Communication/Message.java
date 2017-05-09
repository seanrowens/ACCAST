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
 * Message.java
 *
 * Created on 4 July 2002, 13:14
 */

package Machinetta.Communication;

import Machinetta.State.BeliefType.ProxyID;
import Machinetta.State.ProxyState;
import java.io.Serializable;

/**
 * This class encapsulates a single message
 *
 * @author  scerri
 */
public abstract class Message implements Serializable {
    
    /**
     * This is how important this message is to the effective functioning of the team.
     * Since it is set by the creator of a message, there are no enforced semantics,
     * but the following interpretations are encouraged:
     *
     * HIGH: This message must get through, communication should do everything it can to
     * get it through.
     * MEDIUM: Default level, coordination is going to break if this message doesn't get through,
     * but in a fixable and non-catastrophic way.  Communication should really endeavour to get it
     * through.
     * LOW: More routine message, coordination needs this message to get through but likely things
     * will work fine even if it doesn't get through.  Communication should try to get the message
     * through, but prioritize other things.
     * NONE: Doesn't matter too much whether this gets through or not.  Of course, we wouldn't be
     * sending it if was completely useless but don't stress about getting it through.
     */
    public enum Criticality { HIGH, MEDIUM, LOW, NONE };
    
    /** Creates a new instance of Message
     * @param dest The identifier of the proxy to send this message to
     */
    public Message(ProxyID dest) {
        this.dest = dest;
        this.source = (new ProxyState()).getSelf().getProxyID();
    }
    
    /** Creates a new instance of Message
     * @param dest The identifier of the proxy to send this message to
     */
    public Message(ProxyID dest, Criticality c) {
        this(dest);
        criticality = c;
    }
    
    /** Get the destination proxy for this message
     *
     * @return The id of the proxy to receive the message
     */
    public ProxyID getDest() { return dest; }
    
    /** Get the source proxy for this message
     *
     * @return The id of the proxy to receive the message
     */
    public ProxyID getSource() { return source; }
    
    /**
     * Set the criticality.
     */
    public void setCriticality(Criticality c) {
        this.criticality = c;
    }
    
    /**
     * Get the message criticality.
     */
    public Criticality getCriticality() { return criticality; }
    
    /** The proxy to whom this message should be sent */
    protected ProxyID dest = null;
    /** The proxY FROM whom this message is being sentl. */
    protected ProxyID source = null;
    
    /** The criticality of the message */
    protected Criticality criticality = Criticality.MEDIUM;
}

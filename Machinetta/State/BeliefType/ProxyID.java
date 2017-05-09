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
 * ProxyID.java
 *
 * Created on 17 September 2002, 15:26
 */

package Machinetta.State.BeliefType;

import Machinetta.State.BeliefID;

import java.io.Serializable;

/**
 *
 * @author  scerri
 */
public abstract class ProxyID extends Belief implements Serializable {
    
    /** Required for auto create XML */
    public ProxyID() {}
     
    /** Creates a new instance of ProxyID */
    public ProxyID(BeliefID id) {
        super(id);
    }
        
    /**
     * Return true if id refers to same agent as this id
     * @param id The id of the proxy to compare to
     * @return true if id matches this ProxyID, false otherwise
     */
    public abstract boolean matches(ProxyID id);    
    
    public BeliefID makeID() {
        return new Machinetta.State.BeliefNameID("AnonymousProxyID");
    }
    
    public boolean equals(Object obj) {
        if (obj.getClass().equals(this.getClass())) {
            return matches((ProxyID)obj);
        }
        return false;
    }
}

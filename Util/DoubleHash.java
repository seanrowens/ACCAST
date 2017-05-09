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
 * DoubleHash.java
 *
 * Created on September 17, 2005, 10:39 AM
 *
 */

package Util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;


/**
 * This simple class allow you to pass two values to be stored with the same key
 * 
 * @author pscerri
 */
public class DoubleHash <KeyType, Val1Type, Val2Type> {
    
    HashMap <KeyType, Val1Type> hash1 = new HashMap<KeyType, Val1Type>();
    HashMap <KeyType, Val2Type> hash2 = new HashMap<KeyType, Val2Type>();
    
    /** Creates a new instance of DoubleHash */
    public DoubleHash() {
    }
    
    public void put (KeyType key, Val1Type val1, Val2Type val2) {
        hash1.put(key, val1);
        hash2.put(key, val2);        
    }
    
    public boolean contains(KeyType key) { return hash1.containsKey(key); }
    
    public Val1Type getVal1(KeyType key) { return hash1.get(key); }
    public Val2Type getVal2(KeyType key) { return hash2.get(key); }
    
    public void remove (KeyType key) { 
        /*if (!hash1.containsKey(key)) {
            System.out.println("DoubleHash will have problem removing " + key + " " + getKeys());
        }*/
        Val1Type v1 = hash1.remove(key);
        Val2Type v2 = hash2.remove(key);
        /*if (v1 == null || v2 == null) {
            System.out.println("Problem removing " + key + " from DoubleHash");
        }*/
    }
    
    public int size() { return hash1.size(); }
    
    public Set<KeyType> getKeys() { return hash1.keySet(); }
    public Collection<Val1Type> getVals1() { return hash1.values(); }
    public Collection<Val2Type> getVals2() { return hash2.values(); }
}

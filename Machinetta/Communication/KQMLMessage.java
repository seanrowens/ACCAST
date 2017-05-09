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
 * KQMLMessage.java
 *
 * Created on August 14, 2002, 1:57 PM
 */

package Machinetta.Communication;

import Machinetta.State.BeliefType.ProxyID;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 *
 * @author  pynadath
 */
public class KQMLMessage extends TextMessage {
    
    /** Creates a new instance of KQMLMessage
     *  @param dest
     * The ID of the intended recipient
     *  @param msgStr
     * String containing the actual message to be sent
     */
    public KQMLMessage(ProxyID dest,String msgStr) {
        super(dest,msgStr);
        table = parseKQML(msgStr);
    }
    
    /** Extracts key-value pairs from raw text string (following KQML syntax)
     * @param rawText Raw text string in KQML format
     *
     * @return
     * A hash table with the extracted key-value pairs
     */
    private Hashtable<String,String> parseKQML(String rawText) {
        Hashtable<String,String> msg = new Hashtable<String,String>();
        StringTokenizer tokens = new StringTokenizer(rawText);
        String lastKey = null;
        /* Performative before first key */
        if (tokens.hasMoreTokens()) {
            String performative = tokens.nextToken(":");
            /** Remove whitespace */
            performative = performative.trim();
            /** Remove opening "(" */
            performative = performative.substring(1);
            lastKey = ":performative";
            msg.put(lastKey, performative);
        }
        while (tokens.hasMoreTokens()) {
            String key = tokens.nextToken(" ");
            String value = tokens.nextToken(":");
            value = value.trim();
            if ((value.startsWith("\"")) || (value.startsWith("("))) {
                /* Multi-word value */
                int unmatchedDelimiters = 1;
                char delimiter = value.charAt(0);
                while (unmatchedDelimiters > 0) {
                    int nextIndex = 0;
                    if (delimiter == '(') { /* Match parens */
                        /* Count left parens */
                        while (nextIndex >= 0) {
                            nextIndex = value.indexOf("(", nextIndex+1);
                            if ((nextIndex > 0) && (value.charAt(nextIndex-1) != '\\'))
                                unmatchedDelimiters++;
                        }
                        /* Count right parens */
                        nextIndex = 0;
                        while (nextIndex >= 0) {
                            nextIndex = value.indexOf(")", nextIndex+1);
                            if ((nextIndex > 0) && (value.charAt(nextIndex-1) != '\\'))
                                unmatchedDelimiters--;
                        }
                    }
                    else {
                        /* Match double-quotes (there should be only one other) */
                        while (nextIndex >= 0) {
                            nextIndex = value.indexOf("\"",nextIndex+1);
                            if ((nextIndex > 0) && (value.charAt(nextIndex-1) != '\\'))
                                unmatchedDelimiters--;
                        }
                    }
                    /* Not yet matched, so move on to next candidate split point */
                    if (unmatchedDelimiters > 0)
                        value = value + ":" + tokens.nextToken(":");
                }
            }
            msg.put(key,value);
            lastKey = key;
        }
        /** The last string value will have an extra ")", so we remove that here */
        String value = (String)msg.get(lastKey);
        value = value.substring(0,value.length()-1);
        msg.put(lastKey,value);
        return msg;
    }
    
    /** Returns the message's field value for the specified key
     *  @param key
     *  Field name
     *  @return
     *  The string value stored in the field, or null if there is no such field
     */
    public String get(String key) {
        return (String)table.get(key);
    }
    
    /** Adds a new message field with a specified value
     *  @param key
     *  Field name
     *  @param value
     *  The value to be stored in the specified field
     */
    public void put(String key,String value) {
        table.put(key,value);
    }
    
    /** Converts the message structure into a KQML-compatible string
     * @return
     * String version of message contents suitable for immediate sending
     */
    public String toString() {
        String str = "("+(String)table.get(":performative");
        for (Enumeration e = table.keys(); e.hasMoreElements(); ) {
            String key = (String)e.nextElement();
            if (!key.equalsIgnoreCase(":performative"))
            str = str + " " + key + " " + (String)table.get(key);
        }
        str = str + ")";
        return str;
    }
    /** Hash table storing key-value pairs corresponding to KQML fields and their contents */
    public Hashtable<String,String> table;
    
    public static final long serialVersionUID = 1L;
}

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
 * PrintHelpers.java
 *
 * Created on March 7, 2005, 10:11 PM
 */

package Util;

import java.util.*;

/**
 *
 * @author paul
 */
public class PrintHelpers {
    
    /** Creates a new instance of PrintHelpers */
    public PrintHelpers () {
    }
    
    public static String prettyPrint (Object o) {
        if (o instanceof Vector)
            return vectorToString ((Vector)o);
        else if (o instanceof Object [])
            return arrayToString ((Object[])o);
        else if (o instanceof int [])
            return arrayToString ((int [])o);
        else if (o instanceof Double)
            return doubleToString (((Double)o).doubleValue (), 6);
        else
            return o.toString ();
    }
    
    public static String vectorToString (Vector v) {
        StringBuffer sb = new StringBuffer ();
        sb.append ("<");
        for (Enumeration e = v.elements (); e.hasMoreElements (); ) {
            sb.append (prettyPrint (e.nextElement ()));
            if (e.hasMoreElements ()) sb.append (", ");
        }
        sb.append (">");
        return sb.toString ();
    }
    
    public static String arrayToString (Object [] o) {
        StringBuffer sb = new StringBuffer ();
        sb.append ("[");
        for (int i = 0; i < o.length; i++) {
            sb.append (prettyPrint (o[i]));
            if (i < o.length - 1) sb.append (", ");
        }
        sb.append ("]");
        return sb.toString ();
    }
    
    public static String arrayToString (int [] o) {
        StringBuffer sb = new StringBuffer ();
        sb.append ("[");
        for (int i = 0; i < o.length; i++) {
            sb.append (o[i] + "");
            if (i < o.length - 1) sb.append (", ");
        }
        sb.append ("]");
        return sb.toString ();
    }
    
    public static String arrayToString (short [] o) {
        StringBuffer sb = new StringBuffer ();
        sb.append ("[");
        for (int i = 0; i < o.length; i++) {
            sb.append (o[i] + "");
            if (i < o.length - 1) sb.append (", ");
        }
        sb.append ("]");
        return sb.toString ();
    }
    
    public static String arrayToString (double [] o) {
        return arrayToString(o, 3);
    }
    
    public static String arrayToString (double [] o, int precision) {
        StringBuffer sb = new StringBuffer ();
        sb.append ("[");
        for (int i = 0; i < o.length; i++) {
            sb.append (doubleToString (o[i], precision) + "");
            if (i < o.length - 1) sb.append (", ");
        }
        sb.append ("]");
        return sb.toString ();
    }
    
    public static String doubleToString (double d, int precision) {
        String s = "" + d;
        if (s.length () > precision) {
            if (s.indexOf ('E') > 0) {
                // @todod Something trick needs to be done to pretty print doubles with 'E's
            } else if (s.indexOf ('.') > precision)
                s = s.substring (0, s.indexOf ('.'));
            else
                s = s.substring (0, precision + 1);
        }
        return s;
    }
}

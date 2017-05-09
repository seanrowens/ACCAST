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
 * MatchCondition.java
 *
 * Created on September 26, 2002, 6:41 PM
 */

package Machinetta.State.BeliefType.Match;

import Machinetta.State.BeliefType.Belief;
import Machinetta.State.BeliefNameID;
import Machinetta.State.BeliefID;

import java.util.Vector;
import java.util.Enumeration;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author  pynadath
 */
public class MatchCondition extends Belief {
    
    /** String value to match against */
    public String myString = null;
    
    /** String label (used as key when storing match result) */
    public String label = null;
    
    /** Vector of keys to compare */
    public Vector<String> inputKeys = null;
    /** Vector of keys to extract */
    public Vector<String> outputKeys = null;
    
    /** For auto-XML */
    public MatchCondition() {
    }
    
    /** Creates a new instance of MatchCondition */
    public MatchCondition(String str,Vector<String> inKeys,Vector<String> outKeys,String label) {
        myString = str;
        inputKeys = inKeys;
        outputKeys = outKeys;
        this.label = label;
    }
    
    public String getString() { return myString; }
    public String getLabel() {  return label; }
    
    public static MatchCondition createFromXML(Element element) {
        Vector<String> inKeys = new Vector<String>();
        Vector<String> outKeys = new Vector<String>();
        /** Iterate through all of the child nodes in the XML tree */
        for (Node node=element.getFirstChild(); node != null; node=node.getNextSibling()) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element)node;
                String tag = child.getTagName();
                if (tag.equalsIgnoreCase("KEY")) {
                    String type = child.getAttribute("Type");
                    if (type.equalsIgnoreCase("output"))
                        outKeys.add(child.getAttribute("Name"));
                    else
                        inKeys.add(child.getAttribute("Name"));
                }
            }
        }
        return new MatchCondition(element.getAttribute("Value"),inKeys,outKeys,element.getAttribute("Label"));
    }
    
    public String toXML() {
        String result = "<Condition Value=\""+getString()+"\"";
        if (label != null)
            result = result + " Label=\""+label+"\"";
        result = result + ">";
        for (Enumeration keys=inputKeys.elements(); keys.hasMoreElements(); )
            result = result + "<Key Name=\""+(String)keys.nextElement()+"\" Type=\"input\"/>";
        for (Enumeration keys=outputKeys.elements(); keys.hasMoreElements(); )
            result = result + "<Key Name=\""+(String)keys.nextElement()+"\" Type=\"output\"/>";
        result = result + "</Condition>";
        return result;
    }
    
    public String toString() {
        return getString();
    }
    
    /** Create a BeliefID for this particular Belief
     * This method should be overloaded by subclasses
     * It will be used by auto create from XML to make an ID
     * once all fields are filled in.
     * If fields are not properly filled in, this method should return null
     * and auto XML will give an error
     * <br>
     * Callees wanting to know the id of this object should call getID(),
     * although the BeliefIDs returned by getID and this should be equal
     */
    public BeliefID makeID() {
        return new BeliefNameID(myString);
    }    
    
    public static final long serialVersionUID = 1L;
    
}

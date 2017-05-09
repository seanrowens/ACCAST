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
 * BeliefsXML.java
 *
 * Created on 10 July 2002, 14:16
 */

package Machinetta.State;

import Machinetta.Configuration;
import Machinetta.State.BeliefType.*;
import Machinetta.State.BeliefType.TeamBelief.*;
import Machinetta.State.BeliefType.TeamBelief.Priority.*;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.io.*;

import org.w3c.dom.*;

/**
 * Looks after the details of reading XML belief specifications from files
 *
 * @author  scerri
 */
public class BeliefsXML {
    
    static ProxyState state = new ProxyState();
    static String endLine = System.getProperty("line.separator");
    
    /** For testing */
    public static void main(String argv[]) {
        
        ProxyState state = new ProxyState();
        
        
        //Belief [] bels  = getBeliefs("C:\\Code\\Machinetta\\State\\SampleBeliefs_2.xml");
        Belief [] bels  = getBeliefs("C:\\Code\\Machinetta\\Configs\\FireBrigade1.xml");
        
        
        for (int i = 0; i < bels.length; i++) {
            state.addBelief(bels[i]);
        }
        
        System.out.println("\n\n\n****************");
        writeBeliefs("C:\\Code\\Machinetta\\State\\BeliefsOut.xml");
        
        System.out.println("\n\n\n****************");
        getBeliefs("C:\\Code\\Machinetta\\State\\BeliefsOut.xml");
        
    }
    
    /** Creates a new instance of BeliefsXML */
    public BeliefsXML() {
    }
    
    /**
     * Reads in an XML file conforming to DTD : Machinetta/State/Beliefs.dtd
     *
     * If an error occurs a message is printed to output and null is returned.
     *
     * @param fileName The path of the XML file to be read
     * @return A list of Belief objects as specified by the file, notice that many will be null
     */
    public static Belief [] getBeliefs(String fileName) {
        Document doc = getDocument(fileName);
        if (doc == null) return null;
        return getBeliefs(doc);
    }
    
    /**
     * Reads in an XML document conforming to DTD : Machinetta/State/Beliefs.dtd
     *
     * If an error occurs a message is printed to output and null is returned.
     *
     * @param doc The XML document
     * @return A list of Belief objects as specified by the file, notice that many will be null
     */
    public static Belief [] getBeliefs(Document doc) {
        Element element = doc.getDocumentElement();
        if ((element == null) || !element.getTagName().equals("Beliefs")) {
            Machinetta.Debugger.debug(5, "Could not get Beliefs node from Beliefs XML file");
            return null;
        }
        
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        Belief [] beliefs = new Belief[nodes.getLength()];
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            if (node.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                if (node.getNodeType() != 3 && node.getNodeType() != 8) { // I.e., empty text field or comment
                    Machinetta.Debugger.debug(3, "Unrecognized node type " + node.getNodeType()
                    + " when parsing belief " + node);
                }
                continue;
            }
            org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
            if (!nodeElement.getTagName().equals("Belief")) {
                Machinetta.Debugger.debug(3, "Found tag " + nodeElement.getTagName() +
                        " when expecteding \"Belief\"");
                continue;
            }
            
            /** Iterate through children (there should be only one, but this iteration seems
             * to work with the StringReader InputSources, unlike the direct item(1) access) */
            for (org.w3c.dom.Node beliefNode=nodeElement.getFirstChild(); beliefNode != null; beliefNode=beliefNode.getNextSibling()) {
                if (beliefNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Element  beliefData = (Element)beliefNode;
                    if (beliefData == null) {
                        System.out.println("BeliefData was null");
                    } else {
                        beliefs[i] = (Belief)createBeliefFromXML(beliefData);
                    }
                }
            }
        }
        return beliefs;
    }
    
    /** This array keeps possible prefixes for belief names when translating to class names
     *
     * Will need to be able to read them in from file or something to allow clients to
     * put classes in other places ...
     */
    static String [] prefixes = null;
    
    private static void createPrefixes() {
        String all = Configuration.DEFAULT_BELIEF_CLASS_FILE_LOCATIONS + Configuration.BELIEF_CLASS_FILE_LOCATIONS;
        StringTokenizer t = new StringTokenizer(all);
        prefixes = new String[t.countTokens()];
        int i = 0;
        while (t.hasMoreTokens())
            prefixes[i++] = t.nextToken();
    }
    
    public static Object createBeliefFromXML(org.w3c.dom.Element beliefData) {
        String tag = beliefData.getTagName();
        Class beliefClass = null;
        int index = 0;
        String name = null;
        
        if (prefixes == null) createPrefixes();
        
        Machinetta.Debugger.debug(-1, "Checking for " + tag);
        
        while (beliefClass == null && index < prefixes.length) {
            try {
                name = prefixes[index] + tag;
                
                beliefClass = Class.forName(name);
                Belief b = null;
                try {
                    b = createObj(beliefClass, beliefData);
                } catch (Exception e) {
                    Machinetta.Debugger.debug(5, "Exception auto create from XML " + e + " for beliefClass " + beliefClass);
                    e.printStackTrace();
                }
                if (b != null) return b;
            } catch (ClassNotFoundException e) {
                Machinetta.Debugger.debug(-1, "Did not find " + name);
                // That wasn't it, try another, if possible
                index++;
            }
        }
        
        // Perhaps it is a standard Java class
        try {
            name = "java.lang." + tag;
            
            beliefClass = Class.forName(name);
            
            // If we have got here, we are processing a java.lang.* class
            NamedNodeMap attributes = beliefData.getAttributes();
            org.w3c.dom.Node attrNode = attributes.getNamedItem("Value");
            if (attrNode != null) {
                if (tag.equalsIgnoreCase("String")) {
                    return attrNode.getNodeValue().toString();
                } else if (tag.endsWith("Integer")) {
                    Machinetta.Debugger.debug(-1, "Processing an Integer: " + beliefData);
                    try {
                        return new Integer(Integer.parseInt(attrNode.getNodeValue().toString()));
                    } catch (NumberFormatException e) {
                        Machinetta.Debugger.debug(5, "Number format exception! " + attrNode.getNodeValue());
                        return new Integer(0);
                    }
                } else if (tag.endsWith("Double")) {
                    Machinetta.Debugger.debug(-1, "Processing a Double: " + beliefData);
                    try {
                        return new Double(Double.parseDouble(attrNode.getNodeValue().toString()));
                    } catch (NumberFormatException e) {
                        Machinetta.Debugger.debug(5, "Number format exception! " + attrNode.getNodeValue());
                        return new Double(0.0);
                    }
                } else if (tag.endsWith("Long")) {
                    Machinetta.Debugger.debug(-1, "Processing a Long: " + beliefData);
                    try {
                        return new Long(Long.parseLong(attrNode.getNodeValue().toString()));
                    } catch (NumberFormatException e) {
                        Machinetta.Debugger.debug(5, "Number format exception! " + attrNode.getNodeValue());
                        return new Long(0);
                    }
                }
                // Add in other options ...
            }
        } catch (ClassNotFoundException e) {
            Machinetta.Debugger.debug(-1, tag + " was not standard Java, either ... ");
        }
        
        // Maybe java.awt.* -- added by Jijun Wang
        try {
            name = "java.awt." + tag;
            
            beliefClass = Class.forName(name);
            Object b = null;
            NamedNodeMap attributes = beliefData.getAttributes();
            if (tag.equalsIgnoreCase("Point")) {
                Machinetta.Debugger.debug(-1, "Processing a Point: " + beliefData);
                org.w3c.dom.Node xNode = attributes.getNamedItem("x");
                String x = (xNode!=null)?xNode.getNodeValue().toString():"0";
                org.w3c.dom.Node yNode = attributes.getNamedItem("y");
                String y = (yNode!=null)?yNode.getNodeValue().toString():"0";
                try {
                    return new java.awt.Point(Integer.parseInt(x), Integer.parseInt(y));
                } catch (NumberFormatException e) {
                    Machinetta.Debugger.debug(5, "Number format exception! " + xNode.getNodeValue()+" "+yNode.getNodeValue());
                    return new java.awt.Point(0,0);
                }
            }
        } catch (ClassNotFoundException e) {
            Machinetta.Debugger.debug(-1, tag + " was not java.awt, either ... ");
        }
        // End --
        
        // Maybe java.util.*
        try {
            name = "java.util." + tag;
            
            beliefClass = Class.forName(name);
            Object b = null;
            NamedNodeMap attributes = beliefData.getAttributes();
            org.w3c.dom.Node attrNode = attributes.getNamedItem("Name");
            Vector v = createVector(beliefData, attrNode.getNodeValue().toString());
            return v;
        } catch (ClassNotFoundException e) {
            Machinetta.Debugger.debug(-1, tag + " was not java.util, either ... ");
        }
        
        // Maybe BeliefID
        if (tag.equalsIgnoreCase("BeliefID")) {
            NamedNodeMap attributes = beliefData.getAttributes();
            org.w3c.dom.Node attrNode = attributes.getNamedItem("Name");
            if (attrNode != null) {
                return new BeliefNameID(attrNode.getNodeValue().toString());
            }
        }
        
        Machinetta.Debugger.debug(5, "BeliefType " + tag + " could not be auto created from XML");
        
        return null;
    }
    
    /**
     * This function attempts to recreate a belief object from XML
     *
     * The function works as follows
     * 1. Try to find belief class for object
     * 2. Create an object of that class
     * 3. Fill in public fields of class from data
     * 3a. Simple types (int, double, string, boolean) are expected to be attributes
     * 3b. BeliefTypes are expected to be child nodes
     */
    @SuppressWarnings("unchecked")
    private static Belief createObj(Class beliefClass, org.w3c.dom.Element beliefData) {
        
        // Attempt to find class for this belief
        // if there are multiple classes matching belief - problem
        if (belClass == null) {
            try {
                belClass = Class.forName("Machinetta.State.BeliefType.Belief");
                belIDClass = Class.forName("Machinetta.State.BeliefID");
            } catch (ClassNotFoundException e) {
                Machinetta.Debugger.debug(5, "Could not find Belief or BeliefID class");
                return null;
            }
        }
        
        // Create an instance of this belief, must have no arg constructor
        Belief b = null;
        Machinetta.Debugger.debug(-1, "Creating a " + beliefClass);
        
        try {
            b = (Belief) beliefClass.newInstance();
        } catch (IllegalAccessException e) {
            Machinetta.Debugger.debug(3, "Class " + beliefClass + " XML create error: " + e + " " + beliefData);
            return null;
        } catch (InstantiationException e2) {
            Machinetta.Debugger.debug(3, "Class " + beliefClass + " XML create error: " + e2 + " " + beliefData);
            return null;
        }
        
        // Cycles through all public fields and tries to set them
        // If they are public strings, ints, booleans or doubles look for attributes
        // if they are hashtables or vectors, look for lists
        // else look recursively
        Field fs [] = beliefClass.getFields();
        NamedNodeMap attributes = beliefData.getAttributes();
        
        for (int i = 0; i < fs.length; i++) {
            Field f = fs[i];
            Machinetta.Debugger.debug(-1, "Field " + f + " " + f.PUBLIC);
            Machinetta.Debugger.debug(-1, "Type : " + f.getType());
            String fname = f.getName();
            
            if (belClass.isAssignableFrom(f.getType())) {
                Machinetta.Debugger.debug(-1, "Belief type field");
                Machinetta.Debugger.debug(-1, "Trying to create child with " + beliefData);
                Belief bChild = createBeliefForName(beliefData, f.getName());
                if (bChild != null) {
                    Machinetta.Debugger.debug(-1, "Created child " + bChild);
                    try {
                        f.set(b,bChild);
                    } catch (IllegalAccessException e) {
                        Machinetta.Debugger.debug(5, "Failed to set " + f + " to " + bChild);
                    }
                } else {
                    Machinetta.Debugger.debug(-1, "Failed to create child for " + f);
                }
            } else if (f.getType().isAssignableFrom(belIDClass)) {
                Machinetta.Debugger.debug(-1, "Attempting to create id for field " + f);
                BeliefID id = createBeliefID(beliefData, f.toString());
                if (id != null) {
                    try {
                        f.set(b, id);
                    } catch (IllegalAccessException e) {
                        Machinetta.Debugger.debug(5, "Failed to set " + f + " to " + id);
                    }
                } else {
                    // This is likely not important, hence the 0 debug level
                    Machinetta.Debugger.debug(-1, "Failed to create id for " + f);
                }
                
            } else if (f.getType().toString().endsWith("Hashtable")) {
                Machinetta.Debugger.debug(-1, "Processing hashtable " + f);
                Hashtable h = createHashtable(beliefData, f.getName());
                if (h != null) {
                    try {
                        f.set(b, h);
                    } catch (IllegalAccessException e) {
                        Machinetta.Debugger.debug(3, "Failed to set " + f + " to " + h + " in hashtable");
                    }
                }
                
            } else if (f.getType().toString().endsWith("HashMap")) {
                Machinetta.Debugger.debug(-1, "Processing a hashmap");
                HashMap h = createHashMap(beliefData, f.getName());
                if (h != null) {
                    try {
                        f.set(b, h);
                    } catch (IllegalAccessException e) {
                        Machinetta.Debugger.debug(3, "Failed to set " + f + " to " + h + " in hashmap");
                    }
                }
                
            } else if (f.getType().toString().endsWith("Vector")) {
                Machinetta.Debugger.debug(-1, "Processing a vector");
                Vector v = createVector(beliefData, f.getName());
                if (v != null) {
                    try {
                        f.set(b, v);
                    } catch (IllegalAccessException e) {
                        Machinetta.Debugger.debug(3, "Failed to set " + f + " to " + v + " in Vector");
                    }
                }
                // Added by Jijun Wang
            } else if (f.getType().toString().endsWith("Point")) {
                Machinetta.Debugger.debug(-1, "Processing Point");
                java.awt.Point p = createPoint(beliefData, f.getName());
                if (p != null) {
                    try {
                        f.set(b, p);
                    } catch (IllegalAccessException e) {
                        Machinetta.Debugger.debug(3, "Failed to set " + f + " to " + p);
                    }
                }
                // End --
            } else {
                org.w3c.dom.Node attrNode = attributes.getNamedItem(fname);
                if (attrNode != null) {
                    String value = attrNode.getNodeValue();
                    try {
                        if (f.getType().toString().equalsIgnoreCase("int")) {
                            f.setInt(b, (new Integer(value)).intValue());
                        } else if (f.getType().toString().equalsIgnoreCase("double")) {
                            f.setDouble(b, (new Double(value)).doubleValue());
                        } else if (f.getType().toString().equalsIgnoreCase("float")) {
                            f.setFloat(b, (new Float(value)).floatValue());
                        } else if (f.getType().toString().equalsIgnoreCase("boolean")) {
                            f.setBoolean(b, (new Boolean(value)).booleanValue());
                        } else if (f.getType().toString().equalsIgnoreCase("long")) {
                            f.setLong(b, (new Long(value)).longValue());
                        } else {
                            // Since not Belief or BeliefID, assume object (probably only string works?)
                            f.set(b, value);
                        }
                    } catch (IllegalAccessException e) {
                        Machinetta.Debugger.debug(3, "Failed to set " + f + " to " + value);
                    }
                    Machinetta.Debugger.debug(-1, "Attr " + fname + " has value " + value);
                } else {
                    Machinetta.Debugger.debug(-1, "Field " + fname + " not found");
                }
            }
        }
        
        // Once finished, should be all required information available to makeID
        if (b.id == null) {
            BeliefID id = b.makeID();
            if (id != null) {
                b.id = id;
            } else {
                Machinetta.Debugger.debug(3, "Could not create id for Belief object : " + b);
                return null;
            }
        }
        
        // Hack for RAPBeliefs
        if (b instanceof RAPBelief) {
            ((RAPBelief)b).setProxyID(new NamedProxyID(b.id.toString()));
        }
        try {
            Machinetta.Debugger.debug(0, "Read from file: " + b);
        } catch (Exception e) {
            e.printStackTrace();
            Machinetta.Debugger.debug(3, "Read from file: " + beliefData);
        }
        
        // Success!
        return b;
    }
    
    /** Looks for a sub node representing this belief and returns a Belief
     * created from that sub-node
     * <br>
     * Returns null if not found.
     */
    private static Belief createBeliefForName(Node element, String fieldName) {
        // Iterate through all of the child nodes in the XML tree
        for (Node node=element.getFirstChild(); node != null; node=node.getNextSibling()) {
            Machinetta.Debugger.debug(-1, "Checking node " + node);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element)node;
                String tag = child.getTagName();
                
                if (tag.equalsIgnoreCase("Child")) {
                    NamedNodeMap attributes = child.getAttributes();
                    org.w3c.dom.Node attrNode = attributes.getNamedItem("Name");
                    if (attrNode != null && attrNode.getNodeValue().toString().equalsIgnoreCase(fieldName)) {
                        Node data = child.getFirstChild();
                        while (data != null && data.getNodeType() != Node.ELEMENT_NODE) {
                            data = data.getNextSibling();
                        }
                        Machinetta.Debugger.debug(-1, "Found node for child " + fieldName + " : " + data);
                        // Check whether just given ID or whole belief
                        attrNode = attributes.getNamedItem("ReferenceOnly");
                        if (attrNode == null)
                            return (Belief)createBeliefFromXML((Element)data);
                        else {
                            BeliefNameID bid = new BeliefNameID(attrNode.getNodeValue());
                            Belief b = (Belief)state.getBelief(bid);
                            if (b == null) {
                                // This is a problem, fix at some point
                                Machinetta.Debugger.debug(-1, "Value sent ReferenceOnly but no value at receiver, id: " + bid + " for field " + fieldName);
                            }
                            return b;
                        }
                    } else {
                        Machinetta.Debugger.debug(-1, "Could not find name of child element: " + child + " for " + fieldName);
                    }
                } else {
                    Machinetta.Debugger.debug(-1, "Nup, tag was " + tag);
                }
            }
        }
        
        return null;
    }
    
    /** Looks for sub node representing ID and return BeliefID
     * <br>
     * Returns null if ID not found
     */
    private static BeliefID createBeliefID(Node element, String name) {
        String fieldName = name.substring(name.lastIndexOf('.') + 1);
        // Iterate through all of the child nodes in the XML tree
        Machinetta.Debugger.debug(-1, "Looking for " + fieldName);
        for (Node node=element.getFirstChild(); node != null; node=node.getNextSibling()) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                
                Element child = (Element)node;
                String tag = child.getTagName();
                
                if (tag.equalsIgnoreCase(fieldName)) {
                    // Hack for now, assume BeliefNameID
                    NamedNodeMap attributes = node.getAttributes();
                    org.w3c.dom.Node attrNode = attributes.getNamedItem("Name");
                    Machinetta.Debugger.debug(-1, "AttrNode : " + attrNode + " " + attrNode.getNodeValue());
                    return new BeliefNameID(attrNode.getNodeValue());
                }
            }
        }
        Machinetta.Debugger.debug(-1, "ID not found in XML, will try makeID");
        return null;
    }
    
    /** Looks for sub node representing Point and return it
     * <br>
     * Returns null if Point not found -- Added by Jijun Wang
     */
    private static java.awt.Point createPoint(Node element, String name) {
        String fieldName = name.substring(name.lastIndexOf('.') + 1);
        // Iterate through all of the child nodes in the XML tree
        Machinetta.Debugger.debug(-1, "Looking for " + fieldName);
        for (Node node=element.getFirstChild(); node != null; node=node.getNextSibling()) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap attributes = ((Element)node).getAttributes();
                org.w3c.dom.Node attrNode = attributes.getNamedItem("Name");
                String tag = attrNode.getNodeValue().toString();
                if (tag.equalsIgnoreCase(fieldName)) {
                    return (java.awt.Point)createBeliefFromXML((Element)node);
                }
            }
        }
        Machinetta.Debugger.debug(-1, "Point name not found in XML");
        return null;
    }
    
    /**
     * Creates a hashtable from XML file
     *
     * Expects the hashtable name (tableName) to be the beliefType name + "H"
     * Looks for beliefs of the correct name, with attribute Ket providing the store key
     *
     * At the moment, keys probably need to be strings (whatever is returned by attrNode.getNodeValue())
     */
    private static Hashtable createHashtable(Node element, String tableName) {
        
        Machinetta.Debugger.debug(-1, "Creating Hashtable");
        for (Node node=element.getFirstChild(); node != null; node=node.getNextSibling()) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element)node;
                String tag = child.getTagName();
                
                if (tag.equalsIgnoreCase("Hashtable")) {
                    Machinetta.Debugger.debug(-1, "Processing Hashtable " + node);
                    NamedNodeMap attributes = child.getAttributes();
                    org.w3c.dom.Node attrNode = attributes.getNamedItem("Name");
                    if (attrNode != null && attrNode.getNodeValue().toString().equalsIgnoreCase(tableName)) {
                        Hashtable<Object,Object> h = new Hashtable<Object,Object>();
                        for (org.w3c.dom.Node beliefNode=child.getFirstChild(); beliefNode != null; beliefNode=beliefNode.getNextSibling()) {
                            if (beliefNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                                NamedNodeMap attrs = beliefNode.getAttributes();
                                org.w3c.dom.Node keyAttr = attrs.getNamedItem("Key");
                                if (keyAttr != null) {
                                    Machinetta.Debugger.debug(-1, "Adding " + beliefNode + " to Hashtable");
                                    Object b = createBeliefFromXML((Element)beliefNode);
                                    h.put(keyAttr.getNodeValue(), b);
                                } else {
                                    Machinetta.Debugger.debug(1, "Could not find key in " + beliefNode + " for table " + tableName);
                                }
                            }
                        }
                        return h;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Create a Vector
     */
    private static Vector createVector(Node element, String name) {
        Machinetta.Debugger.debug(-1, "Creating vector");
        Element vnode = null;
        for (Node node=element.getFirstChild(); node != null; node=node.getNextSibling()) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element)node;
                String tag = child.getTagName();
                
                if (tag.equalsIgnoreCase("Vector")) {
                    NamedNodeMap attributes = child.getAttributes();
                    org.w3c.dom.Node attrNode = attributes.getNamedItem("Name");
                    if (attrNode != null && attrNode.getNodeValue().toString().equalsIgnoreCase(name)) {
                        vnode = child;
                        break;
                    } else {
                        // Probably just multiple vectors, with different names
                        Machinetta.Debugger.debug(-1, "Wrong vector name when looking for " + name + " got " + (attrNode == null ? null:attrNode.getNodeValue().toString()));
                    }
                }
            }
        }
        
        if (vnode == null) {
            NamedNodeMap attributes = element.getAttributes();
            org.w3c.dom.Node attrNode = attributes.getNamedItem("Name");
            if (attrNode != null && attrNode.getNodeValue().toString().equalsIgnoreCase(name)) {
                vnode = (Element)element;
            }
        }
        
        // Something does not quite work right here ....
        if (vnode != null) {
            Vector<Object> v = new Vector<Object>();
            for (org.w3c.dom.Node beliefNode=vnode.getFirstChild(); beliefNode != null; beliefNode=beliefNode.getNextSibling()) {
                if (beliefNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    Object b = createBeliefFromXML((Element)beliefNode);
                    v.addElement(b);
                }
            }
            return v;
        }
        return null;
    }
    
    /**
     * Create a HashMap for a field with name tableName
     */
    private static HashMap createHashMap(Node element, String tableName) {
        Machinetta.Debugger.debug(-1, "Creating Hashmap");
        for (Node node=element.getFirstChild(); node != null; node=node.getNextSibling()) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element child = (Element)node;
                String tag = child.getTagName();
                
                if (tag.equalsIgnoreCase("HashMap")) {
                    Machinetta.Debugger.debug(-1, "Processing Hashmap " + node);
                    NamedNodeMap attributes = child.getAttributes();
                    org.w3c.dom.Node attrNode = attributes.getNamedItem("Name");
                    if (attrNode != null && attrNode.getNodeValue().toString().equalsIgnoreCase(tableName)) {
                        HashMap<Object,Object> h = new HashMap<Object,Object>();
                        for (org.w3c.dom.Node beliefNode=child.getFirstChild(); beliefNode != null; beliefNode=beliefNode.getNextSibling()) {
                            if (beliefNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                                Machinetta.Debugger.debug(-1, "Adding " + beliefNode + " to HashMap");
                                Object b = createBeliefFromXML((Element)beliefNode);
                                h.put(b, null);
                            }
                        }
                        return h;
                    } else {
                        Machinetta.Debugger.debug(1, "Could not find name of child element: " + child);
                    }
                }
            }
        }
        
        return null;
    }
    
    /** Used by auto XML to decide whether object is a simple type or not */
    private static Class belClass = null;
    /** Used by auto XML to decide whether object is a BeliefID */
    private static Class belIDClass = null;
    
    /**
     * Reads in an XML file conforming to DTD : Machinetta/State/Beliefs.dtd
     * (by default, treats string argument as file name)
     *
     * If an error occurs a message is printed to output and null is returned.
     *
     * @param fileName The path of the XML file to be read
     * @return A Document with the XML parsed
     */
    public static Document getDocument(String fileName) {
        return getDocumentFromFile(fileName);
    }
    
    
    /**
     * Reads in XML source conforming to DTD : Machinetta/State/Beliefs.dtd
     *
     * If an error occurs a message is printed to output and null is returned.
     *
     * @param source The XML source to be parsed
     * @return A Document with the XML parsed
     */
    public static Document getDocument(org.xml.sax.InputSource source) {
        try {
            javax.xml.parsers.DocumentBuilderFactory builderFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = builderFactory.newDocumentBuilder();
            org.w3c.dom.Document document = builder.parse(source);
            BeliefsScanner scanner = new BeliefsScanner(document);
            scanner.visitDocument();
            
            return document;
            
        } catch (java.io.IOException e0) {
            System.out.println("Problem loading beliefs " + e0);
        } catch (javax.xml.parsers.ParserConfigurationException e1) {
            System.out.println("Problem loading beliefs " + e1);
        } catch (org.xml.sax.SAXException e2) {
            System.out.println("Problem loading beliefs " + e2);
        }
        
        return null;
    }
    
    /**
     * Reads in an XML file conforming to DTD : Machinetta/State/Beliefs.dtd
     * (explicit specification that string is a file name)
     *
     * If an error occurs a message is printed to output and null is returned.
     *
     * @param fileName The path of the XML file to be read
     * @return A Document with the XML parsed
     */
    public static Document getDocumentFromFile(String fileName) {
        return getDocument(new org.xml.sax.InputSource(fileName));
    }
    
    /**
     * Reads in an XML string conforming to DTD : Machinetta/State/Beliefs.dtd
     * (explicit specification that string is actual XML content)
     *
     * If an error occurs a message is printed to output and null is returned.
     *
     * @param str The XML content to be read
     * @return A Document with the XML parsed
     */
    public static Document getDocumentFromString(String str) {
        return getDocument(new org.xml.sax.InputSource(new StringReader(str)));
    }
    
    public static String toXML(Belief b) { return toXML(b,false,null); }
    
    public static String toXML(Belief b, boolean child) { return toXML(b,child,null); }
    
    @SuppressWarnings("unchecked")
    public static String toXML(Belief b, boolean child, String additionalAttr) {
        if (b == null) return null;
        
        if (belClass == null) {
            try {
                belClass = Class.forName("Machinetta.State.BeliefType.Belief");
                belIDClass = Class.forName("Machinetta.State.BeliefID");
            } catch (ClassNotFoundException e) {
                Machinetta.Debugger.debug(5, "Could not find Belief or BeliefID class");
                return null;
            }
        }
        
        // Belief names
        StringBuffer sb = new StringBuffer();
        if (!child) sb.append("<Belief>"+endLine);
        String belType = b.getClass().toString();
        belType = belType.substring(belType.lastIndexOf('.') + 1);
        sb.append("<" + belType + " ");
        
        // Fields as attributes
        Field fs [] = b.getClass().getFields();
        
        for (int i = 0; i < fs.length; i++) {
            Field f = fs[i];
            Machinetta.Debugger.debug(-1, "Field " + f + " " + f.PUBLIC);
            Machinetta.Debugger.debug(-1, "Type : " + f.getType());
            String fname = f.getName();
            Object o = null;
            try {
                o = f.get(b);
            } catch (Exception e) {
                Machinetta.Debugger.debug(3, "Problem getting field " + f + " from object " + b);
                continue;
            }
            if (o != null) {
                if (f.getType().toString().equalsIgnoreCase("int") ||
                        f.getType().toString().equalsIgnoreCase("double") ||
                        f.getType().toString().equalsIgnoreCase("float") ||
                        f.getType().toString().endsWith("long") ||
                        f.getType().toString().endsWith("java.lang.String") ||
                        f.getType().toString().equalsIgnoreCase("boolean")) {
                    sb.append(fname + "=\"" + o + "\" ");
                }
            }
        }
        if (additionalAttr != null) sb.append(additionalAttr);
        sb.append(">"+endLine);
        
        // Child nodes
        for (int i = 0; i < fs.length; i++) {
            //Machinetta.Debugger.debug("Current : " + sb, 1, "BeliefsXML");
            Field f = fs[i];
            Machinetta.Debugger.debug(-1, "Field " + f + " " + f.PUBLIC);
            Machinetta.Debugger.debug(-1, "Type : " + f.getType());
            String fname = f.getName();
            Object o = null;
            try {
                o = f.get(b);
            } catch (Exception e) {
                Machinetta.Debugger.debug(3, "Problem getting field " + f + " from object " + b);
                continue;
            }
            if (o == null) {
                // Just ignore
                sb.append("<!-- No value for " + fname + " -->"+endLine);
            } else if (belClass.isAssignableFrom(f.getType())) {
                // Huge hack ... top level objects in agents get sent in full
                if (b instanceof Machinetta.State.BeliefType.MAC.MABelief) {
                    sb.append("<Child Name=\"" + fname + "\">"+endLine);
                    sb.append(toXML((Belief)o, true));
                    sb.append("</Child>"+endLine);
                } else {
                    sb.append("<Child Name=\"" + fname + "\" ReferenceOnly=\"" + ((Belief)o).getID() + "\"></Child>"+endLine);
                }
            } else if (f.getType().isAssignableFrom(belIDClass)) {
                sb.append("<" + fname + " Name=\"" + o + "\"></"+fname+">"+endLine);
                
            } else {
                writeObject(sb, o, fname, f.getType().toString(), false);
            }
        }
        
        // Close belief name stuff
        sb.append("</" + belType + ">"+endLine);
        if (!child) sb.append("</Belief>"+endLine);
        return new String(sb);
    }
    private static void writeObject(StringBuffer sb, Object o, String fname, String ftype, boolean asObject) {
        writeObject(sb, o, fname, ftype, asObject, "");
    }
    
    private static void writeObject(StringBuffer sb, Object o, String fname, String ftype, boolean asObject, String addAttr) {
        if (ftype.endsWith("Hashtable")) {
            Hashtable h = (Hashtable)o;
            sb.append("<Hashtable Name=\"" + fname + "\" " + addAttr + ">"+endLine);
            for (Enumeration e = h.keys(); e.hasMoreElements(); ) {
                Object key = e.nextElement();
                try {
                    sb.append(toXML((Belief)h.get(key), true, " Key=\"" + key + "\""));
                } catch (ClassCastException exp) {
                    writeObject(sb, h.get(key), fname, h.get(key).getClass().toString(), true, " Key=\"" + key + "\"");
                }
                
            }
            sb.append("</Hashtable>"+endLine);
            
        } else if (ftype.endsWith("HashMap")) {
            HashMap h = (HashMap)o;
            sb.append("<HashMap Name=\"" + fname + "\" " + addAttr + ">"+endLine);
            Object [] elements = h.keySet().toArray();
            for (int j = 0; j < elements.length; j++) {
                try {
                    sb.append(toXML((Belief)elements[j], true));
                } catch (ClassCastException exp) {
                    writeObject(sb, elements[j], fname, elements[j].getClass().toString(), true);
                }
            }
            sb.append("</HashMap>"+endLine);
            
        } else if (ftype.endsWith("Vector")) {
            Vector v = (Vector)o;
            sb.append("<Vector Name=\"" + fname + "\" " + addAttr + ">"+endLine);
            for (int j = 0; j < v.size(); j++) {
                try {
                    sb.append(toXML((Belief)v.elementAt(j), true));
                } catch (ClassCastException e) {
                    writeObject(sb, v.elementAt(j), "elem"+j, v.elementAt(j).getClass().toString(), true);
                }
            }
            sb.append("</Vector>"+endLine);
            // Added by Jijun Wang
        } else if (ftype.endsWith("Point")) {
            if (fname.startsWith("elem")) fname = "";
            sb.append("<Point Name=\""+ fname +"\" x = \"" + ((java.awt.Point)o).x + "\" y = \"" + ((java.awt.Point)o).y + "\" "+ addAttr + "> </Point>"+endLine);
            // End --
        } else if (ftype.endsWith("String") ||  ftype.endsWith("boolean") || ftype.endsWith("int") || ftype.endsWith("double")) {
            if (asObject) {
                String cname = o.getClass().toString();
                cname = cname.substring(cname.lastIndexOf('.') + 1);
                sb.append("<" + cname + " Value =\"" + o + "\"" + addAttr + "></"+ cname + ">"+endLine);
            }
        } else if (ftype.endsWith("BeliefNameID")) {
            sb.append("<BeliefID Name=\"" + o + "\" " + addAttr + "> </BeliefID>"+endLine);
            
            // The following two may be causing a problem since they were commented out ...
        } else if (ftype.endsWith("Integer")) {
            sb.append("<Integer Value = \"" + ((Integer)o).intValue() + "\" " + addAttr + "> </Integer>"+endLine);
        } else if (ftype.endsWith("Double")) {
            sb.append("<Double Value = \"" + ((Double)o).doubleValue() + "\" " + addAttr + "> </Double>"+endLine);
        } else if (ftype.endsWith("Long")) {
            sb.append("<Long Value = \"" + ((Long)o).longValue() + "\" " + addAttr + "> </Long>"+endLine);
        } else {
            Machinetta.Debugger.debug(3, "Stumped writing " + o + " of type " + o.getClass() + " fType=" + ftype + " field " + fname);
            sb.append("<!-- Data not written " + fname + " -->"+endLine);
        }
    }
    
    /** Writes out the contents of the proxy's state to file */
    public static void writeBeliefs(String fileName) {
        PrintWriter out = null;
        try {
            File f = new File(fileName);
            out = new PrintWriter(new FileOutputStream(f));
        } catch (FileNotFoundException e) {
            Machinetta.Debugger.debug(3, "File not found " + fileName);
            return;
        }
        try {
            //out.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+endLine+"<!DOCTYPE Beliefs SYSTEM \"Beliefs.dtd\">"+endLine+"<Beliefs>"+endLine+endLine);
            out.print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+endLine+"<Beliefs>"+endLine+endLine);
            ProxyState state = new ProxyState();
            BeliefID [] ids = state.getAllBeliefs();
            for (int i = 0; i < ids.length; i++) {
                Belief b = state.getBelief(ids[i]);
                Machinetta.Debugger.debug(-1, "Writing " + b);
                try {
                    out.print(BeliefsXML.toXML(b, false));
                } catch (Exception e) {
                    Machinetta.Debugger.debug(5, "Problem writing belief : " + b + " " + e);
                }
            }
            out.println();
            Machinetta.Debugger.debug(1, "Done Writing: " + fileName);
        } catch (Exception e) {
            Machinetta.Debugger.debug(5, "Problem writing beliefs !! " + e);
            e.printStackTrace();
        }
        
        try {
            out.print("</Beliefs>"+endLine+endLine);
            out.flush();
            out.close();
        } catch (Exception e) {
            Machinetta.Debugger.debug(5, "Problem closing file!! " + e);
        }
    }
    
}

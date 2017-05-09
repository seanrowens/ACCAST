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
 * DataStructure3D.java
 *
 * Created on June 1, 2004, 7:28 PM
 */
package AirSim.Environment;

import java.util.*;

/**
 * Objects are stored both in a 3D tree (each node breaks down in 8 pieces, representing
 * the 8 "corners" of the box represented by the parent) and in a hashtable, for easy removal.
 *
 * How do we store things like roads?  Maybe not in here?
 *
 * @author  paul
 */
public class DataStructure3D {
    
    Node top = null;
    Hashtable objs = null;
    
    final int minSize = 5;
    
    /** Creates a new instance of DataStructure3D */
    public DataStructure3D(int x1, int y1, int z1, int x2, int y2, int z2) {
        top = new Node(x1, y1, z1, x2, y2, z2);
        objs = new Hashtable();
    }
    
    public void addObject(int x, int y, int z, Object o) {
        top.addObject(x, y, z, o);
    }
    
    public boolean removeObject(Object o) {
        boolean success = true;
        Node n = (Node)objs.get(o);
        if (n != null) {
            success = n.removeObject(o);
            objs.remove(o);
        } else { success = false; }
        return success;
    }
    
    public boolean moveObject(int x, int y, int z, Object o) {
        boolean success = removeObject(o);
        addObject(x,y,z,o);
        return success;
    }
    
    public LinkedList getAll(int x1, int y1, int z1, int x2, int y2, int z2) {
        if (x1 > x2) { int tmp = x2; x2 = x1; x1 = tmp; }
        if (y1 > y2) { int tmp = y2; y2 = y1; y1 = tmp; }
        if (z1 > z2) { int tmp = z2; z2 = z1; z1 = tmp; }
        // System.out.println("Getting : " + x1 + " " + y1 + " " + x2 + " " + y2);
        return top.getAll(x1, y1, z1, x2, y2, z2);
    }
    
    class Node {
        
        int x1, x2, y1, y2, z1, z2;
        boolean leaf = false;
        java.util.LinkedList objects = null;
        Node [] children = null;
        int noChildren = 0;
        
        public Node(int x1, int y1, int z1, int x2, int y2, int z2) {
            this.x1 = x1; this.y1 = y1; this.z1 = z1;
            this.x2 = x2; this.y2 = y2; this.z2 = z2;
            if (x2 - x1 < minSize || y2 - y1 < minSize || z2 - z1 < minSize) {
                leaf = true;
                objects = new java.util.LinkedList();
            } else {
                children = new Node[8];
            }
        }
        
        public synchronized void addObject(int x, int y, int z, Object o) {
            if (leaf) {
                // System.out.println("Object " + o + " added to node @ " + x1 + " " + y1 + " " + z1 + " " + x2 + " " + y2 + " " + z2);
                objects.add(o);
                objs.put(o, this);
            } else {
                getSubnode(x,y,z).addObject(x, y, z, o);
            }
        }
        
        public synchronized boolean removeObject(Object o) {
            return objects.remove(o);
        }
        
        public synchronized LinkedList getAll(int x1, int y1, int z1, int x2, int y2, int z2) {
            if (leaf) {
                // System.out.println("Leaf : " + x1 + " " + y1 + " " + x2 + " " + y2);
                return objects;
            } else {
                LinkedList list = new LinkedList();
                for (int i = 0; i < 8; i++) {
                    if (children[i] != null && children[i].intersects(x1,y1,z1,x2,y2,z2)) {
                        list.addAll(children[i].getAll(x1,y1,z1,x2,y2,z2));
                    }
                }
                return list;
            }
        }
        
        public boolean intersects(int ix1, int iy1, int iz1, int ix2, int iy2, int iz2) {
            if (ix1 > x2 || ix2 < x1 ||
            iy1 > y2 || iy2 < y1 ||
            iz1 > z2 || iz2 < z1) {                
                //System.out.println("Excludes : " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + z1 + " " + z2);
                return false;
            } else {
                //System.out.println("Includes : " + x1 + " " + y1 + " " + x2 + " " + y2);
                return true;
            }
        }
        
        /**
         * Works out which subnode a particular point corresponds to
         */
        private Node getSubnode(int x, int y, int z) {
            Node n = null;
            if (z > (z1 + z2)/2) {
                if (y > (y1 + y2)/2) {
                    if (x > (x1 + x2)/2) {
                        if (children[0] == null) { children[0] = new Node((x1+x2)/2, (y1+y2)/2, (z1 + z2)/2, x2, y2, z2); }
                        n = children[0];
                    } else {
                        if (children[1] == null) { children[1] = new Node(x1, (y1+y2)/2, (z1 + z2)/2, (x1+x2)/2, y2, z2); }
                        n = children[1];
                    }
                } else {
                    if (x > (x1 + x2)/2) {
                        if (children[2] == null) { children[2] = new Node((x1+x2)/2, y1, (z1 + z2)/2, x2, (y1+y2)/2, z2); }
                        n = children[2];
                    } else {
                        if (children[3] == null) { children[3] = new Node(x1, y1, (z1 + z2)/2, (x1+x2)/2, (y1+y2)/2, z2); }
                        n = children[3];
                    }
                }
            } else {
                if (y > (y1 + y2)/2) {
                    if (x > (x1 + x2)/2) {
                        if (children[4] == null) { children[4] = new Node((x1+x2)/2, (y1+y2)/2, z1, x2, y2, (z1 + z2)/2); }
                        n = children[4];
                    } else {
                        if (children[5] == null) { children[5] = new Node(x1, (y1+y2)/2, z1, (x1+x2)/2, y2, (z1 + z2)/2); }
                        n = children[5];
                    }
                } else {
                    if (x > (x1 + x2)/2) {
                        if (children[6] == null) { children[6] = new Node((x1+x2)/2, y1, z1, x2, (y1+y2)/2, (z1 + z2)/2); }
                        n = children[6];
                    } else {
                        if (children[7] == null) { children[7] = new Node(x1, y1, z1, (x1+x2)/2, (y1+y2)/2, (z1 + z2)/2); }
                        n = children[7];
                    }
                }
            }
            return n;
        }
    }
    
    /** Just for testing */
    public static void main(String argv[]) {
        DataStructure3D d = new DataStructure3D(0, 0, 0, 1000, 1000, 1000);
        Integer i1 = new Integer(1);
        Integer i2 = new Integer(2);
        Integer i3 = new Integer(3);
        d.addObject(50, 50, 50, i1);
        d.addObject(100, 100, 100, i2);
        d.addObject(25, 25, 25, i3);
        System.out.println("Removed il? " + d.removeObject(i1));
        List l = d.getAll(10, 10, 10, 500, 500, 500);
        for (ListIterator li = l.listIterator(); li.hasNext(); )
            System.out.println("Was in : " + li.next());
        d.moveObject(750, 75, 75, i2);
        l = d.getAll(10, 10, 10, 500, 500, 500);
        for (ListIterator li = l.listIterator(); li.hasNext(); )
            System.out.println("Was in : " + li.next());
    }
}

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
/**
 * RTSI.java
 *
 * Created: Wed Jan 24 11:15:02 2001
 *
 */

package Util;

import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.JarURLConnection;
import java.util.Vector;
import java.util.jar.*;
import java.util.zip.*;
import java.util.Enumeration;

/**
 * This utility class is looking for all the classes implementing or
 * inheriting from a given interface or class.
 * (RunTime Subclass Identification)
 *
 * some edits by GWK
 *
 * @author <a href="mailto:daniel@satlive.org">Daniel Le Berre</a>
 * @version 1.0
 */
public class RTSI {
    
    
    /**
     * Display all the classes inheriting or implementing a given
     * class in the currently loaded packages.
     * @param tosubclassname the name of the class to inherit from
     */
    public static String[] find(String tosubclassname) {
        return findInPackages(null, tosubclassname);
    }
    
    public static String[] findInPackages(String[] packages, String tosubclassname) {
        Vector<String> strVec = new Vector<String>();
        try {
            Class tosubclass = Class.forName(tosubclassname);
            Package [] pcks = Package.getPackages();
            for (int i=0;i<pcks.length;i++) {
                if(packages == null){
                    Vector<String> s = find(pcks[i].getName(),tosubclass);
                    if(s != null)
                        strVec.addAll(s);
                } else if(searchPackage(packages, pcks[i].getName())){
                    Vector<String> s = find(pcks[i].getName(),tosubclass);
                    if(s != null)
                        strVec.addAll(s);
                    
                }
            }
        } catch (ClassNotFoundException ex) {
            System.err.println("Class "+tosubclassname+" not found!");
        }
        //check for null
        strVec.trimToSize();
        if(strVec != null) {
            String [] res = new String[strVec.size()];
            return  strVec.toArray(res);
        }
        return null;
    }
    
    
    /**
     * Display all the classes inheriting or implementing a given
     * class in a given package.
     * @param pckgname the fully qualified name of the package
     * @param tosubclass the name of the class to inherit from
     */
    public static void find(String pckname, String tosubclassname) {
        try {
            Class tosubclass = Class.forName(tosubclassname);
            find(pckname,tosubclass);
        } catch (ClassNotFoundException ex) {
            System.err.println("Class "+tosubclassname+" not found!");
        }
    }
    
    /**
     * Display all the classes inheriting or implementing a given
     * class in a given package.
     * @param pckgname the fully qualified name of the package
     * @param tosubclass the Class object to inherit from
     */
    public static void printFind(String pckgname, Class tosubclass) {
        String[] s = (String[])find(pckgname, tosubclass).toArray();
        if(s == null) return;
        for(String s1 : s)
            System.out.println(s1);
    }
    
    public static Vector<String> find(String pckgname, Class tosubclass) {
        // Code from JWhich
        // ======
        // Translate the package name into an absolute path
        Vector<String> strVector = new Vector<String>();
        
        
        String name = new String(pckgname);
        if (!name.startsWith("/")) {
            name = "/" + name;
        }
        name = name.replace('.','/');
        
        // Get a File object for the package
        URL url = RTSI.class.getResource(name);
        // URL url = tosubclass.getResource(name);
        // URL url = ClassLoader.getSystemClassLoader().getResource(name);
        //strVector.add(name+"->"+url);
        
        // Happens only if the jar file is not well constructed, i.e.
        // if the directories do not appear alone in the jar file like here:
        //
        //          meta-inf/
        //          meta-inf/manifest.mf
        //          commands/                  <== IMPORTANT
        //          commands/Command.class
        //          commands/DoorClose.class
        //          commands/DoorLock.class
        //          commands/DoorOpen.class
        //          commands/LightOff.class
        //          commands/LightOn.class
        //          RTSI.class
        //
        if (url==null) return null;
        
        File directory = new File(url.getFile());
        
        // New code
        // ======
        if (directory.exists()) {
            // Get the list of the files contained in the package
            String [] files = directory.list();
            for (int i=0;i<files.length;i++) {
                
                // we are only interested in .class files
                if (files[i].endsWith(".class")) {
                    // removes the .class extension
                    String classname = files[i].substring(0,files[i].length()-6);
                    try {
                        // Try to create an instance of the object
                        //Object o = Class.forName(pckgname+"."+classname).newInstance();
                        Class c = Class.forName(pckgname+"."+classname);
                        /*if(pckgname.equalsIgnoreCase("tokensim.Experiment")){
                            System.out.println(pckgname);
                        }*/
                        //if(c.isInstance())
                        if (tosubclass.isAssignableFrom(c)) {
                            strVector.add(pckgname+"."+classname);
                        }
                    } catch (ClassNotFoundException cnfex) {
                        System.err.println(cnfex);
                    }
                }
            }
        } else {
            try {
                // It does not work with the filesystem: we must
                // be in the case of a package contained in a jar file.
                JarURLConnection conn = (JarURLConnection)url.openConnection();
                String starts = conn.getEntryName();
                JarFile jfile = conn.getJarFile();
                Enumeration e = jfile.entries();
                while (e.hasMoreElements()) {
                    ZipEntry entry = (ZipEntry)e.nextElement();
                    String entryname = entry.getName();
                    if (entryname.startsWith(starts)
                    &&(entryname.lastIndexOf('/')<=starts.length())
                    &&entryname.endsWith(".class")) {
                        String classname = entryname.substring(0,entryname.length()-6);
                        if (classname.startsWith("/"))
                            classname = classname.substring(1);
                        classname = classname.replace('/','.');
                        try {
                            // Try to create an instance of the object
                            //Object o = Class.forName(classname).newInstance();
                            Class c = Class.forName(classname);
                            if (tosubclass.isAssignableFrom(c)) {
                                strVector.add(classname);//.substring(classname.lastIndexOf('.')+1));
                            }
                        } catch (ClassNotFoundException cnfex) {
                            System.err.println(cnfex);
                        }
                    }
                }
            } catch (IOException ioex) {
                System.err.println(ioex);
            }
        }
        return strVector;
    }
    
    public static void main(String []args) {
        if (args.length==2) {
            find(args[0],args[1]);
        } else {
            if (args.length==1) {
                find(args[0]);
            } else {
                System.out.println("Usage: java RTSI [<package>] <subclass>");
            }
        }
    }

    private static boolean searchPackage(String[] packages, String pkg) {
        for(String s : packages)
        {
            if(pkg.contains(s.subSequence(0,s.length())))
                return true;
        }
        return false;
    }
}// RTSI

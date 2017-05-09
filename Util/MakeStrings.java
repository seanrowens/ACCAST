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
 * MakeStrings.java
 *
 * Created on November 29, 2005, 11:19 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author paul
 */
public class MakeStrings {
    
    /** Creates a new instance of MakeStrings */
    public MakeStrings(String fileName) {
        try {
            File f = new File(fileName);
            BufferedReader in = new BufferedReader(new FileReader(f));
            String s = in.readLine();
            int count = 0;
            while (s != null) {
                System.out.print("\"" + s + "\",");
                if (++count % 10 == 0) System.out.println("");
                s = in.readLine();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public MakeStrings(String firstNames, String surnames, int names) {
        ArrayList <String> firstName = new ArrayList<String>(), secondName = new ArrayList<String>();
        try {
            File f = new File(firstNames);
            BufferedReader in = new BufferedReader(new FileReader(f));
            String s = in.readLine();
            int count = 0;
            while (s != null) {
                s = s.substring(s.indexOf(' ') + 1);
                if (s.length() > 2) firstName.add(s);
                s = in.readLine();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        try {
            File f = new File(surnames);
            BufferedReader in = new BufferedReader(new FileReader(f));
            String s = in.readLine();
            int count = 0;
            while (s != null) {
                s = s.substring(s.indexOf(' ') + 1);
                if (s.length() > 2) secondName.add(s);
                s = in.readLine();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        Random rand = new Random();
        for (int i = 0; i < names; i++) {
            int f = rand.nextInt(firstName.size());
            int s = rand.nextInt(secondName.size());
            
            System.out.println(firstName.get(f) + " " + secondName.get(s));
        }
    }
    
    public static void main(String argv[]) {
        //new MakeStrings("C:\\Documents and Settings\\paul\\Desktop\\BoatNames");
        new MakeStrings("C:\\Documents and Settings\\paul\\Desktop\\FullNames");
        //new MakeStrings("C:\\Documents and Settings\\paul\\Desktop\\FirstNames", "C:\\Documents and Settings\\paul\\Desktop\\Surnames", 500);
    }
}

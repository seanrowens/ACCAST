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
package Util;

import java.util.*;
import java.io.*;
import java.io.IOException;

// Java has no native CSV API; there are a variety of 'frameworks' out
// there, but come on guys, a FRAMEWORK for parsing CSV??  Although
// CSV is sadly more complicated than it appears to be, and this code
// really doesn't handle any of those complexities.  For instance
// @TODO: this class doesn't handle CSV 'quoting' at all.
//
// How this class SHOULD work, from;
//
// http://www.creativyst.com/Doc/Articles/CSV/CSV01.htm
//
// > The CSV File Format
// > 
// > * Each record is one line   ...but
// >   A record separator may consist of a line feed (ASCII/LF=0x0A), or a
// >   carriage return and line feed pair (ASCII/CRLF=0x0D 0x0A).
// >   ...but: fields may contain embedded line-breaks (see below) so a
// >           record may span more than one line.
// > 
// > * Fields are separated with commas.
// >   Example John,Doe,120 any st.,"Anytown, WW",08123
// > 
// > * Leading and trailing space-characters adjacent to comma field separators are ignored.
// >   So John , Doe ,... resolves to "John" and "Doe", etc. Space
// >   characters can be spaces, or tabs.
// > 
// > * Fields with embedded commas must be delimited with double-quote characters.
// >   In the above example. "Anytown, WW" had to be delimited in double
// >   quotes because it had an embedded comma.
// > 
// > * Fields that contain double quote characters must be surounded by
// >   double-quotes, and the embedded double-quotes must each be
// >   represented by a pair of consecutive double quotes.
// > 
// >   So, John "Da Man" Doe would convert to "John ""Da Man""",Doe, 120 any st.,...
// > 
// > * A field that contains embedded line-breaks must be surounded by double-quotes
// >   So:
// >     Field 1: Conference room 1  
// >     Field 2:
// >       John,
// >       Please bring the M. Mathers file for review  
// >       -J.L.
// >     Field 3: 10/18/2002
// >      ...
// > 
// >   would convert to:
// >     Conference room 1, "John,  
// >     Please bring the M. Mathers file for review  
// >     -J.L.
// >     ",10/18/2002,...
// > 
// >   Note that this is a single CSV record, even though it takes up more
// >   than one line in the CSV file. This works because the line breaks
// >   are embedded inside the double quotes of the field.
// > 
// >           Implementation note: In Excel, leading spaces between the
// >           comma used for a field sepparator and the double quote will
// >           sometimes cause fields to be read in as unquoted fields,
// >           even though the first non-space character is a double
// >           quote. To avoid this quirk, simply remove all leading spaces
// >           after the field-sepparator comma and before the double quote
// >           character in your CSV export files.
// > 
// > * Fields with leading or trailing spaces must be delimited with double-quote characters.
// >   So to preserve the leading and trailing spaces around the last name above: John ,"   Doe   ",...
// > 
// >           Usage note: Some applications will insist on helping you by
// >           removing leading and trailing spaces from all fields
// >           regardless of whether the CSV used quotes to preserve
// >           them. They may also insist on removing leading zeros from
// >           all fields regardless of whether you need them. One such
// >           application is Excel. :-( For some help with this quirk, see
// >           the section below entitled Excel vs. Leading Zero & Space.
// > 
// > * Fields may always be delimited with double quotes.
// >   The delimiters will always be discarded.
// > 
// >           Implementation note: When importing CSV, do not reach down a
// >           layer and try to use the quotes to impart type information
// >           to fields. Also, when exporting CSV, you may want to be
// >           defensive of apps that improperly try to do this. Though, to
// >           be honest, I have not found any examples of applications
// >           that try to do this. If you have encountered any apps that
// >           attempt to use the quotes to glean type information from CSV
// >           files (like assuming quoted fields are strings even if they
// >           are numeric), please let me know about it.
// > 
// > * The first record in a CSV file may be a header record containing column (field) names
// >   There is no mechanism for automatically discerning if the first
// >   record is a header row, so in the general case, this will have to be
// >   provided by an outside process (such as prompting the user). The
// >   header row is encoded just like any other CSV record in accordance
// >   with the rules above. A header row for the multi-line example above,
// >   might be:
// > 
// >    Location, Notes, "Start Date", ...


public class CSV {

    private static final char DQ = '"';
    private static final char ESC = '\\';

    private static String trimChar(String field, char trimChar) {
	boolean noSubstring = true;
	if(field == null)
	    return null;

	int start = 0;
	while(field.charAt(start) == trimChar) {
	    start++;
	    noSubstring = true; 
	}
	int end = field.length();
	while(field.charAt(end) == trimChar) {
	    end--;
	    noSubstring = true; 
	}
	if(noSubstring)
	    return field;

	return field.substring(start,end);
    }

    public static ArrayList<String> parseLine(String list, char delim) {
	if(list == null)
	    return null;
	if(list.equals(""))
	    return null;

	ArrayList<String> fieldList = new ArrayList<String>();

	// Copy list into a char array.
	char listChars[];
	listChars = new char[list.length()];
	list.getChars(0, list.length(), listChars, 0);

	int count = 0;
	int itemStart = 0;
	int itemEnd = 0;
	String newItem = null;

	while(count < listChars.length) {
	    boolean quotedFlag = false;
	    count = itemEnd;
	    itemStart = count;
	    itemEnd = itemStart;
	    while(itemEnd < listChars.length) {
		// skip over double quoted values
		if(DQ == listChars[itemEnd]) {
		    quotedFlag = true;
                    while(itemEnd < listChars.length) {
			itemEnd++;
                        if(DQ == listChars[itemEnd]) {
                            if(ESC != listChars[itemEnd-1])
                                break;
                        }
                    }
                }

		if(delim == listChars[itemEnd])
		    break;
		itemEnd++;
	    }
	    newItem = new String(listChars, itemStart, itemEnd - itemStart);
	    itemEnd++;
	    count = itemEnd;
	    if(quotedFlag) {
		newItem = trimChar(newItem.trim(),DQ);
	    }
	    newItem = newItem.trim().intern();
	    fieldList.add(newItem);
	}

	return fieldList;
    }

    public static String[] parseLine2Ary(String list, char delim) {
	ArrayList<String> fieldList = parseLine(list, delim);
	return fieldList.toArray(new String[1]);
    }

    // Returns null for eof
    public static ArrayList<String> parseNextLine(BufferedReader bufferedReader, char delim) {
	try {
	    String line = bufferedReader.readLine();
	    if(null == line)
		return null;
	    return parseLine(line, delim);
	}
	catch(IOException e){
	    // Now what?  Do we only get this for real errors, or do we get it for EOF?
	    System.err.println("IOException reading file, e="+e);
	    e.printStackTrace();
	}
	return null;
    }

    public static ArrayList<ArrayList<String>> parseFile(String filename, char delim) {
	ArrayList<ArrayList<String>> listOfLines = new ArrayList<ArrayList<String>>();
	FileReader fileReader = null;
	BufferedReader bufferedReader = null;
	try {
	    fileReader = new FileReader(filename);
	    bufferedReader = new BufferedReader(fileReader);
	    while(true) {
		String line = bufferedReader.readLine();
		if(null == line)
		    break;
		listOfLines.add(parseLine(line, delim));
	    }
	    bufferedReader.close();
	}
	catch(IOException e){
	    // Now what?  Do we only get this for real errors, or do we get it for EOF?
	    System.err.println("IOException reading file "+filename+", e="+e);
	    e.printStackTrace();
	    return null;
	}
	return listOfLines;
    }
}

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
package Gui;

// @author      Sean R. Owens
// @version     $Id: StringUtils.java,v 1.3 2007/07/27 02:00:05 owens Exp $ 

import java.util.*;

public class StringUtils {

  public static boolean isWhiteSpace(char c) {
    if (c == ' ')  return (true);
    if (c == '\n') return (true);
    if (c == '\t') return (true);
    return false ;
  }

  public static void printErrMsg(String methodName, String errorMsg, int mCount, char message[]) {
      System.err.println("ERROR: "+methodName+": "+errorMsg);
      System.err.print("ERROR: "+methodName+": msg=\\");
      for(int loopi = 0; loopi < message.length; loopi++) {
	  System.err.print(message[loopi]);
      }
      System.err.println("\\");
      System.err.print("ERROR: "+methodName+":      ");
      for(int i = 0; i < mCount; i++)
	  System.err.print(" ");
      System.err.println("^");
  }

    // if errMsg != null, then we test if we've run past end of message
    // and if so, printErrMsg(errMsg), and return -1.  If no error then
    // we return the mCount indexing the next non-whitespace char.
    public static int skipWhiteSpace(int mCount, char messageChars[], String errMsg) {
	//Skip whitespace
	while(mCount < messageChars.length) {
	    if(messageChars[mCount] == ' ' ||messageChars[mCount] == '\n' ||messageChars[mCount] == '\t') {
		mCount++;
	    }
	    else
		break;
	}
	if(errMsg != null)
	    if(mCount >= messageChars.length) {
		printErrMsg("RString.skipWhiteSpace", errMsg, mCount, messageChars);
		return -1;
	    }
	return mCount;
    }

    // Parse a string with a known number of space separated doubles
    // into an array of doubles.
    public static double[] parseDoubleList(int numDoubles, String list) {
	if(list == null)
	    return null;
	if(list.equals(""))
	    return null;

	double[] returnArray = new double[numDoubles];
	int returnArrayCount = 0;

	// Copy list into a char array.
	char listChars[];
	listChars = new char[list.length()];
	list.getChars(0, list.length(), listChars, 0);
	int listLength = listChars.length;
	
	int count = 0;
	int itemStart = 0;
	int itemEnd = 0;
	int itemLength = 0;
	String newItem = null;

	int lastStart = 0;
	int lastEnd = 0;
	int lastLength = 0;
	int cacheHitCount = 0;
	boolean cacheHit = false;

	while(count < listLength) {
	    // Skip any leading whitespace
	    itemEnd = skipWhiteSpace(count, listChars, null);
	    count = itemEnd;
	    if(count >= listLength)
		break;
	    itemStart = count;
	    itemEnd = itemStart;
	    while(itemEnd < listLength) {
		if((listChars[itemEnd] != ' ') && (listChars[itemEnd] != '\n') && (listChars[itemEnd] != '\t')) {
		    itemEnd++;
		}
		else
		    break;
	    }
	    itemLength = itemEnd - itemStart;
	    cacheHit = false;
	    if(itemLength == lastLength) {
		if(6 == itemLength) {
		    if((listChars[lastStart] == listChars[itemStart]) 
		       && (listChars[lastStart+1] == listChars[itemStart+1])
		       && (listChars[lastStart+2] == listChars[itemStart+2])
		       && (listChars[lastStart+3] == listChars[itemStart+3])
 		       && (listChars[lastStart+4] == listChars[itemStart+4])
		       && (listChars[lastStart+5] == listChars[itemStart+5])) {
			cacheHit = true;
			returnArray[returnArrayCount] = returnArray[returnArrayCount-1];
			returnArrayCount++;
			cacheHitCount++;
		    }
		} 
	    }
	    if(!cacheHit)
		returnArray[returnArrayCount++] = Double.parseDouble(new String(listChars, itemStart, itemLength));

	    count = itemEnd;
	    lastStart = itemStart;
	    lastEnd = itemEnd;
	    lastLength = itemLength;
	}
	//	Debug.debug("CacheHitCount ="+cacheHitCount);
	return returnArray;
    }


    // Parse a string with a known number of space separated doubles
    // into an array of doubles - very quickly.
    //
    // This very gnarly version of parseDoubleList below with hand
    // coded double parsing seems to be about 2.5 times faster.
    // Scary.  I ran a test with this routine against the 'safe'
    // routine above, comparing the numbers resulting, and they're
    // identical.  Note that this fast version only handles numbers
    // formed like; [-]digit*[.digit*] with a maximum of 10 digits
    // after the decimal point.  (But making it handle more digits
    // after the decimal place should be easy, just increase the size
    // of the tenPowers array.)
    public static void parseDoubleListFast(int numDoubles, double returnArray[], String list) {
	if(list == null)
	    return;
	if(list.equals(""))
	    return;

	int returnArrayCount = 0;

	// Copy list into a char array.
	char listChars[];
	listChars = new char[list.length()];
	list.getChars(0, list.length(), listChars, 0);

	int count = 0;
	int itemStart = 0;
	int itemEnd = 0;
	String newItem = "0.0";
	int accum = 0;
	boolean negative = false;
	boolean parsingFraction = false;
	int fractionAccum = 0;
	int fractionDecimalPlaces = 0;
	double result = 0.0;
	double tenPowers[]= {
	    1,
	    10,
	    100,
	    1000,
	    10000,
	    100000,
	    1000000,
	    10000000,
	    100000000,
	    1000000000
	};
	char nextChar;
	
	while(count < listChars.length) {
	    // Skip any leading whitespace

	    while(count < listChars.length) {
		if(listChars[count] == ' ' ||listChars[count] == '\n' ||listChars[count] == '\t') {
		    count++;
		}
		else
		    break;
	    }
	    if(count >= listChars.length)
		break;

	    accum = 0;
	    fractionAccum = 0;
	    fractionDecimalPlaces = 0;
	    negative = false;
	    parsingFraction = false;
	    while(count < listChars.length) {
		nextChar = listChars[count];
		if(nextChar == ' ' ||nextChar == '\n' ||nextChar == '\t') 
		    break;

		if(nextChar == '-') {
		    negative = true;
		}
		else if (nextChar == '.') {
		    parsingFraction = true;
		}
		else {
		    if(false == parsingFraction) {
			if (nextChar == '0') {
			    accum = (accum * 10);
			}
			else if (nextChar == '1') {
			    accum = (accum * 10) + 1;
			}
			else if (nextChar == '2') {
			    accum = (accum * 10) + 2;
			}
			else if (nextChar == '3') {
			    accum = (accum * 10) + 3;
			}
			else if (nextChar == '4') {
			    accum = (accum * 10) + 4;
			}
			else if (nextChar == '5') {
			    accum = (accum * 10) + 5;
			}
			else if (nextChar == '6') {
			    accum = (accum * 10) + 6;
			}
			else if (nextChar == '7') {
			    accum = (accum * 10) + 7;
			}
			else if (nextChar == '8') {
			    accum = (accum * 10) + 8;
			}
			else if (nextChar == '9') {
			    accum = (accum * 10) + 9;
			}
			else 
			    break;
		    }
		    else {
			fractionDecimalPlaces++;
			if (nextChar == '0') {
			    fractionAccum = (fractionAccum * 10);
			}
			else if (nextChar == '1') {
			    fractionAccum = (fractionAccum * 10) + 1;
			}
			else if (nextChar == '2') {
			    fractionAccum = (fractionAccum * 10) + 2;
			}
			else if (nextChar == '3') {
			    fractionAccum = (fractionAccum * 10) + 3;
			}
			else if (nextChar == '4') {
			    fractionAccum = (fractionAccum * 10) + 4;
			}
			else if (nextChar == '5') {
			    fractionAccum = (fractionAccum * 10) + 5;
			}
			else if (nextChar == '6') {
			    fractionAccum = (fractionAccum * 10) + 6;
			}
			else if (nextChar == '7') {
			    fractionAccum = (fractionAccum * 10) + 7;
			}
			else if (nextChar == '8') {
			    fractionAccum = (fractionAccum * 10) + 8;
			}
			else if (nextChar == '9') {
			    fractionAccum = (fractionAccum * 10) + 9;
			}
			else 
			    break;
		    }
		}
		count++;
	    }
	    result = (double)accum + (((double)fractionAccum)/tenPowers[fractionDecimalPlaces]);

	    if(negative)
		result = -result;
	    returnArray[returnArrayCount++] = result;
	}
    }

    // Parse a string with a known number of space separated floats
    // into an array of floats.
    public static float[] parseFloatList(int numFloats, String list) {
	if(list == null)
	    return null;
	if(list.equals(""))
	    return null;

	float[] returnArray = new float[numFloats];
	int returnArrayCount = 0;

	// Copy list into a char array.
	char listChars[];
	listChars = new char[list.length()];
	list.getChars(0, list.length(), listChars, 0);

	int count = 0;
	int itemStart = 0;
	int itemEnd = 0;
	String newItem = null;

	while(count < listChars.length) {
	    // Skip any leading whitespace
	    itemEnd = skipWhiteSpace(count, listChars, null);
	    count = itemEnd;
	    if(count >= listChars.length)
		break;
	    itemStart = count;
	    itemEnd = itemStart;
	    while(itemEnd < listChars.length) {
		if(!isWhiteSpace(listChars[itemEnd])) {
		    itemEnd++;
		}
		else
		    break;
	    }
	    returnArray[returnArrayCount++] = Float.parseFloat(new String(listChars, itemStart, itemEnd - itemStart));
	    count = itemEnd;
	}
	return returnArray;
    }

    // Parse a string with a known number of space separated numbers
    // into an array of longs.
    public static float[] parseLongList(int numLongs, String list) {
	if(list == null)
	    return null;
	if(list.equals(""))
	    return null;

	float[] returnArray = new float[numLongs];
	int returnArrayCount = 0;

	// Copy list into a char array.
	char listChars[];
	listChars = new char[list.length()];
	list.getChars(0, list.length(), listChars, 0);

	int count = 0;
	int itemStart = 0;
	int itemEnd = 0;
	String newItem = null;

	while(count < listChars.length) {
	    // Skip any leading whitespace
	    itemEnd = skipWhiteSpace(count, listChars, null);
	    count = itemEnd;
	    if(count >= listChars.length)
		break;
	    itemStart = count;
	    itemEnd = itemStart;
	    while(itemEnd < listChars.length) {
		if(!isWhiteSpace(listChars[itemEnd])) {
		    itemEnd++;
		}
		else
		    break;
	    }
	    returnArray[returnArrayCount++] = Long.parseLong(new String(listChars, itemStart, itemEnd - itemStart));
	    count = itemEnd;
	}
	return returnArray;
    }

    // Parse a string with a known number of space separated numbers
    // into an array of ints.
    public static int[] parseIntList(int numInts, String list) {
	if(list == null)
	    return null;
	if(list.equals(""))
	    return null;

	int[] returnArray = new int[numInts];
	int returnArrayCount = 0;

	// Copy list into a char array.
	char listChars[];
	listChars = new char[list.length()];
	list.getChars(0, list.length(), listChars, 0);
	int listLength = listChars.length;

	int count = 0;
	int itemStart = 0;
	int itemEnd = 0;
	String newItem = null;

	while(count < listLength) {
	    // Skip any leading whitespace
	    itemEnd = skipWhiteSpace(count, listChars, null);
	    count = itemEnd;
	    if(count >= listLength)
		break;
	    itemStart = count;
	    itemEnd = itemStart;
	    while(itemEnd < listLength) {
		if((listChars[itemEnd] != ' ') && (listChars[itemEnd] != '\n') && (listChars[itemEnd] != '\t')) {
		    itemEnd++;
		}
		else
		    break;
	    }
	    int diff = itemEnd - itemStart;
	    if((2 ==  diff) && (listChars[itemStart] == '1') && (listChars[itemStart+1] == '4'))
		returnArray[returnArrayCount++] = 14;
	    else if((1 ==  diff) && (listChars[itemStart] == '2'))
		returnArray[returnArrayCount++] = 2;
	    else if((2 ==  diff) && (listChars[itemStart] == '1') && (listChars[itemStart+1] == '0'))
		returnArray[returnArrayCount++] = 10;
	    else if(1 ==  diff) {
		if(listChars[itemStart] == '4')
		    returnArray[returnArrayCount++] = 4;
		else if(listChars[itemStart] == '0')
		    returnArray[returnArrayCount++] = 0;
		else if(listChars[itemStart] == '1')
		    returnArray[returnArrayCount++] = 1;
		else if(listChars[itemStart] == '3')
		    returnArray[returnArrayCount++] = 3;
		else if(listChars[itemStart] == '5')
		    returnArray[returnArrayCount++] = 5;
		else if(listChars[itemStart] == '6')
		    returnArray[returnArrayCount++] = 6;
		else if(listChars[itemStart] == '7')
		    returnArray[returnArrayCount++] = 7;
		else if(listChars[itemStart] == '8')
		    returnArray[returnArrayCount++] = 8;
		else if(listChars[itemStart] == '9')
		    returnArray[returnArrayCount++] = 9;
		else
		    returnArray[returnArrayCount++] = Integer.parseInt(new String(listChars, itemStart, itemEnd - itemStart));
	    }
	    else if (2 ==  diff) {
		if((listChars[itemStart] == '1') && (listChars[itemStart+1] == '1'))
		    returnArray[returnArrayCount++] = 11;
		else if((listChars[itemStart] == '1') && (listChars[itemStart+1] == '2'))
		    returnArray[returnArrayCount++] = 12;
		else if((listChars[itemStart] == '1') && (listChars[itemStart+1] == '3'))
		    returnArray[returnArrayCount++] = 13;
		else if((listChars[itemStart] == '1') && (listChars[itemStart+1] == '5'))
		    returnArray[returnArrayCount++] = 15;
		else
		    returnArray[returnArrayCount++] = Integer.parseInt(new String(listChars, itemStart, itemEnd - itemStart));
	    }
	    count = itemEnd;
	}
	return returnArray;
    }

    // Parse a string with a known number of digits (no spaces between
    // them - stuck together, i.e. 333224455) into an array of
    // ints. (i.e. the example above would become int foo[] =
    // {3,3,3,2,2,4,4,5,5};
    public static int[] parseDigitList(int numDigits, String list) {
	if(list == null)
	    return null;
	if(list.equals(""))
	    return null;

	int[] returnArray = new int[numDigits+1];
	int returnArrayCount = 0;

	for(int loopi = 0; loopi < numDigits; loopi++) {
	    returnArray[loopi] = list.charAt(loopi) - '0';
	}

	return returnArray;
    }

    public static String[] parseList(char delim, String list) {
	if(list == null)
	    return null;
	if(list.equals(""))
	    return null;

	Vector returnVec = new Vector();
	String[] returnArray = null;

	// Copy list into a char array.
	char listChars[];
	listChars = new char[list.length()];
	list.getChars(0, list.length(), listChars, 0);

	int count = 0;
	int itemStart = 0;
	int itemEnd = 0;
	String newItem = null;

	while(count < listChars.length) {
	    count = itemEnd;
	    if(count >= listChars.length)
		break;
	    itemStart = count;
	    itemEnd = itemStart;
	    while(itemEnd < listChars.length) {
		if(delim != listChars[itemEnd]) {
		    itemEnd++;
		}
		else
		    break;
	    }
	    newItem = new String(listChars, itemStart, itemEnd - itemStart);
	    itemEnd++;
	    count = itemEnd;
	    returnVec.add(newItem);
	}
	// Convert from vector to array, and return it.
	returnArray = new String[1];
	returnArray = (String[])returnVec.toArray((Object[])returnArray);
	return returnArray;
    }

    public static int[] parseIntFixedField(String list, int returnArray[], int starts[], int ends[]) {
	if(list == null)
	    return null;
	if(list.equals(""))
	    return null;
	if(null == starts)
	    return null;
	if(null == ends)
	    return null;
	if(starts.length != ends.length)
	    return null;

	int returnArrayCount = 0;
	for(int loopi = 0; loopi < returnArray.length; loopi++) 
	    returnArray[loopi] = 0;

	// Copy list into a char array.
	char listChars[];
	listChars = new char[list.length()];
	list.getChars(0, list.length(), listChars, 0);
	int listLength = listChars.length;

	int count = 0;
	int itemStart = 0;
	int itemEnd = 0;
	String newItem = null;

	for(int loopi = 0; loopi < starts.length; loopi++) {
	    itemStart = starts[loopi];
	    if(itemStart >= listLength)
		break;
	    itemEnd = ends[loopi];
	    int diff = itemEnd - itemStart;
	    returnArray[returnArrayCount] = Integer.parseInt(new String(listChars, itemStart, diff));
	    if(0 == returnArray[returnArrayCount])
		break;
	    returnArrayCount++;
	}
	return returnArray;
    }
}

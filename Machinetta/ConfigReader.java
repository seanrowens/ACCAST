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
package Machinetta;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;

/**
 * ConfigReader.java
 * 
 * Given an object and a file, this class will read the file and update public static variables
 * based on the configuration file
 * 
 * E.g., if the config file has BALH 2 and the obj o has a public static field "BLAH" the
 * value of that field will be set to 2.
 * 
 * Created on September 15, 2003, 3:03 PM
 * @author pscerri
 */
public class ConfigReader {
    
    /**
     * Read the config file and set parameters
     * @param o Object on which the fields will be set
     * @param configFile The file from which the configuration parameters will be read
     */
    @SuppressWarnings("unchecked")

    private static void setField(Object o, String field, String value, String source, HashMap<String,String> notFoundMap) {
	try {
	    Field f = o.getClass().getField(field);
	    //Machinetta.Debugger.debug(1,"Field " + f + " has type " + f.getType());
                            
	    if (f.getType().toString().equalsIgnoreCase("int")) {
		f.setInt(o, (new Integer(value)).intValue());
	    } else if (f.getType().toString().equalsIgnoreCase("double")) {
		f.setDouble(o, (new Double(value)).doubleValue());
	    } else if (f.getType().toString().endsWith("String")) {
		f.set(o, value);
	    } else if (f.getType().toString().endsWith("boolean")) {
		f.setBoolean(o, (new Boolean(value)).booleanValue());
	    } else if (f.getType().isEnum()) {
		Machinetta.Debugger.debug(5, "TRYING TO READ ENUM from config!!!");                                
	    } else {
                // This used to be a problem, but now is routine, given the hashtable
		Machinetta.Debugger.debug(0, "Unknown parameter type in "+source+" : " + f.getType());
	    }
	} catch (NoSuchFieldException e1) {
	    Machinetta.Debugger.debug(1, "Unknown parameter in "+source+" " + field+" adding to 'notFound' and 'all' Maps.");
	    if(null != notFoundMap) 
		notFoundMap.put(field, value);
	} catch (SecurityException e2) {
	    Machinetta.Debugger.debug(3, "Error setting field " + field + " : " + e2);
	} catch (IllegalAccessException e3) {
	    Machinetta.Debugger.debug(3, "Error setting field " + field + " : " + e3);
	}
    }

    public static void readConfig (Object o, String configFile) {
	HashMap<String,String> allMap = null;
	HashMap<String,String> notFoundMap = null;

	try {
	    Field allMapField = o.getClass().getField("allMap");
	    allMap = (HashMap<String,String>)allMapField.get(new HashMap());
        } catch (Exception e) {
            Machinetta.Debugger.debug(3, "No allMap available in config object.");
        }
	try {
	    Field notFoundMapField = o.getClass().getField("notFoundMap");
	    notFoundMap = (HashMap<String,String>)notFoundMapField.get(new HashMap());
        } catch (Exception e) {
            Machinetta.Debugger.debug(3, "No notFoundMap available in config object.");
        }

        /*
	if(null != allMap) {
            Machinetta.Debugger.debug(0,"we found an allMap HashMap.");
	}
	if(null != notFoundMap) {
            Machinetta.Debugger.debug(0,"we found a notFoundMap HashMap.");
	}
        */
        
        Machinetta.Debugger.debug(1, "Reading config file : " + configFile);
        try {
            BufferedReader in = new BufferedReader(new FileReader(configFile));
            String line = in.readLine();
            while (line != null) {
		if (!line.startsWith("#")) {      
		    int splitPoint = line.indexOf(' ');
		    if (splitPoint > 0) {
			String field = line.substring(0,splitPoint).trim().intern();
			String value = line.substring(splitPoint).trim().intern();
			if(null != allMap) 
			    allMap.put(field, value);

			Machinetta.Debugger.debug(1, "Setting " + field + " to " + value);
			setField(o, field, value, "config file", notFoundMap);
                    }
                }
                line = in.readLine();
            }
        } catch (Exception e) {
            Machinetta.Debugger.debug(3, "Config file read failed : " + e.toString());
            Machinetta.Debugger.debug(3, "Using default configuration : " + e.toString());
        }
    }

    public static void extraValues (Object o, HashMap<String, String> extraConfigValues) {
	HashMap<String,String> allMap = null;
	HashMap<String,String> notFoundMap = null;

	try {
	    Field allMapField = o.getClass().getField("allMap");
	    allMap = (HashMap<String,String>)allMapField.get(new HashMap());
        } catch (Exception e) {
            Machinetta.Debugger.debug(5, "No allMap available in config object.");
        }
	try {
	    Field notFoundMapField = o.getClass().getField("notFoundMap");
	    notFoundMap = (HashMap<String,String>)notFoundMapField.get(new HashMap());
        } catch (Exception e) {
            Machinetta.Debugger.debug(5, "No notFoundMap available in config object.");
        }

	for(String fieldName: extraConfigValues.keySet()) {
	    String fieldValue = extraConfigValues.get(fieldName);
	    if(null != allMap) 
		allMap.put(fieldName, fieldValue);

	    try {
		setField(o, fieldName, fieldValue, "extra config values map", notFoundMap);
	    } catch (SecurityException e2) {
		Machinetta.Debugger.debug(3, "Error setting field " + fieldName + " : " + e2);
	    } catch (Exception e) {
		Machinetta.Debugger.debug(3, "Invalid parameter in extraConfigValues map : " + e.toString());
		Machinetta.Debugger.debug(3, "Using default configuration : " + e.toString());
	    }
	}
    }

    public static String getConfig(String fieldName, String defaultValue, boolean errorMsgForFieldNotFound) {
	String fieldValue = null;
	HashMap<String,String> allMap = Configuration.allMap;
// 	try {
// 	    Field allMapField = configuration.getClass().getField("allMap");
// 	    allMap = (HashMap<String,String>)allMapField.get(new HashMap());
//         } catch (Exception e) {
//             Machinetta.Debugger.debug("allMap is null in config object.", 0, "ConfigReader");
//         }
	
	fieldValue = allMap.get(fieldName);
	Machinetta.Debugger.debug(1, "allMap value for "+fieldName+" = "+fieldValue);

	if(null == fieldValue) {
	    if(errorMsgForFieldNotFound)
		Machinetta.Debugger.debug(1, "Couldn't find field named "+fieldName+" in allMap in config object, using default="+defaultValue);
	    return defaultValue;
	}
	return fieldValue;
    }

    public static int getConfig(String fieldName, int defaultValue, boolean errorMsgForFieldNotFound) {
	int fieldValueInt = 0;
	String fieldValue = getConfigString(fieldName,null,false);
	if(null == fieldValue) {
	    if(errorMsgForFieldNotFound)
		Machinetta.Debugger.debug(1, "Couldn't find field named "+fieldName+" in allMap in config object, using default="+defaultValue);
	    return defaultValue;
	}
	fieldValueInt = Integer.parseInt(fieldValue);
	return fieldValueInt;
    }

    public static long getConfig(String fieldName, long defaultValue, boolean errorMsgForFieldNotFound) {
	long fieldValueLong = 0;
	String fieldValue = getConfigString(fieldName,null,false);
	if(null == fieldValue) {
	    if(errorMsgForFieldNotFound)
		Machinetta.Debugger.debug(1, "Couldn't find field named "+fieldName+" in allMap in config object, using default="+defaultValue);
	    return defaultValue;
	}
	fieldValueLong = Long.parseLong(fieldValue);
	return fieldValueLong;
    }

    public static double getConfig(String fieldName, double defaultValue, boolean errorMsgForFieldNotFound) {
	double fieldValueDouble = 0;
	String fieldValue = getConfigString(fieldName,null,false);
	if(null == fieldValue) {
	    if(errorMsgForFieldNotFound)
		Machinetta.Debugger.debug(1, "Couldn't find field named "+fieldName+" in allMap in config object, using default="+defaultValue);
	    return defaultValue;
	}
	fieldValueDouble = Double.parseDouble(fieldValue);
	return fieldValueDouble;
    }

    public static boolean getConfig(String fieldName, boolean defaultValue, boolean errorMsgForFieldNotFound) {
	boolean fieldValueBoolean = false;
	String fieldValue = getConfigString(fieldName,null,false);
	if(null == fieldValue) {
	    if(errorMsgForFieldNotFound)
		Machinetta.Debugger.debug(1, "Couldn't find field named "+fieldName+" in allMap in config object, using default="+defaultValue);
	    return defaultValue;
	}
	fieldValueBoolean = Boolean.parseBoolean(fieldValue);
	return fieldValueBoolean;
    }

    public static String getConfigString(String fieldName, String defaultValue, boolean errorMsgForFieldNotFound) {
	return getConfig(fieldName,defaultValue,errorMsgForFieldNotFound);
    }
    public static int getConfigInt(String fieldName, int defaultValue, boolean errorMsgForFieldNotFound) {
	return getConfig(fieldName,defaultValue,errorMsgForFieldNotFound);
    }
    public static double getConfigDouble(String fieldName, double defaultValue, boolean errorMsgForFieldNotFound) {
	return getConfig(fieldName,defaultValue,errorMsgForFieldNotFound);
    }
    public static boolean getConfigBoolean(String fieldName, boolean defaultValue, boolean errorMsgForFieldNotFound) {
	return getConfig(fieldName,defaultValue,errorMsgForFieldNotFound);
    }
}

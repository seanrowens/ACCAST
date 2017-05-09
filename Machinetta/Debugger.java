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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * @author iwarfiel
 */
public class Debugger
{
	private static final String LOGGER_NAME = "Debugger";

	// we want to use log4j, but not if it isn't on the classpath
	// also, save the reflection info, rather than loading it over and over
	private static Class<?> logger_class;
	private static Method trace_method;
	private static Method debug_method;
	private static Method info_method;
	private static Method warn_method;
	private static Method error_method;
	private static Method fatal_method;
	private static Object logger;
	static
	{
		try
		{
			// load log4j, if it exists
			logger_class = Class.forName("org.apache.log4j.Logger");

			// get each method
			trace_method = logger_class.getMethod("trace", Object.class);
			debug_method = logger_class.getMethod("debug", Object.class);
			info_method = logger_class.getMethod("info", Object.class);
			warn_method = logger_class.getMethod("warn", Object.class);
			error_method = logger_class.getMethod("error", Object.class);
			fatal_method = logger_class.getMethod("fatal", Object.class);

			// get the logger
			Method getLogger = logger_class.getMethod("getLogger", String.class);
			logger = getLogger.invoke(null, LOGGER_NAME);
		}
		catch (ClassNotFoundException cnfe)
		{
			// this could happen if log4j isn't present, so don't print anything
		}
		catch (NoSuchMethodException nsme)
		{
			// should normally never happen
			nsme.printStackTrace();
		}
		catch (InvocationTargetException ite)
		{
			// should normally never happen
			ite.printStackTrace();
		}
		catch (IllegalArgumentException iae)
		{
			// should normally never happen
			iae.printStackTrace();
		}
		catch (IllegalAccessException iae)
		{
			// should normally never happen
			iae.printStackTrace();
		}

		// @TODO HACK HACK
		logger = null;

		if (logger == null)
			System.err.println("Unable to initialize log4j; using stdout instead");
	}

	/**
	 * Debug level very routine. This type of message will typically not be output.
	 * Basically a way of commenting debugs out.
	 */
	public static final int DL_VERY_ROUTINE = -1;

	/**
	 * Debug level routine. This type of message will only be printed in the most
	 * verbose cases, e.g., step in path planner Use inside loops etc. for things that
	 * are not important to understanding what the proxy did, only understanding how
	 * some particular algorithm worked in detail.
	 */
	public static final int DL_ROUTINE = 0;

	/**
	 * Debug level step. Something has happened that might be of interest to the
	 * developers, e.g., a message sent. Expect many of these as they will indicate
	 * everything that the proxy does.
	 */
	public static final int DL_STEP = 1;

	/**
	 * Debug level major step. Something significant has happened, e.g., a role has been
	 * accepted or plan initiated. Only messages at this level and above will likely be
	 * output for anyone other than developers.
	 */
	public static final int DL_MAJOR_STEP = 2;

	/**
	 * Debug level warning. Something went wrong, but nothing the proxy can't handle,
	 * e.g., the planner might have to rapidly replan because something was wrong with
	 * the last plan or a failed communication. Expect that someone looking at these
	 * messages will be trying to work out what went wrong with the behavior when things
	 * didn't crash, but didn't work out as hoped. Alternative names, same basic
	 * meaning: DL_WARNING or DL_PROBLEM
	 */
	// public static final int DL_WARNING = 3;
	public static final int DL_PROBLEM = 3;

	/**
	 * Debug level serious problem. Something is seriously wrong that is likely to lead
	 * to the proxy falling apart, but the proxy will keep trying to go ahead. E.g., No
	 * other proxies could be found or the specified RAPInterface wasn't there. There
	 * shouldn't be many of these in a logfile, they are pretty serious. If you see a
	 * message with this level in the logfile, then fix it, don't expect the proxy to
	 * keep working, even if it did once.
	 */
	public static final int DL_SERIOUS_PROBLEM = 4;

	/**
	 * Debug level fatal problem. The should only appear at the end of a logfile, just
	 * before the proxy dies. Only put these messages when the proxy is highly likely to
	 * fail immediately. E.g., when there is no belief about RAP in initial belief file
	 * the proxy can do nothing and will print a message and stop.
	 */
	public static final int DL_FATAL_PROBLEM = 5;

	/**
	 * Messages with priority at or above this will be printed.
	 */
	private static int DebugLevel = DL_STEP;

	public Debugger()
	{
		DebugLevel = Configuration.DEBUG_LEVEL;
	}

	public static void setDebugLevel(int level)
	{
		DebugLevel = level;
	}

	public static int getDebugLevel()
	{
		return DebugLevel;
	}

	/**
	 * Prints msg to screen if level > DebugLevel
	 * 
	 * @param msg Message to be printed
	 * @param level Importance of message, higher more important
	 * @param callee Object which wants to write the message
	 * @deprecated
	 */
	@Deprecated
	public static void debug(String msg, int level, Object callee)
	{
		_debug(level, msg);
	}

	/**
	 * Prints msg to screen if level >= DebugLevel
	 * 
	 * @param msg Message to be printed
	 * @param level Importance of message, higher more important
	 * @param callee String representing callee Class
	 * @deprecated
	 */
	@Deprecated
	public static void debug(String msg, int level, String callee)
	{
		_debug(level, msg);
	}

	private static final String[] prefixes =
	{
		".....",
		"....",
		"   > ",
		" > ",
		" * ",
		"****"
	};

	public static void debug(int priority, String message)
	{
		_debug(priority, message);
	}

	private static void _debug(int priority, String message)
	{
		if (logger == null && priority < DebugLevel)
			return;

		String time = "" + System.currentTimeMillis();
		if (time.length() > 7)
			time = time.substring(time.length() - 7);

		String s = prefixes[Math.min(5, Math.max(0, priority))] + " " + time;

		StackTraceElement[] trace = (new Throwable()).getStackTrace();

		if (trace.length > 1)
		{
			String fn = trace[2].getFileName();
			fn = fn.substring(0, fn.length() - 5);
			s += " " + fn + " (" + trace[2].getLineNumber() + ")";
		}

		s += "\t  \"" + message + "\"";

		// use log4j?
		if (logger != null)
		{
			// log using reflection
			try
			{
				switch (priority)
				{
					case DL_VERY_ROUTINE:
					case DL_ROUTINE:
						trace_method.invoke(logger, s);
						break;

					case DL_STEP:
						debug_method.invoke(logger, s);
						break;

					case DL_MAJOR_STEP:
						info_method.invoke(logger, s);
						break;

					case DL_PROBLEM:
						warn_method.invoke(logger, s);
						break;

					case DL_SERIOUS_PROBLEM:
						error_method.invoke(logger, s);
						break;

					case DL_FATAL_PROBLEM:
						fatal_method.invoke(logger, s);
						break;
				}
			}
			catch (IllegalAccessException iae)
			{
				// should normally never happen
				iae.printStackTrace();
			}
			catch (IllegalArgumentException iae)
			{
				// should normally never happen
				iae.printStackTrace();
			}
			catch (InvocationTargetException ite)
			{
				// should normally never happen
				ite.printStackTrace();
			}
		}
		else
		{
			// log using stdout
			System.out.println(s);
		}
	}
}

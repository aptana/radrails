/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.rails.internal.ui.tail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.radrails.rails.ui.RailsUILog;
import org.rubypeople.rdt.ui.text.ansi.ANSIParser;
import org.rubypeople.rdt.ui.text.ansi.ANSIToken;

/**
 * Acts similar to the UNIX 'tail' command which allows watching a file
 * update in real time.
 *
 * @author	mbaumbach
 *
 * @version	0.5.0
 */
public class Tail {

    /**
     * The file to tail.
     */
    private File file;
    
    /**
     * Keeps track of the last time the file was modified.
     */
    private long fileLength;
    
    /**
     * The BufferedReader used to read the file.
     */
    private BufferedReader in;
    
    /**
     * The MessageConsole this tail is part of.
     */
    private MessageConsole console;
    
    /**
     * The current Device.
     */
    private Device dev = Display.getCurrent();
    
    /**
     * Specifies if the thread should continue running or not.
     */
    private boolean continueRunning;
    
    private ANSIParser parser;

    /**
     * The console output streams. One for each possible combination of font style and color
     */
	private Map<String, MessageConsoleStream> outputStreams;
    
    /**
     * The number of characters to display on opening a file.
     */
    private static final int NUM_CHARS_START = 400;
    
    /**
     * Creates a new Tail object for the specified filename.
     * 
     * @param   filename    The File to tail.
     */
    public Tail(File filename) {
        this.file = filename;
        fileLength = 0;
        continueRunning = false;
        parser = new ANSIParser();
    }
    
    /**
     * Prints out the file's contents in real time.
     */
    public void start(MessageConsole console) {
        // Setup the reader for the file and start the file length thread
        try {
            in = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
        	RailsUILog.logError("Error opening reader on tailed file", e);
        }
        this.console = console;
        continueRunning = true;
        
        skiptoLastNChars(NUM_CHARS_START);
        startFileLengthThread();
    }

	private void skiptoLastNChars(int n) {
		if (file.length() > n) {
        	try {
				in.skip(file.length() - n);
			} catch (IOException e) {
				RailsUILog.logError("Error skipping to end of tailed file", e);
			}
        }
	}
    
    /**
     * Reads the latest information from a file and outputs it to the screen.
     */
    private void read() {
        // As long as we have a reader for the file, read in the latest
    	if (in == null) return;
  
    	// looking up the right stream should be faster than creating a new one for each token
    	if (outputStreams == null) outputStreams = new HashMap<String, MessageConsoleStream>();
    	try {
    		List<ANSIToken> tokens;
    		while( (tokens = parser.parse(in.readLine())) != null ) {    			
    			MessageConsoleStream out = null;
    			for (ANSIToken token : tokens) {
    				out = getStream(token);
    				if (out != null) out.print(token.toString());
				}
    			if (out != null) out.println();
    		}
    	} catch (IOException e) {
    		RailsUILog.logError("Error reading from tailed file", e);
    	}
    }
    
    private MessageConsoleStream getStream(final ANSIToken token) {
    	if (outputStreams.containsKey(token.getAnsi())) {
			return outputStreams.get(token.getAnsi());
    	}
    	final MessageConsoleStream out = new MessageConsoleStream(console);
    	if (token.hasForegroundColor()) {
    		Display.getDefault().syncExec(new Runnable() {
    			public void run() {
    				out.setColor(new Color(dev, token.getForegroundRGB()));
    			}
    		});
    	}
    	if (token.hasFontStyle()) {
    		Display.getDefault().syncExec(new Runnable() {
    			public void run() {                				
    				out.setFontStyle(token.getFontStyle());
    			}
    		});
    	}
    	outputStreams.put(token.getAnsi(), out);
    	return out;
	}

	/**
     * Starts a thread that checks the length of a file. When the file size changes,
     * it calls read() so that the new changes are printed.
     */
    private void startFileLengthThread() {
        Thread t = new Thread(new Runnable() {
           public void run() {
               // Run forever
               while( continueRunning ) {
                   // If the file length has changed, update the file length and print the new info
                   if ( file.length() > fileLength ) {
                       fileLength = file.length();
                       read();
                   }
                   Thread.yield();
               }
           }
        });
        t.start();
    }
    
    /**
     * Stops the tail program.
     */
    public void stop() {
        continueRunning = false;
        // close the file input stream
        try {
			if (in != null) in.close();
		} catch (IOException e) {
			// ignore
		}
		// close the console output streams
		if (outputStreams == null) return;
		for (MessageConsoleStream out : outputStreams.values()) {
			try {
				if (out != null) out.close();
			} catch (IOException e) {
				// ignore
			}
		}		
		outputStreams = null;
    }
    
    public boolean isStopped() {
    	return !continueRunning;
    }
    
} // Tail
/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.radrails.server.core;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.radrails.rails.core.RailsLog;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Content handler for the server manager configuration file. Creates a new
 * server object for each <code>server</code> element and stores it in a
 * Collection for retrieval. The <code>type</code> attribute in the
 * <code>server</code> element determines which subclass of Server will be
 * instantiated.
 * 
 * @author mkent
 * 
 */
public class ServerManagerContentHandler implements ContentHandler {

	private StringBuffer data;
	private Collection<Server> servers;
	
	private String environment;
	private String port;
	private String name;
	private String host;
	private IProject project;
	private String type;

	public void endDocument() throws SAXException {
		// Do nothing
	}

	public void startDocument() throws SAXException {
		servers = new ArrayList<Server>();
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		for (int i = start; i < start + length; i++) {
			data.append(ch[i]);
		}
	}

	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		// Do nothing
	}

	public void endPrefixMapping(String prefix) throws SAXException {
		// Do nothing
	}

	public void skippedEntity(String name) throws SAXException {
		// Do nothing
	}

	public void setDocumentLocator(Locator locator) {
		// Do nothing
	}

	public void processingInstruction(String target, String data)
			throws SAXException {
		// Do nothing
	}

	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		// Do nothing
	}

	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		if (qName.equals("project")) {			
			project = findProject(data.toString());			
		} else if (qName.equals("name")) {
			name = data.toString();
		} else if (qName.equals("port")) {
			port = data.toString();
		} else if (qName.equals("environment")) {
			environment = data.toString();
		} else if (qName.equals("host")) {
			host = data.toString();
		} else if (qName.equals("server")) {
			if (project != null)
				servers.add(new Server(project, name, type, host, port, environment));
			host = null;
		}
	}

	private IProject findProject(String string) {
		IPath proj = Path.fromPortableString(string);
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IPath projFile = projects[i].getLocation();
			if (projFile.equals(proj)) {
				return projects[i];
			}
		}
		RailsLog.logError("Unable to find a project for saved Rails server. Project path: " + string, new IllegalStateException());
		return null;
	}

	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {
		data = new StringBuffer();
		if (qName.equals("server")) {
			type = atts.getValue("type");
		}
	}

	public Collection getServers() {
		return servers;
	}

}

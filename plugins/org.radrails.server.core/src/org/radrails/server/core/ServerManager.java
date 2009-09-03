/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.server.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Observer;
import java.util.Set;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ui.PlatformUI;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Manages a collection of all servers. Operations on servers should be called
 * through this class, because it notifies any listening views of changes to the
 * server.
 * 
 * The <code>ServerManager</code> manages all of the servers in the workspace.
 * It is also the model represented by <code>ServersView</code>.
 * 
 * When a new <code>Server</code> has been created, it should be added to
 * <code>ServerManager</code> using {@link #addServer(Server) addServer}
 * method. Similarly, to remove a <code>Server</code>, use the
 * {@link #removeServer(Server) removeServer} method.
 * 
 * The <code>ServerManager</code> also maintains a list of
 * <code>Observer</code> objects. These are conceptually the observers of the
 * <code>ServerManager</code>, but actually the observers of the
 * <code>Server</code>s maintained by the <code>ServerManager</code>. As
 * such, the {@link #addServerObserver(Observer) addServerObserver} and
 * {@link #deleteServerObserver(Observer) deleteServerObserver} methods should
 * be used instead of the <code>addObserver</code> and
 * <code>deleteObserver</code> methods on <code>Server</code>.
 * 
 * @author mkent
 * 
 */
public class ServerManager implements IResourceChangeListener {

	private static final String SERVERS_XML = "servers.xml";
	private static ServerManager instance;
	private Set<Server> fServers;
	private List<Observer> serverObservers;

	/**
	 * Default constructor.
	 */
	private ServerManager() {
		fServers = new HashSet<Server>();
		serverObservers = new ArrayList<Observer>();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	/**
	 * @return the singleton instance of <code>ServerManager</code>.
	 */
	public static ServerManager getInstance() {
		if (instance == null) {
			instance = new ServerManager();
		}
		return instance;
	}

	/**
	 * Convenience method to stop all servers in the <code>ServerManager</code>.
	 */
	public void stopAll() {
		for (Server s : fServers) {
			if (s.isStarted()) {
				s.stop();

				// Force the status to STOPPED
				// Needed on workbench shutdown because the view gets disposed
				// before the status can be updated
				s.updateStatus(IServerConstants.STOPPED);
			}
		}
	}

	/**
	 * @return the next available server port
	 */
	public String getNextAvailablePort() {
		String port = IServerConstants.DEFAULT_WEBRICK_PORT;
		boolean found = false;

		while (!found) {
			if (!portInUse(port)) {
				found = true;
			} else {
				port = String.valueOf(Integer.parseInt(port) + 1);
			}
		}
		return port;
	}

	/**
	 * Determines if the given port is currently in use by a <code>Server</code>.
	 * 
	 * @param port
	 *            the port to check
	 * @return true if the port is in use, false otherwise
	 */
	public boolean portInUse(String port) {
		boolean taken = false;
		Iterator i = fServers.iterator();
		while (i.hasNext() && !taken) {
			Server s = (Server) i.next();
			taken = port.equals(s.getPort());
		}
		return taken;
	}

	/**
	 * Determines if the given project has a <code>Server</code> of the given
	 * type.
	 * 
	 * @param projectName
	 *            the name of the <code>Server</code> to check
	 * @param type
	 *            the type of <code>Server</code> to check for
	 * @return true if the project has the <code>Server</code>, false
	 *         otherwise
	 */
	public boolean projectHasServer(String projectName, String type) {
		for (Server s : fServers) {
			if (s.getProject().getName().equals(projectName) && s.getType().equals(type)) return true;
		}
		return false;
	}

	/**
	 * Adds a <code>Server</code> to the <code>ServerManager</code>.
	 * 
	 * @param server
	 *            the <code>Server</code> to add
	 */
	public synchronized void addServer(Server server) {
		fServers.add(server);
		saveServers();
		
		// Need to add the current observers to the new server
		Iterator i = serverObservers.iterator();
		while (i.hasNext()) {
			Observer o = (Observer) i.next();
			server.addServerObserver(o);
		}

		// Notify the observers that the server has been added
		server.touch();
		server.notifyServerObservers(IServerConstants.ADD);
	}

	/**
	 * Removes a <code>Server</code> from the <code>ServerManager</code>.
	 * 
	 * @param server
	 *            the <code>Server</code> to remove
	 */
	public synchronized void removeServer(Server server) {
		fServers.remove(server);
		saveServers();

		// Notify the observers that the server has been removed
		server.touch();
		server.notifyServerObservers(IServerConstants.REMOVE);
	}

	/**
	 * Clients should not call this method. Exists only for use by ServersView.
	 * 
	 * @return the servers held by the manager
	 */
	public Collection<Server> getServers() {
		return Collections.unmodifiableCollection(fServers);
	}
	
	/**
	 * Convenience method for the Rails App launch configuration
	 * 
	 * @param project
	 * @return collection of servers whose root is the given project
	 */
	public Collection<Server> getServersForProject(IProject project) {
		Collection<Server> ret = new ArrayList<Server>();
		for (Server s : fServers) {
			if(s.getProject().equals(project)) {
				ret.add(s);
			}
		}
		return ret;
	}
	
	/**
	 * Convenience method for the Rails App launch configuration
	 * 
	 * @param name
	 * @return the server for the given name
	 */
	public Server getServer(String name) {
		for (Server s : fServers) {
			if(s.getName().equals(name)) {
				return s;
			}
		}
		return null;
	}
	
	public Server findByProcess(IProcess process) {
		for (Server server : fServers) {
			if (process.equals(server.getProcess())) {
				return server;
			}
		}
		return null;
	}

	/**
	 * Adds an <code>Observer</code> to the list of current server observers.
	 * 
	 * @param ob
	 *            the observer to add
	 */
	public void addServerObserver(Observer ob) {
		serverObservers.add(ob);

		// Add observer of all servers
		Iterator<Server> i = fServers.iterator();
		while (i.hasNext()) {
			Server s = i.next();
			s.addServerObserver(ob);
		}
	}

	/**
	 * Deletes an <code>Observer</code> from the list of current server
	 * observers.
	 * 
	 * @param ob
	 *            the observer to delete
	 */
	public void deleteServerObserver(Observer ob) {
		serverObservers.remove(ob);

		// Remove observer of all servers
		for (Server s : fServers) {
			s.deleteServerObserver(ob);
		}
	}

	/**
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		// When a project is deleted, delete its servers
		if (event.getType() == IResourceChangeEvent.PRE_DELETE) {
			IProject project = (IProject) event.getResource();

			// Find all of this project's servers and remove them
			List<Server> servers = new ArrayList<Server>(fServers);
			for (int i = 0; i < servers.size(); i++) {
				final Server s = (Server) servers.get(i);
				if (project.equals(s.getProject())) {
					// This method will be called from a separate thread, so
					// use syncExec
					PlatformUI.getWorkbench().getDisplay().syncExec(
							new Runnable() {
								public void run() {
									// Stop the server, then remove
									s.stop();
									long start = System.currentTimeMillis();
									while (!s.getStatus().equals(IServerConstants.STOPPED)) {
										Thread.yield();
										if (System.currentTimeMillis() > start + 10000) break;
									}
									removeServer(s);
								}
							});
				}
			}
		}
		// When a project is closed, stop its servers
		else if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			IProject project = (IProject) event.getResource();

			// Find all of this project's servers and stop them
			List<Server> servers = new ArrayList<Server>(fServers);
			for (int i = 0; i < servers.size(); i++) {
				final Server s = (Server) servers.get(i);
				if (project.equals(s.getProject())) {
					// This method will be called from a separate thread, so
					// use syncExec
					PlatformUI.getWorkbench().getDisplay().syncExec(
							new Runnable() {
								public void run() {
									// Stop the server
									s.stop();
								}
							});
				}
			}
		}
	}

	/**
	 * Saves all current server configurations to file in XML format. The status
	 * of each server is not saved, because this method is only used on
	 * workbench shutdown and each server is assumed to be stopped.
	 */
	public synchronized void saveServers() {
		File f = getConfigFile();
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(f));
			writeXML(out);			
		} catch (FileNotFoundException e) {
			ServerLog.logError("Servers config file not found", e);
		} catch (IOException e) {
			ServerLog.logError("Error opening servers config", e);
		} finally {
			if (out != null) out.close();
		}	
	}

	/**
	 * Discards all existing server configurations, then loads saved server
	 * configurations from file.
	 */
	public void loadServers() {
		try {
			Reader fileReader = new FileReader(getConfigFile());
			XMLReader reader = SAXParserFactory.newInstance().newSAXParser()
					.getXMLReader();
			ServerManagerContentHandler handler = new ServerManagerContentHandler();
			reader.setContentHandler(handler);
			reader.parse(new InputSource(fileReader));

			// Discard current servers
			fServers.clear();

			// Load each saved server into the map
			Collection servers = handler.getServers();
			Iterator i = servers.iterator();
			while (i.hasNext()) {
				Server s = (Server) i.next();
				s.checkIfLeftHanging();				
				fServers.add(s);
			}
		} catch (FileNotFoundException e) {
			// This is okay, will get thrown if no config exists yet
		} catch (SAXException e) {
			ServerLog.logError("Error parsing servers config file", e);
		} catch (ParserConfigurationException e) {
			ServerLog.logError("Error configuring XML parser", e);
		} catch (FactoryConfigurationError e) {
			ServerLog.logError("Error configuring parser factory", e);
		} catch (IOException e) {
			ServerLog.logError("Error reading servers config file", e);
		}
	}

	/**
	 * Writes each server configuration to file in XML format.
	 * 
	 * @param out
	 *            the writer to use
	 */
	private void writeXML(PrintWriter out) {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<server-manager>");
		Iterator i = fServers.iterator();
		while (i.hasNext()) {
			Server s = (Server) i.next();
			out.println("<server type=\"" + s.getType() + "\">");
			out.println("<project>" + s.getProject().getLocation().toPortableString() + "</project>");
			out.println("<name>" + s.getName() + "</name>");
			out.println("<host>" + s.getHost() + "</host>");
			out.println("<port>" + s.getPort() + "</port>");
			out.println("<environment>" + s.getEnvironment() + "</environment>");
			out.println("<runMode>" + s.getRunMode() + "</runMode>");
			out.println("</server>");
		}
		out.println("</server-manager>");
		out.flush();
	}

	/**
	 * Returns the configuration file to use for the servers. The file is
	 * located in the plugin state directory and called <code>servers.xml</code>.
	 * 
	 * @return the config file
	 */
	private File getConfigFile() {
		return ServerPlugin.getInstance().getStateLocation().append(SERVERS_XML).toFile();
	}
}
/*******************************************************************************
 * Copyright (c) 2006 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.db.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.jruby.util.ByteList;
import org.jvyamlb.YAML;
import org.radrails.rails.core.RailsLog;
import org.radrails.rails.internal.core.RailsPlugin;

public class DatabaseYml
{

	/**
	 * To avoid recreating/re-parsing the same database.yml file a lot
	 */
	private static Map<String, DatabaseYml> map = new HashMap<String, DatabaseYml>();
	private static Map<String, Long> timestamps = new HashMap<String, Long>();

	private Map<String, DatabaseDescriptor> fDescriptors;
	private File file;
	private IProject project;

	private DatabaseYml(File databaseYML, IProject project)
	{
		this.file = databaseYML;
		this.project = project;
	}

	public DatabaseDescriptor getDescriptor(String name)
	{
		return getDescriptorMap().get(name);
	}

	private synchronized Map<String, DatabaseDescriptor> getDescriptorMap()
	{
		if (fDescriptors == null)
		{
			if (!file.exists())
				return null;
			InputStream reader = null;
			try
			{
				reader = new FileInputStream(file);
				fDescriptors = load(reader);
			}
			catch (Exception e)
			{
				RailsLog.log(e);
			}
			finally
			{
				try
				{
					if (reader != null)
						reader.close();
				}
				catch (IOException e)
				{
					// ignore
				}
			}
		}
		return fDescriptors;
	}

	public Collection<DatabaseDescriptor> getDescriptors()
	{
		if (getDescriptorMap() == null)
			return Collections.emptySet();
		return getDescriptorMap().values();
	}

	@SuppressWarnings("unchecked")
	private Map<String, DatabaseDescriptor> load(InputStream stream) throws IOException
	{
		Map<String, DatabaseDescriptor> descriptors = new HashMap<String, DatabaseDescriptor>();
		Map<ByteList, Object> configuration = (Map<ByteList, Object>) YAML.load(stream);
		for (Map.Entry<ByteList, Object> entry : configuration.entrySet())
		{
			DatabaseDescriptor desc = new DatabaseDescriptor(entry.getKey().toString(), project);
			Object children = entry.getValue();
			if (children instanceof Map)
			{
				Map<ByteList, Object> subNodes = (Map<ByteList, Object>) children;
				for (Map.Entry<ByteList, Object> subEntry : subNodes.entrySet())
				{
					if (subEntry != null && subEntry.getKey() != null && (!(subEntry.getValue() instanceof Map)))
					{
						String value = "";
						if (subEntry.getValue() != null)
						{
							value = subEntry.getValue().toString();
						}
						dbMapping(desc, subEntry.getKey().toString(), value);
					}
				}
			}
			descriptors.put(desc.getName(), desc);
		}
		return descriptors;
	}

	private void dbMapping(DatabaseDescriptor descriptor, String key, String value)
	{
		if (descriptor == null)
		{
			return;
		}

		if (key.equals("adapter"))
		{
			descriptor.setAdapter(value);
		}
		else if (key.equals("database"))
		{
			descriptor.setDatabase(value);
		}
		else if (key.equals("host"))
		{
			descriptor.setHost(value);
		}
		else if (key.equals("username"))
		{
			descriptor.setUsername(value);
		}
		else if (key.equals("password"))
		{
			descriptor.setPassword(value);
		}
		else if (key.equals("port"))
		{
			descriptor.setPort(value);
		}
		else if (key.equals("sslcert"))
		{
			descriptor.setSSLCert(value);
		}
		else if (key.equals("sslcapath"))
		{
			descriptor.setSSLCapath(value);
		}
		else if (key.equals("sslcipher"))
		{
			descriptor.setSSLCipher(value);
		}
		else if (key.equals("socket"))
		{
			descriptor.setSocket(value);
		}
		else if (key.equals("sslkey"))
		{
			descriptor.setSSLKey(value);
		}
		else if (key.equals("schema_order"))
		{
			descriptor.setSchemaOrder(value);
		}
		else if (key.equals("dbfile"))
		{
			descriptor.setDBFile(value);
		}
		else if (key.equals("url"))
		{
			descriptor.setURL(value);
		}
		else if (key.equals("driver"))
		{
			descriptor.setDriver(value);
		}
	}

	public static DatabaseYml create(IProject project)
	{
		synchronized (project)
		{
			return create(getPath(project).toFile(), project);
		}
	}

	private static DatabaseYml create(File databaseYML, IProject project)
	{
		if (map.containsKey(databaseYML.getAbsolutePath()))
		{
			Long cachedTimestamp = timestamps.get(databaseYML.getAbsolutePath());
			Long timestamp = databaseYML.lastModified();
			if (timestamp.equals(cachedTimestamp)) // if timestamp hasn't changed
				return map.get(databaseYML.getAbsolutePath());
		}
		// have no cached copy, or file has changed

		DatabaseYml yml = new DatabaseYml(databaseYML, project);
		map.put(databaseYML.getAbsolutePath(), yml);
		Long timestamp = databaseYML.lastModified();
		timestamps.put(databaseYML.getAbsolutePath(), timestamp);
		return yml;
	}

	static IPath getPath(IProject project)
	{
		if (project == null)
			return Path.EMPTY;
		IPath root = RailsPlugin.findRailsRoot(project);
		if (root == null)
			root = Path.EMPTY;
		if (project.getLocation() == null)
			return Path.EMPTY;
		return project.getLocation().append(root).append("config").append("database.yml");
	}

	public boolean setDescriptor(String environment, DatabaseDescriptor desc)
	{
		if (!file.exists())
			return false;
		StringBuilder modified = new StringBuilder();
		BufferedReader reader = null;
		boolean inDesignatedEnvironment = false;
		try
		{
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				if (line.startsWith(environment + ":"))
				{
					inDesignatedEnvironment = true;
					modified.append(desc.toYML());
					continue;
				}
				if (inDesignatedEnvironment && !Character.isWhitespace(line.charAt(0)))
				{
					inDesignatedEnvironment = false;
				}
				if (!inDesignatedEnvironment)
					modified.append(line).append("\n");
			}
		}
		catch (Exception e)
		{
			RailsLog.log(e);
			return false;
		}
		finally
		{
			try
			{
				if (reader != null)
					reader.close();
			}
			catch (IOException e)
			{
				// ignore
			}
		}

		Writer writer = null;
		try
		{
			writer = new BufferedWriter(new FileWriter(file));
			// TODO Use FileWriter's newline to write the new lines?!
			writer.write(modified.toString());
		}
		catch (IOException e)
		{
			RailsLog.log(e);
			return false;
		}
		finally
		{
			try
			{
				if (writer != null)
					writer.close();
			}
			catch (IOException e)
			{
				// ignore
			}
		}
		return true;
	}
}

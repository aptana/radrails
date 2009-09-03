/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.db.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.radrails.rails.internal.core.RailsPlugin;

/**
 * Describes a database in a YML file.
 */
public class DatabaseDescriptor
{
	private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DEFAULT_HOST = "localhost";

	private String descriptorName;
	private String adapter;
	private String database;
	private String host;
	private String username;
	private String password;
	private String port;
	private String sslCert;
	private String sslCapath;
	private String sslCipher;
	private String socket;
	private String sslKey;
	private String schemaOrder;
	private String dbFile;
	private String driver;
	private String url;

	private IProject project;

	public DatabaseDescriptor(String name, IProject project)
	{
		this.project = project;
		this.descriptorName = name;
		this.adapter = "";
		this.database = "";
		this.host = DEFAULT_HOST;
		this.username = "";
		this.password = "";
		this.port = "";
		this.socket = "";
		this.sslCert = "";
		this.sslCapath = "";
		this.sslKey = "";
		this.schemaOrder = "";
		this.dbFile = "";
		this.url = "";
		this.driver = "";
	}

	public IProject getProject()
	{
		return project;
	}

	public String getName()
	{
		return descriptorName;
	}

	/**
	 * Gets the adapter.
	 * 
	 * @return Returns the adapter.
	 */
	public String getAdapter()
	{
		return adapter;
	}

	/**
	 * Sets the adapter to adapter.
	 * 
	 * @param adapter
	 *            The adapter to set.
	 */
	public void setAdapter(String adapter)
	{
		this.adapter = adapter;
	}

	/**
	 * Gets the database.
	 * 
	 * @return Returns the database.
	 */
	public String getDatabase()
	{
		return database;
	}

	/**
	 * Sets the database to database.
	 * 
	 * @param database
	 *            The database to set.
	 */
	public void setDatabase(String database)
	{
		this.database = database;
	}

	/**
	 * Gets the host.
	 * 
	 * @return Returns the host.
	 */
	public String getHost()
	{
		if (host == null || host.trim().equals(""))
		{
			host = DEFAULT_HOST;
		}
		return host;
	}

	/**
	 * Sets the host to host.
	 * 
	 * @param host
	 *            The host to set.
	 */
	public void setHost(String host)
	{
		this.host = host;
	}

	/**
	 * Gets the password.
	 * 
	 * @return Returns the password.
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * Sets the password to password.
	 * 
	 * @param password
	 *            The password to set.
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/**
	 * Gets the username.
	 * 
	 * @return Returns the username.
	 */
	public String getUsername()
	{
		return username;
	}

	/**
	 * Sets the username to username.
	 * 
	 * @param username
	 *            The username to set.
	 */
	public void setUsername(String username)
	{
		this.username = username;
	}

	/**
	 * @return Returns the dbfile.
	 */
	public String getDBFile()
	{
		return dbFile;
	}

	/**
	 * @param dbfile
	 *            The dbfile to set.
	 */
	public void setDBFile(String dbfile)
	{
		this.dbFile = dbfile;
	}

	/**
	 * @return Returns the port.
	 */
	public String getPort()
	{
		if (port == null || port.trim().equals(""))
		{
			if (isMySQL())
			{
				return "3306";
			}
			else if (isIBMDB2())
			{
				return "50000";
			}
			else if (isOracle())
			{
				return "1521";
			}
			else if (isPostgresql())
			{
				return "5432";
			}
			else if (isSQLServer())
			{
				return "1433";
			}
		}
		return port;
	}

	/**
	 * @param port
	 *            The port to set.
	 */
	public void setPort(String port)
	{
		this.port = port;
	}

	/**
	 * @return Returns the schema_order.
	 */
	public String getSchemaOrder()
	{
		return schemaOrder;
	}

	/**
	 * @param schema_order
	 *            The schema_order to set.
	 */
	public void setSchemaOrder(String schema_order)
	{
		this.schemaOrder = schema_order;
	}

	/**
	 * @return Returns the socket.
	 */
	public String getSocket()
	{
		return socket;
	}

	/**
	 * @param socket
	 *            The socket to set.
	 */
	public void setSocket(String socket)
	{
		this.socket = socket;
	}

	/**
	 * @return Returns the sslcapath.
	 */
	public String getSSLCapath()
	{
		return sslCapath;
	}

	/**
	 * @param sslcapath
	 *            The sslcapath to set.
	 */
	public void setSSLCapath(String sslcapath)
	{
		this.sslCapath = sslcapath;
	}

	/**
	 * @return Returns the sslcert.
	 */
	public String getSSLCert()
	{
		return sslCert;
	}

	/**
	 * @param sslcert
	 *            The sslcert to set.
	 */
	public void setSSLCert(String sslcert)
	{
		this.sslCert = sslcert;
	}

	/**
	 * @return Returns the sslcipher.
	 */
	public String getSSLCipher()
	{
		return sslCipher;
	}

	/**
	 * @param sslcipher
	 *            The sslcipher to set.
	 */
	public void setSSLCipher(String sslcipher)
	{
		this.sslCipher = sslcipher;
	}

	/**
	 * @return Returns the sslkey.
	 */
	public String getSSLKey()
	{
		return sslKey;
	}

	/**
	 * @param sslkey
	 *            The sslkey to set.
	 */
	public void setSSLKey(String sslkey)
	{
		this.sslKey = sslkey;
	}

	public void setDriver(String driver)
	{
		this.driver = driver;
	}

	public String getDriver()
	{
		if (driver == null || driver.trim().equals(""))
			return buildDriver();
		return driver;
	}

	private String buildDriver()
	{
		if (isMySQL())
			return MYSQL_DRIVER;
		if (isODBC())
			return "sun.jdbc.odbc.JdbcOdbcDriver";
		if (isOracle())
			return "oracle.jdbc.driver.OracleDriver";
		if (isPostgresql())
			return "org.postgresql.Driver";
		if (isSQLServer())
			return "com.microsoft.jdbc.sqlserver.SQLServerDriver";
		if (isIBMDB2())
			return "com.ibm.db2.jcc.DB2Driver";
		if (isSQLite())
			return "org.sqlite.JDBC";
		return "";
	}

	public void setURL(String url)
	{
		this.url = url;
	}

	public String getUrl()
	{
		if (url == null || url.trim().equals(""))
			return buildURL();
		return url;
	}

	private String buildURL()
	{
		if (isSQLite())
		{
			String file = getDBFile();
			if (file == null || file.trim().length() == 0)
			{
				file = getDatabase();
			}
			IPath root = RailsPlugin.findRailsRoot(project);
			file = project.getLocation().append(root).append(file).toOSString();
			return "jdbc:sqlite:" + file;
		}
		String extra = "";
		if (isMySQL())
		{
			extra = "?zeroDateTimeBehavior=convertToNull";
		}
		return getConnectFragment() + getHost() + ":" + getPort() + "/" + getDatabase() + extra;
	}

	private String getConnectFragment()
	{
		String connect = "jdbc:" + getAdapter() + "://";
		if (isOracle())
		{
			connect = "jdbc:oracle:thin:@";
		}
		else if (isSQLServer())
		{
			connect = "jdbc:Microsoft:sqlserver://";
		}
		else if (isODBC())
		{
			connect = "jdbc:odbc:";
		}
		return connect;
	}

	public boolean isODBC()
	{
		return getAdapter().equals("odbc");
	}

	public boolean isSQLServer()
	{
		return getAdapter().equals("sqlserver");
	}

	public boolean isSQLite()
	{
		return getAdapter().equals("sqlite") || getAdapter().equals("sqlite3");
	}

	public boolean isPostgresql()
	{
		return getAdapter().equals("postgresql");
	}

	public boolean isOracle()
	{
		return getAdapter().equals("oci");
	}

	public boolean isIBMDB2()
	{
		return getAdapter().equals("db2");
	}

	public boolean isMySQL()
	{
		return getAdapter().equals("mysql");
	}

	public String toYML()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(getName()).append(":\n");
		builder.append("  adapter: ").append(getAdapter()).append("\n");
		builder.append("  username: ").append(getUsername()).append("\n");
		builder.append("  password: ").append(getPassword()).append("\n");
		builder.append("  host: ").append(getHost()).append("\n");
		builder.append("  database: ").append(getDatabase()).append("\n");
		return builder.toString();
	}

}
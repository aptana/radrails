/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.radrails.db.core;

/**
 * Provides constants for the various database core classes.
 * 
 * @author mkent
 * 
 * @version 0.2.1
 */
public interface IDatabaseConstants {

	// Environment constants
	public static final String ENV_DEVELOPMENT = "development";

	public static final String ENV_TEST = "test";

	public static final String ENV_PRODUCTION = "production";

	// Adapter constants
	public static final String ADAPTER_DB2 = "ibm_db";

	public static final String ADAPTER_MYSQL = "mysql";

	public static final String ADAPTER_OCI = "oci";

	public static final String ADAPTER_POSTGRESQL = "postgresql";

	public static final String ADAPTER_FRONTBASE = "frontbase";
	
	public static final String ADAPTER_SQLITE = "sqlite";
	
	public static final String ADAPTER_SQLITE3 = "sqlite3";

	public static final String ADAPTER_SQLSERVER = "sqlserver";

	public static final String ADAPTER_ODBC = "odbc";

	public static final String ADAPTER_JDBC = "jdbc";

}

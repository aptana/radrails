package org.radrails.db.core;

import java.util.Collection;

import org.apache.derby.drda.NetworkServerControl;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

class DerbyStarter extends Job
{

	private static boolean derbyStarted = false;

	public DerbyStarter()
	{
		super("Checking if we need to start derby server");
		setSystem(true);
		setPriority(LONG);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor)
	{
		if (!shouldStartDerby())
			return Status.CANCEL_STATUS;
		start();
		return Status.OK_STATUS;
	}

	private boolean shouldStartDerby()
	{
		Collection<ProjectDatabaseManager> managers = DatabaseManager.getInstance().getAllProjectDatabaseManagers();
		for (ProjectDatabaseManager projectDatabaseManager : managers)
		{
			Collection<DatabaseDescriptor> descriptors = projectDatabaseManager.getDatabaseDescriptors();
			for (DatabaseDescriptor databaseDescriptor : descriptors)
			{
				String db = databaseDescriptor.getAdapter();
				if (db.equalsIgnoreCase(IDatabaseConstants.ADAPTER_JDBC))
				{
					String driver = databaseDescriptor.getDriver();
					if (driver.equals("org.apache.derby.jdbc.ClientDriver"))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	public static void start()
	{
		if (derbyStarted)
			return;
		try
		{
			NetworkServerControl server = new NetworkServerControl();
			server.start(null);
			derbyStarted = true;
		}
		catch (Exception e)
		{
			DatabaseLog.log(e);
		}
	}
}

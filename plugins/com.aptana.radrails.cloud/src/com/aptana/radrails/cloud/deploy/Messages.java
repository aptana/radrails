package com.aptana.radrails.cloud.deploy;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.radrails.cloud.deploy.messages"; //$NON-NLS-1$
	
	public static String EndpointDialog_LBL_Deploy_to;
	public static String EndpointDialog_LBL_Public_endpoint;
	public static String EndpointDialog_LBL_Staging_endpoint;
	public static String EndpointDialog_MSG_Enter_deploy_comment;
	public static String EndpointDialog_MSG_Help_link;
	public static String EndpointDialog_MSG_Verify_and_select_target;
	public static String EndpointDialog_TTL;
	public static String EndpointDialog_TTL_Header;
	public static String RailsCloudDeployer_LBL_Deploying_job_title;
	public static String RailsCloudDeployer_MSG_Deploy_event_comment_header;
	public static String RailsCloudDeployer_TTL_Deploy_event_subject;
	public static String EndpointDialog_LBL_Deploy_button;
	
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}

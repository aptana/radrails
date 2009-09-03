package org.radrails.rails.internal.parser.warnings;

import org.radrails.rails.core.IDeprecationProblems;
import org.radrails.rails.internal.core.RailsPlugin;

public class DeprecatedPostFormatMethods extends DeprecationVisitor {

	private static final String[] DEPRECATED = new String[] {"post_format", "formatted_post?", "xml_post?", "yaml_post?"};
	
	public DeprecatedPostFormatMethods() {
		super(DEPRECATED, "Deprecated method call, use respond_to or request.format instead");
	}

	@Override
	protected String getOptionKey() {
		return RailsPlugin.RAILS_DEPRECATION_POST_FORMAT;
	}
	
	@Override
	protected int getProblemID() {
		return IDeprecationProblems.DeprecatedPostFormatMethods;
	}
}

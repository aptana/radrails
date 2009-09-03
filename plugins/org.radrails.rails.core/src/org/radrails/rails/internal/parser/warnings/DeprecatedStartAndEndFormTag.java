package org.radrails.rails.internal.parser.warnings;

import org.radrails.rails.core.IDeprecationProblems;
import org.radrails.rails.internal.core.RailsPlugin;

public class DeprecatedStartAndEndFormTag extends DeprecationVisitor {

	private static final String[] DEPRECATED = new String[] {"start_form_tag", "end_form_tag"};
	
	public DeprecatedStartAndEndFormTag() {
		super(DEPRECATED, "Deprecated method call, use form_tag with a block instead");
	}

	@Override
	protected String getOptionKey() {
		return RailsPlugin.RAILS_DEPRECATION_START_END_FORM_TAG;
	}
	
	@Override
	protected int getProblemID() {
		return IDeprecationProblems.DeprecatedStartEndFormTags;
	}
}

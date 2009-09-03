package org.radrails.rails.internal.parser.warnings;

import org.radrails.rails.core.IDeprecationProblems;
import org.radrails.rails.internal.core.RailsPlugin;

public class HumanSizeHelperAlias extends DeprecationVisitor {

	private static final String[] DEPRECATED = new String[] {"human_size"};
	
	public HumanSizeHelperAlias() {
		super(DEPRECATED, "Deprecated method call, use number_to_human_size instead");
	}

	@Override
	protected String getOptionKey() {
		return RailsPlugin.RAILS_DEPRECATION_HUMAN_SIZE_HELPER_ALIAS;
	}
	
	@Override
	protected int getProblemID() {
		return IDeprecationProblems.DeprecatedHumanSizeAlias;
	}

}

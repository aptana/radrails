package org.radrails.rails.internal.parser.warnings;

import org.radrails.rails.core.IDeprecationProblems;
import org.radrails.rails.internal.core.RailsPlugin;

public class DeprecatedImageLinkMethods extends DeprecationVisitor {
	
	private static final String[] DEPRECATED = new String[] {"link_to_image", "link_image_to"};
	
	public DeprecatedImageLinkMethods() {
		super(DEPRECATED, "Deprecated method call, use link_to(image_tag(..), url) instead");
	}

	@Override
	protected String getOptionKey() {
		return RailsPlugin.RAILS_DEPRECATION_IMAGE_LINK_METHODS;
	}
	
	@Override
	protected int getProblemID() {
		return IDeprecationProblems.DeprecatedImageLinkMethods;
	}
	
}

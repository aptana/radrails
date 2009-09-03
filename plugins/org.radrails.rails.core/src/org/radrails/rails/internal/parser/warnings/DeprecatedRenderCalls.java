package org.radrails.rails.internal.parser.warnings;

import org.radrails.rails.core.IDeprecationProblems;
import org.radrails.rails.internal.core.RailsPlugin;

public class DeprecatedRenderCalls extends DeprecationVisitor {
	
	private static final String[] DEPRECATED = new String[] {"render_text", "render_file", "render_template", "render_partial", "render_partial_collection", "render_action", "render_with_layout", "render_without_layout"};
	private static String[] SOLUTIONS = new String[] {"render :text => \"string\"", "render :file => \"path\"", "render :template => \"template_path\"", "render :partial => \"partial_path\"", "render :partial => \"partial_path\", :collection => @collection", "render :action => \"action_name\"", "render :action => \"action_name\", :layout => \"layout_name\"", "render :action => \"action_name\", :layout => false"};
	static {
		for (int i = 0; i < SOLUTIONS.length; i++) {
			SOLUTIONS[i] = "Deprecated method call, use " + SOLUTIONS[i] + " instead";
		}
	}
	public DeprecatedRenderCalls() {
		super(DEPRECATED, SOLUTIONS);
	}

	@Override
	protected String getOptionKey() {
		return RailsPlugin.RAILS_DEPRECATION_RENDER_CALLS;
	}
	
	@Override
	protected int getProblemID() {
		return IDeprecationProblems.DeprecatedRenderCalls;
	}
}

package org.radrails.rails.internal.parser.warnings;

import java.util.HashMap;
import java.util.Map;

import org.radrails.rails.internal.core.RailsPlugin;
import org.rubypeople.rdt.core.RubyCore;

public class RailsDeprecationOptions {
	
	public static final long RailsInstanceVariables = 0x1;
	public static final long DeprecatedRenderCalls = 0x2;
	public static final long DeprecatedRedirectCalls = 0x4;
	public static final long DeprecatedPostFormatMethods = 0x8;
	public static final long StartEndFormTag = 0x10;
	public static final long UpdateElementFunction = 0x20;
	public static final long ImageLinkMethods = 0x40;
	public static final long HumanSizeHelperAlias = 0x80;
	public static final long PushWithAttributes = 0x100;
	public static final long DeprecatedFindMethods = 0x200;
	
	public static final String ERROR = RubyCore.ERROR;
	public static final String WARNING = RubyCore.WARNING;
	public static final String IGNORE = RubyCore.IGNORE;
	public static final String ENABLED = RubyCore.ENABLED;
	public static final String DISABLED = RubyCore.DISABLED;
	
//	 Default severity level for handlers
	public long errorThreshold = 0;
	
	public long warningThreshold = 
		RailsInstanceVariables
		| DeprecatedRenderCalls
		| DeprecatedRedirectCalls
		| DeprecatedPostFormatMethods
		| StartEndFormTag
		| UpdateElementFunction
		| ImageLinkMethods
		| HumanSizeHelperAlias
		| PushWithAttributes
		| DeprecatedFindMethods
		/*| NullReference -- keep RubyCore#getDefaultOptions comment in sync */;
	
	public Map<String, String> getMap() {
		Map<String, String> optionsMap = new HashMap<String, String>(30);
		optionsMap.put(RailsPlugin.RAILS_DEPRECATION_INSTANCE_VARIABLES,
				getSeverityString(RailsInstanceVariables));
		optionsMap.put(RailsPlugin.RAILS_DEPRECATION_RENDER_CALLS,
				getSeverityString(DeprecatedRenderCalls));
		optionsMap.put(RailsPlugin.RAILS_DEPRECATION_REDIRECT_CALLS,
				getSeverityString(DeprecatedRedirectCalls));
		optionsMap.put(RailsPlugin.RAILS_DEPRECATION_POST_FORMAT,
				getSeverityString(DeprecatedPostFormatMethods));
		optionsMap.put(RailsPlugin.RAILS_DEPRECATION_START_END_FORM_TAG,
				getSeverityString(StartEndFormTag));
		optionsMap.put(RailsPlugin.RAILS_DEPRECATION_UPDATE_ELEMENT_FUNCTION,
				getSeverityString(UpdateElementFunction));
		optionsMap.put(RailsPlugin.RAILS_DEPRECATION_IMAGE_LINK_METHODS,
				getSeverityString(ImageLinkMethods));
		optionsMap.put(RailsPlugin.RAILS_DEPRECATION_HUMAN_SIZE_HELPER_ALIAS,
				getSeverityString(HumanSizeHelperAlias));
		optionsMap.put(RailsPlugin.RAILS_DEPRECATION_PUSH_WITH_ATTRIBUTES,
				getSeverityString(PushWithAttributes));		
		optionsMap.put(RailsPlugin.RAILS_DEPRECATION_ACTIVE_RECORD_FIND_METHODS,
				getSeverityString(DeprecatedFindMethods));		
		return optionsMap;
	}

	public String getSeverityString(long irritant) {
		if ((this.warningThreshold & irritant) != 0)
			return WARNING;
		if ((this.errorThreshold & irritant) != 0)
			return ERROR;
		return IGNORE;
	}

	public void set(Map optionsMap) {
		Object optionValue;
		if ((optionValue = optionsMap
				.get(RailsPlugin.RAILS_DEPRECATION_INSTANCE_VARIABLES)) != null)
			updateSeverity(RailsInstanceVariables, optionValue);
		if ((optionValue = optionsMap
				.get(RailsPlugin.RAILS_DEPRECATION_RENDER_CALLS)) != null)
			updateSeverity(DeprecatedRenderCalls, optionValue);		
		if ((optionValue = optionsMap
				.get(RailsPlugin.RAILS_DEPRECATION_REDIRECT_CALLS)) != null)
			updateSeverity(DeprecatedRedirectCalls, optionValue);
		if ((optionValue = optionsMap
				.get(RailsPlugin.RAILS_DEPRECATION_POST_FORMAT)) != null)
			updateSeverity(DeprecatedPostFormatMethods, optionValue);
		if ((optionValue = optionsMap
				.get(RailsPlugin.RAILS_DEPRECATION_START_END_FORM_TAG)) != null)
			updateSeverity(StartEndFormTag, optionValue);
		if ((optionValue = optionsMap
				.get(RailsPlugin.RAILS_DEPRECATION_UPDATE_ELEMENT_FUNCTION)) != null)
			updateSeverity(UpdateElementFunction, optionValue);
		if ((optionValue = optionsMap
				.get(RailsPlugin.RAILS_DEPRECATION_IMAGE_LINK_METHODS)) != null)
			updateSeverity(ImageLinkMethods, optionValue);
		if ((optionValue = optionsMap
				.get(RailsPlugin.RAILS_DEPRECATION_HUMAN_SIZE_HELPER_ALIAS)) != null)
			updateSeverity(HumanSizeHelperAlias, optionValue);
		if ((optionValue = optionsMap
				.get(RailsPlugin.RAILS_DEPRECATION_PUSH_WITH_ATTRIBUTES)) != null)
			updateSeverity(PushWithAttributes, optionValue);		
		if ((optionValue = optionsMap
				.get(RailsPlugin.RAILS_DEPRECATION_ACTIVE_RECORD_FIND_METHODS)) != null)
			updateSeverity(DeprecatedFindMethods, optionValue);
	}

	void updateSeverity(long irritant, Object severityString) {
		if (ERROR.equals(severityString)) {
			this.errorThreshold |= irritant;
			this.warningThreshold &= ~irritant;
		} else if (WARNING.equals(severityString)) {
			this.errorThreshold &= ~irritant;
			this.warningThreshold |= irritant;
		} else if (IGNORE.equals(severityString)) {
			this.errorThreshold &= ~irritant;
			this.warningThreshold &= ~irritant;
		}
	}
}

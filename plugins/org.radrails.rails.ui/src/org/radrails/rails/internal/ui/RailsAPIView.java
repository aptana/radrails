package org.radrails.rails.internal.ui;

import com.aptana.rdt.ui.BrowserView;

public class RailsAPIView extends BrowserView {

	private static final String URL = "http://api.rubyonrails.org/";

	@Override
	protected String getURL() {
		return URL;
	}
}

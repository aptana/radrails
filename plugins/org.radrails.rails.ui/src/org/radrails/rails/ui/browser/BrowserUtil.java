package org.radrails.rails.ui.browser;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.browser.BrowserViewer;
import org.eclipse.ui.internal.browser.WebBrowserEditorInput;
import org.radrails.rails.internal.ui.browser.BrowserEditor;
import org.radrails.rails.internal.ui.browser.CoreBrowserLocationListener;
import org.radrails.rails.ui.RailsUIPlugin;

public class BrowserUtil {

	public static final IEditorPart openBrowser(String url) throws PartInitException,
			MalformedURLException {
		IEditorPart part = IDE.openEditor(getActivePage(), getInput(url), BrowserEditor.ID);
		if (part instanceof BrowserEditor) {
			final BrowserEditor browser = (BrowserEditor) part;
			browser.getBrowser().addTitleListener(new TitleListener() {
			
				public void changed(TitleEvent event) {
					browser.getBrowser().execute(RailsUIPlugin.getInstance().getFileContents("scripts/link.js"));
				}
			
			});
			browser.getBrowser().addLocationListener(new CoreBrowserLocationListener());
		}
		return part;
	}

	public static void openOrActivateBrowser(String url)
			throws PartInitException, MalformedURLException {
		IEditorPart editor = getActivePage().findEditor(getInput(url));
		if (editor != null) {
			getActivePage().activate(editor); // TODO reload/refresh?
		} 
		IEditorReference[] refs = getActivePage().getEditorReferences();
		for (int i = 0; i < refs.length; i++) {
			IEditorInput input = refs[i].getEditorInput();
			if (input instanceof WebBrowserEditorInput) {
				WebBrowserEditorInput webInput = (WebBrowserEditorInput) input;
				String blah = webInput.getURL().toString();
				if (blah.equals(url)) {
					editor = refs[i].getEditor(false);
					if (editor != null) {
						getActivePage().activate(editor);// TODO reload/refresh?
						return;
					} 
				}
			}
		}
		// if the internal browser isn't open yet, do so
		BrowserUtil.openBrowser(url);
	}

	private static WebBrowserEditorInput getInput(String url)
			throws MalformedURLException {
		return new WebBrowserEditorInput(new URL(url),
				BrowserViewer.BUTTON_BAR | BrowserViewer.LOCATION_BAR);
	}

	private static IWorkbenchPage getActivePage() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage();
	}

}

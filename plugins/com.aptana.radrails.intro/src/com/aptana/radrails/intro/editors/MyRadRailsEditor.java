/**
 * This file Copyright (c) 2005-2009 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain Eclipse Public Licensed code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.radrails.intro.editors;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.aptana.ide.core.ui.CoreUIUtils;
import com.aptana.ide.core.ui.browser.BaseBrowserAdapter;
import com.aptana.ide.core.ui.browser.BrowserRegistry;
import com.aptana.ide.core.ui.browser.IBrowser;
import com.aptana.ide.server.jetty.JettyPlugin;
import com.aptana.ide.server.portal.PortalPlugin;
import com.aptana.ide.server.portal.preferences.IPreferenceConstants;

public class MyRadRailsEditor extends EditorPart {

    public static final String ID = "com.aptana.radrails.MyRadRailsEditor"; //$NON-NLS-1$
    public static final MyRadRailsEditorInput INPUT = new MyRadRailsEditorInput();

    private static boolean isOpen;

    private Composite displayArea;
    private IBrowser browser;

    public MyRadRailsEditor() {
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    public void dispose() {
        super.dispose();
        isOpen = false;

        try {
            if (browser != null) {
                browser.dispose();
                browser = null;
            }
        } catch (Exception e) {
        }
    }

    /**
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void doSave(IProgressMonitor monitor) {
    }

    /**
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    public void doSaveAs() {
    }

    /**
     * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
     *      org.eclipse.ui.IEditorInput)
     */
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        isOpen = true;
        if (input != INPUT) {
            // forces the same input to be used
            input = INPUT;
        }
        setSite(site);
        setInput(input);
        // resets the preference
        PortalPlugin.getDefault().getPreferenceStore().setValue(
                IPreferenceConstants.MY_APTANA_PREVIOUSLY_OPENED, false);
    }

    /**
     * @see org.eclipse.ui.part.EditorPart#isDirty()
     */
    public boolean isDirty() {
        return false;
    }

    /**
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed() {
        return false;
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        displayArea = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        displayArea.setLayout(layout);
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        if (JettyPlugin
                .getDefault()
                .getPreferenceStore()
                .getBoolean(
                        com.aptana.ide.server.jetty.preferences.IPreferenceConstants.USE_FIREFOX)) {
            if (CoreUIUtils.onWindows) {
                browser = BrowserRegistry.getRegistry().getBrowser(
                        "com.aptana.ide.xul.firefox"); //$NON-NLS-1$
            }
        }
        if (browser == null) {
            browser = new BaseBrowserAdapter();
        }
        browser.createControl(displayArea);
        browser.getControl().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        updateContent();

        browser.addLocationListener(new LocationListener() {

            public void changed(LocationEvent event) {
            }

            public void changing(LocationEvent event) {
                String location = event.location;
                // For absolute URLs with http or https protocols 
                if (location.startsWith("http:") || location.startsWith("https:")) { //$NON-NLS-1$ //$NON-NLS-2$
                    // Launch external browser
                    CoreUIUtils.openBrowserURL(location);
                    event.doit = false;
                }
            }
            
        });
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus() {
        displayArea.setFocus();
    }

    public static boolean isOpen() {
        return isOpen;
    }

    private void updateContent() {
        browser.setURL(MyRadRailsPageLoader.getURLLocation());
    }
}

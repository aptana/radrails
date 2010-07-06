/**
 * This file Copyright (c) 2005-2007 Aptana, Inc. This program is
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
package com.aptana.ide.editor.erb;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.radrails.server.core.Server;
import org.radrails.server.core.ServerManager;
import org.rubypeople.rdt.core.SocketUtil;

import com.aptana.ide.core.IdeLog;
import com.aptana.ide.editor.html.BrowserExtensionLoader;
import com.aptana.ide.editor.html.MultiPageHTMLEditor;
import com.aptana.ide.editor.html.preview.HTMLPreviewPropertyPage;
import com.aptana.ide.editor.html.preview.IBrowserTabAdder;
import com.aptana.ide.editor.html.preview.IPreviewConfigurationPage;
import com.aptana.ide.editor.html.preview.PreviewConfigurationPage;
import com.aptana.ide.editors.UnifiedEditorsPlugin;
import com.aptana.ide.editors.unified.ContributedBrowser;

/**
 * @author Chris Williams (cwilliams@aptana.com)
 * @author Kevin Sawicki (ksawicki@aptana.com)
 */
public class ERBTabAdder implements IBrowserTabAdder
{

	private boolean autoStartServer = false; // TODO Pull this from a preference that's on the ERB pref page!

	/**
	 * @see com.aptana.ide.editor.html.preview.IBrowserTabAdder#getAddOnTabs(com.aptana.ide.editor.html.MultiPageHTMLEditor,
	 *      org.eclipse.swt.widgets.Composite)
	 */
	public IPreviewConfigurationPage[] getAddOnTabs(MultiPageHTMLEditor editor, Composite parent)
	{
		if (editor.getSourceEditor() instanceof ERBSourceEditor)
		{
			int pageCount = editor.getPageCount();
			for (int i = 1; i < pageCount; i++)
			{
				editor.removePage(1);
			}

			IFile file = getFile(editor);
			if (file != null)
			{
				IProject project = file.getProject();
				if (project != null)
				{
					Collection<Server> servers = ServerManager.getInstance().getServersForProject(project);
					Server runningServer = null;
					for (Server server : servers)
					{
						if (server.isStarted())
						{
							runningServer = server;
							break;
						}
					}
					if (runningServer == null)
					{
						for (Server server : servers)
						{
							if (server.isLocalhost()
									&& !SocketUtil.portFree(server.getHost(), Integer.parseInt(server.getPort())))
							{
								runningServer = server;
								continue;
							}
						}
					}
					if (autoStartServer)
					{
						if (runningServer == null && servers != null && !servers.isEmpty())
						{
							runningServer = servers.iterator().next();
							runningServer.start(false);
							// TODO Wait until it's up before continuing?
						}
					}
					if (runningServer != null)
					{
						return getPages(editor, runningServer, file, parent);
					}
				}
			}
		}
		return null;
	}

	private static IFile getFile(IEditorPart editor)
	{
		if (editor == null)
		{
			return null;
		}
		if (editor.getEditorInput() instanceof IFileEditorInput)
		{
			IFileEditorInput input = (IFileEditorInput) editor.getEditorInput();
			return input.getFile();
		}
		return null;
	}

	private static IPreviewConfigurationPage[] getPages(MultiPageHTMLEditor editor, Server server, IFile file,
			Composite parent)
	{

		List<IConfigurationElement> browserList = BrowserExtensionLoader.loadBrowsers();
		IPreviewConfigurationPage[] pages = new IPreviewConfigurationPage[browserList.size()];
		String url = getURL(server, file);
		if (url == null)
		{
			return null;
		}
		for (int j = 0; j < browserList.size(); j++)
		{
			IConfigurationElement element = (IConfigurationElement) browserList.get(j);
			String name = BrowserExtensionLoader.getBrowserLabel(element);
			try
			{
				Object obj = element.createExecutableExtension(UnifiedEditorsPlugin.CLASS_ATTR);
				if (obj instanceof ContributedBrowser)
				{
					ContributedBrowser browser = (ContributedBrowser) obj;
					pages[j] = getPage(editor, browser, name, url, parent);
				}
			}
			catch (CoreException e)
			{
				IdeLog.logError(ERBPlugin.getDefault(), e.getMessage(), e);
			}
		}
		return pages;
	}

	private static String getURL(Server server, IFile file)
	{
		String url = "http://" + server.getBrowserHost() + ":" + server.getPort();
		if (!url.endsWith("/"))
		{
			url += "/";
		}

		// Mangle path to try and match Rails routing!
		// TODO Actually load up and parse the routes.rb in JRuby to reverse engineer them?!
		IPath path = file.getProjectRelativePath();
		path = path.removeFirstSegments(2);
		String controller = path.segment(0);
		if (controller.equals("layouts"))
		{
			return null;
		}
		String fileName = path.lastSegment();
		if (fileName.contains("."))
		{
			fileName = fileName.substring(0, fileName.indexOf('.'));
		}
		path = path.removeLastSegments(1);
		if (!fileName.equals("index"))
		{
			path = path.append(fileName);
		}
		url += path.toPortableString();
		return url;
	}

	private static IPreviewConfigurationPage getPage(MultiPageHTMLEditor editor, ContributedBrowser browser,
			String name, String url, Composite parent)
	{

		PreviewConfigurationPage page = new PreviewConfigurationPage(editor);
		page.setType(HTMLPreviewPropertyPage.ABSOLUTE_BASED_TYPE);
		page.setValue(url);
		page.setTitle(name);
		page.createControl(parent);
		page.setBrowser(browser, name);
		page.showBrowserArea();
		return page;
	}

}

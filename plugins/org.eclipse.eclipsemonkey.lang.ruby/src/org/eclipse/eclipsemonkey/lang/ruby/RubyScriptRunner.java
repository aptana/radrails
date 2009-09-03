package org.eclipse.eclipsemonkey.lang.ruby;
/**
 * Copyright (c) 2007 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.eclipsemonkey.DOMDescriptor;
import org.eclipse.eclipsemonkey.EclipseMonkeyPlugin;
import org.eclipse.eclipsemonkey.IMonkeyScriptRunner;
import org.eclipse.eclipsemonkey.RunMonkeyException;
import org.eclipse.eclipsemonkey.ScriptMetadata;
import org.eclipse.eclipsemonkey.StoredScript;
import org.eclipse.eclipsemonkey.dom.IMonkeyDOMFactory;
import org.eclipse.eclipsemonkey.dom.Utilities;
import org.eclipse.eclipsemonkey.lang.ruby.doms.IRubyDOMFactory;
import org.eclipse.osgi.service.environment.Constants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.Java;
import org.jruby.javasupport.JavaObject;
import org.jruby.javasupport.JavaUtil;
import org.jruby.runtime.GlobalVariable;
import org.jruby.runtime.builtin.IRubyObject;
import org.osgi.framework.Bundle;

/**
 * @author Chris Williams
 */
public class RubyScriptRunner implements IMonkeyScriptRunner {	

	private static final String DOM_EXTENSION_POINT = "org.eclipse.eclipsemonkey.dom";
	private static final String CLASS = "class";
	private static final String VARIABLE_NAME = "variableName";
	
	private static final String RUBY_DOM_EXTENSION_POINT = "org.eclipse.eclipsemonkey.lang.ruby.ruby_dom";
	private static final String BASED_ON = "basedOn";

	private IWorkbenchWindow window;
	private IPath path;
	private StoredScript storedScript;

	private static Ruby fgRuby;

	public RubyScriptRunner(IPath path, IWorkbenchWindow window) {
		this.path = path;

		if (window == null) {
			this.window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		} else {
			this.window = window;
		}
	}

	public StoredScript getStoredScript() {
		return storedScript;
	}

	public Object run(String entryName, Object[] functionArgs)
			throws RunMonkeyException {
		String fileName = this.path.toPortableString();
		try {

			Map scriptStore = EclipseMonkeyPlugin.getDefault().getScriptStore();
			storedScript = (StoredScript) (scriptStore.get(fileName));

			if (!storedScript.metadata.ensure_doms_are_loaded(window)) {
				return null;
			}

			String script = Utilities.getFileContents(path);
			Ruby ruby = getJRubyInstance();
			defineStandardGlobalVariables(ruby);
			List/*<ExtensionDOMLoader>*/ loaders = new ArrayList/*<ExtensionDOMLoader>*/();
			loaders.add(new StandardDOMLoader(ruby, storedScript.metadata));
			loaders.add(new ExtensionRubyDOMLoader(ruby, storedScript.metadata));
//			for (ExtensionDOMLoader loader : loaders) {
//				loader.run();
//			}			
			for (Iterator iter = loaders.iterator(); iter.hasNext();) {
				ExtensionDOMLoader loader = (ExtensionDOMLoader) iter.next();
				loader.run();
			}
			ruby.setCurrentDirectory(path.toFile().getParent());
			
			IRubyObject result = ruby.evalScriptlet(script);
			return result;
		} catch (CoreException e) {
			error(e, fileName, "Core Exception");
		} catch (IOException e) {
			error(e, fileName, "IO error");
		} catch (RaiseException e) {
			error(e, fileName, e.getException().message.toString());
		} catch (RuntimeException e) {
			e.printStackTrace();
			error(e, fileName, e.getLocalizedMessage());
		}
		return null;
	}

	private static Ruby getJRubyInstance() {
		if (fgRuby == null) {
			RubyInstanceConfig config = new RubyInstanceConfig();
			PrintStream out = new PrintStream(RubyScriptConsole
					.getConsoleStream());
			config.setOutput(out);
			fgRuby = Ruby.newInstance(config);
			File jrubyHome = getIncludedJRuby();
			if (jrubyHome != null)
				fgRuby.setJRubyHome(jrubyHome.getAbsolutePath());
			fgRuby.getLoadService().init(new ArrayList());
		}
		return fgRuby;
	}
	
	private static File getIncludedJRuby() {
		try {
			Bundle bundle = Platform.getBundle("org.jruby");
			URL url = FileLocator.find(bundle, new Path(""), null);
			url = FileLocator.toFileURL(url);
			String fileName = url.getFile();
			File file = new File(fileName);
			if (!file.exists()) {
				return null;
			}
			return file;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private void defineStandardGlobalVariables(Ruby ruby) {
		defineGlobal(ruby, "window", window);
	}

	private void defineGlobal(Ruby ruby, String name, Object value) {
		IRubyObject object = JavaUtil.convertJavaToRuby(ruby, value);
		if (object instanceof JavaObject) {
			object = Java.wrap(ruby.getJavaSupport().getJavaUtilitiesModule(),
					object);
		}
		ruby.defineVariable(new GlobalVariable(ruby, GlobalVariable
				.variableName(name), object));
	}

	private void error(Exception x, String fileName, String string)
			throws RunMonkeyException {
		RunMonkeyException e = new RunMonkeyException(x.getClass().getName(),
				fileName, null, string);
		PrintStream cs = new PrintStream(RubyScriptConsole.getConsoleStream());
		cs.println(string);
		cs.println(x.getLocalizedMessage());
		x.printStackTrace(cs);
		throw e;
	}
	
	private abstract class ExtensionDOMLoader {
		protected ScriptMetadata metadata;
		protected Ruby ruby;
		
		public ExtensionDOMLoader(Ruby ruby, ScriptMetadata metadata) {
			this.ruby = ruby;
			this.metadata = metadata;
		}
		
		public void run() throws IOException {
			IExtension[] extensions = getExtensions(getExtensionPoint());
			for (int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] configurations = extension.getConfigurationElements();
				for (int j = 0; j < configurations.length; j++) {
					IConfigurationElement element = configurations[j];
					try {
						IExtension declaring = element.getDeclaringExtension();
						String declaring_plugin_id = declaring.getNamespaceIdentifier();

						if (metadata.containsDOM_by_plugin(declaring_plugin_id)) {
							checkExtensionPointSpecifics(element);
							String variableName = element.getAttribute(VARIABLE_NAME);
							Object object = element.createExecutableExtension(CLASS);							
							defineGlobal(ruby, variableName, getRootObject(object));
						}
					} catch (InvalidRegistryObjectException x) {
						// ignore bad extensions
					} catch (CoreException x) {
						// ignore bad extensions
					}
				}
			}
		}		
		
		private IExtension[] getExtensions(String extensionPoint) {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint point = registry.getExtensionPoint(extensionPoint);

			if (point != null)
				return point.getExtensions();
			return new IExtension[0];
		}

		protected abstract Object getRootObject(Object object);
		
		protected abstract String getExtensionPoint();

		protected abstract void checkExtensionPointSpecifics(IConfigurationElement element) throws IOException;
	}
	
	private class StandardDOMLoader extends ExtensionDOMLoader {

		public StandardDOMLoader(Ruby ruby, ScriptMetadata metadata) {
			super(ruby, metadata);
		}

//		@Override
		protected String getExtensionPoint() {
			return DOM_EXTENSION_POINT;
		}
		
//		@Override
		protected void checkExtensionPointSpecifics(IConfigurationElement element) throws IOException {
			return; // do nothing
		}		
		
		protected Object getRootObject(Object object) {
			IMonkeyDOMFactory factory = (IMonkeyDOMFactory) object;
			return factory.getDOMroot();
		}
	}
	
	private class ExtensionRubyDOMLoader extends ExtensionDOMLoader {
		public ExtensionRubyDOMLoader(Ruby ruby, ScriptMetadata metadata) {
			super(ruby, metadata);
		}

		protected void checkExtensionPointSpecifics(IConfigurationElement element) throws IOException {
			String basedOnDOM = element.getAttribute(BASED_ON);

			if (basedOnDOM != null
					&& basedOnDOM.trim().length() > 0) {
				Pattern p = Pattern.compile(
						"\\s*(\\p{Graph}+)\\/((\\p{Alnum}|\\.)+)",
						Pattern.DOTALL);
				Matcher m = p.matcher(basedOnDOM);
				while (m.find()) {
					metadata.getDOMs().add(
							new DOMDescriptor(m.group(1), m
									.group(2)));
				}

				if (metadata.ensure_doms_are_loaded(window) == false) {
					throw new IOException(
							"Cannot load the required DOM extension:\n\n"
									+ basedOnDOM + "\n");
				}
			}
		}
		
		protected Object getRootObject(Object object) {
			IRubyDOMFactory factory = (IRubyDOMFactory) object;
			return factory.getDOMroot(ruby);
		}
		
//		@Override
		protected String getExtensionPoint() {
			return RUBY_DOM_EXTENSION_POINT;
		}
	}

}

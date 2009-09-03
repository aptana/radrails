package org.radrails.rails.ui.text;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.radrails.rails.core.RailsLog;
import org.rubypeople.rdt.core.IMethod;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.RubyCore;
import org.rubypeople.rdt.core.RubyModelException;
import org.rubypeople.rdt.core.util.Util;

public class RailsHeuristicCompletionComputer {

	private static final String CONTROLLER_FILE_SUFFIX = "_controller.rb";
	
	public static Map<String, File> getControllerCompletions(File controllersFolder, IDocument doc, int offset) {
		Map<String, File> list = new HashMap<String, File>();
		if (controllersFolder == null) return list;
		
		String fullPrefix = getFullPrefix(doc, offset);
		if (!looksLikeControllerCompletion(fullPrefix)) return list;
		
		File[] controllers = controllersFolder.listFiles(new FilenameFilter() {
		
			public boolean accept(File dir, String name) {
				return name.endsWith(CONTROLLER_FILE_SUFFIX);
			}
		
		});
		for (int i = 0; i < controllers.length; i++) {
			File controller = controllers[i];
			String name = controller.getName().substring(0, controller.getName().length() - CONTROLLER_FILE_SUFFIX.length());
			String replacement = surroundWithQuotes(offset, doc, fullPrefix, name);
			list.put(replacement, controller);
		}
		return list;
	}
	
	private static String surroundWithQuotes(int offset, IDocument doc, String fullPrefix, String replacement) {
		// Only add these closing quotes if next char is not a closing quote
		char next = 'a';
		try {
			if (offset < doc.getLength())
				next = doc.getChar(offset);
		} catch (BadLocationException e) {
			// ignore
		}
		if (fullPrefix.endsWith("'")) {
			if (next != '\'') replacement = replacement + "'";
		} else if (fullPrefix.endsWith("\"")) {
			if (next != '"') replacement = replacement + "\"";
		} else {
			replacement = "'" + replacement + "'";
		}
		return replacement;
	}

	private static boolean looksLikeControllerCompletion(String prefix) {
		return Pattern.matches(".*:controller\\s*=>\\s*['|\"]?", prefix);
	}
	
	private static String getFullPrefix(IDocument doc, int offset) {
		int length = 0;
		String prefix = "";
		try {
			while((offset - length > 0) && Pattern.matches("[^\\n|^\\r|^;]", doc.get(offset - length - 1, 1))) {
				length++;
			}
			prefix = doc.get(offset - length, length);
		} catch (BadLocationException e) {
			// ignore
		}
		return prefix;
	}
	
	
	public static Map<String, File> getActionCompletions(File controllersFolder, IDocument doc, int offset) {
		Map<String, File> list = new HashMap<String, File>();
		if (controllersFolder == null) return list;
		
		String fullPrefix = getFullPrefix(doc, offset);
		if (!looksLikeActionCompletion(fullPrefix)) return list;
		//  Extract the controller name
		String controllerName = getControllerName(fullPrefix);		
		// Grab that controller
		File controller = getController(controllersFolder, controllerName);
		if (controller == null) return list;
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(controller.getAbsolutePath()));		
		IRubyScript script = RubyCore.create(file);
		String typeName = Util.underscoresToCamelCase(handleNamespacing(controllerName)) + "Controller";
		try {
			IType type = script.getType(typeName);
			if (type == null) return list;
			// Grab all the public methods
			IMethod[] methods = type.getMethods();
			for (int i = 0; i < methods.length; i++) {
				if (methods[i].isPublic()) {
					String name = methods[i].getElementName();
					String replacement = surroundWithQuotes(offset, doc, fullPrefix, name);
					list.put(replacement, controller);
				}
			}
		} catch (RubyModelException e) {
			RailsLog.log(e);
		}
		return list;
	}

	private static String handleNamespacing(String controllerName) {
		String[] parts = controllerName.split("[\\|/]");
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < parts.length; i++) {
			if (i > 0) buffer.append("::");
			String upper = Character.toUpperCase(parts[i].charAt(0)) + parts[i].substring(1);
			buffer.append(upper);
		}
		return buffer.toString();
	}

	private static File getController(File controllersFolder, String controllerName) {
		String[] parts = controllerName.split("[\\|/]");
		controllerName = parts[parts.length - 1];
		for (int i = 0; i < parts.length - 1; i++) {
			controllersFolder = new File(controllersFolder, parts[i]);
			if (!controllersFolder.exists()) return null;
		}
		
		File[] controllers = controllersFolder.listFiles(new FilenameFilter() {
			
			public boolean accept(File dir, String name) {
				return name.endsWith(CONTROLLER_FILE_SUFFIX);
			}
		
		});
		for (int i = 0; i < controllers.length; i++) {
			File controller = controllers[i];
			String name = controller.getName().substring(0, controller.getName().length() - CONTROLLER_FILE_SUFFIX.length());
			if (name.equals(controllerName)) return controller;
		}
		return null;
	}

	private static String getControllerName(String prefix) {
		Pattern pat = Pattern.compile(".*:controller\\s*=>\\s*['|\"|:]?([\\w|\\|/]+)?['|\"]?");
		Matcher matcher = pat.matcher(prefix);
		if (!matcher.find()) {
			return null;
		}
		return matcher.group(1);
	}

	private static boolean looksLikeActionCompletion(String prefix) {
		return Pattern.matches(".*:action\\s*=>\\s*['|\"]?", prefix);
	}

}

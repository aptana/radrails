package org.radrails.rails.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.radrails.rails.internal.core.RailsPlugin;

public abstract class RailsConventions {
		
	private static final String APP = "app";

	private static final String RB = ".rb";
	
	private static final String MODELS = "models";
	private static final String VIEWS = "views";
	private static final String CONTROLLERS = "controllers";
	private static final String HELPERS = "helpers";
	private static final String TEST = "test";
	private static final String FUNCTIONAL = "functional";
	private static final String UNIT = "unit";
		
	private static final String CONTROLLER_SUFFIX = "_controller";
	private static final String CONTROLLER_FILE_SUFFIX = CONTROLLER_SUFFIX + RB;
	private static final String TEST_SUFFIX = "_test";
	private static final String FUNCTIONAL_TEST_SUFFIX = CONTROLLER_SUFFIX + TEST_SUFFIX + RB;
	private static final String UNIT_TEST_SUFFIX = TEST_SUFFIX + RB;
	private static final String HELPER_SUFFIX = "_helper";
	private static final String HELPER_FILE_SUFFIX = HELPER_SUFFIX + RB;
	
	public static IFile getModelFromController(IFile controllerFile) {
		if (!looksLikeController(controllerFile)) return null; // if we're not in a controller, just return
		IPath controllerFilePath = controllerFile.getProjectRelativePath();
		String controllerFilename = controllerFilePath.lastSegment();
		
		IPath model = getModelFromController(controllerFilePath, getModelName(controllerFilename));
		IFile file = controllerFile.getProject().getFile(model);
		if (!file.exists()) return null;
		return file;
	}
	
	public static IFile getControllerFromModel(IFile modelFile) {
		IPath modelFilePath = modelFile.getProjectRelativePath();
		String modelFilename = modelFilePath.lastSegment();
		if (!looksLikeModel(modelFile)) return null;
		String singular = modelFilename.substring(0, modelFilename.indexOf('.'));
		String plural = Inflector.pluralize(singular);
		
		String controllerName = plural + CONTROLLER_FILE_SUFFIX;
		IPath controllerPath = buildController(getAppFolderFromModel(modelFilePath), controllerName);
		
		IFile file = modelFile.getProject().getFile(controllerPath);
		if (!file.exists()) return null;
		return file;
	}

	private static String getModelName(String controllerFilename) {
		String singular = Inflector.singularize(getControllerBaseName(controllerFilename));
		singular += RB;
		return singular;
	}

	private static IPath getModelFromController(IPath controllerFilePath, String singular) {
		return getAppFolderFromController(controllerFilePath).append(MODELS).append(singular);
	}
	
	private static IPath getModelFromHelper(IPath helperFilePath, String singular) {
		return getAppFolderFromHelper(helperFilePath).append(MODELS).append(singular);
	}

	private static String getControllerBaseName(String controllerFilename) {
		return controllerFilename.substring(0, controllerFilename.length() - CONTROLLER_FILE_SUFFIX.length());
	}

	public static IFile getModelFromView(IFile viewFile) {		
		if (!looksLikeView(viewFile)) return null; // if we're not in a view, just return
		IPath viewFilePath = viewFile.getProjectRelativePath();
		IPath model = getModelFromView(viewFilePath);
		return viewFile.getProject().getFile(model);
	}

	public static boolean looksLikeView(IFile file) {		
		if (file == null) return false;
		IPath railsRoot = RailsPlugin.findRailsRoot(file.getProject());
		if (!railsRoot.isPrefixOf(file.getProjectRelativePath())) return false;
		IPath afterRoot = file.getProjectRelativePath().removeFirstSegments(railsRoot.segmentCount());
		return afterRoot.segment(0).equals(APP) && afterRoot.segment(1).equals(VIEWS);
	}
	
	private static String getModelNameFromViewPath(IPath viewFilePath) {
		String singular = Inflector.singularize(getPluralResourceNameFromViewPath(viewFilePath));
		singular += RB;
		return singular;
	}

	private static String getPluralResourceNameFromViewPath(IPath viewFilePath) {
		IFile viewFile = ResourcesPlugin.getWorkspace().getRoot().getFile(viewFilePath);		
		IPath railsRoot = RailsPlugin.findRailsRoot(viewFile.getProject());
		IPath appViews = railsRoot.append(APP).append(VIEWS);
		return viewFilePath.removeFirstSegments(appViews.segmentCount()).removeLastSegments(1).toPortableString();
	}

	private static IPath getModelFromView(IPath viewFilePath) {
		String singular = getModelNameFromViewPath(viewFilePath);
		return getAppFolderFromView(viewFilePath).append(MODELS).append(singular);
	}

	public static IFile getControllerFromView(IFile viewFile) {		
		if (!looksLikeView(viewFile)) return null;
		IPath railsRoot = RailsPlugin.findRailsRoot(viewFile.getProject());
		IPath viewFilePath = viewFile.getProjectRelativePath();
		String plural = getPluralResourceNameFromViewPath(viewFilePath);		
		String[] namespace = getControllerNamespace(viewFilePath);
		
		String controllerName = new Path(plural).lastSegment() + CONTROLLER_FILE_SUFFIX;
		IPath controllerPath = railsRoot.append(APP).append(CONTROLLERS);
		for (int i = 0; i < namespace.length; i++) {
			controllerPath = controllerPath.append(namespace[i]);
		}
		controllerPath = controllerPath.append(controllerName);
		
		IFile file = viewFile.getProject().getFile(controllerPath);
		if (!file.exists()) return null;
		return file;
	}

	private static IPath buildController(IPath appFolder, String controllerName) {
		return appFolder.append(CONTROLLERS).append(controllerName);
	}

	private static IPath getAppFolderFromView(IPath viewFilePath) {
		String[] segments = viewFilePath.segments();
		for (int i = 0; i < segments.length; i++)
		{
			if (segments[i].equals(APP))
			{
				return viewFilePath.uptoSegment(i + 1);
			}			
		}
		return viewFilePath.removeLastSegments(3);
	}
	
	private static IPath getAppFolderFromController(IPath controllerFilePath) {
		return controllerFilePath.removeLastSegments(2);
	}
	
	private static IPath getAppFolderFromModel(IPath modelFilePath) {
		return modelFilePath.removeLastSegments(2);
	}

	public static IFile getFunctionalTestFromView(IFile viewFile) {		
		if (!looksLikeView(viewFile)) return null;	
		IPath viewFilePath = viewFile.getProjectRelativePath();
		String plural = getPluralResourceNameFromViewPath(viewFilePath);
		IPath functionalTestPath = buildFunctionalTest(getAppFolderFromView(viewFilePath), plural);
		return viewFile.getProject().getFile(functionalTestPath);
	}

	private static IPath buildFunctionalTest(IPath appFolder, String plural) {
		return appFolder.removeLastSegments(1).append(TEST).append(FUNCTIONAL).append(plural + FUNCTIONAL_TEST_SUFFIX);
	}

	public static boolean looksLikeController(IFile currentFile) {
		if (currentFile == null) return false;
		String name = currentFile.getName();
		if (name.endsWith(CONTROLLER_FILE_SUFFIX)) return true;
		return false;
	}

	public static boolean looksLikeHelper(IFile currentFile) {
		if (currentFile == null) return false;
		String name = currentFile.getName();
		if (name.endsWith(HELPER_FILE_SUFFIX)) return true;
		return false;
	}

	public static IFile getHelperFromModel(IFile modelFile) {
		IPath modelFilePath = modelFile.getProjectRelativePath();
		String modelFilename = modelFilePath.lastSegment();
		if (!looksLikeModel(modelFile)) return null;
		String singular = modelFilename.substring(0, modelFilename.indexOf('.'));
		String plural = Inflector.pluralize(singular);
		
		String helperName = plural + HELPER_FILE_SUFFIX;
		IPath helperPath = buildHelper(getAppFolderFromModel(modelFilePath), helperName);
		
		IFile file = modelFile.getProject().getFile(helperPath);
		if (!file.exists()) return null;
		return file;
	}
	
	private static IPath buildHelper(IPath appFolder, String helperName) {
		return appFolder.append(HELPERS).append(helperName);
	}

	public static IFile getHelperFromView(IFile viewFile) {		
		if (!looksLikeView(viewFile)) return null;
		IPath viewFilePath = viewFile.getProjectRelativePath();
		String plural = getPluralResourceNameFromViewPath(viewFilePath);
		
		IPath helperPath = buildHelper(getAppFolderFromView(viewFilePath), plural + HELPER_FILE_SUFFIX);		
		IFile file = viewFile.getProject().getFile(helperPath);
		if (!file.exists()) return null;
		return file;
	}

	public static IFile getHelperFromController(IFile controllerFile) {
		if (!looksLikeController(controllerFile)) return null; // if we're not in a controller, just return
		IPath controllerFilePath = controllerFile.getProjectRelativePath();
		String controllerFilename = controllerFilePath.lastSegment();
		
		String plural = getControllerBaseName(controllerFilename);
		String helperName = plural + HELPER_FILE_SUFFIX;
		IPath helperPath = buildHelper(getAppFolderFromController(controllerFilePath), helperName);
		
		IFile file = controllerFile.getProject().getFile(helperPath);
		if (!file.exists()) return null;
		return file;
	}

	public static IFile getControllerFromHelper(IFile helperFile) {
		if (!looksLikeHelper(helperFile)) return null;
		IPath helperFilePath = helperFile.getProjectRelativePath();		
		String plural = getPluralResourceNameFromHelperPath(helperFilePath.lastSegment());
		
		String controllerName = plural + CONTROLLER_FILE_SUFFIX;
		IPath controllerPath = buildController(getAppFolderFromHelper(helperFilePath), controllerName);
		
		IFile file = helperFile.getProject().getFile(controllerPath);
		if (!file.exists()) return null;
		return file;
	}

	private static String getPluralResourceNameFromHelperPath(String helperFileName) {
		return helperFileName.substring(0, helperFileName.length() - HELPER_FILE_SUFFIX.length());
	}

	private static IPath getAppFolderFromHelper(IPath helperFilePath) {
		return helperFilePath.removeLastSegments(2);
	}

	public static IFile getModelFromHelper(IFile helperFile) {
		if (!looksLikeHelper(helperFile)) return null; // if we're not in a helper, just return
		IPath helperFilePath = helperFile.getProjectRelativePath();
		String helperFilename = helperFilePath.lastSegment();
		
		String modelName = Inflector.singularize(getPluralResourceNameFromHelperPath(helperFilename))+ RB;		
		IPath model = getModelFromHelper(helperFilePath, modelName);
		IFile file = helperFile.getProject().getFile(model);
		if (!file.exists()) return null;
		return file;
	}

	public static IFile getControllerFromFunctionalTest(IFile currentFile) {
		if (!looksLikeFunctionalTest(currentFile)) return null; // if we're not in a functional test, just return
		IPath helperFilePath = currentFile.getProjectRelativePath();
		String filename = helperFilePath.lastSegment();
		
		String[] namespace = getControllerNamespace(helperFilePath);
		IPath railsRoot = RailsPlugin.findRailsRoot(currentFile.getProject());
		String controllerName = filename.substring(0, filename.length() - (TEST_SUFFIX + RB).length()) + RB;		
		IPath controller = railsRoot.append(APP).append(CONTROLLERS);
		for (int i = 0; i < namespace.length; i++) {
			controller = controller.append(namespace[i]);
		}
		controller = controller.append(controllerName);
		IFile file = currentFile.getProject().getFile(controller);
		if (!file.exists()) return null;
		return file;
	}

	private static String[] getControllerNamespace(IPath helperFilePath) {
		String[] segments = helperFilePath.segments();
		List<String> namespace = new ArrayList<String>();
		for (int i = segments.length - 2; i >=0; i--) {
			if (segments[i].equals(VIEWS)) {
				namespace.remove(0);
				break;
			}
			if (segments[i].equals(FUNCTIONAL) || segments[i].equals(CONTROLLERS)) {
				break;
			}
			namespace.add(segments[i]);
		}
		Collections.reverse(namespace);
		return namespace.toArray(new String[namespace.size()]);
	}

	private static boolean looksLikeFunctionalTest(IFile currentFile) {
		IPath railsRoot = RailsPlugin.findRailsRoot(currentFile.getProject());
		if (!railsRoot.isPrefixOf(currentFile.getProjectRelativePath())) return false;
		IPath relativeToRailsRoot = currentFile.getProjectRelativePath().removeFirstSegments(railsRoot.segmentCount());
		if (!relativeToRailsRoot.segment(0).equals(TEST)) return false;
		if (!relativeToRailsRoot.segment(1).equals(FUNCTIONAL)) return false;
		return relativeToRailsRoot.lastSegment().endsWith(TEST_SUFFIX + RB);
	}

	public static IFile getControllerFromUnitTest(IFile currentFile) {
		if (!looksLikeUnitTest(currentFile)) return null; // if we're not in a unit test, just return
		String fileName = currentFile.getProjectRelativePath().lastSegment();
				
		IPath railsRoot = RailsPlugin.findRailsRoot(currentFile.getProject());
		String singularModel = fileName.substring(0, fileName.length() - (TEST_SUFFIX + RB).length());
		String controllerName = Inflector.pluralize(singularModel) + CONTROLLER_FILE_SUFFIX;
		IPath controller = railsRoot.append(APP).append(CONTROLLERS).append(controllerName);
		IFile file = currentFile.getProject().getFile(controller);
		if (!file.exists()) return null;
		return file;
	}

	private static boolean looksLikeUnitTest(IFile currentFile) {
		IPath railsRoot = RailsPlugin.findRailsRoot(currentFile.getProject());
		if (!railsRoot.isPrefixOf(currentFile.getProjectRelativePath())) return false;
		IPath relativeToRailsRoot = currentFile.getProjectRelativePath().removeFirstSegments(railsRoot.segmentCount());
		if (!relativeToRailsRoot.segment(0).equals(TEST)) return false;
		if (!relativeToRailsRoot.segment(1).equals(UNIT)) return false;
		return relativeToRailsRoot.lastSegment().endsWith(TEST_SUFFIX + RB);
	}

	public static IFile getModelFromFunctionalTest(IFile currentFile) {
		if (!looksLikeFunctionalTest(currentFile)) return null; // if we're not in a functional test, just return
		IPath filePath = currentFile.getProjectRelativePath();
		String fileName = filePath.lastSegment();
		for (int i = 0; i < filePath.segmentCount(); i++)
		{
			if (filePath.segment(i).equals(FUNCTIONAL))
			{
				fileName = filePath.removeFirstSegments(i + 1).toPortableString();
				break;
			}
		}
		String pluralModelName = fileName.substring(0, fileName.length() - (FUNCTIONAL_TEST_SUFFIX).length());
		String modelName = Inflector.singularize(pluralModelName) + RB;
		IPath model = new Path(APP).append(MODELS).append(modelName);
		IFile file = currentFile.getProject().getFile(model);
		if (!file.exists()) return null;
		return file;
	}
	
	public static IFile getModelFromUnitTest(IFile currentFile) {
		if (!looksLikeUnitTest(currentFile)) return null; // if we're not in a unit test, just return
		IPath filePath = currentFile.getProjectRelativePath();
		String fileName = filePath.lastSegment();
				
		String modelName = fileName.substring(0, fileName.length() - (TEST_SUFFIX + RB).length()) + RB;
		IPath model = new Path(APP).append(MODELS).append(modelName);
		IFile file = currentFile.getProject().getFile(model);
		if (!file.exists()) return null;
		return file;
	}

	public static IFile getHelperFromFunctionalTest(IFile currentFile) {
		if (!looksLikeFunctionalTest(currentFile)) return null; // if we're not in a functional test, just return
		IPath filePath = currentFile.getProjectRelativePath();
		String fileName = filePath.lastSegment();
			
		IPath railsRoot = RailsPlugin.findRailsRoot(currentFile.getProject());
		String pluralModelName = fileName.substring(0, fileName.length() - (FUNCTIONAL_TEST_SUFFIX).length());
		String helperName = pluralModelName + HELPER_FILE_SUFFIX;
		IPath helper = railsRoot.append(APP).append(HELPERS).append(helperName);
		IFile file = currentFile.getProject().getFile(helper);
		if (!file.exists()) return null;
		return file;
	}

	public static IFile getHelperFromUnitTest(IFile currentFile) {
		if (!looksLikeUnitTest(currentFile)) return null; // if we're not in a unit test, just return
		IPath filePath = currentFile.getProjectRelativePath();
		String fileName = filePath.lastSegment();
				
		String modelName = fileName.substring(0, fileName.length() - (TEST_SUFFIX + RB).length());
		String helperName = Inflector.pluralize(modelName) + HELPER_FILE_SUFFIX;
		IPath helper = new Path(APP).append(HELPERS).append(helperName);
		IFile file = currentFile.getProject().getFile(helper);
		if (!file.exists()) return null;
		return file;
	}

	public static boolean looksLikeModel(IFile currentFile) {	
		if (currentFile == null) return false;
		IPath path = currentFile.getProjectRelativePath();
		int index = findAppIndex(path);
		if (index == -1) return false;
		if (index + 2 > path.segmentCount()) return false; // need at least 2 more segments past "app" ("models", and model file)
		String appSubfolder = path.segment(index + 1);
		if (!appSubfolder.equals(MODELS)) return false;
		String modelFilename = path.lastSegment();
		return modelFilename.endsWith(RB);
		// TODO Check the type contained for subtype of ActiveRecord::Base?
	}

	public static boolean looksLikeTest(IFile currentFile) {
		if (currentFile == null) return false;
		IPath path = currentFile.getProjectRelativePath();		
		int index = findTestIndex(path);
		if (index == -1) return false;
		if (index + 2 > path.segmentCount()) return false; // need at least 2 more segments past "test" ("unit"/"functional", and test file)
		String appSubfolder = path.segment(index + 1);
		if (appSubfolder.equals(UNIT)) {
			return path.lastSegment().endsWith(UNIT_TEST_SUFFIX);
		}
		if (appSubfolder.equals(FUNCTIONAL)) {
			return path.lastSegment().endsWith(FUNCTIONAL_TEST_SUFFIX);
		}
		return false;
	}

	private static int findAppIndex(IPath path) {
		return findSegmentIndex(path, APP);
	}
	
	private static int findTestIndex(IPath path) {
		return findSegmentIndex(path, TEST);
	}
	
	private static int findSegmentIndex(IPath path, String segmentToFind) {
		for (int i = 0; i < path.segmentCount(); i++) {
			String segment = path.segment(i);
			if (segment.equals(segmentToFind)) {
				return i;
			}
		}
		return -1;
	}
}

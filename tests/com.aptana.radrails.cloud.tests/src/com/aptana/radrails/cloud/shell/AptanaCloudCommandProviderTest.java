package com.aptana.radrails.cloud.shell;

import java.net.URI;
import java.util.Map;

import javax.net.ssl.SSLEngineResult.Status;

import junit.framework.TestCase;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentTypeMatcher;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.rubypeople.rdt.launching.ITerminal;

public class AptanaCloudCommandProviderTest extends TestCase
{

	/**
	 * ROR-1289 - entering "apcloudify ." by hand in a rails shell results in an error when there's a space in paths
	 */
	public void testApcloudifyCommandsWrapProjectsWithSpacesInPath()
	{
		final IPath projectPath = new Path("/this is/a/path/with spaces");
		AptanaCloudCommandProvider provider = new AptanaCloudCommandProvider()
		{
			@Override
			protected void launchInsideShell(ITerminal shell, String command, Map<String, String> env,
					Map<String, Object> attrs)
			{
				assertEquals("apcloudify \"" + projectPath.toOSString() + "\"", command);
			}
			
			@Override
			protected IStatus installCloudGemIfNecessary()
			{
				return org.eclipse.core.runtime.Status.OK_STATUS;
			}

			@Override
			protected IProject getProject()
			{
				return new IProject()
				{

					public void build(int kind, IProgressMonitor monitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void build(int kind, String builderName, Map args, IProgressMonitor monitor)
							throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void close(IProgressMonitor monitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void create(IProgressMonitor monitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void create(IProjectDescription description, IProgressMonitor monitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void create(IProjectDescription description, int updateFlags, IProgressMonitor monitor)
							throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void delete(boolean deleteContent, boolean force, IProgressMonitor monitor)
							throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public IContentTypeMatcher getContentTypeMatcher() throws CoreException
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IProjectDescription getDescription() throws CoreException
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IFile getFile(String name)
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IFolder getFolder(String name)
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IProjectNature getNature(String natureId) throws CoreException
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IPath getPluginWorkingLocation(IPluginDescriptor plugin)
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IProject[] getReferencedProjects() throws CoreException
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IProject[] getReferencingProjects()
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IPath getWorkingLocation(String id)
					{
						// TODO Auto-generated method stub
						return null;
					}

					public boolean hasNature(String natureId) throws CoreException
					{
						// TODO Auto-generated method stub
						return false;
					}

					public boolean isNatureEnabled(String natureId) throws CoreException
					{
						// TODO Auto-generated method stub
						return false;
					}

					public boolean isOpen()
					{
						// TODO Auto-generated method stub
						return false;
					}

					public void move(IProjectDescription description, boolean force, IProgressMonitor monitor)
							throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void open(IProgressMonitor monitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void open(int updateFlags, IProgressMonitor monitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void setDescription(IProjectDescription description, IProgressMonitor monitor)
							throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void setDescription(IProjectDescription description, int updateFlags,
							IProgressMonitor monitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public boolean exists(IPath path)
					{
						// TODO Auto-generated method stub
						return false;
					}

					public IFile[] findDeletedMembersWithHistory(int depth, IProgressMonitor monitor)
							throws CoreException
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IResource findMember(String name)
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IResource findMember(IPath path)
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IResource findMember(String name, boolean includePhantoms)
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IResource findMember(IPath path, boolean includePhantoms)
					{
						// TODO Auto-generated method stub
						return null;
					}

					public String getDefaultCharset() throws CoreException
					{
						// TODO Auto-generated method stub
						return null;
					}

					public String getDefaultCharset(boolean checkImplicit) throws CoreException
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IFile getFile(IPath path)
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IFolder getFolder(IPath path)
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IResource[] members() throws CoreException
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IResource[] members(boolean includePhantoms) throws CoreException
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IResource[] members(int memberFlags) throws CoreException
					{
						// TODO Auto-generated method stub
						return null;
					}

					public void setDefaultCharset(String charset) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void setDefaultCharset(String charset, IProgressMonitor monitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void accept(IResourceVisitor visitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void accept(IResourceProxyVisitor visitor, int memberFlags) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void accept(IResourceVisitor visitor, int depth, boolean includePhantoms)
							throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void accept(IResourceVisitor visitor, int depth, int memberFlags) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void clearHistory(IProgressMonitor monitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void copy(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void copy(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void copy(IProjectDescription description, boolean force, IProgressMonitor monitor)
							throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void copy(IProjectDescription description, int updateFlags, IProgressMonitor monitor)
							throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public IMarker createMarker(String type) throws CoreException
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IResourceProxy createProxy()
					{
						// TODO Auto-generated method stub
						return null;
					}

					public void delete(boolean force, IProgressMonitor monitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void delete(int updateFlags, IProgressMonitor monitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void deleteMarkers(String type, boolean includeSubtypes, int depth) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public boolean exists()
					{
						// TODO Auto-generated method stub
						return false;
					}

					public IMarker findMarker(long id) throws CoreException
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IMarker[] findMarkers(String type, boolean includeSubtypes, int depth) throws CoreException
					{
						// TODO Auto-generated method stub
						return null;
					}

					public int findMaxProblemSeverity(String type, boolean includeSubtypes, int depth)
							throws CoreException
					{
						// TODO Auto-generated method stub
						return 0;
					}

					public String getFileExtension()
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IPath getFullPath()
					{
						// TODO Auto-generated method stub
						return null;
					}

					public long getLocalTimeStamp()
					{
						// TODO Auto-generated method stub
						return 0;
					}

					public IPath getLocation()
					{
						return projectPath;
					}

					public URI getLocationURI()
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IMarker getMarker(long id)
					{
						// TODO Auto-generated method stub
						return null;
					}

					public long getModificationStamp()
					{
						// TODO Auto-generated method stub
						return 0;
					}

					public String getName()
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IContainer getParent()
					{
						// TODO Auto-generated method stub
						return null;
					}

					public Map getPersistentProperties() throws CoreException
					{
						// TODO Auto-generated method stub
						return null;
					}

					public String getPersistentProperty(QualifiedName key) throws CoreException
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IProject getProject()
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IPath getProjectRelativePath()
					{
						// TODO Auto-generated method stub
						return null;
					}

					public IPath getRawLocation()
					{
						// TODO Auto-generated method stub
						return null;
					}

					public URI getRawLocationURI()
					{
						// TODO Auto-generated method stub
						return null;
					}

					public ResourceAttributes getResourceAttributes()
					{
						// TODO Auto-generated method stub
						return null;
					}

					public Map getSessionProperties() throws CoreException
					{
						// TODO Auto-generated method stub
						return null;
					}

					public Object getSessionProperty(QualifiedName key) throws CoreException
					{
						// TODO Auto-generated method stub
						return null;
					}

					public int getType()
					{
						// TODO Auto-generated method stub
						return 0;
					}

					public IWorkspace getWorkspace()
					{
						// TODO Auto-generated method stub
						return null;
					}

					public boolean isAccessible()
					{
						// TODO Auto-generated method stub
						return false;
					}

					public boolean isDerived()
					{
						// TODO Auto-generated method stub
						return false;
					}

					public boolean isDerived(int options)
					{
						// TODO Auto-generated method stub
						return false;
					}

					public boolean isHidden()
					{
						// TODO Auto-generated method stub
						return false;
					}

					public boolean isLinked()
					{
						// TODO Auto-generated method stub
						return false;
					}

					public boolean isLinked(int options)
					{
						// TODO Auto-generated method stub
						return false;
					}

					public boolean isLocal(int depth)
					{
						// TODO Auto-generated method stub
						return false;
					}

					public boolean isPhantom()
					{
						// TODO Auto-generated method stub
						return false;
					}

					public boolean isReadOnly()
					{
						// TODO Auto-generated method stub
						return false;
					}

					public boolean isSynchronized(int depth)
					{
						// TODO Auto-generated method stub
						return false;
					}

					public boolean isTeamPrivateMember()
					{
						// TODO Auto-generated method stub
						return false;
					}

					public void move(IPath destination, boolean force, IProgressMonitor monitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void move(IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void move(IProjectDescription description, int updateFlags, IProgressMonitor monitor)
							throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void move(IProjectDescription description, boolean force, boolean keepHistory,
							IProgressMonitor monitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void refreshLocal(int depth, IProgressMonitor monitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void revertModificationStamp(long value) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void setDerived(boolean isDerived) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void setHidden(boolean isHidden) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void setLocal(boolean flag, int depth, IProgressMonitor monitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public long setLocalTimeStamp(long value) throws CoreException
					{
						// TODO Auto-generated method stub
						return 0;
					}

					public void setPersistentProperty(QualifiedName key, String value) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void setReadOnly(boolean readOnly)
					{
						// TODO Auto-generated method stub

					}

					public void setResourceAttributes(ResourceAttributes attributes) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void setSessionProperty(QualifiedName key, Object value) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void setTeamPrivateMember(boolean isTeamPrivate) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public void touch(IProgressMonitor monitor) throws CoreException
					{
						// TODO Auto-generated method stub

					}

					public Object getAdapter(Class adapter)
					{
						// TODO Auto-generated method stub
						return null;
					}

					public boolean contains(ISchedulingRule rule)
					{
						// TODO Auto-generated method stub
						return false;
					}

					public boolean isConflicting(ISchedulingRule rule)
					{
						// TODO Auto-generated method stub
						return false;
					}

				};
			}
		};

		provider.run(null, "apcloudify .");
	}

}

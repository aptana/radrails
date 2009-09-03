package org.radrails.rails.internal.ui.text;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.rubypeople.rdt.core.Flags;
import org.rubypeople.rdt.core.IMethod;
import org.rubypeople.rdt.core.IOpenable;
import org.rubypeople.rdt.core.IRubyElement;
import org.rubypeople.rdt.core.IRubyModel;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.IRubyScript;
import org.rubypeople.rdt.core.ISourceRange;
import org.rubypeople.rdt.core.IType;
import org.rubypeople.rdt.core.RubyModelException;

class PsuedoMethod implements IMethod {
	
	private String name;
	private String[] params;
	private int flags;

	public PsuedoMethod(String name, String[] params, int flags) {
		this.name = name;
		this.params = params;
		this.flags = flags;
	}

	public int getNumberOfParameters() throws RubyModelException {
		if (params == null)
			return 0;
		return params.length;
	}

	public String[] getParameterNames() throws RubyModelException {
		return params;
	}

	public int getVisibility() throws RubyModelException {
		if (isConstructor()) return IMethod.PUBLIC;
		if (Flags.isPrivate(flags)) return IMethod.PRIVATE;
		if (Flags.isProtected(flags)) return IMethod.PROTECTED;
		return IMethod.PUBLIC;
	}

	public boolean isConstructor() {
		return name.equals("initialize");
	}

	public boolean isPrivate() throws RubyModelException {
		return getVisibility() == IMethod.PRIVATE;
	}

	public boolean isProtected() throws RubyModelException {
		return getVisibility() == IMethod.PROTECTED;
	}

	public boolean isPublic() throws RubyModelException {
		return getVisibility() == IMethod.PUBLIC;
	}

	public boolean isSingleton() {
		return Flags.isStatic(flags);
	}

	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	public IRubyElement getAncestor(int ancestorType) {
		// TODO Auto-generated method stub
		return null;
	}

	public IResource getCorrespondingResource() throws RubyModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getElementName() {
		return name;
	}

	public int getElementType() {
		return IRubyElement.METHOD;
	}

	public String getHandleIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	public IOpenable getOpenable() {
		// TODO Auto-generated method stub
		return null;
	}

	public IRubyElement getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	public IPath getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public IRubyElement getPrimaryElement() {
		// TODO Auto-generated method stub
		return null;
	}

	public IResource getResource() {
		// TODO Auto-generated method stub
		return null;
	}

	public IRubyModel getRubyModel() {
		// TODO Auto-generated method stub
		return null;
	}

	public IRubyProject getRubyProject() {
		// TODO Auto-generated method stub
		return null;
	}

	public IResource getUnderlyingResource() throws RubyModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isStructureKnown() throws RubyModelException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isType(int type) {
		return type == IRubyElement.METHOD;
	}

	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	public IType getDeclaringType() {
		// TODO Auto-generated method stub
		return null;
	}

	public ISourceRange getNameRange() throws RubyModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public IRubyScript getRubyScript() {
		// TODO Auto-generated method stub
		return null;
	}

	public IType getType(String name, int occurrenceCount) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSource() throws RubyModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public ISourceRange getSourceRange() throws RubyModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public IRubyElement[] getChildren() throws RubyModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasChildren() throws RubyModelException {
		// TODO Auto-generated method stub
		return false;
	}

	public String[] getBlockParameters() throws RubyModelException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSimilar(IMethod method)
	{
		try
		{
			return getElementName().equals(method.getElementName()) && getNumberOfParameters() == method.getNumberOfParameters();
		}
		catch (RubyModelException e)
		{
			return false;
		}
	}
	
}

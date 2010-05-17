package org.radrails.rails.internal.ui.text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jruby.ast.ArrayNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.Node;
import org.jruby.ast.RootNode;
import org.rubypeople.rdt.core.Flags;
import org.rubypeople.rdt.core.IMethod;
import org.rubypeople.rdt.internal.core.parser.InOrderVisitor;
import org.rubypeople.rdt.internal.core.util.ASTUtil;

public class ActiveRecordAssociationsVisitor extends InOrderVisitor {
		
	private List<IMethod> methods = new ArrayList<IMethod>();
	
	/**
	 * Names of methods which mark associations
	 */
	private static Set<String> associationMethodNames = new HashSet<String>();
	private static Set<String> multiAssociationMethodNames = new HashSet<String>();
	private static Set<String> singularAssociationMethodNames = new HashSet<String>();
	static {
		singularAssociationMethodNames.add("belongs_to");
		singularAssociationMethodNames.add("has_one");
		multiAssociationMethodNames.add("has_many");
		multiAssociationMethodNames.add("has_and_belongs_to_many");
		associationMethodNames.addAll(singularAssociationMethodNames);
		associationMethodNames.addAll(multiAssociationMethodNames);
	}
    
	public List<IMethod> getMethods() {
		return methods;
	}
	
	@Override
	public Object visitRootNode(RootNode iVisited) {
		methods.clear();
		return super.visitRootNode(iVisited);
	}
	
	@Override
	public Object visitFCallNode(FCallNode iVisited) {
		if (associationMethodNames.contains(iVisited.getName())) {
			ArrayNode args = (ArrayNode) iVisited.getArgsNode();
			Node firstArg = args.get(0);
			String modelName = ASTUtil.stringRepresentation(firstArg);
			if (multiAssociationMethodNames.contains(iVisited.getName())) {
				methods.add(new PsuedoMethod(modelName, new String[0], Flags.AccPublic));
			} else {			
				// belongs_to and has_one
				methods.add(new PsuedoMethod(modelName, new String[0], Flags.AccPublic));
				methods.add(new PsuedoMethod(modelName + "=", new String[] {modelName}, Flags.AccPublic));
			}
		}
		return super.visitFCallNode(iVisited);
	}

}

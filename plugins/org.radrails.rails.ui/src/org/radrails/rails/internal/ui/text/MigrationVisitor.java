package org.radrails.rails.internal.ui.text;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jruby.ast.ArrayNode;
import org.jruby.ast.CallNode;
import org.jruby.ast.FCallNode;
import org.jruby.ast.Node;
import org.rubypeople.rdt.internal.core.parser.InOrderVisitor;
import org.rubypeople.rdt.internal.core.util.ASTUtil;

class MigrationVisitor extends InOrderVisitor {

	private Map<String, Set<String>> tableToFields = new HashMap<String, Set<String>>();
	private Set<String> fields = new HashSet<String>();
	private String tableName;
	
	/**
	 * Names of methods which mark defining a column in a migration
	 */
	private static Set<String> columnMethodNames = new HashSet<String>();
	static {
		columnMethodNames.add("column");
		columnMethodNames.add("integer");
		columnMethodNames.add("string");
		columnMethodNames.add("text");
		columnMethodNames.add("float");
		columnMethodNames.add("decimal");
		columnMethodNames.add("datetime");
		columnMethodNames.add("timestamp");
		columnMethodNames.add("time");
		columnMethodNames.add("date");
		columnMethodNames.add("binary");
		columnMethodNames.add("boolean");
	}
	
	public Set<String> getFieldNames(String string) {
		if (tableToFields.containsKey(string.toLowerCase()))
			return tableToFields.get(string.toLowerCase());	
		return new HashSet<String>();
	}

	@Override
	public Object visitFCallNode(FCallNode iVisited) {
		String name = iVisited.getName();
		if (name.equals("create_table")) {
			ArrayNode args = (ArrayNode) iVisited.getArgsNode();
			Node firstArg = args.get(0);
			tableName = ASTUtil.stringRepresentation(firstArg);
			
			Object ins = super.visitFCallNode(iVisited);
			
			// Add all the fields to table in tableToFields
			Set<String> temp = tableToFields.get(tableName);
			if (temp == null) temp = new HashSet<String>();
			temp.addAll(fields);
			tableToFields.put(tableName, temp);			
			fields = new HashSet<String>();
			tableName = null;
			return ins;
		} else if (name.equals("add_column")) {
			ArrayNode args = (ArrayNode) iVisited.getArgsNode();
			Node firstArg = args.get(0);
			String aTableName = ASTUtil.stringRepresentation(firstArg);
			Node secondArg = args.get(1);
			String field = ASTUtil.stringRepresentation(secondArg);
			
			Set<String> temp = tableToFields.get(aTableName);
			if (temp == null) temp = new HashSet<String>();
			temp.add(field);
			tableToFields.put(aTableName, temp);	
		}
		return super.visitFCallNode(iVisited);
	}
	
	@Override
	public Object visitCallNode(CallNode iVisited) {
		if (columnMethodNames.contains(iVisited.getName())) {
			ArrayNode args = (ArrayNode) iVisited.getArgsNode();
			Node firstArg = args.get(0);
			String fieldName = ASTUtil.stringRepresentation(firstArg);
			fields.add(fieldName);
		} else if (iVisited.getName().equals("timestamps")) {
			fields.add("created_at");
			fields.add("updated_at");
		} else if (iVisited.getName().equals("references")) {
			ArrayNode args = (ArrayNode) iVisited.getArgsNode();
			Node firstArg = args.get(0);
			String fieldName = ASTUtil.stringRepresentation(firstArg);
//			fields.add(fieldName);  // t.references :tag => tag field
			fields.add(fieldName + "_id"); // t.references :tag => 'tag_id' field
		}
		return super.visitCallNode(iVisited);
	}

	public Collection<? extends String> getTableNames() {
		return tableToFields.keySet();
	}
}

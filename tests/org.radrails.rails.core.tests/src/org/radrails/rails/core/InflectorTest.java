package org.radrails.rails.core;

import junit.framework.TestCase;

public class InflectorTest extends TestCase
{

	public void testSingularize()
	{
		assertEquals("boat", Inflector.singularize("boats"));
		assertEquals("alias", Inflector.singularize("aliases"));
		assertEquals("octopus", Inflector.singularize("octopi"));
		assertEquals("virus", Inflector.singularize("viri"));
		assertEquals("ox", Inflector.singularize("oxen"));
		assertEquals("quiz", Inflector.singularize("quizzes"));
		assertEquals("matrix", Inflector.singularize("matrices"));
		assertEquals("vertex", Inflector.singularize("vertices"));
		assertEquals("index", Inflector.singularize("indices"));
		assertEquals("shoe", Inflector.singularize("shoes"));
		assertEquals("status", Inflector.singularize("statuses"));
		assertEquals("crisis", Inflector.singularize("crises"));
		assertEquals("axis", Inflector.singularize("axes"));
		assertEquals("bus", Inflector.singularize("buses"));
		assertEquals("mouse", Inflector.singularize("mice"));
		assertEquals("calf", Inflector.singularize("calves"));
		assertEquals("datum", Inflector.singularize("data"));
		assertEquals("parenthesis", Inflector.singularize("parentheses"));
		assertEquals("hive", Inflector.singularize("hives"));
		assertEquals("objective", Inflector.singularize("objectives"));
		// uncountable
		assertEquals("rice", Inflector.singularize("rice"));
		assertEquals("equipment", Inflector.singularize("equipment"));
		assertEquals("information", Inflector.singularize("information"));
		assertEquals("money", Inflector.singularize("money"));
		assertEquals("species", Inflector.singularize("species"));
		assertEquals("series", Inflector.singularize("series"));
		assertEquals("fish", Inflector.singularize("fish"));
		assertEquals("sheep", Inflector.singularize("sheep"));
		//irregular
		assertEquals("person", Inflector.singularize("people"));
		assertEquals("man", Inflector.singularize("men"));
		assertEquals("child", Inflector.singularize("children"));
		assertEquals("sex", Inflector.singularize("sexes"));
		assertEquals("move", Inflector.singularize("moves"));
	}

	public void testPluralize()
	{
		assertEquals("boats", Inflector.pluralize("boat"));
		assertEquals("aliases", Inflector.pluralize("alias"));
		assertEquals("octopi", Inflector.pluralize("octopus"));
		assertEquals("viri", Inflector.pluralize("virus"));
		assertEquals("oxen", Inflector.pluralize("ox"));
		assertEquals("quizzes", Inflector.pluralize("quiz"));
		assertEquals("matrices", Inflector.pluralize("matrix"));
		assertEquals("vertices", Inflector.pluralize("vertex"));
		assertEquals("indices", Inflector.pluralize("index"));
		assertEquals("shoes", Inflector.pluralize("shoe"));
		assertEquals("statuses", Inflector.pluralize("status"));
		assertEquals("crises", Inflector.pluralize("crisis"));
		assertEquals("axes", Inflector.pluralize("axis"));
		assertEquals("buses", Inflector.pluralize("bus"));
		assertEquals("mice", Inflector.pluralize("mouse"));
		assertEquals("calves", Inflector.pluralize("calf"));
		assertEquals("data", Inflector.pluralize("datum"));
		assertEquals("parentheses", Inflector.pluralize("parenthesis"));
		assertEquals("hives", Inflector.pluralize("hive"));
		assertEquals("objectives", Inflector.pluralize("objective"));
		// uncountable
		assertEquals("rice", Inflector.pluralize("rice"));
		assertEquals("equipment", Inflector.pluralize("equipment"));
		assertEquals("information", Inflector.pluralize("information"));
		assertEquals("money", Inflector.pluralize("money"));
		assertEquals("species", Inflector.pluralize("species"));
		assertEquals("series", Inflector.pluralize("series"));
		assertEquals("fish", Inflector.pluralize("fish"));
		assertEquals("sheep", Inflector.pluralize("sheep"));
		//irregular
		assertEquals("people", Inflector.pluralize("person"));
		assertEquals("men", Inflector.pluralize("man"));
		assertEquals("children", Inflector.pluralize("child"));
		assertEquals("sexes", Inflector.pluralize("sex"));
		assertEquals("moves", Inflector.pluralize("move"));
	}
}

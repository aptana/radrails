package org.radrails.rails.internal.ui.generators;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.rubypeople.eclipse.shams.resources.ShamProject;

public class GeneratorsConsoleLineTest extends TestCase {

	private IProject project = new ShamProject("testing");
	
	public void testAppModels() {
		assertFalse(GeneratorsConsoleLine.isMatching("      exists  app/models/"));
	}

	public void testAppControllers() {
		assertFalse(GeneratorsConsoleLine.isMatching("      exists  app/controllers/"));
	}

	public void testAppHelpers() {
		assertFalse(GeneratorsConsoleLine.isMatching("      exists  app/helpers/"));
	}

	public void testAppViews() {
		assertFalse(GeneratorsConsoleLine.isMatching("      create  app/views/addresses"));
	}

	public void testTestFunctional() {
		assertFalse(GeneratorsConsoleLine.isMatching("      exists  test/functional/"));
	}

	public void testTestUnit() {
		assertFalse(GeneratorsConsoleLine.isMatching("      exists  test/unit/"));
	}

	public void testIndexView() {
		assertCreateMatch("app/views/addresses/index.rhtml");
	}
	
	private void assertCreateMatch(String filename) {
		assertMatch("      create  ", filename);
	}
	
	private void assertIdenticalMatch(String filename) {
		assertMatch("   identical  ", filename);		
	}

	private void assertMatch(String existence, String filename) {
		String line = existence + filename;
		assertTrue(GeneratorsConsoleLine.isMatching(line));
		GeneratorsConsoleLine matcher = new GeneratorsConsoleLine(line, project);
		assertEquals("/" + project.getName() + "/" + filename, matcher.getFilename());		
	}
	
	public void testShowView() {		
		assertCreateMatch("app/views/addresses/show.rhtml");
	}

	public void testNewView() {
		assertCreateMatch("app/views/addresses/new.rhtml");
	}

	public void testEditView() {
		assertCreateMatch("app/views/addresses/edit.rhtml");
	}

	public void testLayout() {
		assertCreateMatch("app/views/layouts/addresses.rhtml");
	}

	public void testScaffoldCSS() {
		assertIdenticalMatch("public/stylesheets/scaffold.css");
	}

	public void testModel() {
		assertCreateMatch("app/models/address.rb");
	}

	public void testController() {
		assertCreateMatch("app/controllers/addresses_controller.rb");
	}

	public void testControllerTest() {
		assertCreateMatch("test/functional/addresses_controller_test.rb");
	}

	public void testHelper() {
		assertCreateMatch("app/helpers/addresses_helper.rb");
	}

	public void testModelTest() {
		assertCreateMatch("test/unit/address_test.rb");
	}

	public void testFixture() {
		assertCreateMatch("test/fixtures/addresses.yml");
	}

	public void testDBMigrate() {
		assertFalse(GeneratorsConsoleLine.isMatching("      exists  db/migrate"));
	}

	public void testMigration() {
		assertCreateMatch("db/migrate/003_create_addresses.rb");
	}

	public void testRoute() {
		assertFalse(GeneratorsConsoleLine
				.isMatching("       route  map.resources :addresses"));
	}
	
	public void testBeginsWithRailsRoot() {
		String line = "    #{RAILS_ROOT}/app/models/tree_diff_element.rb:137:in `find_duration_diff_counts'";
		assertTrue(GeneratorsConsoleLine.isMatching(line));
		GeneratorsConsoleLine matcher = new GeneratorsConsoleLine(line, project);
		assertEquals("/testing/app/models/tree_diff_element.rb", matcher.getFilename());	
		assertEquals(137, matcher.getLineNumber());
		assertEquals(49, matcher.getLength());
	}
	
	public void testHAMLStack()
	{
		String line = "   app/views/subscriptions/new.haml:20:in `blah`";
		assertTrue(GeneratorsConsoleLine.isMatching(line));
		GeneratorsConsoleLine matcher = new GeneratorsConsoleLine(line, project);
		assertEquals("/testing/app/views/subscriptions/new.haml", matcher.getFilename());	
		assertEquals(20, matcher.getLineNumber());
		assertEquals(35, matcher.getLength());
	}

	public void testViewFilenameref()
	{
		String line = "on line #1 of app/views/shared/_hosting_plan.haml:";
		assertTrue(GeneratorsConsoleLine.isMatching(line));
		GeneratorsConsoleLine matcher = new GeneratorsConsoleLine(line, project);
		assertEquals("/testing/app/views/shared/_hosting_plan.haml", matcher.getFilename());	
		assertEquals(0, matcher.getLineNumber());
		assertEquals(35, matcher.getLength());
	}
}

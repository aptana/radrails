package org.radrails.rails.internal.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.radrails.rails.internal.core.RailsPlugin;
import org.rubypeople.rdt.core.IRubyProject;
import org.rubypeople.rdt.core.tests.ModifyingResourceTest;

public class RailsLightweightDecoratorTest extends ModifyingResourceTest
{

	public RailsLightweightDecoratorTest(String name)
	{
		super(name);
	}

	public void testDecorateNullDecoration() throws Exception
	{
		RailsLightweightDecorator decorator = new RailsLightweightDecorator();
		Object element = "";
		decorator.decorate(element, null);
	}

	public void testDecorateNullElement() throws Exception
	{
		RailsLightweightDecorator decorator = new RailsLightweightDecorator();
		TestDecoration decoration = new TestDecoration();
		decorator.decorate(null, decoration);
		assertTrue(decoration.getOverlays().isEmpty());
	}

	public void testDecorateRailsProject() throws Exception
	{
		RailsLightweightDecorator decorator = new RailsLightweightDecorator();
		TestDecoration decoration = new TestDecoration();
		IRubyProject project = createRubyProject("lightweight_decorator");
		final boolean[] finished = new boolean[1];
		RailsPlugin.addRailsNature(project.getProject(), new IProgressMonitor()
		{

			private boolean isCanceled;

			public void beginTask(String name, int totalWork)
			{
			}

			public void done()
			{
				finished[0] = true;
			}

			public void internalWorked(double work)
			{
			}

			public boolean isCanceled()
			{
				return isCanceled;
			}

			public void setCanceled(boolean value)
			{
				isCanceled = value;
			}

			public void setTaskName(String name)
			{
			}

			public void subTask(String name)
			{
			}

			public void worked(int work)
			{
			}

		});
		long start = System.currentTimeMillis();
		while (!finished[0])
		{
			Thread.yield();
			if (System.currentTimeMillis() > start + 10000)
				break;
		}
		decorator.decorate(project.getProject(), decoration);
		assertEquals(1, decoration.getOverlays().size());
	}

}

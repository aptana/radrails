package net.lucky_dip.sasseditor.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.lucky_dip.hamleditor.editor.HamlReconcilingStrategy;
import net.lucky_dip.hamleditor.editor.IColorManager;
import net.lucky_dip.hamleditor.editor.scanners.DefaultScanner;
import net.lucky_dip.sasseditor.editor.contentassist.SassContentAssistantProcessor;
import net.lucky_dip.sasseditor.editor.scanners.SassAttributeScanner;
import net.lucky_dip.sasseditor.editor.scanners.SassClassScanner;
import net.lucky_dip.sasseditor.editor.scanners.SassConstantScanner;
import net.lucky_dip.sasseditor.editor.scanners.SassIDScanner;
import net.lucky_dip.sasseditor.editor.scanners.SassPartitionScanner;
import net.lucky_dip.sasseditor.editor.scanners.SassTagScanner;

import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class SassConfiguration extends SourceViewerConfiguration
{
	private SassIndentLineAutoEditStrategy autoIndent;

	private IColorManager colorManager;
	private SassEditor editor;

	private SassClassScanner classScanner;
	private SassIDScanner idScanner;
	private SassAttributeScanner attributeScanner;
	private SassConstantScanner constantScanner;
	private SassTagScanner tagScanner;

	public SassConfiguration(IColorManager colorManager, SassEditor editor)
	{
		this.colorManager = colorManager;
		this.editor = editor;
	}

	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer)
	{
		List<String> types = new ArrayList<String>();
		types.add(IDocument.DEFAULT_CONTENT_TYPE);
		types.addAll(Arrays.asList(SassPartitionScanner.SASS_PARTITION_TYPES));
		return (String[]) types.toArray(new String[0]);
	}

	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer)
	{
		return SassEditor.SASS_PARTITIONING;
	}

	public IReconciler getReconciler(ISourceViewer sourceViewer)
	{
		HamlReconcilingStrategy strategy = new HamlReconcilingStrategy(editor);
		MonoReconciler reconciler = new MonoReconciler(strategy, false);
		return reconciler;
	}

	private ITokenScanner getClassScanner()
	{
		if (classScanner == null)
		{
			classScanner = new SassClassScanner(colorManager);
		}
		return classScanner;
	}

	private ITokenScanner getIDScanner()
	{
		if (idScanner == null)
		{
			idScanner = new SassIDScanner(colorManager);
		}
		return idScanner;
	}

	private ITokenScanner getAttributeScanner()
	{
		if (attributeScanner == null)
		{
			attributeScanner = new SassAttributeScanner(colorManager);
		}
		return attributeScanner;
	}

	private ITokenScanner getConstantScanner()
	{
		if (constantScanner == null)
		{
			constantScanner = new SassConstantScanner(colorManager);
		}
		return constantScanner;
	}

	private ITokenScanner getTagScanner()
	{
		if (tagScanner == null)
		{
			tagScanner = new SassTagScanner(colorManager);
		}
		return tagScanner;
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
	{
		PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(new DefaultScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(getIDScanner());
		reconciler.setDamager(dr, SassPartitionScanner.SASS_ID);
		reconciler.setRepairer(dr, SassPartitionScanner.SASS_ID);

		dr = new DefaultDamagerRepairer(getAttributeScanner());
		reconciler.setDamager(dr, SassPartitionScanner.SASS_ATTRIBUTE);
		reconciler.setRepairer(dr, SassPartitionScanner.SASS_ATTRIBUTE);

		dr = new DefaultDamagerRepairer(getConstantScanner());
		reconciler.setDamager(dr, SassPartitionScanner.SASS_CONSTANT);
		reconciler.setRepairer(dr, SassPartitionScanner.SASS_CONSTANT);

		dr = new DefaultDamagerRepairer(getClassScanner());
		reconciler.setDamager(dr, SassPartitionScanner.SASS_CLASS);
		reconciler.setRepairer(dr, SassPartitionScanner.SASS_CLASS);

		dr = new DefaultDamagerRepairer(getTagScanner());
		reconciler.setDamager(dr, SassPartitionScanner.SASS_TAG);
		reconciler.setRepairer(dr, SassPartitionScanner.SASS_TAG);

		return reconciler;
	}

	public int getTabWidth(ISourceViewer sourceViewer)
	{
		return SassIndentLineAutoEditStrategy.INDENT_STRING.length();
	}

	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType)
	{
		return new String[] { SassIndentLineAutoEditStrategy.INDENT_STRING };
	}

	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType)
	{
		if (autoIndent == null)
		{
			autoIndent = new SassIndentLineAutoEditStrategy();
		}
		return new IAutoEditStrategy[] { autoIndent };
	}

	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer)
	{
		ContentAssistant ca = new ContentAssistant();
		ca.setDocumentPartitioning(SassEditor.SASS_PARTITIONING);
		IContentAssistProcessor pr = new SassContentAssistantProcessor();

		ca.setContentAssistProcessor(pr, SassPartitionScanner.SASS_ATTRIBUTE);
		ca.setContentAssistProcessor(pr, SassPartitionScanner.SASS_CONSTANT);
		ca.setContentAssistProcessor(pr, IDocument.DEFAULT_CONTENT_TYPE);

		ca.enableAutoActivation(true);
		ca.setAutoActivationDelay(500);
		ca.setProposalPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
		ca.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
		ca.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		return ca;
	}
}
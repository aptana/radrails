package net.lucky_dip.hamleditor.editor;

import java.util.ArrayList;
import java.util.Arrays;

import net.lucky_dip.hamleditor.editor.contentassist.HamlContentAssistantProcessor;
import net.lucky_dip.hamleditor.editor.scanners.DefaultScanner;
import net.lucky_dip.hamleditor.editor.scanners.HamlClassScanner;
import net.lucky_dip.hamleditor.editor.scanners.HamlCommentScanner;
import net.lucky_dip.hamleditor.editor.scanners.HamlDoctypeScanner;
import net.lucky_dip.hamleditor.editor.scanners.HamlElementScanner;
import net.lucky_dip.hamleditor.editor.scanners.HamlIDScanner;
import net.lucky_dip.hamleditor.editor.scanners.HamlPartitionScanner;
import net.lucky_dip.hamleditor.editor.scanners.RubyExpressionScanner;

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
import org.eclipse.ui.editors.text.TextEditor;
import org.rubypeople.rdt.internal.ui.text.ruby.AbstractRubyTokenScanner;

public class HamlConfiguration extends SourceViewerConfiguration {
	private HamlIndentLineAutoEditStrategy autoIndent;

	private HamlElementScanner elementScanner;

	private ColorManager colorManager;

	private HamlClassScanner classScanner;

	private HamlIDScanner idScanner;

	private HamlCommentScanner commentScanner;

	private HamlDoctypeScanner doctypeScanner;

	private AbstractRubyTokenScanner rubyScanner;

	private TextEditor editor;

	public HamlConfiguration(ColorManager colorManager, TextEditor editor) {
		this.colorManager = colorManager;
		this.editor = editor;
	}

	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		ArrayList types = new ArrayList();
		types.add(IDocument.DEFAULT_CONTENT_TYPE);
		types.addAll(Arrays.asList(HamlPartitionScanner.HAML_PARTITION_TYPES));

		return (String[]) types.toArray(new String[0]);
	}

	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		return HamlEditor.HAML_PARTITIONING;
	}

	protected HamlElementScanner getElementScanner() {
		if (elementScanner == null) {
			elementScanner = new HamlElementScanner(colorManager);
		}
		return elementScanner;
	}

	private HamlClassScanner getClassScanner() {
		if (classScanner == null) {
			classScanner = new HamlClassScanner(colorManager);
		}
		return classScanner;
	}

	private ITokenScanner getIDScanner() {
		if (idScanner == null) {
			idScanner = new HamlIDScanner(colorManager);
		}
		return idScanner;
	}

	private ITokenScanner getCommentScanner() {
		if (commentScanner == null) {
			commentScanner = new HamlCommentScanner(colorManager);
		}
		return commentScanner;
	}

	private ITokenScanner getDoctypeScanner() {
		if (doctypeScanner == null) {
			doctypeScanner = new HamlDoctypeScanner(colorManager);
		}
		return doctypeScanner;
	}

	private ITokenScanner getRubyScanner() {
		if (rubyScanner == null) {
			rubyScanner = new RubyExpressionScanner();
		}
		return rubyScanner;
	}
	
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		HamlReconcilingStrategy strategy = new HamlReconcilingStrategy((HamlEditor) editor);
		
		MonoReconciler reconciler = new MonoReconciler(strategy, false);
		
		return reconciler;
	} 

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(new DefaultScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(getElementScanner());
		reconciler.setDamager(dr, HamlPartitionScanner.HAML_ELEMENT);
		reconciler.setRepairer(dr, HamlPartitionScanner.HAML_ELEMENT);

		dr = new DefaultDamagerRepairer(getDoctypeScanner());
		reconciler.setDamager(dr, HamlPartitionScanner.HAML_DOCTYPE);
		reconciler.setRepairer(dr, HamlPartitionScanner.HAML_DOCTYPE);

		dr = new DefaultDamagerRepairer(getCommentScanner());
		reconciler.setDamager(dr, HamlPartitionScanner.HAML_COMMENT);
		reconciler.setRepairer(dr, HamlPartitionScanner.HAML_COMMENT);

		dr = new DefaultDamagerRepairer(getIDScanner());
		reconciler.setDamager(dr, HamlPartitionScanner.HAML_ID);
		reconciler.setRepairer(dr, HamlPartitionScanner.HAML_ID);

		dr = new DefaultDamagerRepairer(getClassScanner());
		reconciler.setDamager(dr, HamlPartitionScanner.HAML_CLASS);
		reconciler.setRepairer(dr, HamlPartitionScanner.HAML_CLASS);

		dr = new DefaultDamagerRepairer(getRubyScanner());
		reconciler.setDamager(dr, HamlPartitionScanner.HAML_RUBY);
		reconciler.setRepairer(dr, HamlPartitionScanner.HAML_RUBY);

		return reconciler;
	}

	public int getTabWidth(ISourceViewer sourceViewer) {
		return HamlIndentLineAutoEditStrategy.INDENT_STRING.length();
	}

	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[] { HamlIndentLineAutoEditStrategy.INDENT_STRING };
	}

	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		if (autoIndent == null) {
			autoIndent = new HamlIndentLineAutoEditStrategy();
		}
		return new IAutoEditStrategy[] { autoIndent };
	}

	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant ca = new ContentAssistant();
		ca.setDocumentPartitioning(HamlEditor.HAML_PARTITIONING);
		IContentAssistProcessor pr = new HamlContentAssistantProcessor();

		ca.setContentAssistProcessor(pr, HamlPartitionScanner.HAML_CLASS);
		ca.setContentAssistProcessor(pr, HamlPartitionScanner.HAML_ELEMENT);
		ca.setContentAssistProcessor(pr, IDocument.DEFAULT_CONTENT_TYPE);

		ca.enableAutoActivation(true);
		ca.setAutoActivationDelay(500);
		ca.setProposalPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
		ca.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
		ca.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		return ca;
	}
}
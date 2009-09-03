package net.lucky_dip.sasseditor.editor.contentassist;

import java.util.ArrayList;
import java.util.Collections;

import net.lucky_dip.hamleditor.HTMLCSSKeywords;
import net.lucky_dip.sasseditor.editor.SassEditor;
import net.lucky_dip.sasseditor.editor.scanners.SassPartitionScanner;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class SassContentAssistantProcessor implements IContentAssistProcessor {

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		IDocument doc = viewer.getDocument();
		IDocumentExtension3 doc3 = (IDocumentExtension3) viewer.getDocument();
		ArrayList res = new ArrayList();

		try {
			ITypedRegion region = doc3.getPartition(SassEditor.SASS_PARTITIONING,
					offset > 0 ? offset - 1 : 0, false);
			// + 1 to strip the leading %
			int regionOffset = region.getOffset() + 1;
			String start = viewer.getDocument().get(regionOffset, offset - regionOffset);
			start = start.toLowerCase();

			if (region.getType().equals(SassPartitionScanner.SASS_ATTRIBUTE)) {
				res.addAll(HTMLCSSKeywords.getCssAttributeMatches(start, regionOffset, region));
			}
			else if (region.getType().equals(SassPartitionScanner.SASS_CONSTANT)) {
				ITypedRegion[] regions = doc3.computePartitioning(SassEditor.SASS_PARTITIONING, 0,
						doc.getLength(), false);
				String[] classNames = getConstants(doc, regions);
				for (int i = 0; i < classNames.length; i++) {
					String cname = classNames[i];

					CompletionProposal cp = new CompletionProposal(cname, regionOffset, region
							.getLength() - 1, cname.length());
					res.add(cp);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return (ICompletionProposal[]) res.toArray(new ICompletionProposal[res.size()]);
	}

	private String[] getConstants(IDocument document, ITypedRegion[] regions) {
		ArrayList res = new ArrayList();
		for (int i = 0; i < regions.length; i++) {
			if (regions[i].getType().equals(SassPartitionScanner.SASS_CONSTANT)) {
				try {
					int offset = regions[i].getOffset() + 1;
					int length = regions[i].getLength() - 1;
					if (length > 0) {
						String constant = document.get(offset, length);
						if (!res.contains(constant)) {
							res.add(constant);
						}
					}
				}
				catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		}

		Collections.sort(res);
		return (String[]) res.toArray(new String[res.size()]);
	}

	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return null;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	public String getErrorMessage() {
		return null;
	}

}

package net.lucky_dip.hamleditor.editor.contentassist;

import java.util.ArrayList;
import java.util.Collections;

import net.lucky_dip.hamleditor.HTMLCSSKeywords;
import net.lucky_dip.hamleditor.editor.HamlEditor;
import net.lucky_dip.hamleditor.editor.scanners.HamlPartitionScanner;

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

public class HamlContentAssistantProcessor implements IContentAssistProcessor {

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		IDocument doc = viewer.getDocument();
		IDocumentExtension3 doc3 = (IDocumentExtension3) viewer.getDocument();
		ArrayList res = new ArrayList();

		try {
			ITypedRegion region = doc3.getPartition(HamlEditor.HAML_PARTITIONING,
					offset > 0 ? offset - 1 : 0, false);
			// + 1 to strip the leading %
			int regionOffset = region.getOffset() + 1;
			String start = viewer.getDocument().get(regionOffset, offset - regionOffset);
			start = start.toLowerCase();

			if (region.getType().equals(HamlPartitionScanner.HAML_ELEMENT)) {
				res.addAll(HTMLCSSKeywords.getHtmlTagMatches(start, regionOffset, region));
			}
			else if (region.getType().equals(HamlPartitionScanner.HAML_CLASS)) {
				ITypedRegion[] regions = doc3.computePartitioning(HamlEditor.HAML_PARTITIONING, 0,
						doc.getLength(), false);
				String[] classNames = getClassNames(doc, regions);
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

	private String[] getClassNames(IDocument document, ITypedRegion[] regions) {
		ArrayList res = new ArrayList();
		for (int i = 0; i < regions.length; i++) {
			if (regions[i].getType().equals(HamlPartitionScanner.HAML_CLASS)) {
				try {
					int offset = regions[i].getOffset() + 1;
					int length = regions[i].getLength() - 1;
					if (length > 0) {
						res.add(document.get(offset, length));
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

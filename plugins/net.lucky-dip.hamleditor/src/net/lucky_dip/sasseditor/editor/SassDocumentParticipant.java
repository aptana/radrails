package net.lucky_dip.sasseditor.editor;

import net.lucky_dip.sasseditor.editor.scanners.SassPartitionScanner;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

public class SassDocumentParticipant implements IDocumentSetupParticipant {
	public void setup(IDocument document) {
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension3 = (IDocumentExtension3) document;
			IDocumentPartitioner partitioner = new FastPartitioner(
					SassPartitionScanner.getInstance(),
					SassPartitionScanner.SASS_PARTITION_TYPES);
			extension3.setDocumentPartitioner(SassEditor.SASS_PARTITIONING,
					partitioner);
			partitioner.connect(document);
		}
	}
}
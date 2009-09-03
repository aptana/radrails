package net.lucky_dip.hamleditor.editor;

import net.lucky_dip.hamleditor.editor.scanners.HamlPartitionScanner;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

public class HamlDocumentParticipant implements IDocumentSetupParticipant {
	public void setup(IDocument document) {
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension3 = (IDocumentExtension3) document;
			IDocumentPartitioner partitioner = new FastPartitioner(
					HamlPartitionScanner.getInstance(),
					HamlPartitionScanner.HAML_PARTITION_TYPES);
			extension3.setDocumentPartitioner(HamlEditor.HAML_PARTITIONING,
					partitioner);
			partitioner.connect(document);
		}
	}
}
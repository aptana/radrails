package net.lucky_dip.sasseditor.editor;

import net.lucky_dip.sasseditor.editor.scanners.SassPartitionScanner;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class SassDocumentProvider extends FileDocumentProvider {

	protected IDocument createDocument(Object element) throws CoreException
	{
	    IDocument document = super.createDocument(element);
	    if (document != null)
	    {
	        IDocumentPartitioner partitioner = new FastPartitioner(SassPartitionScanner.getInstance(), SassPartitionScanner.SASS_PARTITION_TYPES);
	        partitioner.connect(document);
	        document.setDocumentPartitioner(partitioner);
	    }
	    return document;
	}
}

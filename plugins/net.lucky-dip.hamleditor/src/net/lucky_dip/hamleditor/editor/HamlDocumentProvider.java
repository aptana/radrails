package net.lucky_dip.hamleditor.editor;

import net.lucky_dip.hamleditor.editor.scanners.HamlPartitionScanner;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

public class HamlDocumentProvider extends FileDocumentProvider {

	protected IDocument createDocument(Object element) throws CoreException
	{
	    IDocument document = super.createDocument(element);
	    if (document != null)
	    {
	        IDocumentPartitioner partitioner = new FastPartitioner(HamlPartitionScanner.getInstance(), HamlPartitionScanner.HAML_PARTITION_TYPES);
	        partitioner.connect(document);
	        document.setDocumentPartitioner(partitioner);
	    }
	    return document;
	}
}

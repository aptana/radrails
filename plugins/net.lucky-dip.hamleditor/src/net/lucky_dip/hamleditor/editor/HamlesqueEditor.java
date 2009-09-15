package net.lucky_dip.hamleditor.editor;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

public abstract class HamlesqueEditor extends TextEditor {

	protected IColorManager colorManager;
	protected ProjectionAnnotationModel annotationModel;
	protected ProjectionSupport projectionSupport;
	protected Annotation[] oldAnnotations;

	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		ProjectionViewer pv = (ProjectionViewer) getSourceViewer();

		projectionSupport = new ProjectionSupport(pv, getAnnotationAccess(),
				getSharedColors());
		projectionSupport.install();

		pv.doOperation(ProjectionViewer.TOGGLE);

		annotationModel = pv.getProjectionAnnotationModel();
	}

	protected ISourceViewer createSourceViewer(Composite parent,
			IVerticalRuler ruler, int styles) {
		ISourceViewer res = new ProjectionViewer(parent, ruler,
				getOverviewRuler(), isOverviewRulerVisible(), styles);

		SourceViewerDecorationSupport svds = getSourceViewerDecorationSupport(res);
		svds.setCharacterPairMatcher(new HamlCharacterPairMatcher());
		svds.setMatchingCharacterPainterPreferenceKeys(
				"rails.ui.editor.haml.char_matching",
				"rails.ui.editor.haml.char_matching.background");

		return res;
	}

	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}

	/**
	 * Updates the folding markers to display on the editor pane.
	 */
	public void updateFoldingStructure(ArrayList positions) {
		Annotation[] annotations = new Annotation[positions.size()];

		// this will hold the new annotations along
		// with their corresponding positions
		HashMap newAnnotations = new HashMap();

		for (int i = 0; i < positions.size(); i++) {
			ProjectionAnnotation annotation = new ProjectionAnnotation();

			newAnnotations.put(annotation, positions.get(i));

			annotations[i] = annotation;
		}

		annotationModel.modifyAnnotations(oldAnnotations, newAnnotations, null);

		oldAnnotations = annotations;
	}
}

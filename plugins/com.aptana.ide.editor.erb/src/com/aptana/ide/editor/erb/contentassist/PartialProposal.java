package com.aptana.ide.editor.erb.contentassist;

import java.io.File;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import org.rubypeople.rdt.core.util.Util;

import com.aptana.ide.core.IdeLog;
import com.aptana.ide.editor.erb.ERBPlugin;
import com.aptana.ide.editors.unified.IUnifiedViewer;

class PartialProposal extends ERBCompletionProposal {
	
	private File partial;
	private String fAdditional;
	
	PartialProposal(File partial, String replacement, int offset, String displayString, IUnifiedViewer unifiedViewer, String filename) {
		super(replacement, offset, 0, replacement.length(), ERBPlugin.getImage("icons/page_code.png"), displayString, null, null, -1, unifiedViewer, null, filename);
		this.partial = partial;
	}
	
	@Override
	public String getAdditionalProposalInfo() {
		if (fAdditional == null) {
			fAdditional = "";			
			try {
				fAdditional = new String(Util.getFileCharContent(partial, null));
				fAdditional = escapeHTML(fAdditional);
			} catch (IOException e) {
				IdeLog.logError(ERBPlugin.getDefault(), "Problem reading partial's contents", e);
			}
		}
		return fAdditional;
	}
	
	public static String escapeHTML(String aText){
	     final StringBuilder result = new StringBuilder();
	     final StringCharacterIterator iterator = new StringCharacterIterator(aText);
	     char character =  iterator.current();
	     while (character != CharacterIterator.DONE ){
	       if (character == '<') {
	         result.append("&lt;");
	       }
	       else if (character == '>') {
	         result.append("&gt;");
	       }
	       else if (character == '&') {
	         result.append("&amp;");
	      }
	       else if (character == '\"') {
	         result.append("&quot;");
	       }
	       else if (character == '\'') {
	         result.append("&#039;");
	       }
	       else if (character == '(') {
	         result.append("&#040;");
	       }
	       else if (character == ')') {
	         result.append("&#041;");
	       }
	       else if (character == '#') {
	         result.append("&#035;");
	       }
	       else if (character == '%') {
	         result.append("&#037;");
	       }
	       else if (character == ';') {
	         result.append("&#059;");
	       }
	       else if (character == '+') {
	         result.append("&#043;");
	       }
	       else if (character == '-') {
	         result.append("&#045;");
	       }
	       else {
	         //the char is not a special one
	         //add it to the result as is
	         result.append(character);
	       }
	       character = iterator.next();
	     }
	     return result.toString();
	  }

}

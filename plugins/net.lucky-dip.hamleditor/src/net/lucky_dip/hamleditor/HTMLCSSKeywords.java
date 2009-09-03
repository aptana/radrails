package net.lucky_dip.hamleditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;

/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.radrails.org/legal/cpl-v10.html
 *******************************************************************************/

/**
 * Constants for all of the HTML and CSS keywords
 * 
 * @author mbaumbach
 * 
 * @version 0.4.1
 */
public class HTMLCSSKeywords {

	public static final String[] HTML_TAGS = { "a", "abbr", "acronym", "address", "applet", "area",
			"b", "base", "basefont", "bdo", "big", "blockquote", "body", "br", "button", "caption",
			"center", "cite", "code", "col", "colgroup", "dd", "del", "dir", "div", "dfn", "dl",
			"dt", "em", "fieldset", "font", "form", "frame", "frameset", "h1", "head", "hr",
			"html", "i", "iframe", "img", "input", "ins", "isindex", "kbd", "label", "legend",
			"li", "link", "map", "menu", "meta", "noframes", "noscript", "object", "ol",
			"optgroup", "option", "p", "param", "pre", "q", "s", "samp", "script", "select",
			"small", "span", "strike", "strong", "style", "sub", "sup", "table", "tbody", "td",
			"textarea", "tfoot", "th", "thead", "title", "tr", "tt", "u", "ul", "var", "xmp" };

	public static final String[] CSS_PROPERTIES = { "background", "background-attachment",
			"background-color", "background-image", "background-position", "background-repeat",
			"border", "border-bottom", "border-bottom-color", "border-bottom-style",
			"border-bottom-width", "border-color", "border-left", "border-left-color",
			"border-left-style", "border-left-width", "border-right", "border-right-color",
			"border-right-style", "border-right-width", "border-style", "border-top",
			"border-top-color", "border-top-style", "border-top-width", "border-width", "clear",
			"cursor", "display", "float", "position", "visibility", "height", "line-height",
			"max-height", "max-width", "min-height", "min-width", "width", "font", "font-family",
			"font-size", "font-size-adjust", "font-stretch", "font-style", "font-variant",
			"font-weight", "content", "counter-increment", "counter-reset", "quotes", "list-style",
			"list-style-image", "list-style-position", "list-style-type", "margin",
			"margin-bottom", "margin-left", "margin-right", "margin-top", "outline",
			"outline-color", "outline-style", "outline-width", "padding", "padding-bottom",
			"padding-left", "padding-right", "padding-top", "bottom", "clip", "left", "overflow",
			"position", "right", "top", "vertical-align", "z-index", "border-collapse",
			"border-spacing", "caption-side", "empty-cells", "table-layout", "color", "direction",
			"letter-spacing", "text-align", "text-decoration", "text-indent", "text-shadow",
			"text-transform", "unicode-bidi", "white-space", "word-spacing" };

	public static final String[] CSS_VALUES = { "separate", "show", "hide", "ltr", "rtl",
			"justify", "underline", "overline", "line-through", "blink", "capitalize", "uppercase",
			"lowercase", "embed", "bidi-override", "pre", "nowrap", "scroll", "baseline", "sub",
			"super", "text-top", "middle", "text-bottom", "invert", "inside", "outside", "disc",
			"circle", "square", "decimal", "decimal-leading-zero", "lower-roman", "upper-roman",
			"lower-alpha", "upper-alpha", "lower-greek", "upper-greek", "lower-latin",
			"upper-latin", "hebrew", "armenian", "georgian", "cjk-ideographic", "hirogana",
			"katakana", "hirogana-iroha", "katakana-iroha", "counter", "counters", "attr",
			"open-quote", "close-quote", "no-open-quote", "no-close-quote", "caption", "icon",
			"menu", "message-box", "small-caption", "status-bar", "xx-small", "x-small", "small",
			"rgb", "aliceblue", "antiquewhite", "aqua", "aquamarine", "azure", "beige", "bisque",
			"black", "blanchedalmond", "blue", "blueviolet", "brown", "burlywood", "cadetblue",
			"chartreuse", "chocolate", "coral", "cornflowerblue", "cornsilk", "crimson", "cyan",
			"darkblue", "darkcyan", "darkgoldenrod", "darkgray", "darkgreen", "darkkhaki",
			"darkmagenta", "darkolivegreen", "darkorange", "darkorchid", "darkred", "darksalmon",
			"darkseagreen", "darkslateblue", "darkslategray", "darkturquoise", "darkviolet",
			"deeppink", "deepskyblue", "dimgray", "dodgerblue", "feldspar", "firebrick",
			"floralwhite", "forestgreen", "fuchsia", "gainsboro", "ghostwhite", "gold",
			"goldenrod", "gray", "green", "greenyellow", "honeydew", "hotpink", "indianred",
			"indigo", "ivory", "khaki", "lavender", "lavenderblush", "lawngreen", "lemonchiffon",
			"lightblue", "lightcoral", "lightcyan", "lightgoldenrodyellow", "lightgrey",
			"lightgreen", "lightpink", "lightsalmon", "lightseagreen", "lightskyblue",
			"lightslateblue", "lightslategray", "lightsteelblue", "lightyellow", "lime",
			"limegreen", "linen", "magenta", "maroon", "mediumaquamarine", "mediumblue",
			"mediumorchid", "mediumpurple", "mediumseagreen", "mediumslateblue",
			"mediumspringgreen", "mediumturquoise", "mediumvioletred", "midnightblue", "mintcream",
			"mistyrose", "moccasin", "navajowhite", "navy", "oldlace", "olive", "olivedrab",
			"orange", "orangered", "orchid", "palegoldenrod", "palegreen", "paleturquoise",
			"palevioletred", "papayawhip", "peachpuff", "peru", "pink", "plum", "powderblue",
			"purple", "red", "rosybrown", "royalblue", "saddlebrown", "salmon", "sandybrown",
			"seagreen", "seashell", "sienna", "silver", "skyblue", "slateblue", "slategray",
			"snow", "springgreen", "steelblue", "tan", "teal", "thistle", "tomato", "turquoise",
			"violet", "violetred", "wheat", "white", "whitesmoke", "yellow", "yellowgreen" };

	public static final String[] CSS_UNITS = { "%", "in", "cm", "mm", "em", "ex", "pt", "pc", "px" };

	public static final String[] CSS_PSEUDO_CLASSES = { "active", "hover", "link", "visited",
			"first-child", "lang", "first-letter", "first-line", "before", "after" };

	public static final String[][] HTML_KEYWORDS = { HTML_TAGS, CSS_PSEUDO_CLASSES };

	public static final String[][] CSS_KEYWORDS = { CSS_PROPERTIES, CSS_VALUES };

	public static Collection getHtmlTagMatches(String start, int offset, ITypedRegion region) {
		List matches = getMatches(HTML_TAGS, start);
		return getSortedCompletionProposals(matches, start, offset, region);
	}

	public static Collection getCssAttributeMatches(String start, int offset, ITypedRegion region) {
		List matches = getMatches(CSS_PROPERTIES, start);
		return getSortedCompletionProposals(matches, start, offset, region);
	}

	private static List getMatches(String[] strings, String start) {
		ArrayList res = new ArrayList();

		for (int i = 0; i < strings.length; i++) {
			String tag = strings[i];
			if (tag.startsWith(start)) {
				res.add(tag);
			}
		}

		return res;
	}

	private static Collection getSortedCompletionProposals(List matches, String start, int offset,
			ITypedRegion region) {
		ArrayList res = new ArrayList(matches.size());
		Collections.sort(matches);
		for (int i = 0; i < matches.size(); i++) {
			String tag = (String) matches.get(i);
			CompletionProposal cp = new CompletionProposal(tag, offset, region.getLength() - 1, tag
					.length());
			res.add(cp);
		}
		return res;

	}

	public static boolean isHtmlTag(String str) {
		boolean res = false;
		
		for (int i = 0; i < HTML_KEYWORDS.length; i++) {
			for (int j = 0; j < HTML_KEYWORDS[i].length; j++) {
				if (HTML_KEYWORDS[i][j].equals(str)) {
					res = true;
					break;
				}
			}
		}

		return res;
	}
	
	public static boolean isCssTag(String str) {
		boolean res = false;
		
		for (int i = 0; i < CSS_KEYWORDS.length; i++) {
			for (int j = 0; j < CSS_KEYWORDS[i].length; j++) {
				if (CSS_KEYWORDS[i][j].equals(str)) {
					res = true;
					break;
				}
			}
		}

		return res;
	}	
}

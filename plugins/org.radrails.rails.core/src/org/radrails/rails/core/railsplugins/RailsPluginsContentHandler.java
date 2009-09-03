/*******************************************************************************
 * Copyright (c) 2005 RadRails.org and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.radrails.rails.core.railsplugins;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Content handler for an XML document containing a list of Rails plugins from the RailsPlugins web service.
 * 
 * @author mkent
 */
public class RailsPluginsContentHandler implements ContentHandler
{

	private List<RailsPluginDescriptor> fRailsPlugins;
	private StringBuffer fData;
	private RailsPluginDescriptor fPlugin;

	public List<RailsPluginDescriptor> getRailsPlugins()
	{
		return fRailsPlugins;
	}

	public void endDocument() throws SAXException
	{
	}

	public void startDocument() throws SAXException
	{
		fRailsPlugins = new ArrayList<RailsPluginDescriptor>();
	}

	public void characters(char[] ch, int start, int length) throws SAXException
	{
		for (int i = start; i < start + length; i++)
		{
			fData.append(ch[i]);
		}
	}

	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
	{
	}

	public void endPrefixMapping(String prefix) throws SAXException
	{
	}

	public void skippedEntity(String name) throws SAXException
	{
	}

	public void setDocumentLocator(Locator locator)
	{
	}

	public void processingInstruction(String target, String data) throws SAXException
	{
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException
	{
	}

	public void endElement(String namespaceURI, String localName, String qName) throws SAXException
	{
		if (qName.equals("plugin"))
		{
			fRailsPlugins.add(fPlugin);
		}
		else
		{
			if (fPlugin != null && fData != null)
				fPlugin.setProperty(qName, fData.toString());
		}
	}

	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
	{
		if (qName.equals("plugin"))
		{
			fPlugin = new RailsPluginDescriptor();
		}
		fData = new StringBuffer();
	}

}

package com.evervolv.toolbox.utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlHandler extends DefaultHandler {
	
	private static final String TAG = "EVToolbox";
	Boolean currentElement = false;
	String currentValue = null;
	
	/**
	 * Called when tag starts ( ex:- <name>AndroidPeople</name> -- <name> )
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		currentElement = true;

		if (localName.equals("applist")) {
			/** Start */

		} else if (localName.equals("bloatentry")){

		} else if (localName.equals("manifest")){

		}

	}

	/**
	 * Called when tag closing ( ex:- <name>AndroidPeople</name> -- </name> )
	 */
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {

		currentElement = false;

		/** set value */
		if (localName.equalsIgnoreCase("name")){

		} else if (localName.equalsIgnoreCase("website")){

		} else if (localName.equalsIgnoreCase("bloatitem")){

		} else if (localName.equalsIgnoreCase("romname")) {

		} else if (localName.equalsIgnoreCase("siteurl")) {

		} else if (localName.equalsIgnoreCase("download")) {

		}

	}

	/**
	 * Called to get tag characters ( ex:- <name>AndroidPeople</name> -- to get
	 * AndroidPeople Character )
	 */
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {

		if (currentElement) {
			currentValue = new String(ch, start, length);
			currentElement = false;
		}

	}

}

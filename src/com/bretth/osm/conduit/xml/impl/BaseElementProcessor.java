package com.bretth.osm.conduit.xml.impl;

import java.util.Date;

import com.bretth.osm.conduit.task.Sink;


/**
 * Provides common functionality shared by element processor implementations.
 * 
 * @author Brett Henderson
 */
public abstract class BaseElementProcessor implements ElementProcessor {
	private BaseElementProcessor parentProcessor;
	private ElementProcessor dummyChildProcessor;
	//private DateFormat dateFormat;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parentProcessor
	 *            The parent of this element processor.
	 */
	protected BaseElementProcessor(BaseElementProcessor parentProcessor) {
		this.parentProcessor = parentProcessor;
		
		//dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
	
	
	/**
	 * @return The osmSink.
	 */
	protected Sink getOsmSink() {
		return parentProcessor.getOsmSink();
	}
	
	
	/**
	 * This implementation returns a dummy element processor as the child which
	 * ignores all nested xml elements. Sub-classes wishing to handle child
	 * elements must override this method and delegate to this method for xml
	 * elements they don't care about.
	 * 
	 * @param uri
	 *            The element uri.
	 * @param localName
	 *            The element localName.
	 * @param qName
	 *            The element qName.
	 * @return A dummy element processor.
	 */
	public ElementProcessor getChild(String uri, String localName, String qName) {
		if (dummyChildProcessor == null) {
			dummyChildProcessor = new DummyElementProcessor(this);
		}
		
		return dummyChildProcessor;
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public ElementProcessor getParent() {
		return parentProcessor;
	}
	
	
	/**
	 * Parses a date using the standard osm date format.
	 * 
	 * @param data
	 *            The date string to be parsed.
	 * @return The parsed date.
	 */
	protected Date parseTimestamp(String data) {
		//try {
			// TODO: Fix date format so it doesn't break on the planet file.
			// TODO: Fix the timezones so that it treats value as GMT.
			//return dateFormat.parse(data);
			return new Date();
			
		//} catch (ParseException e) {
		//	throw new OsmLoaderRuntimeException("Unable to parse date from data (" + data + ")");
		//}
	}
}

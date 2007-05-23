package com.bretth.osm.conduit.filter;

import java.util.BitSet;

import com.bretth.osm.conduit.ConduitRuntimeException;
import com.bretth.osm.conduit.data.Node;
import com.bretth.osm.conduit.data.Segment;
import com.bretth.osm.conduit.data.SegmentReference;
import com.bretth.osm.conduit.data.Tag;
import com.bretth.osm.conduit.data.Way;
import com.bretth.osm.conduit.task.Sink;
import com.bretth.osm.conduit.task.SinkSource;


/**
 * Provides a filter for extracting all elements that lie within a specific
 * geographical box identified by latitude and longitude coordinates.
 * 
 * @author Brett Henderson
 */
public class BoundingBoxFilter implements SinkSource {
	private Sink osmSink;
	private double left;
	private double right;
	private double top;
	private double bottom;
	private BitSet availableNodes;
	private BitSet availableSegments;
	
	
	/**
	 * Creates a new instance with the specified geographical coordinates. When
	 * filtering, nodes right on the edge of the box will be included.
	 * 
	 * @param left
	 *            The longitude marking the left edge of the bounding box.
	 * @param right
	 *            The longitude marking the right edge of the bounding box.
	 * @param top
	 *            The latitude marking the top edge of the bounding box.
	 * @param bottom
	 *            The latitude marking the bottom edge of the bounding box.
	 */
	public BoundingBoxFilter(double left, double right, double top, double bottom) {
		this.left = left;
		this.right = right;
		this.top = top;
		this.bottom = bottom;
		
		availableNodes = new BitSet();
		availableSegments = new BitSet();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void addNode(Node node) {
		long nodeId;
		double latitude;
		double longitude;
		
		nodeId = node.getId();
		latitude = node.getLatitude();
		longitude = node.getLongitude();
		
		// Only add the node if it lies within the box boundaries.
		if (top >= latitude && bottom <= latitude && left <= longitude && right >= longitude) {
			osmSink.addNode(node);
			
			// Ensure that the node identifier can be represented as an integer.
			if (nodeId > Integer.MAX_VALUE) {
				throw new ConduitRuntimeException("The bounding box filter can only handle node identifiers up to " + Integer.MAX_VALUE + ".");
			}
			
			availableNodes.set((int) nodeId);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void addSegment(Segment segment) {
		long segmentId;
		long nodeIdFrom;
		long nodeIdTo;
		
		segmentId = segment.getId();
		nodeIdFrom = segment.getFrom();
		nodeIdTo = segment.getTo();
		
		// Ensure that all identifiers can be represented as integers.
		if (segmentId > Integer.MAX_VALUE) {
			throw new ConduitRuntimeException("The bounding box filter can only handle segment identifiers up to " + Integer.MAX_VALUE + ".");
		}
		if (nodeIdFrom > Integer.MAX_VALUE) {
			throw new ConduitRuntimeException("The bounding box filter can only handle node identifiers up to " + Integer.MAX_VALUE + ".");
		}
		if (nodeIdTo > Integer.MAX_VALUE) {
			throw new ConduitRuntimeException("The bounding box filter can only handle node identifiers up to " + Integer.MAX_VALUE + ".");
		}
		
		// Only add the segment if both of its nodes are within the bounding box.
		if (availableNodes.get((int) nodeIdFrom) && availableNodes.get((int) nodeIdTo)) {
			osmSink.addSegment(segment);
			availableSegments.set((int) segmentId);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void addWay(Way way) {
		Way filteredWay;
		
		// Create a new way object to contain only items within the bounding box.
		filteredWay = new Way(way.getId(), way.getTimestamp());
		
		// Only add segment references to segments that are within the bounding box.
		for (SegmentReference segmentReference : way.getSegmentReferenceList()) {
			long segmentId;
			
			segmentId = segmentReference.getSegmentId();
			
			// Ensure that the segment identifier can be represented as an integer.
			if (segmentId > Integer.MAX_VALUE) {
				throw new ConduitRuntimeException("The bounding box filter can only handle segment identifiers up to " + Integer.MAX_VALUE + ".");
			}
			
			
			if (availableSegments.get((int) segmentId)) {
				filteredWay.addSegmentReference(segmentReference);
			}
		}
		
		// Only add ways that contain segments.
		if (filteredWay.getSegmentReferenceList().size() > 0) {
			// Add all tags to the filtered node.
			for (Tag tag : way.getTagList()) {
				filteredWay.addTag(tag);
			}
			
			osmSink.addWay(filteredWay);
		}
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void complete() {
		osmSink.complete();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void release() {
		osmSink.release();
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void setSink(Sink osmSink) {
		this.osmSink = osmSink;
	}
}

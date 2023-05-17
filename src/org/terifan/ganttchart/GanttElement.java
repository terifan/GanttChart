package org.terifan.ganttchart;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;
import static org.terifan.ganttchart.StyleSheet.COLORS;


public class GanttElement implements AutoCloseable
{
	protected final Rectangle mBounds = new Rectangle();

	protected final GanttElement mParent;
	protected final ArrayList<GanttSegment> mSegments;
	protected final ArrayList<GanttElement> mElements;

	protected Object mFrom;
	protected Object mTo;


	GanttElement(GanttElement aParent)
	{
		mParent = aParent;
		mSegments = new ArrayList<>();
		mElements = new ArrayList<>();
	}


	public int getElementCount()
	{
		return mElements.size();
	}


	public GanttElement getElement(int aIndex)
	{
		return mElements.get(aIndex);
	}


	public long getStartTime()
	{
		return mSegments.get(0).getStartTime();
	}


	public long getEndTime()
	{
		return mSegments.get(mSegments.size() - 1).getEndTime();
	}


	public GanttSegment getSegment(int aIndex)
	{
		return mSegments.get(aIndex);
	}


	public int getSegmentCount()
	{
		return mSegments.size();
	}


	void add(GanttSegment aSegment)
	{
		mSegments.add(aSegment);
	}


	public void tick(String aSegmentName)
	{
		long time = System.nanoTime();

		if (mSegments.size() > 0)
		{
			mSegments.get(mSegments.size() - 1).setEndTime(time);
		}

		mSegments.add(new GanttSegment(time, time, createColorFromName(aSegmentName), aSegmentName, null));
	}


	/**
	 * Creates a new element below the current element.
	 *
	 * @param aElementName
	 *   a name or description of the element
	 * @return
	 *   the new element
	 */
	public GanttElement enter(String aElementName)
	{
		return enter(aElementName, null);
	}


	/**
	 * Creates a new element below the current element.
	 *
	 * @param aElementName
	 *   a name or description of the element
	 * @param aSegmentName
	 *
	 * @return
	 *   the new element
	 */
	public synchronized GanttElement enter(String aElementName, String aSegmentName)
	{
		long time = System.nanoTime();

		GanttElement element = new GanttElement(this);

		element.add(new GanttSegment(time, time, createColorFromName(aElementName + aSegmentName), aElementName, aSegmentName));

		mElements.add(element);

		repaint();

		return element;
	}


	/**
	 * Sets the end time of the last element
	 */
	public void exit()
	{
		mSegments.get(mSegments.size() - 1).setEndTime(System.nanoTime());
	}


	public void repaint()
	{
		mParent.repaint();
	}


	@Override
	public void close()
	{
		exit();
	}


	Object getFrom()
	{
		return mFrom;
	}


	Object getTo()
	{
		return mTo;
	}


	public GanttElement from(Object aToken)
	{
		mFrom = aToken;
		return this;
	}


	public GanttElement to(Object aToken)
	{
		mTo = aToken;
		return this;
	}


	private static Color createColorFromName(String aDescription)
	{
		return COLORS[new Random(aDescription.hashCode()).nextInt(COLORS.length)];
	}
}

package org.terifan.ganttchart;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;


public class GanttElement implements AutoCloseable
{
	private final static Color[] COLORS =
	{
		new Color(0, 150, 220),
		new Color(230, 25, 75),
		new Color(60, 180, 75),
		new Color(255, 225, 25),
		new Color(245, 130, 48),
		new Color(145, 30, 180),
		new Color(70, 240, 240),
		new Color(240, 50, 230),
		new Color(210, 245, 60),
		new Color(250, 190, 190),
		new Color(0, 128, 128),
		new Color(230, 190, 255),
		new Color(170, 110, 40),
		new Color(255, 250, 200),
		new Color(128, 0, 0),
		new Color(170, 255, 195),
		new Color(128, 128, 0),
		new Color(255, 215, 180),
		new Color(0, 0, 128),
		new Color(128, 128, 128)
	};

	protected final Rectangle mBounds = new Rectangle();

	protected final GanttElement mParent;
	protected final ArrayList<GanttSegment> mSegments;
	protected final ArrayList<GanttElement> mElements;

	protected Object mFrom;
	protected Object mTo;

	private static int ci;


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


	public void tick(String aDescription)
	{
		long time = System.nanoTime();

		if (mSegments.size() > 0)
		{
			mSegments.get(mSegments.size() - 1).setEndTime(time);
		}

		mSegments.add(new GanttSegment(time, time, aDescription, COLORS[ci++ % COLORS.length]));
	}


	/**
	 * Creates a new element below the current element.
	 *
	 * @param aDescription
	 *   a name or description of the element
	 * @return
	 *   the new element
	 */
	public synchronized GanttElement enter(String aDescription)
	{
		long time = System.nanoTime();

		GanttElement element = new GanttElement(this);

		element.add(new GanttSegment(time, time, aDescription, COLORS[ci++ % COLORS.length]));

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
}

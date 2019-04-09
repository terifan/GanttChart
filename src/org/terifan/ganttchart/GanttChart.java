package org.terifan.ganttchart;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.TreeMap;


/**
 * This GanttChart implementation is used to measure performance of processes.
 */
public class GanttChart implements AutoCloseable
{
	private final static Color[] C =
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

	private final ArrayDeque<Long> mStack;

	private ArrayList<Long> mKeys;
	private TreeMap<Long, GanttChartElement> mMap;

	GanttChartPanel mPanel;

	private long mStartTime;
	private long mEndTime;
	private int ci;


	public GanttChart()
	{
		mMap = new TreeMap<>();
		mKeys = new ArrayList<>();
		mStack = new ArrayDeque<>();
	}


	/**
	 * Splits the current work item.
	 *
	 * @param aDescription
	 *   a name or description of the work item
	 */
	public synchronized void tick(String aDescription)
	{
		long time = System.nanoTime();

		mMap.get(mStack.getLast()).add(new GanttChartElement(time, aDescription, C[ci++ % C.length]));

		mEndTime = time;

		if (mPanel != null)
		{
			mPanel.repaint();
		}
	}


	/**
	 * Creates a new work item nested below the current work item.
	 *
	 * @param aDescription
	 *   a name or description of the work item
	 * @return
	 *   this
	 */
	public synchronized GanttChart enter(String aDescription)
	{
		long time = System.nanoTime();

		if (mMap.isEmpty())
		{
			mStartTime = time;
			mEndTime = time;
		}

		mKeys.add(time);
		mStack.add(time);

		mMap.put(time, new GanttChartElement(time, aDescription, C[ci++ % C.length]));

		if (mPanel != null)
		{
			mPanel.revalidate();
			mPanel.repaint();
		}

		return this;
	}


	/**
	 * Exits the current work item.
	 */
	public synchronized void exit()
	{
		long key = mStack.removeLast();
		long time = System.nanoTime();

		mMap.get(key).setEndTime(time);

		mEndTime = time;

		if (mPanel != null)
		{
			mPanel.repaint();
		}
	}


	/**
	 * Same as calling the exit method.
	 */
	@Override
	public void close()
	{
		exit();
	}


	long getStartTime()
	{
		return mStartTime;
	}


	synchronized long getEndTime()
	{
		return mStack.isEmpty() ? mEndTime : System.nanoTime();
	}


	public int size()
	{
		return mMap.size();
	}


	GanttChartElement get(int aIndex)
	{
		return mMap.get(mKeys.get(aIndex));
	}
}

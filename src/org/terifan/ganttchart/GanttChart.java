package org.terifan.ganttchart;

import java.util.ArrayList;
import java.util.TreeMap;
import static org.terifan.ganttchart.GanttChartPanel.C;

 
/**
 * This GanttChart implementation is used to measure performance of processes. Create an instance of the chart, visualise it in a
 * window and then for each point of interest add an enter before and exit after to measure and display the time. Processes can be nested
 * and divided into subroutines by calling the tick method.
 *
 * GanttChart chart = new GanttChart();
 * new SimpleGanttWindow(chart).show();
 * chart.enter("step 1");
 * // perform work...
 * chart.tick("step 2");
 * // perform work...
 * chart.exit();
 */
public class GanttChart implements AutoCloseable
{
	private final ArrayList<Long> mStack;

	final TreeMap<Long, GanttChartElement> mMap;
	GanttChartPanel mPanel;

	private long mStartTime;
	private long mEndTime;
	private int ci;


	public GanttChart()
	{
		mMap = new TreeMap<>();
		mStack = new ArrayList<>();
	}


	/**
	 * Splits the current work item.
	 *
	 * @param aDescription
	 *   a name or description of the work item
	 */
	public void tick(String aDescription)
	{
		long time = System.nanoTime();

		mMap.get(mStack.get(mStack.size() - 1)).getSubElements().add(new GanttChartElement(time, aDescription, C[ci++ % C.length]));

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
	public GanttChart enter(String aDescription)
	{
		long time = System.nanoTime();

		if (mMap.isEmpty())
		{
			mStartTime = time;
			mEndTime = time;
		}

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
	public void exit()
	{
		long key = mStack.remove(mStack.size() - 1);
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


	long getEndTime()
	{
		return mStack.isEmpty() ? mEndTime : System.nanoTime();
	}
}

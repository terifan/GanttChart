package org.terifan.ganttchart;

import java.util.ArrayList;


/**
 * This GanttChart implementation is used to measure performance of processes.
 */
public class GanttChart extends GanttElement
{
	GanttChartPanel mPanel;


	public GanttChart()
	{
		super(null);
	}


	@Override
	public long getStartTime()
	{
		if (mElements.isEmpty())
		{
			return 0;
		}

		return mElements.get(0).mSegments.get(0).getStartTime();
	}


	@Override
	public long getEndTime()
	{
		if (mElements.isEmpty())
		{
			return 0;
		}

		ArrayList<GanttSegment> segments = mElements.get(mElements.size() - 1).mSegments;

		return segments.get(segments.size() - 1).getEndTime();
	}


	@Override
	public void repaint()
	{
		if (mPanel != null)
		{
			mPanel.revalidate();
			mPanel.repaint();
		}
	}
}

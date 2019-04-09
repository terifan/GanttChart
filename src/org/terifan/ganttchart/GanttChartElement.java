package org.terifan.ganttchart;

import java.awt.Color;
import java.util.ArrayList;


public class GanttChartElement
{
	private ArrayList<GanttChartElement> mSubElements;
	private long mStartTime;
	private long mEndTime;
	private String mDescription;
	private Color mColor;


	GanttChartElement(long aStartTime, String aDescription, Color aColor)
	{
		mStartTime = aStartTime;
		mEndTime = aStartTime;
		mDescription = aDescription;
		mColor = aColor;
	}


	public long getStartTime()
	{
		return mStartTime;
	}


	public long getEndTime()
	{
		return isRunning() ? System.nanoTime() : mEndTime;
	}


	void setEndTime(long aEndTime)
	{
		mEndTime = aEndTime;
	}


	public String getDescription()
	{
		return mDescription;
	}


	public Color getColor()
	{
		return mColor;
	}


	public boolean isRunning()
	{
		return mEndTime == mStartTime;
	}


	public long getSubTime(int aIndex)
	{
		if (aIndex == 0)
		{
			return mStartTime;
		}
		if (aIndex == getSubCount())
		{
			return getEndTime();
		}

		return mSubElements.get(aIndex - 1).getStartTime();
	}


	public GanttChartElement getSubElement(int aIndex)
	{
		return aIndex == 0 ? this : mSubElements.get(aIndex - 1);
	}


	public int getSubCount()
	{
		return mSubElements == null ? 1 : 1 + mSubElements.size();
	}


	void add(GanttChartElement aElement)
	{
		if (mSubElements == null)
		{
			mSubElements = new ArrayList<>();
		}
		mSubElements.add(aElement);
	}
}

package org.terifan.ganttchart;

import java.awt.Color;


public class GanttSegment
{
	protected long mStartTime;
	protected long mEndTime;
	protected String mDescription;
	protected Color mColor;


	public GanttSegment(long aStartTime, long aEndTime, String aDescription, Color aColor)
	{
		mStartTime = aStartTime;
		mEndTime = aEndTime;
		mDescription = aDescription;
		mColor = aColor;
	}


	long getStartTime()
	{
		return mStartTime;
	}


	long getEndTime()
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


	boolean isRunning()
	{
		return mEndTime == mStartTime;
	}
}

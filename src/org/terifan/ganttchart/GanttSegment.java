package org.terifan.ganttchart;

import java.awt.Color;


public class GanttSegment
{
	protected long mStartTime;
	protected long mEndTime;
	protected String mDescription;
	protected String mSegmentDescription;
	protected Color mColor;


	public GanttSegment(long aStartTime, long aEndTime, Color aColor, String aDescription, String aSegmentDescription)
	{
		mStartTime = aStartTime;
		mEndTime = aEndTime;
		mColor = aColor;
		mDescription = aDescription;
		mSegmentDescription = aSegmentDescription;
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


	public String getSegmentDescription()
	{
		return mSegmentDescription != null ? mSegmentDescription : mDescription;
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

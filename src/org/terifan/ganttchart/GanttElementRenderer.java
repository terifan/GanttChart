package org.terifan.ganttchart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;


public class GanttElementRenderer
{
	private final static Color ROW_LIGHT = new Color(255, 255, 255);
	private final static Color ROW_DARK = new Color(242, 242, 242);
	private final static int MINIMUM_WIDTH = 50;

	private int mRowHeight = 24;
	private int mBarHeight = 9;
	private int mLabelWidth = 200;
	private int mRightMargin = 50;
	private Font mLabelFont = new Font("segoe ui", Font.PLAIN, 12);
	private Font mTimeFont = new Font("segoe ui", Font.PLAIN, 9);
	private Color mSelectionColor = new Color(0.0f, 0.3f, 0.6f);
	private BasicStroke mDottedStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{1f}, 0.5f);
	private int mIndent = 18;
	private boolean mHideSubSegmentRanges;

	private GanttChart mGanttChart;
	private GanttElement mSelectedElement;


	protected void paintComponent(Graphics aGraphics, int aX, int aY, int aWidth, int aHeight)
	{
		Graphics2D g = (Graphics2D)aGraphics;

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int w = Math.max(aWidth, mLabelWidth + mRightMargin + MINIMUM_WIDTH);
		int h = aHeight;

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, w, aHeight);

		for (int i = 0, y = 0; y < h; i++, y += mRowHeight)
		{
			g.setColor((i & 1) == 0 ? ROW_LIGHT : ROW_DARK);
			g.fillRect(0, y, w, mRowHeight);
		}

		long startTime = mGanttChart.getStartTime();
		long endTime = mGanttChart.getEndTime();

		int wi = w - mLabelWidth - mRightMargin;

//		drawFlowLines(g);
		drawElements(g, mGanttChart, w, startTime, endTime, wi, 0, "");
		drawGrid(g, wi, h);
	}


//	private void drawFlowLines(Graphics2D aGraphics)
//	{
//		HashMap<Object, ArrayList<GanttElement>> startPoints = new HashMap<>();
//		HashMap<Object, ArrayList<GanttElement>> endPoints = new HashMap<>();
//
//		forEach((r, e) ->
//		{
//			if (e.getFrom() != null)
//			{
//				startPoints.computeIfAbsent(e.getFrom(), x -> new ArrayList<>()).add(e);
//			}
//			if (e.getTo() != null)
//			{
//				endPoints.computeIfAbsent(e.getTo(), x -> new ArrayList<>()).add(e);
//			}
//			return null;
//		});
//
//		Stroke oldStroke = aGraphics.getStroke();
//		aGraphics.setColor(new Color(220, 220, 220));
//		aGraphics.setStroke(new BasicStroke(3f));
//
//		for (Entry<Object, ArrayList<GanttElement>> entry : startPoints.entrySet())
//		{
//			for (GanttElement end : endPoints.computeIfAbsent(entry.getKey(), e -> new ArrayList<>()))
//			{
//				int x1 = 0;
//				for (GanttElement start : startPoints.computeIfAbsent(entry.getKey(), e -> new ArrayList<>()))
//				{
//					x1 = Math.max(x1, start.mBounds.x + start.mBounds.width);
//				}
//				for (GanttElement start : startPoints.computeIfAbsent(entry.getKey(), e -> new ArrayList<>()))
//				{
//					int x0 = start.mBounds.x + start.mBounds.width;
//					int y0 = start.mBounds.y + start.mBounds.height - 1 - (mRowHeight - mBarHeight) / 4;
//					int x2 = end.mBounds.x;
//					int y2 = end.mBounds.y + end.mBounds.height / 2;
//
//					if (x2 > x0)
//					{
//						aGraphics.drawLine(x0, y0, x1, y0);
//						aGraphics.drawLine(x1, y0, x1, y2);
//						aGraphics.drawLine(x1, y2, x2, y2);
//					}
//				}
//			}
//		}
//
//		aGraphics.setStroke(oldStroke);
//	}


	private int drawElements(Graphics2D aGraphics, GanttElement aElement, int aWidth, long aStartTime, long aEndTime, int aContentWidth, int aRowIndex, String aTreePath)
	{
		for (int i = 0, sz = aElement.getElementCount(); i < sz; i++)
		{
			aRowIndex = drawElement(aGraphics, aElement.getElement(i), aWidth, aStartTime, aEndTime, aContentWidth, aRowIndex, aTreePath + (aRowIndex == 0 ? " " : i == sz - 1 ? "\\" : "+"));

			aRowIndex = drawElements(aGraphics, aElement.getElement(i), aWidth, aStartTime, aEndTime, aContentWidth, aRowIndex, aTreePath + (i == sz - 1 ? " " : "|"));
		}

		return aRowIndex;
	}


	private int drawElement(Graphics2D aGraphics, GanttElement aElement, int aWidth, long aStartTime, long aEndTime, int aContentWidth, int aRowIndex, String aTreePath)
	{
		int y = aRowIndex * mRowHeight;

		if (mSelectedElement == aElement)
		{
			aGraphics.setColor(mSelectionColor);
			aGraphics.fillRect(0, y, aWidth, mRowHeight);
		}

		long startTime = aElement.getStartTime();
		long endTime = aElement.getEndTime();

		int xleft = 1 << 30;
		int xright = 0;

		for (int i = 0, sz = aElement.getSegmentCount(); i < sz; i++)
		{
			GanttSegment segment = aElement.getSegment(i);

			int x0 = mLabelWidth + (int)((segment.getStartTime() - aStartTime) * aContentWidth / (aEndTime - aStartTime));
			int x1 = mLabelWidth + (int)((segment.getEndTime() - aStartTime) * aContentWidth / (aEndTime - aStartTime));

			aGraphics.setColor(segment.getColor());

			if (segment.isRunning() && i == sz - 1)
			{
				aGraphics.drawRect(x0, y + (mRowHeight - mBarHeight) / 2, x1 - x0, mBarHeight);
			}
			else
			{
				aGraphics.fillRect(x0, y + (mRowHeight - mBarHeight) / 2, x1 - x0 + 1, mBarHeight + 1);
			}

			if (x0 < xleft)
			{
				xleft = x0;
			}
			if (x1 > xright)
			{
				xright = x1;
			}
		}

		if (mHideSubSegmentRanges)
		{
			Stroke oldStroke = aGraphics.getStroke();
			aGraphics.setStroke(mDottedStroke);
			for (long[] range : findRanges(aElement, new ArrayList<>(), false))
			{
				int x0 = mLabelWidth + (int)((range[0] - aStartTime) * aContentWidth / (aEndTime - aStartTime));
				int x1 = mLabelWidth + (int)((range[1] - aStartTime) * aContentWidth / (aEndTime - aStartTime));

				aGraphics.setColor(mSelectedElement == aElement ? mSelectionColor : (aRowIndex & 1) == 0 ? ROW_LIGHT : ROW_DARK);
				aGraphics.fillRect(x0 + 1, y + (mRowHeight - mBarHeight) / 2, x1 - x0, mBarHeight + 1);

				aGraphics.setColor(mSelectedElement == aElement ? Color.WHITE : Color.BLACK);
				aGraphics.drawLine(x0, y + (mRowHeight - mBarHeight) / 2, x1, y + (mRowHeight - mBarHeight) / 2);
				aGraphics.drawLine(x0, y + (mRowHeight - mBarHeight) / 2 + mBarHeight, x1, y + (mRowHeight - mBarHeight) / 2 + mBarHeight);
			}
			aGraphics.setStroke(oldStroke);
		}

		int x1 = mLabelWidth + (int)((endTime - aStartTime) * aContentWidth / (aEndTime - aStartTime));

		aGraphics.setColor(mSelectedElement == aElement ? Color.WHITE : Color.BLACK);
		aGraphics.setFont(mTimeFont);
		aGraphics.drawString(formatTime(endTime - startTime), x1 + 5, y + mRowHeight / 2 + aGraphics.getFontMetrics().getDescent() + 1);

		drawElementLabel(aGraphics, aElement, aWidth, aStartTime, aEndTime, aContentWidth, aRowIndex, aTreePath);

		aElement.mBounds.setBounds(xleft, y, xright - xleft, mRowHeight);

		return aRowIndex + 1;
	}


	private void drawElementLabel(Graphics2D aGraphics, GanttElement aElement, int aWidth, long aStartTime, long aEndTime, int aContentWidth, int aRowIndex, String aTreePath)
	{
		int y = aRowIndex * mRowHeight;
		int ty = y + mRowHeight / 2;

		String description = aElement.getSegment(0).getDescription();
		int j = description.indexOf("::");
		if (j != -1)
		{
			description = description.substring(0, j);
		}

		aGraphics.setColor(mSelectedElement == aElement ? Color.WHITE : Color.BLACK);
		aGraphics.setFont(mLabelFont);
		aGraphics.drawString(description, 2 + (aTreePath.length() - 1) * mIndent, ty + aGraphics.getFontMetrics().getDescent() + 1);

		if (mIndent > 0)
		{
			Stroke oldStroke = aGraphics.getStroke();
			aGraphics.setStroke(mDottedStroke);

			for (int i = 1; i < aTreePath.length(); i++)
			{
				int x = 2 + (i - 1) * mIndent;
				int w = mIndent - 2;
				int h = mRowHeight / 2;
				int hh = mRowHeight;

				switch (aTreePath.charAt(i))
				{
					case '\\':
						aGraphics.drawLine(x + 5, y, x + 5, y + h);
						aGraphics.drawLine(x + 5, y + h, x + w, y + h);
						break;
					case '+':
						aGraphics.drawLine(x + 5, y, x + 5, y + hh);
						aGraphics.drawLine(x + 5, y + h, x + w, y + h);
						break;
					case '|':
						aGraphics.drawLine(x + 5, y, x + 5, y + hh);
						break;
					case '-':
						aGraphics.drawLine(x, y + h, x + w, y + h);
						break;
					case ' ':
						break;
				}

				if (aElement.getElementCount() > 0)
				{
					drawElementTreeIcon(aGraphics, oldStroke, x, y, h);
				}
			}

			aGraphics.setStroke(oldStroke);
		}
	}


	private void drawElementTreeIcon(Graphics2D aGraphics, Stroke aOldStroke, int aX, int aY, int aH)
	{
		Stroke oldStroke = aGraphics.getStroke();
		Color oldColor = aGraphics.getColor();

		aGraphics.setStroke(aOldStroke);

		aGraphics.setColor(Color.WHITE);
		aGraphics.fillRoundRect(aX, aY + aH - 5, 10, 10, 4, 4);

		aGraphics.setColor(Color.BLACK);
		aGraphics.drawRoundRect(aX, aY + aH - 5, 10, 10, 4, 4);
		aGraphics.drawLine(aX + 2, aY + aH, aX + 8, aY + aH);
//		aGraphics.drawLine(x + 5, y + h - 3, x + 5, y + h + 3);

		aGraphics.setColor(oldColor);
		aGraphics.setStroke(oldStroke);
	}


	private ArrayList<long[]> findRanges(GanttElement aElement, ArrayList<long[]> aList, boolean aInclude)
	{
		if (aInclude)
		{
			for (int i = 0, sz = aElement.getSegmentCount(); i < sz; i++)
			{
				aList.add(new long[]
				{
					aElement.getSegment(i).getStartTime(), aElement.getSegment(i).getEndTime()
				});
			}
		}

		for (int i = 0, sz = aElement.getElementCount(); i < sz; i++)
		{
			findRanges(aElement.getElement(i), aList, true);
		}

		return aList;
	}


	private void drawGrid(Graphics2D aGraphics, int aContentWidth, int aHeight)
	{
		Color c0 = new Color(0.6f, 0.6f, 0.6f, 0.2f);
		Color c1 = new Color(0.8f, 0.8f, 0.8f, 0.2f);

		for (int col = 0; col <= 10; col++)
		{
			int x = mLabelWidth + col * aContentWidth / 10;
			aGraphics.setColor(c0);
			aGraphics.drawLine(x, 0, x, aHeight);

			if (col < 10)
			{
				x += aContentWidth / 10 / 2;
				aGraphics.setColor(c1);
				aGraphics.drawLine(x, 0, x, aHeight);
			}
		}
	}


	static String formatTime(long aTimeNanos)
	{
		if (aTimeNanos < 1000)
		{
			return aTimeNanos + "ns";
		}
		if (aTimeNanos < 1000_000)
		{
			return aTimeNanos / 1000 + "µs";
		}
		if (aTimeNanos < 10_000_000_000L)
		{
			return aTimeNanos / 1000_000 + "ms";
		}

		return aTimeNanos / 1000_000_000 + "s";
	}
}

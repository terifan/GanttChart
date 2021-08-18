package org.terifan.ganttchart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import static org.terifan.ganttchart.GanttChartPanel.formatTime;


public class GanttChartDetailPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private GanttElement mSelectedElement;
	private int mLabelWidth = 100;
	private int mRowHeight = 24;
	private int mBarHeight = 9;
	private int mRightMargin = 50;
	private Font mLabelFont = new Font("segoe ui", Font.PLAIN, 12);
	private Font mTimeFont = new Font("segoe ui", Font.PLAIN, 9);


	public void setSelectedElement(GanttElement aSelectedElement)
	{
		mSelectedElement = aSelectedElement;
	}


	@Override
	protected void paintComponent(Graphics aGraphics)
	{
		Graphics2D g = (Graphics2D)aGraphics;

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int w = Math.max(getWidth(), mLabelWidth + mRightMargin + 50);
		int h = getHeight();

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, w, h);

		if (mSelectedElement == null)
		{
			return;
		}

		long startTime = mSelectedElement.getStartTime();
		long endTime = mSelectedElement.getEndTime();

		int wi = w - mLabelWidth - mRightMargin;
		int y = 0;

		for (int i = 0, sz = mSelectedElement.getSegmentCount(); i < sz; i++)
		{
			GanttSegment segment = mSelectedElement.getSegment(i);

			long t0 = segment.getStartTime();
			long t1 = segment.getEndTime();

			int x0 = (int)((t0 - startTime) * wi / (endTime - startTime));
			int x1 = (int)((t1 - startTime) * wi / (endTime - startTime));

			g.setColor(segment.getColor());
			g.fillRect(mLabelWidth + x0, y + (mRowHeight - mBarHeight) / 2, Math.max(1, x1 - x0), mBarHeight);

			g.setColor(Color.BLACK);
			g.setFont(mTimeFont);
			g.drawString(formatTime(t1 - t0), mLabelWidth + x1 + 5, y + mRowHeight/2 + g.getFontMetrics().getDescent());

			g.setColor(Color.BLACK);
			g.setFont(mLabelFont);
			g.drawString(segment.getSegmentDescription(), 2, y + mRowHeight/2 + g.getFontMetrics().getDescent());

			y += mRowHeight;
		}
	}


	@Override
	public Dimension preferredSize()
	{
		return new Dimension(mLabelWidth + 50 + mRightMargin, mSelectedElement == null ? 1 : mRowHeight * mSelectedElement.getSegmentCount());
	}
}

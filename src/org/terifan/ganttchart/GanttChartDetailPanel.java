package org.terifan.ganttchart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import javax.swing.JPanel;
import static org.terifan.ganttchart.GanttChartPanel.formatTime;


public class GanttChartDetailPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private GanttChartElement mElement;
	private int mLabelWidth = 100;
	private int mRowHeight = 24;
	private int mBarHeight = 9;
	private int mRightMargin = 50;
	private Font mLabelFont = new Font("segoe ui", Font.PLAIN, 12);
	private Font mTimeFont = new Font("segoe ui", Font.PLAIN, 9);


	public GanttChartDetailPanel()
	{
	}


	public void setElement(GanttChartElement aElement)
	{
		mElement = aElement;
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

		if (mElement == null)
		{
			return;
		}

		ArrayList<GanttChartElement> subElements = mElement.getSubElements();

		long startTime = mElement.getStartTime();
		long endTime = mElement.getEndTime();

		int wi = w - mLabelWidth - mRightMargin;
		int x0 = 0;
		int y = 0;
		long lastTime = startTime;

		for (int i = 0; i <= subElements.size(); i++)
		{
			int x1;
			long tickTime;

			if (i == subElements.size())
			{
				tickTime = endTime;
				x1 = wi;
			}
			else
			{
				tickTime = subElements.get(i).getStartTime();
				x1 = (int)((tickTime - startTime) * wi / (endTime - startTime));
			}

			g.setColor(new Color(i == 0 ? mElement.getColor() : subElements.get(i - 1).getColor()));
			g.fillRect(mLabelWidth + x0, y + (mRowHeight - mBarHeight) / 2, x1 - x0, mBarHeight);

			g.setColor(Color.BLACK);
			g.setFont(mTimeFont);
			g.drawString(formatTime(tickTime - lastTime), mLabelWidth + x1 + 5, y + 15);

			g.setColor(Color.BLACK);
			g.setFont(mLabelFont);
			g.drawString(i == 0 ? mElement.getDescription() : subElements.get(i - 1).getDescription(), 0, y + 15);

			x0 = x1;
			lastTime = tickTime;
			y += mRowHeight;
		}
	}


	@Override
	public Dimension preferredSize()
	{
		return new Dimension(mLabelWidth + 50 + mRightMargin, mElement == null ? 1 : mRowHeight * mElement.getSubElements().size());
	}
}

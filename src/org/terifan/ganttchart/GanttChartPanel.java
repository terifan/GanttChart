package org.terifan.ganttchart;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.swing.JPanel;


public class GanttChartPanel extends JPanel
{
	private final static long serialVersionUID = 1L;
	private final GanttChart mChart;

	final static int[] C =
	{
		0xEF7586, 0xE599FF, 0xE08453, 0x7F9FBF, 0x56C83F
	};

	private final static Color ROW_LIGHT = new Color(255, 255, 255);
	private final static Color ROW_DARK = new Color(242, 242, 242);
	private final static int MINIMUM_WIDTH = 50;

	private int mRowHeight = 24;
	private int mBarHeight = 9;
	private int mLabelWidth = 200;
	private int mRightMargin = 50;
	private Font mLabelFont = new Font("segoe ui", Font.PLAIN, 12);
	private Font mTimeFont = new Font("segoe ui", Font.PLAIN, 9);
	private boolean mRequestFocusOnDisplay;

	private final GanttChartDetailPanel mDetailPanel;
	private GanttChartElement mSelectedElement;


	public GanttChartPanel(GanttChart aGanttChart, GanttChartDetailPanel aDetailPanel)
	{
		mChart = aGanttChart;

		mDetailPanel = aDetailPanel;
		mChart.mPanel = this;
		mRequestFocusOnDisplay = true;

		MouseAdapter ma = new MouseAdapter()
		{
			@Override
			public void mouseDragged(MouseEvent aEvent)
			{
				setSelectedElementIndex(aEvent.getY() / mRowHeight);
			}

			@Override
			public void mousePressed(MouseEvent aEvent)
			{
				requestFocus();
				setSelectedElementIndex(aEvent.getY() / mRowHeight);
			}
		};

		addMouseListener(ma);
		addMouseMotionListener(ma);

		addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent aEvent)
			{
				switch (aEvent.getKeyCode())
				{
					case KeyEvent.VK_UP:
						setSelectedElementIndex(Math.max(getSelectedElementIndex() - 1, 0));
						break;
					case KeyEvent.VK_DOWN:
						setSelectedElementIndex(Math.min(getSelectedElementIndex() + 1, mChart.mMap.size() - 1));
						break;
					case KeyEvent.VK_END:
						setSelectedElementIndex(Math.max(mChart.mMap.size() - 1, 0));
						break;
					case KeyEvent.VK_HOME:
						setSelectedElementIndex(0);
						break;
				}
			}
		});
	}


	public GanttChartElement getSelectedElement()
	{
		return mSelectedElement;
	}


	public int getSelectedElementIndex()
	{
		TreeMap<Long, GanttChartElement> map = mChart.mMap;

		int i = 0;
		for (GanttChartElement element : map.values())
		{
			if (element == mSelectedElement)
			{
				return i;
			}
			i++;
		}

		return -1;
	}


	public void setSelectedElementIndex(int aIndex)
	{
		TreeMap<Long, GanttChartElement> map = mChart.mMap;

		if (aIndex < 0 || aIndex >= map.size())
		{
			return;
		}

		mSelectedElement = map.get(map.keySet().toArray(new Long[map.size()])[aIndex]);

		if (mDetailPanel != null)
		{
			mDetailPanel.setElement(mSelectedElement);
			mDetailPanel.repaint();
		}

		repaint();
	}


	@Override
	protected void paintComponent(Graphics aGraphics)
	{
		Graphics2D g = (Graphics2D)aGraphics;

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		TreeMap<Long, GanttChartElement> map = mChart.mMap;

		int w = Math.max(getWidth(), mLabelWidth + mRightMargin + MINIMUM_WIDTH);
		int h = getHeight();

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, w, getHeight());

		for (int i = 0, y = 0; y < h; i++, y += mRowHeight)
		{
			g.setColor((i & 1) == 0 ? ROW_LIGHT : ROW_DARK);
			g.fillRect(0, y, w, mRowHeight);
		}

		if (map.isEmpty())
		{
			return;
		}

		long start = mChart.getStartTime();
		long end = mChart.getEndTime();

		int wi = w - mLabelWidth - mRightMargin;
		int y = 0;

		for (GanttChartElement element : map.values())
		{
			drawElement(g, element, y, w, start, end, wi);

			y += mRowHeight;
		}

		drawGrid(g, wi, h);

		if (mRequestFocusOnDisplay)
		{
			requestFocus();
			mRequestFocusOnDisplay = false;
		}
	}


	private void drawGrid(Graphics aGraphics, int aContentWidth, int aHeight)
	{
		for (int col = 0; col <= 10; col++)
		{
			int x = mLabelWidth + col * aContentWidth / 10;
			aGraphics.setColor(new Color(0.6f, 0.6f, 0.6f, 0.2f));
			aGraphics.drawLine(x, 0, x, aHeight);

			if (col < 10)
			{
				x += aContentWidth / 10 / 2;
				aGraphics.setColor(new Color(0.8f, 0.8f, 0.8f, 0.2f));
				aGraphics.drawLine(x, 0, x, aHeight);
			}
		}
	}


	private void drawElement(Graphics aGraphics, GanttChartElement aElement, int aY, int aWidth, long aStartTime, long aEndTime, int aContentWidth)
	{
		if (mSelectedElement == aElement)
		{
			aGraphics.setColor(new Color(0, 116, 232));
			aGraphics.fillRect(0, aY, aWidth, mRowHeight);
		}

		long startTime = aElement.getStartTime();
		long endTime = aElement.getEndTime();

		aGraphics.setColor(new Color(aElement.getColor()));

		for (int i = 0, sz = aElement.getSubCount(); i < sz; i++)
		{
			int x0 = mLabelWidth + (int)((aElement.getSubTime(i + 0) - aStartTime) * aContentWidth / (aEndTime - aStartTime));
			int x1 = mLabelWidth + (int)((aElement.getSubTime(i + 1) - aStartTime) * aContentWidth / (aEndTime - aStartTime));

			aGraphics.setColor(new Color(aElement.getSubElement(i).getColor()));

			if (aElement.isRunning() && i == sz - 1)
			{
				aGraphics.drawRect(x0, aY + (mRowHeight - mBarHeight) / 2, x1 - x0, mBarHeight);
			}
			else
			{
				aGraphics.fillRect(x0, aY + (mRowHeight - mBarHeight) / 2, x1 - x0 + 1, mBarHeight + 1);
			}
		}

		int x1 = mLabelWidth + (int)((endTime - aStartTime) * aContentWidth / (aEndTime - aStartTime));

		aGraphics.setColor(mSelectedElement == aElement ? Color.WHITE : Color.BLACK);
		aGraphics.setFont(mTimeFont);
		aGraphics.drawString(formatTime(endTime - startTime), x1 + 5, aY + 15);

		aGraphics.setColor(mSelectedElement == aElement ? Color.WHITE : Color.BLACK);
		aGraphics.setFont(mLabelFont);
		aGraphics.drawString(aElement.getDescription(), 0, aY + 15);
	}


	@Override
	public Dimension preferredSize()
	{
		return new Dimension(mLabelWidth + MINIMUM_WIDTH + mRightMargin, mRowHeight * mChart.mMap.size());
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

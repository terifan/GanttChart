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
import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JPanel;


public class GanttChartPanel extends JPanel
{
	private final static long serialVersionUID = 1L;
	private final GanttChart mChart;

	private final static Color ROW_LIGHT = new Color(255, 255, 255);
	private final static Color ROW_DARK = new Color(242, 242, 242);
	private final static int MINIMUM_WIDTH = 50;

	private int mRowHeight = 24;
	private int mBarHeight = 9;
	private int mLabelWidth = 200;
	private int mRightMargin = 50;
	private Font mLabelFont = new Font("segoe ui", Font.PLAIN, 12);
	private Font mTimeFont = new Font("segoe ui", Font.PLAIN, 9);

	private GanttChartDetailPanel mDetailPanel;
	private GanttElement mSelectedElement;
	private boolean mRequestFocusOnDisplay;


	public GanttChartPanel(GanttChart aChart, GanttChartDetailPanel aDetailPanel)
	{
		mChart = aChart;

		mDetailPanel = aDetailPanel;
		mChart.mPanel = this;
		mRequestFocusOnDisplay = true;

		MouseAdapter ma = new MouseAdapter()
		{
			@Override
			public void mouseDragged(MouseEvent aEvent)
			{
				setSelectedRowIndex(aEvent.getY() / mRowHeight);
			}


			@Override
			public void mousePressed(MouseEvent aEvent)
			{
				requestFocus();
				setSelectedRowIndex(aEvent.getY() / mRowHeight);
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
						setSelectedRowIndex(Math.max(getSelectedRowIndex() - 1, 0));
						break;
					case KeyEvent.VK_DOWN:
						setSelectedRowIndex(Math.min(getSelectedRowIndex() + 1, getElementCount() - 1));
						break;
					case KeyEvent.VK_END:
						setSelectedRowIndex(Math.max(getElementCount() - 1, 0));
						break;
					case KeyEvent.VK_HOME:
						setSelectedRowIndex(0);
						break;
				}
			}
		});
	}


	public GanttElement getSelectedElement()
	{
		return mSelectedElement;
	}


	public int getSelectedRowIndex()
	{
		Object o = forEach((r, e) -> e == mSelectedElement ? r : null);

		return o == null ? -1 : (Integer)o;
	}


	public void setSelectedRowIndex(int aRowIndex)
	{
		if (aRowIndex < 0)
		{
			return;
		}

		GanttElement o = getElement(aRowIndex);

		if (o != null)
		{
			mSelectedElement = o;

			if (mDetailPanel != null)
			{
				mDetailPanel.setSelectedElement(mSelectedElement);
				mDetailPanel.repaint();
			}

			repaint();
		}
	}


	public GanttElement getElement(int aRowIndex)
	{
		return (GanttElement)forEach((r, e) -> r == aRowIndex ? e : null);
	}


	public int getElementCount()
	{
		AtomicInteger counter = new AtomicInteger();

		forEach((r, e) ->
		{
			counter.set(r + 1);
			return null;
		});

		return counter.get();
	}


	public Object forEach(Visitor aVisitor)
	{
		ArrayDeque<GanttElement> stack = new ArrayDeque<>();
		stack.add(mChart);

		int row = -1;

		while (!stack.isEmpty())
		{
			GanttElement element = stack.removeFirst();

			Object o = aVisitor.visit(row, element);

			if (o != null)
			{
				return o;
			}

			row++;

			for (int j = element.mElements.size(); --j >= 0;)
			{
				stack.addFirst(element.mElements.get(j));
			}
		}

		return null;
	}


	public interface Visitor
	{
		Object visit(int aRow, GanttElement aElement);
	}


	@Override
	protected void paintComponent(Graphics aGraphics)
	{
		Graphics2D g = (Graphics2D)aGraphics;

		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int w = Math.max(getWidth(), mLabelWidth + mRightMargin + MINIMUM_WIDTH);
		int h = getHeight();

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, w, getHeight());

		for (int i = 0, y = 0; y < h; i++, y += mRowHeight)
		{
			g.setColor((i & 1) == 0 ? ROW_LIGHT : ROW_DARK);
			g.fillRect(0, y, w, mRowHeight);
		}

		long start = mChart.getStartTime();
		long end = mChart.getEndTime();

		int wi = w - mLabelWidth - mRightMargin;
		int y = 0;

		drawElements(g, mChart, y, w, start, end, wi);

		drawGrid(g, wi, h);

		if (mRequestFocusOnDisplay)
		{
			requestFocus();
			mRequestFocusOnDisplay = false;
		}
	}


	private int drawElements(Graphics2D aGraphics, GanttElement aElement, int aY, int aWidth, long aStartTime, long aEndTime, int aContentWidth)
	{
		for (int i = 0, sz = aElement.size(); i < sz; i++)
		{
			aY = drawElement(aGraphics, aElement.get(i), aY, aWidth, aStartTime, aEndTime, aContentWidth);

			aY = drawElements(aGraphics, aElement.get(i), aY, aWidth, aStartTime, aEndTime, aContentWidth);
		}

		return aY;
	}


	private int drawElement(Graphics2D aGraphics, GanttElement aElement, int aY, int aWidth, long aStartTime, long aEndTime, int aContentWidth)
	{
		if (mSelectedElement == aElement)
		{
			aGraphics.setColor(new Color(0, 116, 232));
			aGraphics.fillRect(0, aY, aWidth, mRowHeight);
		}

		long startTime = aElement.getStartTime();
		long endTime = aElement.getEndTime();

		for (int i = 0, sz = aElement.getSegmentCount(); i < sz; i++)
		{
			GanttSegment segment = aElement.getSegment(i);

			int x0 = mLabelWidth + (int)((segment.getStartTime() - aStartTime) * aContentWidth / (aEndTime - aStartTime));
			int x1 = mLabelWidth + (int)((segment.getEndTime() - aStartTime) * aContentWidth / (aEndTime - aStartTime));

			aGraphics.setColor(segment.getColor());

			if (segment.isRunning() && i == sz - 1)
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
		aGraphics.drawString(formatTime(endTime - startTime), x1 + 5, aY + mRowHeight / 2 + aGraphics.getFontMetrics().getDescent());

		aGraphics.setColor(mSelectedElement == aElement ? Color.WHITE : Color.BLACK);
		aGraphics.setFont(mLabelFont);
		aGraphics.drawString(aElement.getSegment(0).getDescription(), 2, aY + mRowHeight / 2 + aGraphics.getFontMetrics().getDescent());

		return aY + mRowHeight;
	}


	private void drawGrid(Graphics2D aGraphics, int aContentWidth, int aHeight)
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


	@Override
	public Dimension preferredSize()
	{
		return new Dimension(mLabelWidth + MINIMUM_WIDTH + mRightMargin, mRowHeight * mChart.size());
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

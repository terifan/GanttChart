package org.terifan.ganttchart.rev2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import static org.terifan.ganttchart.rev2.StyleSheet.COLORS;
import static org.terifan.ganttchart.rev2.StyleSheet.SELECTION_COLOR;
import static org.terifan.ganttchart.rev2.StyleSheet.SELECTION_OUTLINE_COLOR;
import static org.terifan.ganttchart.rev2.StyleSheet.TEXT_COLOR_SELECTED;
import static org.terifan.ganttchart.rev2.StyleSheet.mAnimationRate;
import static org.terifan.ganttchart.rev2.StyleSheet.mAnimationSteps;
import static org.terifan.ganttchart.rev2.StyleSheet.mGroupColor;
import static org.terifan.ganttchart.rev2.StyleSheet.mIconDetail;
import static org.terifan.ganttchart.rev2.StyleSheet.mIconFinished;
import static org.terifan.ganttchart.rev2.StyleSheet.mIconSpinner;
import static org.terifan.ganttchart.rev2.StyleSheet.mIconStatus;
import static org.terifan.ganttchart.rev2.StyleSheet.mRowColors;
import static org.terifan.ganttchart.rev2.StyleSheet.mRowOutlineColors;
import org.terifan.ganttchart.rev2.TextBox.Anchor;
import org.terifan.ganttchart.rev2.Work.Status;
import static org.terifan.ganttchart.rev2.StyleSheet.DIVIDER_COLOR;


public class WorkVisuals
{
	private final static int TREE_ICON_SIZE = 16;

	private Work mWork;
	private LayoutInfo[] mLayout;
	private int mLabelWidth;
	private int mSingeLineHeight;
	private int mRightMarginWidth;


	public WorkVisuals(Work aWork)
	{
		mWork = aWork;

		mRightMarginWidth = 100;
		mLabelWidth = 250;
		mSingeLineHeight = 24;
	}


	public Work getWork()
	{
		return mWork;
	}


	public void layout()
	{
		ArrayList<LayoutInfo> layout = new ArrayList<>();
		int y = layoutRow(layout, mWork, 0, "");
		layout(layout, mWork, y, "");
		mLayout = layout.toArray(LayoutInfo[]::new);
	}


	public int getLayoutHeight()
	{
		LayoutInfo[] tmp = mLayout;
		return tmp == null ? 0 : tmp[tmp.length - 1].y1;
	}


	private int layout(ArrayList<LayoutInfo> aLayout, Work aWork, int aY, String aIndent)
	{
		if (aWork.getChildren() != null)
		{
			Work[] children = aWork.getChildren().toArray(Work[]::new);

			for (int i = 0, last = children.length - 1; i <= last; i++)
			{
				aY = layoutRow(aLayout, children[i], aY, aIndent + (i == last ? "." : "+"));
				aY = layout(aLayout, children[i], aY, aIndent + (i == last ? " " : "|"));
			}
		}

		return aY;
	}


	private int layoutRow(ArrayList<LayoutInfo> aLayout, Work aWork, int aY, String aIndent)
	{
		int rowHeight = Math.max(new TextBox().setMaxLineCount(10).setBreakChars(new char[0]).setHeight(1000).setText(aWork.getLabel()).measure().height, mSingeLineHeight);
		aLayout.add(new LayoutInfo(aLayout.size(), aY, rowHeight, aWork, aIndent));
		return aY + rowHeight;
	}


	public void paint(Graphics2D g, int aWidth, Long aSelectedWork)
	{
		TextBox tb = new TextBox().setAnchor(Anchor.WEST).setMaxLineCount(10).setBreakChars(new char[0]);

		int pw = Math.max(mLabelWidth + mRightMarginWidth + mRightMarginWidth, aWidth);

		long currentTime = System.currentTimeMillis();
		long minStartTime = mWork.getStartTime();
		long maxEndTime = getMaxEndTime(mWork);

		if (mWork.getEndTime() == 0)
		{
			maxEndTime = currentTime;
		}

		for (LayoutInfo row : mLayout)
		{
			Work work = row.work;

			if (g.hitClip(0, row.y0, pw, row.height))
			{
				g.translate(0, row.y0);

				long timeRange = maxEndTime - minStartTime;
				int barMaxWidth = pw - mLabelWidth - 10 - mRightMarginWidth;

				Long workId = work.getId();
				Color rowColor = workId.equals(aSelectedWork) ? SELECTION_COLOR : mRowColors[row.index & 1];
				Color rowOutlineColor = workId.equals(aSelectedWork) ? SELECTION_OUTLINE_COLOR : mRowOutlineColors[row.index & 1];
				Color foreground = workId.equals(aSelectedWork) ? TEXT_COLOR_SELECTED : StyleSheet.FOREGROUND;

				int ix = 4;
				int iy = (mSingeLineHeight - TREE_ICON_SIZE) / 2;
				int cy = mSingeLineHeight / 2;

				g.setColor(rowColor);
				g.fillRect(0, 0, pw, row.height);

				ix += TREE_ICON_SIZE / 2 - 1;

				for (int i = 0; i < row.indent.length(); i++, ix += TREE_ICON_SIZE)
				{
					switch (row.indent.charAt(i))
					{
						case '|':
							drawDottedLine(g, ix, 0, 0, row.height, StyleSheet.TREE_COLOR);
							break;
						case '+':
							drawDottedLine(g, ix, 0, 0, row.height, StyleSheet.TREE_COLOR);
							drawDottedLine(g, ix, 0 + cy, TREE_ICON_SIZE - 2, 0, StyleSheet.TREE_COLOR);
							break;
						case '.':
							drawDottedLine(g, ix, 0, 0, cy, StyleSheet.TREE_COLOR);
							drawDottedLine(g, ix, cy, TREE_ICON_SIZE - 2, 0, StyleSheet.TREE_COLOR);
							break;
					}
				}

				ix -= TREE_ICON_SIZE / 2 - 1;

				if (work.isDetail())
				{
					g.drawImage(mIconDetail, ix, iy, null);
				}
				else if (work.getStartTime() == 0)
				{
					g.drawImage(mIconStatus[Status.PENDING.ordinal()], ix, iy, null);
				}
				else if (work.getStatus() == Status.ABORTED || work.getStatus() == Status.FAILED || work.getStatus() == Status.SUCCESS || work.getStatus() == Status.FINISH)
				{
					g.drawImage(mIconStatus[work.getStatus().ordinal()], ix, iy, null);
				}
				else if (work.getEndTime() != 0)
				{
					g.drawImage(mIconFinished, ix, iy, null);
				}
				else
				{
					AffineTransform tx = g.getTransform();
					g.translate(ix, iy);
					g.rotate(Math.toRadians((mAnimationSteps * (currentTime / mAnimationRate / mAnimationSteps)) % 360), TREE_ICON_SIZE / 2, TREE_ICON_SIZE / 2);
					g.drawImage(mIconSpinner, 0, 0, null);
					g.setTransform(tx);
				}

				ix += TREE_ICON_SIZE;

				if (work.getValue() != null && !work.getValue().isEmpty())
				{
					tb.setWidth(pw - 10 - ix);
					tb.setText(work.getLabel() + " -- " + work.getValue());
				}
				else if (work.isDetail())
				{
					tb.setWidth(pw - 10 - ix);
					tb.setText(work.getLabel());
				}
				else
				{
					tb.setWidth(mLabelWidth - 10 - ix - 2);
					tb.setText(work.getLabel());
				}

				tb.setForeground(foreground);
				tb.setHeight(row.height);
				tb.setX(ix + 2);
				tb.render(g);

				if (!(work.getValue() != null && !work.getValue().isEmpty() || work.isDetail()))
				{
					g.setColor(DIVIDER_COLOR);
					g.drawLine(mLabelWidth - 5, 0, mLabelWidth - 5, row.height);

					if (work.getStartTime() > 0)
					{
						long startTime = work.getStartTime() - minStartTime;
						long endTime = Math.max(startTime, (work.getEndTime() == 0 ? maxEndTime : work.getEndTime()) - minStartTime);

						if (work.getChildren() != null && timeRange > 0)
						{
							long selfTime = endTime - startTime;
							boolean onlyDetails = true;

							Work[] children = work.getChildren().toArray(Work[]::new);

							long midTime = 0;
							for (Work w : children)
							{
								midTime = Math.max(midTime, Math.max(startTime, (w.getEndTime() == 0 ? maxEndTime : w.getEndTime()) - minStartTime));
							}

							int selfX0 = mLabelWidth + (int)(startTime * barMaxWidth / timeRange);
							int selfX1 = mLabelWidth + (int)(midTime * barMaxWidth / timeRange);
							int selfX2 = mLabelWidth + (int)(endTime * barMaxWidth / timeRange);

							if (selfX0 != selfX2)
							{
								g.setColor(COLORS[work.getColor()]);
								g.fillRect(selfX0, cy - 4, selfX2 - selfX0, 9);
							}

							if (selfX1 > selfX2)
							{
								g.setColor(mGroupColor);
								g.drawLine(selfX1, cy - 1, selfX2, cy - 1);
								g.drawLine(selfX1, cy, selfX2, cy);
							}

							int labelOffset = Math.max(0, Math.max(selfX0, Math.max(selfX0, selfX2)));

							for (Work child : children)
							{
								if (child.getStartTime() > 0)
								{
									long childStartTime = child.getStartTime() - minStartTime;
									long childEndTime = (child.getEndTime() == 0 ? maxEndTime : child.getEndTime()) - minStartTime;

									int childX0 = mLabelWidth + (int)(childStartTime * barMaxWidth / timeRange);
									int childX1 = mLabelWidth + (int)(childEndTime * barMaxWidth / timeRange);

									drawDottedRect(g, childX0, cy - 4, childX1 - childX0, 9, rowColor, rowOutlineColor);

									labelOffset = Math.max(labelOffset, Math.max(childX0, childX1));

									if (child.getEndTime() > 0 && child.getEndTime() < endTime)
									{
										selfTime -= child.getEndTime() - child.getStartTime();
									}

									onlyDetails &= child.isDetail();
								}
							}

							int boxX1 = labelOffset;
							int boxX2 = mLabelWidth + (int)((maxEndTime - minStartTime) * barMaxWidth / timeRange);

							if (boxX2 > boxX1 && getMaxEndTime(work) == maxEndTime)
							{
								g.setColor(mGroupColor);
								g.drawLine(boxX1, cy - 1, boxX2, cy - 1);
								g.drawLine(boxX1, cy, boxX2, cy);

								labelOffset = boxX2;
							}

							long durTime = maxEndTime - work.getStartTime();
							String dur = formatTime(durTime);
							String slf = formatTime(selfTime);

							tb.setX(labelOffset + 5);
							tb.setWidth(pw - labelOffset - 5);
							tb.setText(onlyDetails ? dur : selfTime <= 0 ? "Σ" + dur : selfTime >= durTime ? "Σ" + slf : "Ø" + slf + " / Σ" + dur);
							tb.render(g);
						}
						else if (timeRange > 0)
						{
							int boxX0 = mLabelWidth + (int)(startTime * barMaxWidth / timeRange);
							int boxX1 = mLabelWidth + (int)(endTime * barMaxWidth / timeRange);

							g.setColor(COLORS[work.getColor()]);
							g.fillRect(boxX0, cy - 4, boxX1 - boxX0 + 1, 9);

							tb.setX(boxX1 + 5).setWidth(pw - boxX1 - 5);
							tb.setText(formatTime(endTime - startTime));
							tb.render(g);
						}
					}
				}

				g.setColor(DIVIDER_COLOR);
				g.drawLine(0, row.height - 1, pw, row.height - 1);

				g.translate(0, -row.y0);
			}
		}
	}


	private void drawDottedRect(Graphics2D g, int aX, int aY, int aWidth, int aHeight, Color aRowColor, Color aLineColor)
	{
		if (aRowColor != null)
		{
			g.setColor(aRowColor);
			g.fillRect(aX, aY, aWidth, aHeight);
		}

		g.setColor(aLineColor);
		for (int x = 0, x1 = aX, y1 = aY + aHeight - 1; x < aWidth; x++, x1++)
		{
			drawDot(g, x1, aY);
			drawDot(g, x1, y1);
		}
		for (int y = 0, y1 = aY + y, x1 = aX + aWidth - 1; y < aHeight; y++, y1++)
		{
			drawDot(g, aX, y1);
			drawDot(g, x1, y1);
		}
	}


	private void drawDottedLine(Graphics2D g, int aX, int aY, int aWidth, int aHeight, Color aLineColor)
	{
		g.setColor(aLineColor);

		if (aWidth > 0)
		{
			for (int x = 0, x1 = aX; x < aWidth; x++, x1++)
			{
				drawDot(g, x1, aY);
			}
		}
		else if (aHeight > 0)
		{
			for (int y = 0, y1 = aY + y; y < aHeight; y++, y1++)
			{
				drawDot(g, aX, y1);
			}
		}
	}


	private void drawDot(Graphics2D aGraphics, int aX, int aY)
	{
		if (((aX ^ aY) & 1) == 1)
		{
			aGraphics.drawLine(aX, aY, aX, aY);
		}
	}


	private long getMaxEndTime(Work aWork)
	{
		long endTime = 0;
		if (aWork.getChildren() != null)
		{
			for (Work work : aWork.getChildren())
			{
				endTime = Math.max(endTime, work.getEndTime());
			}
		}
		return endTime;
	}


	private long getMinStartTime(Work aWork)
	{
		long startTime = Long.MAX_VALUE;
		if (aWork.getChildren() != null)
		{
			for (Work work : aWork.getChildren())
			{
				if (work.getStartTime() > 0 && work.getStartTime() < startTime)
				{
					startTime = work.getStartTime();
				}
			}
		}
		return startTime;
	}


	public static enum AbortOption
	{
		ABORT,
		CONTINUE
	}


	private static String formatTime(long aTime)
	{
		if (aTime < 1000)
		{
			return aTime + "ms";
		}

		return String.format("%.1f", aTime / 1000.0) + "s";
	}


	private static class LayoutInfo
	{
		int index;
		int y0;
		int y1;
		int height;
		Work work;
		String indent;


		public LayoutInfo(int aIndex, int aY, int aHeight, Work aWork, String aIndent)
		{
			index = aIndex;
			y0 = aY;
			y1 = aY + aHeight;
			height = aHeight;
			work = aWork;
			indent = aIndent;
		}
	}
}

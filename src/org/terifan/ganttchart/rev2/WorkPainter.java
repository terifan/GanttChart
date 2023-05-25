package org.terifan.ganttchart.rev2;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import org.terifan.ganttchart.rev2.TextBox.Anchor;
import org.terifan.ganttchart.rev2.Work.Status;
import static org.terifan.ganttchart.rev2.StyleSheet.COLORS;
import static org.terifan.ganttchart.rev2.StyleSheet.mAnimationRate;
import static org.terifan.ganttchart.rev2.StyleSheet.mAnimationSteps;
import static org.terifan.ganttchart.rev2.StyleSheet.mIconDetail;
import static org.terifan.ganttchart.rev2.StyleSheet.mIconStatus;
import static org.terifan.ganttchart.rev2.StyleSheet.DIVIDER_COLOR;
import static org.terifan.ganttchart.rev2.StyleSheet.LABEL_FONT;
import static org.terifan.ganttchart.rev2.StyleSheet.TIME_COLOR;
import static org.terifan.ganttchart.rev2.StyleSheet.TIME_FONT;
import static org.terifan.ganttchart.rev2.StyleSheet.TREE_COLOR;
import static org.terifan.ganttchart.rev2.StyleSheet.mRowBackgroundColors;
import static org.terifan.ganttchart.rev2.StyleSheet.LABEL_FOREGROUND_SELECTED;
import static org.terifan.ganttchart.rev2.StyleSheet.LABEL_FOREGROUND;
import static org.terifan.ganttchart.rev2.StyleSheet.ROW_BACKGROUND_SELECTED;
import static org.terifan.ganttchart.rev2.StyleSheet.ROW_OUTLINE_SELECTED;
import static org.terifan.ganttchart.rev2.StyleSheet.STAGE_COLORS;
import static org.terifan.ganttchart.rev2.StyleSheet.mLabelWidth;
import static org.terifan.ganttchart.rev2.StyleSheet.mRightMarginWidth;
import static org.terifan.ganttchart.rev2.StyleSheet.mRowOutlineColors;
import static org.terifan.ganttchart.rev2.StyleSheet.mSingeLineHeight;


public class WorkPainter
{
	private final static int TREE_ICON_SIZE = 16;

	private Work mWork;
	private LayoutInfo[] mLayout;


	public WorkPainter(Work aWork)
	{
		mWork = aWork;
	}


	public Work getWork()
	{
		return mWork;
	}


	public void layout()
	{
		ArrayList<LayoutInfo> newLayout = new ArrayList<>();
		int y = layoutRow(newLayout, mWork, 0, "");
		layout(newLayout, mWork, y, "");
		mLayout = newLayout.toArray(LayoutInfo[]::new);
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
				if (!children[i].isStage())
				{
					aY = layoutRow(aLayout, children[i], aY, aIndent + (i == last ? "." : "+"));
					aY = layout(aLayout, children[i], aY, aIndent + (i == last ? " " : "|"));
				}
			}
		}

		return aY;
	}


	private int layoutRow(ArrayList<LayoutInfo> aLayout, Work aWork, int aY, String aIndent)
	{
		int rowHeight = Math.max(new TextBox().setMaxLineCount(10).setBreakChars(new char[0]).setHeight(1000).setText(aWork.getLabel()).setFont(LABEL_FONT).measure().height, mSingeLineHeight);
		aLayout.add(new LayoutInfo(aLayout.size(), aY, rowHeight, aWork, aIndent));
		return aY + rowHeight;
	}


	public void paint(Graphics2D g, int aWidth, Long aSelectedWork)
	{
		TextBox tb = new TextBox().setAnchor(Anchor.WEST).setMaxLineCount(10).setBreakChars(new char[0]);

		int pw = Math.max(mLabelWidth + mRightMarginWidth + mRightMarginWidth, aWidth);

		long currentTime = System.currentTimeMillis();
		long minStartTime = mWork.getMinStartTime();
		long timeRange = (mWork.getEndTime() == 0 ? currentTime : mWork.getEndTime()) - minStartTime;

		for (LayoutInfo row : mLayout)
		{
			Work work = row.work;

			if (g.hitClip(0, row.y0, pw, row.height))
			{
				g.translate(0, row.y0);

				boolean selected = aSelectedWork != null && aSelectedWork.equals(work.getId());

				int ix = 3 + TREE_ICON_SIZE / 2;
				int iy = (mSingeLineHeight - TREE_ICON_SIZE) / 2;
				int cy = mSingeLineHeight / 2;

				g.setColor(selected ? ROW_BACKGROUND_SELECTED : mRowBackgroundColors[row.index & 1]);
				g.fillRect(0, 0, pw, row.height);

				for (int i = 0; i < row.indent.length(); i++, ix += TREE_ICON_SIZE)
				{
					switch (row.indent.charAt(i))
					{
						case '|':
							drawDottedLine(g, TREE_COLOR, ix, 0, 0, row.height);
							break;
						case '+':
							drawDottedLine(g, TREE_COLOR, ix, 0, 0, row.height);
							drawDottedLine(g, TREE_COLOR, ix, cy, TREE_ICON_SIZE - 2, 0);
							break;
						case '.':
							drawDottedLine(g, TREE_COLOR, ix, 0, 0, cy);
							drawDottedLine(g, TREE_COLOR, ix, cy, TREE_ICON_SIZE - 2, 0);
							break;
					}
				}

				ix -= TREE_ICON_SIZE / 2 - 1;

				if (work.isDetail())
				{
					g.drawImage(mIconDetail, ix, iy, null);
				}
				else if (work.getStatus() == Status.RUNNING)
				{
					AffineTransform tx = g.getTransform();
					g.translate(ix, iy);
					g.rotate(Math.toRadians((mAnimationSteps * (currentTime / mAnimationRate / mAnimationSteps)) % 360), TREE_ICON_SIZE / 2, TREE_ICON_SIZE / 2);
					g.drawImage(mIconStatus.get(work.getStatus()), 0, 0, null);
					g.setTransform(tx);
				}
				else
				{
					g.drawImage(mIconStatus.get(work.getStatus()), ix, iy, null);
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

				tb.setX(ix + 2);
				tb.setHeight(row.height);
				tb.setForeground(selected ? LABEL_FOREGROUND_SELECTED : LABEL_FOREGROUND);
				tb.setFont(LABEL_FONT);
				tb.render(g);

				if (!(work.getValue() != null && !work.getValue().isEmpty() || work.isDetail()))
				{
					g.setColor(DIVIDER_COLOR);
					g.drawLine(mLabelWidth - 5, 0, mLabelWidth - 5, row.height);

					if (work.getStartTime() > 0)
					{
						long startTime = work.getStartTime();
						long endTime = Math.min(currentTime, work.getEndTime() == 0 ? currentTime : work.getEndTime());

						int barMaxWidth = pw - mLabelWidth - 10 - mRightMarginWidth;
						int x0 = mLabelWidth + (int)((startTime - minStartTime) * barMaxWidth / timeRange);
						int x1 = mLabelWidth + (int)((endTime - minStartTime) * barMaxWidth / timeRange);

						g.setColor(COLORS[work.getColor()]);
						g.fillRect((int)Math.ceil(x0), cy - 4, Math.max((int)Math.ceil(x1 - x0), 1), 9);

						long childTime = 0;
						boolean onlyDetails = true;

						if (work.getChildren() != null)
						{
							int stageIndex = 0;
							for (Work child : work.getChildren())
							{
								if (child.getStatus() != Status.PENDING)
								{
									long childStartTime = child.getStartTime();
									long childEndTime = Math.min(child.getEndTime() == 0 ? currentTime : child.getEndTime(), endTime);

									int childX0 = mLabelWidth + (int)((childStartTime - minStartTime) * barMaxWidth / timeRange);
									int childX1 = mLabelWidth + (int)((childEndTime - minStartTime) * barMaxWidth / timeRange);

									if (child.isStage())
									{
										g.setColor(child.getColor() != 255 ? COLORS[child.getColor()] : STAGE_COLORS[stageIndex++ % STAGE_COLORS.length]);
										g.fillRect(childX0, cy - 4, childX1 - childX0, 9);
									}
									else
									{
										drawDottedRect(g, childX0, cy - 4, childX1 - childX0, 9, selected ? ROW_BACKGROUND_SELECTED : mRowBackgroundColors[row.index & 1], selected ? ROW_OUTLINE_SELECTED : mRowOutlineColors[row.index & 1]);
									}

									childTime += childEndTime - childStartTime;
									onlyDetails &= child.isDetail();
								}
							}
						}

						long totalTime = endTime - startTime;
						long selfTime = endTime - startTime - childTime;
						String totalText = formatTime(totalTime);
						String selfText = formatTime(selfTime);

						tb.setX(x1 + 5);
						tb.setWidth(pw - x1 - 5);
						tb.setForeground(TIME_COLOR);
						tb.setFont(TIME_FONT);
						tb.setText(onlyDetails ? totalText : selfTime <= 0 ? "Σ" + totalText : selfTime >= totalTime ? "Σ" + selfText : "Ø" + selfText + " / Σ" + totalText);
						tb.render(g);
					}
				}

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


	private void drawDottedLine(Graphics2D g, Color aLineColor, int aX, int aY, int aWidth, int aHeight)
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

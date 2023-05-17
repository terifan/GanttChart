package org.terifan.ganttchart.rev2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.BiFunction;
import javax.swing.ActionMap;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import static org.terifan.ganttchart.rev2.StyleSheet.BACKGROUND;
import static org.terifan.ganttchart.rev2.StyleSheet.COLORS;
import static org.terifan.ganttchart.rev2.StyleSheet.FOREGROUND;
import static org.terifan.ganttchart.rev2.StyleSheet.LINE_COLOR;
import static org.terifan.ganttchart.rev2.StyleSheet.LINE_COLOR_UNSELECTED;
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
import static org.terifan.ganttchart.rev2.StyleSheet.mSeparatorColor;
import static org.terifan.ganttchart.rev2.StyleSheet.mTreeIcons;
import org.terifan.ganttchart.rev2.TextBox.Anchor;
import org.terifan.ganttchart.rev2.Work.Status;


public class WorkStatusPanel extends JPanel
{
	private final static int TREE_ICON_SIZE = 16;

	private int mLabelWidth;
	private int mRowHeight;
	private int mRightMarginWidth;

	private WorkStatusModel mModel;
	private Work mSelectedWork;
	private WorkStatusPanelRepaintTimer mRepaintTimer;


	public WorkStatusPanel() throws IOException
	{
		this(new WorkStatusPanelRepaintTimer());
	}


	public WorkStatusPanel(WorkStatusPanelRepaintTimer aPanelUpdater) throws IOException
	{
		mRepaintTimer = aPanelUpdater;

		mModel = new WorkStatusModel();

		super.setForeground(FOREGROUND);
		super.setBackground(BACKGROUND);

		mRightMarginWidth = 100;
		mLabelWidth = 250;
		mRowHeight = 20; //Math.max(16 + 3, new TextBox("jg[").setBounds(0, 0, 100, 100).measure().height + 3);

		super.addKeyListener(mKeyAdapter);
		super.addMouseListener(mMouseAdapter);
		super.addMouseMotionListener(mMouseAdapter);

		requestFocusInWindow();

		mRepaintTimer.add(this);
	}


	void startRepaintTimer()
	{
		if (mRepaintTimer != null)
		{
			mRepaintTimer.startRepaintTimer();
		}
	}


	public void scrollToSelectedRow()
	{
		if (mSelectedWork != null)
		{
			Mutable<Integer> row = new Mutable<>(0);
			visit((aWork, aIndent) ->
			{
				if (mSelectedWork.equals(aWork))
				{
					scrollRectToVisible(new Rectangle(0, row.value * mRowHeight, 100, mRowHeight));
					repaint();
					return AbortOption.ABORT;
				}
				row.value++;
				return AbortOption.CONTINUE;
			});
		}
	}

	private MouseAdapter mMouseAdapter = new MouseAdapter()
	{
		@Override
		public void mouseDragged(MouseEvent aEvent)
		{
			mousePressed(aEvent);
		}


		@Override
		public void mousePressed(MouseEvent aEvent)
		{
			requestFocusInWindow();

			Mutable<Integer> row = new Mutable<>(0);

			visit((aWork, aIndent) ->
			{
				if (row.value == -1)
				{
					return AbortOption.ABORT;
				}
				if (mSelectedWork != aWork && aEvent.getY() >= row.value * mRowHeight && aEvent.getY() < (row.value + 1) * mRowHeight)
				{
					mSelectedWork = aWork;
					row.value = -1; // break outer loop
					repaint();
					return AbortOption.ABORT;
				}
				row.value++;
				return AbortOption.CONTINUE;
			});
		}
	};


	public void visit(BiFunction<Work, String, AbortOption> aVisitor)
	{
		ArrayList<Work> children = mModel.getWork().getChildren();
		if (children != null)
		{
			for (int workIndex = children.size(); --workIndex >= 0;)
			{
				if (children.get(workIndex).visit(aVisitor) == AbortOption.ABORT)
				{
					return;
				}
			}
		}
	}

	private KeyAdapter mKeyAdapter = new KeyAdapter()
	{
		@Override
		public void keyPressed(KeyEvent aEvent)
		{
			final Work sw = mSelectedWork;

			int keyCode = aEvent.getKeyCode();

			switch (keyCode)
			{
				case KeyEvent.VK_C:
					if (sw != null && aEvent.isControlDown())
					{
						copyToClipboard(DataFlavor.stringFlavor, sw.toInfoString());
					}
					break;
			}

			ArrayList<Work> children = mModel.getWork().getChildren();
			if (children == null)
			{
				return;
			}

			boolean up = keyCode == KeyEvent.VK_HOME || keyCode == KeyEvent.VK_PAGE_UP || keyCode == KeyEvent.VK_UP;

			switch (keyCode)
			{
				case KeyEvent.VK_HOME:
				case KeyEvent.VK_END:
					mSelectedWork = up ? children.get(children.size() - 1) : children.get(0);
					scrollToSelectedRow();
					break;
				case KeyEvent.VK_PAGE_UP:
				case KeyEvent.VK_PAGE_DOWN:
				{
					Mutable<Work> prev = new Mutable<>(null);
					Mutable<Integer> targetWorkIndex = new Mutable<>(0);
					for (int workIndex = children.size(); --workIndex >= 0;)
					{
						Mutable<Integer> _workIndex = new Mutable<>(0);
						visit((aWork, aIndent) ->
						{
							if (up && sw != null && sw.equals(aWork))
							{
								targetWorkIndex.value = prev.value == null ? -1 : Math.min(children.size() - 1, _workIndex.value + 1);
								return AbortOption.ABORT;
							}
							if (!up && prev.value != null && prev.value.equals(sw))
							{
								targetWorkIndex.value = Math.max(0, _workIndex.value - 1);
								return AbortOption.ABORT;
							}
							prev.value = aWork;
							return AbortOption.CONTINUE;
						});
					}
					if (targetWorkIndex.value >= 0)
					{
						mSelectedWork = children.get(targetWorkIndex.value);
						scrollToSelectedRow();
					}
					break;
				}
				case KeyEvent.VK_UP:
				case KeyEvent.VK_DOWN:
					Mutable<Work> prev = new Mutable<>(null);
					visit((aWork, aIndent) ->
					{
						if (up && sw != null && sw.equals(aWork) && prev.value != null)
						{
							mSelectedWork = prev.value;
							return AbortOption.ABORT;
						}
						if (!up && prev.value != null && prev.value.equals(sw))
						{
							mSelectedWork = aWork;
							return AbortOption.ABORT;
						}
						prev.value = aWork;
						return AbortOption.CONTINUE;
					});
					scrollToSelectedRow();
					break;
			}
		}
	};


	public WorkStatusModel getModel()
	{
		return mModel;
	}


	public WorkStatusPanel setModel(WorkStatusModel aModel)
	{
		if (mModel != null)
		{
			mModel.setPanel(null);
		}
		mModel = aModel;
		mModel.setPanel(this);

		if (mRepaintTimer != null)
		{
			mRepaintTimer.startRepaintTimer();
		}

		return this;
	}


	@Override
	public void addNotify()
	{
		super.addNotify();
		configureEnclosingScrollPane();
	}


	private void configureEnclosingScrollPane()
	{
		JScrollPane scrollPane = (JScrollPane)SwingUtilities.getAncestorOfClass(JScrollPane.class, this);

		if (scrollPane != null)
		{
			JScrollBar vsb = scrollPane.getVerticalScrollBar();
			vsb.setUnitIncrement(mRowHeight);
			vsb.setBlockIncrement(10 * mRowHeight);

			scrollPane.setActionMap(new ActionMap());
		}
	}


	@Override
	protected void paintComponent(Graphics aGraphics)
	{
		TextBox tb = new TextBox().setAnchor(Anchor.WEST).setMaxLineCount(1).setHeight(mRowHeight).setBreakChars(new char[0]);

		int pw = Math.max(mLabelWidth + mRightMarginWidth + mRightMarginWidth, getWidth());
		int ph = getHeight();

		Graphics2D g = (Graphics2D)aGraphics;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(BACKGROUND);
		g.fillRect(0, 0, pw, ph);
		g.setColor(FOREGROUND);

		long currentTime = System.currentTimeMillis();

		Mutable<Integer> rowIndex = new Mutable<>(0);

		ArrayList<Work> children = mModel.getWork().getChildren();
		if (children != null)
		{
			for (int workIndex = children.size(); --workIndex >= 0;)
			{
				Work work = children.get(workIndex);

				Mutable<Long> minStartTime = new Mutable<>(Long.MAX_VALUE);
				Mutable<Long> maxEndTime = new Mutable<>(0L);

				work.visit((aWork, aIndent) ->
				{
					if (aWork.getStartTime() > 0)
					{
						minStartTime.value = Math.min(minStartTime.value, aWork.getStartTime());
					}
					maxEndTime.value = Math.max(maxEndTime.value, aWork.getEndTime());
					return AbortOption.CONTINUE;
				});

				if (work.getEndTime() == 0)
				{
					maxEndTime.value = currentTime;
				}

				long timeRange = maxEndTime.value - minStartTime.value;
				int barMaxWidth = pw - mLabelWidth - 10 - mRightMarginWidth;

				work.visit((aWork, aIndent) ->
				{
					if (g.hitClip(0, tb.getY(), pw, tb.getY() + mRowHeight))
					{
						Color rowColor = aWork.equals(mSelectedWork) ? SELECTION_COLOR : mRowColors[rowIndex.value & 1];
						Color rowOutlineColor = aWork.equals(mSelectedWork) ? SELECTION_OUTLINE_COLOR : mRowOutlineColors[rowIndex.value & 1];

						tb.setForeground(aWork.equals(mSelectedWork) ? TEXT_COLOR_SELECTED : super.getForeground());

						g.setColor(rowColor);
						g.fillRect(0, tb.getY(), pw, mRowHeight);
						rowIndex.value++;

						int ix = 4;
						int iy = tb.getY() + (mRowHeight - TREE_ICON_SIZE) / 2;

						for (int i = 1; i < aIndent.length(); i++, ix += TREE_ICON_SIZE)
						{
							g.drawImage(mTreeIcons.get(aIndent.substring(i, i + 1)), ix, iy, null);
						}

						if (aWork.isDetail())
						{
							g.drawImage(mIconDetail, ix, iy, null);
						}
						else if (aWork.getStartTime() == 0)
						{
							g.drawImage(mIconStatus[Work.Status.PENDING.ordinal()], ix, iy, null);
						}
						else if (aWork.getStatus() == Status.ABORTED || aWork.getStatus() == Status.FAILED || aWork.getStatus() == Status.SUCCESS || aWork.getStatus() == Status.FINISH)
						{
							g.drawImage(mIconStatus[aWork.getStatus().ordinal()], ix, iy, null);
						}
						else if (aWork.getEndTime() != 0)
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

						int textOffset = ix + TREE_ICON_SIZE + 4;

						if (aWork.getValue() != null && !aWork.getValue().toString().isEmpty())
						{
							tb.setX(textOffset + 2);
							tb.setWidth(pw - 10 - textOffset);
							tb.setText(aWork.getLabel() + " -- " + aWork.getValue() + " " + aWork.getLocation());
							tb.render(g);
						}
						else if (aWork.isDetail())
						{
							tb.setX(textOffset + 2);
							tb.setWidth(pw - 10 - textOffset);
							tb.setText(aWork.getLabel() + " " + aWork.getLocation());
							tb.render(g);
						}
						else
						{
							g.setColor(LINE_COLOR);
							g.drawLine(mLabelWidth - 5, tb.getY(), mLabelWidth - 5, tb.getY() + mRowHeight);

							tb.setX(textOffset + 2);
							tb.setWidth(mLabelWidth - 10 - textOffset - 2);
							tb.setText(aWork.getLabel() + " " + aWork.getLocation());
							tb.render(g);

							if (aWork.getStartTime() > 0)
							{
								long startTime = aWork.getStartTime() - minStartTime.value;
								long endTime = Math.max(startTime, (aWork.getEndTime() == 0 ? maxEndTime.value : aWork.getEndTime()) - minStartTime.value);

								if (aWork.getChildren() != null && timeRange > 0)
								{
									boolean hasFinalDotted = false;
									int labelOffset = 0;

									long selfTime = endTime - startTime;
									boolean onlyDetails = true;

									Work[] tmp = aWork.getChildren().toArray(new Work[0]);

									long midTime = 0;
									for (Work w : tmp)
									{
										midTime = Math.max(midTime, Math.max(startTime, (w.getEndTime() == 0 ? maxEndTime.value : w.getEndTime()) - minStartTime.value));
									}

									int selfX0 = mLabelWidth + (int)(startTime * barMaxWidth / timeRange);
									int selfX1 = mLabelWidth + (int)(midTime * barMaxWidth / timeRange);
									int selfX2 = mLabelWidth + (int)(endTime * barMaxWidth / timeRange);

									if (selfX0 != selfX2)
									{
										g.setColor(COLORS[aWork.getColor()]);
										g.fillRect(selfX0, tb.getY() + mRowHeight / 2 - 4, selfX2 - selfX0, 9);
									}

									if (selfX1 > selfX2)
									{
										g.setColor(mGroupColor);
										g.drawLine(selfX1, tb.getY() + mRowHeight / 2 - 1, selfX2, tb.getY() + mRowHeight / 2 - 1);
										g.drawLine(selfX1, tb.getY() + mRowHeight / 2, selfX2, tb.getY() + mRowHeight / 2);
									}

									labelOffset = Math.max(labelOffset, Math.max(selfX0, Math.max(selfX0, selfX2)));

									for (Work w : tmp)
									{
										if (w.getStartTime() > 0)
										{
											long childStartTime = w.getStartTime() - minStartTime.value;
											long childEndTime = (w.getEndTime() == 0 ? maxEndTime.value : w.getEndTime()) - minStartTime.value;

											int childX0 = mLabelWidth + (int)(childStartTime * barMaxWidth / timeRange);
											int childX1 = mLabelWidth + (int)(childEndTime * barMaxWidth / timeRange);

											drawDottedRect(g, childX0, tb.getY() + mRowHeight / 2 - 4, childX1 - childX0, 9, rowColor, rowOutlineColor);

											labelOffset = Math.max(labelOffset, Math.max(childX0, childX1));

											if (w.getEndTime() > 0 && w.getEndTime() < endTime)
											{
												selfTime -= w.getEndTime() - w.getStartTime();
											}

											onlyDetails &= w.isDetail();
										}
									}

									Mutable<Long> childrenMaxEndTime = new Mutable<>(0L);
									aWork.visit((w, i) ->
									{
										childrenMaxEndTime.value = Math.max(childrenMaxEndTime.value, w.getEndTime());
										return AbortOption.CONTINUE;
									});

									int boxX1 = labelOffset;
									int boxX2 = mLabelWidth + (int)((maxEndTime.value - minStartTime.value) * barMaxWidth / timeRange);

									if (boxX2 > boxX1 && childrenMaxEndTime.value == maxEndTime.value)
									{
										g.setColor(mGroupColor);
										g.drawLine(boxX1, tb.getY() + mRowHeight / 2 - 1, boxX2, tb.getY() + mRowHeight / 2 - 1);
										g.drawLine(boxX1, tb.getY() + mRowHeight / 2, boxX2, tb.getY() + mRowHeight / 2);

										labelOffset = boxX2;
									}

									long totalTime = maxEndTime.value - aWork.getStartTime();

									tb.setX(labelOffset + 5).setWidth(pw - labelOffset - 5).setText(onlyDetails ? formatTime(totalTime) : selfTime <= 0 ? "Σ" + formatTime(totalTime) : selfTime >= totalTime ? "Σ" + formatTime(selfTime) : "Ø" + formatTime(selfTime) + " / Σ" + formatTime(totalTime)).render(g);
								}
								else if (timeRange > 0)
								{
									int boxX0 = mLabelWidth + (int)(startTime * barMaxWidth / timeRange);
									int boxX1 = mLabelWidth + (int)(endTime * barMaxWidth / timeRange);
									g.setColor(COLORS[aWork.getColor()]);
									g.fillRect(boxX0, tb.getY() + mRowHeight / 2 - 4, boxX1 - boxX0 + 1, 9);

									tb.setX(boxX1 + 5).setWidth(pw - boxX1 - 5).setText(formatTime(endTime - startTime)).render(g);
								}
							}
						}

						g.setColor(LINE_COLOR);
						g.drawLine(0, tb.getY() + mRowHeight - 1, pw, tb.getY() + mRowHeight - 1);
					}

					tb.translate(0, mRowHeight);

					return AbortOption.CONTINUE;
				});

				g.setColor(mSeparatorColor);
				g.drawLine(0, tb.getY() - 1, pw, tb.getY() - 1);
			}
		}
	}


	private void drawDottedRect(Graphics2D g, int aX, int aY, int aWidth, int aHeight, Color aRowColor, Color aLineColor)
	{
		g.setColor(aRowColor);
		g.fillRect(aX, aY, aWidth, aHeight);
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


	private void drawDot(Graphics2D aGraphics, int aX, int aY)
	{
		if (((aX ^ aY) & 1) == 1)
		{
			aGraphics.drawLine(aX, aY, aX, aY);
		}
	}


	@Override
	public Dimension getPreferredSize()
	{
		Mutable<Integer> count = new Mutable<>(0);

		visit((aWork, aIndent) ->
		{
			count.value++;
			return AbortOption.CONTINUE;
		});

		return new Dimension(0, mRowHeight * count.value);
	}


	public boolean hasWork()
	{
		return mModel.hasWork();
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


	public static void copyToClipboard(final DataFlavor aDataFlavor, final Object aValue)
	{
		Transferable transferable = new Transferable()
		{
			@Override
			public Object getTransferData(DataFlavor aFlavor) throws UnsupportedFlavorException
			{
				if (aFlavor.equals(aDataFlavor))
				{
					return aValue;
				}
				throw new UnsupportedFlavorException(aFlavor);
			}


			@Override
			public DataFlavor[] getTransferDataFlavors()
			{
				return new DataFlavor[]
				{
					aDataFlavor
				};
			}


			@Override
			public boolean isDataFlavorSupported(DataFlavor aFlavor)
			{
				return aFlavor.equals(aDataFlavor);
			}
		};

		ClipboardOwner owner = (clipboard, contents) ->
		{
		};

		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, owner);
	}
}

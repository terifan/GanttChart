package org.terifan.ganttchart.rev2;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import static org.terifan.ganttchart.rev2.StyleSheet.BACKGROUND;
import static org.terifan.ganttchart.rev2.StyleSheet.LABEL_FOREGROUND;


public class WorkStatusPanel extends JPanel
{
	private final static int TREE_ICON_SIZE = 16;

	private WorkStatusModel mModel;
	private WorkStatusPanelRepaintTimer mRepaintTimer;
	private HashMap<Long, WorkVisuals> mLayouts;
	private Long mSelectedWork;


	public WorkStatusPanel() throws IOException
	{
		this(new WorkStatusPanelRepaintTimer());
	}


	public WorkStatusPanel(WorkStatusPanelRepaintTimer aRepaintTimer) throws IOException
	{
		mRepaintTimer = aRepaintTimer;

		mModel = new WorkStatusModel();
		mLayouts = new HashMap<>();

		super.setForeground(LABEL_FOREGROUND);
		super.setBackground(BACKGROUND);
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
//		if (mSelectedRow != null)
//		{
//			scrollRectToVisible(new Rectangle(0, mSelectedRow.y0, 100, mSelectedRow.height));
//			repaint();
//		}
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
			int my = aEvent.getY();
			int vy = 0;
			for (WorkVisuals info : mLayouts.values())
			{
				if (my >= vy && my < vy - info.getLayoutHeight())
				{
					mSelectedWork = info.getWork().getId();
					repaint();
					break;
				}
				vy += info.getLayoutHeight();
			}
		}
	};


	private KeyAdapter mKeyAdapter = new KeyAdapter()
	{
		@Override
		public void keyPressed(KeyEvent aEvent)
		{
//			final Work sw = mSelectedRow.work;
//
//			int keyCode = aEvent.getKeyCode();
//
//			switch (keyCode)
//			{
//				case KeyEvent.VK_C:
//					if (sw != null && aEvent.isControlDown())
//					{
//						copyToClipboard(DataFlavor.stringFlavor, sw.toInfoString());
//					}
//					break;
//			}
//
//			ArrayList<Work> children = mModel.getWork().getChildren();
//			if (children == null)
//			{
//				return;
//			}
//
//			boolean up = keyCode == KeyEvent.VK_HOME || keyCode == KeyEvent.VK_PAGE_UP || keyCode == KeyEvent.VK_UP;
//
//			switch (keyCode)
//			{
//				case KeyEvent.VK_HOME:
//					mSelectedRow = mLayouts.get(0);
//					scrollToSelectedRow();
//					break;
//				case KeyEvent.VK_END:
//					mSelectedRow = mLayouts.get(mLayouts.size() - 1);
//					scrollToSelectedRow();
//					break;
//				case KeyEvent.VK_PAGE_UP:
//				case KeyEvent.VK_PAGE_DOWN:
//				{
//					break;
//				}
//				case KeyEvent.VK_UP:
//					mSelectedRow = mLayouts.get(mSelectedRow == null ? 0 : Math.max(mSelectedRow.index - 1, 0));
//					scrollToSelectedRow();
//					break;
//				case KeyEvent.VK_DOWN:
//					mSelectedRow = mLayouts.get(mSelectedRow == null ? 0 : Math.min(mSelectedRow.index + 1, mLayouts.size() - 1));
//					scrollToSelectedRow();
//					break;
//			}
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

//		if (scrollPane != null)
//		{
//			JScrollBar vsb = scrollPane.getVerticalScrollBar();
//			vsb.setUnitIncrement(mRowMinimumHeight);
//			vsb.setBlockIncrement(10 * mRowMinimumHeight);
//
//			scrollPane.setActionMap(new ActionMap());
//		}
	}


	@Override
	protected void paintComponent(Graphics aGraphics)
	{
		int w = getWidth();
		int h = getHeight();

		Graphics2D g = (Graphics2D)aGraphics;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(BACKGROUND);
		g.fillRect(0, 0, w, h);
		g.setColor(LABEL_FOREGROUND);

		ArrayList<Work> children = mModel.getWork();
		for (int i = 0; i < children.size(); i++)
		{
			Work work = children.get(i);

			WorkVisuals visuals = mLayouts.computeIfAbsent(work.getId(), id -> new WorkVisuals(work));

			visuals.layout();
			visuals.paint(g, w, mSelectedWork);

			g.translate(0, visuals.getLayoutHeight());
		}
	}


	@Override
	public Dimension getPreferredSize()
	{
		int height = 0;

		for (WorkVisuals visuals : mLayouts.values())
		{
			height += visuals.getLayoutHeight();
		}

		return new Dimension(0, height);
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

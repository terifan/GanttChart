package org.terifan.ganttchart;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;


public class SimpleGanttWindow
{
	private JFrame mFrame;
	private JSplitPane mPanel;
	private GanttChartDetailPanel mDetailPanel;
	private GanttChartPanel mChartPanel;


	public SimpleGanttWindow(GanttChart aChart)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
		}

		mDetailPanel = new GanttChartDetailPanel();
		mChartPanel = new GanttChartPanel(aChart, mDetailPanel);

		mPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(mChartPanel), new JScrollPane(mDetailPanel));

//		ListViewModel model = new ListViewModel(Entry.class);
//		model.addColumn("text").setWidth(100); //.setSortable(false);
//		model.addColumn("number").setWidth(100);
//		model.addColumn("progress");
//		model.addItem(new Entry("adam", 1, "/"));
//		model.addItem(new Entry("bertil", 2, "|*"));
//		model.addItem(new Entry("ceasar", 3, "|."));
//		model.addItem(new Entry("david", 4, "."));
//
//		BiFunction<Entry,Integer,String> xx = (n,i)->i==0?n.tree:null;
//		model.setItemTreeFunction(xx);
//
//		JLabel ganttComponent = new JLabel()
//		{
//			@Override
//			protected void paintComponent(Graphics aGraphics)
//			{
//				Rectangle bounds = getBounds();
//				aGraphics.setColor(getBackground());
//				aGraphics.fillRect(bounds.x, bounds.y, getWidth(), getHeight());
//				aGraphics.setColor(getForeground());
//				aGraphics.drawString(getText(), bounds.x, bounds.y + 15);
//
//				aGraphics.setColor(Color.RED);
//				aGraphics.fillOval(bounds.x+bounds.width/2-5, bounds.y+bounds.height/2-5, 10, 10);
//				aGraphics.fillOval(bounds.x+bounds.width-5, bounds.y+bounds.height/2-5, 10, 10);
//			}
//		};
//
//		ListViewCellRenderer<Entry> ganttColumnRenderer = new DetailItemValueRenderer<Entry>()
//		{
//			@Override
//			public JComponent getListViewCellRendererComponent(ListView aListView, Entry aItem, int aItemIndex, int aColumnIndex, boolean aIsSelected, boolean aIsFocused, boolean aIsRollover, boolean aIsSorted)
//			{
//				Color bg = (aItemIndex & 1) == 0 ? new Color(255, 255, 255) : new Color(242, 242, 242);
//
//				ganttComponent.setBackground(aIsSelected ? aListView.getStyles().itemSelectedBackground : bg);
//				ganttComponent.setText(aItem.text);
//				return ganttComponent;
//			}
//		};
//
//		ListViewCellRenderer<Entry> defaultCellRenderer = new DetailItemValueRenderer<Entry>()
//		{
//			@Override
//			public JComponent getListViewCellRendererComponent(ListView aListView, Entry aItem, int aItemIndex, int aColumnIndex, boolean aIsSelected, boolean aHasFocus, boolean aIsRollover, boolean aIsSorted)
//			{
//				Color bg = (aItemIndex & 1) == 0 ? new Color(255, 255, 255) : new Color(242, 242, 242);
//
//				JComponent comp = super.getListViewCellRendererComponent(aListView, aItem, aItemIndex, aColumnIndex, aIsSelected, aHasFocus, aIsRollover, aIsSorted);
//				comp.setBackground(bg);
//				return comp;
//			}
//		};
//
//		DetailItemRenderer renderer = new DetailItemRenderer()
//		{
//			@Override
//			protected ListViewCellRenderer getCellRenderer(ListView aListView, Object aItem, int aItemIndex, int aColumnIndex)
//			{
//				if (aColumnIndex == 2)
//				{
//					return ganttColumnRenderer;
//				}
//
//				return defaultCellRenderer;
//			}
//		};
//
//		renderer.setDefaultRenderer(defaultCellRenderer);
//		renderer.setCellRenderer(2, ganttColumnRenderer);
//		renderer.setExtendLastItem(true);
//
//		ListView listView = new ListView(model);
//		listView.getStyles().itemHorizontalLineThickness = 0;
//		listView.setItemRenderer(renderer);
//		listView.setHeaderRenderer(new ColumnHeaderRenderer().setExtendLastItem(true));
//
//		JPanel panel = new JPanel(new BorderLayout());
//		panel.add(new JScrollPane(listView), BorderLayout.CENTER);
//		panel.add(mPanel, BorderLayout.SOUTH);
//
//		JSplitPane x = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panel, mPanel);

		mFrame = new JFrame();
		mFrame.add(mPanel);
		mFrame.setSize(1024, 768);
		mFrame.setLocationRelativeTo(null);
		mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}


	public static class Entry
	{
		String text;
		int number;
		String tree;


		public Entry(String aText, int aNumber, String aTree)
		{
			this.text = aText;
			this.number = aNumber;
			this.tree = aTree;
		}
	}


	public GanttChartPanel getChartPanel()
	{
		return mChartPanel;
	}


	public GanttChartDetailPanel getDetailPanel()
	{
		return mDetailPanel;
	}


	public SimpleGanttWindow show()
	{
		mFrame.setVisible(true);

		mPanel.setContinuousLayout(true);
		mPanel.setDividerLocation(0.8);

		return this;
	}
}

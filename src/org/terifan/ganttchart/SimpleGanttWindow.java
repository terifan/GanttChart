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

		JScrollPane scrollPane1 = new JScrollPane(mChartPanel);
		JScrollPane scrollPane2 = new JScrollPane(mDetailPanel);
		scrollPane1.setBorder(null);
		scrollPane2.setBorder(null);

		mPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane1, scrollPane2);

		mFrame = new JFrame();
		mFrame.add(mPanel);
		mFrame.setSize(1024, 768);
		mFrame.setLocationRelativeTo(null);
		mFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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

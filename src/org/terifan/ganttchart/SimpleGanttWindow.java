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
		scrollPane1.setBorder(null);

		JScrollPane scrollPane2 = new JScrollPane(mDetailPanel);
		scrollPane2.setBorder(null);

		mPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane1, scrollPane2);

		mFrame = new JFrame();
		mFrame.add(mPanel);
		mFrame.setSize(1024, 768);
		mFrame.setLocationRelativeTo(null);
		mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

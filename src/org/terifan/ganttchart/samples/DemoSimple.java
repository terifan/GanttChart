package org.terifan.ganttchart.samples;

import java.util.Timer;
import java.util.TimerTask;
import org.terifan.ganttchart.GanttChart;
import org.terifan.ganttchart.GanttElement;
import org.terifan.ganttchart.SimpleGanttWindow;


public class DemoSimple
{
	public static void main(String... args)
	{
		try
		{
			GanttChart chart = new GanttChart();

			SimpleGanttWindow window = new SimpleGanttWindow(chart).show();

			Timer timer = new Timer();
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					window.getChartPanel().repaint();
					window.getDetailPanel().repaint();
				}
			}, 100, 100);

			try (GanttElement func1 = chart.enter("program"))
			{
				Thread.sleep(500);

				try (GanttElement func2 = chart.enter("subroutine 1"))
				{
					Thread.sleep(500);
					chart.tick("tick 1");
					Thread.sleep(500);
					chart.tick("tick 2");
					Thread.sleep(500);
					chart.tick("tick 3");
					Thread.sleep(500);
				}

				try (GanttElement func2 = chart.enter("subroutine 2"))
				{
					Thread.sleep(500);
					chart.tick("tick 1");
					Thread.sleep(500);
					chart.tick("tick 2");
					Thread.sleep(500);
					chart.tick("tick 3");
					Thread.sleep(500);
				}

				Thread.sleep(500);
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}

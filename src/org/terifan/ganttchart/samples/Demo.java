package org.terifan.ganttchart.samples;

import java.util.Timer;
import java.util.TimerTask;
import org.terifan.ganttchart.GanttChart;
import org.terifan.ganttchart.SimpleGanttWindow;


public class Demo
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

			try (GanttChart unused1 = chart.enter("my function"))
			{
				Thread.sleep(10);

				chart.enter("start tiny work");
				chart.tick("tick");
				chart.exit();

				Thread.sleep(100);

				try (GanttChart unused2 = chart.enter("doing something"))
				{
					Thread.sleep(100);
					try (GanttChart unused3 = chart.enter("lets do it"))
					{
						Thread.sleep(1500);
						chart.tick("next step");
						Thread.sleep(500);
						chart.tick("last step");
						Thread.sleep(1000);
					}
					Thread.sleep(50);
					try (GanttChart unused3 = chart.enter("even more work"))
					{
						Thread.sleep(100);
					}
					Thread.sleep(50);
					try (GanttChart unused3 = chart.enter("some more"))
					{
						Thread.sleep(100);
					}
					Thread.sleep(50);
				}
				Thread.sleep(50);
				try (GanttChart unused2 = chart.enter("almost finished"))
				{
					Thread.sleep(100);
					try (GanttChart unused3 = chart.enter("almost there"))
					{
						Thread.sleep(100);
						chart.tick("only some more");
						Thread.sleep(100);
						chart.tick("yeeees");
						Thread.sleep(100);
					}
					Thread.sleep(50);
				}
				Thread.sleep(50);
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}

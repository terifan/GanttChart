package org.terifan.ganttchart.samples;

import java.util.ArrayList;
import java.util.Random;
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

				Random rnd = new Random();

				ArrayList<Runnable> elements = new ArrayList<>();
				for (int i = 0; i < 5; i++)
				{
					elements.add(() ->
					{
						try (GanttChart unused2 = chart.enter("doing something"))
						{
							Thread.sleep(rnd.nextInt(1000));
							try (GanttChart unused3 = chart.enter("lets do it"))
							{
								Thread.sleep(rnd.nextInt(1000));
								chart.tick("next step");
								Thread.sleep(rnd.nextInt(1000));
								chart.tick("last step");
								Thread.sleep(rnd.nextInt(1000));
							}
							Thread.sleep(rnd.nextInt(1000));
							try (GanttChart unused3 = chart.enter("even more work"))
							{
								Thread.sleep(rnd.nextInt(1000));
							}
							Thread.sleep(rnd.nextInt(1000));
							try (GanttChart unused3 = chart.enter("some more"))
							{
								Thread.sleep(rnd.nextInt(1000));
							}
							Thread.sleep(rnd.nextInt(1000));
						}
						catch (Exception e)
						{
						}
					});
				}

				elements.parallelStream().forEach(e->e.run());

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

package org.terifan.ganttchart.samples;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import org.terifan.ganttchart.GanttChart;
import org.terifan.ganttchart.GanttElement;
import org.terifan.ganttchart.SimpleGanttWindow;


public class DemoMultiThread
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

			try (GanttElement func1 = chart.enter("my function"))
			{
				Thread.sleep(10);

				try (GanttElement func2 = func1.enter("start tiny work"))
				{
					func2.tick("tick");
				}

				Thread.sleep(100);

				Random rnd = new Random();

				ArrayList<Runnable> elements = new ArrayList<>();
				for (int i = 0; i < 5; i++)
				{
					elements.add(() ->
					{
						try (GanttElement func2 = func1.enter("doing something").from("x"))
						{
							Thread.sleep(rnd.nextInt(1000));
							try (GanttElement func3 = func2.enter("lets do it"))
							{
								Thread.sleep(rnd.nextInt(1000));
								func3.tick("next step");
								Thread.sleep(rnd.nextInt(1000));
								func3.tick("last step");
								Thread.sleep(rnd.nextInt(1000));
							}
							Thread.sleep(rnd.nextInt(1000));
							try (GanttElement func3 = func2.enter("even more work"))
							{
								Thread.sleep(rnd.nextInt(1000));
							}
							Thread.sleep(rnd.nextInt(1000));
							try (GanttElement func3 = func2.enter("some more"))
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

				elements.parallelStream().forEach(Runnable::run);

				try (GanttElement func2 = func1.enter("almost finished").to("x"))
				{
					Thread.sleep(100);
					try (GanttElement func3 = func2.enter("almost there"))
					{
						Thread.sleep(100);
						func3.tick("only some more");
						Thread.sleep(100);
						func3.tick("yeeees");
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

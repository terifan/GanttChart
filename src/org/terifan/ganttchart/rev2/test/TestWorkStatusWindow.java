package org.terifan.ganttchart.rev2.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;
import org.terifan.ganttchart.rev2.PendingWork;
import org.terifan.ganttchart.rev2.Work;
import org.terifan.ganttchart.rev2.WorkStatusModel;
import org.terifan.ganttchart.rev2.WorkStatusPanel;


public class TestWorkStatusWindow
{
	public static void main(String ... args)
	{
		try
		{
			WorkStatusModel model = new WorkStatusModel();
			WorkStatusPanel panel = new WorkStatusPanel();
			panel.setModel(model);

			JFrame frame = new JFrame();
			frame.add(panel);
			frame.setSize(1024, 768);
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);

			ExecutorService executor = Executors.newFixedThreadPool(1);

			try (Work project = model.start())
			{
				Thread.sleep(100);
				try (Work task = project.start("task"))
				{
					Thread.sleep(100);
					try (Work item = task.start("item"))
					{
						Thread.sleep(100);
						item.detail("routine").setBody("aaa\nbbb\nccc");
					}

					Thread.sleep(100);

					try (Work item = task.start("item"))
					{
						PendingWork[] routines = new PendingWork[10];
						PendingWork[] subroutines = new PendingWork[10];
						for (int i = 0; i < 10; i++)
						{
							routines[i] = item.pending("routine " + i);
							subroutines[i] = routines[i].pending("subroutine");
						}
						Thread.sleep(100);
						for (int i = 0; i < 8; i++)
						{
							try (Work w = routines[i].start())
							{
								Thread.sleep(100);
								int _i = i;
								executor.submit(() -> {
									try (Work wx = subroutines[_i].start().setColor(1))
									{
										try
										{
											Thread.sleep(5000);
										}
										catch (Exception e)
										{
											e.printStackTrace(System.out);
										}
										wx.success();
									}
								});
							}
							Thread.sleep(100);
						}
						routines[8].detail("123abc").success();
					}
				}
				project.success();
			}

			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.DAYS);
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}

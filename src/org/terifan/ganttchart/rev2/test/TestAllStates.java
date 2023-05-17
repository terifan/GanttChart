package org.terifan.ganttchart.rev2.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import org.terifan.ganttchart.rev2.PendingWork;
import org.terifan.ganttchart.rev2.Work;
import org.terifan.ganttchart.rev2.WorkStatusModel;
import org.terifan.ganttchart.rev2.WorkStatusPanel;


public class TestAllStates
{
	private static Random rnd = new Random(3);


	public static void main(String... args)
	{
		try
		{
			WorkStatusPanel panel = new WorkStatusPanel();

			JFrame frame = new JFrame();
			frame.add(new JScrollPane(panel));
			frame.setSize(1024, 768);
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);

			WorkStatusModel model = new WorkStatusModel();

			new Timer().schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					try
					{
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						try (ObjectOutputStream oos = new ObjectOutputStream(baos))
						{
							oos.writeObject(model);
						}

						try (ObjectInputStream oos = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())))
						{
							panel.setModel((WorkStatusModel)oos.readObject());
						}
					}
					catch (Exception e)
					{
						e.printStackTrace(System.out);
					}
				}
			}, 250, 250);

			try (Work w0 = model.start("test 1"))
			{
					Thread.sleep(rnd.nextInt(100));
				try (Work w1 = w0.start("a"))
				{
					Thread.sleep(rnd.nextInt(100));
					try (Work w2 = w1.start("b"))
					{
					Thread.sleep(rnd.nextInt(100));
						try (Work w3 = w2.start("c"))
						{
					Thread.sleep(rnd.nextInt(100));
							w3.detail("details");
					Thread.sleep(rnd.nextInt(100));
							w3.success();
					Thread.sleep(rnd.nextInt(100));
						}
					Thread.sleep(rnd.nextInt(100));
					}
					Thread.sleep(rnd.nextInt(100));
				}
					Thread.sleep(rnd.nextInt(100));
			}

			try (Work w0 = model.start("test 2"))
			{
					Thread.sleep(rnd.nextInt(100));
				try (Work w1 = w0.start("a"))
				{
					Thread.sleep(rnd.nextInt(100));
					try (Work w2 = w1.start("b"))
					{
					Thread.sleep(rnd.nextInt(100));
						try (Work w3 = w2.start("c"))
						{
					Thread.sleep(rnd.nextInt(100));
							w3.fail();
						}
					Thread.sleep(rnd.nextInt(100));
					}
					Thread.sleep(rnd.nextInt(100));
				}
					Thread.sleep(rnd.nextInt(100));
			}

			try (Work w0 = model.start("test 3"))
			{
				Thread.sleep(rnd.nextInt(100));
				try (Work w1 = w0.start("a"))
				{
					Thread.sleep(rnd.nextInt(100));
					try (Work w2 = w1.start("b"))
					{
						Thread.sleep(rnd.nextInt(100));
						try (Work w3 = w2.start("c"))
						{
							Thread.sleep(rnd.nextInt(100));
							w3.abort();
						}
					}
					Thread.sleep(rnd.nextInt(100));
				}
			}

			try (Work w0 = model.start("test 4"))
			{
				Thread.sleep(rnd.nextInt(100));
				try (Work w1 = w0.start("a"))
				{
					Thread.sleep(rnd.nextInt(100));
					try (Work w2 = w1.start("b"))
					{
						Thread.sleep(rnd.nextInt(100));
						try (Work w3 = w2.start("c"))
						{
							Thread.sleep(rnd.nextInt(100));
							w3.abort();
						}
						Thread.sleep(rnd.nextInt(100));
						try (Work w3 = w2.start("c"))
						{
							Thread.sleep(rnd.nextInt(100));
							w3.abort();
						}
						Thread.sleep(rnd.nextInt(100));
						w2.fail();
					}
				}
			}

			try (Work w0 = model.start("test 5"))
			{
					Thread.sleep(rnd.nextInt(100));
				try (Work w1 = w0.start("a"))
				{
					Thread.sleep(rnd.nextInt(100));
					try (Work w2 = w1.start("b"))
					{
					Thread.sleep(rnd.nextInt(100));
						try (Work w3 = w2.start("c"))
						{
							Thread.sleep(rnd.nextInt(100));
							w3.abort();
						}
					Thread.sleep(rnd.nextInt(100));
						try (Work w3 = w2.start("c"))
						{
							Thread.sleep(rnd.nextInt(100));
							w3.success();
						}
					Thread.sleep(rnd.nextInt(100));
					}
				}
					Thread.sleep(rnd.nextInt(100));
			}

			{
				Work w0 = model.start("test 5");
					Thread.sleep(rnd.nextInt(100));
				Work w1 = w0.start("a");
					Thread.sleep(rnd.nextInt(100));
				Work w2 = w1.start("b");
					Thread.sleep(rnd.nextInt(100));
				try (Work w3 = w2.start("c"))
				{
					Thread.sleep(rnd.nextInt(100));
					w3.abort();
				}
					Thread.sleep(rnd.nextInt(100));
				try (Work w3 = w2.start("d"))
				{
					Thread.sleep(rnd.nextInt(100));
					w3.finish();
				}
					Thread.sleep(rnd.nextInt(100));
				Work w3 = w2.start("e");
					Thread.sleep(rnd.nextInt(100));
				PendingWork w4 = w2.pending("f");
					Thread.sleep(rnd.nextInt(100));
				PendingWork w5 = w2.pending("g");
			}

//			for (int i = 0; i < 10; i++)
//			{
//				try (Work w0 = model.start("test " + i))
//				{
//					for (int j = 1 + rnd.nextInt(5); --j >= 0;)
//					{
//						Boolean r = a(w0);
//						if (r != null)
//						{
//							break;
//						}
//					}
//				}
//			}
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}


	private static Boolean a(final Work aWork)
	{
		try (Work w0 = aWork.start())
		{
			for (int i = 1 + rnd.nextInt(5); --i >= 0;)
			{
				sleep();
				Boolean r = b(w0);
				if (r != null)
				{
					return r;
				}
			}
		}
		return null;
	}


	private static Boolean b(final Work aWork)
	{
		try (Work w0 = aWork.start())
		{
			for (int i = 1 + rnd.nextInt(5); --i >= 0;)
			{
				sleep();
				Boolean r = c(w0);
				if (r != null)
				{
					return r;
				}
			}
		}
		return null;
	}


	private static Boolean c(final Work aWork)
	{
		try (Work w0 = aWork.start())
		{
			for (int i = 1 + rnd.nextInt(5); --i >= 0;)
			{
				sleep();
			}
			if (rnd.nextInt(10) == 0)
			{
				w0.fail();
				return false;
			}
			if (rnd.nextInt(10) == 0)
			{
				w0.abort();
				return null;
			}
			if (rnd.nextInt(10) == 0)
			{
				w0.success();
				return true;
			}
		}
		return rnd.nextInt(10)<5 ? false : null;
	}


	public static void sleep()
	{
		try
		{
			Thread.sleep(rnd.nextInt(100));
		}
		catch (Exception e)
		{
		}
	}
}
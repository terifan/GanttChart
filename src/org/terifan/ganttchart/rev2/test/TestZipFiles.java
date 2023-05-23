package org.terifan.ganttchart.rev2.test;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import org.terifan.ganttchart.rev2.PendingWork;
import org.terifan.ganttchart.rev2.Work;
import org.terifan.ganttchart.rev2.WorkStatusModel;
import org.terifan.ganttchart.rev2.WorkStatusPanel;


public class TestZipFiles
{
	private static Random rnd = new Random(3);


	public static void main(String... args)
	{
		try
		{
			WorkStatusPanel panel = new WorkStatusPanel();

			JFrame frame = new JFrame();
			frame.add(new JScrollPane(panel));
			frame.setSize(1200, 1400);
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);

			WorkStatusModel model = new WorkStatusModel();

			Timer timer = new Timer();
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					try
					{
//						ByteArrayOutputStream baos = new ByteArrayOutputStream();
//						try (ObjectOutputStream oos = new ObjectOutputStream(baos))
//						{
//							oos.writeObject(model);
//						}
//
//						try (ObjectInputStream oos = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())))
//						{
//							panel.setModel((WorkStatusModel)oos.readObject());
//						}

						panel.revalidate();
						panel.repaint();
					}
					catch (Exception e)
					{
						e.printStackTrace(System.out);
					}
				}
			}, 250, 250);

			panel.setModel(model);

			String src = "d:\\Pictures";
			String dst = "d:\\test.zip";
			AtomicInteger limit = new AtomicInteger(100);

			try (Work w0 = model.start("creating zip"); ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dst)))
			{
				w0.detail("Zip %s files from %s to %s", limit.get(), src, dst);

				visit(Paths.get(src), zos, w0, limit);
			}

			panel.revalidate();
			panel.repaint();
			timer.cancel();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try (ObjectOutputStream oos = new ObjectOutputStream(baos))
			{
				model.writeExternal(oos);
			}
			Files.write(Paths.get("d:\\gantt.dat"), baos.toByteArray());

			System.out.println("size: " + model.total());
			System.out.println("serialized: " + baos.size());

			baos = new ByteArrayOutputStream();
			try (ObjectOutputStream oos = new ObjectOutputStream(new DeflaterOutputStream(baos)))
			{
				model.writeExternal(oos);
			}

			System.out.println("compressed: " + baos.size());
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}


	private static void visit(Path aPath, ZipOutputStream aZip, Work aWork, AtomicInteger aCounter) throws IOException
	{
		try (Work w = aWork.start("Processing folder"))
		{
			w.detail("Path " + aPath);

			List<Path> files = Files.list(aPath).sorted((e, f) -> e.equals(f) ? 0 : Files.isDirectory(e) && Files.isDirectory(f) ? e.compareTo(f) : Files.isDirectory(e) ? -1 : e.compareTo(f)).toList();

			TreeMap<Path, PendingWork> pending = new TreeMap<>();
			for (Path path : files)
			{
				if (Files.isDirectory(path))
				{
					visit(path, aZip, w, aCounter);
				}
				else if (aCounter.get() > 0 && pending.size() < 10)
				{
					aCounter.decrementAndGet();

					pending.put(path, w.pending("Adding file " + path.getFileName().toString()));
				}
			}

			for (Entry<Path, PendingWork> entry : pending.entrySet())
			{
//				Thread.sleep(rnd.nextInt(50));

				try (Work w0 = entry.getValue().start())
				{
//					Thread.sleep(rnd.nextInt(50));

					try
					{
						byte[] bytes;
						try (Work w1 = w0.start("Reading file"))
						{
							bytes = Files.readAllBytes(entry.getKey());
//							if (rnd.nextInt(2) == 0) throw new EOFException();
//							Thread.sleep(rnd.nextInt(1000));
						}

						ZipEntry zipEntry;
						try (Work w1 = w0.start("Writing to zip"))
						{
							w1.setColor(3);
							try
							{
								zipEntry = new ZipEntry(entry.getKey().subpath(1, entry.getKey().getNameCount()).toString());
								aZip.putNextEntry(zipEntry);
								aZip.write(bytes);
								aZip.closeEntry();
							}
							catch (Exception e)
							{
								w1.detail(e);
								w1.fail();
								return;
							}
//							Thread.sleep(rnd.nextInt(1000));
						}

						w0.detail("Finished writing " + bytes.length + " bytes, compressed " + zipEntry.getCompressedSize() + " bytes, ratio " + (100 - zipEntry.getCompressedSize() * 100 / bytes.length) + "%");
						w0.success();
					}
					catch (Exception e)
					{
						w0.detail(e);
						w0.fail();
					}
				}
				catch (Exception e)
				{
					e.printStackTrace(System.out);
					w.abort();
				}
			}
		}
	}
}

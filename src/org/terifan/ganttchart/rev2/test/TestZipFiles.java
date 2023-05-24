package org.terifan.ganttchart.rev2.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

						panel.revalidate();
						panel.repaint();

						if (!panel.getModel().hasWork())
						{
							cancel();
						}
					}
					catch (Exception e)
					{
						e.printStackTrace(System.out);
					}
				}
			}, 0, 250);

//			panel.setModel(model);

			ExecutorService pool = Executors.newFixedThreadPool(2);
			String[] srcs = {"d:\\Pictures","d:\\Documents","d:\\Music","d:\\Videos"};
			String[] dsts = {"d:\\test1.zip","d:\\test2.zip","d:\\test3.zip","d:\\test4.zip"};

			for (int i = 0; i < srcs.length; i++)
			{
				String src = srcs[i];
				String dst = dsts[i];

				pool.submit(() -> {
					try
					{
						AtomicInteger limit = new AtomicInteger(100);

						try (Work w0 = model.start("creating zip"); ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dst)))
						{
							w0.detail("Zip %s files from %s to %s", limit.get(), src, dst);

							visit(Paths.get(src), zos, w0, limit);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace(System.out);
					}
				});
			}

			try
			{
				pool.shutdown();
				pool.awaitTermination(1, TimeUnit.DAYS);
			}
			catch (Exception e)
			{
			}


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
		try (Work w0 = aWork.start("Processing folder"))
		{
			w0.detail("Path " + aPath);

			List<Path> files = Files.list(aPath).sorted((e, f) -> e.equals(f) ? 0 : Files.isDirectory(e) && Files.isDirectory(f) ? e.compareTo(f) : Files.isDirectory(e) ? -1 : e.compareTo(f)).toList();

			TreeMap<Path, PendingWork> pending = new TreeMap<>();
			for (Path path : files)
			{
				if (Files.isDirectory(path))
				{
					visit(path, aZip, w0, aCounter);
				}
				else if (aCounter.get() > 0) // && pending.size() < 10)
				{
					aCounter.decrementAndGet();

					pending.put(path, w0.pending("Adding file " + path.getFileName().toString()));
				}
			}

			if (pending.isEmpty())
			{
				w0.success();
			}

			ExecutorService pool = Executors.newFixedThreadPool(5);
			for (Entry<Path, PendingWork> entry : pending.entrySet())
			{
				pool.submit(()-> {
					try (Work w1 = entry.getValue().start())
					{
						try
						{
							Thread.sleep(rnd.nextInt(100));

							byte[] bytes;
							try (Work w2 = w1.stage("Reading file"))
							{
								bytes = Files.readAllBytes(entry.getKey());
	//							if (rnd.nextInt(2) == 0) throw new EOFException();
								Thread.sleep(rnd.nextInt(100));
							}

							Thread.sleep(rnd.nextInt(100));

							try (Work w2 = w1.stage("Computing checksum"))
							{
								MessageDigest md = MessageDigest.getInstance("SHA512");
								md.update(bytes);
								w2.detail(HexFormat.of().formatHex(md.digest()));
							}

							ZipEntry zipEntry;
							try (Work w2 = w1.stage("Writing to zip"))
							{
								w2.setColor(3);
								synchronized (TestZipFiles.class)
								{
									try
									{
										zipEntry = new ZipEntry(entry.getKey().subpath(1, entry.getKey().getNameCount()).toString());
										aZip.putNextEntry(zipEntry);
										aZip.write(bytes);
										aZip.closeEntry();
									}
									catch (Exception e)
									{
										w2.detail(e);
										w2.fail();
										return;
									}
								}
								Thread.sleep(rnd.nextInt(100));

								w2.detail("Finished writing " + bytes.length + " bytes, compressed " + zipEntry.getCompressedSize() + " bytes, ratio " + (100 - zipEntry.getCompressedSize() * 100 / bytes.length) + "%");
							}

							Thread.sleep(rnd.nextInt(100));

							w1.success();
						}
						catch (Exception e)
						{
							w1.detail(e);
							w1.fail();
						}
					}
				});
			}
			try
			{
				pool.shutdown();
				pool.awaitTermination(1, TimeUnit.DAYS);
			}
			catch (Exception e)
			{
			}
		}
	}
}

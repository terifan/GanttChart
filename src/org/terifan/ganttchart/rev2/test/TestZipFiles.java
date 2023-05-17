package org.terifan.ganttchart.rev2.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import org.terifan.ganttchart.rev2.Mutable;
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

			String src = "d:\\pictures";
			String dst = "d:\\test.zip";
			Mutable<Integer> limit = new Mutable<>(10);

			try (Work w0 = model.start("creating zip"); ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dst)))
			{
				w0.detail("Zip %s files from %s to %s", limit.value, src, dst);

				visit(Paths.get(src), zos, w0, limit);
			}
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}


	private static void visit(Path aPath, ZipOutputStream aZip, Work aWork, Mutable<Integer> aCounter) throws IOException
	{
		try (Work w0 = aWork.start(aPath.toString()))
		{
			List<Path> files = Files.list(aPath).toList();

			for (Path path : files)
			{
				try
				{
					if (aCounter.value <= 0)
					{
						aWork.detail("Reached file limit, aborting...");
						aWork.success();
						return;
					}

					if (Files.isDirectory(path))
					{
						visit(path, aZip, w0, aCounter);
					}
					else if (Files.size(path) < 1000000)
					{
						w0.start("Ignoring short file").abort();
					}
					else
					{
						aCounter.value = aCounter.value - 1;

						try (Work w1 = w0.start("Adding file " + path.getFileName().toString()))
						{
							try
							{
								byte[] bytes;
								try (Work w2 = w1.start("Reading file"))
								{
									bytes = Files.readAllBytes(path);

									if (rnd.nextInt(2) == 0) throw new EOFException();
								}

								ZipEntry zipEntry;
								try (Work w2 = w1.start("Writing to zip"))
								{
									try
									{
										zipEntry = new ZipEntry(path.subpath(1, path.getNameCount()).toString());
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

								w1.detail("Finished wiring " + bytes.length + "bytes, compressed " + zipEntry.getCompressedSize() + " bytes, ratio " + (100-zipEntry.getCompressedSize()*100/bytes.length)+"%");
								w1.success();
							}
							catch (Exception e)
							{
								w1.detail(e);
								w1.fail();
							}
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace(System.out);
					w0.abort();
				}
			}
		}
	}
}

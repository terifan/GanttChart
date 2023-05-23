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
import java.util.zip.DeflaterOutputStream;
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

						panel.repaint();
					}
					catch (Exception e)
					{
						e.printStackTrace(System.out);
					}
				}
			}, 250, 250);

panel.setModel(model);

			String src = "d:\\pictures";
			String dst = "d:\\test.zip";
			Mutable<Integer> limit = new Mutable<>(4000);

			try (Work w0 = model.start("creating zip"); ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dst)))
			{
				w0.detail("Zip %s files from %s to %s", limit.value, src, dst);

				visit(Paths.get(src), zos, w0, limit);
			}

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


	private static void visit(Path aPath, ZipOutputStream aZip, Work aWork, Mutable<Integer> aCounter) throws IOException
	{
		List<Path> files = Files.list(aPath).toList();

		for (Path path : files)
		{
			if (aCounter.value <= 0)
			{
				return;
			}

			try
			{
				if (Files.isDirectory(path))
				{
					visit(path, aZip, aWork, aCounter);
				}
//				else if (Files.size(path) < 1000000)
//				{
//					aWork.start("Ignoring short file").abort();
//				}
				else
				{
					aCounter.value = aCounter.value - 1;

					try (Work w0 = aWork.start("Adding file " + path.getFileName().toString()))
					{
						try
						{
							byte[] bytes;
							try (Work w1 = w0.start("Reading file"))
							{
								bytes = Files.readAllBytes(path);

//								if (rnd.nextInt(2) == 0) throw new EOFException();
							}

							ZipEntry zipEntry;
							try (Work w1 = w0.start("Writing to zip"))
							{
								w1.setColor(3);
								try
								{
									zipEntry = new ZipEntry(path.subpath(1, path.getNameCount()).toString());
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
							}

							w0.detail("Finished writing " + bytes.length + " bytes, compressed " + zipEntry.getCompressedSize() + " bytes, ratio " + (100-zipEntry.getCompressedSize()*100/bytes.length)+"%");
							w0.success();
						}
						catch (Exception e)
						{
							w0.detail(e);
							w0.fail();
						}
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace(System.out);
				aWork.abort();
			}
		}
	}
}

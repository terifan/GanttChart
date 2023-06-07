package org.terifan.ganttchart.rev2.test;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.terifan.ganttchart.rev2.StatusPanelRow;
import org.terifan.ganttchart.rev2.StatusPanelRow.Status;


public class X
{
	public static void main(String... args)
	{
		try
		{
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.insets = new Insets(4, 4, 4, 4);
			c.weightx = 1;

			Consumer<StatusPanelRow> action = btn -> System.out.println(btn.getText());

			JPanel panel = new JPanel(new GridBagLayout());
			panel.setBackground(Color.BLACK);
			panel.add(new StatusPanelRow(Status.ABORT, "Asassas assasasa assasasa asassass assasasas", "15:20", action), c);
			panel.add(new StatusPanelRow(Status.ERROR, "Asassas assasasa assasasa asassass assasasas", "15:20", action), c);
			panel.add(new StatusPanelRow(Status.FAIL, "Asassas assasasa assasasa asassass assasasas", "15:20", action), c);
			panel.add(new StatusPanelRow(Status.INFO, "Asassas assasasa assasasa asassass assasasas", "15:20", action), c);
			panel.add(new StatusPanelRow(Status.SUCCESS, "Asassas assasasa assasasa asassass assasasas", "15:20", action), c);
			panel.add(new StatusPanelRow(Status.WARN, "Asassas assasasa assasasa asassass assasasas", "15:20", action), c);
			panel.add(new StatusPanelRow(Status.PENDING, "Asassas assasasa assasasa asassass assasasas", "15:20", action), c);
			panel.add(new StatusPanelRow(Status.RUNNING, "Asassas assasasa assasasa asassass assasasas", "15:20", action), c);
			c.weighty = 1;
			panel.add(new JLabel(), c);

			JFrame frame = new JFrame();
			frame.add(new JScrollPane(panel));
			frame.setSize(1024, 768);
			frame.setLocationRelativeTo(null);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
		}
		catch (Throwable e)
		{
			e.printStackTrace(System.out);
		}
	}
}

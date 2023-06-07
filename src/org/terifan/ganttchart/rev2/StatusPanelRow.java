package org.terifan.ganttchart.rev2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.JButton;


public class StatusPanelRow extends JButton
{
	private Status mStatus;
	private String mTime;


	public enum Status
	{
		INFO,
		WARN,
		ERROR,
		ABORT,
		FAIL,
		SUCCESS,
		PENDING,
		RUNNING
	}


	public StatusPanelRow(Status aStatus, String aText, String aTime, Consumer<StatusPanelRow> aOnClick)
	{
		mStatus = aStatus;
		mTime = aTime;

		setForeground(new Color(255, 255, 255));
		setBackground(new Color(32, 32, 32));
		setBorderPainted(false);
		setFocusPainted(false);
		setRolloverEnabled(true);
		setAction(new AbstractAction(aText)
		{
			@Override
			public void actionPerformed(ActionEvent aEvent)
			{
				aOnClick.accept(StatusPanelRow.this);
			}
		});
	}


	@Override
	protected void paintComponent(Graphics aGraphics)
	{
		((Graphics2D)aGraphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		aGraphics.setColor(getParent().getBackground());
		aGraphics.fillRect(0, 0, getWidth(), getHeight());

		if (getModel().isRollover())
		{
			aGraphics.setColor(getBackground().brighter());
		}
		else
		{
			aGraphics.setColor(getBackground());
		}
		aGraphics.fillRoundRect(0, 0, getWidth(), getHeight(), 13, 13);

		aGraphics.setFont(aGraphics.getFont().deriveFont(Font.PLAIN));

		int x0 = getWidth() - 10 - aGraphics.getFontMetrics().stringWidth(mTime);
		aGraphics.setColor(getForeground().darker());
		aGraphics.drawString(mTime, x0, 20);

		Shape old = aGraphics.getClip();
		aGraphics.setClip(0, 0, x0 - 10, getHeight());
		aGraphics.setColor(getForeground());
		aGraphics.drawString(getText(), 40, 20);
		aGraphics.setClip(old);

		aGraphics.drawImage(StyleSheet.mIconRowStatus.get(mStatus), 10, 8, null);
	}


	@Override
	public Dimension getPreferredSize()
	{
		return new Dimension(100, 30);
	}
}

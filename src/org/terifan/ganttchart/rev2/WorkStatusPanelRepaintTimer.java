package org.terifan.ganttchart.rev2;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;


public class WorkStatusPanelRepaintTimer
{
	private Timer mTimer;
	private AtomicBoolean mTimerTaskStarted;
	private ArrayList<WorkStatusPanel> mPanels;


	public WorkStatusPanelRepaintTimer()
	{
		mPanels = new ArrayList<>();
		mTimer = new Timer(true);
		mTimerTaskStarted = new AtomicBoolean();
	}


	void add(WorkStatusPanel aPanel)
	{
		mPanels.remove(aPanel);
		mPanels.add(aPanel);
	}


	public void remove(WorkStatusPanel aPanel)
	{
		mPanels.remove(aPanel);
	}


	void startRepaintTimer()
	{
//		if (!mTimerTaskStarted.getAndSet(true))
//		{
//			mTimer.schedule(new RepaintTask(), 100, 100);
//		}
//
//		for (WorkStatusPanel panel : mPanels)
//		{
//			if (panel != null)
//			{
//				panel.invalidate();
//				panel.revalidate();
//			}
//		}
	}


	private class RepaintTask extends TimerTask
	{
		@Override
		public void run()
		{
			boolean hasWork = false;

			for (WorkStatusPanel panel : mPanels)
			{
				panel.repaint();

//				hasWork |= panel.hasWork();
			}

			if (!hasWork)
			{
				cancel();
				mTimer.schedule(new RepaintTask(), 100);
				mTimerTaskStarted.set(false);
			}
		}
	};
}

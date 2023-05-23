package org.terifan.ganttchart.rev2;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;


public class WorkStatusModel implements Externalizable
{
	private final static long serialVersionUID = 1L;

	private Work mRoot;

	private transient WorkStatusPanel mPanel;


	public WorkStatusModel()
	{
		mRoot = new Work("root");
	}


	public Work start()
	{
		StackTraceElement stack = new Throwable().getStackTrace()[1];
		String classPath = stack.getClassName();
		String className = classPath.substring(classPath.lastIndexOf('.') + 1);

		if (mRoot.getStartTime() == 0)
		{
			mRoot.startSelf();
		}

		Work work = mRoot.start(stack.getMethodName() + ":" + className);

		if (mPanel != null)
		{
			mPanel.startRepaintTimer();
		}

		return work;
	}


	public Work start(Object aLabel)
	{
		if (mRoot.getStartTime() == 0)
		{
			mRoot.startSelf();
		}

		Work work = mRoot.start(aLabel);

		if (mPanel != null)
		{
			mPanel.startRepaintTimer();
		}

		return work;
	}


	public Work detail(Object aLabel)
	{
		return mRoot.detail(aLabel);
	}


	public synchronized PendingWork pending(Object aLabel)
	{
		return new PendingWork(new Work(aLabel));
	}


	public synchronized int size()
	{
		return mRoot.size();
	}


	public synchronized int total()
	{
		return total(mRoot);
	}


	public synchronized int total(Work aWork)
	{
		int sz = 1;
		if (aWork.getChildren() != null)
		{
			for (Work w: aWork.getChildren())
			{
				sz += total(w);
			}
		}
		return sz;
	}


	void setPanel(WorkStatusPanel aPanel)
	{
		mPanel = aPanel;
	}


	ArrayList<Work> getWork()
	{
		return mRoot.getChildren();
	}


	@Override
	public synchronized void writeExternal(ObjectOutput aOut) throws IOException
	{
		aOut.writeObject(mRoot);
	}


	@Override
	public synchronized void readExternal(ObjectInput aIn) throws IOException, ClassNotFoundException
	{
		mRoot = (Work)aIn.readObject();
	}
}
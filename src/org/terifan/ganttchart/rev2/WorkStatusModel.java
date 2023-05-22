package org.terifan.ganttchart.rev2;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


public class WorkStatusModel implements Externalizable
{
	private final static long serialVersionUID = 1L;

	private Work mWork;

	private transient WorkStatusPanel mPanel;


	public WorkStatusModel()
	{
		mWork = new Work("root");
	}


	public Work start()
	{
		StackTraceElement stack = new Throwable().getStackTrace()[1];
		String classPath = stack.getClassName();
		String className = classPath.substring(classPath.lastIndexOf('.') + 1);

		Work work = mWork.start(stack.getMethodName() + ":" + className);

		mWork.cleanUp();

		if (mPanel != null)
		{
			mPanel.startRepaintTimer();
		}

		return work;
	}


	public Work start(Object aLabel)
	{
		Work work = mWork.start(aLabel);

		mWork.cleanUp();

		if (mPanel != null)
		{
			mPanel.startRepaintTimer();
		}

		return work;
	}


	public Work detail(Object aLabel)
	{
		return mWork.detail(aLabel);
	}


	public synchronized PendingWork pending(Object aLabel)
	{
		return new PendingWork(new Work(aLabel));
	}


	public synchronized int size()
	{
		return mWork.size();
	}


	void setPanel(WorkStatusPanel aPanel)
	{
		mPanel = aPanel;
	}


	Work getWork()
	{
		return mWork;
	}


//	public boolean hasWork()
//	{
//		Mutable<Boolean> state = new Mutable<>(false);
//		mWork.visit((aWork, aIndent) -> {state.value |= aWork.getEndTime() == 0; return state.value ? AbortOption.ABORT : AbortOption.CONTINUE;});
//		return state.value;
//	}


	@Override
	public synchronized void writeExternal(ObjectOutput aOut) throws IOException
	{
		aOut.writeObject(mWork);
	}


	@Override
	public synchronized void readExternal(ObjectInput aIn) throws IOException, ClassNotFoundException
	{
		mWork = (Work)aIn.readObject();
	}
}
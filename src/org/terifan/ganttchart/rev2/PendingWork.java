package org.terifan.ganttchart.rev2;


public class PendingWork implements AutoCloseable
{
	private Work mWork;


	PendingWork(Work aWork)
	{
		mWork = aWork;
	}


	public Work start()
	{
		return mWork.startSelf();
	}


	public Work fail()
	{
		return mWork.fail();
	}


	public Work abort()
	{
		return mWork.abort();
	}


	public Work success()
	{
		return mWork.success();
	}


	public Work detail(Object aValue)
	{
		return mWork.detail(aValue);
	}


	public Work detail(String aFormat, Object... aValues)
	{
		return mWork.detail(aFormat, aValues);
	}


	public PendingWork pending(Object aLabel)
	{
		return new PendingWork(mWork.add(new Work(aLabel)));
	}


	@Override
	public void close() throws Exception
	{
		mWork.close();
	}
}

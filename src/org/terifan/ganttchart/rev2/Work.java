package org.terifan.ganttchart.rev2;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;


public class Work implements AutoCloseable, Externalizable
{
	private final static long serialVersionUID = 1L;

	private final static AtomicLong COUNTER = new AtomicLong(new Random().nextLong());

	public enum Status
	{
		PENDING,
		RUNNING,
		/** Aborted work items were never run */
		ABORT,
		/** Finished work items were closed without a status, assumed to be okay */
		FINISH,
		FAIL,
		SUCCESS
	}

	private long mId;
	private ArrayList<Work> mChildren;
	private String mLabel;
	private String mValue;
	private String mBody;
	private String mSourceClass;
	private String mSourceMethod;
	private Status mStatus;
	private long mStartTime;
	private long mEndTime;
	private boolean mDetail;
	private byte mColor;
	private boolean mStage;


	public Work()
	{
		mId = COUNTER.getAndIncrement();
		mLabel = "";
		mBody = "";
		mValue = "";
		mStatus = Status.PENDING;
		mSourceClass = "";
		mSourceMethod = "";

		StackTraceElement[] elements = new Throwable().getStackTrace();
		for (int i = 0; i < elements.length; i++)
		{
			StackTraceElement stack = elements[i];
			if (!stack.getClassName().equals(Work.class.getCanonicalName()))
			{
				String classPath = stack.getClassName();
				setSourceClass(classPath.substring(classPath.lastIndexOf('.') + 1));
				setSourceMethod(stack.getMethodName());
				break;
			}
		}
	}


	Work(Object aLabel)
	{
		this();

		mLabel = nullToEmpty(formatException(aLabel));
	}


	public long getId()
	{
		return mId;
	}


	ArrayList<Work> getChildren()
	{
		return mChildren;
	}


	public String getLabel()
	{
		return mLabel;
	}


	public Work setLabel(Object aLabel)
	{
		mLabel = nullToEmpty(formatException(aLabel));
		return this;
	}


	public String getSourceClass()
	{
		return mSourceClass;
	}


	/**
	 * note: the <i>start</i> and <i>stage</i> methods will automatically set the source location.
	 */
	public Work setSourceClass(String aSourceClass)
	{
		mSourceClass = aSourceClass;
		return this;
	}


	public String getSourceMethod()
	{
		return mSourceMethod;
	}


	/**
	 * note: the <i>start</i> and <i>stage</i> methods will automatically set the source location.
	 */
	public Work setSourceMethod(String aSourceMethod)
	{
		mSourceMethod = aSourceMethod;
		return this;
	}


	public long getStartTime()
	{
		return mStartTime;
	}


	public long getEndTime()
	{
		return mEndTime;
	}


	public String getValue()
	{
		return mValue;
	}


	public Work setValue(Object aValue)
	{
		mValue = nullToEmpty(formatException(aValue));
		return this;
	}


	public String getBody()
	{
		return mBody;
	}


	public Work setBody(Object aBody)
	{
		mBody = nullToEmpty(formatException(aBody));
		return this;
	}


	public Work setBody(String aFormat, Object... aParams)
	{
		for (int i = 0; i < aParams.length; i++)
		{
			aParams[i] = formatException(aParams[i]);
		}

		mBody = String.format(aFormat, aParams);

		return this;
	}


	/**
	 * Creates, attaches and return a Work item.
	 */
	public Work start()
	{
		return start("");
	}


	/**
	 * Creates, attaches and return a Work item.
	 */
	public Work start(Object aLabel)
	{
		Work work = new Work(aLabel);
		work.mStartTime = System.currentTimeMillis();
		if (work.mStatus == Status.PENDING)
		{
			work.mStatus = Status.RUNNING;
		}
		add(work);
		return work;
	}


	/**
	 * Creates, attaches and return a stage Work item.
	 */
	public Work stage(Object aLabel)
	{
		Work work = new Work(aLabel);
		work.mStartTime = System.currentTimeMillis();
		work.mStage = true;
		work.mColor = (byte)255;
		if (work.mStatus == Status.PENDING)
		{
			work.mStatus = Status.RUNNING;
		}
		add(work);
		return work;
	}


	Work startSelf()
	{
		mStartTime = System.currentTimeMillis();
		if (mStatus == Status.PENDING)
		{
			mStatus = Status.RUNNING;
		}
		return this;
	}


	public Work setColor(int aColor)
	{
		if (aColor < 0 || aColor > 255)
		{
			throw new IllegalArgumentException();
		}
		mColor = (byte)aColor;
		return this;
	}


	public int getColor()
	{
		return 0xff & mColor;
	}


	/**
	 * sets the status to ABORT
	 */
	public Work abort()
	{
		return setStatus(Status.ABORT);
	}


	/**
	 * sets the status to FINISH
	 */
	public Work finish()
	{
		return setStatus(Status.FINISH);
	}


	/**
	 * sets the status to FAIL
	 */
	public Work fail()
	{
		return setStatus(Status.FAIL);
	}


	/**
	 * sets the status to SUCCESS
	 */
	public Work success()
	{
		return setStatus(Status.SUCCESS);
	}


	public boolean isStage()
	{
		return mStage;
	}


	public boolean isDetail()
	{
		return mDetail;
	}


	/**
	 * Creates, attaches and return a detail work item to this Work
	 */
	public Work detail(Object aValue)
	{
		String s = aValue == null ? "" : formatException(aValue).toString();

		try (Work work = add(new Work(s)))
		{
			work.mStartTime = System.currentTimeMillis();
			if (work.mStatus == Status.PENDING)
			{
				work.mStatus = Status.RUNNING;
			}
			work.mDetail = true;

			return work;
		}
	}


	/**
	 * Creates, attaches and return a detail work item to this Work
	 */
	public Work detail(String aFormat, Object... aParams)
	{
		for (int i = 0; i < aParams.length; i++)
		{
			aParams[i] = formatException(aParams[i]);
		}

		return detail(String.format(aFormat, aParams));
	}


	public Status getStatus()
	{
		return mStatus;
	}


	/**
	 * Sets either Status.SUCCESS or Status.FAILED depending on the status provided
	 */
	public Work setStatus(boolean aStatus)
	{
		return setStatus(aStatus ? Status.SUCCESS : Status.FAIL);
	}


	public Work setStatus(Status aStatus)
	{
		if (mEndTime == 0)
		{
			mEndTime = System.currentTimeMillis();
			mStatus = aStatus;

			if (mStartTime == 0)
			{
				mStartTime = mEndTime;
			}
		}
		else if (mDetail)
		{
			mStatus = aStatus;
		}
		else
		{
			System.err.println("Work status already set, ignoring status change");
		}
		return this;
	}


	synchronized Work add(Work aWork)
	{
		if (mChildren == null)
		{
			mChildren = new ArrayList<>();
		}

		mChildren.add(aWork);

		return aWork;
	}


	/**
	 * Return a PendingWork item. Once started the PendingWork item will create and attach Work item to this Work item.
	 */
	public PendingWork pending(Object aLabel)
	{
		return new PendingWork(add(new Work(aLabel)));
	}


	/**
	 * Create a batch of PendingWork items.
	 *
	 * @param aCount number of items to create
	 * @param aLabelProvider function providing the label for PendingWork
	 * @return an array of PendingWork items
	 */
	public PendingWork[] pending(int aCount, Function<Integer, Object> aLabelProvider)
	{
		PendingWork[] pw = new PendingWork[aCount];

		for (int i = 0; i < aCount; i++)
		{
			pw[i] = new PendingWork(add(new Work(aLabelProvider.apply(i))));
		}

		return pw;
	}


	public int size()
	{
		if (mChildren == null)
		{
			return 0;
		}
		return mChildren.size();
	}


	@Override
	public String toString()
	{
		return "Work{" + "mId=" + mId + ", mChildren=" + (mChildren == null ? 0 : mChildren.size()) + ", mLabel=" + mLabel + ", mValue=" + mValue + ", mBody=" + mBody + ", mStatus=" + mStatus + ", mStartTime=" + mStartTime + ", mEndTime=" + mEndTime + ", mDetail=" + mDetail + ", mColor=" + mColor + '}';
	}


	/**
	 * Update the status of this Work and all it's children who are still pending or running.
	 */
	@Override
	public void close()
	{
		if (mEndTime == 0)
		{
			mEndTime = System.currentTimeMillis();
		}
		if (mStartTime == 0)
		{
			mStartTime = mEndTime;
			mStatus = lastChildStatus(Status.ABORT);
		}
		if (mStatus == Status.PENDING)
		{
			mStatus = lastChildStatus(Status.ABORT);
		}
		else if (mStatus == Status.RUNNING)
		{
			mStatus = lastChildStatus(Status.FINISH);
		}
	}


	private Status lastChildStatus(Status aLast)
	{
		if (mChildren != null)
		{
			for (Work child : mChildren)
			{
				switch (child.getStatus())
				{
					case PENDING:
					case RUNNING:
						child.close();
						break;
				}
				switch (child.getStatus())
				{
					case ABORT:
					case PENDING:
					case RUNNING:
						break;
					default:
						aLast = child.getStatus();
						break;
				}
			}
		}
		return aLast;
	}


	@Override
	public synchronized void writeExternal(ObjectOutput aOut) throws IOException
	{
		aOut.writeLong(mId);
		aOut.writeByte(mStatus.ordinal());
		aOut.writeLong(mStartTime);
		aOut.writeLong(mEndTime);
		aOut.writeUTF(mSourceClass);
		aOut.writeUTF(mSourceMethod);
		aOut.writeBoolean(mDetail);
		aOut.writeBoolean(mStage);
		aOut.writeByte(mColor);
		aOut.writeUTF(mLabel);
		aOut.writeUTF(mBody);
		aOut.writeUTF(mValue);
		aOut.writeInt(mChildren == null ? 0 : mChildren.size());

		if (mChildren != null)
		{
			for (Work child : mChildren)
			{
				child.writeExternal(aOut);
			}
		}
	}


	@Override
	public synchronized void readExternal(ObjectInput aIn) throws IOException
	{
		mId = aIn.readLong();
		mStatus = Status.values()[aIn.readByte()];
		mStartTime = aIn.readLong();
		mEndTime = aIn.readLong();
		mSourceClass = aIn.readUTF();
		mSourceMethod = aIn.readUTF();
		mDetail = aIn.readBoolean();
		mStage = aIn.readBoolean();
		mColor = aIn.readByte();
		mLabel = aIn.readUTF();
		mBody = aIn.readUTF();
		mValue = aIn.readUTF();
		int count = aIn.readInt();

		if (count > 0)
		{
			mChildren = new ArrayList<>();
			for (int i = 0; i < count; i++)
			{
				Work child = new Work();
				child.readExternal(aIn);
				mChildren.add(child);
			}
		}
	}


	@Override
	public int hashCode()
	{
		return Long.hashCode(mId);
	}


	@Override
	public boolean equals(Object aObj)
	{
		if (aObj instanceof Work)
		{
			return ((Work)aObj).mId == mId;
		}
		return false;
	}


	long getTotalTime()
	{
		if (mEndTime == 0)
		{
			return 0;
		}

		long t = mEndTime - mStartTime;

		if (mChildren != null)
		{
			for (Work child : mChildren)
			{
				t -= child.getTotalTime();
			}
		}

		return t;
	}


	long getMaxEndTime()
	{
		long endTime = 0;
		if (getChildren() != null)
		{
			for (Work work : getChildren())
			{
				endTime = Math.max(endTime, work.getEndTime());
			}
		}
		return endTime;
	}


	long getMinStartTime()
	{
		long startTime = mStartTime;
		if (getChildren() != null)
		{
			for (Work work : getChildren())
			{
				if (startTime == 0 || work.getStartTime() > 0 && work.getStartTime() < startTime)
				{
					startTime = work.getMinStartTime();
				}
			}
		}
		return startTime;
	}


	public void print()
	{
		System.out.println("work:");
		print(1);
	}


	private void print(int aIndent)
	{
		String gray = "\033[0;100m";
		String white = "\033[0;37m";
		String reset = "\033[0m";
		String cyan = "\033[0;36m";

		String indent = cyan + "... ".repeat(aIndent) + reset;
		System.out.println(indent + gray + "location: " + white + mSourceClass + "." + mSourceMethod + reset);
		System.out.println(indent + gray + "status: " + white + mStatus + reset);
		if (!mLabel.isEmpty())
		{
			System.out.println(indent + gray + "label: " + white + mLabel + reset);
		}
		if (!mBody.isEmpty())
		{
			System.out.println(indent + gray + "body: " + white + mBody + reset);
		}
		if (!mValue.isEmpty())
		{
			System.out.println(indent + gray + "value: " + white + mValue + reset);
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-DD HH:mm:ss");
		System.out.println(indent + gray + "time: " + white + df.format(mStartTime) + reset + " .. " + white + df.format(mEndTime) + reset);

		if (mChildren != null && !mChildren.isEmpty())
		{
			System.out.println(indent + gray + "children:" + reset);
			for (Work w : mChildren)
			{
				w.print(aIndent + 1);
			}
		}
	}


	public static Object formatException(Object aThrowable)
	{
		if (aThrowable instanceof Throwable)
		{
			StringWriter w = new StringWriter();
			((Throwable)aThrowable).printStackTrace(new PrintWriter(w, true));
			return w.toString();
		}
		return aThrowable;
	}


	public static String nullToEmpty(Object aObject)
	{
		return aObject == null ? "" : aObject.toString();
	}


//	public void nextPending()
//	{
//		for (Work work : mChildren)
//		{
//			if (work instanceof PendingWork)
//			{
//			}
//		}
//	}
}

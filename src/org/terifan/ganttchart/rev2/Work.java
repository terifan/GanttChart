package org.terifan.ganttchart.rev2;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.function.Function;
import static org.terifan.ganttchart.rev2.Work.Status.PENDING;


public class Work implements Externalizable, AutoCloseable
{
	private final static long serialVersionUID = 1L;

	private final static SecureRandom RND = new SecureRandom();


	public enum Status
	{
		RUNNING,
		FAILED,
		ABORTED,
		PENDING,
		FINISH,
		SUCCESS,
		UNKNOWN
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


	public Work()
	{
		mId = RND.nextLong();
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


	public Work setSourceClass(String aSourceClass)
	{
		mSourceClass = aSourceClass;
		return this;
	}


	public String getSourceMethod()
	{
		return mSourceMethod;
	}


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


	public Work start()
	{
		return start("");
	}


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


	public Work abort()
	{
		return setStatus(Status.ABORTED);
	}


	public Work finish()
	{
		return setStatus(Status.FINISH);
	}


	public Work fail()
	{
		return setStatus(Status.FAILED);
	}


	public Work success()
	{
		return setStatus(Status.SUCCESS);
	}


	public boolean isDetail()
	{
		return mDetail;
	}


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
		return setStatus(aStatus ? Status.SUCCESS : Status.FAILED);
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


	public PendingWork pending(Object aLabel)
	{
		return new PendingWork(add(new Work(aLabel)));
	}


	/**
	 * Create a batch of pending work items.
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


	public String toInfoString()
	{
		return mLabel + ", " + mValue + ", " + mBody + ", " + mDetail + ", " + mChildren;
	}


	@Override
	public String toString()
	{
		return "Work{" + "mId=" + mId + ", mChildren=" + mChildren + ", mLabel=" + mLabel + ", mValue=" + mValue + ", mBody=" + mBody + ", mStatus=" + mStatus + ", mStartTime=" + mStartTime + ", mEndTime=" + mEndTime + ", mDetail=" + mDetail + ", mColor=" + mColor + '}';
	}


	@Override
	public void close()
	{
		if (mStartTime == 0)
		{
			throw new IllegalStateException("Work was not properly started!");
		}
		if (mEndTime == 0)
		{
			mEndTime = System.currentTimeMillis();
		}
		if (mStatus == Status.PENDING)
		{
			mStatus = Status.ABORTED;
		}
		if (mStatus == Status.RUNNING)
		{
			Status s = Status.UNKNOWN;
			boolean hasSuccess = false;
			boolean hasFail = false;
			if (mChildren != null)
			{
				for (Work c : mChildren)
				{
					if (s == Status.UNKNOWN)
					{
						if (c.mStatus == Status.ABORTED)
						{
							s = Status.ABORTED;
						}
						if (c.mStatus == Status.FAILED)
						{
							hasFail = true;
						}
						if (c.mStatus == Status.SUCCESS)
						{
							hasSuccess = true;
						}
					}
					else if (c.mStatus == Status.PENDING)
					{
						c.mStatus = Status.ABORTED;
					}
				}
				if (hasSuccess)
				{
					s = Status.SUCCESS;
				}
				else if (hasFail)
				{
					s = Status.FAILED;
				}
			}
			mStatus = s;
		}
	}


	@Override
	public synchronized void writeExternal(ObjectOutput aOut) throws IOException
	{
		aOut.writeLong(mId);
		aOut.writeUTF(mLabel);
		aOut.writeLong(mStartTime);
		aOut.writeLong(mEndTime);
		aOut.writeUTF(mBody);
		aOut.writeUTF(mValue);
		aOut.writeUTF(mSourceClass);
		aOut.writeUTF(mSourceMethod);
		aOut.writeByte(mStatus.ordinal());
		aOut.writeBoolean(mDetail);
		aOut.writeByte(mColor);
		aOut.writeShort(mChildren == null ? 0 : mChildren.size());

		if (mChildren != null)
		{
			for (Work w : mChildren)
			{
				w.writeExternal(aOut);
			}
		}
	}


	@Override
	public synchronized void readExternal(ObjectInput aIn) throws IOException
	{
		mId = aIn.readLong();
		mLabel = aIn.readUTF();
		mStartTime = aIn.readLong();
		mEndTime = aIn.readLong();
		mBody = aIn.readUTF();
		mValue = aIn.readUTF();
		mSourceClass = aIn.readUTF();
		mSourceMethod = aIn.readUTF();
		mStatus = Status.values()[aIn.readByte()];
		mDetail = aIn.readBoolean();
		mColor = aIn.readByte();
		int count = aIn.readShort();

		if (count > 0)
		{
			mChildren = new ArrayList<>();
			for (int i = 0; i < count; i++)
			{
				Work w = new Work();
				w.readExternal(aIn);
				mChildren.add(w);
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

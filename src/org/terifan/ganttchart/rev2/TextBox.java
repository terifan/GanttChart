package org.terifan.ganttchart.rev2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.io.Serializable;
import java.util.ArrayList;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.Utilities;



public class TextBox implements Cloneable, Serializable
{
	private static final long serialVersionUID = 1L;
	private final static Insets ZERO_INSETS = new Insets(0,0,0,0);
	private static char[] DEFAULT_BREAK_CHARS = {' ', '.', ',', '-', '_', ':', ';', '?', '!'};

	protected final Insets mMargins;
	protected final Insets mPadding;
	protected final Rectangle mBounds;
	protected ArrayList<TextSegment> mTextLines;
	protected ArrayList<Rectangle> mTextBounds;
	protected StringBuilder mText;
	protected Font mFont;
	protected Color mForeground;
	protected Color mBackground;
	protected Color mHighlight;
	protected Border mBorder;
	protected Border mTextBorder;
	protected Anchor mAnchor;
	protected int mLineSpacing;
	protected int mMaxLineCount;
	protected char[] mBreakChars;
	protected int mMaxWidth;
	protected int mMinWidth;
	protected String mSuffix;
	protected boolean mDirty;
	protected Color mShadowColor;
	protected Point mShadowOffset;
	protected TextRenderCallback mRenderCallback;
	private boolean mBackgroundImageSurroundText;
	private boolean mMirrorShadow;
	private Color mShadowMirrorColor;

	enum Anchor
	{
		NORTH_WEST,
		NORTH,
		NORTH_EAST,
		CENTER,
		SOUTH_WEST,
		SOUTH,
		SOUTH_EAST,
		WEST,
		EAST;
	}

	public TextBox()
	{
		this("");
	}


	public TextBox(String aText)
	{
		mText = new StringBuilder(aText);
		mBounds = new Rectangle();
		mMargins = new Insets(0, 0, 0, 0);
		mPadding = new Insets(0, 0, 0, 0);
		mForeground = Color.BLACK;
		mAnchor = Anchor.NORTH_WEST;
		mFont = UIManager.getDefaults().getFont("TextField.font");
		mBreakChars = DEFAULT_BREAK_CHARS;
		mDirty = true;
	}


	public synchronized String getText()
	{
		return mText.toString();
	}


	public synchronized TextBox setText(Object aText)
	{
		if (aText != null)
		{
			mText.setLength(0);
			mText.append(aText);
			mDirty = true;
		}
		return this;
	}


	public synchronized TextBox append(Object aText)
	{
		if (aText != null)
		{
			mText.append(aText);
			mDirty = true;
		}
		return this;
	}


	public synchronized TextBox appendLine(Object aText)
	{
		if (aText != null)
		{
			mText.append(aText).append("\n");
			mDirty = true;
		}
		return this;
	}


	public TextRenderCallback getRenderCallback()
	{
		return mRenderCallback;
	}


	public TextBox setRenderCallback(TextRenderCallback aRenderCallback)
	{
		mRenderCallback = aRenderCallback;
		return this;
	}


	public Font getFont()
	{
		return mFont;
	}


	public TextBox setFont(Font aFont)
	{
		if (aFont == null)
		{
			throw new IllegalArgumentException("aFont is null");
		}

		mFont = aFont;
		mDirty = true;
		return this;
	}


	public Color getForeground()
	{
		return mForeground;
	}


	public TextBox setForeground(Color aForeground)
	{
		if (aForeground == null)
		{
			throw new IllegalArgumentException("aForeground is null");
		}

		mForeground = aForeground;
		return this;
	}


	public Color getBackground()
	{
		return mBackground;
	}


	public TextBox setBackground(Color aBackground)
	{
		mBackground = aBackground;
		return this;
	}


	public TextBox setBackgroundSurroundText(boolean aBackgroundSurroundText)
	{
		mBackgroundImageSurroundText = aBackgroundSurroundText;
		return this;
	}


	public Color getHighlight()
	{
		return mHighlight;
	}


	public TextBox setHighlight(Color aHighlight)
	{
		mHighlight = aHighlight;
		return this;
	}


	public Insets getMargins()
	{
		return mMargins;
	}


	public TextBox setMargins(Insets aMargins)
	{
		if (aMargins == null)
		{
			throw new IllegalArgumentException("aMargins is null");
		}

		return setMargins(aMargins.top, aMargins.left, aMargins.bottom, aMargins.right);
	}


	public TextBox setMargins(int aTop, int aLeft, int Bottom, int aRight)
	{
		mMargins.set(aTop, aLeft, Bottom, aRight);
		mDirty = true;
		return this;
	}


	public Insets getPadding()
	{
		return mPadding;
	}


	public TextBox setPadding(Insets aPadding)
	{
		if (aPadding == null)
		{
			throw new IllegalArgumentException("aPadding is null");
		}

		return setPadding(aPadding.top, aPadding.left, aPadding.bottom, aPadding.right);
	}


	public TextBox setPadding(int aTop, int aLeft, int Bottom, int aRight)
	{
		mPadding.set(aTop, aLeft, Bottom, aRight);
		mDirty = true;
		return this;
	}


	public TextBox setPaddingTop(int aTop)
	{
		mPadding.top = aTop;
		mDirty = true;
		return this;
	}


	public TextBox setPaddingLeft(int aLeft)
	{
		mPadding.left = aLeft;
		mDirty = true;
		return this;
	}


	public TextBox setPaddingBottom(int aBottom)
	{
		mPadding.bottom = aBottom;
		mDirty = true;
		return this;
	}


	public TextBox setPaddingRight(int aRight)
	{
		mPadding.right = aRight;
		mDirty = true;
		return this;
	}


	public Border getBorder()
	{
		return mBorder;
	}


	public TextBox setBorder(Border aBorder)
	{
		mBorder = aBorder;
		mDirty = true;
		return this;
	}


	public Border getTextBorder()
	{
		return mTextBorder;
	}


	public TextBox setTextBorder(Border aBorder)
	{
		mTextBorder = aBorder;
		mDirty = true;
		return this;
	}


	public Rectangle getBounds()
	{
		return mBounds;
	}


	public TextBox setBounds(Rectangle aBounds)
	{
		if (aBounds == null)
		{
			throw new IllegalArgumentException("aBounds is null");
		}

		mBounds.setBounds(aBounds);
		mDirty = true;
		return this;
	}


	public TextBox setBounds(int aOffsetX, int aOffsetY, int aWidth, int aHeight)
	{
		mBounds.setBounds(aOffsetX, aOffsetY, aWidth, aHeight);
		mDirty = true;
		return this;
	}


	public TextBox setDimensions(Dimension aDimension)
	{
		if (aDimension == null)
		{
			throw new IllegalArgumentException("aDimension is null");
		}

		mBounds.setSize(aDimension);
		mDirty = true;
		return this;
	}


	public TextBox setDimensions(int aWidth, int aHeight)
	{
		mBounds.setSize(aWidth, aHeight);
		mDirty = true;
		return this;
	}


	public Anchor getAnchor()
	{
		return mAnchor;
	}


	public TextBox setAnchor(Anchor aAnchor)
	{
		if (aAnchor == null)
		{
			throw new IllegalArgumentException("aAnchor is null");
		}

		mAnchor = aAnchor;
		mDirty = true;
		return this;
	}


	public int getLineSpacing()
	{
		return mLineSpacing;
	}


	public TextBox setLineSpacing(int aLineSpacing)
	{
		mLineSpacing = aLineSpacing;
		mDirty = true;
		return this;
	}


	public int getMaxLineCount()
	{
		return mMaxLineCount;
	}


	public TextBox setMaxLineCount(int aLineCount)
	{
		mMaxLineCount = aLineCount;
		mDirty = true;
		return this;
	}


	public TextBox translate(int aDeltaX, int aDeltaY)
	{
		mBounds.translate(aDeltaX, aDeltaY);
		mDirty = true;
		return this;
	}


	public int getWidth()
	{
		return mBounds.width;
	}


	public TextBox setWidth(int aWidth)
	{
		mBounds.width = aWidth;
		mDirty = true;
		return this;
	}


	public int getHeight()
	{
		return mBounds.height;
	}


	public TextBox setHeight(int aHeight)
	{
		mBounds.height = aHeight;
		mDirty = true;
		return this;
	}


	public int getX()
	{
		return mBounds.x;
	}


	public TextBox setX(int aOffsetX)
	{
		mBounds.x = aOffsetX;
		mDirty = true;
		return this;
	}


	public int getY()
	{
		return mBounds.y;
	}


	public TextBox setY(int aOffsetY)
	{
		mBounds.y = aOffsetY;
		mDirty = true;
		return this;
	}


	public TextBox pack()
	{
		setBounds(measure());
		mDirty = false;
		return this;
	}


	public TextBox setBreakChars(char[] aBreakChars)
	{
		if (aBreakChars == null)
		{
			aBreakChars = new char[0];
		}
		mBreakChars = aBreakChars.clone();
		return this;
	}


	public char[] getBreakChars()
	{
		return mBreakChars.clone();
	}


	public boolean isLayoutRequired()
	{
		return mDirty;
	}


	public Rectangle measure()
	{
		return measure(new FontRenderContext(null, true, false));
	}


	public Rectangle measure(FontRenderContext aFontRenderContext)
	{
		if (aFontRenderContext == null)
		{
			return measure();
		}
//		if (mBounds.isEmpty())
		{
			mBounds.setSize(Short.MAX_VALUE, Short.MAX_VALUE);
		}
		if (mMaxWidth > 0)
		{
			mBounds.width = Math.min(mBounds.width, mMaxWidth);
		}
		if (mMinWidth > 0)
		{
			mBounds.width = Math.max(mBounds.width, mMinWidth);
		}

		if (mDirty)
		{
			layout(aFontRenderContext);
		}

		if (mTextBounds.isEmpty())
		{
			return new Rectangle();
		}

		Rectangle bounds = new Rectangle(mTextBounds.get(0));
		for (Rectangle r : mTextBounds)
		{
			bounds.add(r);
		}

		if (mBorder != null)
		{
			Insets bi = mBorder.getBorderInsets(null);
			bounds.x -= bi.left;
			bounds.y -= bi.top;
			bounds.width += bi.left + bi.right;
			bounds.height += bi.top + bi.bottom;
		}

		if (mTextBorder != null)
		{
			Insets bi = mTextBorder.getBorderInsets(null);
			bounds.x -= bi.left;
			bounds.y -= bi.top;
			bounds.width += bi.left + bi.right;
			bounds.height += bi.top + bi.bottom;
		}

		bounds.x -= mMargins.left;
		bounds.y -= mMargins.top;
		bounds.width += mMargins.left + mMargins.right;
		bounds.height += mMargins.top + mMargins.bottom;

//		bounds.width += mPadding.left + mPadding.right;
//		bounds.height += mPadding.top + mPadding.bottom;

		return bounds;
	}


	public TextBox updateBounds()
	{
		return updateBounds(new FontRenderContext(null, true, false));
	}


	public TextBox updateBounds(FontRenderContext aFontRenderContext)
	{
		Rectangle bounds = measure(aFontRenderContext);
		setBounds(bounds);
		return this;
	}


	public TextBox render(Graphics aGraphics)
	{
		return render(aGraphics, 0, 0);
	}


	public TextBox render(Graphics aGraphics, boolean aAntialiase)
	{
		if (aAntialiase)
		{
			((Graphics2D)aGraphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		}

		return render(aGraphics, 0, 0);
	}


	public TextBox render(Graphics aGraphics, int aTranslateX, int aTranslateY)
	{
		boolean hasShadow = mShadowColor != null;

		if (mShadowColor != null)
		{
			renderImpl(aGraphics, aTranslateX, aTranslateY, hasShadow, true);
		}

		return renderImpl(aGraphics, aTranslateX, aTranslateY, hasShadow, false);
	}


	protected TextBox renderImpl(Graphics aGraphics, int aTranslateX, int aTranslateY, boolean aHasShadow, boolean aShadow)
	{
		if (mDirty)
		{
			layout(aGraphics.getFontMetrics().getFontRenderContext());
		}

		Font oldFont = aGraphics.getFont();
		Color oldColor = aGraphics.getColor();

		aGraphics.translate(aTranslateX, aTranslateY);

		int boxX = mBounds.x + mMargins.left;
		int boxY = mBounds.y + mMargins.top;
		int boxW = mBounds.width - mMargins.left - mMargins.right;
		int boxH = mBounds.height - mMargins.top - mMargins.bottom;

		if (mBorder != null)
		{
			mBorder.paintBorder(null, aGraphics, boxX, boxY, boxW, boxH);

			Insets bi = mBorder.getBorderInsets(null);
			boxX += bi.left;
			boxY += bi.top;
			boxW -= bi.left + bi.right;
			boxH -= bi.top + bi.bottom;
		}

		if (mBackground != null && (aShadow || !aHasShadow))
		{
			aGraphics.setColor(mBackground);
			aGraphics.fillRect(boxX, boxY, boxW, boxH);
		}

		Insets ti = mTextBorder != null ? mTextBorder.getBorderInsets(null) : ZERO_INSETS;
		LineMetrics lm = mFont.getLineMetrics("Adgj", aGraphics.getFontMetrics().getFontRenderContext());

		aGraphics.setColor(mForeground);
		aGraphics.setFont(mFont);

		for (int i = 0, sz = mTextBounds.size(); i < sz; i++)
		{
			Rectangle r = mTextBounds.get(i);

			if (mTextBorder != null)
			{
				mTextBorder.paintBorder(null, aGraphics, r.x - ti.left, r.y - ti.top, r.width + ti.left + ti.right, r.height + ti.top + ti.bottom);
			}

			drawSingleLine(aGraphics, mTextLines.get(i), lm, r.x, r.y, r.width, r.height, aHasShadow, aShadow);
		}

		aGraphics.translate(-aTranslateX, -aTranslateY);
		aGraphics.setFont(oldFont);
		aGraphics.setColor(oldColor);

		return this;
	}


	private synchronized void layout(FontRenderContext aFontRenderContext)
	{
		if (mText == null)
		{
			throw new IllegalStateException("Text is null");
		}
		if (mMaxWidth > 0)
		{
			mBounds.width = Math.min(mBounds.width, mMaxWidth);
		}
		if (mMinWidth > 0)
		{
			mBounds.width = Math.max(mBounds.width, mMinWidth);
		}

		layoutLines(aFontRenderContext);
		layoutBounds(aFontRenderContext);

		mDirty = false;
	}


	private void layoutBounds(FontRenderContext aFontRenderContext)
	{
		ArrayList<Rectangle> list = new ArrayList<>();

		int boxX = mBounds.x;
		int boxY = mBounds.y;
		int boxW = mBounds.width;
		int boxH = mBounds.height;

		if (mBorder != null)
		{
			Insets bi = mBorder.getBorderInsets(null);
			boxX += bi.left;
			boxY += bi.top;
			boxW -= bi.left + bi.right;
			boxH -= bi.top + bi.bottom;
		}

		boxX += mMargins.left;
		boxY += mMargins.top;
		boxW -= mMargins.left + mMargins.right;
		boxH -= mMargins.top + mMargins.bottom;

		int extraLineHeight = 0;
		if (mTextBorder != null)
		{
			Insets bi = mTextBorder.getBorderInsets(null);
			boxX += bi.left;
			boxY += bi.top;
			boxW -= bi.left + bi.right;
			boxH -= bi.top + bi.bottom;
			extraLineHeight = bi.top + bi.bottom;
		}

		LineMetrics lm = mFont.getLineMetrics("Adgjy", aFontRenderContext);
		int lineHeight = (int)lm.getHeight() + mPadding.top + mPadding.bottom;

		if (boxH < lineHeight)
		{
			boxH = lineHeight;
		}

		int lineHeightExtra = lineHeight + mLineSpacing + extraLineHeight;
		int boxHeightExtra = boxH + mLineSpacing + extraLineHeight;

		int lineY = boxY;
		int lineCount = Math.min(Math.min(mTextLines.size(), mMaxLineCount > 0 ? mMaxLineCount : Integer.MAX_VALUE), boxHeightExtra / lineHeightExtra);

		switch (mAnchor)
		{
			case SOUTH_EAST:
			case SOUTH:
			case SOUTH_WEST:
				lineY += Math.max(0, boxHeightExtra - lineCount * lineHeightExtra);
				break;
			case CENTER:
			case WEST:
			case EAST:
				lineY += Math.max(0, (boxHeightExtra - lineCount * lineHeightExtra) / 2);
				break;
		}

		for (int i = 0; i < lineCount; i++)
		{
			TextSegment str = mTextLines.get(i);

			int lineX = boxX;
			int lineW = getStringLength(aFontRenderContext, str.mText, mFont) + mPadding.left + mPadding.right;

			switch (mAnchor)
			{
				case NORTH:
				case CENTER:
				case SOUTH:
					lineX += (boxW - lineW) / 2;
					break;
				case NORTH_EAST:
				case EAST:
				case SOUTH_EAST:
					lineX += boxW - lineW;
					break;
			}

			list.add(new Rectangle(lineX, lineY, lineW, lineHeight));

			lineY += lineHeightExtra;
		}

		mTextBounds = list;
	}


	private void layoutLines(FontRenderContext aFontRenderContext)
	{
		ArrayList<TextSegment> list = new ArrayList<>();

		int boxW = mBounds.width - mMargins.left - mMargins.right;

		if (mBorder != null)
		{
			Insets bi = mBorder.getBorderInsets(null);
			boxW -= bi.left + bi.right;
		}
		if (mTextBorder != null)
		{
			Insets bi = mTextBorder.getBorderInsets(null);
			boxW -= bi.left + bi.right;
		}
		boxW -= mPadding.left + mPadding.right;

		if (boxW > 0)
		{
			int line = 0;

			for (String str : mText.toString().split("\n"))
			{
				do
				{
					boolean isLastLine = mMaxLineCount > 0 && list.size() >= mMaxLineCount - 1;
					int w = getStringLength(aFontRenderContext, str, mFont);
					String nextLine;

					String suffix = "";
					int tmpBoxW = boxW;
					if ((isLastLine || w < boxW) && mSuffix != null)
					{
						suffix = mSuffix;
						tmpBoxW -= getStringLength(aFontRenderContext, suffix, mFont);
					}

					if (w > tmpBoxW)
					{
						int offset = Math.max(findStringLimit(aFontRenderContext, str, tmpBoxW), 1);
						int temp = offset;

						outer: for (; temp > 1; temp--)
						{
							char c = str.charAt(temp - 1);
							for (char d : mBreakChars)
							{
								if (c == d)
								{
									break outer;
								}
							}
						}

						if (temp > 1)
						{
							offset = temp;
						}

						nextLine = str.substring(0, offset) + suffix;
						str = str.substring(offset).trim();
					}
					else
					{
						nextLine = str + suffix;
						str = "";
					}

					list.add(new TextSegment(line, trim(nextLine)));

					if (isLastLine)
					{
						break;
					}
				}
				while (str.length() > 0);

				if (mMaxLineCount > 0 && list.size() >= mMaxLineCount)
				{
					break;
				}

				line++;
			}
		}

		mTextLines = list;
	}


	private String trim(String aString)
	{
		int len = aString.length();
		for (; len > 0 && Character.isWhitespace(aString.charAt(len - 1)); len--)
		{
		}
		return len <= 0 ? "" : aString.substring(0, len);
	}


	private int findStringLimit(FontRenderContext aFontRenderContext, String aString, int aWidth)
	{
		int min = 0;
		int max = aString.length();

		while (Math.abs(min - max) > 1)
		{
			int mid = (max + min) / 2;

			int w = getStringLength(aFontRenderContext, aString.substring(0, mid), mFont);

			if (w > aWidth)
			{
				max = mid;
			}
			else
			{
				min = mid;
			}
		}

		return min;
	}


	private int getStringLength(FontRenderContext aFontRenderContext, String aString, Font aFont)
	{
		return (int)Math.ceil(aFont.getStringBounds(aString, aFontRenderContext).getWidth());
	}


	private void drawSingleLine(Graphics aGraphics, TextSegment aText, LineMetrics aLineMetrics, int aOffsetX, int aOffsetY, int aWidth, int aHeight, boolean aHasShadow, boolean aShadow)
	{
		if (mHighlight != null && (aShadow || !aHasShadow))
		{
			aGraphics.setColor(mHighlight);
			aGraphics.fillRect(aOffsetX, aOffsetY, aWidth, aHeight);
			aGraphics.setColor(mForeground);
		}
		else if (aShadow)
		{
			aGraphics.setColor(mShadowColor);
		}

		int adjust = (int)(aLineMetrics.getHeight() - aLineMetrics.getDescent());

		int ix = aOffsetX + mPadding.left;
		int iy = aOffsetY + adjust + mPadding.top;
		if (aShadow)
		{
			ix += mShadowOffset.x;
			iy += mShadowOffset.y;
		}

		if (mRenderCallback != null)
		{
			mRenderCallback.beforeRender(aText, aOffsetX, aOffsetY, aWidth, aHeight, ix, iy);
		}

		aGraphics.drawString(aText.mText, ix, iy);

		if (aShadow && mMirrorShadow)
		{
			aGraphics.setColor(mShadowMirrorColor);
			aGraphics.drawString(aText.mText, ix - 2 * mShadowOffset.x, iy);
		}

		if (mRenderCallback != null)
		{
			mRenderCallback.afterRender(aText, aOffsetX, aOffsetY, aWidth, aHeight, ix, iy);
		}
	}


	public int getMinWidth()
	{
		return mMinWidth;
	}


	public TextBox setMinWidth(int aMinWidth)
	{
		mMinWidth = aMinWidth;
		mDirty = true;
		return this;
	}


	public int getMaxWidth()
	{
		return mMaxWidth;
	}


	public TextBox setMaxWidth(int aMaxWidth)
	{
		mMaxWidth = aMaxWidth;
		mDirty = true;
		return this;
	}


	public TextBox setShadow(Color aColor, int aX, int aY)
	{
		return setShadow(aColor, aX, aY, false);
	}


	public TextBox setShadow(Color aColor, int aX, int aY, boolean aMirrorShadow)
	{
		mShadowColor = aColor;
		mShadowMirrorColor = aColor;
		mShadowOffset = new Point(aX, aY);
		mMirrorShadow = aMirrorShadow;
		return this;
	}


	public TextBox setShadow(Color aColor, int aX, int aY, Color aMirrorColor)
	{
		mShadowColor = aColor;
		mShadowOffset = new Point(aX, aY);
		mMirrorShadow = aMirrorColor != null;
		mShadowMirrorColor = aMirrorColor;
		return this;
	}


	public Color getShadowColor()
	{
		return mShadowColor;
	}


	public String getSuffix()
	{
		return mSuffix;
	}


	public TextBox setSuffix(String aPrefix)
	{
		mSuffix = aPrefix;
		mDirty = true;
		return this;
	}


	public static class TextSegment implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private int mLine;
		private String mText;

		public TextSegment(int aLine, String aText)
		{
			mLine = aLine;
			mText = aText;
		}


		public int getLine()
		{
			return mLine;
		}


		public String getText()
		{
			return mText;
		}


		@Override
		public String toString()
		{
			return mText;
		}
	}


	public static interface TextRenderCallback
	{
		public void beforeRender(TextSegment aText, int aOffsetX, int aOffsetY, int aWidth, int aHeight, int aTextX, int aTextY);

		public void afterRender(TextSegment aText, int aOffsetX, int aOffsetY, int aWidth, int aHeight, int aTextX, int aTextY);
	}


	@Override
	public String toString()
	{
		return getText();
	}


	@Override
	public TextBox clone()
	{
		try
		{
			TextBox textBox = (TextBox)super.clone();
			textBox.mAnchor = this.mAnchor;
			textBox.mBackground = this.mBackground;
			textBox.mBorder = this.mBorder;
			textBox.mBounds.setBounds(this.mBounds);
			textBox.mBreakChars = this.mBreakChars == DEFAULT_BREAK_CHARS ? DEFAULT_BREAK_CHARS : this.mBreakChars.clone();
			textBox.mDirty = this.mDirty;
			textBox.mFont = this.mFont;
			textBox.mForeground = this.mForeground;
			textBox.mHighlight = this.mHighlight;
			textBox.mMaxLineCount = this.mMaxLineCount;
			textBox.mLineSpacing = this.mLineSpacing;
			textBox.mMargins.set(mMargins.top, mMargins.left, mMargins.bottom, mMargins.right);
			textBox.mPadding.set(mPadding.top, mPadding.left, mPadding.bottom, mPadding.right);
			textBox.mText = this.mText;
			textBox.mTextBorder = this.mTextBorder;
			textBox.mTextLines = this.mTextLines == null ? null : new ArrayList<>(this.mTextLines);
			textBox.mMaxWidth = this.mMaxWidth;
			textBox.mMinWidth = this.mMinWidth;
			textBox.mSuffix = this.mSuffix;

			return textBox;
		}
		catch (CloneNotSupportedException e)
		{
			throw new Error();
		}
	}


	public static void enableAntialiasing(Graphics aGraphics)
	{
		Graphics2D g = (Graphics2D)aGraphics;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}
}

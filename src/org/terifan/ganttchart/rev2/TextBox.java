package org.terifan.ganttchart.rev2;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.UIManager;
import javax.swing.border.Border;


class TextBox
{
	private final static Insets ZERO_INSETS = new Insets(0,0,0,0);
	private static char[] DEFAULT_BREAK_CHARS = {' ', '.', ',', '-', '_', ':', ';', '?', '!'};

	private final Insets mMargins;
	private final Insets mPadding;
	private final Rectangle mBounds;
	private ArrayList<String> mTextLines;
	private ArrayList<Rectangle> mTextBounds;
	private List<String> mText;
	private Font mFont;
	private Color mForeground;
	private Color mBackground;
	private Color mHighlight;
	private Border mBorder;
	private Border mTextBorder;
	private Anchor mAnchor;
	private int mLineSpacing;
	private int mMaxLineCount;
	private char[] mBreakChars;
	private int mMaxWidth;
	private int mMinWidth;
	private String mSuffix;
	private boolean mDirty;
	private Color mShadowColor;

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


	TextBox()
	{
		this("");
	}


	TextBox(String aText)
	{
		mBounds = new Rectangle();
		mMargins = new Insets(0, 0, 0, 0);
		mPadding = new Insets(0, 0, 0, 0);
		mForeground = Color.BLACK;
		mAnchor = Anchor.NORTH_WEST;
		mFont = UIManager.getDefaults().getFont("TextField.font");
		mBreakChars = DEFAULT_BREAK_CHARS;

		setText(aText);
	}


	synchronized TextBox setText(String aText)
	{
		if (aText == null)
		{
			throw new IllegalArgumentException("aText is null");
		}

		mText = Arrays.asList(aText.split("\n"));
		mDirty = true;
		return this;
	}


	TextBox setForeground(Color aForeground)
	{
		if (aForeground == null)
		{
			throw new IllegalArgumentException("aForeground is null");
		}

		mForeground = aForeground;
		return this;
	}


	TextBox setAnchor(Anchor aAnchor)
	{
		if (aAnchor == null)
		{
			throw new IllegalArgumentException("aAnchor is null");
		}

		mAnchor = aAnchor;
		mDirty = true;
		return this;
	}


	TextBox setMaxLineCount(int aLineCount)
	{
		mMaxLineCount = aLineCount;
		mDirty = true;
		return this;
	}


	void translate(int aDeltaX, int aDeltaY)
	{
		mBounds.translate(aDeltaX, aDeltaY);
		mDirty = true;
	}


	TextBox setWidth(int aWidth)
	{
		mBounds.width = aWidth;
		return this;
	}


	TextBox setHeight(int aHeight)
	{
		mBounds.height = aHeight;
		return this;
	}


	TextBox setX(int aOffsetX)
	{
		mBounds.x = aOffsetX;
		return this;
	}


	int getY()
	{
		return mBounds.y;
	}


	TextBox setBreakChars(char[] aBreakChars)
	{
		if (aBreakChars == null)
		{
			aBreakChars = new char[0];
		}
		mBreakChars = aBreakChars.clone();
		return this;
	}


	Rectangle measure()
	{
		return measure(new FontRenderContext(null, true, false));
	}


	Rectangle measure(FontRenderContext aFontRenderContext)
	{
		if (aFontRenderContext == null)
		{
			return measure();
		}
		if (mBounds.isEmpty())
		{
			mBounds.setBounds(0, 0, Short.MAX_VALUE, Short.MAX_VALUE);
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

		return bounds;
	}


	TextBox render(Graphics aGraphics)
	{
		return render(aGraphics, 0, 0);
	}


	TextBox render(Graphics aGraphics, int aTranslateX, int aTranslateY)
	{
		boolean hasShadow = mShadowColor != null;

		if (mShadowColor != null)
		{
			renderImpl(aGraphics, aTranslateX - 1, aTranslateY + 1, hasShadow, true);
		}
		return renderImpl(aGraphics, aTranslateX, aTranslateY, hasShadow, false);
	}


	private TextBox renderImpl(Graphics aGraphics, int aTranslateX, int aTranslateY, boolean aHasShadow, boolean aShadow)
	{
		if (mDirty)
		{
			layout(aGraphics.getFontMetrics().getFontRenderContext());
		}

		aGraphics.translate(aTranslateX, aTranslateY);

		int boxX = mBounds.x;
		int boxY = mBounds.y;
		int boxW = mBounds.width;
		int boxH = mBounds.height;

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

		return this;
	}


	private synchronized void layout(FontRenderContext aFontRenderContext)
	{
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
			boxH -= bi.left + bi.bottom;
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
			boxH -= bi.left + bi.bottom;
			extraLineHeight = bi.top + bi.bottom;
		}

		LineMetrics lm = mFont.getLineMetrics("Adgj", aFontRenderContext);
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
			String str = mTextLines.get(i);

			int lineX = boxX;
			int lineW = getStringLength(aFontRenderContext, str, mFont) + mPadding.left + mPadding.right;

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
		ArrayList<String> list = new ArrayList<>();

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
			for (String str : mText)
			{
				do
				{
					boolean isLastLine = mMaxLineCount == 0 || list.size() >= mMaxLineCount - 1;
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

					list.add(nextLine.trim());

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
			}
		}

		mTextLines = list;
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


	private void drawSingleLine(Graphics aGraphics, String aText, LineMetrics aLineMetrics, int aOffsetX, int aOffsetY, int aWidth, int aHeight, boolean aHasShadow, boolean aShadow)
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

		aGraphics.drawString(aText, aOffsetX + mPadding.left, aOffsetY + adjust + mPadding.top);
	}
}
package org.terifan.ganttchart.rev2;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import javax.imageio.ImageIO;


public class StyleSheet
{
	static Font LABEL_FONT = new Font("segoe ui", Font.PLAIN, 12);
	static Font TIME_FONT = new Font("segoe ui", Font.PLAIN, 9);
	static Color BACKGROUND = new Color(20, 20, 20);
	static Color EMPTY_BACKGROUND = new Color(25, 25, 29);
	static Color FOREGROUND = new Color(177, 177, 179);
	static Color SELECTION_COLOR = new Color(32, 78, 138);
	static Color SELECTION_OUTLINE_COLOR = new Color(166, 166, 175);
	static Color FLOWLINE = new Color(96, 96, 101);
	static Color LINE_COLOR_SELECTED = Color.WHITE;
	static Color LINE_COLOR_UNSELECTED = new Color(86, 86, 95);
	static Color DIVIDER_COLOR = new Color(60, 60, 60);
	static Color TEXT_COLOR_SELECTED = new Color(255, 255, 255);
	static Color TEXT_COLOR_UNSELECTED = new Color(177, 177, 179);
	static Color GRID_COLOR_0 = new Color(0.6f, 0.6f, 0.6f, 0.2f);
	static Color GRID_COLOR_1 = new Color(0.8f, 0.8f, 0.8f, 0.1f);
	static Color TREE_COLOR = new Color(128, 128, 128);
	static Color[] mRowColors = new Color[]
	{
		new Color(35, 35, 39), new Color(46, 46, 50)
	};
	static Color[] mRowOutlineColors = new Color[]
	{
		new Color(86, 86, 95), new Color(96, 96, 105)
	};

	static Color mGroupColor = new Color(255, 0, 0);
	static Color mSeparatorColor1 = Color.BLACK;
	static Color mSeparatorColor2 = new Color(90, 90, 90);

//	static Font LABEL_FONT = new Font("segoe ui", Font.PLAIN, 12);
//	static Font TIME_FONT = new Font("segoe ui", Font.PLAIN, 9);
//	static Color BACKGROUND = Color.WHITE;
//	static Color FOREGROUND = new Color(44, 53, 26);
//	static Color[] mRowColors = new Color[]
//	{
//		new Color(255, 255, 255), new Color(242, 242, 242)
//	};
//	static Color SELECTION_COLOR = new Color(218, 235, 252);
//	static Color FLOWLINE = new Color(220, 220, 220);
//	static Color LINE_COLOR_SELECTED = new Color(245, 245, 245);
//	static Color LINE_COLOR_UNSELECTED = Color.BLACK;
//	static Color TEXT_COLOR_SELECTED = new Color(0, 0, 0);
//	static Color TEXT_COLOR_UNSELECTED = Color.BLACK;
//	static Color GRID_COLOR_0 = new Color(0.6f, 0.6f, 0.6f, 0.2f);
//	static Color GRID_COLOR_1 = new Color(0.8f, 0.8f, 0.8f, 0.2f);

	static Color[] COLORS =
	{
		new Color(80, 114, 146),
		new Color(255, 179, 210),
		new Color(183, 107, 209),
		new Color(179, 87, 38),
		new Color(61, 145, 187),
		new Color(95, 158, 71),
		new Color(127, 159, 191),
		new Color(86, 200, 63),
		new Color(224, 132, 83),
		new Color(164, 0, 15),
		new Color(0, 150, 220),
		new Color(230, 25, 75),
		new Color(60, 180, 75),
		new Color(255, 225, 25),
		new Color(245, 130, 48),
		new Color(145, 30, 180),
		new Color(70, 240, 240),
		new Color(240, 50, 230),
		new Color(210, 245, 60),
		new Color(250, 190, 190),
		new Color(0, 128, 128),
		new Color(230, 190, 255),
		new Color(170, 110, 40),
		new Color(255, 250, 200),
		new Color(128, 0, 0),
		new Color(170, 255, 195),
		new Color(128, 128, 0),
		new Color(255, 215, 180),
		new Color(0, 0, 128),
		new Color(128, 128, 128)
	};

	static BufferedImage mIconSheet;
	static BufferedImage mIconSpinner;
	static BufferedImage mIconFinished;
	static BufferedImage mIconDetail;
	static BufferedImage[] mIconStatus;
	static int mAnimationRate;
	static int mAnimationSteps;


	static
	{
		if (mIconSheet == null)
		{
			try
			{
				mIconSheet = ImageIO.read(WorkStatusPanel.class.getResource("icons.png"));

				mIconStatus = new BufferedImage[]
				{
					mIconSheet.getSubimage(24 * 0, 24 * 3, 16, 16), // running
					mIconSheet.getSubimage(24 * 3, 24 * 1, 16, 16), // failed
					mIconSheet.getSubimage(24 * 6, 24 * 1, 16, 16), // aborted
					mIconSheet.getSubimage(24 * 8, 24 * 2, 16, 16), // pending
					mIconSheet.getSubimage(24 * 1, 24 * 0, 16, 16), // finish
					mIconSheet.getSubimage(24 * 2, 24 * 0, 16, 16), // success
					mIconSheet.getSubimage(24 * 5, 24 * 2, 16, 16)  // unknown
				};

				mIconFinished = mIconSheet.getSubimage(24 * 1, 24 * 0, 16, 16);
				mIconDetail = mIconSheet.getSubimage(24 * 3, 24 * 3, 16, 16);

				mIconSpinner = mIconSheet.getSubimage(24 * 5, 24 * 5, 16, 16);

				mAnimationSteps = 1;
				mAnimationRate = 4;
			}
			catch (Exception e)
			{
				e.printStackTrace(System.out);
			}
		}
	}
}

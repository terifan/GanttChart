package org.terifan.ganttchart.rev2;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import javax.imageio.ImageIO;
import org.terifan.ganttchart.rev2.Work.Status;


public class StyleSheet
{
	static Font LABEL_FONT = new Font("segoe ui", Font.PLAIN, 13);
	static Font TIME_FONT = new Font("segoe ui", Font.PLAIN, 11);
	static Color BACKGROUND = new Color(20, 20, 20);
	static Color LABEL_FOREGROUND = new Color(177, 177, 179);
	static Color LABEL_FOREGROUND_SELECTED = new Color(255, 255, 255);
	static Color ROW_BACKGROUND_SELECTED = new Color(32, 78, 138);
	static Color ROW_OUTLINE_SELECTED = new Color(166, 166, 175);
	static Color DIVIDER_COLOR = new Color(56,56,61);
	static Color TIME_COLOR = new Color(255, 255, 255);
	static Color TREE_COLOR = new Color(128, 128, 128);

	static Color[] mRowBackgroundColors = new Color[]
	{
		new Color(35, 35, 39), new Color(46, 46, 50)
	};
	static Color[] mRowOutlineColors = new Color[]
	{
		new Color(86, 86, 95), new Color(96, 96, 105)
	};

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
//		new Color(145, 30, 180),
//		new Color(70, 240, 240),
//		new Color(240, 50, 230),
//		new Color(210, 245, 60),
//		new Color(250, 190, 190),
//		new Color(0, 128, 128),
//		new Color(230, 190, 255),
//		new Color(170, 110, 40),
//		new Color(255, 250, 200),
//		new Color(128, 0, 0),
//		new Color(170, 255, 195),
//		new Color(128, 128, 0),
//		new Color(255, 215, 180),
//		new Color(0, 0, 128),
//		new Color(128, 128, 128)
	};

	static Color[] STAGE_COLORS =
	{
//		new Color(80, 114, 146),
//		new Color(255, 179, 210),
//		new Color(183, 107, 209),
//		new Color(179, 87, 38),
//		new Color(61, 145, 187),
//		new Color(95, 158, 71),
//		new Color(127, 159, 191),
//		new Color(86, 200, 63),
//		new Color(224, 132, 83),
//		new Color(164, 0, 15),
//		new Color(0, 150, 220),
//		new Color(230, 25, 75),
//		new Color(60, 180, 75),
//		new Color(255, 225, 25),
//		new Color(245, 130, 48),
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
	static BufferedImage mIconDetail;
	static HashMap<Status, BufferedImage> mIconStatus;
	static int mAnimationRate = 4;
	static int mAnimationSteps = 1;
	static int mRightMarginWidth = 100;
	static int mLabelWidth = 250;
	static int mSingeLineHeight = 24;

	static
	{
		if (mIconSheet == null)
		{
			try
			{
				mIconSheet = ImageIO.read(WorkStatusPanel.class.getResource("icons.png"));

				mIconDetail = mIconSheet.getSubimage(24 * 3, 24 * 3, 16, 16);

				mIconStatus = new HashMap<>();
				mIconStatus.put(Status.PENDING, mIconSheet.getSubimage(24 * 10, 24 * 2, 16, 16));
				mIconStatus.put(Status.RUNNING, mIconSheet.getSubimage(24 * 5, 24 * 5, 16, 16));
				mIconStatus.put(Status.FINISH, mIconSheet.getSubimage(24 * 1, 24 * 0, 16, 16));
				mIconStatus.put(Status.ABORT, mIconSheet.getSubimage(24 * 6, 24 * 1, 16, 16));
				mIconStatus.put(Status.FAIL, mIconSheet.getSubimage(24 * 3, 24 * 1, 16, 16));
				mIconStatus.put(Status.SUCCESS, mIconSheet.getSubimage(24 * 2, 24 * 0, 16, 16));
			}
			catch (Exception e)
			{
				e.printStackTrace(System.out);
			}
		}
	}
}

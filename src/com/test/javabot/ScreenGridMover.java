package com.test.javabot;

import java.awt.Dimension;
import java.awt.Robot;
import java.awt.Toolkit;

import com.lewisybt.javabot.Javabot;
import com.lewisybt.javabot.objects.BotAction;
import com.lewisybt.javabot.objects.BotHotkeyListener;
import com.lewisybt.javabot.views.BotBasicUI;

/**
 * While the Javabot is running, the arrow keys will move the mouse to a 4x4 grid around the screen.
 * @author Lewis
 * 
 */
public class ScreenGridMover implements BotHotkeyListener {

	private static final int KEY_LEFT = 37;
	private static final int KEY_UP = 38;
	private static final int KEY_RIGHT = 39;
	private static final int KEY_DOWN = 40;

	private static final int NUM_COLS = 4;
	private static final int NUM_ROWS = 4;
	private int rowHeight, colWidth;
	private int currRow, currCol;
	private int x, y;

	private Javabot bot;

	public static void main(String[] args) {
		new ScreenGridMover();
	}

	public ScreenGridMover() {
		// init Javabot and a basic UI
		bot = Javabot.getInstance();
		new BotBasicUI(bot);

		// bind hotkeys to this class (listener)
		bot.bindHotkey(KEY_LEFT, this);
		bot.bindHotkey(KEY_UP, this);
		bot.bindHotkey(KEY_RIGHT, this);
		bot.bindHotkey(KEY_DOWN, this);

		// calculate screen locations
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		colWidth = (int) (screenSize.getWidth() / NUM_COLS);
		rowHeight = (int) (screenSize.getHeight() / NUM_ROWS);
		x = colWidth / 2;
		y = rowHeight / 2;
	}

	@Override
	public void onHotkey(int hotkey) {
		if (!bot.isRunning())
			return;

		switch (hotkey) {
		case KEY_LEFT:
			if (currCol > 0) {
				x -= colWidth;
				currCol--;
				bot.queueAction(new MouseMover(x, y));
			}
			break;
		case KEY_UP:
			if (currRow > 0) {
				y -= rowHeight;
				currRow--;
				bot.queueAction(new MouseMover(x, y));
			}
			break;
		case KEY_RIGHT:
			if (currCol < NUM_COLS - 1) {
				x += colWidth;
				currCol++;
				bot.queueAction(new MouseMover(x, y));
			}
			break;
		case KEY_DOWN:
			if (currRow < NUM_ROWS - 1) {
				y += rowHeight;
				currRow++;
				bot.queueAction(new MouseMover(x, y));
			}
			break;
		}
	}

	/**
	 * A BotAction that simply moves to the given x,y location on the screen
	 * @author Lewis
	 *
	 */
	protected class MouseMover implements BotAction {
		int x, y;

		public MouseMover(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public void doAction(Robot robot) {
			robot.mouseMove(x, y);
		}

	}

}

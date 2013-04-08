package com.test.javabot;

import com.lewisybt.javabot.JavaBot;
import com.lewisybt.javabot.views.BotBasicUI;

public class ScreenClicker {

	public static void main(String[] args) {
		
		JavaBot bot = JavaBot.getInstance();
		new BotBasicUI(bot);

	}

}

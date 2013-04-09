package com.lewisybt.javabot.objects;

import java.awt.Robot;

/**
 * Interface for actions queued to the Javabot
 * @author Lewis
 *
 */
public interface BotAction {
	/**
	 * The action to be performed by the Java Robot class when this BotAction
	 * is triggered.
	 * @param robot
	 */
	public void doAction(Robot robot);
}

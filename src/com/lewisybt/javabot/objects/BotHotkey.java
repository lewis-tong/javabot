package com.lewisybt.javabot.objects;

/**
 * Class representing a Javabot Hotkey
 * @author Lewis
 *
 */
public class BotHotkey {

	public int id;
	public int key;
	public BotHotkeyListener listener;
	
	public BotHotkey(int id, int key, BotHotkeyListener listener) {
		this.id = id;
		this.key = key;
		this.listener = listener;
	}

}

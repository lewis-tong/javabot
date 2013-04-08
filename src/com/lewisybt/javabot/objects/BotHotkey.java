package com.lewisybt.javabot.objects;

public class BotHotkey {

	public int id;
	public int key;
	public BotAction action;
	
	public BotHotkey(int id, int key, BotAction action) {
		this.id = id;
		this.key = key;
		this.action = action;
	}

}

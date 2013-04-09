package com.lewisybt.javabot.objects;

/**
 * Interface that is bound to by hotkeys. A hotkey that is bound to this 
 * interface will trigger the onHotkey() method of this interface when the key
 * is pressed.
 * 
 * @author Lewis
 * 
 */
public interface BotHotkeyListener {
	/**
	 * Callback for a key-press involving one of the bind keys. Here you should
	 * check which hotkey is being called.
	 * @param hotkey
	 */
	public void onHotkey(int hotkey);
}

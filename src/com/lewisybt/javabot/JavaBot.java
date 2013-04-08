package com.lewisybt.javabot;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import com.lewisybt.javabot.objects.BotAction;
import com.lewisybt.javabot.objects.BotHotkey;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

public class JavaBot implements Runnable {

	private static JavaBot instance; // singleton of JavaBot
	private JIntellitype jInstance; // singleton of JIntellitype

	private int nextHotkeyID = 100; // incremental ID of hotkey mapping
	private HashMap<Integer, BotHotkey> hotkeyIDMap; // map of hotkey ID to it's object

	private Robot bot; // the bot
	private Thread botThread; // thread running the bot
	private volatile Queue<BotAction> actionQueue; // queue of actions to execute

	private volatile int status = 0; // current status
	public static int FINISHED = 0; // status codes for running, paused, finished
	public static int RUNNING = 1;
	public static int PAUSED = 2;
	private Object lock; // lock for pausing/resuming

	private long startTime; // the system time when bot thread began execution
	private static int RUNNING_TIME = 6000000; // max running time of the bot

	/**
	 * Obtain singleton instance of JavaBot and starts the bot thread.
	 * @return
	 */
	public static JavaBot getInstance() {
		// check library support
		JIntellitype.setLibraryLocation("JIntellitype32.dll"); // try 32-bit library
		if (!JIntellitype.isJIntellitypeSupported()) {
			JIntellitype.setLibraryLocation("JIntellitype64.dll"); // try 64-bit library
			if (!JIntellitype.isJIntellitypeSupported()) {
				System.out.println("JIntellitype not supported - missing DLL");
				return null;
			}
		}

		if (null == instance) {
			instance = new JavaBot();
		}
		return instance;
	}

	/**
	 * Private constructor for singleton instance
	 */
	private JavaBot() {
		// initializations
		this.jInstance = JIntellitype.getInstance();
		this.hotkeyIDMap = new HashMap<>();
		this.lock = new Object();
		this.actionQueue = new LinkedList<>();
		try {
			this.bot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}

		// listen for hotkeys to route to mapped action
		HotkeyListener hkl = new HotkeyListener() {
			public void onHotKey(int keyID) {
				if (hotkeyIDMap.containsKey(keyID)) {
					sendAction(hotkeyIDMap.get(keyID).action);
				}
			}
		};
		jInstance.addHotKeyListener(hkl);

		// finish bot thread when program closes
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				finish();
			}
		});
	}

	/**
	 * Returns the status of the bot.
	 * @return 0=Not Running, 1=Running, 2=Paused
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Returns the ID associated with a given hotkey
	 * @param hotkey
	 * @return
	 */
	private int getHotkeyID(int hotkey) {
		for (int id : hotkeyIDMap.keySet()) {
			if (hotkeyIDMap.get(id).key == hotkey)
				return id;
		}
		return -1;
	}

	/**
	 * Sets a hotkey to perform the given action
	 * @param hotkey hotkey id in ASCII
	 * @param action
	 */
	public void setHotkey(int hotkey, BotAction action) {
		int hotkeyID = getHotkeyID(hotkey);
		if (hotkeyID != -1) {
			hotkeyIDMap.get(hotkeyID).action = action;
		} else {
			hotkeyID = nextHotkeyID++;
			jInstance.registerHotKey(hotkeyID, 0, hotkey);
			hotkeyIDMap.put(hotkeyID, new BotHotkey(hotkeyID, hotkey, action));
		}
	}

	/**
	 * Unbinds a hotkey
	 * @param hotkey hotkey id in ASCII
	 */
	public void removeHotkey(int hotkey) {
		int hotkeyID = getHotkeyID(hotkey);
		if (hotkeyID != -1) {
			jInstance.unregisterHotKey(hotkeyID);
			hotkeyIDMap.remove(hotkeyID);
		}
	}

	/**
	 * Starts the bot thread
	 */
	public void start() {
		if (status == FINISHED) {
			botThread = new Thread(this);
			botThread.start();
		}
	}

	/**
	 * Resumes the bot, if paused
	 */
	public void resume() {
		synchronized (lock) {
			if (status == PAUSED) {
				status = RUNNING;
				lock.notify();
			}
		}
	}

	/**
	 * Pauses the bot, if running
	 */
	public void pause() {
		if (status == RUNNING) {
			status = PAUSED;
		}
	}

	/**
	 * Terminates the bot.
	 */
	public void finish() {
		synchronized (lock) {
			status = FINISHED;
			lock.notify();
		}
	}

	/**
	 * Sends an action command to the bot, and returns whether it was successful
	 * @param action
	 * @return true if action successfully added to action queue; false otherwise.
	 */
	public boolean sendAction(BotAction action) {
		if (status == RUNNING) {
			actionQueue.add(action);
			return true;
		}
		return false;
	}

	/**
	 * Called at the end of program to cleanup instances
	 */
	public void cleanUp() {
		JIntellitype.getInstance().cleanUp();
	}

	/**
	 * The thread that runs the bot
	 */
	@Override
	public void run() {
		System.out.println("bot started");
		status = RUNNING;
		startTime = System.currentTimeMillis();
		while (System.currentTimeMillis() - startTime < RUNNING_TIME && status != FINISHED) {
			// pause checking
			synchronized (lock) {
				while (status == PAUSED) {
					System.out.println("bot paused");
					try {
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("bot resumed");
				}
			}
			// execute any action that was queued up
			if (!actionQueue.isEmpty()) {
				actionQueue.poll().doAction(bot);
			}
		}
		System.out.println("bot finished");
	}

}

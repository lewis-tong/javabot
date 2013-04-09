package com.lewisybt.javabot;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import com.lewisybt.javabot.objects.BotAction;
import com.lewisybt.javabot.objects.BotHotkey;
import com.lewisybt.javabot.objects.BotHotkeyListener;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

/**
 * The Javabot object. Also contains the thread that will execute the Java
 * Robot actions.
 * 
 * @author Lewis
 *
 */
public class Javabot implements Runnable {

	private static final String JINTELLITYPE32 = "JIntellitype32.dll";
	private static final String JINTELLITYPE64 = "JIntellitype64.dll";
	private static Javabot instance; // singleton of JavaBot
	private JIntellitype jInstance; // singleton of JIntellitype

	private int nextHotkeyID = 100; // incremental ID of hotkey mapping
	private HashMap<Integer, BotHotkey> hotkeyIDMap; // map of hotkey ID to it's object

	private Robot bot; // the bot
	private Thread botThread; // thread running the bot
	private volatile Queue<BotAction> actionQueue; // queue of actions to execute

	public static final int FINISHED = 0; // status codes for running, paused, finished
	public static final int RUNNING = 1;
	public static final int PAUSED = 2;
	private volatile int status = 0; // current status
	private Object lock; // lock for pausing/resuming

	private static final int RUNNING_TIME = 6000000; // max running time of the bot
	private long startTime; // the system time when bot thread began execution

	/**
	 * Obtain singleton instance of JavaBot and starts the bot thread.
	 * If the JIntellitype library is not found, will return null.
	 * @return
	 */
	public static Javabot getInstance() {
		// check library support
		JIntellitype.setLibraryLocation(JINTELLITYPE32); // try 32-bit library
		if (!JIntellitype.isJIntellitypeSupported()) {
			JIntellitype.setLibraryLocation(JINTELLITYPE64); // try 64-bit library
			if (!JIntellitype.isJIntellitypeSupported()) {
				System.out.println("JIntellitype not supported - missing DLL");
				return null;
			}
		}

		if (null == instance) {
			instance = new Javabot();
		}
		return instance;
	}

	/**
	 * Private constructor for singleton instance
	 */
	private Javabot() {
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
					hotkeyIDMap.get(keyID).listener.onHotkey(hotkeyIDMap.get(keyID).key);
				}
			}
		};
		jInstance.addHotKeyListener(hkl);

		// finish bot thread when program closes
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				finish();
				cleanUp();
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
	 * Returns the ID associated with a given hotkey, or -1 if no ID exists.
	 * @param hotkey hotkey value in ASCII
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
	 * Binds a hotkey to a listener
	 * @param hotkey hotkey value in ASCII
	 * @param listener the object handling the hotkey action
	 */
	public void bindHotkey(int hotkey, BotHotkeyListener listener) {
		int hotkeyID = getHotkeyID(hotkey);
		if (hotkeyID != -1) {
			hotkeyIDMap.get(hotkeyID).listener = listener;
		} else {
			hotkeyID = nextHotkeyID++;
			jInstance.registerHotKey(hotkeyID, 0, hotkey);
			hotkeyIDMap.put(hotkeyID, new BotHotkey(hotkeyID, hotkey, listener));
		}
	}

	/**
	 * Unbinds a hotkey from its listener
	 * @param hotkey hotkey value in ASCII
	 */
	public void unbindHotkey(int hotkey) {
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
	 * Returns whether the bot's status is Running
	 * @return
	 */
	public boolean isRunning() {
		return status == RUNNING;
	}
	
	/**
	 * Returns whether the bot's status is Paused
	 * @return
	 */
	public boolean isPaused() {
		return status == PAUSED;
	}
	
	/**
	 * Returns whether the bot's status is Stopped/Finished
	 * @return
	 */
	public boolean isStopped() {
		return status == FINISHED;
	}

	/**
	 * Sends an action command to the bot, and returns whether it was successful
	 * @param action the action to be queued up
	 * @return true if action successfully added to action queue; false otherwise.
	 */
	public boolean queueAction(BotAction action) {
		if (status == RUNNING) {
			actionQueue.add(action);
			return true;
		}
		return false;
	}

	/**
	 * Called at the end of program to cleanup library instances
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

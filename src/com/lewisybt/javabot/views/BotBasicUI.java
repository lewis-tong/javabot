package com.lewisybt.javabot.views;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.lewisybt.javabot.JavaBot;

public class BotBasicUI {

	private JavaBot bot;
	private JFrame window;
	
	public BotBasicUI(JavaBot bot) {
		this.bot = bot;
		init();
	}
	
	/**
	 * Initialize a basic UI with buttons to Start, Resume, Pause and Finish
	 */
	private void init() {	
		window = new JFrame("Basic UI");
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel();
		JButton startButton = new JButton("Start");
		JButton resumeButton = new JButton("Resume");
		JButton pauseButton = new JButton("Pause");
		JButton finishButton = new JButton("Finish");
		
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				bot.start();
			}
		});
		resumeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				bot.resume();
			}
		});
		
		pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				bot.pause();
			}
		});
		finishButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				bot.finish();
			}
		});
		
		panel.add(startButton);
		panel.add(resumeButton);
		panel.add(pauseButton);
		panel.add(finishButton);
		window.add(panel);
		
		window.pack(); // tight fit
		window.setLocationRelativeTo(null); // center the window
		window.setVisible(true); // let's roll
		
		window.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent evt) {
				// don't forget to clean up any resources before close
				bot.cleanUp();
				System.exit(0);
			}
		});
	}
	
	

}

package com.fisherevans.giflooper.app;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import com.fisherevans.giflooper.GIFLooper;

public class SettingsFrame extends JFrame implements ActionListener {
	private JPanel _rootPanel, _buttonButton;
	private JComboBox _clear, _aa, _graphics;
	private JLabel _speedLabel;
	private JSlider _speedSlider;
	
	public SettingsFrame() {
		super("GIFLooper Settings");
	    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		pack();
		setVisible(true);
		setResizable(false);
		GIFLooper.activeFrame.setVisible(false);
	}
	
	public void save() {
		
	}
	
	public void close() {
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
	}
}

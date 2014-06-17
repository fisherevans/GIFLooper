package com.fisherevans.giflooper;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.fisherevans.giflooper.panels.OpenPanel;
import com.fisherevans.giflooper.panels.ProjectPanel;

public class GIFLooper {
	public static JFrame activeFrame = null;
	
	public static File gifFile, settingsFile;
	
	public static void loadProject() {
		closeActiveFrame();
		activeFrame = new JFrame("GIF Looper - Project Editor");
		activeFrame.add(new ProjectPanel());
		displayActiveFrame();
	}
	
	public static void loadOpen() {
		closeActiveFrame();
		activeFrame = new JFrame("GIF Looper - Open Project");
		activeFrame.add(new OpenPanel());
		displayActiveFrame();
	}
	
	public static void closeActiveFrame() {
		if(activeFrame != null)
			activeFrame.dispose();
	}
	
	public static void displayActiveFrame() {
		if(activeFrame != null) {
			activeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			activeFrame.pack();
			center(activeFrame);
			activeFrame.setVisible(true);
		}
	}
	
	public static void error(String message) {
		JOptionPane.showMessageDialog(activeFrame,
				message, "Error", JOptionPane.WARNING_MESSAGE);
	}
	
	public static void center(JFrame frame) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
	}
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			error("Failed to set UI theme...");
		}
		GIFLooper.loadOpen();
	}
}

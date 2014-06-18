package com.fisherevans.giflooper;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.fisherevans.giflooper.panels.OpenPanel;
import com.fisherevans.giflooper.panels.project.ProjectPanel;

public class GIFLooper {
	public static JFrame activeFrame = null;
	
	public static File gifFile, settingsFile;
	
	public static void loadProject() {
		closeActiveFrame();
		activeFrame = new JFrame("GIF Looper - Project Editor");
		activeFrame.add(new ProjectPanel());
		displayActiveFrame();
        activeFrame.setMinimumSize(new Dimension(800, 600));
	}
	
	public static void loadOpen() {
		closeActiveFrame();
		activeFrame = new JFrame("GIF Looper - Open Project");
		activeFrame.add(new OpenPanel());
		displayActiveFrame();
        activeFrame.setResizable(false);
	}
	
	public static void closeActiveFrame() {
		if(activeFrame != null) {
            activeFrame.setJMenuBar(null);
			activeFrame.dispose();
        }
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

    public static boolean confirm(String message) {
        int result = JOptionPane.showConfirmDialog(activeFrame, message,
                "Are You Sure?", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
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

package com.fisherevans.giflooper.app;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.fisherevans.giflooper.App;
import com.fisherevans.giflooper.GIFLooper;
import com.fisherevans.giflooper.GIFSaver;

import net.miginfocom.swing.MigLayout;

/**
 * Author: Fisher Evans
 * Date: 6/18/14
 */
public class CommandPanel extends JPanel implements ActionListener {
	private boolean _locked = false;
	
	public CommandPanel() {
		super(new MigLayout("fillx"));
		addButton("save", "Save Project");
		addButton("preview", "Preview GIF");
		addButton("export", "Export GIF");
	}
	
	private void addButton(String command, String name) {
		JButton button = new JButton(name);
		button.setActionCommand(command);
		button.addActionListener(this);
		add(button, "width 100%, wrap");
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if(!_locked) {
			_locked = true;
			try {
				if("save".equals(event.getActionCommand())) {
					App.current.saveProjectFile();
				} else if("preview".equals(event.getActionCommand())) {
					String file = GIFLooper.gifFile.getAbsolutePath() + ".tmp_" + System.currentTimeMillis();
					GIFSaver.save(true, file, App.project, App.current.decoder, App.current.gifWidth, App.current.gifHeight);
				} else if("export".equals(event.getActionCommand())) {
					App.current.export();
				}
			} catch(Exception e) {
				GIFLooper.error("There was an error while processing you " + ((JButton)event.getSource()).getText() + " request.");
			}
			_locked = false;
		}
	}
}

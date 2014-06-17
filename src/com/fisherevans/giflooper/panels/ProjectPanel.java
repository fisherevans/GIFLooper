package com.fisherevans.giflooper.panels;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.logging.Logger;

import javax.swing.JPanel;

import com.fisherevans.giflooper.GIFLooper;
import com.fisherevans.giflooper.components.Project;

import lib.GifDecoder;
import lib.GifEncoder;

public class ProjectPanel extends JPanel {
	private Logger log = Logger.getLogger(ProjectPanel.class.getName());
	private GifDecoder _decoder;
	private GifEncoder _encoder;
	private Project _project;
	
	public ProjectPanel() {
		openDecoder();
		openProjectFile();
	}

	private void openDecoder() {
		_decoder = new GifDecoder();
		int status = _decoder.read(GIFLooper.gifFile.getAbsolutePath());
		if(status == GifDecoder.STATUS_FORMAT_ERROR) {
			forceCloseProject("Invalid Animated GIF: " + GIFLooper.gifFile.getName());
			return;
		} else if(status == GifDecoder.STATUS_OPEN_ERROR) {
			forceCloseProject("IO Error Opening: " + GIFLooper.gifFile.getName());
			return;
		}
	}
	
	private void openProjectFile() {
		if(GIFLooper.settingsFile.exists()) {
			try {
				FileInputStream fileIn = new FileInputStream(GIFLooper.settingsFile.getAbsolutePath());
				ObjectInputStream in = new ObjectInputStream(fileIn);
				_project = (Project) in.readObject();
				in.close();
				fileIn.close();
			} catch (Exception e) {
				forceCloseProject("Invalid/corrupt project file: " + GIFLooper.settingsFile.getName());
				return;
			}
			if(_project.frameCount != _decoder.getFrameCount()) {
				forceCloseProject("This project file does not belong to this selected GIF.");
				return;
			}
		} else {
			_project = new Project(_decoder.getFrameCount());
		}
	}
	
	private void forceCloseProject(String message) {
		if(message != null)
			GIFLooper.error(message);
		GIFLooper.loadOpen();
	}
}

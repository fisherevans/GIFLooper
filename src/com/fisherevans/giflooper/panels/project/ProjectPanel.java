package com.fisherevans.giflooper.panels.project;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.logging.Logger;

import javax.swing.JPanel;

import com.fisherevans.giflooper.GIFLooper;
import com.fisherevans.giflooper.Menu;
import com.fisherevans.giflooper.components.Project;

import com.fisherevans.giflooper.panels.project.timeline.TimelinePanel;
import lib.GifDecoder;
import lib.GifEncoder;
import net.miginfocom.swing.MigLayout;

public class ProjectPanel extends JPanel {
	private Logger log = Logger.getLogger(ProjectPanel.class.getName());

    public GifDecoder decoder;
    public GifEncoder encoder;

    public int width, height;

	public Project project;

    private Menu _menu;
    private TimelinePanel _timelinePanel;
	
	public ProjectPanel() {
        super(new MigLayout("fill"));
		openDecoder();
		openProjectFile();
        loadMenu();
        loadNavigator();
	}

	private void openDecoder() {
		decoder = new GifDecoder();
		int status = decoder.read(GIFLooper.gifFile.getAbsolutePath());
		if(status == GifDecoder.STATUS_FORMAT_ERROR) {
			forceCloseProject("Invalid Animated GIF: " + GIFLooper.gifFile.getName());
			return;
		} else if(status == GifDecoder.STATUS_OPEN_ERROR) {
			forceCloseProject("IO Error Opening: " + GIFLooper.gifFile.getName());
			return;
		}
        width = decoder.getFrame(0).getWidth();
        height = decoder.getFrame(0).getHeight();
	}
	
	private void openProjectFile() {
		if(GIFLooper.settingsFile.exists()) {
            FileInputStream fileIn = null;
            ObjectInputStream in = null;
			try {
				fileIn = new FileInputStream(GIFLooper.settingsFile.getAbsolutePath());
				in = new ObjectInputStream(fileIn);
				project = (Project) in.readObject();
			} catch (Exception e1) {
                try {
                    if(in != null)
                        in.close();
                    if(fileIn != null)
                        fileIn.close();
                } catch (Exception e2) { }
				forceCloseProject("Invalid/corrupt project file: " + GIFLooper.settingsFile.getName());
				return;
			}
			if(project.frameCount != decoder.getFrameCount()) {
				forceCloseProject("This project file does not belong to this selected GIF.");
				return;
			}
		} else {
			project = new Project(decoder.getFrameCount());
		}
	}

    private void loadMenu() {
        _menu = new Menu();
        GIFLooper.activeFrame.setJMenuBar(_menu);
    }

    private void loadNavigator() {
        _timelinePanel = new TimelinePanel(this);
        add(_timelinePanel, "height 150px, dock north");
    }
	
	private void forceCloseProject(String message) {
		if(message != null)
			GIFLooper.error(message);
		GIFLooper.loadOpen();
	}
}

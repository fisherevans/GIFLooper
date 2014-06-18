package com.fisherevans.giflooper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.fisherevans.giflooper.app.AnchorPanel;
import com.fisherevans.giflooper.app.AppWindowListener;
import com.fisherevans.giflooper.app.Menu;
import com.fisherevans.giflooper.app.TimelinePanel;
import com.fisherevans.giflooper.app.TransitionPanel;
import com.fisherevans.giflooper.app.components.Anchor;
import com.fisherevans.giflooper.app.components.Project;
import com.fisherevans.giflooper.app.events.EventRouter;
import com.fisherevans.giflooper.app.events.EventRouter.EventRouterListener;
import com.fisherevans.giflooper.app.events.EventType;

import lib.GifDecoder;
import lib.GifEncoder;
import net.miginfocom.swing.MigLayout;

public class App extends JPanel implements EventRouterListener {
	public static App current;
	public static Project project;
	
	private Logger log = Logger.getLogger(App.class.getName());

    public GifDecoder decoder;
    public GifEncoder encoder;

    public int gifWidth, gifHeight;

    private Menu _menu;
    private TimelinePanel _timelinePanel;
    private AnchorPanel _anchorPanel;
    private TransitionPanel _transitionPanel;
    
    public Anchor activeAnchor = null;
	
	public App() {
        super(new MigLayout("fill"));
        GIFLooper.activeFrame.addWindowListener(new AppWindowListener());
        GIFLooper.activeFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        EventRouter.addListener(this, EventType.AnchorSelected);
        EventRouter.addListener(this, EventType.AnchorDeleted);
        EventRouter.addListener(this, EventType.AnchorAdded);
        EventRouter.addListener(this, EventType.ActiveAnchorUpdated);
        
        current = this;
        
		openDecoder();
		openProjectFile();
        loadMenu();
        loadTimelinePanel();
        loadAnchorPanel();
        loadTransitionPanel();
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
        gifWidth = decoder.getFrame(0).getWidth();
        gifHeight = decoder.getFrame(0).getHeight();
	}
	
	private void openProjectFile() {
		if(GIFLooper.settingsFile.exists()) {
            FileInputStream fileIn = null;
            ObjectInputStream in = null;
			try {
				fileIn = new FileInputStream(GIFLooper.settingsFile.getAbsolutePath());
				in = new ObjectInputStream(fileIn);
				project = (Project) in.readObject();
                in.close();
                fileIn.close();
			} catch (Exception e1) {
                try {
                    if(in != null)
                        in.close();
                    if(fileIn != null)
                        fileIn.close();
                } catch (Exception e2) { }
				forceCloseProject("Invalid/corrupt project file: " + GIFLooper.settingsFile.getName() + ". " + e1.getMessage());
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
	
	public void saveProjectFile() {
		if(project != null) {
	        FileOutputStream fileOut  = null;
	        ObjectOutputStream out = null;
			try {
				fileOut = new FileOutputStream(GIFLooper.settingsFile.getAbsolutePath());
				out = new ObjectOutputStream(fileOut);
				out.writeObject(project);
	        	out.close();
	        	fileOut.close();
			} catch (Exception e1) {
	            try {
	                if(out != null)
	                	out.close();
	                if(fileOut != null)
	                	fileOut.close();
	            } catch (Exception e2) { }
	            GIFLooper.error("Failed to save the project file: " + GIFLooper.settingsFile.getName());
				return;
			}
		} else {
			forceCloseProject("Fatal Error - Project is not real!");
		}
        GIFLooper.message("Project saved.");
	}

    private void loadMenu() {
        _menu = new Menu();
        GIFLooper.activeFrame.setJMenuBar(_menu);
    }

    private void loadTimelinePanel() {
        _timelinePanel = new TimelinePanel();
        add(_timelinePanel, "height " + TimelinePanel.HEIGHT + "px, dock north, gapbottom 4px");
    }
    
    private void loadAnchorPanel() {
    	_anchorPanel = new AnchorPanel();
        add(_anchorPanel, "width " + AnchorPanel.WIDTH + "px, dock east, gapleft 4px");
    }

	private void loadTransitionPanel() {
		_transitionPanel = new TransitionPanel();
		add(_transitionPanel, "grow");
	}
	
	private void forceCloseProject(String message) {
		if(message != null)
			GIFLooper.error(message);
		GIFLooper.loadOpen();
	}

	@Override
	public void event(EventType eventType, Object source, Object obj) {
		if(eventType == EventType.AnchorSelected) {
        	activeAnchor = (Anchor)obj;
        	_anchorPanel.setAnchor(activeAnchor);
		} else if(eventType == EventType.AnchorDeleted) { 
        	project.removeAnchor((Anchor)obj);
		} else if(eventType == EventType.AnchorAdded)  {
        	project.addAnchor((Anchor)obj);
		} else if(eventType == EventType.ActiveAnchorUpdated)  {
        	repaint();
		}
	}
}

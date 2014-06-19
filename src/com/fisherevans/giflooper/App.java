package com.fisherevans.giflooper;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import com.fisherevans.giflooper.app.AnchorPanel;
import com.fisherevans.giflooper.app.AppWindowListener;
import com.fisherevans.giflooper.app.CommandPanel;
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

    public int gifWidth, gifHeight;

    private Menu _menu;
    private TimelinePanel _timelinePanel;
    private TransitionPanel _transitionPanel;
    private JPanel _bottomPanel, _rightPanel;
    private AnchorPanel _anchorPanel;
    private CommandPanel _commandPanel;
    
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
        loadComponents();
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

    private void loadComponents() {
        _menu = new Menu();
        GIFLooper.activeFrame.setJMenuBar(_menu);
        
        _timelinePanel = new TimelinePanel();
        add(_timelinePanel, "height 100px, width 100%, wrap");
        
    	_bottomPanel = new JPanel(new MigLayout("fill, insets 0"));
    	_bottomPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        add(_bottomPanel, "height 100%-100px, width 100%, wrap");
        
		_transitionPanel = new TransitionPanel();
		_transitionPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		_bottomPanel.add(_transitionPanel, "width 70%, height 100%");

    	_rightPanel = new JPanel(new MigLayout("fillx, insets 0"));
    	_rightPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
    	
    	_anchorPanel = new AnchorPanel();
    	_anchorPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        _rightPanel.add(_anchorPanel, "width 100%, wrap");
        
    	_commandPanel = new CommandPanel();
    	_commandPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        _rightPanel.add(_commandPanel, "width 100%, wrap");
        
        _bottomPanel.add(_rightPanel, "width 30%, height 100%, wrap");
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
			project.sort();
        	repaint();
		}
	}

	public void export() {
		String file = GIFLooper.gifFile.getAbsolutePath().replace(".gif", ".out.gif");
		file = JOptionPane.showInputDialog("Please enter a filename", file);
		if(file != null)
			GIFSaver.save(false, file, App.project, App.current.decoder, App.current.gifWidth, App.current.gifHeight);
	}
}

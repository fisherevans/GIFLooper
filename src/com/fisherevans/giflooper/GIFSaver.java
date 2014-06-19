package com.fisherevans.giflooper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import lib.GifDecoder;
import lib.GifEncoder;
import net.miginfocom.swing.MigLayout;

import com.fisherevans.giflooper.app.components.Anchor;
import com.fisherevans.giflooper.app.components.Project;

public class GIFSaver {
	private String fileLocation;
    private List<Anchor> anchors;
    private GifDecoder decoder;
    private int gifWidth;
    private int gifHeight;

	public GIFSaver(String fileLocation, List<Anchor> anchors, GifDecoder decoder, int gifWidth, int gifHeight) {
		this.fileLocation = fileLocation;
		this.anchors = anchors;
		this.decoder = decoder;
		this.gifWidth = gifWidth;
		this.gifHeight = gifHeight;
	}

	public String saveGif(JProgressBar bar) {
		String errorMsg = "Failed to save file to " + fileLocation;
		if(decoder != null) {
			File saveFile = new File(fileLocation);
			GifEncoder encoder = new GifEncoder();
			encoder.setQuality(20);
			encoder.setRepeat(decoder.getLoopCount());
			if(encoder.start(saveFile.getAbsolutePath())) {
				double value, dx, dy, xscale, yscale, width, height, rotation;
				Anchor left, right;
				BufferedImage img, imgOut = new BufferedImage(gifWidth, gifHeight, BufferedImage.TYPE_INT_RGB);
				Graphics2D gfx = GraphicsUtil.getG2D(imgOut.createGraphics());

				int count = anchors.get(anchors.size()-1).frameID - anchors.get(0).frameID;
				int start = anchors.get(0).frameID;
				int anchorId = 0;
				for(int id = 0;id < count;id++) {
					int frameId = start + id;
					left = anchors.get(anchorId);
					right = anchors.get(anchorId+1);
					img = decoder.getFrame(frameId);
					value = (frameId-left.frameID)/(double)(right.frameID-left.frameID);
					
					dx = interp(left.deltaX, right.deltaX, value);
					dy = interp(left.deltaY, right.deltaY, value);
					xscale = interp(left.scaleX, right.scaleX, value);
					yscale = interp(left.scaleY, right.scaleY, value);
					width = gifWidth*xscale;
					height = gifHeight*yscale;
					rotation = interp(left.degrees, right.degrees, value);
					
					dx += (gifWidth-width)/2.0;
					dy += (gifHeight-height)/2.0;
					
					if(App.project.settings.clearEachFrame) {
						gfx.setColor(Color.BLACK);
						gfx.fillRect(0, 0, imgOut.getWidth(), imgOut.getHeight());
					}

					AffineTransform old = gfx.getTransform();
					gfx.rotate(Math.toRadians(rotation), width/2.0+dx, height/2.0+dy);
					AffineTransform t = new AffineTransform();
			        t.translate(dx, dy);
			        t.scale(xscale, yscale);
			        gfx.drawImage(img, t, null);
					gfx.setTransform(old);
					
					encoder.setDelay(decoder.getDelay(frameId));
					encoder.addFrame(imgOut);
					
					bar.setValue((int)((id/(double)count)*100));
				}
				if(!encoder.finish())
					return errorMsg;
			} else
				return errorMsg;
		} else
			return errorMsg;
		return null;
	}
	
	public static void save(final boolean preview, final String fileLocation, Project project, GifDecoder decoder, int gifWidth, int gifHeight) {
		List<Anchor> anchors = project.getAnchors();
		if(anchors == null || anchors.size() < 2) {
			GIFLooper.error("You need at least 2 anchors set to generate a GIF");
			return;
		}
		
		JPanel progressPanel = new JPanel(new MigLayout());
		progressPanel.add(new JLabel("Generating GIF, please wait...", JLabel.CENTER), "width 100%, wrap");
		final JProgressBar bar = new JProgressBar(0, 100);
		progressPanel.add(bar, "width 100%, wrap");
		
		GIFLooper.activeFrame.setVisible(false);
		final JFrame frame = new JFrame("Generation in Progress");
		frame.add(progressPanel);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.pack();
		frame.setSize(new Dimension(300, frame.getHeight()));
		frame.setVisible(true);
		frame.setResizable(false);
		GIFLooper.center(frame);
		
		final GIFSaver saver = new GIFSaver(fileLocation, project.getAnchors(), decoder, gifWidth, gifHeight);
		Thread thread = new Thread() {
			@Override
			public void run() {
				String error = saver.saveGif(bar);
				done(frame);
				if(preview)
					preview(fileLocation);
				else
					export(fileLocation);
				if(error != null)
					JOptionPane.showMessageDialog(GIFLooper.activeFrame, error, "Error", JOptionPane.ERROR_MESSAGE);
			}
		};
		thread.start();
	}
	
	public static void preview(String file) {
		try {
			new File(file).deleteOnExit();
			ImageIcon icon = new ImageIcon(new File(file).toURI().toURL());
			JFrame previewFrame = new JFrame("GIF Preview");
			previewFrame.add(new JLabel(icon));
			previewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			previewFrame.pack();
			previewFrame.setVisible(true);
			previewFrame.setResizable(false);
			GIFLooper.center(previewFrame);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void export(String file) {
		GIFLooper.message("Generated GIF saved to " + file);
	}
	
	public static void done(JFrame frame) {
		frame.setVisible(false);
		GIFLooper.activeFrame.setVisible(true);
	}
	
	public static double interp(double left, double right, double value) {
		return left*(1.0-value) + right*value;
	}
}

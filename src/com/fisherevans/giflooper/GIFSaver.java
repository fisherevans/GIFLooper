package com.fisherevans.giflooper;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
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
				int start = anchors.get(0).frameID;
				int end = anchors.get(anchors.size()-1).frameID;
				int count = end - start;
				boolean useCosine = App.project.settings.timeCosineInterpolation;
				bar.setMaximum(count*2);
				
				List<FrameTransform> transforms = new ArrayList<FrameTransform>(count);
				Rectangle bounds, shape = new Rectangle(0, 0, gifWidth, gifHeight);
				int x1 = Integer.MAX_VALUE;
				int y1 = Integer.MIN_VALUE;
				int x2 = Integer.MIN_VALUE;
				int y2 = Integer.MAX_VALUE;
				int anchorId = 0;
				for(int frameId = start;frameId <= end;frameId++) {
					Anchor left = anchors.get(anchorId);
					Anchor right = anchors.get(anchorId+1);
					double interpValue = (frameId-left.frameID)/(double)(right.frameID-left.frameID);
					
					FrameTransform transform = getInterpolatedTransformation(left, right, gifWidth, gifHeight, interpValue, useCosine, frameId);
					transforms.add(transform);
					
					bounds = transform.createTransformedShape(shape).getBounds();
					x1 = Math.min((int) bounds.getX(), x1);
					y1 = Math.max((int) bounds.getY(), y1);
					x2 = Math.max((int) (bounds.getX()+bounds.getWidth()), x2);
					y2 = Math.min((int) (bounds.getY()-bounds.getHeight()), y2);
					
                    if(frameId == right.frameID)
                        anchorId++;
			        bar.setValue((frameId-start)/bar.getMaximum()); // GUI
				}
				BufferedImage imgOut = new BufferedImage(x2-x1, y1-y2, BufferedImage.TYPE_INT_RGB);
				Graphics2D gfx = GraphicsUtil.getG2D(imgOut.createGraphics());
                gfx.setColor(Color.BLACK);
				for(FrameTransform transform:transforms) {
					BufferedImage img = decoder.getFrame(transform.getFrameId());
					//transform.translate(x1, y1);
					if(App.project.settings.clearEachFrame)
						gfx.fillRect(0, 0, imgOut.getWidth(), imgOut.getHeight());
			        gfx.drawImage(img, transform, null);
			        encoder.setDelay((int)(decoder.getDelay(transform.getFrameId())/App.project.settings.speed));
			        encoder.addFrame(imgOut);
			        bar.setValue((++count)/bar.getMaximum()); // GUI
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
		final JProgressBar bar = new JProgressBar(0, 1);
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
	
	public static FrameTransform getInterpolatedTransformation(Anchor left, Anchor right, int width, int height, double time, boolean cosine, int frameId) {
		// get the interpolated values from the two anchors
		double deltaX = interp(left.deltaX, right.deltaX, time, cosine);
		double deltaY = interp(left.deltaX, right.deltaX, time, cosine);
		double scaleX = interp(left.scaleX, right.scaleX, time, cosine);
		double scaleY = interp(left.scaleY, right.scaleY, time, cosine);
		double degrees = interp(left.degrees, right.degrees, time, cosine);
		
		// Create the AffineTransformation based on the two interpolated acnhors
		FrameTransform transform = new FrameTransform();
		transform.setFrameId(frameId);
		transform.translate(deltaX, deltaY);
		transform.scale(scaleX, scaleY);
		transform.rotate(Math.toRadians(degrees),
        		scaleX*width/2.0+deltaX,
        		scaleY*height/2.0+deltaY);
		
		return transform; // return it
	}
	
	public static double linearInterpolation(double left, double right, double time) {
		return left*(1.0-time) + right*time;
	}

	public static double cosineInterpolation(double left, double right, double time) {
		double result = (1.0 - Math.cos(time * Math.PI)) * 0.5;
		return linearInterpolation(left, right, result);
	}
	
	public static double interp(double left, double right, double value, boolean useCosine) {
		if(useCosine)
			return cosineInterpolation(left, right, value);
		else
			return linearInterpolation(left, right, value);
	}
	
	private static class FrameTransform extends AffineTransform {
		private int _frameId;
		
		public void setFrameId(int frameId) {
			_frameId = frameId;
		}
		
		public int getFrameId() {
			return _frameId;
		}
	}
}

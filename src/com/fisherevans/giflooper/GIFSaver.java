package com.fisherevans.giflooper;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
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

	public String saveGif2(JProgressBar bar) {
		String errorMsg = "Failed to save file to " + fileLocation;
		if(decoder != null) {
            boolean interpType = App.project.settings.timeCosineInterpolation;
			File saveFile = new File(fileLocation);
			Rectangle bounds = getBounds(anchors, gifWidth, gifHeight, interpType);
			bounds.y = bounds.height-gifHeight;
			System.out.println(bounds);
			GifEncoder encoder = new GifEncoder();
			encoder.setQuality(20);
			encoder.setRepeat(decoder.getLoopCount());
			if(encoder.start(saveFile.getAbsolutePath())) {
				double value, dx, dy, xScale, yScale, width, height, rotation;
				Anchor left, right;
				BufferedImage img, imgOut = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_RGB);
				Graphics2D gfx = GraphicsUtil.getG2D(imgOut.createGraphics());
                gfx.setColor(Color.BLACK);

				int count = anchors.get(anchors.size()-1).frameID - anchors.get(0).frameID;
				int start = anchors.get(0).frameID;
				int anchorId = 0;
				for(int id = 0;id <= count;id++) {
                    AffineTransform oldT = gfx.getTransform();
                    Composite oldC = gfx.getComposite();

					int frameId = start + id;
					left = anchors.get(anchorId);
					right = anchors.get(anchorId+1);
					img = decoder.getFrame(frameId);
					value = (frameId-left.frameID)/(double)(right.frameID-left.frameID);

                    xScale = interp(left.scaleX, right.scaleX, value, interpType);
                    yScale = interp(left.scaleY, right.scaleY, value, interpType);
                    width = gifWidth*xScale;
                    height = gifHeight*yScale;
					dx = interp(left.deltaX, right.deltaX, value, interpType) + (gifWidth-width)/2.0;
					dy = interp(left.deltaY, right.deltaY, value, interpType) + (gifHeight-height)/2.0;
                    rotation = interp(left.degrees, right.degrees, value, interpType);

					if(App.project.settings.clearEachFrame)
						gfx.fillRect(0, 0, imgOut.getWidth(), imgOut.getHeight());

					AffineTransform t = new AffineTransform();
			        t.translate(dx-bounds.x, dy+bounds.y);
			        t.scale(xScale, yScale);
			        t.rotate(Math.toRadians(rotation), width/2.0+dx, height/2.0+dy);
			        gfx.drawImage(img, t, null);

                    int delay = (int) (decoder.getDelay(frameId)/App.project.settings.speed);
					encoder.setDelay(delay < 1 ? 1 : delay);
					encoder.addFrame(imgOut);

					bar.setValue((int)((id/(double)count)*100));
                    if(frameId == right.frameID)
                        anchorId++;

                    gfx.setComposite(oldC);
                    gfx.setTransform(oldT);
				}
				if(!encoder.finish())
					return errorMsg;
			} else
				return errorMsg;
		} else
			return errorMsg;
		return null;
	}

	public String saveGif(JProgressBar bar) {
		String errorMsg = "Failed to save file to " + fileLocation;
		if(decoder != null) {
			File saveFile = new File(fileLocation);
			GifEncoder encoder = new GifEncoder();
			encoder.setQuality(20);
			encoder.setRepeat(decoder.getLoopCount());
			if(encoder.start(saveFile.getAbsolutePath())) {
				double value, dx, dy, xScale, yScale, width, height, rotation;
				Anchor left, right;
				BufferedImage img, imgOut = new BufferedImage(gifWidth, gifHeight, BufferedImage.TYPE_INT_RGB);
				Graphics2D gfx = GraphicsUtil.getG2D(imgOut.createGraphics());
                gfx.setColor(Color.BLACK);

                boolean interpType = App.project.settings.timeCosineInterpolation;
				int count = anchors.get(anchors.size()-1).frameID - anchors.get(0).frameID;
				int start = anchors.get(0).frameID;
				int anchorId = 0;
				for(int id = 0;id <= count;id++) {
                    AffineTransform oldT = gfx.getTransform();
                    Composite oldC = gfx.getComposite();

					int frameId = start + id;
					left = anchors.get(anchorId);
					right = anchors.get(anchorId+1);
					img = decoder.getFrame(frameId);
					value = (frameId-left.frameID)/(double)(right.frameID-left.frameID);

                    xScale = interp(left.scaleX, right.scaleX, value, interpType);
                    yScale = interp(left.scaleY, right.scaleY, value, interpType);
                    width = gifWidth*xScale;
                    height = gifHeight*yScale;
					dx = interp(left.deltaX, right.deltaX, value, interpType) + (gifWidth-width)/2.0;
					dy = interp(left.deltaY, right.deltaY, value, interpType) + (gifHeight-height)/2.0;
                    rotation = interp(left.degrees, right.degrees, value, interpType);

					if(App.project.settings.clearEachFrame)
						gfx.fillRect(0, 0, imgOut.getWidth(), imgOut.getHeight());

					AffineTransform t = new AffineTransform();
			        t.translate(dx, dy);
			        t.scale(xScale, yScale);
			        t.rotate(Math.toRadians(rotation), width/2.0+dx, height/2.0+dy);
			        gfx.drawImage(img, t, null);

                    int delay = (int) (decoder.getDelay(frameId)/App.project.settings.speed);
					encoder.setDelay(delay < 1 ? 1 : delay);
					encoder.addFrame(imgOut);

					bar.setValue((int)((id/(double)count)*100));
                    if(frameId == right.frameID)
                        anchorId++;

                    gfx.setComposite(oldC);
                    gfx.setTransform(oldT);
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
	
	public static Rectangle getBounds(List<Anchor> anchors, int width, int height, boolean cosine) {
		Rectangle globalBounds = null, localBounds;
		for(int x = 0;x < anchors.size()-1;x++) {
			localBounds = getBounds(anchors.get(x), anchors.get(x+1), width, height, cosine);
			if(globalBounds == null)
				globalBounds = localBounds;
			else
				globalBounds.add(localBounds);
		}
		return globalBounds;
	}
	
	public static Rectangle getBounds(Anchor leftAnchor, Anchor rightAnchor, int shapeWidth, int shapeHeight, boolean cosine) {
		Rectangle baseShape = new Rectangle(0, 0, shapeWidth, shapeHeight); // the shape we draw in the animation
		Rectangle localBounds = null, anchorBounds; // global bounds is the bounds of the whole animation
		double timeStep = 1.0/((double)(rightAnchor.frameID-leftAnchor.frameID)); // step for each frame for the animation
		for(double time = 0;time <= 1;time += timeStep) { // interpolate from one anchor to the next (1000 steps)
			// Create the transformation and find the finds of the resultant shape
			AffineTransform transformation = getInterpolatedTransformation(leftAnchor, rightAnchor, shapeWidth, shapeHeight, time, cosine);
			anchorBounds = transformation.createTransformedShape(baseShape).getBounds();
			if(localBounds == null) // if it's the first step, create the inital bounds
				localBounds = anchorBounds;
			else // otherwise continue adding bounds
				localBounds.add(anchorBounds);
		}
		return localBounds; // return the global bounds
	}
	
	public static AffineTransform getInterpolatedTransformation(Anchor left, Anchor right, int width, int height, double time, boolean cosine) {
		// get the interpolated values from the two anchors
		double deltaX = interp(left.deltaX, right.deltaX, time, cosine);
		double deltaY = interp(left.deltaX, right.deltaX, time, cosine);
		double scaleX = interp(left.scaleX, right.scaleX, time, cosine);
		double scaleY = interp(left.scaleY, right.scaleY, time, cosine);
		double degrees = interp(left.degrees, right.degrees, time, cosine);
		
		// Create the AffineTransformation based on the two interpolated acnhors
		AffineTransform transform = new AffineTransform();
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
}

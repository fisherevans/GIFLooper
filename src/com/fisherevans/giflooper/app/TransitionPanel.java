package com.fisherevans.giflooper.app;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.fisherevans.giflooper.App;
import com.fisherevans.giflooper.GraphicsUtil;
import com.fisherevans.giflooper.app.components.Anchor;
import com.fisherevans.giflooper.app.events.EventRouter.EventRouterListener;
import com.fisherevans.giflooper.app.events.EventRouter;
import com.fisherevans.giflooper.app.events.EventType;

import net.miginfocom.swing.MigLayout;

public class TransitionPanel extends JPanel implements ChangeListener, EventRouterListener {
	private TransitionViewer _viewer;
	private double _alpha = 0.5;
	
	public TransitionPanel() {
		super(new MigLayout("fill"));
		createSlider();
		_viewer = new TransitionViewer();
		add(_viewer, "width 100%, height 96%");

		EventRouter.addListener(this, EventType.ActiveAnchorUpdated);
		EventRouter.addListener(this, EventType.AnchorSelected);
	}
	
	private void createSlider() {
		add(new JLabel("Transition", JLabel.CENTER), "height 2%, width 100%, wrap");

		JSlider transitionSlider = new JSlider(-100, 100, 50);
		transitionSlider.addChangeListener(this);
		JPanel sliderPanel = new JPanel(new MigLayout("fill"));
		sliderPanel.add(new JLabel("Left", JLabel.RIGHT), "gapleft 20%, width 5%");
		sliderPanel.add(transitionSlider, "width 50%");
		sliderPanel.add(new JLabel("Right", JLabel.LEFT), "gapright 20%, width 5%");

		add(sliderPanel, "height 2%, width 100%, wrap");
	}

	@Override
	public void stateChanged(ChangeEvent event) {
		_alpha = ((JSlider)event.getSource()).getValue()/100.0;
		repaint();
	}

	@Override
	public void event(EventType eventType, Object source, Object obj) {
		if(eventType == EventType.ActiveAnchorUpdated
				|| eventType == EventType.AnchorSelected)
			repaint();
	}
	
	public class TransitionViewer extends JPanel {
		public TransitionViewer() {
			super(new MigLayout("fill"));
			setBackground(Color.WHITE);
		}

	    @Override
	    protected void paintComponent(Graphics g) {
	    	super.paintComponent(g);
			Graphics2D g2d = GraphicsUtil.getG2D(g);
			
			if(App.current.activeAnchor == null)
				return;

	        Anchor thisAnchor = App.current.activeAnchor;
	        Anchor otherAnchor = App.project.getNextAnchor(_alpha < 0 ? -1 : 1);
			BufferedImage thisImg = App.current.decoder.getFrame(thisAnchor.frameID);
			BufferedImage otherImg = App.current.decoder.getFrame(otherAnchor.frameID);
			double alpha = Math.abs(_alpha);
			
			double globalScale = getWidth()/(double)App.current.gifWidth;
			if(globalScale > 1)
				globalScale = 1;

			double globalDX = (getWidth()-App.current.gifWidth*globalScale)/2.0;
			double globalDY = (getHeight()-App.current.gifHeight*globalScale)/2.0;

			draw(g2d, otherAnchor, otherImg, globalScale, globalDX, globalDY, 1);
			draw(g2d, thisAnchor, thisImg, globalScale, globalDX, globalDY, 1f-(float)alpha);
		}
	    
	    private void draw(Graphics2D g2d, Anchor anchor, BufferedImage img, double globalScale, double globalDX, double globalDY, float alpha) {
			AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
			g2d.setComposite(ac);
			double dx = anchor.deltaX;
			double dy = anchor.deltaY;
			double xscale = anchor.scaleX*globalScale;
			double yscale = anchor.scaleY*globalScale;
			double width = App.current.gifWidth*xscale;
			double height = App.current.gifHeight*yscale;
			dx += (App.current.gifWidth-width)/2.0 + globalDX;
			dy += (App.current.gifHeight-height)/2.0 + globalDY;
			
			g2d.rotate(Math.toRadians(anchor.degrees), width/2.0 + dx, height/2.0 + dy);
			AffineTransform at = new AffineTransform();
			at.translate(dx, dy);
			at.scale(xscale, yscale);
	        g2d.drawImage(img, at, null);
	    }
	}
}

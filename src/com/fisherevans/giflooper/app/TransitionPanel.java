package com.fisherevans.giflooper.app;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
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
    private JSlider _transition;
	
	public TransitionPanel() {
		super(new MigLayout("fill"));
		setBackground(Color.WHITE);
		createSlider();
		_viewer = new TransitionViewer();
		add(_viewer, "width 100%, height 96%");

		EventRouter.addListener(this, EventType.ActiveAnchorUpdated);
		EventRouter.addListener(this, EventType.AnchorSelected);
	}
	
	private void createSlider() {
		add(new JLabel("Transition", JLabel.CENTER), "height 2%, width 100%, wrap");

        _transition = new JSlider(-100, 100, 50);
        _transition.addChangeListener(this);
		JPanel sliderPanel = new JPanel(new MigLayout("fill"));
		sliderPanel.setBackground(Color.WHITE);
		sliderPanel.add(new JLabel("Left", JLabel.RIGHT), "gapleft 20%, width 5%");
		sliderPanel.add(_transition, "width 50%");
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
			
			double globalScale = Math.min(getWidth()/(double)App.current.gifWidth,
					getHeight()/(double)App.current.gifHeight);
			if(globalScale > 1)
				globalScale = 1;

			double globalDX = (getWidth()-App.current.gifWidth*globalScale)/2.0;
			double globalDY = (getHeight()-App.current.gifHeight*globalScale)/2.0;

			draw(g2d, thisAnchor, thisImg, globalScale, globalDX, globalDY, 1);
			draw(g2d, otherAnchor, otherImg, globalScale, globalDX, globalDY, (float)alpha);
			drawBorder(g2d, globalScale, globalDX, globalDY);
		}
	    
	    private void draw(Graphics2D g2d, Anchor anchor, BufferedImage img, double globalScale, double globalDX, double globalDY, float alpha) {
	    	Composite oldC = g2d.getComposite();
			AffineTransform oldT = g2d.getTransform();
	    	
			AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
			g2d.setComposite(ac);
			double dx = anchor.deltaX;
			double dy = anchor.deltaY;
			double scaleX = anchor.scaleX*globalScale;
			double scaleY = anchor.scaleY*globalScale;
			double width = App.current.gifWidth*scaleX;
			double height = App.current.gifHeight*scaleY;
			dx += globalDX;
			dy += globalDY;

			g2d.rotate(Math.toRadians(anchor.degrees), width/2.0 + dx, height/2.0 + dy);
			AffineTransform at = new AffineTransform();
			at.translate(dx*globalScale, dy*globalScale);
			at.scale(scaleX, scaleY);
	        g2d.drawImage(img, at, null);
	        
	        g2d.setTransform(oldT);
	        g2d.setComposite(oldC);
	    }
	    
	    private void drawBorder(Graphics2D g2d, double globalScale, double globalDX, double globalDY) {
			double width = App.current.gifWidth*globalScale;
			double height = App.current.gifHeight*globalScale;
			g2d.setColor(Color.BLACK);
	        g2d.setStroke(new BasicStroke(2f));
	        g2d.draw(new Rectangle2D.Double(globalDX*globalScale+1, globalDY*globalScale+1, width-2, height-2));
	    }
	}
}

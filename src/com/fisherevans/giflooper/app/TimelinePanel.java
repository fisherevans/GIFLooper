package com.fisherevans.giflooper.app;

import com.fisherevans.giflooper.GIFLooper;
import com.fisherevans.giflooper.GraphicsUtil;
import com.fisherevans.giflooper.App;
import com.fisherevans.giflooper.app.components.Anchor;
import com.fisherevans.giflooper.app.components.Project;
import com.fisherevans.giflooper.app.events.EventRouter;
import com.fisherevans.giflooper.app.events.EventType;
import com.fisherevans.giflooper.app.events.EventRouter.EventRouterListener;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

public class TimelinePanel extends JPanel implements ActionListener, MouseMotionListener, MouseListener, EventRouterListener {
    private static final Color ODD_IDLE = new Color(230, 230, 230);
    private static final Color EVEN_IDLE = new Color(215, 215, 215);

    private static final Color ODD_HOVER = new Color(182, 195, 214);
    private static final Color EVEN_HOVER = new Color(195, 210, 230);

    private static final Color ODD_ANCHOR_IDLE = new Color(107, 152, 214);
    private static final Color EVEN_ANCHOR_IDLE = new Color(115, 163, 230);

    private static final Color ODD_ANCHOR_HOVER = new Color(54, 120, 214);
    private static final Color EVEN_ANCHOR_HOVER = new Color(57, 129, 230);

    private static final Color ODD_SELECTED_IDLE = new Color(214, 170, 107);
    private static final Color EVEN_SELECTED_IDLE = new Color(230, 182, 115);

    private static final Color ODD_SELECTED_HOVER = new Color(214, 148, 54);
    private static final Color EVEN_SELECTED_HOVER = new Color(230, 159, 58);

    public static final int HEIGHT = 100;

    private JPopupMenu _bgPopup, _anchorPopup;
    private JMenuItem _newAnchor, _pasteAnchor, _copyAnchor, _deleteAnchor;

    private int _thumbWidth, _thumbHeight;
    private double _anchorWidth = 1;

    private int _mx, _my;

    private Anchor _copiedAnchor = null;

    public TimelinePanel() {
        EventRouter.addListener(this, EventType.ActiveAnchorUpdated);

        setBackground(Color.BLACK);
        createPopups();
        
        addMouseMotionListener(this);
        addMouseListener(this);

        _thumbHeight = HEIGHT;
        _thumbWidth = (int) ((App.current.gifWidth/((double)App.current.gifHeight))*HEIGHT);
    }

    private void createPopups() {
        _bgPopup = new JPopupMenu();
        _newAnchor = getPopupItem("New Anchor");
        _pasteAnchor = getPopupItem("Paste Anchor");
        _pasteAnchor.setEnabled(false);
        _bgPopup.add(_newAnchor);
        _bgPopup.add(_pasteAnchor);

        _anchorPopup = new JPopupMenu();
        _copyAnchor = getPopupItem("Copy Anchor");
        _deleteAnchor = getPopupItem("Delete Anchor");
        _anchorPopup.add(_copyAnchor);
        _anchorPopup.add(_deleteAnchor);
    }

    private JMenuItem getPopupItem(String label) {
        JMenuItem item = new JMenuItem(label);
        item.addActionListener(this);
        return item;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = GraphicsUtil.getG2D(g);
        _anchorWidth = (getWidth()-_thumbWidth)/(double)App.project.frameCount;
        boolean even, hover;
        Color c;
        int frameId = -1;
        double did;
        for(int id = 0;id < App.project.frameCount;id++) {
            did = id;
            even = id % 2 == 0;
            hover = _mx >= did*_anchorWidth && _mx < (did+1.0)*_anchorWidth;
            c = getColor(even, hover, App.project.anchorByFrameID(id) != null, App.current.activeAnchor == App.project.anchorByFrameID(id));
            g2d.setColor(c);
            g2d.fill(new Rectangle2D.Double(did*_anchorWidth, 0, _anchorWidth, HEIGHT));
            g2d.setColor(Color.BLACK);
            g2d.fill(new Rectangle2D.Double((did+1)*_anchorWidth - 1, 0, 1, HEIGHT));
            if(hover)
                frameId = id;
        }
        if(frameId != -1) {
			AffineTransform t = new AffineTransform();
	        double scale = HEIGHT/(double)App.current.gifHeight;
	        t.scale(scale, scale);
	        t.translate(getWidth()/scale - (double)App.current.gifWidth, 0);
	        g2d.drawImage(App.current.decoder.getFrame(frameId), t, null);
        }
        g2d.setColor(Color.BLACK);
        g2d.fill(new Rectangle2D.Double(App.project.frameCount*_anchorWidth - 1, 0, 2, HEIGHT));
    }

    private Color getColor(boolean even, boolean hover, boolean anchor, boolean selected) {
        if(even) {
            if(hover) {
                if(anchor) {
                	if(selected)
                		return EVEN_SELECTED_HOVER;
                	else 
                		return EVEN_ANCHOR_HOVER;
                } else
                    return EVEN_HOVER;
            } else {
                if(anchor) {
                	if(selected)
                		return EVEN_SELECTED_IDLE;
                	else 
                		return EVEN_ANCHOR_IDLE;
                } else
                    return EVEN_IDLE;
            }
        } else {
            if(hover) {
                if(anchor) {
                	if(selected)
                		return ODD_SELECTED_HOVER;
                	else 
                		return ODD_ANCHOR_HOVER;
                } else
                    return ODD_HOVER;
            } else {
                if(anchor) {
                	if(selected)
                		return ODD_SELECTED_IDLE;
                	else 
                		return ODD_ANCHOR_IDLE;
                } else
                    return ODD_IDLE;
            }
        }
    }

    private int getMouseID() {
        return getMouseID(_mx);
    }

    private int getMouseID(int mx) {
        if(mx < App.project.frameCount*_anchorWidth)
            return (int) (mx/_anchorWidth);
        else
            return -1;
    }

    private void hoverOnSelected() {
        _mx = (int) ((App.current.activeAnchor.frameID+0.5)*_anchorWidth);
        _my = HEIGHT/2;
        repaint();
    }

	@Override
	public void event(EventType eventType, Object source, Object obj) {
        if(eventType == EventType.ActiveAnchorUpdated)
            hoverOnSelected();
	}

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == _newAnchor) {
        	EventRouter.event(this, EventType.AnchorAdded, new Anchor(getMouseID()));
        } else if(e.getSource() == _pasteAnchor) {
            if(_copiedAnchor != null)
            	EventRouter.event(this, EventType.AnchorAdded, _copiedAnchor.copy(getMouseID()));
        } else if(e.getSource() == _copyAnchor) {
            _copiedAnchor = App.project.anchorByFrameID(getMouseID());
            _pasteAnchor.setEnabled(_copiedAnchor != null);
        } else if(e.getSource() == _deleteAnchor) {
        	EventRouter.event(this, EventType.AnchorDeleted, App.project.anchorByFrameID(getMouseID()));
        	EventRouter.event(this, EventType.AnchorSelected, null);
        }
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(e.getX() <= 0)
            return;
        int id = getMouseID();
        Anchor anchor = App.project.anchorByFrameID(id);
        int nextId = getMouseID(e.getX());
        if(id != -1 && nextId != -1) {
            Anchor nextAnchor = App.project.anchorByFrameID(nextId);
            if(anchor != null && nextAnchor == null) {
                _mx = e.getX();
                _my = e.getY();
                anchor.frameID = nextId;
                App.project.sort();
                EventRouter.event(this, EventType.ActiveAnchorUpdated, null);
            }
            if(anchor == null) {
                _mx = e.getX();
                _my = e.getY();
                repaint();
            }
        }
        if(anchor == null)
            repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    	if(_bgPopup.isVisible() || _anchorPopup.isVisible())
    		return;
    	if(e.getX() >= App.project.frameCount*_anchorWidth)
    		mouseExited(e);
    	else {
            _mx = e.getX();
            _my = e.getY();
            repaint();
    	}
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        _mx = e.getX();
        _my = e.getY();
        if(e.getButton() == MouseEvent.BUTTON3) {
            if(e.getX() < App.project.frameCount*_anchorWidth) {
                int id = getMouseID();
                Anchor anchor = App.project.anchorByFrameID(id);
                if(anchor == null) {
                    _bgPopup.show(this, e.getX(), e.getY());
                } else {
                    _anchorPopup.show(this, e.getX(), e.getY());
                }
            }
        }
        if(e.getButton() == MouseEvent.BUTTON1) {
            if(e.getX() < App.project.frameCount*_anchorWidth) {
                int id = getMouseID();
                Anchor anchor = App.project.anchorByFrameID(id);
                if(anchor != null)
                	EventRouter.event(this, EventType.AnchorSelected, anchor);
            }
        }
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {
    	if(_bgPopup.isVisible() || _anchorPopup.isVisible() || App.current.activeAnchor == null)
    		return;
        hoverOnSelected();
    }
}

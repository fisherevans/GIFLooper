package com.fisherevans.giflooper.panels.project.timeline;

import com.fisherevans.giflooper.components.Anchor;
import com.fisherevans.giflooper.components.Project;
import com.fisherevans.giflooper.panels.project.ProjectPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Rectangle2D;

public class TimelinePanel extends JPanel implements ActionListener, MouseMotionListener, MouseListener {
    private static Color ODD_IDLE = new Color(0.3019608f, 0.3019608f, 0.3019608f);
    private static Color EVEN_IDLE = new Color(0.4509804f, 0.4509804f, 0.4509804f);

    private static Color ODD_HOVER = new Color(0.34901962f, 0.34901962f, 0.34901962f);
    private static Color EVEN_HOVER = new Color(0.5019608f, 0.5019608f, 0.5019608f);

    private static Color ODD_ANCHOR_IDLE = new Color(0.47058824f, 0.69803923f, 0.9411765f);
    private static Color EVEN_ANCHOR_IDLE = new Color(0.4f, 0.5882353f, 0.8f);

    private static Color ODD_ANCHOR_HOVER = new Color(0.7529412f, 0.84705883f, 0.9411765f);
    private static Color EVEN_ANCHOR_HOVER = new Color(0.6392157f, 0.7176471f, 0.8f);

    private static int HEIGHT = 150;

    private ProjectPanel _projectPanel;

    private JPopupMenu _bgPopup, _anchorPopup;
    private JMenuItem _newAnchor, _pasteAnchor, _copyAnchor, _deleteAnchor;

    private int _thumbWidth, _thumbHeight;
    private double _anchorWidth = 1;

    private int _mx, _my;

    private Anchor _copiedAnchor = null;

    public TimelinePanel(ProjectPanel projectPanel) {
        _projectPanel = projectPanel;
        setBackground(Color.BLACK);
        createPopups();
        //setComponentPopupMenu(_bgPopup);
        addMouseMotionListener(this);
        addMouseListener(this);

        _thumbHeight = HEIGHT;
        _thumbWidth = (int) ((_projectPanel.width/((double)_projectPanel.height))*HEIGHT);
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
        Graphics2D g2d = (Graphics2D)g;
        _anchorWidth = (getWidth()-_thumbWidth)/(double)_projectPanel.project.frameCount;
        boolean even, hover;
        Color c;
        int frameId = -1;
        double did;
        for(int id = 0;id < _projectPanel.project.frameCount;id++) {
            did = id;
            even = id % 2 == 0;
            hover = _mx >= did*_anchorWidth && _mx < (did+1.0)*_anchorWidth;
            c = getColor(even, hover, Project.get.anchorByFrameID(id) != null);
            g2d.setColor(c);
            g2d.fill(new Rectangle2D.Double(did*_anchorWidth, 0, _anchorWidth, HEIGHT/2.0));
            if(hover)
                frameId = id;
        }
        if(frameId != -1) {
            g2d.drawImage(_projectPanel.decoder.getFrame(frameId),
                    getWidth()-_thumbWidth, 0, _thumbWidth, _thumbHeight, null);
        }
    }

    private Color getColor(boolean even, boolean hover, boolean anchor) {
        if(even) {
            if(hover) {
                if(anchor)
                    return EVEN_ANCHOR_HOVER;
                else
                    return EVEN_HOVER;
            } else {
                if(anchor)
                    return EVEN_ANCHOR_IDLE;
                else
                    return EVEN_IDLE;
            }
        } else {
            if(hover) {
                if(anchor)
                    return ODD_ANCHOR_HOVER;
                else
                    return ODD_HOVER;
            } else {
                if(anchor)
                    return ODD_ANCHOR_IDLE;
                else
                    return ODD_IDLE;
            }
        }
    }

    private int getMouseID() {
        return getMouseID(_mx);
    }

    private int getMouseID(int mx) {
        if(mx < _projectPanel.project.frameCount*_anchorWidth)
            return (int) (mx/_anchorWidth);
        else
            return -1;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == _newAnchor) {
            Project.get.addNewAnchor(getMouseID());
        } else if(e.getSource() == _pasteAnchor) {
            if(_copiedAnchor != null) {
                Project.get.addAnchorCopy(_copiedAnchor, getMouseID());
            }
        } else if(e.getSource() == _copyAnchor) {
            _copiedAnchor = Project.get.anchorByFrameID(getMouseID());
            _pasteAnchor.setEnabled(_copiedAnchor != null);
        } else if(e.getSource() == _deleteAnchor) {
            Project.get.removeAnchorByID(getMouseID());
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int id = getMouseID();
        Anchor anchor = Project.get.anchorByFrameID(id);
        int nextId = getMouseID(e.getX());
        if(id != -1 && nextId != -1) {
            Anchor nextAnchor = Project.get.anchorByFrameID(nextId);
            if(anchor != null && nextAnchor == null) {
                _mx = e.getX();
                _my = e.getY();
                anchor.frameID = nextId;
                Project.get.sort();
            }
            repaint();
        }
        if(anchor == null)
            repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        _mx = e.getX();
        _my = e.getY();
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        _mx = e.getX();
        _my = e.getY();
        if(e.getButton() == MouseEvent.BUTTON3) {
            if(e.getX() < _projectPanel.project.frameCount*_anchorWidth) {
                int id = getMouseID();
                Anchor anchor = Project.get.anchorByFrameID(id);
                if(anchor == null) {
                    _bgPopup.show(this, e.getX(), e.getY());
                } else {
                    _anchorPopup.show(this, e.getX(), e.getY());
                }
            }
        }
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

    }
}

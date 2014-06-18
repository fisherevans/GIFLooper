package com.fisherevans.giflooper.components;

import java.io.Serializable;
import java.util.*;

public class Project implements Serializable {
	private static final long serialVersionUID = -2028594137369529380L;
	
	public int frameCount;
	public List<Anchor> _anchors;
	public Settings settings;

    public static Project get;
	
	public Project(int frameCount) {
		this.frameCount = frameCount;
		_anchors = new ArrayList<Anchor>();
		loadDefaults();
		settings = new Settings();

        get = this;
	}
	
	private void loadDefaults() {
        addNewAnchor(0);
        addNewAnchor(frameCount-1);
	}

    public boolean addNewAnchor(int frameId) {
        return addAnchor(new Anchor(frameId));
    }

    public boolean addAnchorCopy(Anchor anchor, int frameId) {
        return addAnchor(anchor.copy(frameId));
    }

    private boolean addAnchor(Anchor anchor) {
        for(Anchor temp:_anchors)
            if(temp.frameID == anchor.frameID)
                return false;
        _anchors.add(anchor);
        Collections.sort(_anchors);
        return true;
    }

    public Anchor anchorByFrameID(int frameID) {
        for(Anchor anchor:_anchors) {
            if(anchor.frameID == frameID)
                return anchor;
            else if(anchor.frameID > frameID)
                return null;
        }
        return null;
    }

    public void removeAnchorByID(int frameID) {
        for(int i = 0;i < _anchors.size();i++) {
            if(_anchors.get(i).frameID == frameID) {
                _anchors.remove(i);
                return;
            }
        }
    }

    public void sort() {
        Collections.sort(_anchors);
    }
}

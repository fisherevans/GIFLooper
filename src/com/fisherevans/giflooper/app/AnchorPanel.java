package com.fisherevans.giflooper.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import com.fisherevans.giflooper.App;
import com.fisherevans.giflooper.app.components.Anchor;
import com.fisherevans.giflooper.app.events.EventRouter;
import com.fisherevans.giflooper.app.events.EventRouter.EventRouterListener;
import com.fisherevans.giflooper.app.events.EventType;

public class AnchorPanel extends JPanel implements EventRouterListener {
	public static final int WIDTH = 300;
	
	private Map<String, InputRow> _inputs;
	private Anchor _anchor;
	
	public AnchorPanel() {
		super(new MigLayout("fillx"));
		EventRouter.addListener(this, EventType.ActiveAnchorUpdated);
		_inputs = new HashMap<String, InputRow>();
		
		addRow("frame", "Frame ID", 0, App.project.frameCount, 0, 1);
		addRow("deltax", "Shift X", -App.current.gifWidth, App.current.gifWidth, 0, 8);
		addRow("deltay", "Shift Y", -App.current.gifHeight, App.current.gifHeight, 0, 8);
		addRow("scalex", "Scale X", 0, 2, 1, 100);
		addRow("scaley", "Scale Y", 0, 2, 1, 100);
		addRow("degrees", "Rotation", -360, 360, 0, 10);
		
		setAnchor(null);
	}
	
	private void addRow(String code, String display, int low, int high, int start, int accuracy) {
		InputRow row = new InputRow(code, display, low, high, start, accuracy);
		_inputs.put(code, row);
		this.add(row, "width 100%, wrap");
	}
	
	private void update(String code, double value) {
		if("frame".equals(code))
			App.current.activeAnchor.frameID = (int) value;
		else if("deltax".equals(code))
			App.current.activeAnchor.deltaX = value;
		else if("deltay".equals(code))
			App.current.activeAnchor.deltaY = value;
		else if("scalex".equals(code))
			App.current.activeAnchor.scaleX = value;
		else if("scaley".equals(code))
			App.current.activeAnchor.scaleY = value;
		else if("degrees".equals(code))
			App.current.activeAnchor.degrees = value;
		else
			return;
		
		EventRouter.event(this, EventType.ActiveAnchorUpdated, null);
	}
	
	public void setAnchor(Anchor anchor) {
		_anchor = anchor;
		boolean isNull = _anchor == null;
		for(InputRow row:_inputs.values())
			row.setDisabled(isNull);
		if(!isNull) {
			_inputs.get("frame").setValue(_anchor.frameID);
			_inputs.get("deltax").setValue(_anchor.deltaX);
			_inputs.get("deltay").setValue(_anchor.deltaY);
			_inputs.get("scalex").setValue(_anchor.scaleX);
			_inputs.get("scaley").setValue(_anchor.scaleY);
			_inputs.get("degrees").setValue(_anchor.degrees);
		}
	}

	@Override
	public void event(EventType eventType, Object source, Object obj) {
		if(source != this && eventType == EventType.ActiveAnchorUpdated)
			setAnchor(App.current.activeAnchor);
	}
	
	public class InputRow extends JPanel implements ChangeListener, KeyListener {
		public JLabel label;
		public JSlider slider;
		public JTextField field;
		public String code, display;
		public int accuracy;
		public int low, high;
		
		private boolean _progChange = false;
		
		public InputRow(String code, String display, int low, int high, int start, int accuracy) {
			super(new MigLayout("fillx"));
			
			this.code = code;
			this.display = display;
			this.low = low;
			this.high = high;
			this.accuracy = accuracy;
			
			label = new JLabel(display);
			
			slider = new JSlider(low*accuracy, high*accuracy, start*accuracy);
			slider.addChangeListener(this);
			
			field = new JTextField(start + "");
			field.addKeyListener(this);
			
			add(label, "width 25%");
			add(slider, "width 50%");
			add(field, "width 25%");
		}
		
		public void setDisabled(boolean disable) {
			slider.setEnabled(!disable);
			field.setEnabled(!disable);
		}
		
		public double getValue() {
			double value = slider.getValue()/(double)accuracy;
			if(value < low)
				return low;
			if(value > high)
				return high;
			return value;
		}
		
		public void sendUpdate() {
			AnchorPanel.this.update(code, getValue());
		}
		
		public void setValue(double value) {
			if(value < low)
				value = low;
			if(value > high)
				value = high;
			setField(value);
			setSlider(value);
		}
		
		private void setField(double value) {
			if(value % 1 == 0)
				field.setText(String.format("%d", (int)value));
			else
				field.setText(String.format("%.2f", value));
		}
		
		private void setSlider(double value) {
    		_progChange = true;
			slider.setValue((int)(accuracy*value));
	    	_progChange = false;
		}

		// SLIDER
		@Override
		public void stateChanged(ChangeEvent event) {
			if(_progChange)
				return;
			setField(getValue());
			sendUpdate();
		}

		// FIELD
		@Override
		public void keyReleased(KeyEvent event) {
		    try {
		    	Double value = new Double(field.getText());
		    	if(value >= low && value <= high) {
			    	setSlider(value);
					sendUpdate();
		    	}
		    } catch(Exception e) { }
		}
		
		@Override
		public void keyPressed(KeyEvent event) { }
		
		@Override
		public void keyTyped(KeyEvent event) { }
	}

}

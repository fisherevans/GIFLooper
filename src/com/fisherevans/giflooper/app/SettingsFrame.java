package com.fisherevans.giflooper.app;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.fisherevans.giflooper.App;
import com.fisherevans.giflooper.GIFLooper;
import net.miginfocom.swing.MigLayout;

public class SettingsFrame extends JFrame implements ActionListener {
    private final static int SPEED_SCALE = 100;

	private JPanel _rootPanel, _speedPanel, _buttonPanel;
	private JCheckBox _clear, _aa, _graphics, _cosine;
	private JLabel _speedLabel;
	private JSlider _speedSlider;
    private JTextField _speedField;
    private JButton _cancel, _ok;
	
	public SettingsFrame() {
		super("GIFLooper Settings");
	    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                GIFLooper.activeFrame.setVisible(true);
            }
        });

        loadComponents();
        pack();
        setSize(new Dimension(400, getHeight()));
        setResizable(false);

        GIFLooper.activeFrame.setVisible(false);
        setVisible(true);
        GIFLooper.center(this);
	}

    private void loadComponents() {
        _rootPanel = new JPanel(new MigLayout("fillx"));
        _clear = new JCheckBox("Clear GIF Each Frame", App.project.settings.clearEachFrame);
        _aa = new JCheckBox("Use Antialiasing", App.project.settings.aa);
        _graphics = new JCheckBox("Use Graphical Interpolation", App.project.settings.graphicsInterpolation);
        _cosine = new JCheckBox("Use Cosine Time Interpolation", App.project.settings.timeCosineInterpolation);
        _rootPanel.add(_clear, "width 100%, wrap");
        _rootPanel.add(_aa, "width 100%, wrap");
        _rootPanel.add(_graphics, "width 100%, wrap");
        _rootPanel.add(_cosine, "width 100%, wrap");

        _speedPanel = new JPanel(new MigLayout("fillx"));
        _speedLabel = new JLabel("Exported GIF Speed");
        _speedSlider = new JSlider(1, 4*SPEED_SCALE, 1);
        _speedField = new JTextField();
        _speedField.setEditable(false);
        _speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                _speedField.setText(String.format("%.2f", _speedSlider.getValue() / (double) SPEED_SCALE));
            }
        });
        _speedSlider.setValue((int) (App.project.settings.speed * SPEED_SCALE));
        _speedPanel.add(_speedLabel, "width 20%");
        _speedPanel.add(_speedSlider, "width 60%");
        _speedPanel.add(_speedField, "width 20%, wrap");
        _rootPanel.add(_speedPanel, "width 100%, wrap");

        _buttonPanel = new JPanel(new MigLayout("fillx"));
        _ok = new JButton("Okay");
        _ok.addActionListener(this);
        _cancel = new JButton("Cancel");
        _cancel.addActionListener(this);
        _buttonPanel.add(_ok, "gapleft 10%, width 30%, gapright 10%");
        _buttonPanel.add(_cancel, "gapleft 10%, width 30%, gapright 10%, wrap");
        _rootPanel.add(_buttonPanel, "width 100%, wrap");

        add(_rootPanel);
    }
	
	public void save() {
        App.project.settings.clearEachFrame = _clear.isSelected();
        App.project.settings.aa = _aa.isSelected();
        App.project.settings.graphicsInterpolation = _graphics.isSelected();
        App.project.settings.timeCosineInterpolation = _cosine.isSelected();
        App.project.settings.speed = _speedSlider.getValue()/(double)SPEED_SCALE;
        close();
	}

    private void close() {
        WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == _ok)
            save();
        else
            close();
	}
}

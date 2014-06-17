package com.fisherevans.giflooper.panels;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.fisherevans.giflooper.GIFLooper;

import net.miginfocom.swing.MigLayout;

public class OpenPanel extends JPanel {
	private static final long serialVersionUID = -2146004416751024171L;
	
	private FileChooserListener _fileChooserListener;
	private OpenListener _openListener;
	
	private JLabel _gifLabel, _settingsLabel, _messageLabel;
	private JTextField _gifField, _settingsField;
	private JButton _gifButton, _settingsButton, _openButton;
	private JFileChooser _fileChooser;
	
	private JPanel _fileChooserPanel;
	
	private File _gif = null, _settings = null;
	
	public OpenPanel() {
		super(new MigLayout("fillx"));

		_fileChooserListener = new FileChooserListener();
		_openListener = new OpenListener();
		
		_gifLabel = getLabel("Animated GIF:");
		_settingsLabel = getLabel("Project File:");
		
		_messageLabel = new JLabel("Select an animated GIF above.", JLabel.CENTER);

		_gifField = getField();
		_settingsField = getField();

		_gifButton = getButton("Browse...", _fileChooserListener);
		_settingsButton = getButton("Browse...", _fileChooserListener);
		_openButton = getButton("Open", _openListener);
		
		_fileChooser = new JFileChooser();
		
		_fileChooserPanel = new JPanel(new MigLayout("fillx"));
		
		createLayout();
	}
	
	private void createLayout() {
		_fileChooserPanel.add(_gifLabel, "width 10%");
		_fileChooserPanel.add(_gifField, "width 60%");
		_fileChooserPanel.add(_gifButton, "width 30%, wrap");
		
		_fileChooserPanel.add(_settingsLabel, "width 10%");
		_fileChooserPanel.add(_settingsField, "width 60%");
		_fileChooserPanel.add(_settingsButton, "width 30%, wrap");

		add(_fileChooserPanel, "width 100%, wrap");
		add(_messageLabel, "gapbottom 10, width 100%, wrap");
		add(_openButton, "width 100%, wrap");
	}
	
	private JLabel getLabel(String text) { 
		return new JLabel(text, JLabel.LEFT);
	}
	
	private JTextField getField() { 
		JTextField field = new JTextField();
		field.setEditable(false);
		field.setBackground(Color.WHITE);
		int height = field.getPreferredSize().height;
		field.setMinimumSize(new Dimension(200, height));
		return field;
	}
	
	private JButton getButton(String text, ActionListener listener) {
		JButton button = new JButton(text);
		button.addActionListener(listener);
		return button;
	}
	
	private void findSettings() {
		if(_gif == null)
			return;
		String path = _gif.getAbsolutePath();
		_settings = new File(path.substring(0, path.lastIndexOf(".")) + ".glp");
		if(_settings.exists())
			_messageLabel.setText("Project file found.");
		else 
			_messageLabel.setText("Unable to find a project file. A new project will be created.");
	}
	
	private void updateFields() {
		_gifField.setText(_gif == null ? "" :  _gif.getName());
		_settingsField.setText(_settings == null ? "" : _settings.getName());
	}
	
	private class FileChooserListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			int status = _fileChooser.showDialog(GIFLooper.activeFrame, "Select");
			if(status == JFileChooser.APPROVE_OPTION) {
				File selectedFile = _fileChooser.getSelectedFile();
				if(event.getSource() == _gifButton) {
					_gif = selectedFile;
					findSettings();
				} else {
					_settings = selectedFile;
					_messageLabel.setText("Custome project file selected.");
				}
				updateFields();
			}
		}
	}
	
	private class OpenListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			if(_gif.exists()) {
				GIFLooper.gifFile = _gif;
				GIFLooper.settingsFile = _settings;
				GIFLooper.loadProject();
			} else
				GIFLooper.error("The GIF you selected does not exist.");
		}
	}
}

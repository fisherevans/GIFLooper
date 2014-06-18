package com.fisherevans.giflooper;

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
import javax.swing.filechooser.FileFilter;

import net.miginfocom.swing.MigLayout;

public class Open extends JPanel {
	private static final long serialVersionUID = -2146004416751024171L;
	
	private FileChooserListener _fileChooserListener;
	private OpenListener _openListener;
	
	private JLabel _gifLabel, _settingsLabel, _messageLabel;
	private JTextField _gifField, _settingsField;
	private JButton _gifButton, _settingsButton, _openButton;
	private JFileChooser _fileChooser;
	
	private JPanel _fileChooserPanel;
	private FileTypeFilter _filterGif, _filterGlp;
	
	private File _gif = null, _settings = null;
	
	public Open() {
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
		_filterGif = new FileTypeFilter(".gif", "Graphics Interchange Format");
		_filterGlp = new FileTypeFilter(".glp", "GIFLooper Project");
		
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
		add(_openButton, "gapleft 33%, width 33%, gapright 33%, wrap");
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
        int index = path.lastIndexOf(".");
		_settings = new File(path.substring(0, index > 0 ? index : path.length()) + ".glp");
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
			for(FileFilter ff:_fileChooser.getChoosableFileFilters())
				_fileChooser.removeChoosableFileFilter(ff);
			if(event.getSource() == _gifButton)
				_fileChooser.addChoosableFileFilter(_filterGif);
			else
				_fileChooser.addChoosableFileFilter(_filterGlp);
			int status = _fileChooser.showDialog(GIFLooper.activeFrame, "Select a file...");
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
			if(_gif != null && _gif.exists()) {
				GIFLooper.gifFile = _gif;
				GIFLooper.settingsFile = _settings;
				GIFLooper.loadProject();
			} else
				GIFLooper.error("The GIF you selected does not exist.");
		}
	}
	
	private class FileTypeFilter extends FileFilter {
	    private String extension;
	    private String description;
	 
	    public FileTypeFilter(String extension, String description) {
	        this.extension = extension;
	        this.description = description;
	    }
	 
	    public boolean accept(File file) {
	        if (file.isDirectory()) {
	            return true;
	        }
	        return file.getName().endsWith(extension);
	    }
	 
	    public String getDescription() {
	        return description + String.format(" (*%s)", extension);
	    }
	}
}

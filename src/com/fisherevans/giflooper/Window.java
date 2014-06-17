package com.fisherevans.giflooper;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ext.GifDecoder;
import ext.GifEncoder;
import net.miginfocom.swing.MigLayout;

public class Window {
	public static final double ACCR = 8;
	private JFrame _frame;
	private JPanel _root, _editor, _view;
	private JFileChooser _fileInput;
	private File _file;
	
	private GifDecoder _decoder = null;
	private GifEncoder _encoder;
	
	private BufferedImage _start, _end;
	
	private JSlider _first, _last, _dx, _dy, _rot, _xscale, _yscale;
	
	public Window() {
		_frame = new JFrame("GIF Looper");
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_root = new JPanel(new MigLayout("fillx"));
		addFileChooser();
		_editor = new JPanel(new MigLayout("fillx"));
		_root.add(_editor, "width 100%, wrap");
		addSaveOptions();
		_frame.add(_root);
		_frame.setVisible(true);
		loadEditor();
	}
	
	private void loadEditor() {
		_editor.removeAll();
		if(_decoder != null) {
			updateStartEnd(false);
			_view = new JPanel(new MigLayout()) {
				protected void paintComponent(Graphics g) {
					super.paintComponent(g);
					if(_start != null && _end != null) {
						Graphics2D g2d = (Graphics2D) g;
						g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		                        RenderingHints.VALUE_ANTIALIAS_ON);
						g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
						g2d.drawImage(_start, 0, 0, _start.getWidth(), _start.getHeight(), null);
						
						AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
						g2d.setComposite(ac);
						
						double dx = _dx.getValue()/ACCR;
						double dy = _dy.getValue()/ACCR;
						double xscale = _xscale.getValue()/ACCR/100.0;
						double yscale = _yscale.getValue()/ACCR/100.0;
						double width = ((double)_end.getWidth())*xscale;
						double height = ((double)_end.getHeight())*yscale;
						dx += (_end.getWidth()-width)/2;
						dy += (_end.getHeight()-height)/2;

						g2d.rotate(Math.toRadians(_rot.getValue()/ACCR), width/2+dx, height/2+dy);
						AffineTransform t = new AffineTransform();
				        t.translate(dx, dy);
				        t.scale(xscale, yscale); // scale = 1 
				        g2d.drawImage(_end, t, null);
					}
				}
			};
			ChangeListener clSel = new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent event) {
					updateStartEnd(true);
				}
			};
			JPanel selectors = new JPanel(new MigLayout());

			_first = new JSlider(JSlider.HORIZONTAL, 0, _decoder.getFrameCount()-1, 0);
			_first.addChangeListener(clSel);
			selectors.add(new JLabel("First"), "width 25%");
			selectors.add(_first, "width 75%, wrap");

			_last = new JSlider(JSlider.HORIZONTAL, 0, _decoder.getFrameCount()-1, _decoder.getFrameCount()-1);
			_last.addChangeListener(clSel);
			selectors.add(new JLabel("Last"), "width 25%");
			selectors.add(_last, "width 75%, wrap");

			_editor.add(selectors, "width 100%, wrap");
			_editor.add(_view, "width " + _start.getWidth() + ", height " + _start.getHeight() + ", wrap");
			

			ChangeListener cl = new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent event) {
					updateViewElements();
				}
			};
			JPanel sliders = new JPanel(new MigLayout("fillx"));
			
			_dx = new JSlider(JSlider.HORIZONTAL, -_start.getWidth()/2*(int)ACCR, _start.getWidth()/2*(int)ACCR, 0);
			_dx.addChangeListener(cl);
			sliders.add(new JLabel("Delta X"), "width 25%");
			sliders.add(_dx, "width 75%, wrap");
			
			_dy = new JSlider(JSlider.HORIZONTAL, -_start.getHeight()/2*(int)ACCR, _start.getHeight()/2*(int)ACCR, 0);
			_dy.addChangeListener(cl);
			sliders.add(new JLabel("Delta Y"), "width 25%");
			sliders.add(_dy, "width 75%, wrap");
			
			_xscale = new JSlider(JSlider.HORIZONTAL, 0, 200*(int)ACCR, 100*(int)ACCR);
			_xscale.addChangeListener(cl);
			sliders.add(new JLabel("Scale X"), "width 25%");
			sliders.add(_xscale, "width 75%, wrap");
			
			_yscale = new JSlider(JSlider.HORIZONTAL, 0, 200*(int)ACCR, 100*(int)ACCR);
			_yscale.addChangeListener(cl);
			sliders.add(new JLabel("Scale Y"), "width 25%");
			sliders.add(_yscale, "width 75%, wrap");
			
			_rot = new JSlider(JSlider.HORIZONTAL, -180*(int)ACCR, 180*(int)ACCR, 0);
			_rot.addChangeListener(cl);
			sliders.add(new JLabel("Roatation"), "width 25%");
			sliders.add(_rot, "width 75%, wrap");
			
			_editor.add(sliders, "width 100%, wrap");
			_editor.revalidate();
			updateViewElements();
			_view.setSize(_start.getWidth(), _start.getHeight());
		} else {
			_editor.add(new JLabel("Open a GIF above...", JLabel.CENTER), "width 100%, wrap");
			_editor.revalidate();
		}
		_frame.pack();
		Util.centerJFrame(_frame);
	}
	
	private void updateStartEnd(boolean fromSlider) {
		if(fromSlider) {
			_start = _decoder.getFrame(_first.getValue());
			_end = _decoder.getFrame(_last.getValue());
		} else {
			_start = _decoder.getFrame(0);
			_end = _decoder.getFrame(_decoder.getFrameCount()-1);
		}
		updateViewElements();
	}
	
	private void updateViewElements() {
		_editor.repaint();
	}

	private void addFileChooser() {
		JPanel panel = new JPanel(new MigLayout("fillx"));
		panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		_fileInput = new JFileChooser();
		final JTextField field = new JTextField("Select a file...");
		field.setEditable(false);
		field.setBackground(Color.WHITE);
		JButton button = new JButton("Browse...");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(_decoder != null) {
					int result = JOptionPane.showConfirmDialog(_frame, "Are you sure you want to load a new image?", "Load a New GIF", JOptionPane.YES_NO_OPTION);
					if(result == JOptionPane.NO_OPTION)
						return;
				}
				_fileInput.showOpenDialog(_frame);
				_file = _fileInput.getSelectedFile();
				_decoder = new GifDecoder();
				switch(_decoder.read(_file.getAbsolutePath())) {
				case GifDecoder.STATUS_OK:
					break;
				case GifDecoder.STATUS_FORMAT_ERROR:
					JOptionPane.showMessageDialog(_frame, "Invalid GIF!", "Error", JOptionPane.WARNING_MESSAGE);
					_decoder = null;
					return;
				case GifDecoder.STATUS_OPEN_ERROR:
					JOptionPane.showMessageDialog(_frame, "File IO Error!", "Error", JOptionPane.WARNING_MESSAGE);
					_decoder = null;
					return;
				default:
					JOptionPane.showMessageDialog(_frame, "Unknown Error!", "Error", JOptionPane.WARNING_MESSAGE);
					_decoder = null;
					return;
				}
				field.setText(".../" + _file.getName());
				loadEditor();
			}
		});
		panel.add(field, "width 75%");
		panel.add(button, "width 25%, wrap");
		_root.add(panel, "width 100%, wrap");
	}
	
	private void addSaveOptions() {
		JPanel panel = new JPanel(new MigLayout("fillx"));
		final JTextField field = new JTextField("out.gif");
		JButton button = new JButton("Save!");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if(_decoder != null) {
					File saveFile = new File(_file.getParent() + "/" + field.getText());
					_encoder = new GifEncoder();
					_encoder.setQuality(20);
					_encoder.setRepeat(_decoder.getLoopCount());
					if(_encoder.start(saveFile.getAbsolutePath())) {
						double value;
						double dx, dy, xscale, yscale, width, height;
						BufferedImage tmp, img = new BufferedImage(_start.getWidth(), _start.getHeight(), BufferedImage.TYPE_INT_RGB);
						Graphics2D gfx = img.createGraphics();
						gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		                        RenderingHints.VALUE_ANTIALIAS_ON);
						gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);

						int first = _first.getValue();
						int last = _last.getValue();
						int sign = (int) Math.signum(last-first);
						int count = Math.abs(last-first);
						for(int id = 0;id < count;id++) {
							int frameId = first + sign*id;
							tmp = _decoder.getFrame(frameId);
							value = id/(double)count;
							
							dx = ((double)interp(0, _dx.getValue(), value))/ACCR;
							dy = ((double)interp(0, _dy.getValue(), value))/ACCR;
							xscale = ((double)interp((int)(100*ACCR), _xscale.getValue(), value))/ACCR/100.0;
							yscale = ((double)interp((int)(100*ACCR), _yscale.getValue(), value))/ACCR/100.0;
							width = ((double)tmp.getWidth())*xscale;
							height = ((double)tmp.getHeight())*yscale;
							dx += (tmp.getWidth()-width)/2;
							dy += (tmp.getHeight()-height)/2;

							AffineTransform old = gfx.getTransform();
							gfx.rotate(Math.toRadians(_rot.getValue()/ACCR), width/2+dx, height/2+dy);
							AffineTransform t = new AffineTransform();
					        t.translate(dx, dy);
					        t.scale(xscale, yscale); // scale = 1 
					        gfx.drawImage(tmp, t, null);
							gfx.setTransform(old);
							
							_encoder.setDelay(_decoder.getDelay(frameId));
							_encoder.addFrame(img);
						}
						if(_encoder.finish()) {
							JOptionPane.showMessageDialog(_frame, "Saved file to " + saveFile.getAbsolutePath(), "Saved!", JOptionPane.INFORMATION_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(_frame, "Failed to save file to " + saveFile.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
						}
					} else
						JOptionPane.showMessageDialog(_frame, "Failed to save file to " + saveFile.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(_frame, "No GIF Opened", "Error", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		panel.add(field, "width 75%");
		panel.add(button, "width 25%, wrap");
		_root.add(panel, "width 100%, wrap");
	}
	
	private int interp(int start, int end, double value) {
		return (int)(start*(1.0-value) + end*value);
	}
}

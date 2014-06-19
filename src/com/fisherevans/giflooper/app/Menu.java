package com.fisherevans.giflooper.app;

import javax.swing.*;

import com.fisherevans.giflooper.App;
import com.fisherevans.giflooper.GIFLooper;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class Menu extends JMenuBar implements ActionListener {
    private JMenu _menu;
    private JMenuItem _openItem, _saveItem, _exportItem, _settingsItem, _quitItem;

    public Menu() {
        _menu = new JMenu("Menu");
        _menu.setMnemonic(KeyEvent.VK_M);

        _openItem = getItem("Open", KeyEvent.VK_O);
        _saveItem = getItem("Save", KeyEvent.VK_S);
        _exportItem = getItem("Export", KeyEvent.VK_E);
        _settingsItem = getItem("Settings", KeyEvent.VK_T);
        _quitItem = getItem("Quit", KeyEvent.VK_Q);

        _menu.add(_openItem);
        _menu.add(_saveItem);
        _menu.add(_exportItem);
        _menu.add(_settingsItem);
        _menu.add(_quitItem);

        add(_menu);
    }

    private JMenuItem getItem(String label, int hotkey) {
        JMenuItem item = new JMenuItem(label, hotkey);
        item.addActionListener(this);
        return item;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == _openItem) {
            if(GIFLooper.confirm("Are you sure you want to open a new GIF? Any unsaved work will be lost."))
                GIFLooper.loadOpen();
        } else if(e.getSource() == _saveItem) {
        	App.current.saveProjectFile();
        } else if(e.getSource() == _exportItem) {
            App.current.export();
        } else if(e.getSource() == _settingsItem) {
            GIFLooper.error("Settings - TODO");
        } else if(e.getSource() == _quitItem) {
            if(GIFLooper.confirm("Are you sure you want to quit? Any unsaved work will be lost.")) {
                GIFLooper.closeActiveFrame();
                System.exit(0);
            }
        }
    }
}

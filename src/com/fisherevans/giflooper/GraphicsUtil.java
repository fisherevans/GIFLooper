package com.fisherevans.giflooper;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import com.fisherevans.giflooper.app.components.Project;

public class GraphicsUtil {
	public static Graphics2D getG2D(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		if(App.project.settings.aa)
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
		if(App.project.settings.graphicsInterpolation)
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		return g2d;
	}
}

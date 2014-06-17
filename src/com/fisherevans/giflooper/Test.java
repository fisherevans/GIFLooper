package com.fisherevans.giflooper;

import ext.GifEncoder;
import ext.GifDecoder;

public class Test {
	public static GifDecoder testDecoder(String filename) {
		System.out.println("Reading: " + filename);
		GifDecoder decoder = new GifDecoder();
		int status = decoder.read(filename);
		if(status == GifDecoder.STATUS_OK) {
			System.out.println("Frames: " + decoder.getFrameCount());
			System.out.println("Loops: " + decoder.getLoopCount());
			System.out.println("MS Per Frame: " + decoder.getDelay(0));
		} else {
			switch(status) {
			case GifDecoder.STATUS_FORMAT_ERROR:
				System.out.println("Format Error!");
				break;
			case GifDecoder.STATUS_OPEN_ERROR:
				System.out.println("Open Error!");
				break;
			default:
				System.out.println("Unknown Error!");
				break;
			}
		}
		return decoder;
	}
	public static void testEncoder(String filename) {
		GifDecoder decoder = testDecoder(filename);
		GifEncoder encoder = new GifEncoder();
		encoder.setQuality(20);
		encoder.setRepeat(decoder.getLoopCount());
		String outName = filename.replaceAll(".gif", ".out.gif");
		if(encoder.start(outName)) {
			for(int frameId = decoder.getFrameCount()-1;frameId >= 0;frameId--) {
				encoder.setDelay(decoder.getDelay(frameId));
				encoder.addFrame(decoder.getFrame(frameId));
				System.out.print(".");
			}
			System.out.println();
			if(encoder.finish()) {
				System.out.print("Finished");
			} else {
				System.out.print("Failed to Finish");
			}
		} else
			System.out.println("Failed to Output: " + outName);
	}
}

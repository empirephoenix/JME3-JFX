package com.jme3x.jfx.injfx;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.beans.value.ChangeListener;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import com.jme3.post.SceneProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.texture.FrameBuffer;
import com.jme3.texture.Image.Format;
import com.jme3.util.BufferUtils;
import com.jme3x.jfx.FxPlatformExecutor;

//https://github.com/caprica/vlcj-javafx/blob/master/src/test/java/uk/co/caprica/vlcj/javafx/test/JavaFXDirectRenderingTest.java
//http://stackoverflow.com/questions/15951284/javafx-image-resizing
//TODO manage suspend/resume (eg when image/stage is hidden)
//FIXME better management of sync data  buffer + size (immutable ?) between async read / async write (to avoid IndexOutOfBoundsException reproduce the bug increase framerate and resize)
public class SceneProcessorCopyToImageView implements SceneProcessor {

	private FrameBuffer fb;
	private ByteBuffer byteBuf;
	private RenderManager rm;
	boolean attachAsMain = true;
	private ArrayList<ViewPort> viewPorts = new ArrayList<ViewPort>();
	private int askWidth  = 1;
	private int askHeight = 1;
	private int width  = 1;
	private int height = 1;
	private AtomicBoolean reshapeNeeded  = new AtomicBoolean(true);

	private WritableImage img;
	private final Object imgLock = new Object();
	private ImageView imgView;
	private ChangeListener<? super Number> wlistener = (w,o,n)->{
		componentResized(n.intValue(), (int)this.imgView.getFitHeight());
	};
	private ChangeListener<? super Number> hlistener = (w,o,n)->{
		componentResized((int)this.imgView.getFitWidth(), n.intValue());
	};

	public void componentResized(int w, int h) {
		int newWidth2 = Math.max(w, 1);
		int newHeight2 = Math.max(h, 1);
		if (width != newWidth2 || height != newHeight2){
			askWidth = newWidth2;
			askHeight = newHeight2;
			reshapeNeeded.set(true);
		}
	}

	public void bind(ImageView view, boolean overrideMainFramebuffer, ViewPort ... vps){
		if (viewPorts.size() > 0){
			for (ViewPort vp : viewPorts){
				vp.setOutputFrameBuffer(null);
			}
			viewPorts.get(viewPorts.size()-1).removeProcessor(this);
		}

		viewPorts.addAll(Arrays.asList(vps));
		viewPorts.get(viewPorts.size()-1).addProcessor(this);
		attachAsMain = overrideMainFramebuffer;

		if (imgView != null) {
			imgView.fitWidthProperty().removeListener(wlistener);
			imgView.fitHeightProperty().removeListener(hlistener);
		}
		imgView = view;
		if (imgView != null) {
			imgView.fitWidthProperty().addListener(wlistener);
			imgView.fitHeightProperty().addListener(hlistener);
			componentResized((int)imgView.getFitWidth(), (int)imgView.getFitHeight());
		}
	}

	@Override
	public void initialize(RenderManager rm, ViewPort vp) {
		if (this.rm == null){
			// First time called in OGL thread
			this.rm = rm;
			reshapeInThread(1, 1);
		}
	}
	public void repaintInThread(){
		// Convert screenshot.
		byteBuf.clear();
		rm.getRenderer().readFrameBuffer(fb, byteBuf);
		FxPlatformExecutor.runOnFxApplication(() -> {
			synchronized (imgLock){
				img.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(), byteBuf, width * 4);
			}
		});
	}

	private void reshapeInThread(int width0, int height0) {
		width = width0;
		height = height0;
		byteBuf = BufferUtils.ensureLargeEnough(byteBuf, width * height * 4);

		fb = new FrameBuffer(width, height, 1);
		fb.setDepthBuffer(Format.Depth);
		fb.setColorBuffer(Format.RGB8);

		if (attachAsMain){
			rm.getRenderer().setMainFrameBufferOverride(fb);
		}

		synchronized (imgLock){
			img = new WritableImage(width, height);//, BufferedImage.TYPE_INT_RGB);
		}
		FxPlatformExecutor.runOnFxApplication(() -> {
			//this.canvas.setScaleX(-1.0);
			this.imgView.setScaleY(-1.0);
			synchronized (imgLock){
				imgView.setImage(img);
			}
		});

		for (ViewPort vp : viewPorts){
			if (!attachAsMain){
				vp.setOutputFrameBuffer(fb);
			}
			vp.getCamera().resize(width, height, true);

			// NOTE: Hack alert. This is done ONLY for custom framebuffers.
			// Main framebuffer should use RenderManager.notifyReshape().
			for (SceneProcessor sp : vp.getProcessors()){
				sp.reshape(vp, width, height);
			}
			rm.notifyReshape(width, height);
		}
	}

	@Override
	public boolean isInitialized() {
		return fb != null;
	}

	@Override
	public void preFrame(float tpf) {
	}

	@Override
	public void postQueue(RenderQueue rq) {
	}

	@Override
	public void postFrame(FrameBuffer out) {
		if (!attachAsMain && out != fb){
			throw new IllegalStateException("Why did you change the output framebuffer?");
		}
		if (imgView == null) {
			return;
		}

		if (reshapeNeeded.getAndSet(false)){
			reshapeInThread(askWidth, askHeight);
		}else if (imgView.isVisible()) {
			//System.out.print(".");
			repaintInThread();
		}
	}

	@Override
	public void cleanup() {
	}

	@Override
	public void reshape(ViewPort vp, int w, int h) {
	}

}
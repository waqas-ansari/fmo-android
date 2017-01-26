package cz.fmo.recording;

import android.media.Image;
import android.media.ImageReader;
import android.view.Surface;

import cz.fmo.Lib;

/**
 * Receives image frames and applies FMO library processing to them. This is the entry point for the
 * C++ part of the application.
 */
class ProcessingThread extends cz.fmo.util.GenericThread<ProcessingThreadHandler> {
    private final Callback mCb;
    private final ImageReader mReader;
    private boolean mTornDown = false;

    ProcessingThread(int width, int height, int format, Callback cb) {
        super("ProcessingThread");
        mCb = cb;
        mReader = ImageReader.newInstance(width, height, format, 12);
    }

    /**
     * Called when one or more new frames are ready for dispatch, provided by the ImageReader. Does
     * all the processing required for an individual frame.
     */
    void frame() {
        Image image = mReader.acquireLatestImage();
        if (image == null) return;
        image.getPlanes();
        //long timeNs = image.getTimestamp();
        //Image.Plane plane = image.getPlanes()[0]; // use only the Y channel, i.e. black and white
        //ByteBuffer data = plane.getBuffer();
        //int pixelStride = plane.getPixelStride();
        //int rowStride = plane.getRowStride();

        Lib.onFrame(image, mCb);
        image.close();
    }

    /**
     * Prepares resources. Called just before the thread is started.
     *
     * @param handler Event handler.
     */
    @Override
    protected void setup(ProcessingThreadHandler handler) {
        mReader.setOnImageAvailableListener(handler, handler);
    }

    /**
     * Disposes of all resources. Called as soon as the thread is stopped.
     */
    @Override
    protected void teardown() {
        if (mTornDown) return;
        mTornDown = true;
        mReader.close();
    }

    /**
     * @return A surface used to be used as output of an image source.
     */
    Surface getInputSurface() {
        return mReader.getSurface();
    }

    @Override
    protected ProcessingThreadHandler makeHandler() {
        return new ProcessingThreadHandler(this);
    }

    public interface Callback extends Lib.FrameCallback {
    }
}
package com.ziliang.PhotoGallery;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

public class ThumbnailDownloader extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    Handler mHandler;
    Handler mResponseHandler;
    Map<ImageView, String> requestMap =
            Collections.synchronizedMap(new HashMap<ImageView, String>());
    private LruCache<String, Bitmap> mMemoryCache = new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / (5 * 1024)));

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    //token
                    ImageView imageView = (ImageView) msg.obj;
//                    Log.i(TAG, "Got a request for url: " + requestMap.get(imageView));
                    handleRequest(imageView);
                }
            }
        };
    }

    private void handleRequest(final ImageView imageView) {
        try {
            final String url = requestMap.get(imageView);
            if (url == null) {
                return;
            }
            byte[] bitmapBytes = new FlickrFetcher().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory
                    .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

            mResponseHandler.post(new Runnable() {
                public void run() {
                    if (requestMap.get(imageView) != url)
                        return;

                    requestMap.remove(imageView);
                    mMemoryCache.put(url, bitmap);
                    imageView.setImageBitmap(bitmap);
                }
            });
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }

    public void queueThumbnail(ImageView imageView, String url) {
        if (mMemoryCache.get(url) != null) {
            imageView.setImageBitmap(mMemoryCache.get(url));
            return;
        }
        requestMap.put(imageView, url);
        mHandler
                .obtainMessage(MESSAGE_DOWNLOAD, imageView)
                .sendToTarget();
    }

    public void clearQueue() {
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }
}


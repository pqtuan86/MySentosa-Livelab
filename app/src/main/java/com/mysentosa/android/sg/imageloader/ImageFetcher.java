/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mysentosa.android.sg.imageloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.mysentosa.android.sg.BuildConfig;
import com.mysentosa.android.sg.utils.HttpHelper;
import com.mysentosa.android.sg.utils.LogHelper;
import com.mysentosa.android.sg.utils.SentosaUtils;

/**
 * A simple subclass of {@link ImageResizer} that fetches and resizes images fetched from a URL.
 */
public class ImageFetcher extends ImageWorker {
    private static final String TAG = " testing";

    /**
     * constructor
     *
     * @param context
     */
    public ImageFetcher(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        checkConnection(context);
    }

    /**
     * Simple network connection check.
     *
     * @param context
     */
    private void checkConnection(Context context) {
        final ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnectedOrConnecting()) {
            Toast.makeText(context, "No network connection found.", Toast.LENGTH_LONG).show();
            LogHelper.e(TAG, "checkConnection - no connection found");
        }
    }

    /**
     * The main process method, which will be called by the ImageWorker in the AsyncTask background
     * thread.
     *
     * @param data The data to load the bitmap, in this case, a regular http URL
     * @return The downloaded and resized bitmap
     */
    private Bitmap processBitmap(String data) {
        if (BuildConfig.DEBUG) {
        	LogHelper.d(TAG, "processBitmap - " + data);
        }

        // Download a bitmap, write it to a file
        final File f = downloadBitmap(mContext, data);

        if (f != null) {
            // Return a sampled down version
            //return decodeSampledBitmapFromFile(f.toString(), mImageWidth, mImageHeight);
        	return ImageLoaderUtils.decodeFileWithoutScaling(f);
        }

        return null;
    }

    @Override
    protected Bitmap processBitmap(Object data) {
        return processBitmap(String.valueOf(data));
    }

    /*
     * Download a bitmap from a URL, write it to a disk and return the File pointer. This
     * implementation uses a simple disk cache.
     *
     * @param context The context to use
     * @param urlString The URL to fetch
     * @return A File pointing to the fetched bitmap
     */
    public File downloadBitmap(Context context, String urlString) {
    	final DiskLruCache cache = mImageCache.getmDiskCache();
        final File cacheFile = new File(cache.createFilePath(urlString));
        
        InputStream is = null;
        
        if (cache.containsKey(urlString)) {
            if (BuildConfig.DEBUG) {
            	LogHelper.d(TAG, " testing downloadBitmap - found in cache - " + urlString);
            }
            
            try {
				is = HttpHelper.getImageInputStream(urlString,SentosaUtils.getLastModifiedFormatter().format(new Date(cacheFile.lastModified())));
			} catch (Exception e) {
				is = null;
			}
            if(is == null)return cacheFile;
        }

        if (BuildConfig.DEBUG) {
        	LogHelper.d(TAG, " testing downloadBitmap - downloading - " + urlString);
        }
        
        //ImageLoaderUtils.disableConnectionReuseIfNecessary();
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;

        try {

            if(is == null){
                final URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
            	is =  urlConnection.getInputStream();
            }

            final InputStream in =
                    new BufferedInputStream(is, ImageLoaderUtils.IO_BUFFER_SIZE);
            out = new BufferedOutputStream(new FileOutputStream(cacheFile), ImageLoaderUtils.IO_BUFFER_SIZE);

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            cache.put(urlString, cacheFile.getAbsolutePath());
            return cacheFile;

        } catch (final Exception e) {
        	LogHelper.e(TAG, "Error in downloadBitmap - " + e);
            if(cacheFile.exists())
            	cacheFile.delete();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException e) {
                	LogHelper.e(TAG, "Error in downloadBitmap - " + e);
                }
            }
        }
        return null;
    }
}

package com.mysentosa.android.sg.tilesourcebase;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import org.osmdroid.ResourceProxy;
import org.osmdroid.ResourceProxy.string;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class CustomBitmapTileSourceBase implements ITileSource,
		OpenStreetMapTileProviderConstants {

	private static int globalOrdinal = 0;

	private final int mMinimumZoomLevel;
	private final int mMaximumZoomLevel;

	private final int mOrdinal;
	protected final String mName;
	protected final String mImageFilenameEnding;
	protected final Random random = new Random();

	private final int mTileSizePixels;

	private final string mResourceId;
	
	private AssetManager assetManager;

	public CustomBitmapTileSourceBase(AssetManager assetManager, int max_zoom, int min_zoom, int tile_size, String fileNameEnding, String folderName) {
		this.assetManager = assetManager;
		mResourceId = null;
		mOrdinal = globalOrdinal++;
		mName = folderName;
		mMinimumZoomLevel = min_zoom;
		mMaximumZoomLevel = max_zoom;
		mTileSizePixels = tile_size;
		mImageFilenameEnding = fileNameEnding;
	}

	@Override
	public int ordinal() {
		return mOrdinal;
	}

	@Override
	public String name() {
		return mName;
	}

	public String pathBase() {
		return mName;
	}

	public String imageFilenameEnding() {
		return mImageFilenameEnding;
	}

	@Override
	public int getMinimumZoomLevel() {
		return mMinimumZoomLevel;
	}

	@Override
	public int getMaximumZoomLevel() {
		return mMaximumZoomLevel;
	}

	@Override
	public int getTileSizePixels() {
		return mTileSizePixels;
	}

	@Override
	public String localizedName(final ResourceProxy proxy) {
		return proxy.getString(mResourceId);
	}

	@Override
	public Drawable getDrawable(final String aFilePath) {
		try {
			return getDrawableFromAsset(aFilePath);
		} catch (final OutOfMemoryError e) {
			Log.d("error","OutOfMemoryError loading bitmap: " + aFilePath);
			System.gc();
		}
		return null;
	}

	@Override
	public String getTileRelativeFilenameString(final MapTile tile) {
		final StringBuilder sb = new StringBuilder();
		sb.append(pathBase());
		sb.append('/');
		sb.append(tile.getZoomLevel());
		sb.append('/');
		sb.append(tile.getX());
		sb.append('/');
		sb.append(tile.getY());
		sb.append(imageFilenameEnding());
		return sb.toString();
	}

	private Drawable getDrawableFromAsset(String path)
    {
		InputStream istr;
		Drawable d;
		try {
			istr = assetManager.open(path);
			d = Drawable.createFromStream(istr, null);
			if(d==null) {
				
			}
			return d;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			try {
				istr = assetManager.open(pathBase()+"/water.jpg");
				d = Drawable.createFromStream(istr, null);
				return d;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return null;
    }
	
	@Override
	public Drawable getDrawable(final InputStream aFileInputStream) {
		return null;
	}

	
	public final class LowMemoryException extends Exception {
		private static final long serialVersionUID = 146526524087765134L;

		public LowMemoryException(final String pDetailMessage) {
			super(pDetailMessage);
		}

		public LowMemoryException(final Throwable pThrowable) {
			super(pThrowable);
		}
	}
}

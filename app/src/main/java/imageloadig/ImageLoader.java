package imageloadig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.widget.ImageView;

import com.justsmartapps.myvault.R;
import com.justsmartapps.myvault.utils.MyVaultUtils;

public class ImageLoader {
	MemoryCache memoryCache = new MemoryCache();
	FileCache fileCache;
	private Map<ImageView, String> imageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	ExecutorService executorService;
	Handler handler = new Handler();// handler to display images in UI thread
	private int width;
	private int height;
	private Context context;
	private Drawable colorDrawable;
	private boolean dontLoadFromCache;
	private long size;

	public ImageLoader(Context context, int width, int height) {
		fileCache = new FileCache(context);
		this.width = width;
		this.height = height;
		this.context = context;
		executorService = Executors.newFixedThreadPool(5);
	}

	public void DisplayImage(String url, byte[] byteArray, ImageView imageView,
			boolean decrpted, boolean dontLoadFromCache, boolean showDummy) {
		imageViews.put(imageView, url);
		this.dontLoadFromCache = dontLoadFromCache;
		Bitmap bitmap = memoryCache.get(url);
		if (bitmap != null)
			imageView.setImageBitmap(bitmap);
		else {
			queuePhoto(url, decrpted, imageView);
			// emptyDrawable = new BitmapDrawable(context.getResources());
			int color = context.getResources().getColor(R.color.SemiBlack);
			colorDrawable = new ColorDrawable(color);
			if (showDummy) {
				imageView.setImageDrawable(colorDrawable);
			}
		}
	}

	private void queuePhoto(String url, boolean decrpted, ImageView imageView) {
		PhotoToLoad p = new PhotoToLoad(url, decrpted, imageView);
		executorService.submit(new PhotosLoader(p));
	}

	private int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}

	// private Bitmap getBitmap(String url, boolean decrpted) {
	// String fileName = url;
	// if (size != 0) {
	// fileName= url + "j";
	// }
	// File f = fileCache.getFile(fileName);
	// Bitmap b = decodeFile(f);
	// if (b != null) {
	// return b;
	// }
	// // from web
	// try {
	// BitmapFactory.Options options = new BitmapFactory.Options();
	// options.inJustDecodeBounds = true;
	// if (url != null) {
	// if (decrpted) {
	// byte[] decryptToByteArray = MyVaultUtils
	// .decryptToByteArray(url);
	// BitmapFactory.decodeByteArray(decryptToByteArray, 0,
	// decryptToByteArray.length, options);
	// int sampleSize = calculateInSampleSize(options, width,
	// height);
	// if (width == 0 || height == 0) {
	// sampleSize = 8;
	// }
	// options.inSampleSize = sampleSize;
	// options.inJustDecodeBounds = false;
	// Bitmap hiddenBitmap = BitmapFactory.decodeByteArray(
	// decryptToByteArray, 0, decryptToByteArray.length,
	// options);
	// // fileCache.putBitmap(url, hiddenBitmap);
	// return hiddenBitmap;
	// } else {
	// BitmapFactory.decodeFile(url, options);
	// int sampleSize = calculateInSampleSize(options, width,
	// height);
	// if (width == 0 || height == 0) {
	// sampleSize = 8;
	// }
	// options.inSampleSize = sampleSize;
	// options.inJustDecodeBounds = false;
	// Bitmap bitmap = BitmapFactory.decodeFile(url, options);
	// // fileCache.putBitmap(fileName, bitmap);
	// return bitmap;
	// }
	// }
	// // else {
	// // BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length,
	// // options);
	// // options.inSampleSize = calculateInSampleSize(options, width,
	// // height);
	// // options.inJustDecodeBounds = false;
	// // return BitmapFactory.decodeByteArray(byteArray, 0,
	// // byteArray.length, options);
	// // }
	// // Bitmap bitmap = null;
	// // URL imageUrl = new URL(url);
	// // HttpURLConnection conn = (HttpURLConnection) imageUrl
	// // .openConnection();
	// // conn.setConnectTimeout(30000);
	// // conn.setReadTimeout(30000);
	// // conn.setInstanceFollowRedirects(true);
	// // InputStream is = conn.getInputStream();
	// // OutputStream os = new FileOutputStream(f);
	// // Utils.CopyStream(is, os);
	// // os.close();
	// // conn.disconnect();
	// // bitmap = decodeFile(f);
	// // return bitmap;
	// } catch (Throwable ex) {
	// ex.printStackTrace();
	// if (ex instanceof OutOfMemoryError)
	// memoryCache.clear();
	// }
	// return null;
	// }
	// /**
	// * New
	// *
	// * @param f
	// * @return
	// */
	// private Bitmap getBitmap(String url, boolean decrpted) {
	// File f = fileCache.getFile(url, size);
	// Bitmap b = decodeFileNew(f);
	// if (b != null) {
	// return b;
	// }
	// // from web
	// try {
	// BitmapFactory.Options options = new BitmapFactory.Options();
	// options.inJustDecodeBounds = true;
	// if (url != null) {
	// if (decrpted) {
	// byte[] decryptToByteArray = MyVaultUtils
	// .decryptToByteArray(url);
	// BitmapFactory.decodeByteArray(decryptToByteArray, 0,
	// decryptToByteArray.length, options);
	// int sampleSize = calculateInSampleSize(options, width,
	// height);
	// if (width == 0 || height == 0) {
	// sampleSize = 8;
	// }
	// options.inSampleSize = sampleSize;
	// options.inJustDecodeBounds = false;
	// Bitmap hiddenBitmap = BitmapFactory.decodeByteArray(
	// decryptToByteArray, 0, decryptToByteArray.length,
	// options);
	// // fileCache.putBitmap(url, hiddenBitmap);
	// return hiddenBitmap;
	// } else {
	// BitmapFactory.decodeFile(url, options);
	// int sampleSize = calculateInSampleSize(options, width,
	// height);
	// if (size > 1400000) {
	// // sampleSize = 8;
	// }
	// BitmapFactory.Options o2 = new BitmapFactory.Options();
	// o2.inSampleSize = sampleSize;
	// o2.inJustDecodeBounds = false;
	// Bitmap bitmap = BitmapFactory.decodeFile(url, o2);
	// fileCache.putBitmap(url, size, bitmap);
	// return bitmap;
	// }
	// }
	// // else {
	// // BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length,
	// // options);
	// // options.inSampleSize = calculateInSampleSize(options, width,
	// // height);
	// // options.inJustDecodeBounds = false;
	// // return BitmapFactory.decodeByteArray(byteArray, 0,
	// // byteArray.length, options);
	// // }
	// // Bitmap bitmap = null;
	// // URL imageUrl = new URL(url);
	// // HttpURLConnection conn = (HttpURLConnection) imageUrl
	// // .openConnection();
	// // conn.setConnectTimeout(30000);
	// // conn.setReadTimeout(30000);
	// // conn.setInstanceFollowRedirects(true);
	// // InputStream is = conn.getInputStream();
	// // OutputStream os = new FileOutputStream(f);
	// // Utils.CopyStream(is, os);
	// // os.close();
	// // conn.disconnect();
	// // bitmap = decodeFile(f);
	// // return bitmap;
	// } catch (Throwable ex) {
	// ex.printStackTrace();
	// if (ex instanceof OutOfMemoryError)
	// memoryCache.clear();
	// }
	// return null;
	// }
	/**
	 * Last Release
	 * 
	 * @param url
	 * @param decrpted
	 * @return
	 */
	
	
	private Bitmap getBitmap(String url, boolean decrpted) {
		File f = fileCache.getFile(url, size);
		Bitmap b = decodeFile(f);
		if (b != null && dontLoadFromCache) {
			return b;
		}
		// from web
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			if (url != null) {
				if (decrpted) {
					byte[] decryptToByteArray = MyVaultUtils
							.decryptToByteArray(url);
					BitmapFactory.decodeByteArray(decryptToByteArray, 0,
							decryptToByteArray.length, options);
					int sampleSize = calculateInSampleSize(options, width,
							height);
					if (width == 0 || height == 0) {
						sampleSize = 8;
					}
					options.inSampleSize = sampleSize;
					options.inJustDecodeBounds = false;
					Bitmap hiddenBitmap = BitmapFactory.decodeByteArray(
							decryptToByteArray, 0, decryptToByteArray.length,
							options);
					// fileCache.putBitmap(url, size, hiddenBitmap);
					return hiddenBitmap;
				} else {
					BitmapFactory.decodeFile(url, options);
					int sampleSize = calculateInSampleSize(options, width,
							height);
					if (width == 0 || height == 0) {
						sampleSize = 8;
					}
					options.inSampleSize = sampleSize;
					options.inJustDecodeBounds = false;
					Bitmap bitmap = BitmapFactory.decodeFile(url, options);
					// fileCache.putBitmap(url, size, bitmap);
					return bitmap;
				}
			}
			// else {
			// BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length,
			// options);
			// options.inSampleSize = calculateInSampleSize(options, width,
			// height);
			// options.inJustDecodeBounds = false;
			// return BitmapFactory.decodeByteArray(byteArray, 0,
			// byteArray.length, options);
			// }
			// Bitmap bitmap = null;
			// URL imageUrl = new URL(url);
			// HttpURLConnection conn = (HttpURLConnection) imageUrl
			// .openConnection();
			// conn.setConnectTimeout(30000);
			// conn.setReadTimeout(30000);
			// conn.setInstanceFollowRedirects(true);
			// InputStream is = conn.getInputStream();
			// OutputStream os = new FileOutputStream(f);
			// Utils.CopyStream(is, os);
			// os.close();
			// conn.disconnect();
			// bitmap = decodeFile(f);
			// return bitmap;
		} catch (Throwable ex) {
			ex.printStackTrace();
			if (ex instanceof OutOfMemoryError)
				memoryCache.clear();
		}
		return null;
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFileNew(File f) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			FileInputStream stream1 = new FileInputStream(f);
			BitmapFactory.decodeStream(stream1, null, o);
			stream1.close();
			// Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = 70;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int sampleSize = calculateInSampleSize(o, width, height);
			// int scale = 1;
			// while (true) {
			// if (width_tmp / 2 < REQUIRED_SIZE
			// || height_tmp / 2 < REQUIRED_SIZE)
			// break;
			// width_tmp /= 2;
			// height_tmp /= 2;
			// scale *= 2;
			// }
			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = sampleSize;
			FileInputStream stream2 = new FileInputStream(f);
			Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
			stream2.close();
			return bitmap;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			FileInputStream stream1 = new FileInputStream(f);
			BitmapFactory.decodeStream(stream1, null, o);
			stream1.close();
			// Find the correct scale value. It should be the power of 2.
			int sampleSize = calculateInSampleSize(o, width, height);
			if (width == 0 || height == 0) {
				sampleSize = 8;
			}
			o.inSampleSize = sampleSize;
			o.inJustDecodeBounds = false;
			FileInputStream stream2 = new FileInputStream(f);
			Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o);
			stream2.close();
			return bitmap;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;
		private boolean decrpted;

		public PhotoToLoad(String u, boolean decrpted, ImageView i) {
			url = u;
			this.decrpted = decrpted;
			imageView = i;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			try {
				if (imageViewReused(photoToLoad))
					return;
				Bitmap bmp = getBitmap(photoToLoad.url, photoToLoad.decrpted);
				memoryCache.put(photoToLoad.url, bmp);
				if (imageViewReused(photoToLoad))
					return;
				BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
				handler.post(bd);
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
	}

	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = imageViews.get(photoToLoad.imageView);
		if (tag == null || !tag.equals(photoToLoad.url))
			return true;
		return false;
	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			bitmap = b;
			photoToLoad = p;
		}

		public void run() {
			if (imageViewReused(photoToLoad))
				return;
			if (bitmap != null)
				photoToLoad.imageView.setImageBitmap(bitmap);
			else
				photoToLoad.imageView.setImageDrawable(colorDrawable);
		}
	}

	public void clearCache() {
		System.out.println("CLARING CACHA");
		memoryCache.clear();
		fileCache.clear();
	}

	public void DisplayImage(String url, byte[] byteArray, ImageView imageView,
			boolean decrpted, boolean dontLoadFromCache, boolean showDummy,
			String string) {
		size = Long.parseLong(string);
		DisplayImage(url, byteArray, imageView, decrpted, dontLoadFromCache,
				showDummy);
	}
}

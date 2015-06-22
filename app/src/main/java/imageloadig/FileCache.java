package imageloadig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;

public class FileCache {
	private File cacheDir;

	// private Context context;
	public FileCache(Context context) {
		// Find the dir to save cached images
		cacheDir = context.getCacheDir();
		if (!cacheDir.exists())
			cacheDir.mkdirs();
		// this.context = context;
	}

	public File getFile(String url, long size) {
		// I identify images by hashcode. Not a perfect solution, good for the
		// demo.
		
		String string = String.valueOf(url.hashCode());
		if(size != 0){
			string = string+"j";
		}
		String filename = String.valueOf(string);
		// Another possible solution (thanks to grantland)
		// String filename = URLEncoder.encode(url);
		File f = new File(cacheDir, filename);
		return f;
	}

	public void putBitmap(String url, long size ,Bitmap b) {
		String string = String.valueOf(url.hashCode());
		if(size != 0){
			string = string+"j";
		}
		String filename = String.valueOf(string);
		// Another possible solution (thanks to grantland)
		// String filename = URLEncoder.encode(url);
		File myPath = new File(cacheDir, filename);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(myPath);
			b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void clear() {
		File[] files = cacheDir.listFiles();
		if (files == null)
			return;
		for (File f : files)
			f.delete();
	}
}
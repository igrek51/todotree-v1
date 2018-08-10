package igrek.todotree.service.filesystem;

import android.app.Activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import igrek.todotree.logger.Logger;

public class FilesystemService {
	
	private Logger logger;
	private Activity activity;
	private ExternalCardService externalCardService;
	
	public FilesystemService(Logger logger, Activity activity, ExternalCardService externalCardService) {
		this.logger = logger;
		this.activity = activity;
		this.externalCardService = externalCardService;
	}
	
	public PathBuilder externalSDPath() {
		// returns internal dir but creates also /storage/extSdCard/Android/data/pkg - WTF?!
		//		activity.getExternalFilesDir("data");
		return new PathBuilder(externalCardService.getExternalSDPath());
	}
	
	public boolean mkdirIfNotExist(String path) {
		File f = new File(path);
		return !f.exists() && f.mkdirs();
	}
	
	private List<String> listDir(String path) {
		List<String> lista = new ArrayList<>();
		File f = new File(path);
		File file[] = f.listFiles();
		for (File aFile : file) {
			lista.add(aFile.getName());
		}
		return lista;
	}
	
	public List<String> listDir(PathBuilder path) {
		return listDir(path.toString());
	}
	
	private byte[] openFile(String filename) throws IOException {
		RandomAccessFile f = new RandomAccessFile(new File(filename), "r");
		int length = (int) f.length();
		byte[] data = new byte[length];
		f.readFully(data);
		f.close();
		return data;
	}
	
	public String openFileString(String filename) throws IOException {
		byte[] bytes = openFile(filename);
		return new String(bytes, "UTF-8");
	}
	
	private void saveFile(String filename, byte[] data) throws IOException {
		File file = new File(filename);
		FileOutputStream fos;
		fos = new FileOutputStream(file);
		fos.write(data);
		fos.flush();
		fos.close();
	}
	
	public void saveFile(String filename, String str) throws IOException {
		saveFile(filename, str.getBytes());
	}
	
	public boolean exists(String path) {
		File f = new File(path);
		return f.exists();
	}
	
	private boolean delete(String path) {
		File file = new File(path);
		return file.delete();
	}
	
	public boolean delete(PathBuilder path) {
		return delete(path.toString());
	}
	
	public void copy(File source, File dest) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			if (is != null) {
				is.close();
			}
			if (os != null) {
				os.close();
			}
		}
	}
}

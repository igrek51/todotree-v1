package igrek.todotree.services.filesystem;

import android.app.Activity;
import android.os.Environment;

import com.google.common.base.Joiner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import igrek.todotree.logger.Logs;

public class FilesystemService {
	
	protected String pathToExtSD;
	private Logs logger;
	private Activity activity;
	
	public FilesystemService(Logs logger, Activity activity, ExternalCardService externalCardService) {
		this.logger = logger;
		this.activity = activity;
		
		pathSDInit();
	}
	
	protected void pathSDInit() {
		HashSet<String> externalMounts = getExternalMounts();
		logger.debug("External mounts: " + Joiner.on(", ").join(externalMounts));
		if (!externalMounts.isEmpty())
			pathToExtSD = externalMounts.iterator().next();
		if (!exists(pathToExtSD))
			pathToExtSD = "/storage/extSdCard";
		if (!exists(pathToExtSD))
			pathToExtSD = Environment.getExternalStorageDirectory().toString();
	}
	
	public static HashSet<String> getExternalMounts() {
		// FIXME fuckin Android
		final HashSet<String> out = new HashSet<>();
		String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
		String s = "";
		try {
			final Process process = new ProcessBuilder().command("mount")
					.redirectErrorStream(true)
					.start();
			process.waitFor();
			final InputStream is = process.getInputStream();
			final byte[] buffer = new byte[1024];
			while (is.read(buffer) != -1) {
				s = s + new String(buffer);
			}
			is.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		
		// parse output
		final String[] lines = s.split("\n");
		for (String line : lines) {
			if (!line.toLowerCase(Locale.US).contains("asec")) {
				if (line.matches(reg)) {
					String[] parts = line.split(" ");
					for (String part : parts) {
						if (part.startsWith("/"))
							if (!part.toLowerCase(Locale.US).contains("vold")) {
								// FIXME lg workaround
								part = part.replaceAll("^/mnt/media_rw", "/storage");
								out.add(part);
							}
					}
				}
			}
		}
		return out;
	}
	
	public PathBuilder pathSD() {
		return new PathBuilder(pathToExtSD);
	}
	
	public PathBuilder externalAndroidDir() {
		// FIXME
		// returns internal dir but creates also /storage/extSdCard/Android/data/pkg - WTF?!
		//		activity.getExternalFilesDir("data");
		return pathSD();
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

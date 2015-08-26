package fr.jcharles.files.backup;

import java.io.File;

public class RelativeFile {
	private final File src;
	private final String relative;
	private final File dest;
	
	public RelativeFile(Target target, File src) {
		this.src = src;
		String path = target.getSrc().getAbsolutePath();
		relative = src.getAbsolutePath().substring(path.length() + File.separator.length());
		dest = new File(target.getDest(), relative);
	}
	public String toString() {
		String res;
		if (src.isDirectory()) {
			res = "D ";
		}
		else {
			res = "  ";
		}
		res += relative;
		return res;
	}
	
	public boolean isDirectory() {
		return src.isDirectory();
	}
	
	public long getSize() {
		return src.length();
	}
	
	public boolean destExists() {
		return dest.exists();
	}
	
	public File getDest() {
		return dest;
	}
	public long getDestSize() {
		return dest.length();
	}
	public File getSrc() {
		return src;
	}
	public String getRelativePath() {
		return relative;
	}
}

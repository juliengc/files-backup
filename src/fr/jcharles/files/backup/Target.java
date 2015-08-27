package fr.jcharles.files.backup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Target {
	private final File src;
	private final File dest;
	private final List<RelativeFile> dirs = new ArrayList<RelativeFile>();
	private final List<RelativeFile> backup = new ArrayList<RelativeFile>();
	private final List<RelativeFile> update = new ArrayList<RelativeFile>();
	private long size = 0;
	private long updatesize = 0;

	public Target(File src, File dest) {
		super();
		this.src = src;
		this.dest = dest;
	}
	public Target(String src, String dest) {
		this (new File(src), new File(dest));
	}
	public File getSrc() {
		return src;
	}
	public File getDest() {
		return dest;
	}
	
	
	public Iterable<RelativeFile> getDirs() {
		return dirs;
	}
	public Iterable<RelativeFile> getBackup() {
		return backup;
	}
	public Iterable<RelativeFile> getUpdate() {
		return update;
	}
	
	public long getSize() {
		return size;
	}
	public long getUpdatesize() {
		return updatesize;
	}
	@Override
	public String toString() {
		return "Target [src=" + src + ", dest=" + dest + "]";
	}
	public void addDir(RelativeFile rf) {
		dirs.add(rf);
	}
	public void addFileToUpdate(RelativeFile rf) {
		update.add(rf);
		updatesize += rf.getSize();
	}
	public void addFileToBackup(RelativeFile rf) {
		backup.add(rf);
		size += rf.getSize();
	}
	public int getDirsCount() {
		return dirs.size();
	}
	public int getBackupCount() {
		return backup.size();
	}
	public int getUpdateCount() {
		return update.size();
	}
}

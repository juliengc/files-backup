package fr.jcharles.files.backup;

import java.io.File;

public class Target {
	private final File src;
	private final File dest;
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
	@Override
	public String toString() {
		return "Target [src=" + src + ", dest=" + dest + "]";
	}
	
}

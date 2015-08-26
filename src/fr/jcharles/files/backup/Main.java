package fr.jcharles.files.backup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Main {
	private static final String DEFAULT_CONFIG_FILE = "targets.cfg";
	public static void main(String [] args) {
		System.setProperty("awt.useSystemAAFontSettings", "lcd"); //"on");
		System.setProperty("swing.aatext", "true");
		if (args.length > 1 || (args.length == 1 && args [0].equals("-h"))) {
			System.err.println(
					"The program takes either 1 or no arguments.\n" +
					"The argument is the config file name, default being 'targets.cfg'.\n" +
					"The format of the file is groups of 2 lines, first one with prefix 'src: ', " +
					"second one with prefix 'dest: '. Src symbolising the source and dest the destination.\n");
			System.exit(1);
		}
		Gui gui = new Gui();
		Main m = new Main(gui);
		String cfg = DEFAULT_CONFIG_FILE;
		if (args.length == 1) {
			cfg = args[0];
		}
		System.out.println("Using config file: '" + cfg +"'.");
		m.run(cfg);
	}


	
	private final Gui gui;
	public Main(Gui gui) {
		this.gui = gui;
	}
	private void run(String fileName) {
		if (start(fileName) && gui.err.isEmpty()) {
			System.exit(0);
		}
		else {
			gui.showQuit();
		}
	}
	private boolean start(String fileName) {
		gui.showConfigFileWindow();
		List<Target> targets = parseConfigFile(fileName);
		gui.showTargets(targets);
		int idx = 1;
		for (Target t: targets) {
			gui.target.inspectTarget(t, idx++);
			List<RelativeFile> srces = getSrcFiles(t);
			if (srces == null) {
				return false;
			}
			gui.target.setElementsCount(srces.size());
			List<RelativeFile> dirs = new ArrayList<RelativeFile>();
			List<RelativeFile> backup = new ArrayList<RelativeFile>();
			List<RelativeFile> update = new ArrayList<RelativeFile>();
			long size = 0;
			long updatesize = 0;
			for (RelativeFile rf: srces) {
				if (rf.isDirectory()) {
					if(!rf.destExists()) {
						dirs.add(rf);
					}
				}
				else if (!rf.destExists()){
					backup.add(rf);
					size += rf.getSize();
				}
				else if (rf.getSize() != rf.getDestSize()) {
					update.add(rf);
					updatesize += rf.getSize();
				}
			}
			gui.target.setActionsCount(dirs.size(), backup.size(), size, 
					update.size(), updatesize);

			
			if (dirs.size() > 0) {
				gui.target.setDirMode();
				if (!createDirs(dirs))
					return false;
			}
			
			if (backup.size() > 0) {
				gui.target.setBackupMode();
				if (!createFiles(backup, size))
					return false;
			}
			
			if (update.size() > 0) {
				gui.target.setUpdateMode();
				if (gui.askIfUpdate(update)) {
					if (!createFiles(update, updatesize))
						return false;
				}
			}
		}
		return true;
	}

	private boolean createFiles(List<RelativeFile> backup, long size) {
		long currsize = 0;
		long inittime = System.currentTimeMillis();
		for (RelativeFile rf: backup) {
			long percent = (currsize * 10000) / size;
			final File src = rf.getSrc();
			final File dest = rf.getDest();
			
			if(gui.hasCanceled())
				return false;
			
			gui.target.progress((int)percent, dest, "Copie du fichier");
			BufferedInputStream in;
			try {
				in = new BufferedInputStream(new FileInputStream(src));
				
			} 
			catch (FileNotFoundException e) {
				gui.err.println("Failed to find file: '" + src + "'.");
				continue;
			}
			BufferedOutputStream out;
			try {
				out = new BufferedOutputStream(new FileOutputStream(dest));
			} catch (FileNotFoundException e) {
				gui.err.println("Failed to find file: '" + dest + "'.");
				try {
					in.close();
				} 
				catch (IOException e1) {
					gui.err.println("Failed to close file: '" + rf.getSrc() + "'.");
				}
				continue;
	
			}
			byte[] buff = new byte[1024 * 100];
			int bcount = 0;
			long filesize = rf.getSize();
			long currfilesize = 0;
			try {
				long lastt = 0;
				while ((bcount = in.read(buff)) >= 0) {
					if(gui.hasCanceled()) {
						try {
							in.close();
						} catch (IOException e) {
							gui.err.println("Failed to close file: '" + rf.getSrc() + "'.");
							continue;
						}
						try {
							out.close();
						} catch (IOException e) {
							gui.err.println("Failed to close file: '" + rf.getDest() + "'.");
							continue;
						}
						return false;
					}
					try {
						out.write(buff, 0, bcount);
					}
					catch (IOException e) {
						gui.err.println("Write error on file '" + rf.getDest() + "' " + e.getMessage());
						break;
					}
					currsize += bcount;
					currfilesize += bcount;
					percent = (currsize * 10000) / size;
					long filepercent = (currfilesize * 10000) / filesize;

					gui.target.progress((int)percent, (int) filepercent, dest, "Copie du fichier");
					long t = System.currentTimeMillis() - inittime;
					if (t - lastt > 500) {
						long speed = (currsize * 1000) / t;
						gui.target.updateSpeed(speed, size - currsize);
						lastt = t;
					}
				}
			} catch (IOException e) {
				gui.err.println("Read error on file '" + rf.getSrc() + "'.");
			}
			
			
			try {
				in.close();
			} catch (IOException e) {
				gui.err.println("Failed to close file: '" + rf.getSrc() + "'.");
				continue;
			}
			try {
				out.close();
			} catch (IOException e) {
				gui.err.println("Failed to close file: '" + rf.getDest() + "'.");
				continue;
			}
		}
		return true;
	}

	private boolean createDirs(List<RelativeFile> dirs) {
		int dirsCount = dirs.size();
		int count = 0;
		for (RelativeFile rf: dirs) {
			count++;
			long percent = (count * 10000) / dirsCount;
			File dest = rf.getDest();
			if(gui.hasCanceled())
				return false;
			if (!dest.mkdirs()) {
				gui.err.println("Failed to create directory: '" + dest + "'. Aborting.");
				return false;
			}
			gui.target.progress((int)percent, dest, "Création du répertoire");
		}
		return true;
	}
	


	private List<RelativeFile> getSrcFiles(Target target) {
		List<RelativeFile> files = new ArrayList<RelativeFile>();
		File src = target.getSrc();
		if (!src.exists()) {
			gui.err.println("The source does not exists!");
			return files;
		}
		LinkedList<File> dirs = new LinkedList<File>();
		dirs.add(src);
		while(!dirs.isEmpty()) {
			File dir = dirs.removeFirst();
			File[] content = dir.listFiles();
			if(gui.hasCanceled())
				return null;
			for (File f: content) {
				files.add(new RelativeFile(target, f));
				if (f.isDirectory()) {
					//System.out.println(f);
					dirs.add(f);
				}
			}
		}
		
		return files;
	}

	private List<Target> parseConfigFile(String fileName) {
		List<Target> targets = new ArrayList<Target> ();
		// parse config file
		try {
			LineNumberReader in = new LineNumberReader(new FileReader(DEFAULT_CONFIG_FILE));
			String line;
			try {
				while ((line = in.readLine()) != null) {
					if (line.trim().isEmpty())
						continue;
					if (line.trim().startsWith("#"))
						continue;
					if (line.startsWith("src: ")) {
						String src = line.substring("src: ".length());
						line = in.readLine();
						if (line == null) {
							gui.err.println("Unexpected EOF in the config file.");
							break;
						}
						if (line.startsWith("dest: ")) {
							String dest = line.substring("dest: ".length());
							targets.add(new Target(src, dest));
						}
						else {
							gui.err.println("Bad file format, the second line of the config file must start with 'dest: '.");
							continue;
						}
					}
					else {
						gui.err.println("Bad file format, the first line of the config file must start with 'src: '.");
						continue;
					}
				}
			}
			catch (IOException e) {
				gui.err.println("Reading error for the file "+ DEFAULT_CONFIG_FILE + ".");
			}
			in.close();
		} 
		catch (FileNotFoundException e) {
			gui.err.println("The config file '" + DEFAULT_CONFIG_FILE + "' cannot be found.");
		} 
		catch (IOException e) {
			gui.err.println("Error closing config file " + DEFAULT_CONFIG_FILE + ".");
		} 
	
		return targets;
		
	}
}

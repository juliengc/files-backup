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
		m.start(cfg);
	}


	
	private final Gui gui;
	public Main(Gui gui) {
		this.gui = gui;
	}
	private void start(String fileName) {
		gui.showConfigFileWindow();
		gui.err.info("Fichier de configuration: '" + fileName +"'.");
		List<Target> targets = parseConfigFile(fileName);
		if (targets.isEmpty()) {
			System.exit(1);
		}	
		gui.err.info("");
		if (run(targets) && gui.err.isEmpty()) {
			System.exit(0);
		}
		else {
			gui.showQuit();
		}
	}
	private boolean run(List<Target> targets) {
		
		gui.showTargets(targets);
		int idx = 1;
		for (Target t: targets) {
			gui.err.info("Inspection de la cible " + idx + ":");
			gui.target.inspectTarget(t, idx++);
			
			gui.err.info("  - Inspection de la source");
			gui.target.setSrcInspectMode();
			List<RelativeFile> srces = getSrcFiles(t);
			if (srces == null) {
				return false;
			}
			gui.target.setElementsCount(srces.size());
			
			gui.err.info("  - Inspection de la destination");	
			gui.target.setFileCompMode();
			sortSources(t, srces);
			gui.target.setActionsCount(t.getDirsCount(), t.getBackupCount(), t.getSize(), 
					t.getUpdateCount(), t.getUpdatesize());

			
			gui.err.info("  - Création des répertoires");
			if (t.getDirsCount() > 0) {
				gui.target.setDirMode();
				if (!createDirs(t.getDirs(), t.getDirsCount()))
					return false;
			}
			
			gui.err.info("  - Sauvegarde des fichiers (backup)");
			if (t.getBackupCount() > 0) {
				gui.target.setBackupMode();
				if (!createFiles(t.getBackup(), t.getSize()))
					return false;
			}
			
			gui.err.info("  - Mise à jour des fichiers (update)");
			if (t.getUpdateCount() > 0) {
				gui.target.setUpdateMode();
				if (gui.askIfUpdate(t.getUpdate(), t.getUpdateCount())) {
					if (!createFiles(t.getUpdate(), t.getUpdatesize()))
						return false;
				}
			}
			gui.err.info("");
		}
		return true;
	}
	private void sortSources(Target t, List<RelativeFile> srces) {
		int fnum = 0;
		int fsize = srces.size();
		for (RelativeFile rf: srces) {
			gui.target.progress((fnum++ * 10000) / fsize, rf.getDest(), "Evaluation de");
			if (rf.isDirectory()) {
				if(!rf.destExists()) {
					t.addDir(rf);
				}
			}
			else if (!rf.destExists()){
				t.addFileToBackup(rf);
			}
			else if (rf.getSize() != rf.getDestSize()) {
				t.addFileToUpdate(rf);
			}
		}
	}

	private boolean createFiles(Iterable<RelativeFile> backup, long size) {
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
				gui.err.println("Le fichier source '" + src + "' est introuvable.");
				continue;
			}
			BufferedOutputStream out;
			try {
				out = new BufferedOutputStream(new FileOutputStream(dest));
			} catch (FileNotFoundException e) {
				gui.err.println("Impossible de créer le fichier de destination '" + dest + "'.");
				try {
					in.close();
				} 
				catch (IOException e1) {
					gui.err.println("Impossible de fermer le fichier '" + rf.getSrc() + "'.");
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
							gui.err.println("Impossible de fermer le fichier '" + rf.getSrc() + "'.");
							continue;
						}
						try {
							out.close();
						} catch (IOException e) {
							gui.err.println("Impossible de fermer le fichier '" + rf.getDest() + "'.");
							continue;
						}
						return false;
					}
					try {
						out.write(buff, 0, bcount);
					}
					catch (IOException e) {
						gui.err.println("Erreur d'écriture sur le fichier '" + rf.getDest() + "' " + e.getMessage());
						break;
					}
					currsize += bcount;
					currfilesize += bcount;
					percent = (currsize * 10000) / size;
					long filepercent = (currfilesize * 10000) / filesize;

					gui.target.progress((int)percent, (int) filepercent, dest, "Copie du fichier");
					long t = System.currentTimeMillis() - inittime;
					if (t - lastt > 500) {
						double speed = (currsize * 1000) / (double)t;
						gui.target.updateSpeed(speed, size - currsize);
						lastt = t;
					}
				}
			} catch (IOException e) {
				gui.err.println("Erreur de lecture sur le fichier '" + rf.getSrc() + "'.");
			}
			
			
			try {
				in.close();
			} catch (IOException e) {
				gui.err.println("Impossible de fermer le fichier '" + rf.getSrc() + "'.");
				continue;
			}
			try {
				out.close();
			} catch (IOException e) {
				gui.err.println("Impossible de fermer le fichier '" + rf.getDest() + "'.");
				continue;
			}
		}
		return true;
	}

	private boolean createDirs(Iterable<RelativeFile> dirs, int dirsCount) {
	
		int count = 0;
		for (RelativeFile rf: dirs) {
			count++;
			long percent = (count * 10000) / dirsCount;
			File dest = rf.getDest();
			if(gui.hasCanceled())
				return false;
			if (!dest.mkdirs()) {
				gui.err.println("Impossible de créer le répertoire '" + dest + "'.");
				return false;
			}
			gui.target.progress((int)percent, dest, "Création du répertoire");
		}
		return true;
	}
	

	private boolean isSymlink(File file) {
		  if (file == null)
		    throw new NullPointerException("File must not be null");
		  File canon;
		  if (file.getParent() == null) {
		    canon = file;
		  } else {
		    File canonDir;
			try {
				canonDir = file.getParentFile().getCanonicalFile();
			} catch (IOException e) {
				return false;
			}
		    canon = new File(canonDir, file.getName());
		  }
		  try {
			return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
		} catch (IOException e) {
			return false;
		}
	}
	private List<RelativeFile> getSrcFiles(Target target) {
		List<RelativeFile> files = new ArrayList<RelativeFile>();
		File src = target.getSrc();
		if (!src.exists()) {
			gui.err.println("La source '" + src + "' n'existe pas!");
			return files;
		}
		LinkedList<File> dirs = new LinkedList<File>();
		dirs.add(src);
		while(!dirs.isEmpty()) {
			File dir = dirs.removeFirst();
			if (isSymlink(dir)) {
				continue;
			}
			gui.target.progress(0, dir, "Inspection de");
			File[] content = dir.listFiles();
			if(gui.hasCanceled())
				return null;
			if (content == null)
				continue;
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
			LineNumberReader in = new LineNumberReader(new FileReader(fileName));
			String line;
			try {
				int linenumber = -1;
				while ((line = in.readLine()) != null) {
					linenumber++;
					if (line.trim().isEmpty())
						continue;
					if (line.trim().startsWith("#"))
						continue;
					if (line.startsWith("src: ")) {
						String src = line.substring("src: ".length());
						line = in.readLine();
						if (line == null) {
							gui.err.println("Ligne " + linenumber + ": Fin de fichier inattendue dans le fichier de config.");
							break;
						}
						if (line.startsWith("dest: ")) {
							String dest = line.substring("dest: ".length());
							targets.add(new Target(src, dest));
						}
						else {
							gui.err.println("Ligne " + linenumber + ": Mauvais format de fichier, la ligne doit commencer par 'dest: '.");
							continue;
						}
					}
					else {
						gui.err.println("Ligne " + linenumber + ": Mauvais format de fichier, la ligne doit commencer par 'src: '.");
						continue;
					}
				}
			}
			catch (IOException e) {
				gui.err.println("Erreur de lecture du fichier "+ fileName + ".");
			}
			in.close();
		} 
		catch (FileNotFoundException e) {
			gui.err.println("Le fichier de configuration '" + fileName + "' est introuvable.");
		} 
		catch (IOException e) {
			gui.err.println("Impossible de fermer le fichier de configuration '" + fileName + "'.");
		} 
	
		return targets;
		
	}
}

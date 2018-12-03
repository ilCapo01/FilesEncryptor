import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.crypto.SecretKey;

public class Main {
	
	public Main() throws NoSuchAlgorithmException, IOException {
		AES aes = new AES();
		SecretKey key = null, iv; 
		String filename, path;
		File inputFile;
		
		Scanner scan = new Scanner(System.in);
		System.out.println("Action: "); 
		switch(scan.next()) {
		case "keygen":
			iv = aes.generateIV();
			aes.writeKeyToFile(iv, "iv.dat");
			break;
		case "encrypt":
			System.out.println("Key to encrypt: "); 
			key = aes.generateKey(scan.next());
			System.out.println("Filename to encrypt: "); 
			filename = scan.next();
			
			iv = aes.readKeyFromFile("iv.dat");
			
			inputFile = new File(filename);
			aes.encrypt(key, iv, inputFile, new File(filename+".dat"));
			inputFile.delete();
			break;
		case "decrypt":
			System.out.println("Key to decrypt: "); 
			key = aes.generateKey(scan.next());
			System.out.println("Filename to decrypt: "); 
			filename = scan.next();
			
			iv = aes.readKeyFromFile("iv.dat");
			
			inputFile = new File(filename);
			aes.decrypt(key, iv, new File(filename), new File(filename.replace(".dat", "")));
			inputFile.delete();
			break;
		case "encryptfolder":
			System.out.println("Key to encrypt: "); 
			key = aes.generateKey(scan.next());
			System.out.println("Path to directory: "); 
			path = scan.next();
			
			iv = aes.readKeyFromFile("iv.dat");
			
			encryptFiles(path, key, iv, aes);
			break;
		case "decryptfolder":
			System.out.println("Key to decrypt: "); 
			key = aes.generateKey(scan.next());
			System.out.println("Path to directory: "); 
			path = scan.next();
			
			iv = aes.readKeyFromFile("iv.dat");
			
			decryptFiles(path, key, iv, aes);
			break;
		default:
			System.err.println("[ERROR] Wrong Syntex.");
			break;
		}
	}
	
	public void encryptFiles(String dirPath, SecretKey key, SecretKey iv, AES aes) {
		DirectoriesCrawler crawler = new DirectoriesCrawler(dirPath);
		crawler.scanDirectories();
		File[] listOfFiles = crawler.getAllFiles();
		
		for (int i = 0; i < listOfFiles.length; i++) {
			File inputFile = listOfFiles[i];
			
			System.out.println(inputFile);
			aes.encrypt(key, iv, inputFile, new File(inputFile.getPath()+".dat"));
			inputFile.delete();
		}
	}
	
	public void decryptFiles(String dirPath, SecretKey key, SecretKey iv, AES aes) {
		DirectoriesCrawler crawler = new DirectoriesCrawler(dirPath);
		crawler.scanDirectories();
		File[] listOfFiles = crawler.getAllFiles();
		
		for (int i = 0; i < listOfFiles.length; i++) {
			File inputFile = listOfFiles[i];
			aes.decrypt(key, iv, inputFile, new File(inputFile.getPath().replace(".dat", "")));
			inputFile.delete();
		}
	}
	
	private class DirectoriesCrawler {
		private List<File> listFiles = new ArrayList<File>();
		private List<File> listDirs = new ArrayList<File>();
		private File baseDir = null;
		
		private File[] listOfFiles;
		
		public DirectoriesCrawler(String dirPath) {
			baseDir = new File(dirPath);
		}
		
		public void scanDirectories() {
			scanBaseDirectory();
			scanAllSubDirectories();
			listOfFiles = new File[listFiles.size()];
			convertListToArray();
		}
		
		private void scanBaseDirectory() {
			for (int i=0; i<baseDir.listFiles().length; i++) {
				if (baseDir.listFiles()[i].isFile()) {
					listFiles.add(baseDir.listFiles()[i]);
				} else if (baseDir.listFiles()[i].isDirectory()) {
					listDirs.add(baseDir.listFiles()[i]);
				}
			}
		}
		
		private void scanAllSubDirectories() {
			for (int i=0; i<listDirs.size(); i++) {
				File[] dir = listDirs.get(i).listFiles();
				for (int j=0; j<dir.length; j++) {
					if (dir[j].isFile()) {
						listFiles.add(dir[j]);
					} else if (dir[j].isDirectory()) {
						listDirs.add(dir[j]);
					}
				}
			}
		}
		
		private void convertListToArray() {
			for (int i=0; i<listFiles.size(); i++) { 
				listOfFiles[i] = listFiles.get(i);
			}
		}
		
		public File[] getAllFiles() {
			return listOfFiles;
		}
	}
	
	
	public static void main(String[] args) {
		try {
			new Main();
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
	}
	
}

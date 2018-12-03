import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {
	
	private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5PADDING";
    
    private boolean badDecryptionKey = false;
	
	public void encrypt(SecretKey key, SecretKey initVector, File inputFile, File outputFile) {
        doCrypto(Cipher.ENCRYPT_MODE, key, initVector, inputFile, outputFile);
    }
 
    public void decrypt(SecretKey key, SecretKey initVector, File inputFile, File outputFile) {
        doCrypto(Cipher.DECRYPT_MODE, key, initVector, inputFile, outputFile);
    }
 
    private void doCrypto(int cipherMode, SecretKey key, SecretKey initVector, File inputFile, File outputFile) {
        try {
        	IvParameterSpec iv = new IvParameterSpec(initVector.getEncoded());
        	Key secretKey = new SecretKeySpec(key.getEncoded(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            
			cipher.init(cipherMode, secretKey, iv);
			
			FileInputStream inputStream = new FileInputStream(inputFile);
	        byte[] inputBytes = new byte[(int) inputFile.length()];
	        inputStream.read(inputBytes);
	         
	        byte[] outputBytes = cipher.doFinal(inputBytes);
	         
	        FileOutputStream outputStream = new FileOutputStream(outputFile);
	        outputStream.write(outputBytes);
	         
	        inputStream.close();
	        outputStream.close();
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IOException | IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException e) {
			//e.printStackTrace();
			System.err.print("A bad key is used during decryption");
			badDecryptionKey = true;
		}
    }
    
    public boolean isDecryptionKeyBad() {
    	return badDecryptionKey;
    }
    
    public SecretKey generateIV() throws NoSuchAlgorithmException {
		long timestamp = System.currentTimeMillis()*1000;
		SecureRandom random = new SecureRandom();
		random.setSeed(timestamp);
		
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(128, random); // 128 bits key size
		return kgen.generateKey();
	}
	
	public SecretKey generateKey(String seed) throws NoSuchAlgorithmException {
		SecureRandom random = new SecureRandom();
		random.setSeed(convertSeed(seed));
		
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		kgen.init(128, random); // 128 bits key size
		return kgen.generateKey();
	}
	
	private long convertSeed(String strSeed) {
		long seed = 0;
	    for (int i = 0; i < strSeed.length(); i++) {
	        char ch = strSeed.charAt(i);               
	        seed = seed + (long)ch;
	    }
	    return seed;
	}
	
	public void writeKeyToFile(SecretKey key, String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(new File(filename));
		ObjectOutputStream out = new ObjectOutputStream(fos);
		try {
			out.writeObject(key);
		} finally {
			out.close();
		}
	}
	
	public SecretKey readKeyFromFile(String filename) throws IOException {
		FileInputStream fis = new FileInputStream(new File(filename));
		Key key = null;
		ObjectInputStream oin = new ObjectInputStream(fis);
		try {
		  key = (Key) oin.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
		  oin.close();
		}
		SecretKey sKey = new SecretKeySpec(key.getEncoded(), 0, key.getEncoded().length, "AES");
		return sKey;
	}
	
}

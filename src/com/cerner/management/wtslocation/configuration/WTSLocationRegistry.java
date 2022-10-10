
package com.cerner.management.wtslocation.configuration;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.cerner.management.exception.ManagementException;
import com.cerner.management.logging.ManagementLogger;
import com.cerner.windows.library.registry.NoSuchKeyException;
import com.cerner.windows.library.registry.NoSuchValueException;
import com.cerner.windows.library.registry.RegStringValue;
import com.cerner.windows.library.registry.Registry;
import com.cerner.windows.library.registry.RegistryException;
import com.cerner.windows.library.registry.RegistryKey;

/**
 * Registry for wtslocation data
 */
public class WTSLocationRegistry
{
   private static final Logger logger = ManagementLogger.getLogger("com.cerner.management.wtslocation.configuration");
   private static WTSLocationRegistry instance;
   private static int REG_TREE_ACCESS_PATH;
   private RegistryKey wtsKey;
   private RegistryKey logicalKey;

   /**
    * The key name for the database user stored in the registry
    */
   public static final String DB_USER_KEY = "DBUser";
   /**
    * The key name for the database password stored in the registry
    */
   public static final String DB_PASSWORD_KEY = "DBPassword";
   /**
    * The key name for the unicode database password stored in the registry
    */
   public static final String DB_PASSWORD_UNICODE_KEY = "DBPasswordUnicode";

   /**
    * Constants for encryption
    */
   private static final int SALT_LEN = 16;

   private static final byte[] HASH_SEED = new byte[]{(byte) 0xAB, (byte) 0x24, (byte) 0x65, (byte) 0xF4, (byte) 0x8A,
      (byte) 0xDE, (byte) 0x86, (byte) 0x33, (byte) 0xD9, (byte) 0x95, (byte) 0xF9, (byte) 0xC4, (byte) 0x26,
      (byte) 0x32, (byte) 0x8F, (byte) 0x0E, (byte) 0xDF};

   /**
    * @return an instance of the registry
    * @throws ManagementException
    */
   public synchronized static WTSLocationRegistry getInstance() throws ManagementException
   {
      if (instance == null)
      {
         instance = new WTSLocationRegistry();
      }

      return instance;
   }

   private WTSLocationRegistry() throws ManagementException
   {
      // Determine if the 64bit registry keys for WTSLocation exist. If they do, query those, if
      // not, query the 32 bit tree
      try
      {
         Registry.HKEY_LOCAL_MACHINE.openSubKey("Software\\Cerner\\WTSLocation", RegistryKey.ACCESS_ALL_64);

         // Success Means it exists, set access method to 64 bit.
         REG_TREE_ACCESS_PATH = RegistryKey.ACCESS_ALL_64;
      }
      catch (NoSuchKeyException e)
      {
         REG_TREE_ACCESS_PATH = RegistryKey.ACCESS_ALL_32;
      }

      RegistryKey parent = openOrCreateKey(Registry.HKEY_LOCAL_MACHINE, new String[]{"Software", "Cerner"});

      wtsKey = openOrCreateKey(parent, new String[]{"WTSLocation"});
      logicalKey = openOrCreateKey(parent, new String[]{"Logicals"});
   }

   private RegistryKey openOrCreateKey(RegistryKey parent, String[] subKeyNames) throws ManagementException
   {
      RegistryKey key = parent;

      for (int i = 0; i < subKeyNames.length; i++)
      {
         RegistryKey child;
         try
         {
            child = key.openSubKey(subKeyNames[i], REG_TREE_ACCESS_PATH);
         }
         catch (NoSuchKeyException e)
         {
            child = key.createSubKey(subKeyNames[i], "", REG_TREE_ACCESS_PATH);
         }

         key = child;
      }

      return key;
   }

   /**
    * Get the value for a registry entry in the wtslocation key
    * 
    * @param name
    * @param keyType
    * @return the registry value
    * @throws RegistryException
    */
   public String getValue(String name) throws RegistryException
   {
      try
      {
         return ((RegStringValue) wtsKey.getValue(name)).getData();
      }
      catch (NoSuchValueException e)
      {
         // do nothing because it is likely that the attribute was never set
         return "";
      }
   }

   /**
    * Get the value from the registry at HKLM/Software/Cerner/Logicals
    * 
    * @param name
    * @return the value
    */
   public String getLogicalValue(String name)
   {
      try
      {
         return ((RegStringValue) logicalKey.getValue(name)).getData();
      }
      catch (ManagementException e)
      {
         logger.log(Level.INFO, "Logicals registry value '" + name + "' not defined.", e);
         return null;
      }
   }

   /**
    * Sets the value of a registry key in the Logicals directory
    * 
    * @param name
    * @param value
    * @throws RegistryException
    */
   public void setLogicalValue(String name, String value) throws RegistryException
   {
      logicalKey.setValue(new RegStringValue(logicalKey, name, value));
   }

   /**
    * Set the value for a registry entry in the wtslocation key
    * 
    * @param name
    * @param value
    * @throws RegistryException
    */
   public void setValue(String name, String value) throws RegistryException
   {
      wtsKey.setValue(new RegStringValue(wtsKey, name, value));
   }

   /**
    * Deletes a registry value from a key
    * 
    * @param name
    * @param keyType
    * @throws ManagementException
    */
   public void deleteValue(String name) throws ManagementException
   {
      wtsKey.deleteValue(name);
   }
   
   /**
    * @return The decrypted database password from the registry
    */
   public String getPassword()
   {
      try
      {
         String useUnicodeString = getValue("UseUnicodeEncryption");
         boolean useUnicode = (useUnicodeString != null && useUnicodeString.equalsIgnoreCase("true") ? true : false);
         
         if(useUnicode)
         {
            String encryptedPassword = getValue(DB_PASSWORD_UNICODE_KEY);
            return WTSLocationRegistry.unicodeDecrypt(encryptedPassword);
         }
         
         String encryptedPassword = getValue(DB_PASSWORD_KEY);
         return WTSLocationRegistry.decrypt(encryptedPassword);
      }
      catch (RegistryException e)
      {
         logger.log(Level.SEVERE, "Failed to retrieve WTSLocaiton password.", e);
         return null;         
      }
      catch (ManagementException e)
      {
         logger.log(Level.SEVERE, "Failed to retrieve WTSLocaiton password.", e);
         return null;       
      }
   }

   /**
    * This method performs encrypting and prepares a string to be inserted into the registry or be
    * read from the registry. Since this code is to emulate and be compatible with the wtslocation
    * agent, we must encrpyt/decrypt in the default character set of the system (presumably
    * Windows-1252) and store it in the registry as UTF-16. When mapping Windows APIs/Java will map
    * the Windows-1252 characters to the UTF-16 equivalent if it exists, but should the character
    * not be mappable, Java uses the replacement character (0xFFFD) but Windows APIs use the same
    * value (i.e. if the character was 0x81, Windows API leave it as 0x0081 and Java replaces it
    * with 0xFFFD). Therefore in the Java below we manually perform what the Windows APIs do
    * automatically.
    * 
    * @param value
    * @return the encrypted string
    * @throws ManagementException
    */
   public static String encrypt(String value) throws ManagementException
   {
      if (value == null || value.length() == 0)
      {
         return value;
      }

      try
      {
         // we use an MD5 hash of the seed as the key for our
         // keying option two implementation of Triple DES encryption

         MessageDigest md = MessageDigest.getInstance("MD5");
         byte[] digestOfPassword = md.digest(HASH_SEED);

         // since MD5 only returns 16 bytes and Triple DES requires 24 (even with keying option 2)
         // we copy the first 6 bytes into the last 6 of the new 24 byte key

         byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
         System.arraycopy(digestOfPassword, 0, keyBytes, 16, 8);

         SecretKey key = new SecretKeySpec(keyBytes, "DESede");

         IvParameterSpec iv = new IvParameterSpec(new byte[8]);
         Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
         cipher.init(Cipher.ENCRYPT_MODE, key, iv);

         // add a salt to the beginning of the encrypted value

         byte[] salt = new byte[SALT_LEN];

         Random random = new Random();
         random.nextBytes(salt);

         byte[] pass = Arrays.copyOf(salt, salt.length + value.length());
         for (int j = 0, k = salt.length; j < value.length();)
         {
            pass[k++] = (byte) value.charAt(j++);
         }

         // encrypt
         byte[] enc = cipher.doFinal(pass);

         // we now do the converting of the replacement character
         CharBuffer chars = Charset.defaultCharset().decode(ByteBuffer.wrap(enc));

         for (int i = 0; i < chars.limit(); i++)
         {
            // if we find a replacement character we replace it with the two byte
            // equivalent of the encrypted value
            if (chars.get(i) == (char) 0xFFFD)
            {
               chars.put(i, (char) (0x00FF & enc[i]));
            }
         }

         return chars.toString();
      }
      catch (Exception e)
      {
         throw new ManagementException("Unable to encrypt value.", e);
      }
   }

   /**
    * This method performs decrypting and prepares a string to be read from the registry. Since this
    * code is to emulate and be compatible with the wtslocation agent, we must encrpyt/decrypt in
    * the default character set of the system (presumably Windows-1252) and store it in the registry
    * as UTF-16. When mapping Windows APIs/Java will map the Windows-1252 characters to the UTF-16
    * equivalent if it exists, but should the character not be mappable, Java uses the replacement
    * character (0xFFFD) but Windows APIs use the same value (i.e. if the character was 0x81,
    * Windows API leave it as 0x0081 and Java replaces it with 0xFFFD). Therefore in the Java below
    * we manually perform what the Windows APIs do automatically.
    * 
    * @param value
    * @return the decrypted string
    * @throws ManagementException
    */
   public static String decrypt(String value) throws ManagementException
   {
      if (value == null || value.length() == 0)
      {
         return value;
      }

      try
      {
         // we use an MD5 hash of the seed as the key for our
         // keying option two implementation of Triple DES encryption

         MessageDigest md = MessageDigest.getInstance("MD5");
         byte[] digestOfPassword = md.digest(HASH_SEED);

         // since MD5 only returns 16 bytes and Triple DES requires 24 (even with keying option 2)
         // we copy the first 6 bytes into the last 6 of the new 24 byte key

         byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
         System.arraycopy(digestOfPassword, 0, keyBytes, 16, 8);

         SecretKey key = new SecretKeySpec(keyBytes, "DESede");

         IvParameterSpec iv = new IvParameterSpec(new byte[8]);
         Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
         cipher.init(Cipher.DECRYPT_MODE, key, iv);

         // get the UTF-16 bytes (doing this way appends two extra bytes to the beginning so we
         // remove them)
         byte[] valueBytes = Arrays.copyOfRange(value.getBytes("UTF-16"), 2, value.getBytes("UTF-16").length);

         // get the replacement bytes
         Charset defaultCharset = Charset.defaultCharset();
         byte[] replacementBytes = defaultCharset.newEncoder().replacement();

         // get the byte array
         ByteBuffer buffer = defaultCharset.encode(value);
         byte[] bufferBytes = Arrays.copyOfRange(buffer.array(), 0, buffer.limit());

         // we search for the replacement byte and replace it with the low order byte of the UTF-16
         // character
         // because that was the character that couldn't be mapped initially
         for (int i = 0; i < bufferBytes.length; i++)
         {
            if (bufferBytes[i] == replacementBytes[0] && i * 2 + 1 < valueBytes.length
               && valueBytes[i * 2 + 1] != replacementBytes[0])
            {
               bufferBytes[i] = valueBytes[i * 2 + 1];
            }
         }

         // decrypt
         byte[] dec = cipher.doFinal(bufferBytes);

         // remove the salt
         return new String(dec).substring(SALT_LEN);
      }
      catch (Exception e)
      {
         throw new ManagementException("Unable to decrypt value.", e);
      }

   }

   /**
    * Decryption for unicode characters used by WTSLocation (i.e. SQL Server Auth password and
    * Millennium Password). Uses a 128-bit AES encryption. We encode with UTF-16LE because WTSLocation.exe
    * uses that for the default UTF-16 encoding. Java's default UTF-16 is Big Endian and starts with a 
    * Byte Order Marker (BOM). C# can consume it but by default does not produce a BOM and of course is
    * Little Endian.
    * 
    * @param value
    * @return The Base64 encoded, AES encrypted string
    * @throws ManagementException
    */
   public static String unicodeEncrypt(String value) throws ManagementException
   {
      if (value == null || value.length() == 0)
      {
         return value;
      }

      try
      {
         // generate a key and iv for encrypting
         SecureRandom secureRandom = new SecureRandom();
         
         byte[] key = new byte[16];
         secureRandom.nextBytes(key);


         byte[] iv = new byte[16];
         secureRandom.nextBytes(iv);
         
         // prepare the cipher
         final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
         cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));

         // encrypt
         byte[] cipherText = cipher.doFinal(value.getBytes("UTF-16LE"));

         // create the byte array with embedded iv and key
         ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + key.length + cipherText.length);
         
         byteBuffer.put(iv);
         byteBuffer.put(key);
         byteBuffer.put(cipherText);
         byte[] cipherMessage = byteBuffer.array();

         // create a base 64 encoded string
         return Base64.getEncoder().encodeToString(cipherMessage);
      }
      catch (Exception e)
      {
         throw new ManagementException("Failed to encrypt value.", e);
      }
   }

   /**
    * Decryption for unicode characters used by WTSLocation (i.e. SQL Server Auth password and
    * Millennium Password). Uses a 128-bit AES encryption. We encode with UTF-16LE because WTSLocation.exe
    * uses that for the default UTF-16 encoding. Java's default UTF-16 is Big Endian and starts with a 
    * Byte Order Marker (BOM). C# can consume it but by default does not produce a BOM and of course is
    * Little Endian.
    * 
    * 
    * @param value
    * @return The decrypted string
    * @throws ManagementException
    */
   public static String unicodeDecrypt(String value) throws ManagementException
   {
      if (value == null || value.length() == 0)
      {
         return value;
      }

      try
      {
         // base 64 decode the string
         ByteBuffer byteBuffer = ByteBuffer.wrap(Base64.getDecoder().decode(value));

         // pull out the iv and key
         byte[] iv = new byte[16];
         byteBuffer.get(iv);

         byte[] key = new byte[16];
         byteBuffer.get(key);

         // get the actual encrypted data
         byte[] cipherText = new byte[byteBuffer.remaining()];
         byteBuffer.get(cipherText);

         // decrypt the data
         Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
         cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));

         byte[] plainText = cipher.doFinal(cipherText);

         // return the decrypted string
         return new String(plainText, Charset.forName("UTF-16LE"));
      }
      catch (Exception e)
      {
         throw new ManagementException("Failed decrypt value.", e);
      }
   }
}

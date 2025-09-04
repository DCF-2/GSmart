// Localização: src/main/java/com/gsmart/utils/CryptoUtils.java
package main.java.com.gsmart.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe utilitária para encriptar e desencriptar dados usando o algoritmo AES.
 */
public class CryptoUtils {

    private static final Logger logger = LoggerFactory.getLogger(CryptoUtils.class);
    private static SecretKeySpec secretKey;
    private static final String ALGORITHM = "AES";

    // Chave de segurança. IMPORTANTE: Mude este valor para algo único e secreto no seu projeto.
    private static final String SECRET = "GSmartSuperSecretKey!";

    static {
        try {
            byte[] key = SECRET.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // usa apenas os primeiros 128 bit
            secretKey = new SecretKeySpec(key, ALGORITHM);
        } catch (Exception e) {
            logger.error("Erro ao inicializar a chave de criptografia.", e);
        }
    }

    /**
     * Encripta uma string.
     * @param strToEncrypt A string a ser encriptada.
     * @return A string encriptada em Base64, ou null em caso de erro.
     */
    public static String encrypt(String strToEncrypt) {
        if (strToEncrypt == null || strToEncrypt.isEmpty()) {
            return strToEncrypt;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            logger.error("Erro ao encriptar a string: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Desencripta uma string.
     * @param strToDecrypt A string encriptada em Base64.
     * @return A string original, ou null em caso de erro.
     */
    public static String decrypt(String strToDecrypt) {
        if (strToDecrypt == null || strToDecrypt.isEmpty()) {
            return strToDecrypt;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Se a desencriptação falhar, pode ser que o valor não estivesse encriptado (compatibilidade com versões antigas)
            // logger.warn("Falha ao desencriptar. O valor pode já estar em texto puro. Erro: {}", e.getMessage());
            return strToDecrypt; // Retorna o valor original
        }
    }
}
package com.shubhlab.jweencryption;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class DoubleEncryptionJweDemo {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {

        // ======================================================
        // 1) SAMPLE PCI OBJECT (what we want to protect)
        // ======================================================

        CardPayload original = new CardPayload(
                "4111111111111111",
                "JOHN DOE",
                "12",
                "2030"
        );
        System.out.println("Original object: " + original);

        // ======================================================
        // 2) GENERATE KEYPAIRS
        // ======================================================

        // WHAT THIS DOES:
        //   - innerKeyPair: used for the FIRST encryption layer (custom RSA).
        //   - jweKeyPair: used for the OUTER JWE layer.
        // In a real system, you would load these from keystores or KMS.

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);

        KeyPair innerKeyPair = keyGen.generateKeyPair();
        RSAPublicKey innerPublicKey = (RSAPublicKey) innerKeyPair.getPublic();
        RSAPrivateKey innerPrivateKey = (RSAPrivateKey) innerKeyPair.getPrivate();

        KeyPair jweKeyPair = keyGen.generateKeyPair();
        RSAPublicKey jwePublicKey = (RSAPublicKey) jweKeyPair.getPublic();
        RSAPrivateKey jwePrivateKey = (RSAPrivateKey) jweKeyPair.getPrivate();

        // ======================================================
        // 3) SERIALIZE OBJECT TO JSON
        // ======================================================

        // WHAT THIS DOES: converts Java object to JSON (plaintext).
        String jsonPayload = objectMapper.writeValueAsString(original);
        System.out.println("JSON payload: " + jsonPayload);

        // ======================================================
        // 4) INNER ENCRYPTION (CUSTOM RSA-OAEP)
        // ======================================================

        // WHAT THIS DOES:
        //   - Uses RSA public key (innerPublicKey) to encrypt the JSON.
        //   - Algorithm: RSA/ECB/OAEPWithSHA-256AndMGF1Padding
        //   - Result: innerCipherBytes (binary), then Base64 string for transport.
        Cipher innerCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        innerCipher.init(Cipher.ENCRYPT_MODE, innerPublicKey);
        byte[] innerCipherBytes = innerCipher.doFinal(jsonPayload.getBytes(StandardCharsets.UTF_8));
        String innerCipherBase64 = Base64.getEncoder().encodeToString(innerCipherBytes);

        System.out.println("\nInner encrypted (Base64): " + innerCipherBase64);

        // ======================================================
        // 5) OUTER ENCRYPTION: WRAP INNER CIPHERTEXT IN JWE
        // ======================================================

        // WHAT THIS DOES:
        //   - Build JWE header:
        //       alg = RSA-OAEP-256 (for CEK encryption using jwePublicKey)
        //       enc = A256GCM      (for payload encryption using CEK)
        JWEHeader header = new JWEHeader(
                JWEAlgorithm.RSA_OAEP_256,
                EncryptionMethod.A256GCM
        );

        // WHAT THIS DOES:
        //   - Treat innerCipherBase64 as the JWE payload (not the original JSON).
        Payload jwePayload = new Payload(innerCipherBase64);

        // WHAT THIS DOES:
        //   - Create JWE object (header + payload).
        JWEObject jweObject = new JWEObject(header, jwePayload);

        // WHAT THIS DOES:
        //   - Encrypt JWE using jwePublicKey.
        //   - Nimbus:
        //       * Generates a CEK (symmetric key),
        //       * Encrypts payload (innerCipherBase64) with AES-GCM using CEK,
        //       * Encrypts CEK with RSA-OAEP-256 using jwePublicKey,
        //       * Produces final 5-part JWE structure.
        JWEEncrypter jweEncrypter = new RSAEncrypter(jwePublicKey);
        jweObject.encrypt(jweEncrypter);

        // WHAT THIS DOES:
        //   - Serialize the full JWE to compact string for transport/storage.
        String jweString = jweObject.serialize();

        System.out.println("\n==== OUTER ENCRYPTED JWE STRING ====");
        System.out.println(jweString);

        // ==================================================================
        // RECEIVER SIDE
        // ==================================================================

        // ======================================================
        // 6) PARSE JWE ON RECEIVER SIDE
        // ======================================================

        // WHAT THIS DOES:
        //   - Simulate receiving the JWE string.
        //   - Parse back into a JWEObject.
        JWEObject receivedJwe = JWEObject.parse(jweString);

        System.out.println("\nReceived JWE header alg: " + receivedJwe.getHeader().getAlgorithm());
        System.out.println("Received JWE header enc: " + receivedJwe.getHeader().getEncryptionMethod());

        // ======================================================
        // 7) OUTER DECRYPTION (JWE LAYER)
        // ======================================================

        // WHAT THIS DOES:
        //   - Decrypt JWE using jwePrivateKey.
        //   - Nimbus:
        //       * RSA-OAEP-256 unwraps CEK,
        //       * AES-GCM decrypts payload,
        //       * gives us the original innerCipherBase64 string.
        JWEDecrypter jweDecrypter = new RSADecrypter(jwePrivateKey);
        receivedJwe.decrypt(jweDecrypter);

        String decryptedInnerBase64 = receivedJwe.getPayload().toString();
        System.out.println("\nDecrypted inner ciphertext (Base64): " + decryptedInnerBase64);

        // ======================================================
        // 8) INNER DECRYPTION (CUSTOM RSA LAYER)
        // ======================================================

        // WHAT THIS DOES:
        //   - Decode Base64 to get innerCipherBytes.
        //   - Use innerPrivateKey to decrypt RSA-OAEP and restore original JSON.
        byte[] receivedInnerBytes = Base64.getDecoder().decode(decryptedInnerBase64);
        Cipher innerDecryptCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        innerDecryptCipher.init(Cipher.DECRYPT_MODE, innerPrivateKey);
        byte[] decryptedJsonBytes = innerDecryptCipher.doFinal(receivedInnerBytes);
        String decryptedJson = new String(decryptedJsonBytes, StandardCharsets.UTF_8);

        System.out.println("Decrypted JSON after inner decrypt: " + decryptedJson);

        // ======================================================
        // 9) DESERIALIZE JSON BACK TO OBJECT
        // ======================================================

        // WHAT THIS DOES:
        //   - Map JSON -> CardPayload.
        CardPayload decryptedObject =
                objectMapper.readValue(decryptedJson.getBytes(StandardCharsets.UTF_8), CardPayload.class);

        System.out.println("Decrypted object: " + decryptedObject);
    }
}

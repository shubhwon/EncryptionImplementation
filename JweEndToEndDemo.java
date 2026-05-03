package com.shubhlab.jweencryption;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.util.Base64URL;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class JweEndToEndDemo {

    // WHAT THIS DOES: shared ObjectMapper used to convert between Java object and JSON.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {

        // =======================
        // 1) CREATE SAMPLE OBJECT
        // =======================

        // WHAT THIS DOES: build a sample PCI-like payload object we want to protect.
        CardPayload original = new CardPayload(
                "4111111111111111",
                "JOHN DOE",
                "12",
                "2030"
        );
        System.out.println("Original object: " + original);

        // =======================
        // 2) GENERATE RSA KEYPAIR
        // =======================

        // WHAT THIS DOES:
        //   - In real life you LOAD keys from a keystore, KMS, etc.
        //   - Here we GENERATE a 2048-bit RSA key pair just for the demo.
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair rsaKeyPair = keyGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) rsaKeyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) rsaKeyPair.getPrivate();

        // ===============================
        // 3) SERIALIZE OBJECT TO JSON
        // ===============================

        // WHAT THIS DOES: convert the Java object to a JSON string (your cleartext payload).
        String jsonPayload = objectMapper.writeValueAsString(original);
        System.out.println("JSON payload: " + jsonPayload);

        // ===============================
        // 4) BUILD JWE HEADER & PAYLOAD
        // ===============================

        // WHAT THIS DOES:
        //   - Set JWE algorithms:
        //       alg = RSA-OAEP-256  (key encryption, using RSA)
        //       enc = A256GCM        (content encryption, AES-256-GCM)
        //   - JWE header tells recipient how to decrypt.
            JWEHeader header = new JWEHeader(
                JWEAlgorithm.RSA_OAEP_256,
                EncryptionMethod.A256GCM
        );

        // WHAT THIS DOES: wrap our JSON string as the JWE payload.
        Payload payload = new Payload(jsonPayload);

        // WHAT THIS DOES: create a JWE object with header + plaintext payload.
        JWEObject jweObject = new JWEObject(header, payload);

        // ===============================
        // 5) ENCRYPT WITH PUBLIC KEY
        // ===============================

        // WHAT THIS DOES:
        //   - Create an RSAEncrypter with recipient's public key.
        //   - Library internally:
        //       * generates a random content encryption key (CEK),
        //       * encrypts JSON with AES-GCM using CEK,
        //       * encrypts CEK with RSA-OAEP-256 using publicKey,
        //       * builds the final JWE parts.
        JWEEncrypter encrypter = new RSAEncrypter(publicKey);
        jweObject.encrypt(encrypter);

        // WHAT THIS DOES:
        //   - Serialize JWE to compact form string:
        //     BASE64URL(header).BASE64URL(encryptedKey).BASE64URL(iv).BASE64URL(ciphertext).BASE64URL(tag)
        String jweString = jweObject.serialize();

        System.out.println("\n==== ENCRYPTED JWE STRING ====");
        System.out.println(jweString);

        // At this point, jweString is what you send over the wire or store.
        // It contains NO plaintext card data.

        // ===============================
        // 6) RECEIVER SIDE: PARSE JWE
        // ===============================

        // WHAT THIS DOES:
        //   - Simulate the receiver getting the JWE string.
        //   - Parse back into a JWEObject.
        JWEObject receivedJwe = JWEObject.parse(jweString);

        // Sanity: show header (alg, enc).
        JWEHeader receivedHeader = receivedJwe.getHeader();
        System.out.println("\nReceived JWE header alg: " + receivedHeader.getAlgorithm());
        System.out.println("Received JWE header enc: " + receivedHeader.getEncryptionMethod());

        // ===============================
        // 7) DECRYPT WITH PRIVATE KEY
        // ===============================

        // WHAT THIS DOES:
        //   - Create RSADecrypter with recipient's private key.
        //   - Library internally:
        //       * decrypts CEK using RSA-OAEP-256 and privateKey,
        //       * decrypts ciphertext with AES-GCM using CEK,
        //       * verifies authentication tag,
        //       * restores original plaintext JSON.
        JWEDecrypter decrypter = new RSADecrypter(privateKey);
        receivedJwe.decrypt(decrypter);

        // WHAT THIS DOES:
        //   - Extract the plaintext payload as string (original JSON).
        String decryptedJson = receivedJwe.getPayload().toString();
        System.out.println("\nDecrypted JSON: " + decryptedJson);

        // ===============================
        // 8) DESERIALIZE BACK TO OBJECT
        // ===============================

        // WHAT THIS DOES:
        //   - Convert JSON back to your Java DTO.
        CardPayload decryptedObject =
                objectMapper.readValue(decryptedJson.getBytes(StandardCharsets.UTF_8), CardPayload.class);

        System.out.println("Decrypted object: " + decryptedObject);
    }
}

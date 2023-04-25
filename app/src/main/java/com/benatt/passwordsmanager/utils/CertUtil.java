package com.benatt.passwordsmanager.utils;

import static com.benatt.passwordsmanager.utils.Constants.ALIAS;
import static com.benatt.passwordsmanager.utils.Constants.PRIVATE_KEY_FILE_NAME;
import static com.benatt.passwordsmanager.utils.Constants.IS_CERT_UPLOADED;

import android.content.Context;

import com.benardmathu.tokengeneration.GenerateRandomString;
import com.benatt.passwordsmanager.MainApp;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;

import org.apache.commons.codec.binary.Base64;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collections;

/**
 * @author ben-mathu
 * @date 2/15/23
 */
public class CertUtil {
    public static void exportPrivateKey(KeyStore keyStore,
                                        com.google.api.services.drive.model.File fileDirMetadata,
                                        com.google.api.services.drive.model.File backupFolder,
                                        Drive googleDriveService, Context context)
            throws Exception {

        // Export keystore certificate and store in Google Drive
//        Certificate certificate = keyStore.getCertificate(ALIAS);
//        PublicKey publicKey = certificate.getPublicKey();
        KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(ALIAS, null);

        PrivateKey privateKey = entry.getPrivateKey();
        byte[] encodedCert = privateKey.getEncoded();

        String certStr = Base64.encodeBase64String(encodedCert);

        FileOutputStream fos = context.openFileOutput(PRIVATE_KEY_FILE_NAME, Context.MODE_PRIVATE);
        OutputStreamWriter writer = new OutputStreamWriter(fos);

        writer.write(certStr);
        writer.flush();
        writer.close();

        File fileOutput = new File(context.getFilesDir(), PRIVATE_KEY_FILE_NAME);

        // Create the backup folder if it does not exist
        fileDirMetadata = new com.google.api.services.drive.model.File();
        fileDirMetadata.setName(fileOutput.getName());
        fileDirMetadata.setParents(Collections.singletonList(backupFolder.getId()));
        FileContent fileContent = new FileContent("text/plain", fileOutput);

        com.google.api.services.drive.model.File file =
                googleDriveService.files().create(fileDirMetadata, fileContent)
                        .setFields("id, parents")
                        .execute();

        MainApp.getPreferences().edit()
                .putBoolean(IS_CERT_UPLOADED, true)
                .apply();
    }

    public static PrivateKey getPrivateKey(com.google.api.services.drive.model.File certFile,
                                          Drive googleDriveService)
            throws IOException, NoSuchAlgorithmException,
            InvalidKeySpecException, KeyStoreException, CertificateException, UnrecoverableEntryException {

        try (BufferedInputStream inputStream =
                     (BufferedInputStream) googleDriveService.files().get(certFile.getId()).executeMediaAsInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder stringBuilder = new StringBuilder();
            String line = "";
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            byte[] keyBytes = Base64.decodeBase64(stringBuilder.toString());

            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = kf.generatePrivate(spec);

//            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
//            keyStore.load(null, "".toCharArray());
//
//            // -- start of update
//            CertificateFactory cf = CertificateFactory.getInstance("X.509");
//            Certificate certificate = cf.generateCertificate(new ByteArrayInputStream(keyBytes));
//
//            GenerateRandomString grs = new GenerateRandomString(6, new SecureRandom(),
//                    "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toLowerCase());
//            String alias = grs.nextString();
//            keyStore.setCertificateEntry(alias, certificate);
//            keyStore.store(null);
            // -- end of update

//            Certificate certificate = CertificateFactory.getInstance("X.509")
//                    .generateCertificate(new ByteArrayInputStream(keyBytes));
//            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
//            KeyProtection keyProtection
//                    = new KeyProtection.Builder(KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
//                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
//                    .setBlockModes(BLOCK_MODE_ECB)
//                    .build();
//            keyStore.load(null);
//            KeyStore.PrivateKeyEntry privateKeyEntry =
//                    (KeyStore.PrivateKeyEntry) keyStore.getEntry(ALIAS, null);
//            PrivateKey pKey = privateKeyEntry.getPrivateKey();
//            keyStore.setKeyEntry(ALIAS, pKey, null,
//                    new Certificate[]{certificate});
//            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
//            KeyFactory factory = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA);
//            PrivateKey pKey = factory.generatePrivate(spec);
//            PrivateKey privateKey = (PrivateKey) keyStore.getKey(ALIAS, "".toCharArray());
            return privateKey;
        }
    }
}

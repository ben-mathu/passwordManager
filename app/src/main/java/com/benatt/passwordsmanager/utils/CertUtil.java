package com.benatt.passwordsmanager.utils;

import static com.benatt.passwordsmanager.BuildConfig.ALIAS;
import static com.benatt.passwordsmanager.utils.Constants.IS_CERT_UPLOADED;
import static com.benatt.passwordsmanager.utils.Constants.PRIVATE_KEY_FILE_NAME;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;

import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InvalidObjectException;
import java.io.OutputStreamWriter;
import java.security.KeyStore;
import java.util.Collections;

/**
 * @author ben-mathu
 * @date 2/15/23
 */
public class CertUtil {
    public static void exportPrivateKey(KeyStore keyStore,
                                        com.google.api.services.drive.model.File fileDirMetadata,
                                        com.google.api.services.drive.model.File backupFolder,
                                        Drive googleDriveService, Context context, SharedPreferences preferences)
            throws Exception {

        // Export keystore certificate and store in Google Drive
        KeyStore.PrivateKeyEntry entry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(ALIAS, null);

        byte[] encodedCert = entry.getPrivateKey().getEncoded();

        String certStr = Base64.encodeBase64String(encodedCert);

        FileOutputStream fos = context.openFileOutput(PRIVATE_KEY_FILE_NAME, Context.MODE_PRIVATE);
        OutputStreamWriter writer = new OutputStreamWriter(fos);

        if (certStr.isEmpty())
            throw new InvalidObjectException("You do not have any passwords");

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

        preferences.edit()
                .putBoolean(IS_CERT_UPLOADED, true)
                .apply();
    }
}

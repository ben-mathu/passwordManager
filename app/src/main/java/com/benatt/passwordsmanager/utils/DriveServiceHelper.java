package com.benatt.passwordsmanager.utils;

import static com.benatt.passwordsmanager.utils.Constants.BACKUP_FOLDER;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private Drive drive;

    public DriveServiceHelper(Drive drive) {
        this.drive = drive;
    }

    public Task<String> createFile(String filePath, String fileName) {
        return Tasks.call(executor, () -> {
            File fileMetadata = new File();
            fileMetadata.setName(fileName);

            java.io.File file = new java.io.File(filePath);

            FileContent fileContent = new FileContent("text/plain", file);

            File newFile = null;

            newFile = drive.files().create(fileMetadata, fileContent).execute();

            if (newFile == null) {
                throw new IOException("Null request when requesting file creation");
            }

            return newFile.getId();
        });
    }

    public Task<OutputStream> getAllFiles() {
        return Tasks.call(executor, () -> {
            FileList driverList = null;
            File backupFolder = null;
            String query = "mimeType = 'application/vnd.google-apps.folder'" +
                    " and 'root' in parents and trashed = false";
            try {
                driverList = drive.files().list().setQ(query)
                        .setFields("files(id,name)").execute();

                List<com.google.api.services.drive.model.File> fileList = driverList.getFiles();

                for (com.google.api.services.drive.model.File file : fileList) {
                    if (BACKUP_FOLDER.equals(file.getName())) {
                        backupFolder = file;
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Get the security certificate from Google drive
            query = "mimeType = 'text/plain'" +
                    " and '" + backupFolder.getId() + "' in parents and trashed = false";
            FileList backupFolderList = drive.files().list().setQ(query)
                    .setFields("files(id,name)").execute();

            List<File> fileList = backupFolderList.getFiles();

            for (int i = 0; i < fileList.size(); i++) {
                if (i+1 == fileList.size()) break;

                SimpleDateFormat sp = new SimpleDateFormat("yyyyMMddHHmmssS", Locale.getDefault());

                String initialFile = fileList.get(i).getName().split("\\.")[0];
                Calendar initial = Calendar.getInstance();
                initial.setTime(sp.parse(initialFile));

                String nextFile = fileList.get(i+1).getName().split("\\.")[0];
                Calendar next = Calendar.getInstance();
                next.setTime(sp.parse(nextFile));
                if (initial.getTimeInMillis() > next.getTimeInMillis()) {
                    File temp = fileList.get(i);
                    fileList.set(i, fileList.get(i+1));
                    fileList.set(i+1, temp);
                }
            }

            if (fileList.size() > 5) {
                for (int i = 0; i < fileList.size() - 5; i++) {
                    drive.files().delete(fileList.get(i).getId());
                }
            }

            OutputStream outputStream = new ByteArrayOutputStream();
            drive.files().get(fileList.get(fileList.size() - 1).getId()).executeMediaAndDownloadTo(outputStream);

            return outputStream;
        });
    }
}

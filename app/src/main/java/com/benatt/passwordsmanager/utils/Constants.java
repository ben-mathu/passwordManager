package com.benatt.passwordsmanager.utils;

/**
 * @author bernard
 */
public class Constants {
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "password.db";

    public static final String INITIALIZATION_VECTOR = "initialization_vector";

    public static final String DELIMITER = ":";

    public static final String EDIT_PASSWORD = "edit_password";

    public static final String BACKUP_DATE_FORMAT = "ddMMyyyyHHmmss";

    public static final String IS_DISCLAIMER_SHOWN = "disclaimer";
    public static final String FILE_ID = "file_id";
    public static final String BACKUP_FOLDER = "PasswordsBackups";
    public static final String SIGNED_IN = "signed_in_with_pin";
    public static final String SIGNED_IN_WITH_GOOGLE = "signed_in";
    public static final String PRIVATE_KEY_FILE_NAME = "password_manager_private_key.txt";
    public static final String FOLDER_ID = "folder_id";
    public static final String IS_CERT_UPLOADED = "cert_uploaded";
    public static final String PASSWORDS_MIGRATED = "passwords_migrated";

    public static final String NAMED_PREV_KEY_ALIAS = "PREV_ALIAS";
    public static final String ID_TOKEN = "id_token";

    public static final String APP_PURCHASED = "purchased";

    public static final int PASSWORD_LIMIT = 5;
}

package com.benatt.core.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class AppUtil {
    private static final String TAG = AppUtil.class.getSimpleName();

    public static String readAppDescription(Context context, int resInt) {
        BufferedReader reader;
        StringBuilder builder = new StringBuilder();
        try(InputStream inputStream = context.getResources().openRawResource(resInt);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {

            reader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            return builder.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error reading raw text file line by line: " + e.getMessage(), e);
        }
        return null;
    }
}

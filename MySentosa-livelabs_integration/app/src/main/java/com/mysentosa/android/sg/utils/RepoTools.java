package com.mysentosa.android.sg.utils;

import android.database.Cursor;

public class RepoTools {

    public static String getString(Cursor cursor, String column) {
        if (cursor.getColumnIndex(column) == -1) {
            return null;
        }
        return cursor.getString(cursor.getColumnIndex(column));
    }

    public static int getInt(Cursor cursor, String column) {
        if (cursor.getColumnIndex(column) == -1) {
            return 0;
        }
        return cursor.getInt(cursor.getColumnIndex(column));
    }

    public static long getLong(Cursor cursor, String column) {
        if (cursor.getColumnIndex(column) == -1) {
            return 0;
        }
        return cursor.getLong(cursor.getColumnIndex(column));
    }

    public static double getDouble(Cursor cursor, String column) {
        if (cursor.getColumnIndex(column) == -1) {
            return 0;
        }
        return cursor.getDouble(cursor.getColumnIndex(column));
    }

    public static boolean getBoolean(Cursor cursor, String column) {
        if (cursor.getColumnIndex(column) == -1) {
            return false;
        }
        String bol = cursor.getString(cursor.getColumnIndex(column));
        if (bol.equals("true")) {
            return true;
        }
        return false;
    }

    public static boolean isRowAvailable(Cursor cursor) {
        boolean isRowAvailable = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                isRowAvailable = true;
            } else {
                cursor.close();
            }
        }

        return isRowAvailable;
    }

    public static String makePlaceHolders(int len) {
        if (len < 1) {
            throw new RuntimeException("No place holders");
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }
}
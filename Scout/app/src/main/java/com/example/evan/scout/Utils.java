package com.example.evan.scout;


import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.FormatFlagsConversionMismatchException;
import java.util.Iterator;
import java.util.List;

public class Utils {
    public static Object getField(Object object, String field) throws Exception {
        List<String> fields = Arrays.asList(field.split("\\."));
        if (fields.size() == 1) {
            return getDirectField(object, field);
        } else {
            Object parent = getField(object, field.substring(0, field.indexOf("." + fields.get(fields.size() - 1))));
            return getDirectField(parent, fields.get(fields.size() - 1));
        }
    }

    private static Object getDirectField(Object object, String field) throws Exception {
        if (object instanceof List) {return ((List)object).get(Integer.parseInt(field));}
        return findFieldInInheritedFields(object.getClass(), field).get(object);
    }

    private static Field findFieldInInheritedFields(Class clazz, String field) throws Exception {
        try {
            Field value = clazz.getDeclaredField(field);
            value.setAccessible(true);
            return value;
        } catch (NoSuchFieldException nsfe) {
            return findFieldInInheritedFields(clazz.getSuperclass(), field);
        }
    }

    public static void setField(Object object, String field, Object value) throws Exception {
        List<String> fields = Arrays.asList(field.split("\\."));
        Object parent;
        if (fields.size() == 1) {parent = object;}
        else {parent = getField(object, field.substring(0, field.indexOf("." + fields.get(fields.size() - 1))));}
        if (parent instanceof List) {
            ((List)parent).set(Integer.parseInt(fields.get(fields.size() - 1)), value);
        } else {
            Field variableToBeSet = findFieldInInheritedFields(parent.getClass(), fields.get(fields.size() - 1));
            variableToBeSet.setAccessible(true);
            variableToBeSet.set(parent, value);
        }
    }

    private static Gson gson = new Gson();
    public static String serializeClass(Object object) throws JsonParseException {return gson.toJson(object);}
    public static Object deserializeClass(String serializedClass, Class<?> clazz) throws  JsonParseException {
        return gson.fromJson(serializedClass, clazz);
    }

    public static void toastText(final String text, final int duration) {
        try {
            toastText(text, duration, ScoutApplication.getCurrentActivity());
        } catch (NullPointerException npe) {
            Log.e("Toast Error", "Failed to get Activity to toast");
        }
    }

    public static void toastText(final String text, final int duration, final Activity context) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, duration).show();
            }
        });
    }

    public static String readFile(Activity context, String name) {
        BufferedReader file;
        try {
            file = new BufferedReader(new InputStreamReader(new FileInputStream(
                    new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/MatchData/" + name))));
        } catch (IOException ioe) {
            Log.e("File Error", "Failed To Open File");
            Toast.makeText(context, "Failed To Open File", Toast.LENGTH_LONG).show();
            return null;
        }
        String text;
        try {
            text = file.readLine();
        } catch (IOException ioe) {
            Log.e("File Error", "Failed To Read From File");
            Toast.makeText(context, "Failed To Read From File", Toast.LENGTH_LONG).show();
            return null;
        }
        Log.i("JSON after read", text);
        return text;
    }

    public static class TwoValueStruct<K, V> {
        public TwoValueStruct(K value1, V value2) {this.value1 = value1; this.value2 = value2;}
        public K value1; public V value2;
    }
}

package com.example.evan.scout;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Calvin on 7/14/17.
 */

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


    private static Gson gson = new Gson();
    public static String serializeClass(Object object) throws JsonParseException {return gson.toJson(object);}
}

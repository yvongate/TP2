package com.uici.lecturmultimedia;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MediaFileManager {
    private static final String PREFS_NAME = "MediaFilesPrefs";
    private static final String KEY_MEDIA_FILES = "media_files";

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public MediaFileManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveMediaFiles(List<MediaFile> mediaFiles) {
        String json = gson.toJson(mediaFiles);
        sharedPreferences.edit().putString(KEY_MEDIA_FILES, json).apply();
    }

    public List<MediaFile> getMediaFiles() {
        String json = sharedPreferences.getString(KEY_MEDIA_FILES, null);
        if (json != null) {
            Type type = new TypeToken<ArrayList<MediaFile>>() {}.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }

    public void addMediaFile(MediaFile mediaFile) {
        List<MediaFile> mediaFiles = getMediaFiles();
        mediaFiles.add(mediaFile);
        saveMediaFiles(mediaFiles);
    }

    public void deleteMediaFile(String mediaFileId) {
        List<MediaFile> mediaFiles = getMediaFiles();
        mediaFiles.removeIf(file -> file.getId().equals(mediaFileId));
        saveMediaFiles(mediaFiles);
    }

    public void clearAllMediaFiles() {
        sharedPreferences.edit().remove(KEY_MEDIA_FILES).apply();
    }
}

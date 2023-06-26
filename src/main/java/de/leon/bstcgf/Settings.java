package de.leon.bstcgf;

import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Settings {
    private static final String APP_SETTINGS = "app_settings.json";

    private static final String PROFILE_FOLDER = "profiles\\";

    // at the moment the version is useless, but it will help if the structure of the settings are changing
    private static final String VERSION = "1";
    private static final String VERSION_KEY = "VERSION";
    private static final String NAME_KEY = "NAME";
    private static final String FILENAME_KEY = "FILE_NAME";
    private JSONObject settings;

    @Getter
    private Profile activeProfile;
    private File file;

    public Settings() {

        init();
    }

    private void init() {

        if (new File(APP_SETTINGS).exists()) {
            this.file = new File(APP_SETTINGS);
        } else {
            initNewFile();
        }

        try {
            loadData(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            activeProfile = new Profile(getActiveProfileData().getName());
        } catch (Exception e) {
            if (Profile.getAllProfils().isEmpty()) {
                activeProfile = new Profile("main");
            } else {
                activeProfile = new Profile(Profile.getAllProfils().get(0).getName());
            }
        }
    }

    private void loadData(File file) throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(file.getAbsolutePath());
        String jsonText;
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
            jsonText = scanner.useDelimiter("\\A").next();
        }
        settings = new JSONObject(jsonText);
    }

    private void initNewFile() {
        settings = new JSONObject();
        settings.put(VERSION_KEY, VERSION);

        this.file = new File(APP_SETTINGS);

        saveAsFile(file);
    }

    private void saveAsFile(File file) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(settings.toString(4));
            fileWriter.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void saveAsJSONObject(Settings.Setting setting, JSONObject value) {
        settings.put(setting.toString(), value);
        saveAsFile(file);
    }

    private void saveAsJSONArray(Settings.Setting setting, JSONArray value) {
        settings.put(setting.toString(), value);
        saveAsFile(file);
    }

    /**
     * Reads the settings and returns it as a Json String
     *
     * @param setting the {@link Profile.Setting} to read
     * @return A Json String
     * @throws NullPointerException If no value for the given Setting is found
     */
    private JSONObject getAsJSONObject(Settings.Setting setting) throws NullPointerException {
        try {
            return (JSONObject) settings.get(setting.toString().toUpperCase());
        } catch (JSONException jsonException) {
            throw new NullPointerException(jsonException.getMessage());
        }
    }

    /**
     * Reads the settings and returns it as a Json String
     *
     * @param setting the {@link Profile.Setting} to read
     * @return A Json String
     */
    private JSONArray getAsJSONArray(Settings.Setting setting) {
        try {
            return getAsJSONArrayRaw(setting);
        } catch (JSONException | NullPointerException ignore) {
            return new JSONArray();
        }
    }

    /**
     * Reads the settings and returns it as a Json String
     *
     * @param setting the {@link Profile.Setting} to read
     * @return A Json String
     * @throws NullPointerException If no value for the given Setting is found
     */
    private JSONArray getAsJSONArrayRaw(Settings.Setting setting) throws NullPointerException {
        try {
            return (JSONArray) settings.get(setting.toString().toUpperCase());
        } catch (JSONException | NullPointerException jsonException) {
            throw new NullPointerException(jsonException.getMessage());
        }
    }

    public void saveActiveProfileData(Profile.ProfileData profile) {
        this.activeProfile = new Profile(profile.getName());
        saveAsJSONObject(Setting.ACTIVE_PROFILE, profile.toJSONObject());
    }

    public void saveActiveProfile(Profile profile) {
        saveActiveProfileData(new Profile.ProfileData(profile.getFileName(), profile.getName()));
    }

    public Profile.ProfileData getActiveProfileData() {
        return Profile.ProfileData.fromJSONObject(getAsJSONObject(Setting.ACTIVE_PROFILE));
    }

    public enum Setting {
        ACTIVE_PROFILE;
    }
}

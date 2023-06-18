package de.leon.bstcgf;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Settings {

    // at the moment the version is useless, but it will help if the structure of the settings are changing
    private static final String VERSION = "1";
    private static final String VERSION_KEY = "version";
    private static final String NAME_KEY = "name";
    private JSONObject settings;
    private final String name;
    private final String fileName;

    File file;

    public Settings(String name) {
        this.name = name;
        this.fileName = name.replace(" ", "_") + ".json";

        init();
    }

    private void init() {
        file = new File(fileName);

        if (file.exists()) {
            try {
                InputStream inputStream = new FileInputStream(fileName);
                String jsonText;
                try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
                    jsonText = scanner.useDelimiter("\\A").next();
                }
                settings = new JSONObject(jsonText);

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            initNewFile();
        }
    }

    private void initNewFile() {
        settings = new JSONObject();

        settings.put(VERSION_KEY, VERSION);
        settings.put(NAME_KEY, this.name);

        saveAsFile();
    }

    private void saveAsFile() {
        try (FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write(settings.toString());
            fileWriter.flush();
        } catch (IOException ioException) {
            ioException.fillInStackTrace();
        }
    }

    public void save(Setting setting, JSONObject value) {
        settings.put(setting.toString(), value);
        saveAsFile();
    }

    /**
     * Reads the settings and returns it as a Json String
     *
     * @param setting the {@link Setting} to read
     * @throws NullPointerException If no value for the given Setting is found
     * @return A Json String
     */
    public JSONObject read(Setting setting) throws NullPointerException {
        try {
            return (JSONObject) settings.get(setting.toString().toUpperCase());
        } catch (JSONException jsonException) {
            throw new NullPointerException(jsonException.getMessage());
        }
    }

    public String getVersion() {
        return (String) settings.get(VERSION_KEY);
    }

    public String getName() {
        return (String) settings.get(NAME_KEY);
    }

    public enum Setting {
        REGION
    }
}

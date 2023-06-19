package de.leon.bstcgf;


import de.leon.bstcgf.data.CountryCode;
import de.leon.bstcgf.data.TableGameData;
import de.leon.bstcgf.data.steam.SteamGame;
import lombok.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
            fileWriter.write(settings.toString(4));
            fileWriter.flush();
        } catch (IOException ioException) {
            ioException.fillInStackTrace();
        }
    }

    private void saveAsJSONObject(Setting setting, JSONObject value) {
        settings.put(setting.toString(), value);
        saveAsFile();
    }

    private void saveAsJSONArray(Setting setting, JSONArray value) {
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
    private JSONObject getAsJSONObject(Setting setting) throws NullPointerException {
        try {
            return (JSONObject) settings.get(setting.toString().toUpperCase());
        } catch (JSONException jsonException) {
            throw new NullPointerException(jsonException.getMessage());
        }
    }

    /**
     * Reads the settings and returns it as a Json String
     *
     * @param setting the {@link Setting} to read
     * @throws NullPointerException If no value for the given Setting is found
     * @return A Json String
     */
    private JSONArray getAsJSONArray(Setting setting) throws NullPointerException {
        try {
            return (JSONArray) settings.get(setting.toString().toUpperCase());
        } catch (JSONException ignore) {
            return new JSONArray();
        }
    }

    private static Setting statusToSetting(TableGameData.Status status) {
        switch (status) {
            case PURCHASED:
                return Setting.PURCHASED_GAMES;
            case WISHLISTED:
                return Setting.WISHLISTED_GAMES;
            case IGNORED:
                return Setting.IGNORED_GAMES;
            case NONE:
                return Setting.NONE_STATUS_GAMES;
        }
        return null;
    }

    private static TableGameData.Status settingToStatus(Setting setting) {
        switch (setting) {
            case PURCHASED_GAMES:
                return TableGameData.Status.PURCHASED;
            case WISHLISTED_GAMES:
                return TableGameData.Status.WISHLISTED;
            case IGNORED_GAMES:
                return TableGameData.Status.IGNORED;
            case NONE_STATUS_GAMES:
                return TableGameData.Status.NONE;
        }
        return null;
    }

    private JSONArray addGameIdToJSONArray(Setting setting, TableGameData game) {
        JSONArray jsonArray = new JSONArray();
        removeGameIdFromOtherJSONArrays(setting, game);
        jsonArray = getAsJSONArray(setting);

        if (jsonArray.toList().stream().noneMatch(id -> id.equals(game.getId()))) {
            jsonArray.put(game.getId());
        }
        return jsonArray;
    }

    private void removeGameIdFromOtherJSONArrays(Setting setting, TableGameData game) {

        if (!setting.equals(Setting.PURCHASED_GAMES)) {
            List<Object> idList = getAsJSONArray(Setting.PURCHASED_GAMES).toList();
            idList.stream().filter(id -> id.equals(game.getId())).findFirst().ifPresent(idList::remove);
            saveAsJSONArray(Setting.PURCHASED_GAMES, new JSONArray(idList));

        } else if (!setting.equals(Setting.WISHLISTED_GAMES)) {
            List<Object> idList = getAsJSONArray(Setting.WISHLISTED_GAMES).toList();
            idList.stream().filter(id -> id.equals(game.getId())).findFirst().ifPresent(idList::remove);
            saveAsJSONArray(Setting.WISHLISTED_GAMES, new JSONArray(idList));

        } else if (!setting.equals(Setting.IGNORED_GAMES)) {
            List<Object> idList = getAsJSONArray(Setting.IGNORED_GAMES).toList();
            idList.stream().filter(id -> id.equals(game.getId())).findFirst().ifPresent(idList::remove);
            saveAsJSONArray(Setting.IGNORED_GAMES, new JSONArray(idList));

        } else if (!setting.equals(Setting.NONE_STATUS_GAMES)) {
            List<Object> idList = getAsJSONArray(Setting.NONE_STATUS_GAMES).toList();
            idList.stream().filter(id -> id.equals(game.getId())).findFirst().ifPresent(idList::remove);
            saveAsJSONArray(Setting.NONE_STATUS_GAMES, new JSONArray(idList));
        }

    }

    public String getVersion() {
        return (String) settings.get(VERSION_KEY);
    }

    public String getName() {
        return (String) settings.get(NAME_KEY);
    }

    public void saveCountryCode(CountryCode countryCode) {
        saveAsJSONObject(Setting.COUNTRY_CODE, countryCode.toJSONObject());
    }

    public CountryCode getCountryCode() throws NullPointerException {
        return CountryCode.fromJSONObject(getAsJSONObject(Setting.COUNTRY_CODE));
    }

    public void saveGameIdByStatus(TableGameData.Status status, TableGameData game) {
        saveAsJSONArray(Objects.requireNonNull(statusToSetting(status)), addGameIdToJSONArray(statusToSetting(status), game));
    }

    public List<Integer> getGameIdsByStatus(TableGameData.Status status) throws NullPointerException {
        return getAsJSONArray(Objects.requireNonNull(statusToSetting(status))).toList().stream().map(id -> Integer.valueOf(id.toString())).collect(Collectors.toList());
    }

    public TableGameData.Status getStatusByGameId(Integer gameId) {
        if (getAsJSONArray(Setting.PURCHASED_GAMES).toList().stream().anyMatch(id -> id.equals(gameId))) {
            return TableGameData.Status.PURCHASED;
        } else if (getAsJSONArray(Setting.WISHLISTED_GAMES).toList().stream().anyMatch(id -> id.equals(gameId))) {
            return TableGameData.Status.WISHLISTED;
        } else if (getAsJSONArray(Setting.IGNORED_GAMES).toList().stream().anyMatch(id -> id.equals(gameId))) {
            return TableGameData.Status.IGNORED;
        } else {
            return TableGameData.Status.NONE;
        }
    }

    public TableGameData.Status getStatusByGame(TableGameData game) {
        return getStatusByGameId(game.getId());
    }

    public enum Setting {
        COUNTRY_CODE,
        PURCHASED_GAMES,
        WISHLISTED_GAMES,
        IGNORED_GAMES,
        NONE_STATUS_GAMES;
    }
}

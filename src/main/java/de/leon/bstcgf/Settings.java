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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

public class Settings {

    private static final String ACTIVE_PROFILE_NAME = "active_profile.json";

    private static final String PROFILE_FOLDER = "profiles\\";

    // at the moment the version is useless, but it will help if the structure of the settings are changing
    private static final String VERSION = "1";
    private static final String VERSION_KEY = "version";
    private static final String NAME_KEY = "name";
    private JSONObject settings;
    private final String name;
    private final File file;

    public Settings(String name) {
        this.name = name;
        this.file = new File(PROFILE_FOLDER + name.replace(" ", "_") + ".json");

        init();
    }

    private void init() {

        if (!new File(ACTIVE_PROFILE_NAME).exists()) {
            initNewFile(new File(ACTIVE_PROFILE_NAME));
        }

        if (!new File(PROFILE_FOLDER).exists()) {
            try {
                Files.createDirectories(Paths.get(new File(PROFILE_FOLDER).getAbsolutePath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (file.exists()) {
            activateProfileOrCreateNew();
        } else {
            initNewFile(file);
        }
    }

    private void activateProfileOrCreateNew() {
        try {
            Path selectedProfilePath = Paths.get(file.getAbsolutePath());
            Path activeProfilePath = Paths.get(new File(ACTIVE_PROFILE_NAME).getAbsolutePath());
            Files.copy(selectedProfilePath, activeProfilePath, StandardCopyOption.REPLACE_EXISTING);

            loadData();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadData() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(ACTIVE_PROFILE_NAME);
        String jsonText;
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
            jsonText = scanner.useDelimiter("\\A").next();
        }
        settings = new JSONObject(jsonText);
    }

    private void initNewFile(File file) {
        settings = new JSONObject();

        settings.put(VERSION_KEY, VERSION);
        settings.put(NAME_KEY, this.name);

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

    private void saveAsJSONObject(Setting setting, JSONObject value) {
        settings.put(setting.toString(), value);
        saveAsFile(file);
        activateProfileOrCreateNew();
    }

    private void saveAsJSONArray(Setting setting, JSONArray value) {
        settings.put(setting.toString(), value);
        saveAsFile(file);
        activateProfileOrCreateNew();
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
     * @return A Json String
     */
    private JSONArray getAsJSONArray(Setting setting) {
        try {
            return getAsJSONArrayRaw(setting);
        } catch (JSONException | NullPointerException ignore) {
            return new JSONArray();
        }
    }

    /**
     * Reads the settings and returns it as a Json String
     *
     * @param setting the {@link Setting} to read
     * @throws NullPointerException If no value for the given Setting is found
     * @return A Json String
     */
    private JSONArray getAsJSONArrayRaw(Setting setting) throws NullPointerException {
        try {
            return (JSONArray) settings.get(setting.toString().toUpperCase());
        } catch (JSONException | NullPointerException jsonException) {
            throw new NullPointerException(jsonException.getMessage());
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

    public void saveStatusFilter(List<TableGameData.Status> statusList) {
        JSONArray jsonArray = new JSONArray();
        jsonArray.putAll(statusList);
        saveAsJSONArray(Setting.STATUS_FILTER, jsonArray);
    }

    public List<TableGameData.Status> getStatusFilter() {

        // if a NPE is thrown, that means there are no status filter saved, in that case the array for the saved status filter will be created with all statuses
        try {
            return getAsJSONArrayRaw(Setting.STATUS_FILTER).toList().stream().map(status -> TableGameData.Status.valueOf(status.toString().toUpperCase())).collect(Collectors.toList());
        } catch (NullPointerException ignored) {
            saveStatusFilter(List.of(TableGameData.Status.values()));
            return getStatusFilter();
        }
    }

    public enum Setting {
        COUNTRY_CODE,
        PURCHASED_GAMES,
        WISHLISTED_GAMES,
        IGNORED_GAMES,
        NONE_STATUS_GAMES,
        STATUS_FILTER;
    }
}

package de.leon.bstcgf;

import de.leon.bstcgf.data.CountryCode;
import de.leon.bstcgf.data.CountryCode.Key;
import de.leon.bstcgf.data.GitHubInformation;
import de.leon.bstcgf.data.steam.SteamJsonData;
import de.leon.bstcgf.data.steamcardexchange.SteamCardExchangeJsonData;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class Request {

    private final static String STEAM_CARD_EXCHANGE_URL = "https://www.steamcardexchange.net/api/request.php?GetBadgePrices_Guest";
    private final static String STEAM_STORE_API_APP_DETAILS_URL = "https://store.steampowered.com/api/appdetails?appids=%s&cc=%s&filters=price_overview";
    private final static String STEAM_STORE_API_ALL_COUNTRY_CODES_URL = "https://store.steampowered.com/api/checkoutcountrydata/?l=english";
    private final static String GITHUB_API_REPO_LATEST_RELEASE_URL = "https://api.github.com/repos/n0eL1405/BestSteamTradingCardGamesFinder/releases/latest";

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static SteamCardExchangeJsonData getSteamCardExchangeData() throws IOException {

        URL url = new URL(STEAM_CARD_EXCHANGE_URL);
        InputStream inputStream = url.openStream();
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(readAll(
                new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))));
        } finally {
            inputStream.close();
        }

        return SteamCardExchangeJsonData.fromJSONObject(jsonObject);
    }

    public static SteamJsonData getGameDataFromSteamIds(List<Integer> ids, CountryCode countryCode)
        throws IOException {

        if (ids.size() < 1) {
            throw new IllegalArgumentException("Not enough ids (min 1)");
        } else if (ids.size() > 100) {
            throw new IllegalArgumentException("Too many ids (max 100)");
        }

        StringBuilder idStringBuilder = new StringBuilder();
        idStringBuilder.append(ids.get(0));

        for (int i = 1; i <= ids.size() - 1; i++) {
            idStringBuilder.append(",");
            idStringBuilder.append(ids.get(i));
        }

        URL url = new URL(
            "https://store.steampowered.com/api/appdetails?appids=" + idStringBuilder
                + "&cc=" + countryCode.getCode()
                + "&filters=price_overview");

        url = new URL(String.format(STEAM_STORE_API_APP_DETAILS_URL, idStringBuilder, countryCode.getCode()));

        InputStream inputStream = url.openStream();
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(readAll(
                new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))));
        } finally {
            inputStream.close();
        }

        return SteamJsonData.fromJSONObject(jsonObject);
    }

    public static GitHubInformation getInfoLatestRelease() throws IOException {

        URL url = new URL(GITHUB_API_REPO_LATEST_RELEASE_URL);
        InputStream inputStream = url.openStream();
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(readAll(
                new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))));
        } finally {
            inputStream.close();
        }

        return GitHubInformation.fromJSONObject(jsonObject);
    }

    public static List<CountryCode> getSteamCountryCodes() throws IOException {

        List<CountryCode> countryCodeList = new LinkedList<>();

        URL url = new URL(STEAM_STORE_API_ALL_COUNTRY_CODES_URL);
        InputStream inputStream = url.openStream();
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(readAll(
                new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))));
        } finally {
            inputStream.close();
        }

        ((JSONArray) jsonObject.get(Key.COUNTRIES.toString())).forEach(cc -> countryCodeList.add(
            CountryCode.fromJSONObject((JSONObject) cc)
        ));

        return countryCodeList;
    }
}

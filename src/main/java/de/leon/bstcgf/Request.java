package de.leon.bstcgf;

import de.leon.bstcgf.data.GitHubInformation;
import de.leon.bstcgf.data.steam.SteamJsonData;
import de.leon.bstcgf.data.steamcardexchange.SteamCardExchangeJsonData;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.json.JSONObject;

public class Request {

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static SteamCardExchangeJsonData getSteamCardExchangeData() throws IOException {

        URL url = new URL("https://www.steamcardexchange.net/api/request.php?GetBadgePrices_Guest");
        InputStream inputStream = url.openStream();
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(readAll(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))));
        } finally {
            inputStream.close();
        }

        return SteamCardExchangeJsonData.fromJSONObject(jsonObject);
    }

    public static SteamJsonData getGameDataFromSteamIds(List<Integer> ids)
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

        URL url = new URL("https://store.steampowered.com/api/appdetails?appids=" + idStringBuilder + "&cc=EE&filters=price_overview");
        InputStream inputStream = url.openStream();
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(readAll(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))));
        } finally {
            inputStream.close();
        }

        return SteamJsonData.fromJSONObject(jsonObject);
    }

    public static GitHubInformation getInfoLatestRelease() throws IOException {

        URL url = new URL("https://api.github.com/repos/n0eL1405/BestSteamTradingCardGamesFinder/releases/latest");
        InputStream inputStream = url.openStream();
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(readAll(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))));
        } finally {
            inputStream.close();
        }

        return GitHubInformation.fromJSONObject(jsonObject);
    }
}

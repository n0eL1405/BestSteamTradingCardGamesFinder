package de.leon.bstcgf.data.steam;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SteamJsonData {

    private List<SteamGame> steamGames;

    /**
     * Each steam game id is his own object, because of that I can't use normal Json mapping.<br>
     * This method gets through each internal HashMap and gets the value for each key one by
     * one.<br> The downside is, if the Json changes, this probably won't work anymore and has to be
     * updated with new keys and/or structure.
     */
    @SuppressWarnings("unchecked")
    public static SteamJsonData fromJSONObject(JSONObject jsonObject) {

        List<SteamGame> steamGames = new LinkedList<>();

        for (var game : jsonObject.toMap().entrySet()) {

            HashMap<String, Object> gameHashMap = (HashMap<String, Object>) game.getValue();
            SteamPriceOverview steamPriceOverview;

            try {

                HashMap<String, Object> gameDataHashMap = (HashMap<String, Object>) gameHashMap.get(
                    SteamGame.Key.DATA.toString());

                HashMap<String, Object> priceOverviewHashMap = (HashMap<String, Object>) gameDataHashMap.get(
                    SteamGameData.Key.PRICE_OVERVIEW.toString());

                steamPriceOverview = SteamPriceOverview.builder()
                    .currency(
                        priceOverviewHashMap.get(SteamPriceOverview.Key.CURRENCY.toString())
                            .toString())
                    .finalPriceFormatted(priceOverviewHashMap.get(
                        SteamPriceOverview.Key.FINAL_FORMATTED.toString()).toString())
                    .finalPrice(Integer.parseInt(
                        priceOverviewHashMap.get(SteamPriceOverview.Key.FINAL.toString())
                            .toString()))
                    .initialPriceFormatted(priceOverviewHashMap.get(
                        SteamPriceOverview.Key.INITIAL_FORMATTED.toString()).toString())
                    .initialPrice(Integer.parseInt(
                        priceOverviewHashMap.get(SteamPriceOverview.Key.INITIAL.toString())
                            .toString()))
                    .discountPercentage(Integer.parseInt(priceOverviewHashMap.get(
                        SteamPriceOverview.Key.DISCOUNT_PERCENT.toString()).toString()))
                    .build();

            } catch (Exception e) {

                steamPriceOverview = SteamPriceOverview.free2play();

            }

            SteamGame steamGame = SteamGame.builder()
                .id(Integer.parseInt(game.getKey()))
                .success(Boolean.parseBoolean(
                    gameHashMap.get(SteamGame.Key.SUCCESS.toString()).toString()))
                .data(SteamGameData.builder()
                    .steamPriceOverview(steamPriceOverview)
                    .build())
                .build();

            steamGames.add(steamGame);
        }
        return new SteamJsonData(steamGames);
    }

    public int size() {
        return steamGames.size();
    }

}

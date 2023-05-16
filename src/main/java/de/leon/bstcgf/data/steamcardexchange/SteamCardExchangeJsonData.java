package de.leon.bstcgf.data.steamcardexchange;

import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SteamCardExchangeJsonData {

    private List<SteamCardExchangeGameData> steamCardExchangeGameData;

    public static SteamCardExchangeJsonData fromJSONObject(JSONObject jsonObject) {

        List<SteamCardExchangeGameData> steamCardExchangeGameDataList = new LinkedList<>();

        JSONArray data = (JSONArray) jsonObject.get(Key.DATE.toString());
        data.forEach(game -> {
            JSONArray gameArray = (JSONArray) game;
            JSONArray gameDataArray = (JSONArray) gameArray.get(0);

            SteamCardExchangeGameData steamCardExchangeGameData = SteamCardExchangeGameData.builder()
                .id(Integer.parseInt((String) gameDataArray.get(0)))
                .name((String) gameDataArray.get(1))
                .tradingCards((Integer) gameArray.get(1))
                .valueOfAllCards((String) gameArray.get(2))
                .build();

            steamCardExchangeGameDataList.add(steamCardExchangeGameData);
        });
        return new SteamCardExchangeJsonData(steamCardExchangeGameDataList);
    }

    public List<Integer> getOnlyIds() {
        List<Integer> ids = new LinkedList<>();

        steamCardExchangeGameData.forEach(game -> {
            ids.add(game.getId());
        });

        return ids;
    }

    public List<SteamCardExchangeJsonData> getInPackages(int packageSize) {

        int totalCount = 1;
        List<SteamCardExchangeJsonData> mainPackage = new LinkedList<>();
        List<SteamCardExchangeGameData> gameDataPackage;

        do {

            gameDataPackage = new LinkedList<>();

            for (int i = 0; i < packageSize; i++) {

                gameDataPackage.add(steamCardExchangeGameData.get(totalCount - 1));

                totalCount++;

                if (totalCount > steamCardExchangeGameData.size()) break;
            }
            mainPackage.add(new SteamCardExchangeJsonData(gameDataPackage));

        } while (totalCount < steamCardExchangeGameData.size());

        return mainPackage;
    }

    public int size() {
        return steamCardExchangeGameData.size();
    }

    public enum Key {
        DATE;


        @Override
        public String toString() {
            switch (this) {
                case DATE:
                    return "data";
            }
            return null;
        }
    }

}

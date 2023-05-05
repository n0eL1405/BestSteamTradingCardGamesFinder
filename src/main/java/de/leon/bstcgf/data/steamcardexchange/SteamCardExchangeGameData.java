package de.leon.bstcgf.data.steamcardexchange;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
@Builder
public class SteamCardExchangeGameData {

    @NonNull
    private final int id;
    @NonNull
    private final String name;
    @NonNull
    private final int tradingCards;
    private final String valueOfAllCards;
    private final String unknown;

    public SteamCardExchangeGameData(int id, String name, int tradingCards) {
        this(id, name, tradingCards, null, null);
    }
}

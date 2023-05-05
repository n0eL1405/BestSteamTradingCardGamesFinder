package de.leon.bstcgf.data.steam;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SteamGameData {

    private SteamPriceOverview steamPriceOverview;

    public enum Key {
        PRICE_OVERVIEW;


        @Override
        public String toString() {
            switch (this) {
                case PRICE_OVERVIEW -> {
                    return "price_overview";
                }
            };
            return null;
        }
    }

}

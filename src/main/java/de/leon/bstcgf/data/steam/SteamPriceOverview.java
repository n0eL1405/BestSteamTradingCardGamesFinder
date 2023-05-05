package de.leon.bstcgf.data.steam;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SteamPriceOverview {

    private String currency; // could be an enum I think, maybe later
    private int initialPrice; // in minimal currency, e.g. 19,99€ would be 1999
    private int finalPrice; // in minimal currency, e.g. 19,99€ would be 1999
    private int discountPercentage;
    private String initialPriceFormatted;
    private String finalPriceFormatted;

    public static SteamPriceOverview free2play() {
        return SteamPriceOverview.builder()
            .currency("EUR")
            .initialPrice(0)
            .finalPrice(0)
            .discountPercentage(0)
            .initialPriceFormatted("0,00€")
            .finalPriceFormatted("0,00€")
            .build();
    }

    public enum Key {
        CURRENCY,
        INITIAL,
        FINAL,
        DISCOUNT_PERCENT,
        INITIAL_FORMATTED,
        FINAL_FORMATTED;


        @Override
        public String toString() {
            switch (this) {
                case CURRENCY -> {
                    return "currency";
                }
                case INITIAL -> {
                    return "initial";
                }
                case FINAL -> {
                    return "final";
                }
                case DISCOUNT_PERCENT -> {
                    return "discount_percent";
                }
                case INITIAL_FORMATTED -> {
                    return "initial_formatted";
                }
                case FINAL_FORMATTED -> {
                    return "final_formatted";
                }
            };
            return null;
        }
    }
}

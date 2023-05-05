package de.leon.bstcgf.data.steam;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SteamGame {

    private int id;
    private boolean success;
    private SteamGameData data;


    public enum Key {
        SUCCESS,
        DATA;


        @Override
        public String toString() {
            switch (this) {
                case SUCCESS:
                    return "success";
                case DATA:
                    return "data";
            }
            return null;
        }
    }
}

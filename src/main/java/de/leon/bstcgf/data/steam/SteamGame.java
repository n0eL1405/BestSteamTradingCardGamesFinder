package de.leon.bstcgf.data.steam;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Builder
public class SteamGame {

    private int id;
    private boolean success;
    private SteamGameData data;

    public SteamGame(int id, boolean success, SteamGameData data) {
        this.id = id;
        this.success = success;
        this.data = data;
    }

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

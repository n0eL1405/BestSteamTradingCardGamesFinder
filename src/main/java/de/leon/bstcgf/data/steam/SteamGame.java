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
    private Status status;

    public SteamGame(int id, boolean success, SteamGameData data) {
        this(id, success, data, Status.NONE);
    }

    public SteamGame(int id, boolean success, SteamGameData data, Status status) {
        this.id = id;
        this.success = success;
        this.data = data;
        this.status = Objects.requireNonNullElse(status, Status.NONE);
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

    public enum Status {
        PURCHASED,
        WISHLISTED,
        IGNORED,
        NONE;
    }
}

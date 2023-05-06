package de.leon.bstcgf.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.json.JSONObject;

@Data
@Getter
@AllArgsConstructor
public class CountryCode {

    private String code;
    private String label;

    public static CountryCode fromJSONObject(JSONObject jsonObject) {
        return new CountryCode(
            jsonObject.get(Key.CODE.toString()).toString(),
            jsonObject.get(Key.LABEL.toString()).toString()
        );
    }

    public enum Key {
        COUNTRIES,
        CODE,
        LABEL;


        @Override
        public String toString() {
            switch (this) {
                case COUNTRIES:
                    return "countries";
                case CODE:
                    return "code";
                case LABEL:
                    return "label";
            }
            return null;
        }
    }

}

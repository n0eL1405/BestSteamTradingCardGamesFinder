package de.leon.bstcgf.data;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;

@Data
@Builder
@Getter
public class GitHubInformation {

    private String version;
    private String name;
    private String url;
    private String downloadUrl;

    public static GitHubInformation fromJSONObject(JSONObject jsonObject) {

        JSONObject assets = ((JSONArray) jsonObject.get(Key.ASSETS.toString())).getJSONObject(0);

        return GitHubInformation.builder()
            .version(jsonObject.get(Key.TAG_NAME.toString()).toString())
            .name(jsonObject.get(Key.NAME.toString()).toString())
            .url(jsonObject.get(Key.HTML_URL.toString()).toString())
            .downloadUrl(assets.get(Key.BROWSER_DOWNLOAD_URL.toString()).toString())
            .build();

    }

    public enum Key {
        TAG_NAME,
        NAME,
        HTML_URL,
        BROWSER_DOWNLOAD_URL,
        ASSETS;


        @Override
        public String toString() {
            switch (this) {
                case TAG_NAME:
                    return "tag_name";
                case NAME:
                    return "name";
                case HTML_URL:
                    return "html_url";
                case BROWSER_DOWNLOAD_URL:
                    return "browser_download_url";
                case ASSETS:
                    return "assets";
            }
            return null;
        }
    }

}

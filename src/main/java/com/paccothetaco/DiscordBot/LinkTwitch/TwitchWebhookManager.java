package com.paccothetaco.DiscordBot.LinkTwitch;

import java.io.IOException;
import okhttp3.*;

public class TwitchWebhookManager {
    private static final String CALLBACK_URL = "https://yourdomain.com/twitch/callback";
    private static final String HUB_URL = "https://api.twitch.tv/helix/webhooks/hub";
    private static final String CLIENT_ID = "your_twitch_client_id";
    private static final String TOKEN = "your_twitch_token";
    private static OkHttpClient client = new OkHttpClient();

    public static void subscribeToStream(String twitchUserId) {
        String json = "{ \"hub.callback\": \"" + CALLBACK_URL + "\", " +
                "\"hub.mode\": \"subscribe\", " +
                "\"hub.topic\": \"https://api.twitch.tv/helix/streams?user_id=" + twitchUserId + "\", " +
                "\"hub.lease_seconds\": 864000, " +
                "\"hub.secret\": \"your_secret\"}";

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(HUB_URL)
                .post(body)
                .addHeader("Client-ID", CLIENT_ID)
                .addHeader("Authorization", "Bearer " + TOKEN)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            System.out.println(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

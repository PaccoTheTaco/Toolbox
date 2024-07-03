package com.paccothetaco.DiscordBot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.paccothetaco.DiscordBot.Utils.SecretUtil;
import okhttp3.*;

import java.io.IOException;

public class ToolboxGPT {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final OkHttpClient client = new OkHttpClient();

    public static String getGPTResponse(String prompt) throws IOException {
        String apiKey = SecretUtil.getOpenAIKey();

        JsonObject json = new JsonObject();
        json.addProperty("model", "gpt-3.5-turbo");
        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", prompt);
        messages.add(message);
        json.add("messages", messages);
        json.addProperty("max_tokens", 150);
        json.addProperty("temperature", 0.7);

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body().string();
                throw new IOException("Unexpected code " + response + " Body: " + responseBody);
            }

            String responseBody = response.body().string();
            JsonObject responseObject = JsonParser.parseString(responseBody).getAsJsonObject();
            return responseObject.getAsJsonArray("choices").get(0).getAsJsonObject().get("message").getAsJsonObject().get("content").getAsString();
        }
    }
}

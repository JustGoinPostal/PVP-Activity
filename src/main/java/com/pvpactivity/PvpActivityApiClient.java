package com.pvpactivity;

import com.google.gson.Gson;
import com.pvpactivity.model.ActivityResponse;
import com.pvpactivity.model.HeartbeatRequest;
import com.pvpactivity.model.WorldActivity;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import javax.inject.Inject;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PvpActivityApiClient
{
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final Gson gson;
    private final PvpActivityConfig config;

    @Inject
    PvpActivityApiClient(OkHttpClient httpClient, Gson gson, PvpActivityConfig config)
    {
        this.httpClient = httpClient;
        this.gson = gson;
        this.config = config;
    }

    public void sendHeartbeat(HeartbeatRequest heartbeat, Consumer<Boolean> callback)
    {
        try
        {
            Request request = new Request.Builder()
                .url(baseUrl() + "/v1/heartbeat")
                .post(RequestBody.create(JSON, gson.toJson(heartbeat)))
                .build();

            httpClient.newCall(request).enqueue(new Callback()
            {
                @Override
                public void onFailure(Call call, IOException e)
                {
                    callback.accept(false);
                }

                @Override
                public void onResponse(Call call, Response response)
                {
                    try (Response ignored = response)
                    {
                        callback.accept(response.isSuccessful());
                    }
                }
            });
        }
        catch (RuntimeException ex)
        {
            callback.accept(false);
        }
    }

    public void fetchActivity(String sessionId, Consumer<List<WorldActivity>> success, Runnable failure)
    {
        try
        {
            Request request = new Request.Builder()
                .url(baseUrl() + "/v1/activity")
                .header("X-PVP-Session-ID", sessionId)
                .get()
                .build();

            httpClient.newCall(request).enqueue(new Callback()
            {
                @Override
                public void onFailure(Call call, IOException e)
                {
                    failure.run();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException
                {
                    try (Response ignored = response)
                    {
                        if (!response.isSuccessful() || response.body() == null)
                        {
                            failure.run();
                            return;
                        }

                        ActivityResponse parsed = gson.fromJson(response.body().charStream(), ActivityResponse.class);
                        success.accept(parsed == null ? Collections.emptyList() : parsed.getWorlds());
                    }
                    catch (RuntimeException ex)
                    {
                        failure.run();
                    }
                }
            });
        }
        catch (RuntimeException ex)
        {
            failure.run();
        }
    }

    public void removeSession(String sessionId)
    {
        try
        {
            Request request = new Request.Builder()
                .url(baseUrl() + "/v1/session/" + sessionId)
                .delete()
                .build();
            httpClient.newCall(request).enqueue(new Callback()
            {
                @Override
                public void onFailure(Call call, IOException e)
                {
                    // Best effort. The server TTL also removes stale sessions.
                }

                @Override
                public void onResponse(Call call, Response response)
                {
                    response.close();
                }
            });
        }
        catch (RuntimeException ignored)
        {
            // Best effort.
        }
    }

    private String baseUrl()
    {
        String url = config.apiUrl().trim();
        while (url.endsWith("/"))
        {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}

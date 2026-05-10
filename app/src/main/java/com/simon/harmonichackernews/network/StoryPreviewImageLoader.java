package com.simon.harmonichackernews.network;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

public class StoryPreviewImageLoader {

    public interface PreviewImageCallback {
        void onPreviewImageUrlLoaded(String imageUrl);
    }

    private static final int MAX_CACHE_SIZE = 300;
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final Map<String, String> IMAGE_CACHE = new HashMap<>();
    private static final Set<String> MISS_CACHE = new HashSet<>();
    private static final Map<String, List<PreviewImageCallback>> PENDING_CALLBACKS = new HashMap<>();

    private static final String[] IMAGE_SELECTORS = new String[]{
            "meta[property=og:image:secure_url]",
            "meta[property=og:image:url]",
            "meta[property=og:image]",
            "meta[name=twitter:image:src]",
            "meta[name=twitter:image]",
            "meta[itemprop=image]",
            "link[rel=image_src]"
    };

    public static void loadPreviewImageUrl(String pageUrl, PreviewImageCallback callback) {
        String normalizedPageUrl = normalizeHttpUrl(pageUrl);
        if (TextUtils.isEmpty(normalizedPageUrl)) {
            postResult(callback, null);
            return;
        }

        if (isLikelyImageUrl(normalizedPageUrl)) {
            postResult(callback, normalizedPageUrl);
            return;
        }

        synchronized (StoryPreviewImageLoader.class) {
            String cachedImageUrl = IMAGE_CACHE.get(normalizedPageUrl);
            if (!TextUtils.isEmpty(cachedImageUrl)) {
                postResult(callback, cachedImageUrl);
                return;
            }

            if (MISS_CACHE.contains(normalizedPageUrl)) {
                postResult(callback, null);
                return;
            }

            List<PreviewImageCallback> pendingCallbacks = PENDING_CALLBACKS.get(normalizedPageUrl);
            if (pendingCallbacks != null) {
                pendingCallbacks.add(callback);
                return;
            }

            pendingCallbacks = new ArrayList<>();
            pendingCallbacks.add(callback);
            PENDING_CALLBACKS.put(normalizedPageUrl, pendingCallbacks);
        }

        Request request = new Request.Builder()
                .url(normalizedPageUrl)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .get()
                .build();

        NetworkComponent.getOkHttpClientInstance().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                finish(normalizedPageUrl, null);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (Response closeableResponse = response) {
                    if (!closeableResponse.isSuccessful() || closeableResponse.body() == null) {
                        finish(normalizedPageUrl, null);
                        return;
                    }

                    String contentType = closeableResponse.header("Content-Type", "");
                    if (!TextUtils.isEmpty(contentType)
                            && !contentType.toLowerCase(Locale.US).contains("html")) {
                        finish(normalizedPageUrl, null);
                        return;
                    }

                    String responseBody = closeableResponse.body().string();
                    String baseUrl = closeableResponse.request().url().toString();
                    finish(normalizedPageUrl, extractPreviewImageUrl(responseBody, baseUrl));
                } catch (Exception e) {
                    finish(normalizedPageUrl, null);
                }
            }
        });
    }

    private static String extractPreviewImageUrl(String html, String baseUrl) {
        if (TextUtils.isEmpty(html)) {
            return null;
        }

        Document document = Jsoup.parse(html, baseUrl);
        for (String selector : IMAGE_SELECTORS) {
            Element element = document.selectFirst(selector);
            if (element == null) {
                continue;
            }

            String attribute = "link".equals(element.tagName()) ? "href" : "content";
            String imageUrl = makeAbsoluteHttpUrl(element.attr(attribute), baseUrl);
            if (!TextUtils.isEmpty(imageUrl)) {
                return imageUrl;
            }
        }

        return null;
    }

    private static String normalizeHttpUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        HttpUrl parsedUrl = HttpUrl.parse(url);
        if (parsedUrl == null || !isHttpScheme(parsedUrl)) {
            return null;
        }
        return parsedUrl.toString();
    }

    private static String makeAbsoluteHttpUrl(String candidate, String baseUrl) {
        if (TextUtils.isEmpty(candidate) || candidate.trim().startsWith("data:")) {
            return null;
        }

        HttpUrl parsedBase = HttpUrl.parse(baseUrl);
        HttpUrl parsedUrl = parsedBase == null
                ? HttpUrl.parse(candidate.trim())
                : parsedBase.resolve(candidate.trim());

        if (parsedUrl == null || !isHttpScheme(parsedUrl)) {
            return null;
        }

        return parsedUrl.toString();
    }

    private static boolean isHttpScheme(HttpUrl url) {
        return "http".equals(url.scheme()) || "https".equals(url.scheme());
    }

    private static boolean isLikelyImageUrl(String url) {
        HttpUrl parsedUrl = HttpUrl.parse(url);
        if (parsedUrl == null) {
            return false;
        }

        String path = parsedUrl.encodedPath().toLowerCase(Locale.US);
        return path.endsWith(".jpg")
                || path.endsWith(".jpeg")
                || path.endsWith(".png")
                || path.endsWith(".gif")
                || path.endsWith(".webp")
                || path.endsWith(".avif");
    }

    private static void finish(String pageUrl, String imageUrl) {
        List<PreviewImageCallback> callbacks;
        synchronized (StoryPreviewImageLoader.class) {
            callbacks = PENDING_CALLBACKS.remove(pageUrl);
            if (TextUtils.isEmpty(imageUrl)) {
                MISS_CACHE.add(pageUrl);
            } else {
                if (IMAGE_CACHE.size() >= MAX_CACHE_SIZE) {
                    IMAGE_CACHE.clear();
                    MISS_CACHE.clear();
                }
                IMAGE_CACHE.put(pageUrl, imageUrl);
            }
        }

        if (callbacks == null) {
            return;
        }

        MAIN_HANDLER.post(() -> {
            for (PreviewImageCallback callback : callbacks) {
                callback.onPreviewImageUrlLoaded(imageUrl);
            }
        });
    }

    private static void postResult(PreviewImageCallback callback, String imageUrl) {
        MAIN_HANDLER.post(() -> callback.onPreviewImageUrlLoaded(imageUrl));
    }
}

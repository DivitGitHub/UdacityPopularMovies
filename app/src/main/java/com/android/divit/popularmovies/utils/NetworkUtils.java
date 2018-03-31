package com.android.divit.popularmovies.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.android.divit.popularmovies.BuildConfig;
import com.android.divit.popularmovies.data.MoviePreferences;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String API_KEY_PARAMETER = "api_key";
    private static final String API_KEY_VALUE = BuildConfig.API_KEY;

    private static final String BASE_URL = "https://api.themoviedb.org";
    private static final String AUTH_SEGMENT = "3";
    private static final String MOVIE_SEGMENT = "movie";
    public static final String VIDEOS_SEGMENT = "videos";
    public static final String REVIEWS_SEGMENT = "reviews";

    private static final String PAGE_NUMBER_PARAMETER = "page";
    private static final int PAGE_NUMBER_VALUE = 1;
    private static final String LANGUAGE_PARAMETER = "language";
    private static final String LANGUAGE_EN_US = "en-US";

    private NetworkUtils() {
    }

    public static URL buildUrlForMovies(Context context) {

        /*
         **  Example of URL:
         **  https://api.themoviedb.org/3/movie/popular?page=1&language=en-US&api_key=<api_key>
         */
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(AUTH_SEGMENT)
                .appendPath(MOVIE_SEGMENT)
                .appendPath(MoviePreferences.getSortBy(context)) //sortBy values is in strings and arrays xml
                .appendQueryParameter(PAGE_NUMBER_PARAMETER, Integer.toString(PAGE_NUMBER_VALUE))
                .appendQueryParameter(LANGUAGE_PARAMETER, LANGUAGE_EN_US)
                .appendQueryParameter(API_KEY_PARAMETER, API_KEY_VALUE)
                .build();

        return buildUrlFromString(builtUri.toString());
    }

    public static URL buildUrlForVideosAndReviews(long id, String typeSegment) {

        /*
         **  Example of URL:
         **  https://api.themoviedb.org/3/movie/198663/reviews?page=1&language=en-US&api_key=<api_key>
         */
        Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                .appendPath(AUTH_SEGMENT)
                .appendPath(MOVIE_SEGMENT)
                .appendPath(Long.toString(id))
                .appendPath(typeSegment) // str would be either be videos or reviews
                .appendQueryParameter(PAGE_NUMBER_PARAMETER, Integer.toString(PAGE_NUMBER_VALUE))
                .appendQueryParameter(LANGUAGE_PARAMETER, LANGUAGE_EN_US)
                .appendQueryParameter(API_KEY_PARAMETER, API_KEY_VALUE)
                .build();

        return buildUrlFromString(builtUri.toString());
    }


    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = null;
        InputStream in = null;
        Scanner scanner = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(7000);
            urlConnection.setConnectTimeout(10000);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            in = urlConnection.getInputStream();

            scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                Log.v(TAG, "Scanner has no input");
                return null;
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (in != null) {
                in.close();
            }
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private static URL buildUrlFromString(String strUrl) {
        URL url = null;
        try {
            url = new URL(strUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }

    public static boolean isNetworkConnected(Context context) {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    // Example of size: "/w185"
    public static String movieBaseWithSizePath(String size) {
        return ConstantUtils.MOVIE_POSTER_BASE_URL + size;
    }

    // http://img.youtube.com/vi/<id>/0.jpg
    public static String ytPath(String key) {
        return ConstantUtils.YT_IMAGE_BASE_URL + key + ConstantUtils.YT_IMAGE_END_URL;
    }

}

package com.android.divit.popularmovies.utils;

import android.text.TextUtils;
import android.util.Log;

import com.android.divit.popularmovies.model.BaseModel;
import com.android.divit.popularmovies.model.Movie;
import com.android.divit.popularmovies.model.Review;
import com.android.divit.popularmovies.model.Trailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {

    private static final String TAG = JsonUtils.class.getSimpleName();

    //keys for movie json
    private static final String RESULTS_KEY = "results";
    private static final String VOTE_AVERAGE_KEY = "vote_average";
    private static final String TITLE_KEY = "title";
    private static final String POSTER_PATH_KEY = "poster_path";
    private static final String RELEASE_DATE_KEY = "release_date";
    private static final String OVERVIEW_KEY = "overview";
    private static final String ID_KEY = "id";
    private static final String BACKDROP_PATH_KEY = "backdrop_path";

    //keys for trailer json
    private static final String NAME_KEY = "name";
    private static final String YOUTUBE_KEY = "key";

    //keys for review json
    private static final String AUTHOR_KEY = "author";
    private static final String CONTENT_KEY = "content";

    private JsonUtils() {
    }

    public static List<BaseModel> getMovieListDataFromJson(String movieJsonStr) throws JSONException {
        if (TextUtils.isEmpty(movieJsonStr)) {
            Log.v(TAG, "movie JSON string" + " is empty");
            return null;
        }

        List<BaseModel> movieListData = new ArrayList<>();

        JSONObject baseResponse = new JSONObject(movieJsonStr);
        JSONArray resultsArray = baseResponse.getJSONArray(RESULTS_KEY);

        for (int i = 0; i < resultsArray.length(); i++) {
            JSONObject currentJsonResult = resultsArray.getJSONObject(i);
            float voteAverage = (float)currentJsonResult.optDouble(VOTE_AVERAGE_KEY);
            long id = currentJsonResult.optLong(ID_KEY);
            String title = currentJsonResult.optString(TITLE_KEY);
            String posterPath = currentJsonResult.optString(POSTER_PATH_KEY);
            String releaseDate = currentJsonResult.optString(RELEASE_DATE_KEY);
            String plotSynopsis = currentJsonResult.optString(OVERVIEW_KEY);
            String backDropPath = currentJsonResult.optString(BACKDROP_PATH_KEY);

            movieListData.add(new Movie(id, title, releaseDate, posterPath, voteAverage, plotSynopsis, backDropPath));
        }

        return movieListData;
    }

    public static List<BaseModel> getTrailerListDataFromJson(String trailerJsonStr) throws JSONException {
        if (TextUtils.isEmpty(trailerJsonStr)) {
            Log.v(TAG, "trailer JSON string" + " is empty");
            return null;
        }

        List<BaseModel> trailerListData = new ArrayList<>();

        JSONObject baseResponse = new JSONObject(trailerJsonStr);
        JSONArray resultsArray = baseResponse.getJSONArray(RESULTS_KEY);

        for (int i = 0; i < resultsArray.length(); i++) {
            JSONObject currentJsonResult = resultsArray.getJSONObject(i);
            String name = currentJsonResult.optString(NAME_KEY);
            String key = currentJsonResult.optString(YOUTUBE_KEY);

            trailerListData.add(new Trailer(name, key));
        }

        return trailerListData;
    }

    public static List<BaseModel> getReviewListDataFromJson(String reviewJsonStr) throws JSONException {
        if (TextUtils.isEmpty(reviewJsonStr)) {
            Log.v(TAG, "review JSON string" + " is empty");
            return null;
        }

        List<BaseModel> reviewListData = new ArrayList<>();

        JSONObject baseResponse = new JSONObject(reviewJsonStr);
        JSONArray resultsArray = baseResponse.getJSONArray(RESULTS_KEY);

        for (int i = 0; i < resultsArray.length(); i++) {
            JSONObject currentJsonResults = resultsArray.getJSONObject(i);
            String author = currentJsonResults.optString(AUTHOR_KEY);
            String content = currentJsonResults.optString(CONTENT_KEY);

            reviewListData.add(new Review(author, content));
        }

        return reviewListData;
    }


}

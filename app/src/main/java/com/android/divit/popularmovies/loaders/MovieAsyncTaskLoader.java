package com.android.divit.popularmovies.loaders;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.android.divit.popularmovies.MoviesAdapter;
import com.android.divit.popularmovies.data.MovieContract;
import com.android.divit.popularmovies.model.BaseModel;
import com.android.divit.popularmovies.model.Movie;
import com.android.divit.popularmovies.utils.JsonUtils;
import com.android.divit.popularmovies.utils.NetworkUtils;
import com.android.divit.popularmovies.data.MovieContract.MovieEntry;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// Top-level Asynctaskloader class to prevent memory leaks rather than inner class
public class MovieAsyncTaskLoader extends AsyncTaskLoader<List<BaseModel>> {
    List<BaseModel> mMovieData = null;
    int type;
    long id;

    public static final String TAG = MovieAsyncTaskLoader.class.getSimpleName();

    // For movie data
    public MovieAsyncTaskLoader(Context context) {
        super(context);
        type = MoviesAdapter.TYPE_MOVIE;
    }

    // For favourite movie data
    public MovieAsyncTaskLoader(Context context, int type) {
        super(context);
        this.type = MoviesAdapter.TYPE_FAVOURITE;
    }

    // For Trailers and Reviews data
    public MovieAsyncTaskLoader(Context context, int type, long id) {
        super(context);
        this.type = type;
        this.id = id;
    }

    @Override
    protected void onStartLoading() {
        if (mMovieData != null) {
            deliverResult(mMovieData);
            return;
        }
        forceLoad();
    }

    @Override
    public List<BaseModel> loadInBackground() {
        URL movieRequestUrl;
        List<BaseModel> dataList = null;
        String jsonString = "";
        String segment;
        try {
            switch (type) {
                case MoviesAdapter.TYPE_MOVIE:
                    movieRequestUrl = NetworkUtils.buildUrlForMovies(getContext());
                    jsonString = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);
                    dataList = JsonUtils
                            .getMovieListDataFromJson(jsonString);
                    break;
                case MoviesAdapter.TYPE_TRAILER:
                    segment = NetworkUtils.VIDEOS_SEGMENT;
                    movieRequestUrl = NetworkUtils.buildUrlForVideosAndReviews(id, segment);
                    jsonString = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);
                    dataList = JsonUtils
                            .getTrailerListDataFromJson(jsonString);
                    break;
                case MoviesAdapter.TYPE_REVIEW:
                    segment = NetworkUtils.REVIEWS_SEGMENT;
                    movieRequestUrl = NetworkUtils.buildUrlForVideosAndReviews(id, segment);
                    jsonString = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);
                    dataList = JsonUtils
                            .getReviewListDataFromJson(jsonString);
                    break;
                case MoviesAdapter.TYPE_FAVOURITE:
                    Cursor cursor = getContext().getContentResolver().query(
                            MovieContract.MovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);

                    dataList = cursorDataToList(cursor);
                    break;
                default:
                    Log.v(TAG, "type: " + type + "\nJson string: " + jsonString);
                    return null;
            }
            return dataList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<BaseModel> cursorDataToList(Cursor cursor) {
        List<BaseModel> dataList = new ArrayList<>();

        while (cursor.moveToNext()) {
            // Find the columns of inventory that we're interested in
            int movieIDColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID);
            int titleColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_TITLE);
            int releaseDateColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_RELEASE_DATE);
            int posterPathColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_POSTER_PATH);
            int averageVoteColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_AVERAGE_VOTE);
            int plotSynopsisColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_PLOT_SYNOPSIS);
            int backDropPathColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_BACKDROP_PATH);
            // int favouriteColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_FAVOURITE);

            long movieId = cursor.getLong(movieIDColumnIndex);
            String title = cursor.getString(titleColumnIndex);
            String releaseDate = cursor.getString(releaseDateColumnIndex);
            String posterPath = cursor.getString(posterPathColumnIndex);
            float averageVote = cursor.getFloat(averageVoteColumnIndex);
            String plotSynopsis = cursor.getString(plotSynopsisColumnIndex);
            String backDropPath = cursor.getString(backDropPathColumnIndex);

            dataList.add(new Movie(movieId, title, releaseDate, posterPath, averageVote, plotSynopsis, backDropPath, true));
        }
        return dataList;
    }

    public void deliverResult(List<BaseModel> data) {
        mMovieData = data;
        super.deliverResult(data);
    }
}
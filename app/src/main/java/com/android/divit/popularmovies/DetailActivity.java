package com.android.divit.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.divit.popularmovies.data.MovieContract;
import com.android.divit.popularmovies.data.MovieContract.MovieEntry;
import com.android.divit.popularmovies.loaders.MovieAsyncTaskLoader;
import com.android.divit.popularmovies.model.BaseModel;
import com.android.divit.popularmovies.model.Movie;
import com.android.divit.popularmovies.utils.ConstantUtils;
import com.android.divit.popularmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<BaseModel>> {

    private final String LOG_TAG = DetailActivity.class.getSimpleName();

    @BindView(R.id.detail_recyclerview_reviews)
    RecyclerView mReviewRecyclerView;
    @BindView(R.id.detail_recyclerview_trailers)
    RecyclerView mTrailerRecyclerView;
    @BindView(R.id.iv_detail_movie_poster)
    ImageView mIvMoviePoster;
    @BindView(R.id.tv_detail_title)
    TextView mTvTitle;
    @BindView(R.id.rb_detail_average_vote)
    RatingBar mRatingBar;
    @BindView(R.id.tv_detail_average_vote)
    TextView mTvAverageVote;
    @BindView(R.id.tv_detail_release_date)
    TextView mTvReleaseDate;
    @BindView(R.id.tv_detail_plot_synopsis)
    TextView mTvPlotSynopsis;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.iv_detail_backdrop_poster)
    ImageView mIvBackDrop;
    @BindView(R.id.detail_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.detail_trailer_label)
    TextView mTvTrailerLabel;
    @BindView(R.id.line_divider_reviews)
    View mVLineDividerReviews;
    @BindView(R.id.detail_reviews_label)
    TextView mTvReviewLabel;
    @BindView(R.id.line_divider_trailers)
    View mVLineDividerTrailers;
    @BindView(R.id.fab_favourite_button)
    FloatingActionButton mFab;

    Movie mMovie;
    private boolean wasFavouriteInitially;
    MoviesAdapter mMoviesAdapterTrailers;
    MoviesAdapter mMoviesAdapterReviews;

    private static final int TRAILER_LOADER_ID = 10;
    private static final int REVIEW_LOADER_ID = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);

        // Getting movie data from intent
        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra(ConstantUtils.PARCEL_MOVIE_KEY)) {
                mMovie = intentThatStartedThisActivity.getExtras().getParcelable(ConstantUtils.PARCEL_MOVIE_KEY);

                if (isMovieInFavouriteDatabase(mMovie)) {
                    mMovie.setFavourite(true);
                }
                wasFavouriteInitially = mMovie.isFavourite();
            }
        }

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setupFabFavouriteButton();
        displayDetailData();
        getSupportLoaderManager().initLoader(TRAILER_LOADER_ID, null, this);
        getSupportLoaderManager().initLoader(REVIEW_LOADER_ID, null, this);
    }

    private boolean isMovieInFavouriteDatabase(Movie movie) {
        String[] projection = {MovieContract.MovieEntry.COLUMN_MOVIE_ID};
        String selection = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
        String[] selectionArgs = {Long.toString(movie.getId())};
        Cursor cursor = getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI, projection, selection, selectionArgs, null);

        int count = cursor.getCount();
        cursor.close();

        // If movie id exist in database, return true
        return count != 0;
    }

    private void setupFabFavouriteButton() {
        if (mMovie.isFavourite()) {
            setFabColorFavourite();
        } else {
            setFabColorNotFavourite();
        }

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMovie.isFavourite()) {
                    mMovie.setFavourite(false);
                    setFabColorNotFavourite();
                } else {
                    mMovie.setFavourite(true);
                    setFabColorFavourite();
                }
            }
        });
    }

    private void setFabColorNotFavourite() {
        mFab.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorWhite));
    }

    private void setFabColorFavourite() {
        mFab.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.colorYellow));
    }

    @Override
    protected void onPause() {
        super.onPause();
        addOrDeleteToFavourite();
    }

    private void addOrDeleteToFavourite() {
        // Delete
        if (!mMovie.isFavourite() && wasFavouriteInitially) {
            String selection = MovieEntry.COLUMN_MOVIE_ID + "=?";
            String[] selectionArgs = new String[]{Long.toString(mMovie.getId())};
            int rowDeleted = getContentResolver().delete(MovieEntry.CONTENT_URI, selection, selectionArgs);
            if (rowDeleted != 0) {
                Toast.makeText(this, mMovie.getTitle() + " " + getString(R.string.removed_from_favourite),
                        Toast.LENGTH_SHORT).show();
                mMovie.setFavourite(false);
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, R.string.error_occurred,
                        Toast.LENGTH_SHORT).show();
            }
        } else if (mMovie.isFavourite() && !wasFavouriteInitially) {
            // Add to favourite
            ContentValues values = new ContentValues();

            values.put(MovieEntry.COLUMN_MOVIE_ID, mMovie.getId());
            values.put(MovieEntry.COLUMN_MOVIE_TITLE, mMovie.getTitle());
            values.put(MovieEntry.COLUMN_MOVIE_RELEASE_DATE, mMovie.getReleaseDate());
            values.put(MovieEntry.COLUMN_MOVIE_POSTER_PATH, mMovie.getMoviePosterPath());
            values.put(MovieEntry.COLUMN_MOVIE_AVERAGE_VOTE, mMovie.getVoteAverage());
            values.put(MovieEntry.COLUMN_MOVIE_PLOT_SYNOPSIS, mMovie.getPlotSynopsis());
            values.put(MovieEntry.COLUMN_MOVIE_BACKDROP_PATH, mMovie.getBackdropPath());
            values.put(MovieEntry.COLUMN_MOVIE_FAVOURITE, mMovie.isFavourite());

            Uri newUri = getContentResolver().insert(MovieEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                Toast.makeText(this, R.string.failed_to_add_to_favourite,
                        Toast.LENGTH_SHORT).show();
                mMovie.setFavourite(false);
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, R.string.added_to_favourite,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayDetailData() {
        String backDropUrl = NetworkUtils.movieBaseWithSizePath(ConstantUtils.MOVIE_POSTER_SIZE_W500_PATH_SEGMENT)
                + mMovie.getBackdropPath();

        Picasso.with(this)
                .load(backDropUrl)
                .placeholder(android.R.color.transparent)
                .error(android.R.color.transparent)
                .into(mIvBackDrop);

        String posterUrl = NetworkUtils.movieBaseWithSizePath(ConstantUtils.MOVIE_POSTER_SIZE_W342_PATH_SEGMENT)
                + mMovie.getMoviePosterPath();
        Picasso.with(this)
                .load(posterUrl)
                .placeholder(getResources().getDrawable(R.drawable.placeholder))
                .error(getResources().getDrawable(R.drawable.placeholder))
                .into(mIvMoviePoster);

        float voteScore = mMovie.getVoteAverage();
        String voteAverage = Float.toString(voteScore) + "/10";

        mRatingBar.setRating(voteScore / 2f);
        mTvAverageVote.setText(voteAverage);

        mTvTitle.setText(mMovie.getTitle());

        String releaseDate = dateConvert(mMovie.getReleaseDate());
        mTvReleaseDate.setText(releaseDate);

        mTvPlotSynopsis.setText(mMovie.getPlotSynopsis());

        setupCollapsingToolBar();
        setupTrailerRecyclerView();
        setupReviewsRecyclerView();
    }

    private void setupCollapsingToolBar() {
        mCollapsingToolbarLayout.setTitle(mMovie.getTitle());
        mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
    }

    private void setupTrailerRecyclerView() {
        mTrailerRecyclerView.setHasFixedSize(true);
        mTrailerRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mMoviesAdapterTrailers = new MoviesAdapter(this);
        mTrailerRecyclerView.setAdapter(mMoviesAdapterTrailers);
    }

    private void setupReviewsRecyclerView() {
        mReviewRecyclerView.setHasFixedSize(true);
        mReviewRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mMoviesAdapterReviews = new MoviesAdapter(this);
        mReviewRecyclerView.setAdapter(mMoviesAdapterReviews);
    }

    private String dateConvert(String strDate) {
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy");
        Date date;
        try {
            date = s.parse(strDate);
        } catch (ParseException e) {
            Log.v(LOG_TAG, e.toString());
            return strDate;
        }
        return simpleDateFormat.format(date);
    }

    @Override
    public Loader<List<BaseModel>> onCreateLoader(int id, Bundle args) {
        MovieAsyncTaskLoader movieAsyncTaskLoader = null;
        switch (id) {
            case TRAILER_LOADER_ID:
                movieAsyncTaskLoader = new MovieAsyncTaskLoader(this, MoviesAdapter.TYPE_TRAILER, mMovie.getId());
                break;
            case REVIEW_LOADER_ID:
                movieAsyncTaskLoader = new MovieAsyncTaskLoader(this, MoviesAdapter.TYPE_REVIEW, mMovie.getId());
                break;
        }
        return movieAsyncTaskLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<BaseModel>> loader, List<BaseModel> data) {
        switch (loader.getId()) {
            case TRAILER_LOADER_ID:
                if (data == null || data.isEmpty()) {
                    hideTrailerView();
                } else {
                    mMoviesAdapterTrailers.setMovieData(data);
                }
                break;
            case REVIEW_LOADER_ID:
                if (data == null || data.isEmpty()) {
                    hideReviewView();
                } else {
                    mMoviesAdapterReviews.setMovieData(data);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<List<BaseModel>> loader) {
    }

    private void hideTrailerView() {
        mTrailerRecyclerView.setVisibility(View.GONE);
        mTvTrailerLabel.setVisibility(View.GONE);
        mVLineDividerTrailers.setVisibility(View.GONE);
    }

    private void hideReviewView() {
        mReviewRecyclerView.setVisibility(View.GONE);
        mTvReviewLabel.setVisibility(View.GONE);
        mVLineDividerReviews.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }
}

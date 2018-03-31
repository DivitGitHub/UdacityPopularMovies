package com.android.divit.popularmovies;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.divit.popularmovies.data.MovieContract;
import com.android.divit.popularmovies.data.MoviePreferences;
import com.android.divit.popularmovies.loaders.MovieAsyncTaskLoader;
import com.android.divit.popularmovies.model.BaseModel;
import com.android.divit.popularmovies.model.Movie;
import com.android.divit.popularmovies.settings.SettingsActivity;
import com.android.divit.popularmovies.utils.ConstantUtils;
import com.android.divit.popularmovies.utils.GridUtils;
import com.android.divit.popularmovies.utils.NetworkUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieListActivity extends AppCompatActivity implements MoviesAdapter.MoviesAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<List<BaseModel>>,
        SwipeRefreshLayout.OnRefreshListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = MovieListActivity.class.getSimpleName();

    @BindView(R.id.recyclerview_movie)
    RecyclerView mMovieRecyclerView;
    @BindView(R.id.tv_error_message_display)
    TextView mErrorMessageDisplay;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;
    @BindView(R.id.swipe_refresh_layout_main)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private MoviesAdapter mMovieAdapter;
    private static final int MOVIE_LOADER_ID = 0;
    private static final int FAVOURITE_LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_grid_list);
        ButterKnife.bind(this);

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mErrorMessageDisplay.setText(R.string.error_message);

        mMovieAdapter = new MoviesAdapter(this, this);

        setupMovieRecyclerView();
        setupSharedPref();

        if (isSortByFavourite()) {
            getSupportLoaderManager().initLoader(FAVOURITE_LOADER_ID, null, this);
        } else if (getSupportLoaderManager().getLoader(MOVIE_LOADER_ID) == null && !NetworkUtils.isNetworkConnected(this)) {
            showErrorMessage(getResources().getString(R.string.no_connection_message));
        } else {
            getSupportLoaderManager().initLoader(MOVIE_LOADER_ID, null, this);
        }
    }

    private void setupMovieRecyclerView() {
        int numberOfGridColumn = GridUtils.calculateNumberOfColumns(this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, numberOfGridColumn);
        mMovieRecyclerView.setLayoutManager(gridLayoutManager);
        mMovieRecyclerView.setHasFixedSize(true);
        mMovieRecyclerView.setAdapter(mMovieAdapter);
    }

    private void setupSharedPref() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private boolean isSortByFavourite() {
        return MoviePreferences.getSortBy(this).equals(getString(R.string.pref_sort_by_favourite_value));
    }

    @Override
    public android.support.v4.content.Loader<List<BaseModel>> onCreateLoader(int id, Bundle args) {
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }
        switch (id) {
            case MOVIE_LOADER_ID:
                return new MovieAsyncTaskLoader(this);
            case FAVOURITE_LOADER_ID:
                return new MovieAsyncTaskLoader(this, MoviesAdapter.TYPE_FAVOURITE);
            default:
                Log.v(LOG_TAG, "LOADER ID DOES NOT MATCH");
                return null;
        }
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<List<BaseModel>> loader, List<BaseModel> data) {
        updateTitle();
        mSwipeRefreshLayout.setRefreshing(false);
        mLoadingIndicator.setVisibility(View.INVISIBLE);

        switch (loader.getId()) {
            case FAVOURITE_LOADER_ID:
                if (data == null || data.isEmpty()) {
                    showErrorMessage(getResources().getString(R.string.no_favourites_message));
                    return;
                }
                break;
            case MOVIE_LOADER_ID:
                if (data == null || data.isEmpty()) {
                    showErrorMessage(getResources().getString(R.string.error_message));
                    return;
                }
                break;
            default:
                showErrorMessage(getResources().getString(R.string.error_message));
                return;
        }

        mMovieAdapter.setMovieData(data);
        showMovieDataView();
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<List<BaseModel>> loader) {
        if (isSortByFavourite()) {
            mMovieAdapter.setMovieData(null);
        }
    }

    @Override
    public void onClick(Movie movie) {
        Intent intentToStartDetailActivity = new Intent(this, DetailActivity.class);
        intentToStartDetailActivity.putExtra(ConstantUtils.PARCEL_MOVIE_KEY, movie);
        startActivity(intentToStartDetailActivity);
    }

    // When user pulls down to refresh
    @Override
    public void onRefresh() {
        if (isSortByFavourite()) {
            mSwipeRefreshLayout.setRefreshing(false);
            return;
        }

        if (!NetworkUtils.isNetworkConnected(this)) {
            mSwipeRefreshLayout.setRefreshing(false);

            // Check if loader is null (no data), then hide recyclerview and show connection error
            if (this.getSupportLoaderManager().getLoader(MOVIE_LOADER_ID) == null) {
                showErrorMessage(getResources().getString(R.string.no_connection_message));
            } else {
                Toast.makeText(this, getString(R.string.no_connection_message), Toast.LENGTH_SHORT).show();
            }
        } else {
            this.getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
        }
    }

    private void showMovieDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mMovieRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(String message) {
        mErrorMessageDisplay.setText(message);
        mMovieRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_list_key))) {
            if (isSortByFavourite()) {
                getSupportLoaderManager().restartLoader(FAVOURITE_LOADER_ID, null, this);
                return;
            }

            if (!NetworkUtils.isNetworkConnected(this)) {
                Toast.makeText(this, getString(R.string.internet_connection_required), Toast.LENGTH_SHORT).show();
            }
            getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
        }
    }

    private void updateTitle() {
        String title = "";
        String sortBy = MoviePreferences.getSortBy(this);
        if (sortBy.equals(getString(R.string.pref_sort_by_popular_value))) {
            title = getString(R.string.most_popular_movies_name);
        } else if (sortBy.equals(getString(R.string.pref_sort_by_top_rated_value))) {
            title = getString(R.string.top_rated_movies_name);
        } else if (sortBy.equals(getString(R.string.pref_sort_by_favourite_value))) {
            title = getString(R.string.favourite_movies_name);
        }
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.movie_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent intentToStartSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(intentToStartSettingsActivity);
                return true;
            case R.id.action_deleteAll:
                showDeleteConfirmationDialog();
                return true;
            default:
                Toast.makeText(this, getResources().getString(R.string.error_message), Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_entire_list);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteFavouriteList();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteFavouriteList() {
        int rowsDeleted = getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI, null, null);
        Log.v(LOG_TAG, "rows deleted: " + rowsDeleted);

        if (getSupportActionBar().getTitle().equals(getString(R.string.favourite_movies_name))) {
            showErrorMessage(getString(R.string.no_favourites_message));
        }

        if (rowsDeleted > 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.delete_successful), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.unable_to_delete_list_empty, Toast.LENGTH_SHORT).show();
            ;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        // Refresh Data
        if (isSortByFavourite()) {
            getSupportLoaderManager().restartLoader(FAVOURITE_LOADER_ID, null, this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);

    }
}

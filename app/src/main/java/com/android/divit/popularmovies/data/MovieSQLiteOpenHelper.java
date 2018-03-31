package com.android.divit.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.android.divit.popularmovies.data.MovieContract.MovieEntry;

public class MovieSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "movies.db";
    private static final int DATABASE_VERSION = 1;

    public MovieSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_FAVOURITES_TABLE =
                "CREATE TABLE " + MovieEntry.TABLE_NAME + " ("
                        + MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, "
                        + MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, "
                        + MovieEntry.COLUMN_MOVIE_RELEASE_DATE + " TEXT NOT NULL, "
                        + MovieEntry.COLUMN_MOVIE_POSTER_PATH + " TEXT NOT NULL, "
                        + MovieEntry.COLUMN_MOVIE_AVERAGE_VOTE + " INTEGER NOT NULL DEFAULT 0, "
                        + MovieEntry.COLUMN_MOVIE_PLOT_SYNOPSIS + " TEXT NOT NULL, "
                        + MovieEntry.COLUMN_MOVIE_BACKDROP_PATH + " TEXT, "
                        + MovieEntry.COLUMN_MOVIE_FAVOURITE + " INTEGER);";

        Log.v("SQLITE", "CREATE STATEMENT: " + SQL_CREATE_FAVOURITES_TABLE);

        sqLiteDatabase.execSQL(SQL_CREATE_FAVOURITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // FOR TESTING ONLY
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}

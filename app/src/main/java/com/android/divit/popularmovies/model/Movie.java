package com.android.divit.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;


public class Movie extends BaseModel implements Parcelable {
    private long id;
    private String title;
    private String releaseDate;
    private String moviePosterPath;
    private float voteAverage;
    private String plotSynopsis;
    private String backdropPath;
    private boolean isFavourite;

    public Movie(long id, String title, String releaseDate, String moviePosterPath, float voteAverage, String plotSynopsis, String backdropPath) {
        this.id = id;
        this.title = title;
        this.releaseDate = releaseDate;
        this.moviePosterPath = moviePosterPath;
        this.voteAverage = voteAverage;
        this.plotSynopsis = plotSynopsis;
        this.backdropPath = backdropPath;
        isFavourite = false;
    }

    public Movie(long id, String title, String releaseDate, String moviePosterPath, float voteAverage, String plotSynopsis, String backdropPath, boolean isFavourite) {
        this.id = id;
        this.title = title;
        this.releaseDate = releaseDate;
        this.moviePosterPath = moviePosterPath;
        this.voteAverage = voteAverage;
        this.plotSynopsis = plotSynopsis;
        this.backdropPath = backdropPath;
        this.isFavourite = isFavourite;
    }

    private Movie(Parcel in) {
        id = in.readLong();
        title = in.readString();
        releaseDate = in.readString();
        moviePosterPath = in.readString();
        voteAverage = in.readFloat();
        plotSynopsis = in.readString();
        backdropPath = in.readString();
        isFavourite = in.readInt() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(title);
        parcel.writeString(releaseDate);
        parcel.writeString(moviePosterPath);
        parcel.writeFloat(voteAverage);
        parcel.writeString(plotSynopsis);
        parcel.writeString(backdropPath);
        parcel.writeInt(isFavourite ? 1 : 0);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }

    };

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getMoviePosterPath() {
        return moviePosterPath;
    }

    public float getVoteAverage() {
        return voteAverage;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public boolean isFavourite() {
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {
        this.isFavourite = favourite;
    }

}

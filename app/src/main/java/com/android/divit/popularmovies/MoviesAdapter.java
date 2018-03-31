package com.android.divit.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.divit.popularmovies.data.MovieContract;
import com.android.divit.popularmovies.model.BaseModel;
import com.android.divit.popularmovies.model.Movie;
import com.android.divit.popularmovies.model.Review;
import com.android.divit.popularmovies.model.Trailer;
import com.android.divit.popularmovies.utils.ConstantUtils;
import com.android.divit.popularmovies.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MoviesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<BaseModel> mMoviesData;
    private final MoviesAdapterOnClickHandler mClickHandler;

    public final static int TYPE_MOVIE = 0, TYPE_TRAILER = 1, TYPE_REVIEW = 2, TYPE_FAVOURITE = 3;

    public interface MoviesAdapterOnClickHandler {
        void onClick(Movie movie);
    }

    public MoviesAdapter(MoviesAdapterOnClickHandler clickHandler, Context context) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    public MoviesAdapter(Context context) {
        mContext = context;
        mClickHandler = null;
    }

    public class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.iv_movie_poster_item)
        ImageView mMovieImageView;

        public MoviesAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Movie movie = (Movie) mMoviesData.get(adapterPosition);
            if (mClickHandler != null) {
                mClickHandler.onClick(movie);
            }
        }
    }

    public class TrailersAdapterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_trailers_list_item)
        ImageView mTrailerImageView;

        public TrailersAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public class ReviewsAdapterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.review_card_view_item)
        CardView mReviewCardView;
        @BindView(R.id.tv_author_item)
        TextView mReviewAuthorTextView;
        @BindView(R.id.tv_content_item)
        TextView mReviewContentTextView;

        public ReviewsAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mMoviesData.get(position) instanceof Movie) {
            return TYPE_MOVIE;
        } else if (mMoviesData.get(position) instanceof Trailer) {
            return TYPE_TRAILER;
        } else if (mMoviesData.get(position) instanceof Review) {
            return TYPE_REVIEW;
        }
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        int layoutIdForListItem;
        View view;
        RecyclerView.ViewHolder viewHolder = null;
        switch (viewType) {
            case TYPE_MOVIE:
                layoutIdForListItem = R.layout.movies_list_item;
                view = inflater.inflate(layoutIdForListItem, parent, false);
                viewHolder = new MoviesAdapterViewHolder(view);
                break;
            case TYPE_TRAILER:
                layoutIdForListItem = R.layout.trailers_list_item;
                view = inflater.inflate(layoutIdForListItem, parent, false);
                viewHolder = new TrailersAdapterViewHolder(view);
                break;
            case TYPE_REVIEW:
                layoutIdForListItem = R.layout.reviews_list_item;
                view = inflater.inflate(layoutIdForListItem, parent, false);
                viewHolder = new ReviewsAdapterViewHolder(view);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = holder.getItemViewType();

        switch (viewType) {
            case TYPE_MOVIE:
                MoviesAdapterViewHolder movieViewHolder = (MoviesAdapterViewHolder) holder;
                Movie movie = (Movie) mMoviesData.get(position);

                String posterUrl = NetworkUtils.movieBaseWithSizePath(ConstantUtils.MOVIE_POSTER_SIZE_W342_PATH_SEGMENT)
                        + movie.getMoviePosterPath();

                Picasso.with(mContext)
                        .load(posterUrl)
                        .placeholder(mContext.getResources().getDrawable(R.drawable.placeholder))
                        .error(mContext.getResources().getDrawable(R.drawable.placeholder))
                        .into(movieViewHolder.mMovieImageView);
                break;
            case TYPE_TRAILER:
                TrailersAdapterViewHolder trailerViewHolder = (TrailersAdapterViewHolder) holder;
                Trailer trailer = (Trailer) mMoviesData.get(position);

                final String videoKey = trailer.getKey();
                String trailerThumbnailUrl = NetworkUtils.ytPath(videoKey);

                Picasso.with(mContext)
                        .load(trailerThumbnailUrl)
                        .placeholder(android.R.color.transparent)
                        .error(android.R.color.transparent)
                        .into(trailerViewHolder.mTrailerImageView);

                trailerViewHolder.mTrailerImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String trailerUrl = ConstantUtils.YT_BASE_URL + videoKey;
                        Intent startIntentToYoutube = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
                        mContext.startActivity(startIntentToYoutube);
                    }
                });
                break;
            case TYPE_REVIEW:
                final ReviewsAdapterViewHolder reviewsAdapterViewHolder = (ReviewsAdapterViewHolder) holder;
                Review review = (Review) mMoviesData.get(position);

                reviewsAdapterViewHolder.mReviewAuthorTextView.setText(review.getAuthor());
                reviewsAdapterViewHolder.mReviewContentTextView.setText(review.getContent());

                reviewsAdapterViewHolder.mReviewCardView.setOnClickListener(new View.OnClickListener() {

                    // Clicking on the cardview will expand the content text and collapse
                    @Override
                    public void onClick(View view) {
                        if (reviewsAdapterViewHolder.mReviewContentTextView.getLineCount() == 2) {
                            reviewsAdapterViewHolder.mReviewContentTextView.setMaxLines(Integer.MAX_VALUE);
                        } else {
                            reviewsAdapterViewHolder.mReviewContentTextView.setMaxLines(2);
                        }
                    }
                });

                break;
        }
    }

    @Override
    public int getItemCount() {
        return mMoviesData == null ? 0 : mMoviesData.size();
    }

    public void setMovieData(List<BaseModel> moviesData) {
        mMoviesData = moviesData;
        notifyDataSetChanged();
    }
}

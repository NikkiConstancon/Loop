package com.zetta.android.settings;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zetta.android.R;
import com.zetta.android.StatItem;
import com.zetta.android.browse.GraphStatItem;
import com.zetta.android.browse.SimpleStatItem;
import com.zetta.android.browse.StatListAdapter;

import java.util.List;

/**
 * Created by gregoryGreg on 03/10/2017.
 */

public class SettingsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public SettingsListAdapter.MyAdapterListener onClickListener;

    public interface MyAdapterListener {
        void buttonYesOnClick(View v, int position);
        void buttonNoOnClick(View v, int position);
    }

    private static final String TAG = SettingsListAdapter.class.getSimpleName();

    private List<SettingsItem> settings;

    /**
     * Constructor for SettingsListAdapter that accepts a number of items to display and the specification
     * for the ListItemClickListener.
     *
     * @param settings, the cards to display in a list.
     */
    public SettingsListAdapter(List<SettingsItem> settings, SettingsListAdapter.MyAdapterListener listener ) {
        this.settings = settings;
        this.onClickListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return settings.get(position).getType();
    }

    /**
     *
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new NumberViewHolder that holds the View for each list item
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem;
        View view;
        boolean shouldAttachToParentImmediately = false;
        LayoutInflater inflater = LayoutInflater.from(context);
        switch(viewType) {
            case SettingsItem.TYPE_REQUEST:
                layoutIdForListItem = R.layout.settings_item_incoming;

                view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
                return new RequestViewHolder(view);
//            case SettingsItem.TYPE_:
//                layoutIdForListItem = R.layout.stat_item_simple;
//
//                view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
//                return new StatListAdapter.NumberViewHolder(view);
            default:
                throw new IllegalStateException("Attempted to create view holder for a type you haven't coded for: " + viewType);
        }
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the correct
     * indices in the list for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "#" + position);
        switch(holder.getItemViewType()) {
            case SettingsItem.TYPE_REQUEST:
                ((RequestViewHolder) holder).bind((RequestItem) settings.get(position));
                break;
//            case StatItem.TYPE_SIMPLE_STAT:
//                ((StatListAdapter.NumberViewHolder) holder).bind((SimpleStatItem) cards.get(position));
//                break;
            default:
                throw new IllegalStateException("Attempted to bind a type you haven't coded for: " + holder.getItemViewType());
        }
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        return settings.size();
    }


    /**
     * Cache of the children views for a list item.
     */
    class RequestViewHolder extends RecyclerView.ViewHolder {
        @NonNull
        private final TextView request_title;
        private Button yes;
        private Button no;

        /**
         * Constructor for our ViewHolder. Within this constructor, we get a reference to our
         * TextViews and set an onClickListener to listen for clicks. Those will be handled in the
         * onClick method below.
         * @param itemView The View that you inflated in
         *                 {@link StatListAdapter#onCreateViewHolder(ViewGroup, int)}
         */
        public RequestViewHolder(View itemView) {
            super(itemView);

            request_title = (TextView) itemView.findViewById(R.id.request_title);
            yes = (Button) itemView.findViewById(R.id.btn_yes_sub);
            no = (Button) itemView.findViewById(R.id.btn_no_sub);

            yes.setOnClickListener(new View.OnClickListener() {
               @Override
                public void onClick(View v) {
                   onClickListener.buttonYesOnClick(v, getAdapterPosition());
               }
            });

            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.buttonNoOnClick(v, getAdapterPosition());
                }
            });
        }



        /**
         * A method we wrote for convenience. This method will take an integer as input and
         * use that integer to display the appropriate text within a list item.
         * @param item The simplestatitem that needs to be displayed
         */
        void bind(RequestItem item) {
            request_title.setText("" + item.getTitle());
        }
    }

}

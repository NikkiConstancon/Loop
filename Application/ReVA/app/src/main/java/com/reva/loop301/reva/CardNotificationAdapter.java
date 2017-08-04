package com.reva.loop301.reva;

import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Hristian Vitrychenko on 22/07/2017.
 */

/**
 * CardNotificationAdapter class acts as an adapter for
 * displaying notifications as card views.
 */
public class CardNotificationAdapter extends RecyclerView.Adapter<CardNotificationAdapter.CardNotificationViewHolder>  {

    ArrayList<CardNotification> cardNotificationArrayList = new ArrayList<CardNotification>();

    /**
     * Constructor method for the notification card adapter.
     * Instantiates the cardNotificationArrayList variable.
     * @param notifications is an array list of notifications
     */
    public CardNotificationAdapter(ArrayList<CardNotification> notifications)
    {
        cardNotificationArrayList = notifications;
    }

    /**
     * Overridden onCreateViewHolder, adapted to work for
     * notifications.
     * @param parent holds the view group that is the parent of the view holder
     * @param viewType holds the type of view the view holder will use
     * @return the view holder of the notifications
     */
    @Override
    public CardNotificationAdapter.CardNotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_layout, parent, false);
        CardNotificationAdapter.CardNotificationViewHolder notificationViewHolder = new CardNotificationAdapter.CardNotificationViewHolder(view);
        return notificationViewHolder;
    }

    /**
     * Overridden onBindViewHolder, adapted to work for
     * notifications.
     * @param holder is the view holder
     * @param position is the position at which the view holder is at
     */
    @Override
    public void onBindViewHolder(CardNotificationAdapter.CardNotificationViewHolder holder, int position) {

        CardNotification note = cardNotificationArrayList.get(position);
        holder.title.setText(note.getTitle());
        holder.message.setText(note.getMessage());
        holder.colour.setText(note.getColour());
    }

    /**
     * Overridden getItemCount, changed to return
     * cardNotificationArrayList size (basically the number of
     * cardNotifications in the list)
     * @return the size of cardNotificationArrayList
     */
    @Override
    public int getItemCount() {
        return cardNotificationArrayList.size();
    }

    /**
     * CardNotificationViewHolder works as a holder for the
     * notification card views. Extends upon RecyclerView.
     */
    public static class CardNotificationViewHolder extends RecyclerView.ViewHolder{

        TextView title, message, colour;

        /**
         * Constructor for CardNotificationViewHolder,
         * instantiates title, message and colour for notifications
         * @param itemView acts as a passed object containing notification information
         */
        public CardNotificationViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txt_noteTitle);
            message = itemView.findViewById(R.id.txt_noteContent);
            colour = itemView.findViewById(R.id.card_view_notifications);
        }
    }
}

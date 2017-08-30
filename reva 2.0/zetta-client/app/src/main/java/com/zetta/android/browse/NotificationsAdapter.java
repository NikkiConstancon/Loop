package com.zetta.android.browse;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zetta.android.R;

import java.util.List;

import static android.R.color.holo_red_light;
import static android.R.color.tertiary_text_dark;
import static android.graphics.Color.BLACK;
import static android.graphics.Color.MAGENTA;
import static android.graphics.Color.TRANSPARENT;

/**
 * Created by Hristian Vitrychenko on 30/08/2017.
 */

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>
{
    public static class NotificationViewHolder extends RecyclerView.ViewHolder
    {
        CardView cardView;
        TextView notifTitle;
        TextView notifContent;
        Button advice;
        Button close;
        ImageView img;
        int severity;

        NotificationViewHolder(final View itemView)
        {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.card_view_notifications);
            notifTitle = (TextView) itemView.findViewById((R.id.txt_noteTitle));
            notifContent = (TextView) itemView.findViewById(R.id.txt_noteContent);
            img = (ImageView) itemView.findViewById(R.id.notif_image);
            advice = (Button) itemView.findViewById(R.id.btn_getAdvice);
            close = (Button) itemView.findViewById(R.id.btn_closeNotification);

            Drawable col = cardView.getBackground();

            if(col instanceof ColorDrawable)
            {
                severity = ((ColorDrawable) col).getColor();
            }
        }
    }

    List<NotificationsObject> notifs;
    Context myCont;

    NotificationsAdapter(Context context, List<NotificationsObject> notifs)
    {
        myCont = context;
        this.notifs = notifs;
    }

    @Override
    public int getItemCount()
    {
        return notifs.size();
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view_notifications, viewGroup, false);
        return new NotificationViewHolder(v);
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder notifViewHolder, final int position)
    {
        notifViewHolder.notifTitle.setText(notifs.get(position).getNoteTitle());
        notifViewHolder.notifTitle.setTextColor(BLACK);
        notifViewHolder.notifTitle.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        notifViewHolder.notifContent.setText(notifs.get(position).getNoteContent());
        notifViewHolder.img.setImageResource(notifs.get(position).getImageSource());
        notifViewHolder.cardView.setBackgroundColor(notifs.get(position).getSeverity());

        notifViewHolder.advice.setText("Get Advice");

        notifViewHolder.cardView.setCardElevation(4);
        notifViewHolder.cardView.setRadius(20);

        notifViewHolder.close.setText("X");
        notifViewHolder.close.setBackgroundColor(TRANSPARENT);

        notifViewHolder.close.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                notifs.remove(position);

                notifyItemRemoved(position);


                notifyItemRangeChanged(position, notifs.size());

                Toast.makeText(myCont,"Removed notification" ,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }
}














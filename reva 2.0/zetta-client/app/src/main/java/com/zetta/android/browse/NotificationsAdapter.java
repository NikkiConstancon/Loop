package com.zetta.android.browse;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.v4.content.ContextCompat;
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

import java.util.ArrayList;
import java.util.List;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.GREEN;
import static android.graphics.Color.RED;
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
        Button close;
        ImageView img;
        ImageView status;
        int severity;

        NotificationViewHolder(final View itemView)
        {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.card_view_notifications);
            notifTitle = (TextView) itemView.findViewById((R.id.txt_noteTitle));
            notifContent = (TextView) itemView.findViewById(R.id.txt_noteContent);
            img = (ImageView) itemView.findViewById(R.id.notif_image);
            status = (ImageView) itemView.findViewById(R.id.notif_status_image);
            close = (Button) itemView.findViewById(R.id.btn_closeNotification);

            Drawable col = cardView.getBackground();

            if(col instanceof ColorDrawable)
            {
                severity = ((ColorDrawable) col).getColor();
            }
        }
    }

    ArrayList<NotificationsObject> notifs;
    Context myCont;

    NotificationsAdapter(Context context, ArrayList<NotificationsObject> notifs)
    {
        if(notifs != null) {
            myCont = context;
            this.notifs = notifs;
        }
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
        notifViewHolder.notifContent.setText(notifs.get(position).getNoteContent());
        notifViewHolder.notifContent.setTextColor(ContextCompat.getColor(myCont, R.color.colorSecondaryText));
        notifViewHolder.img.setImageResource(notifs.get(position).getImageSource());

        //GradientDrawable shape = new GradientDrawable();
        //shape.setCornerRadius(15);
        //shape.setStroke(2, notifs.get(position).getSeverity());

        //notifViewHolder.cardView.setBackground(shape);

        notifViewHolder.close.setText("X");
        notifViewHolder.close.setTextSize(10);
        notifViewHolder.close.setBackgroundColor(TRANSPARENT);

        if(notifs.get(position).getSeverity() == RED)
        {
            notifViewHolder.status.setBackgroundResource(R.drawable.notif_status_circle);
        }
        else if(notifs.get(position).getSeverity() == GREEN)
        {
            notifViewHolder.status.setBackgroundResource(R.drawable.notif_status_circle_green);
        }
        else
        {
            notifViewHolder.status.setBackgroundResource(R.drawable.notif_status_circle_yellow);
        }


        notifViewHolder.close.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                int pos = position;

                if(position == notifs.size())
                {
                    while(pos >= notifs.size())
                    {
                        pos--;
                    }
                }

                notifs.remove(pos);

                notifyItemRemoved(pos);

                notifyItemRangeChanged(pos, notifs.size());

                Toast.makeText(myCont,"Notification has been removed" ,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }
}














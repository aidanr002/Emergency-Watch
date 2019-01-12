package com.cycloneproductions.aidan.emergencywatch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private Context mContext;
    private ArrayList <EventItem> mEventList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public EventAdapter  (Context context, ArrayList<EventItem> eventList) {
        mContext = context;
        mEventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int i) {
        EventItem currentItem = mEventList.get(i);

        String event = currentItem.getEvent();
        String location = currentItem.getLocation();
        String time = currentItem.getTime();
        String description = currentItem.getDescription();

        holder.mTextViewEvent.setText(event);
        holder.mTextViewLocation.setText(location);
        holder.mTextViewTime.setText(time);

    }

    @Override
    public int getItemCount() {
        return mEventList.size();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextViewEvent;
        public TextView mTextViewLocation;
        public TextView mTextViewTime;
        public TextView mTextViewDescription;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewEvent = itemView.findViewById(R.id.text_view_event);
            mTextViewLocation = itemView.findViewById(R.id.text_view_location);
            mTextViewTime = itemView.findViewById(R.id.text_view_time);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            mListener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}

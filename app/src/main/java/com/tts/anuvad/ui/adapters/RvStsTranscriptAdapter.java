package com.tts.anuvad.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tts.anuvad.R;
import com.tts.anuvad.models.STSTranscript;
import com.tts.anuvad.ui.activities.MainActivity;

import java.util.List;

public class RvStsTranscriptAdapter extends RecyclerView.Adapter<RvStsTranscriptAdapter.ViewHolder> {
    List<STSTranscript> stsTranscriptList ;
    MainActivity activity;

    public RvStsTranscriptAdapter(List<STSTranscript> stsTranscriptList,MainActivity mainActivity) {
        this.stsTranscriptList = stsTranscriptList;
        this.activity = mainActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_element_sts_transcript,parent,false);
        //activity = (MainActivity) parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textViewTo.setText(stsTranscriptList.get(position).getFrom());
        holder.textViewfrom.setText(stsTranscriptList.get(position).getTo());
        holder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.deleteSTS(stsTranscriptList.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return stsTranscriptList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewfrom,textViewTo;
        ImageButton imageButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewfrom = itemView.findViewById(R.id.textViewFromSts);
            textViewTo = itemView.findViewById(R.id.textViewToSts);
            imageButton = itemView.findViewById(R.id.imageButtonDeleteTranscript);
        }
    }
}

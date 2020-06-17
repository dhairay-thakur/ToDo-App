package com.thakursaab.todoapp.views;
import android.content.ClipData;
import android.content.Context;
import android.graphics.Paint;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thakursaab.todoapp.R;
import com.thakursaab.todoapp.database.model.Note;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.MyViewHolder> implements Filterable {

    private OnItemClickListener mListener;

    //to be used in MAIN ACTIVITY to Delete a note
    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    //will be passed to return object of MyViewHolder
    public void setOnItemClickListener(OnItemClickListener listener){
        mListener=listener;
    }
    private Context context;
    private List<Note> notesList;
    private List<Note> notesFiltered;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView note;
        public TextView dot;
        public TextView timestamp;
        public CheckBox checkBox;
        public ImageButton undo;
        public ImageButton delete;
        public LinearLayout LL;

        public MyViewHolder(View view, final OnItemClickListener listener) {
            super(view);

            note = view.findViewById(R.id.note);
            dot = view.findViewById(R.id.dot);
            timestamp = view.findViewById(R.id.timestamp);
            checkBox=view.findViewById(R.id.checkbox);
            undo=view.findViewById(R.id.undo);
            delete=view.findViewById(R.id.delete);
            LL = view.findViewById(R.id.undodel);
            /*
             Since delete button has to be accessed in MAIN ACTIVITY,
             Hence, listener has to be added.
             Could have used ListView to prevent explicitly coding Listener
             But RecyclerViews offer efficient functionality
             */
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listener != null){
                        int position=getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onDeleteClick(position);
                        }
                    }
                }
            });
        }
    }


    public NotesAdapter(Context context, List<Note> notesList) {
        this.context = context;
        this.notesFiltered=notesList;
        this.notesList = notesList;
    }
    /*
    adding next 2 function to prevent malfunctioning
    of checkboxes in recyclerview
    */
    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public int getItemViewType(int position){
        return position;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_list_row, parent, false);

        return new MyViewHolder(itemView,mListener);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Note note = notesFiltered.get(position);
        holder.note.setText(note.getNote());
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(note.isSelected());
        if(holder.checkBox.isChecked()){
            //strikethrough enabled
            holder.note.setPaintFlags(holder.note.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.LL.setVisibility(View.VISIBLE);
            holder.checkBox.setVisibility(View.INVISIBLE);
        }
        else{
            //strikethrough removed
            holder.note.setPaintFlags(holder.note.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.LL.setVisibility(View.INVISIBLE);
            holder.checkBox.setVisibility(View.VISIBLE);
        }
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                note.setSelected(b);
                if(b){
                    holder.note.setPaintFlags(holder.note.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.LL.setVisibility(View.VISIBLE);
                    holder.checkBox.setVisibility(View.INVISIBLE);
                }
            }
        });
        holder.undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //undo button does the same work as uncheck
                holder.note.setPaintFlags(holder.note.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                holder.LL.setVisibility(View.INVISIBLE);
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.checkBox.setChecked(false);
            }
        });
        // Displaying dot from HTML character code
        holder.dot.setText(Html.fromHtml("&#8226;"));
        // Formatting and displaying timestamp
        holder.timestamp.setText(formatDate(note.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return notesFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    notesFiltered=notesList;
                }else{
                    List<Note> filteredList = new ArrayList<>();
                    for(Note row:notesList){
                        if(row.getNote().toLowerCase().contains(charString.toLowerCase())){
                            filteredList.add(row);
                        }
                    }
                    notesFiltered=filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = notesFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                notesFiltered = Collections.unmodifiableList((ArrayList<Note>) filterResults.values);
                notifyDataSetChanged();
            }
        };
    }

    // timestamp format
    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = fmt.parse(dateStr);
            SimpleDateFormat fmtOut = new SimpleDateFormat("MMM d");
            return fmtOut.format(date);
        } catch (ParseException e) {

        }

        return "";
    }
}

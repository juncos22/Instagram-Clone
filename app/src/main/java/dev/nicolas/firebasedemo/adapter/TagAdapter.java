package dev.nicolas.firebasedemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.nicolas.firebasedemo.R;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {
    private Context context;
    private List<String> tags;
    private List<String> postCounts;

    public TagAdapter(Context context, List<String> tags, List<String> postCounts) {
        this.context = context;
        this.tags = tags;
        this.postCounts = postCounts;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(context).inflate(R.layout.tag_item, parent, false);
        return new TagViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        String tag = tags.get(position);
        String postCount = postCounts.get(position);
        holder.tag.setText(String.format("#%s", tag));
        holder.postNumbers.setText(String.format("%s posts", postCount));
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public void filter(List<String> filterTags, List<String> filterTagCounts) {
        this.tags = filterTags;
        this.postCounts = filterTagCounts;
        notifyDataSetChanged();
    }

    static class TagViewHolder extends RecyclerView.ViewHolder {
        TextView tag, postNumbers;
        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            tag = itemView.findViewById(R.id.hashtag_text);
            postNumbers = itemView.findViewById(R.id.posts_number_text);
        }
    }
}

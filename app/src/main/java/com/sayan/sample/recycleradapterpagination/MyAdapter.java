package com.sayan.sample.recycleradapterpagination;

import android.content.Context;
import android.os.Handler;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc41 on 01-06-2017.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<Model> itemList;
    private Context context;

    private static final int VIEW_TYPE_CONTENT = 1;
    private static final int VIEW_TYPE_PROGRESS = 2;
    private boolean isLoading;

    public MyAdapter(List<Model> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) == null ?
                LayoutInflater.from(context) : (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            switch (viewType) {
                case VIEW_TYPE_CONTENT:
                    view = inflater.inflate(R.layout.child_list_layout, parent, false);
                    break;
                case VIEW_TYPE_PROGRESS:
                    view = inflater.inflate(R.layout.pagination_progress_child, parent, false);
                    break;
                default:
                    view = inflater.inflate(R.layout.child_list_layout, parent, false);
                    break;
            }
            return new ViewHolder(view, viewType);
        }
        return new ViewHolder(null, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (getItemViewType(position) != VIEW_TYPE_PROGRESS) {
            Model listItem = itemList.get(position);
            holder.tvTitle.setText(listItem.getTitle());
            holder.tvDescription.setText(listItem.getDescription());
            Picasso.with(context).load(listItem.getImage()).into(holder.imageView);
            //holder.imageView.setImageResource(Integer.parseInt(listItem.getIcon()));
        } else {
            holder.progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (itemList.get(position) == null) {
            return VIEW_TYPE_PROGRESS;
        } else {
            return VIEW_TYPE_CONTENT;
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


    //region load more work
    public void setLoaded(Boolean isLoading) {
        this.isLoading = isLoading;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void addNewDataToItems(final ArrayList<Model> newItems, final int loadingItemPosition) {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final DiffUtil.DiffResult diffResult =
                        DiffUtil.calculateDiff(new DiffCallBack(itemList, newItems));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        applyDiffResult(newItems, diffResult, loadingItemPosition);
                    }
                });

            }
        }).start();
    }

    private void applyDiffResult(ArrayList<Model> newModels, DiffUtil.DiffResult diffResult, int loadingItemPosition) {
        diffResult.dispatchUpdatesTo(this);
        //add new items
        itemList.addAll(itemList.size(), newModels);
        //Remove loading item
        itemList.remove(loadingItemPosition - 1);
    }


    private static class DiffCallBack extends DiffUtil.Callback {

        private final List<Model> models;
        private final ArrayList<Model> newModels;

        public DiffCallBack(List<Model> models, ArrayList<Model> newPosts) {

            this.models = models;
            this.newModels = newPosts;
        }

        @Override
        public int getOldListSize() {
            return models.size();
        }

        @Override
        public int getNewListSize() {
            return newModels.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return false;
        }
    }

    //endregion


    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription;
        ImageView imageView;
        private ProgressBar progressBar;

        ViewHolder(View itemView, int viewType) {
            super(itemView);
            switch (viewType){
                case VIEW_TYPE_CONTENT:
                    initiateViews(itemView);
                    break;
                case VIEW_TYPE_PROGRESS:
                    initializePaginationProgress();
                    break;
                default:
                    initiateViews(itemView);
                    break;
            }
        }

        private void initiateViews(View itemView) {
            tvTitle = (TextView) itemView.findViewById(R.id.tv_header);
            tvDescription = (TextView) itemView.findViewById(R.id.tv_content);
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
        }
        private void initializePaginationProgress() {
            progressBar = itemView.findViewById(R.id.paginationprogress);
        }
    }
}

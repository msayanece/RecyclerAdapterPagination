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
        //inflate and return the viewholder corresponds to the view by a view type
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
        } else {
            //when view type is VIEW_TYPE_PROGRESS, set progress bar to visible
            holder.progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        //return the view type for a item position
        if (itemList.get(position) == null) {
            //if item is null (which was set before intentionally), return VIEW_TYPE_PROGRESS
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
                //use diff result (calculateDiff) for smooth performance of adapter reload
                final DiffUtil.DiffResult diffResult =
                        DiffUtil.calculateDiff(new DiffCallBack(itemList, newItems));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //apply the result of diff util to the adapter
                        applyDiffResult(newItems, diffResult, loadingItemPosition);
                    }
                });

            }
        }).start();
    }

    /**
     * Use this method for applying diff result from calculate diff to the adapter
     * @param newModels the new items to add
     * @param diffResult the diff result from calculate diff method
     * @param loadingItemPosition position to add new items into
     */
    private void applyDiffResult(ArrayList<Model> newModels, DiffUtil.DiffResult diffResult, int loadingItemPosition) {
        //Dispatches the update events to the given adapter
        diffResult.dispatchUpdatesTo(this);
        //add new items
        itemList.addAll(itemList.size(), newModels);
        //Remove loading (with progress bar) item (which was set to null before adding items to adapter)
        itemList.remove(loadingItemPosition - 1);
    }


    /**
     * A Callback class used by DiffUtil while calculating the diff between two lists.
     */
    private static class DiffCallBack extends DiffUtil.Callback {

        private final List<Model> models;
        private final ArrayList<Model> newModels;

        /**
         * constructor for initiating old and new itemlist
         * @param models
         * @param newPosts
         */
        public DiffCallBack(List<Model> models, ArrayList<Model> newPosts) {
            this.models = models;
            this.newModels = newPosts;
        }

        @Override
        public int getOldListSize() {
            //size of old itemlist
            return models.size();
        }

        @Override
        public int getNewListSize() {
            // size of new itemlist
            return newModels.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            //old itemlist and new itemlist are not same, so return false here
            return false;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            //contents of the old and new itemlist are not same, so return false
            return false;
        }
    }

    //endregion


    //viewholder class for recycler view
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

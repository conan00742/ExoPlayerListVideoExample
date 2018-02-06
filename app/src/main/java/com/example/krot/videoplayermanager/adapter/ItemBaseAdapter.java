package com.example.krot.videoplayermanager.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.example.krot.videoplayermanager.model.Item;
import com.example.krot.videoplayermanager.viewholder.ItemBaseViewHolder;

import java.util.List;

/**
 * Created by Krot on 1/29/18.
 */

public abstract class ItemBaseAdapter extends RecyclerView.Adapter<ItemBaseViewHolder> {

    @Nullable
    private List<Item> itemList;

    @Nullable
    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(@Nullable List<Item> itemList) {
        this.itemList = itemList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return (itemList != null ? itemList.size() : 0);
    }

    @Override
    public void onBindViewHolder(ItemBaseViewHolder holder, int position) {
        holder.bindData(getItemAt(position));
    }

    public Item getItemAt(int position) {
        return (itemList != null ? itemList.get(position) : null);
    }


}

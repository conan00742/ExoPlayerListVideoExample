package com.example.krot.videoplayermanager.viewholder;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.krot.videoplayermanager.model.Item;

/**
 * Created by Krot on 1/29/18.
 */

public class ItemBaseViewHolder<T extends Item> extends RecyclerView.ViewHolder {

    @Nullable
    protected T item;

    public ItemBaseViewHolder(ViewGroup parent, int resId) {
        super(LayoutInflater.from(parent.getContext()).inflate(resId, parent, false));
    }

    @Nullable
    public T getItem() {
        return item;
    }

    public void bindData(@Nullable T item) {
        this.item = item;
    }
}

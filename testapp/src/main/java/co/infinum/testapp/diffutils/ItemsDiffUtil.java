package co.infinum.testapp.diffutils;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.util.List;

import co.infinum.testapp.models.Item;

/**
 * Created by Željko Plesac on 02/11/16.
 */
public class ItemsDiffUtil extends DiffUtil.Callback {

    public static final String EXTRA_ITEM_DESCRIPTION = "EXTRA_ITEM_DESCRIPTION";

    private List<Item> oldList;

    private List<Item> newList;

    public ItemsDiffUtil(List<Item> oldList, List<Item> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ITEM_DESCRIPTION, newList.get(newItemPosition).getName());
        return bundle;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getName().equals(newList.get(newItemPosition).getName());
    }
}

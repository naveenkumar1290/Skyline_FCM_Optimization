package planet.info.skyline.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import planet.info.skyline.R;
import planet.info.skyline.model.Company;
import planet.info.skyline.model.Vendor;


import android.widget.Filter;




public class RecyclerBaseAdapter<VH extends RecyclerView.ViewHolder>
        extends BaseAdapter implements Filterable {

    private final RecyclerView.Adapter<VH> mAdapter;

    private List<Company> itemsList;
    private List<Company> itemsFiltered;
    public RecyclerBaseAdapter(RecyclerView.Adapter<VH> adapter, ArrayList<Company> items) {
        mAdapter = adapter;
        this.itemsList = items;
        itemsFiltered = new ArrayList<Company>(items); // this makes the difference.
    }

    @Override
    public int getItemViewType(int position) {
        return mAdapter.getItemViewType(position);
    }

    @Override
    public int getCount() {
        return mAdapter.getItemCount();
    }

    @Override
    public Object getItem(int position) {
        // not supported
        return null;
    }

    @Override
    public long getItemId(int position) {
        return mAdapter.getItemId(position);
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VH holder;
        if (convertView == null) {
            holder = mAdapter.createViewHolder(parent, getItemViewType(position));
            convertView = holder.itemView;
            convertView.setTag(holder);
        } else {
            holder = (VH) convertView.getTag();
        }
        mAdapter.bindViewHolder(holder, position);
        return holder.itemView;
    }

    @Override
    public Filter getFilter() {
        // TODO: return a real filter
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    itemsFiltered = itemsList;
                } else {
                    List<Company> filteredList = new ArrayList<>();
                    for (Company row : itemsList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getEname().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    itemsFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = itemsFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                itemsFiltered = (ArrayList<Company>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
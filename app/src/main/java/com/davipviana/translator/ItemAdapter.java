package com.davipviana.translator;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class ItemAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> items  = new ArrayList<>();
    private int selected = -1;

    public ItemAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.adapter_item, null);
            viewHolder.textView = (TextView) convertView.findViewById(R.id.adapter_text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.textView.setText(items.get(position));
        viewHolder.textView.setTextColor(selected == position ? Color.WHITE : Color.BLACK);
        convertView.setBackgroundColor(ContextCompat.getColor(context,
                selected == position ? R.color.colorAccent : android.R.color.transparent));

        return convertView;
    }

    public void addItem(String item) {
        items.add(item);
        notifyDataSetChanged();
    }

    public void setItems(String[] items) {
        this.items = new ArrayList<>(Arrays.asList(items));
    }

    public void setSelected(int selected) {
        this.selected = selected;
        notifyDataSetChanged();
    }

    public void clear() {
        this.items.clear();
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView textView;
    }
}

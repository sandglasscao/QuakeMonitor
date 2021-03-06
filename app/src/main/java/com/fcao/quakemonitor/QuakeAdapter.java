package com.fcao.quakemonitor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Frank on 10/1/2017.
 */

public class QuakeAdapter extends BaseAdapter {
    private static final DecimalFormat df2 = new DecimalFormat("#.00");
    private static final DecimalFormat df6 = new DecimalFormat("#.000000");
    private static final SimpleDateFormat dateformat = new SimpleDateFormat("MM-dd HH:mm:ss");
    private LayoutInflater mInflater;
    private List<Record> mRecords;

    public QuakeAdapter(Context context, List<Record> records) {
        mInflater = LayoutInflater.from(context);
        mRecords = records;
    }

    @Override
    public int getCount() {
        return mRecords.size();
    }

    @Override
    public Object getItem(int position) {
        return mRecords.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Record record = (Record) this.getItem(position); //because of the list header
        View view = convertView;

        ViewHolder viewHolder;
        if (null == view) {
            /*LayoutInflater inflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(this.layout, parent, false);*/
            view = mInflater.inflate(R.layout.list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.show_map = view.findViewById(R.id.show_map);
            viewHolder.apl_tot = view.findViewById(R.id.apl_tot);
            viewHolder.apl_x = view.findViewById(R.id.apl_x);
            viewHolder.apl_z = view.findViewById(R.id.apl_z);
            viewHolder.record_time = view.findViewById(R.id.record_time);
            viewHolder.longitude = view.findViewById(R.id.longitude);
            viewHolder.latitude = view.findViewById(R.id.latitude);
            viewHolder.speed = view.findViewById(R.id.speed);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        //viewHolder.show_map.setImageResource(R.mipmap.ic_place_color);
        viewHolder.apl_tot.setText(df2.format(record.getDistance()));
        viewHolder.apl_x.setText(df2.format(record.getX()));
        viewHolder.apl_z.setText(df2.format(record.getZ()));
        viewHolder.record_time.setText(dateformat.format(record.getTime()));
        viewHolder.longitude.setText(df6.format(record.getLongitude()));
        viewHolder.latitude.setText(df6.format(record.getLatitude()));
        viewHolder.speed.setText(String.valueOf(record.getSpeed()));
        return view;
    }

    class ViewHolder {
        TextView apl_tot, apl_x, apl_z, record_time, longitude, latitude, speed;
        ImageView show_map;
    }
}

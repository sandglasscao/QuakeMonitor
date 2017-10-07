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
    private Context mContext;
    private int layout;
    private List<Record> mRecords;
    DecimalFormat df2 = new DecimalFormat("#.00");
    DecimalFormat df6 = new DecimalFormat("#.000000");
    SimpleDateFormat dateformat = new SimpleDateFormat("MM-dd HH:mm:ss");

    public QuakeAdapter(Context context, int layout, List<Record> records) {
        mContext = context;
        this.layout = layout;
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
        Record record = (Record) this.getItem(position);
        View view = convertView;

        ViewHolder viewHolder;
        if (null == view) {
            LayoutInflater inflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(this.layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.show_map = view.findViewById(R.id.show_map);
            viewHolder.apl_tot = view.findViewById(R.id.apl_tot);
            viewHolder.apl_x = view.findViewById(R.id.apl_x);
            viewHolder.apl_z = view.findViewById(R.id.apl_z);
            viewHolder.record_time = view.findViewById(R.id.record_time);
            viewHolder.longitude = view.findViewById(R.id.longitude);
            viewHolder.latitude = view.findViewById(R.id.latitude);
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
        return view;
    }

    class ViewHolder {
        TextView apl_tot, apl_x, apl_z, record_time, longitude, latitude;
        ImageView show_map;
    }
}

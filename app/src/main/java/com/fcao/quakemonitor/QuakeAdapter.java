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
    DecimalFormat df4 = new DecimalFormat("#.0000");
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
            viewHolder.record_image = (ImageView) view.findViewById(R.id.record_image);
            viewHolder.apl_tot = (TextView) view.findViewById(R.id.apl_tot);
            viewHolder.apl_x = (TextView) view.findViewById(R.id.apl_x);
            viewHolder.apl_z = (TextView) view.findViewById(R.id.apl_z);
            viewHolder.record_time = (TextView) view.findViewById(R.id.record_time);
            viewHolder.longitude = (TextView) view.findViewById(R.id.longitude);
            viewHolder.latitude = (TextView) view.findViewById(R.id.latitude);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.record_image.setImageResource(R.mipmap.ic_place_color);
        viewHolder.apl_tot.setText(df2.format(record.getDistance()));
        viewHolder.apl_x.setText(df2.format(record.getX()));
        viewHolder.apl_z.setText(df2.format(record.getZ()));
        viewHolder.record_time.setText(dateformat.format(record.getTime()));
        viewHolder.longitude.setText(df4.format(record.getLongitude()));
        viewHolder.latitude.setText(df4.format(record.getLatitude()));
        return view;
    }

    class ViewHolder {
        TextView apl_tot, apl_x, apl_z, record_time, longitude, latitude;
        ImageView record_image;
    }
}

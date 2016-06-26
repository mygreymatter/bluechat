package com.mayo.bluechat;

import android.support.v7.widget.RecyclerView;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by mayo on 13/6/16.
 */
public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder>{

    private ArrayList<Device> devices;
    private Blue blue;
    private Callback mCallback;

    public DeviceAdapter(){

        devices = new ArrayList<>();
        blue = Blue.getInstance();
        prepareData();

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mCallback = (Callback) parent.getContext();
        ViewHolder holder = new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.r_device,parent,false));
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Logger.print("Pos: " + position + " " + devices.get(position));
        holder.name.setText(devices.get(position).name);

        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.connect(devices.get(position).address);
            }
        });

    }

    @Override
    public int getItemCount() {
        Logger.print("Devices Found: " + devices.size());
        return devices.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView name;

        public ViewHolder(View v) {
            super(v);

            name = (TextView) v.findViewById(R.id.device_name);
        }
    }

    private void prepareData(){
        Logger.print("Preparing data: " + Blue.getInstance().devices.size());
        devices.clear();

        Set<String> s = Blue.getInstance().devices.keySet();
        Iterator<String> it = s.iterator();
        String address;
        while(it.hasNext()){
            address = it.next();
            devices.add(new Device(address,Blue.getInstance().devices.get(address)));
        }
    }

    public void notifyDataChanged(){
        prepareData();
        notifyDataSetChanged();
    }
}

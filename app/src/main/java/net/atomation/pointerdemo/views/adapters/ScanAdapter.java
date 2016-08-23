package net.atomation.pointerdemo.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.atomation.pointerdemo.R;
import net.atomation.pointerdemo.views.ConfigurationActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class for the devices RecyclerView in ScanActivity
 * Created by eyal on 14/08/2016.
 */
public class ScanAdapter extends RecyclerView.Adapter<ScanAdapter.ViewHolder> {
    private static final String TAG = ScanAdapter.class.getSimpleName();

    private final List<String> mDevices = new ArrayList<>();

    @Override
    public ScanAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_list_item, parent, false);
        return new ViewHolder(root);
    }

    @Override
    public void onBindViewHolder(ScanAdapter.ViewHolder holder, int position) {
        holder.mAddress.setText(mDevices.get(position));
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    @UiThread
    public void addDevice(String address) {
        boolean isExists = mDevices.contains(address);

        if (!isExists) {
            mDevices.add(address);
        }

        notifyDataSetChanged();
    }

    public void clear() {
        mDevices.clear();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView mAddress;

        public ViewHolder(final View itemView) {
            super(itemView);

            mAddress = (TextView) itemView.findViewById(R.id.device_address);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick() called with: " + "view = [" + view + "]");
                    Context context = itemView.getContext();
                    Intent intent = ConfigurationActivity.createIntent(context, mAddress.getText().toString());
                    context.startActivity(intent);
                }
            });
        }
    }
}

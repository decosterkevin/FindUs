package decoster.findus;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by kevin on 28.02.18.
 */

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.CustomRecyclerViewHolder>{
    private List<Info> mDataset;
    private GoogleMap mMap;
    // Provide a suitable constructor (depends on the kind of dataset)
    public CustomAdapter(List<Info> mDataset, GoogleMap mMap) {
        this.mMap = mMap;
        this.mDataset = mDataset;
    }
    // Create new views (invoked by the layout manager)
    @Override
    public CustomRecyclerViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_recyclerview_layout, parent, false);
        return new CustomRecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomRecyclerViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Info mUser = mDataset.get(position);

        holder.mStatus.setText(mUser.getStatus());
        holder.mUserId.setText(mUser.getUserId());
        holder.mDate.setText(Utilities.getDateToString(mUser.getTimestamp()));
        holder.bind(mDataset.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    public class CustomRecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView mStatus, mUserId, mDate;
        public CustomRecyclerViewHolder(View itemView) {
            super(itemView);
            mStatus = (TextView)itemView.findViewById(R.id.txtStatus);
            mUserId = (TextView)itemView.findViewById(R.id.txtUserid);
            mDate = (TextView)itemView.findViewById(R.id.txtDate);

        }
        public void bind(final Info info) {
            itemView.setOnClickListener(v -> mMap.moveCamera(CameraUpdateFactory.newLatLng(info.getPosition())));

        }
    }

}

package com.yeahdev.yeahstreamer.adapter;

import android.graphics.BitmapFactory;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yeahdev.yeahstreamer.R;
import com.yeahdev.yeahstreamer.interfaces.IItemButtonClicked;
import com.yeahdev.yeahstreamer.models.RadioStation;

import java.util.ArrayList;


public class RadioStationRvAdapter
        extends RecyclerView.Adapter<RadioStationRvAdapter.RadioStationViewHolder> {

    /**
     * Radio Station Item View Holder
     */
    public static class RadioStationViewHolder extends RecyclerView.ViewHolder {
        /**
         * private Member
         */
        private LinearLayout mLlItem;
        private ImageView mIvLogo;
        private TextView mTvName;
        private FloatingActionButton mFabEdit;
        private FloatingActionButton mFabDelete;

        /**
         * Constructor
         * @param itemView - Layout for Radio Station Item
         */
        public RadioStationViewHolder(View itemView) {
            super(itemView);
            this.mLlItem = (LinearLayout) itemView.findViewById(R.id.llRadioStationItem);
            this.mIvLogo = (ImageView) itemView.findViewById(R.id.ivLogo);
            this.mTvName = (TextView) itemView.findViewById(R.id.tvName);
            this.mFabEdit = (FloatingActionButton) itemView.findViewById(R.id.fabEdit);
            this.mFabDelete = (FloatingActionButton) itemView.findViewById(R.id.fabDelete);
        }

        /**
         * public Getter Methods
         */
        public LinearLayout getLlItem() {
            return this.mLlItem;
        }
        public ImageView getIvLogo() {
            return this.mIvLogo;
        }
        public TextView getTvName() {
            return this.mTvName;
        }
        public FloatingActionButton getFabEdit() {
            return this.mFabEdit;
        }
        public FloatingActionButton getFabDelete() {
            return this.mFabDelete;
        }
    }

    /**
     * private Member
     */
    private ArrayList<RadioStation> mRadioStationList;
    private IItemButtonClicked mItemButtonClicked;

    /**
     * Constructor
     * @param radioStationList - Radio Station List
     * @param itemButtonClicked - Item Clicked Listener
     */
    public RadioStationRvAdapter(ArrayList<RadioStation> radioStationList, IItemButtonClicked itemButtonClicked) {
        this.mRadioStationList = radioStationList;
        this.mItemButtonClicked = itemButtonClicked;
    }

    @Override
    public RadioStationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.radio_station_item, parent, false);
        return new RadioStationViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(RadioStationViewHolder holder, int position) {
        // get Radio Station from List
        final RadioStation radioStation = this.mRadioStationList.get(position);
        // set Radio Station to UI
        byte[] imageData = Base64.decode(radioStation.getIcon(), Base64.DEFAULT);
        holder.getIvLogo().setImageBitmap(BitmapFactory.decodeByteArray(imageData , 0, imageData.length));
        holder.getTvName().setText(radioStation.getName());
        // set Button Listener
        holder.getLlItem().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioStationRvAdapter.this.mItemButtonClicked.playRadioStation(radioStation);
            }
        });
        holder.getFabEdit().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioStationRvAdapter.this.mItemButtonClicked.editRadioStation(radioStation);
            }
        });
        holder.getFabDelete().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RadioStationRvAdapter.this.mItemButtonClicked.deleteRadioStation(radioStation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.mRadioStationList.size();
    }
}

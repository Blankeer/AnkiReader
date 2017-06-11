package com.blanke.ankireader.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blanke.ankireader.R;
import com.blanke.ankireader.bean.Deck;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by blanke on 2017/6/11.
 */

public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.ViewHold> {
    private Context context;
    private List<Deck> datas;

    public DeckAdapter(Context context) {
        this.context = context;
        datas = new ArrayList<>();
    }

    public void setDatas(List<Deck> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    @Override
    public ViewHold onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.item_deck, null);
        return new ViewHold(view);
    }

    @Override
    public void onBindViewHolder(ViewHold holder, int position) {
        holder.tv.setText(datas.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public static class ViewHold extends RecyclerView.ViewHolder {
        TextView tv;

        public ViewHold(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.item_deck_name);
        }
    }
}

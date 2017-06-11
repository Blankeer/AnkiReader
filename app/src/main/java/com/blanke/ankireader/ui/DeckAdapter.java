package com.blanke.ankireader.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.dragselectrecyclerview.IDragSelectAdapter;
import com.blanke.ankireader.R;
import com.blanke.ankireader.bean.Deck;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by blanke on 2017/6/11.
 */

public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.ViewHold>
        implements IDragSelectAdapter {

    interface Listener {

        void onClick(int index);

        void onLongClick(int index);

        void onSelectionChanged(int selectedCount);

    }

    private Context context;

    private List<Deck> datas;
    private List<Integer> selectPositios;
    private Listener listener;

    public DeckAdapter(Context context) {
        this.context = context;
        datas = new ArrayList<>();
        selectPositios = new ArrayList<>();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public boolean isSelected() {
        return selectPositios.size() > 0;
    }

    public void clearSelected() {
        selectPositios.clear();
        notifyDataSetChanged();
    }

    public void toggleSelected(int position) {
        if (selectPositios.contains(position)) {
            selectPositios.remove((Integer) position);
        } else {
            selectPositios.add(position);
        }
        notifyItemChanged(position);
        notifySelectedCount();
    }

    public int getSelectedCount() {
        return selectPositios.size();
    }

    public void selectAll() {
        selectPositios.clear();
        for (int i = 0; i < getItemCount(); i++) {
            selectPositios.add(i);
        }
        notifySelectedCount();
        notifyDataSetChanged();
    }

    public void selectDecks(long... deckIds) {
        if (deckIds == null) {
            return;
        }
        for (long deckId : deckIds) {
            for (int i = 0; i < datas.size(); i++) {
                if (deckId == datas.get(i).getId()) {
                    setSelected(i, true);
                    break;
                }
            }
        }
    }

    public long[] getSelectDeckIds() {
        long[] deckIds = new long[selectPositios.size()];
        for (int i = 0; i < selectPositios.size(); i++) {
            deckIds[i] = datas.get(selectPositios.get(i)).getId();
        }
        return deckIds;
    }

    public void setDatas(List<Deck> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    private void notifySelectedCount() {
        if (listener != null) {
            listener.onSelectionChanged(selectPositios.size());
        }
    }

    @Override
    public ViewHold onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.item_deck, null);
        return new ViewHold(view);
    }

    @Override
    public void onBindViewHolder(ViewHold holder, final int position) {
        holder.tv.setText(datas.get(position).getName());
        holder.itemView.setSelected(selectPositios.contains(position));
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onLongClick(position);
                    return true;
                }
                return false;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(position);
                }
            }
        });
    }

    @Override
    public void setSelected(int index, boolean selected) {
        if (selected) {
            selectPositios.add(index);
        } else {
            selectPositios.remove((Integer) index);
        }
        notifyItemChanged(index);
        notifySelectedCount();
    }

    @Override
    public boolean isIndexSelectable(int index) {
        return true;
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

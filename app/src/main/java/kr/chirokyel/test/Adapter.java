package kr.chirokyel.test;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private List<Message> chatList;

    public Adapter(List<Message> chatList) {
        this.chatList = chatList;
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return chatList.get(position).getType();
    }

    @NonNull
    @Override
    public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = -1;
        switch (viewType) {
            case Message.TYPE_LOG:
                layout = R.layout.item_log;
                break;
            case Message.TYPE_MY_MESSAGE:
                layout = R.layout.item_my_message;
                break;
            case Message.TYPE_YOUR_MESSAGE:
                layout = R.layout.item_your_message;
                break;
            case Message.TYPE_TYPING:
                layout = R.layout.item_typing;
        }
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(layout, parent, false);
        return new ViewHolder(view);
    }


    public class ViewHolder extends  RecyclerView.ViewHolder {

        private TextView item_textview_message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            item_textview_message = itemView.findViewById(R.id.item_textview_message);
        }
        public void setMessage(String message) {
            if (null == item_textview_message) return;
            item_textview_message.setText(message);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull Adapter.ViewHolder holder, int position) {
        Message message = chatList.get(position);
        holder.setMessage(message.getMessage());
    }




}

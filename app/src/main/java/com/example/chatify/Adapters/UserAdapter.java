package com.example.chatify.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chatify.MessageActivity;
import com.example.chatify.Model.Users;
import com.example.chatify.R;

import org.w3c.dom.Text;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<Users> mUsers;

    private boolean isChat;

    public UserAdapter(Context context, List<Users> mUsers, boolean isChat) {
        this.context = context;
        this.mUsers = mUsers;
        this.isChat = isChat;


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent,false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Users users = mUsers.get(position);
        holder.username.setText(users.getUsername());

        if(users.getImageURL().equals("default")){
            holder.imageView.setImageResource(R.mipmap.ic_launcher);
        }else{
            Glide.with(context).load(users.getImageURL()).into(holder.imageView);
        }

        //Status Check.
        if(isChat){
            if(users.getStatus().equals("online")){
                holder.imageViewOn.setVisibility(View.VISIBLE);
                holder.imageViewOff.setVisibility(View.GONE);
            }
            else{
                holder.imageViewOn.setVisibility(View.GONE);
                holder.imageViewOff.setVisibility(View.VISIBLE);
            }
        }else{
            holder.imageViewOn.setVisibility(View.GONE);
            holder.imageViewOff.setVisibility(View.GONE);
        }

        //Message Activity on tapping a user.
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, MessageActivity.class);
                i.putExtra("userid", users.getId());
                context.startActivity(i);
            }
        });



    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView username;
        public ImageView imageView;
        public ImageView imageViewOn;
        public ImageView imageViewOff;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.textView30);
            imageView = itemView.findViewById(R.id.imageView);
            imageViewOn = itemView.findViewById(R.id.statusimageOn);
            imageViewOff = itemView.findViewById(R.id.statusimageOff);

        }
    }
}

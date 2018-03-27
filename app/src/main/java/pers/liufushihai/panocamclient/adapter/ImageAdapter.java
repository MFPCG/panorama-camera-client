package pers.liufushihai.panocamclient.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import pers.liufushihai.panocamclient.R;
import pers.liufushihai.panocamclient.bean.ImageBean;

/**
 * Date        : 2018/3/27
 * Author      : liufushihai
 * Description : RecyclerView展示适配器类
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder>{
    private static final String TAG = "ImageAdapter";

    private List<ImageBean> mImgBeanList;
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;

        public ViewHolder(View view){
            super(view);
            imageView = view.findViewById(R.id.my_image_view);
        }
    }

    public ImageAdapter(Context context, List<ImageBean> mImgBeanList) {
        this.mContext = context;
        this.mImgBeanList = mImgBeanList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //return null;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item,parent,false);

        //添加点击事件
        final ViewHolder holder = new ViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                ImageBean imageBean = mImgBeanList.get(pos);
                Toast.makeText(mContext,"Pos: " + pos + "\nUri:"
                        + imageBean.getUri(),Toast.LENGTH_SHORT).show();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ImageBean imageBean =  mImgBeanList.get(position);

        Glide.with(mContext)
                .load(imageBean.getUri())
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return mImgBeanList.size();
    }
}

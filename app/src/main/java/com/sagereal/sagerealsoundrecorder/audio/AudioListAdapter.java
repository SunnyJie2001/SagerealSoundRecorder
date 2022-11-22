package com.sagereal.sagerealsoundrecorder.audio;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.sagereal.sagerealsoundrecorder.R;
import com.sagereal.sagerealsoundrecorder.bean.AudioBean;
import com.sagereal.sagerealsoundrecorder.databinding.ItemAudioBinding;
import com.sagereal.sagerealsoundrecorder.utils.Contants;

import java.util.List;

/**
 * @version 1.0
 * @anthur ssj
 * @date 2022/11/22 21:23
 */
public class AudioListAdapter extends BaseAdapter {
    private Context context;
    private List<AudioBean> mDatas;

    public AudioListAdapter(Context context, List<AudioBean> mDatas) {
        this.context = context;
        this.mDatas = mDatas;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int i) {
        return mDatas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder =null;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.item_audio,viewGroup,false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }
        //获取指定位置的数据对于控件进行设置
        AudioBean audioBean = mDatas.get(i);
        holder.ab.tvTime.setText(audioBean.getTime());
        holder.ab.tvDuration.setText(audioBean.getDuration());
        holder.ab.tvTitle.setText(audioBean.getTitle());
        if (audioBean.isPlaying()) {
            //当前录音正在播放
            holder.ab.lvControll.setVisibility(View.VISIBLE);
            holder.ab.pb.setMax(100);
            holder.ab.pb.setProgress(audioBean.getCurrentProgress());
            holder.ab.ivPlay.setImageResource(R.mipmap.red_pause);
        }else {
            holder.ab.ivPlay.setImageResource(R.mipmap.red_play);
            holder.ab.lvControll.setVisibility(View.GONE);
        }
        //点击播放图标可以播放或者暂停录音内容
        return view;
    }

    class ViewHolder{
        ItemAudioBinding ab;
        public ViewHolder(View v){
            ab = ItemAudioBinding.bind(v);
        }
    }
}

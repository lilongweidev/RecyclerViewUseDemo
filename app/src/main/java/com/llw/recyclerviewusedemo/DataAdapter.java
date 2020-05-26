package com.llw.recyclerviewusedemo;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * 适配器
 */
public class DataAdapter extends BaseQuickAdapter<DataResponse.DataBean, BaseViewHolder> {

    private static final int STATE_DEFAULT = 0;//默认状态
    int mEditMode = STATE_DEFAULT;

    public DataAdapter(int layoutResId, @Nullable List<DataResponse.DataBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, DataResponse.DataBean item) {
        //Glide加载网络图片
        ImageView imageView = helper.getView(R.id.iv_img);
        Glide.with(mContext).load(item.getUrl()).into(imageView);
        //TextView赋值
        helper.setText(R.id.tv_video_info, item.getDesc());

        helper.addOnClickListener(R.id.item_data);//添加item点击事件

        if (mEditMode == STATE_DEFAULT) {
            //默认不显示
            helper.getView(R.id.iv_check).setVisibility(View.GONE);
        } else {
            //显示   显示之后再做点击之后的判断
            helper.getView(R.id.iv_check).setVisibility(View.VISIBLE);

            if (item.isSelect()) {//点击时，true 选中
                helper.getView(R.id.iv_check).setBackgroundResource(R.mipmap.icon_choose_selected);
            } else {//false 取消选中
                helper.getView(R.id.iv_check).setBackgroundResource(R.mipmap.icon_choose_default);
            }
        }
    }

    /**
     * 设置编辑状态   接收Activity中传递的值，并改变Adapter的状态
     */
    public void setEditMode(int editMode) {
        mEditMode = editMode;
        notifyDataSetChanged();//刷新
    }
}

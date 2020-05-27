package com.llw.recyclerviewusedemo;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.llw.recyclerviewusedemo.RecyclerViewAnimation.runLayoutAnimation;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_edit)
    TextView tvEdit;//编辑
    @BindView(R.id.rv_normal_show)
    LinearLayout rvNormalShow;//没有数据时展示的布局
    @BindView(R.id.rv)
    RecyclerView rv;//列表
    @BindView(R.id.refresh)
    SmartRefreshLayout refresh;//刷新布局
    @BindView(R.id.tv_check_all)
    TextView tvCheckAll;//全选
    @BindView(R.id.tv_delete)
    TextView tvDelete;//删除
    @BindView(R.id.lay_bottom)
    LinearLayout layBottom;//底部布局

    private static final int STATE_DEFAULT = 0;//默认状态
    private static final int STATE_EDIT = 1;//编辑状态
    private int mEditMode = STATE_DEFAULT;
    private boolean editorStatus = false;//是否为编辑状态
    private int index = 0;//当前选中的item数

    List<DataResponse.DataBean> mList = new ArrayList<>();//列表
    DataAdapter mAdapter;//适配器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initList();

        //禁用下拉和上拉
        refresh.setEnableRefresh(false);
        refresh.setEnableLoadMore(false);
    }

    //初始化列表数据
    private void initList() {
        mAdapter = new DataAdapter(R.layout.item_data_list, mList);//绑定视图和数据
        rv.setLayoutManager(new LinearLayoutManager(this));//设置线性布局管理器
        rv.setAdapter(mAdapter);//设置适配器

        Gson gson = new Gson();
        DataResponse dataBean = gson.fromJson(JsonData.JSON, DataResponse.class);//解析数据
        List<DataResponse.DataBean> resultsBeans = dataBean.getData();

        if (resultsBeans.size() > 0) {
            mList.clear();
            mList.addAll(resultsBeans);
            mAdapter.notifyDataSetChanged();//刷新数据
            runLayoutAnimation(rv);//动画显示
            rv.setVisibility(View.VISIBLE);
            rvNormalShow.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.GONE);
            rvNormalShow.setVisibility(View.VISIBLE);
        }
        refresh.finishRefresh();//数据加载出来之后，结束下拉动作
    }

    //页面的点击事件
    @OnClick({R.id.tv_edit, R.id.tv_check_all, R.id.tv_delete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_edit://编辑
                updateEditState();
                break;
            case R.id.tv_check_all://全选
                setAllItemChecked();
                break;
            case R.id.tv_delete://删除
                deleteCheckItem();
                break;
        }
    }

    //改变编辑状态
    private void updateEditState() {
        mEditMode = mEditMode == STATE_DEFAULT ? STATE_EDIT : STATE_DEFAULT;
        if (mEditMode == STATE_EDIT) {
            tvEdit.setText("取消");
            layBottom.setVisibility(View.VISIBLE);
            editorStatus = true;
        } else {
            tvEdit.setText("编辑");
            layBottom.setVisibility(View.GONE);
            editorStatus = false;

            setAllItemUnchecked();//取消全选
        }
        mAdapter.setEditMode(mEditMode);

        mAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                if (editorStatus) {//编辑状态
                    DataResponse.DataBean dataBean = mList.get(position);
                    boolean isSelect = dataBean.isSelect();
                    if (!isSelect) {
                        index++;
                        dataBean.setSelect(true);

                    } else {
                        dataBean.setSelect(false);
                        index--;
                    }
                    if (index == 0) {
                        tvDelete.setText("删除");
                    } else {
                        tvDelete.setText("删除(" + index + ")");
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    //全部选中
    private void setAllItemChecked() {
        if (mAdapter == null) return;
        for (int i = 0; i < mList.size(); i++) {
            mList.get(i).setSelect(true);
        }
        mAdapter.notifyDataSetChanged();
        index = mList.size();
        tvDelete.setText("删除(" + index + ")");
    }

    //取消全部选中
    private void setAllItemUnchecked() {
        if (mAdapter == null) return;
        for (int i = 0; i < mList.size(); i++) {
            mList.get(i).setSelect(false);
        }
        mAdapter.notifyDataSetChanged();
        tvDelete.setText("删除");
        index = 0;
    }



    //删除选中的item
    private void deleteCheckItem() {
        if (mAdapter == null) return;

        for (int i = mList.size() - 1; i >= 0; i--) {

            if (mList.get(i).isSelect() == true) {
                mList.remove(i);
            }
        }

        //删除选中的item之后判断是否还有数据，没有则退出编辑模式
        if (mList.size() != 0) {
            index = 0;//删除之后置为0
            tvDelete.setText("删除");
        }else {
            tvEdit.setText("编辑");
            layBottom.setVisibility(View.GONE);
            editorStatus = false;
            //没有数据自然也不存在编辑了
            tvEdit.setVisibility(View.GONE);
            rvNormalShow.setVisibility(View.VISIBLE);
            //启用下拉
            refresh.setEnableRefresh(true);

            //下拉刷新
            refresh.setOnRefreshListener(refreshLayout -> {
                //重新装填数据
                initList();
                index = 0;
                mEditMode = STATE_DEFAULT;//恢复默认状态
                editorStatus = false;//恢复默认状态
                tvDelete.setText("删除");
                tvEdit.setVisibility(View.VISIBLE);//显示编辑
            });
        }

        mAdapter.notifyDataSetChanged();
        runLayoutAnimation(rv);//动画显示
    }
}

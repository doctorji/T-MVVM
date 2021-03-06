package com.code.mvvm.core.view.correct;

import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.StaggeredGridLayoutManager;

import com.code.mvvm.R;
import com.code.mvvm.config.Constants;
import com.code.mvvm.core.data.pojo.common.TypeVo;
import com.code.mvvm.core.data.pojo.correct.WorkDetailVo;
import com.code.mvvm.core.data.pojo.correct.WorkInfoVo;
import com.code.mvvm.core.data.pojo.correct.WorkMergeVo;
import com.code.mvvm.core.data.pojo.course.CourseInfoVo;
import com.code.mvvm.core.data.pojo.live.LiveRecommendVo;
import com.code.mvvm.core.view.common.TypeItemView;
import com.code.mvvm.core.view.correct.holder.CorrectPicHolder;
import com.code.mvvm.core.view.correct.holder.CorrectRemItemHolder;
import com.code.mvvm.core.view.course.holder.CourseItemHolder;
import com.code.mvvm.core.view.home.holder.HomeLiveItemView;
import com.code.mvvm.core.vm.WorkViewModel;
import com.mvvm.base.AbsLifecycleActivity;
import com.mvvm.event.LiveBus;
import com.mvvm.stateview.StateConstants;
import com.trecyclerview.TRecyclerView;
import com.trecyclerview.multitype.Items;
import com.trecyclerview.multitype.MultiTypeAdapter;

import java.lang.ref.WeakReference;


/**
 * @author：tqzhang on 18/7/16 18:06
 */
public class WorkDetailsActivity extends AbsLifecycleActivity<WorkViewModel> {

    protected TRecyclerView mRecyclerView;
    private MultiTypeAdapter adapter;
    protected Items items = new Items();
    private String correctId;

    private WeakReference<WorkDetailsActivity> weakReference;

    @Override
    protected void onStateRefresh() {
        super.onStateRefresh();
        mViewModel.getWorkDetaiMergeData(correctId);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_correct_details;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        super.initViews(savedInstanceState);
        weakReference = new WeakReference<>(this);
        if (getIntent() != null) {
            correctId = getIntent().getStringExtra("correct_id");
        }

        initAdapter();
        initRecyclerView();
        getNetWorkData();
    }

    private void initRecyclerView() {
        mRecyclerView = findViewById(R.id.recycler_view);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(adapter);
    }

    private void initAdapter() {
        adapter = new MultiTypeAdapter.Builder<>()
                .bind(WorkDetailVo.class, new CorrectPicHolder(weakReference.get()))
                .bind(CourseInfoVo.class, new CourseItemHolder(weakReference.get()))
                .bind(LiveRecommendVo.class, new HomeLiveItemView(weakReference.get()))
                .bind(WorkInfoVo.class, new CorrectRemItemHolder(weakReference.get()))
                .bind(TypeVo.class, new TypeItemView(weakReference.get()))
                .build();

    }

    @Override
    protected void dataObserver() {
        LiveBus.getDefault().subscribe(Constants.EVENT_KEY_WORK_STATE).observe(this, observer);

        LiveBus.getDefault().subscribe(Constants.EVENT_KEY_WORK, WorkMergeVo.class).observe(this, new Observer<WorkMergeVo>() {
            @Override
            public void onChanged(@Nullable WorkMergeVo workMergeVo) {
                if (workMergeVo.workDetailVo != null) {
                    items.add(workMergeVo.workDetailVo);
                }

                if (workMergeVo.workRecommentVo != null) {
                    if (workMergeVo.workRecommentVo.data.course.size() > 0) {
                        if (workMergeVo.workRecommentVo.data.live.size() > 0) {
                            items.add(new TypeVo("直播推荐"));
                            items.addAll(workMergeVo.workRecommentVo.data.live);
                        }
                        items.add(new TypeVo("视频课程"));
                        items.addAll(workMergeVo.workRecommentVo.data.course);
                        items.add(new TypeVo("精彩批改"));
                        items.addAll(workMergeVo.workRecommentVo.data.content);
                    }
                }
                adapter.setItems(items);
                mRecyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });

    }


    private void getNetWorkData() {
        mViewModel.getWorkDetaiMergeData(correctId);
    }

    public static void start(Context context, String correctId) {
        Intent starter = new Intent(context, WorkDetailsActivity.class);
        starter.putExtra("correct_id", correctId);
        context.startActivity(starter);
    }
}

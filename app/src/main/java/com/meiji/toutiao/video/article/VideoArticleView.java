package com.meiji.toutiao.video.article;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meiji.toutiao.R;
import com.meiji.toutiao.adapter.video.VideoArticleAdapter;
import com.meiji.toutiao.bean.video.VideoArticleBean;
import com.meiji.toutiao.interfaces.IOnItemClickListener;
import com.meiji.toutiao.view.BasePageFragment;

import java.util.List;

/**
 * Created by Meiji on 2017/3/29.
 */

public class VideoArticleView extends BasePageFragment implements IVideoArticle.View, SwipeRefreshLayout.OnRefreshListener {

    private static final String CATEGORY = "CATEGORY";
    private static final String TAG = "NewsArticleView";
    private RecyclerView recycler_view;
    private SwipeRefreshLayout refresh_layout;
    private VideoArticleAdapter adapter;
    private String categoryId;
    private boolean canLoading = false;
    private IVideoArticle.Presenter presenter;

    public static VideoArticleView newInstance(String categoryId) {
        Bundle bundle = new Bundle();
        bundle.putString(CATEGORY, categoryId);
        VideoArticleView videoArticleView = new VideoArticleView();
        videoArticleView.setArguments(bundle);
        return videoArticleView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            categoryId = bundle.getString(CATEGORY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_base_main, container, false);
        presenter = new VideoArticlePresenter(this);
        initView(view);
//        onRequestData();
        return view;
    }

    private void initView(View view) {
        recycler_view = (RecyclerView) view.findViewById(R.id.recycler_view);
        recycler_view.setHasFixedSize(true);
        recycler_view.setLayoutManager(new LinearLayoutManager(getActivity()));

        refresh_layout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        // 设置下拉刷新的按钮的颜色
        refresh_layout.setColorSchemeResources(R.color.colorPrimary);
        refresh_layout.setOnRefreshListener(this);
    }

    @Override
    public void fetchData() {
        onRequestData();
    }

    @Override
    public void onRefresh() {
        presenter.doRefresh();
    }

    @Override
    public void onRequestData() {
        presenter.doGetUrl(categoryId);
    }

    @Override
    public void onSetAdapter(final List<VideoArticleBean.DataBean> list) {
        if (adapter == null) {
            adapter = new VideoArticleAdapter(getActivity(), list);
            recycler_view.setAdapter(adapter);
            adapter.setOnItemClickListener(new IOnItemClickListener() {
                @Override
                public void onClick(View view, int position) {
                    presenter.doOnClickItem(position);
                }
            });
        } else {
            adapter.notifyItemInserted(list.size());
        }

        canLoading = true;

        recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (!recyclerView.canScrollVertically(1)) {
                        if (canLoading) {
                            presenter.doRefresh();
                            canLoading = false;
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onShowRefreshing() {
        refresh_layout.post(new Runnable() {
            @Override
            public void run() {
                refresh_layout.setRefreshing(true);
            }
        });
    }

    @Override
    public void onHideRefreshing() {
        refresh_layout.post(new Runnable() {
            @Override
            public void run() {
                refresh_layout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onFail() {
        Snackbar.make(refresh_layout, R.string.network_error, Snackbar.LENGTH_SHORT)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        presenter.doGetUrl(categoryId);
                    }
                }).show();
    }
}

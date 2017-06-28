package com.lob.music_share.fragment.content;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.lob.music_share.adapter.recyclerview.UserAdapter;
import com.lob.music_share.json.ParseJson;
import com.lob.music_share.query.Query;
import com.lob.musicshare.R;
import com.pnikosis.materialishprogress.ProgressWheel;

@SuppressLint("ValidFragment")
public class SearchFragment extends Fragment {

    private Query.QueryType queryType;
    private View rootView;
    private RecyclerView recyclerView;
    private ProgressWheel progressWheel;
    private RelativeLayout tipContainer;
    private boolean mustPopulateOnCreateView;
    private IHandleAdapter handleAdapter;

    public SearchFragment() {
    }

    public SearchFragment(Query.QueryType queryType) {
        this.queryType = queryType;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search, container, false);
        if (mustPopulateOnCreateView && handleAdapter != null) {
            handleAdapter.handle();
        }
        return rootView;
    }

    private void findViews() {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setVisibility(View.GONE);

        progressWheel = (ProgressWheel) rootView.findViewById(R.id.progress_wheel);
        progressWheel.setVisibility(View.VISIBLE);

        tipContainer = (RelativeLayout) rootView.findViewById(R.id.tip_container);
        tipContainer.setVisibility(View.GONE);
    }

    public void search(Activity activity, String inputText) {
        if (rootView == null) {
            mustPopulateOnCreateView = true;
            startQuery(activity, inputText);
            return;
        }

        findViews();

        startQuery(getActivity(), inputText);
    }

    private void startQuery(Activity activity, String inputText) {
        Query query = Query.getInstance(activity, queryType)
                .setOnResultListener(new Query.OnResultListener() {
                    @Override
                    public void onResult(final String jsonResult) {
                        if (mustPopulateOnCreateView) {
                            handleAdapter = new IHandleAdapter() {
                                @Override
                                public void handle() {
                                    findViews();

                                    progressWheel.setVisibility(View.GONE);
                                    if (!jsonResult.equals("no_one")) {
                                        recyclerView.setVisibility(View.VISIBLE);
                                        recyclerView.setHasFixedSize(true);
                                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                        recyclerView.setItemViewCacheSize(Integer.MAX_VALUE);
                                        UserAdapter adapter = new UserAdapter(getActivity(),
                                                (ViewGroup) rootView.findViewById(R.id.popup_container),
                                                recyclerView,
                                                ParseJson.generateUsers(jsonResult));
                                        recyclerView.setAdapter(adapter);
                                    }
                                }
                            };
                        } else {
                            progressWheel.setVisibility(View.GONE);
                            if (!jsonResult.equals("no_one")) {
                                recyclerView.setVisibility(View.VISIBLE);
                                recyclerView.setHasFixedSize(true);
                                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                recyclerView.setItemViewCacheSize(Integer.MAX_VALUE);
                                UserAdapter adapter = new UserAdapter(getActivity(),
                                        (ViewGroup) rootView.findViewById(R.id.popup_container),
                                        recyclerView,
                                        ParseJson.generateUsers(jsonResult));
                                recyclerView.setAdapter(adapter);
                            }
                        }
                    }
                });
        if (queryType != null) {
            switch (queryType) {
                case BY_ARTISTS:
                    query.setArtist(inputText.replace(" ", "--"));
                    break;
                case BY_GENRES:
                    query.setGenre(inputText.replace(" ", "--"));
                    break;
                case PEOPLE:
                    query.setPersonToFind(inputText.replace(" ", "--"));
                    break;
            }
            query.startQuery();
        }
    }

    private interface IHandleAdapter {
        void handle();
    }
}

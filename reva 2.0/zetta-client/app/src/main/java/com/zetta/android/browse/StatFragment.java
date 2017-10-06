package com.zetta.android.browse;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.zetta.android.GraphEntry;
import com.zetta.android.MoreGraph;
import com.zetta.android.R;
import com.zetta.android.StatItem;
import com.zetta.android.revawebsocketservice.CloudAwaitObject;
import com.zetta.android.revawebsocketservice.RevaWebsocketEndpoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
import static com.zetta.android.R.layout.more_graph;

/**
 * Created by Hristian Vitrychenko on 11/08/2017.
 * Made to do something 22/08/2017
 */

public class StatFragment extends android.support.v4.app.Fragment
{
    public static final String Tag = "StatFragment";
    private RecyclerView statList;
    private StatListAdapter statListAdapter;
    private List<StatItem> cards = new ArrayList<>();
    private static StatTmpForNikkiEndpoint endpoint = new StatTmpForNikkiEndpoint();
    long start = new java.util.Date().getTime() - 100000;
    long end = new java.util.Date().getTime();
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.stat_hist_fragment, container, false);
        super.onCreate(savedInstanceState);
        final FloatingActionButton myFab = (FloatingActionButton) view.findViewById(R.id.myFAB);
        endpoint.bind(view.getContext());

        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent toAddStat = new Intent(getActivity(), AddSimpleStat.class);
                startActivityForResult(toAddStat, 0);
            }
        });

        //Nikki


//MainActivity-- for stats
        JSONObject obj = new JSONObject();
        try {
            obj.put("Username", "greg");
            obj.put("StartTime", start);
            obj.put("EndTime", end);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        endpoint.attachCloudAwaitObject(
                null,
                statTmpForNikki
        ).send(view.getContext(), "RAW", obj);

        //getFab(StatFragment.this.getContext(), (ViewGroup)view.getParent());
        // MOCK DATA STARTS HERE
        String timeStamp = new SimpleDateFormat("MM.dd HH:mm").format(new java.util.Date());
//        cards.add(new SimpleStatItem("Heart-rate", "", "average", timeStamp, timeStamp, "C", 57.34 ));
//        cards.add(new SimpleStatItem("That thing", "", "min", timeStamp, timeStamp, "C", 27.34 ));
//        cards.add(new SimpleStatItem("That one", "http://i.imgur.com/R9xBixo.png", "max", timeStamp, timeStamp, "C", 67.34 ));

        statList = (RecyclerView) view.findViewById(R.id.stats_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext());
        statList.setLayoutManager(layoutManager);

        statList.setHasFixedSize(true);
        statListAdapter = new StatListAdapter(cards, new StatListAdapter.MyAdapterListener() {
            @Override
            public void moreInfoOnClick(View v, int position) {
                GraphStatItem card = (GraphStatItem) cards.get(position);
                ArrayList<GraphEntry> aray = new ArrayList<GraphEntry>();
                for (int i = 0; i < card.getEntries().size(); i++) {
                    aray.add(card.getEntries().get(i));
                }
                Intent intent = new Intent(getContext(), MoreGraph.class);
                intent.putExtra("title", card.getDeviceName() + " " + card.getStatName());
                intent.putExtra("entries",  aray);
                startActivity(intent);
            }
        });

        statList.setAdapter(statListAdapter);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //endpoint.unbind(getView().getContext());
    }

    //    public FloatingActionButton getFab(Context context, ViewGroup parent) {
//        LayoutInflater inflater = LayoutInflater.from(context);
//        return (FloatingActionButton) inflater.inflate(R.layout.myfab, parent, false);
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

    }

    //MainActivity.StatTmpForNikkiEndpoint statTmpForNikkiEndpoint = new MainActivity.StatTmpForNikkiEndpoint();
    public static class StatTmpForNikkiEndpoint extends RevaWebsocketEndpoint {
        @Override
        public String key() {
            return "Stats";
        }
    }
    public CloudAwaitObject statTmpForNikki = new CloudAwaitObject("GRAPH_POINTS") {
        @Override
        public Object get(final Object obj, Object localMsg, CloudAwaitObject cao) {
            Log.d("object", obj.toString());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Map<String, List<Map<String, Double>>> stats = (Map<String, List<Map<String, Double>>>) obj;
                        List<StatItem> items = new ArrayList<>();
                        for (Map.Entry<String, List<Map<String, Double>>> stat : stats.entrySet()) {
                            Log.d("ENTRYK", stat.getKey());
                            Log.d("ENTRYV", ""+ stat.getValue());
                            LinkedList<GraphEntry> entries = new LinkedList<GraphEntry>();
                            Boolean x = true;
                            for (Map<String, Double> point : stat.getValue()) {

                                //GraphEntry tmp = new GraphEntry((float)point.values().toArray()[0], (float)point.values().toArray()[1]);
                                Iterator<Double> doubleIterator = point.values().iterator();
                                double tmpX = 0.0;
                                double tmpY = 0.0;
                                while (doubleIterator.hasNext()) {
                                    if (x) {
                                        tmpX =  doubleIterator.next();
                                        x = false;
                                    } else {
                                        tmpY =  doubleIterator.next();
                                        x = true;
                                    }
                                }
                                entries.add(new GraphEntry((float)tmpX, (float)tmpY));
                            }
                            String startDate = new SimpleDateFormat("MM.dd HH:mm").format(new java.util.Date(start));
                            String endDate = new SimpleDateFormat("MM.dd HH:mm").format(new java.util.Date(end));
                            items.add(new GraphStatItem(stat.getKey(), "", "line-graph", startDate, endDate, "NA", entries ));
                            statListAdapter.updateList(items);
                        }
                    } catch (ClassCastException e ) {
                        Log.e("BIGD", e.toString());
                    }

                }
            });
            return null;
        }
    };
}


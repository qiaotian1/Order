package com.zm.order.view;

import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Expression;
import com.couchbase.lite.Ordering;
import com.couchbase.lite.Query;
import com.zm.order.R;

import java.util.List;

import application.MyApplication;
import bean.kitchenmanage.order.OrderC;
import bean.kitchenmanage.table.AreaC;
import bean.kitchenmanage.table.TableC;
import model.AreaAdapter;
import model.CDBHelper;
import model.LiveTableRecyclerAdapter;
import untils.MyLog;

public class DeskActivity extends AppCompatActivity {

    private Database db;
    private ListView listViewArea;
    private List<AreaC> areaCList;
    private AreaAdapter areaAdapter;
    private RecyclerView listViewDesk;
    private LiveTableRecyclerAdapter tableadapter;

    private MyApplication myapp;

    private Handler uiHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {

            switch (msg.what) {
                case 1: //语音播放
                    String id = (String)msg.obj;
                    showDeskListView(id);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desk);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        myapp= (MyApplication) getApplicationContext();

        initWidget();
    }
    private void initWidget()
    {
        db = myapp.getDatabase();
        if(db == null) throw new IllegalArgumentException();
        areaAdapter = new AreaAdapter(this, db);

        listViewArea = (ListView)findViewById(R.id.lv_area);
        listViewArea.setAdapter(areaAdapter);
        listViewArea.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {

                areaAdapter.setSelectItem(i);
                final String id = areaAdapter.getItem(i);

                Message msg = Message.obtain();
                msg.obj = id;
                msg.what = 1;
                uiHandler.sendMessage(msg);
            }
        });
        if(areaAdapter.getCount()>0)
        {
            areaAdapter.setSelectItem(0);
            showDeskListView(areaAdapter.getItem(0));
        }

    }

    private void showDeskListView(String areaId)
    {

        long starttime = System.currentTimeMillis();
        if(tableadapter!=null)
            tableadapter.StopQuery();

        tableadapter=new LiveTableRecyclerAdapter(this,db,areaId);
        tableadapter.setOnItemClickListener(new LiveTableRecyclerAdapter.onRecyclerViewItemClickListener()
        {
            @Override
            public void onItemClick(View view,Object data)
            {
                String tableId= (String)data;
                TableC  tableC =  CDBHelper.getObjById(getApplicationContext(),tableId,TableC.class);
                if(tableC.getState()!=2)
                {
                    tableC.setState(2);
                    CDBHelper.createAndUpdate(getApplicationContext(),tableC);

                }
                myapp.setTable_sel_obj(tableC);
                Intent mainIntent = new Intent();
                mainIntent.setClass(DeskActivity.this, MainActivity.class);
                startActivity(mainIntent);
            }
            @Override
            public void onItemLongClick(View view,Object data)
            {
                String tableId = (String)data;
                final TableC tableC=CDBHelper.getObjById(getApplicationContext(),tableId,TableC.class);
                myapp.setTable_sel_obj(tableC);
                if(tableC.getState()==0)//空闲不用弹出消台框
                    return;

                List<OrderC> orderCList= CDBHelper.getObjByWhere(getApplicationContext(),
                        Expression.property("className").equalTo("OrderC")
                                .and(Expression.property("tableNo").equalTo(tableC.getTableNum()))
                                .and(Expression.property("orderState").equalTo(1))
                        ,null
                        ,OrderC.class);

                if(orderCList.size()>0)//有未买单订单，可以买单
                {
                    android.app.AlertDialog.Builder dialog1 = new android.app.AlertDialog.Builder(DeskActivity.this);
                    dialog1.setTitle("是否买单？").setCancelable(false);
                    dialog1.setNegativeButton("是",
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    Intent mainIntent = new Intent();
                                    mainIntent.setClass(DeskActivity.this, PayActivity.class);
                                    startActivity(mainIntent);
                                }
                            }).setPositiveButton("否",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1)
                                {
                                    // TODO Auto-generated method stub
                                }
                            }).show();
                }
                else
                {
                    android.app.AlertDialog.Builder dialog1 = new android.app.AlertDialog.Builder(DeskActivity.this);
                    dialog1.setTitle("是否消台？").setCancelable(false);
                    dialog1.setNegativeButton("是",
                            new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    tableC.setState(0);
                                    CDBHelper.createAndUpdate(getApplicationContext(),tableC);
                                    myapp.setTable_sel_obj(tableC);
                                }
                            }).setPositiveButton("否",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1)
                                {
                                    // TODO Auto-generated method stub
                                }
                            }).show();
                }



            }
        });
        long endTime1 = System.currentTimeMillis();
        MyLog.e("time1="+(endTime1- starttime));
        //3,recyclerview created
        listViewDesk = (RecyclerView)findViewById(R.id.lv_desk);
        listViewDesk.setItemAnimator(new DefaultItemAnimator());
        listViewDesk.setLayoutManager(new GridLayoutManager(this,3));
        listViewDesk.setAdapter(tableadapter);
        long endTime2 = System.currentTimeMillis();
        MyLog.e("time2="+(endTime2- endTime1));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

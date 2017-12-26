package com.zm.order.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.couchbase.lite.Document;
import com.couchbase.lite.Expression;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;
import com.zm.order.R;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import application.ISharedPreferences;
import application.MyApplication;
import bean.kitchenmanage.dishes.DishesC;
import bean.kitchenmanage.dishes.DishesKindC;
import bean.kitchenmanage.dishes.DishesTasteC;
import bean.kitchenmanage.user.UsersC;
import model.CDBHelper;
import model.DBFactory;
import model.DatabaseSource;
import model.IDBManager;
import presenter.ILoginPresenter;
import presenter.LoginPresentImpl;
import untils.MyLog;

/**
 *
 *
 * @author 董海峰
 * @date 2017/10/25
 */
public class LoginActivity extends AppCompatActivity implements ILoginView, ISharedPreferences{
    private EditText name;
    private EditText password;
    private Button submit;
    private CheckBox saveloginstatueChk;
    private  MyApplication myApplication;

    private String userNumber,userPsw;
    private InputMethodManager inputMethodManager;
    private Intent intent;
    private List<UsersC> usersCList;
    private MyApplication myapp;

    private IDBManager idbManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        myapp = (MyApplication) getApplication();
        /*UsersC usersC = new UsersC(myapp.getCompany_ID());
        usersC.setUserName("001");
        usersC.setPasswd("123456");
        CDBHelper.createAndUpdate(getApplicationContext(),usersC);*/
/*
        idbManager = DBFactory.get(DatabaseSource.CouchBase, this);
        List<Document> list = idbManager.getByClassName("qrcodeC");

        Iterator<Document> i = list.iterator();

        while(i.hasNext()){

            Document d = i.next();

            if(d.getString("wxUrl") == null){


                CDBHelper.deleDocument(getApplicationContext(),d);

            }


        }

        List<Document> list1 = idbManager.getByClassName("qrcodeC");
        MyLog.e("删除后长度 "+list1.size());*/


        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        intent = new Intent(this,DeskActivity.class);
        usersCList = CDBHelper.getObjByWhere(getApplicationContext(),
                Expression.property("className")
                        .equalTo("UsersC"),null, UsersC.class);
        Log.e("Login",usersCList.size()+"null");
        for (int i = 0; i<usersCList.size();i++)
        {
            Log.e("Login",usersCList.get(i).getUserName().toString()+"null");
        }
         myApplication = (MyApplication) getApplication();
      //  myApplication.cancleSharePreferences();

          //查看是否有缓存

        userNumber = myApplication.getSharePreferences().getString("name","");
        userPsw = myApplication.getSharePreferences().getString("password","");


          //无缓存
           if("".equals(userNumber)){

                initView();

           }else {

               //有缓存
               initView();
               name.setText(userNumber);
               password.setText(userPsw);
               saveloginstatueChk.setChecked(true);
               closeInputMethod();

               Log.e("login","num="+userNumber+"_psw="+userPsw);

              // this.success();
           }



}

    /**
     *
     */
    private void closeInputMethod()
    {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();
        if (isOpen)
        {

         imm.hideSoftInputFromWindow(password.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void initView() {

        name = (EditText) findViewById(R.id.loginName_edtTxt);

        password = (EditText) findViewById(R.id.loginPassword_edtTxt);

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(editable.length()==6){

                    if(inputMethodManager.isActive()){
                        inputMethodManager.hideSoftInputFromWindow(LoginActivity.this.getCurrentFocus().getWindowToken(), 0);
                    }
                }
            }
        });

        saveloginstatueChk = (CheckBox) findViewById(R.id.saveLoginStatue_chk);

        submit = (Button) findViewById(R.id.login_bt);

        final ILoginPresenter iLoginPresenter = new LoginPresentImpl(this,getApplicationContext());

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(editable.length()==3){

                    password.setFocusable(true);
                    password.setFocusableInTouchMode(true);
                    password.requestFocus();
                }

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                myApplication.mExecutor.submit(new Runnable() {
                    @Override
                    public void run() {

                        iLoginPresenter.doLogin();
                    }
                });


            }
        });
    }

    @Override
    public String[] getLoginInfo() {

        String[] info = new String[2];
        info[0] = name.getText().toString();
        info[1]= password.getText().toString();
        userNumber = info[0];

        return info;
    }

    @Override
    public void showError(String error) {

        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.constraintLayout);

        Snackbar.make(constraintLayout, error, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void success()
    {

        for(UsersC u : usersCList)
        {
            if(u.getUserName().equals(userNumber))
            {
                myApplication.setUsersC(u);
                break;
            }
        }

        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public boolean isSave() {

        return saveloginstatueChk.isChecked();
    }


    @Override
    public SharedPreferences getSharePreferences() {

        return myApplication.getSharePreferences();
    }

    @Override
    public boolean cancleSharePreferences() {

        return myApplication.cancleSharePreferences();
    }
}

package cn.edu.sdust;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import cn.edu.sdust.Service.ConnectionUtil;
import cn.edu.sdust.Service.ServerIP;
import cn.edu.sdust.Service.VersionService;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {						

	ProgressBar progressHorizontal;
	private TextView realname,account,traffic_up,traffic_down,traffic_all,groupname,usedtime,phone,state,jindu;
	private String read_username,read_password;
	private ProgressDialog proDialog;
	private String key = null;
	private Map <String,String> userinfo = new HashMap<String, String>();
	private Map <String,Double> traffic = new HashMap<String, Double>();
	boolean result;
	//刷新按钮Handler
	Handler checkHandler = new Handler() {
		public void handleMessage(Message msg) {	
			result = msg.getData().getBoolean("ischeckError");
			//填充数据
			pushdata();
			if (proDialog != null) {
				proDialog.dismiss();
			}
			if (result == false) {
				Toast.makeText(MainActivity.this, "加载失败，亲 检查您连接！",Toast.LENGTH_SHORT).show();
			}
		}
	};
	//自动更新Handler
	Handler updateHandler = new Handler() {
		public void handleMessage(Message msg) {
            VersionService update = new VersionService(MainActivity.this);
            update.check_manual();
			result = msg.getData().getBoolean("ischeckError");
			if (proDialog != null) {
				proDialog.dismiss();
			}
			if (result == false) {
				Toast.makeText(MainActivity.this, "加载失败，亲 检查您连接！",Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	//程序入口 初始化
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.info_main);
		setProgressBarVisibility(true);
		
	    ////////////////////////////////////////
        ////////////update//////////////////////
	    VersionService update = new VersionService(MainActivity.this);
	    update.check();
        ////////////////////////////////////////
		
        realname = (TextView) this.findViewById(R.id.realname);
        account = (TextView) this.findViewById(R.id.account);
        traffic_up = (TextView) this.findViewById(R.id.traffic_up);
        traffic_down = (TextView) this.findViewById(R.id.traffic_down);
        traffic_all = (TextView) this.findViewById(R.id.traffic_all);
        state = (TextView) this.findViewById(R.id.state);
        phone = (TextView) this.findViewById(R.id.phone);
        usedtime = (TextView) this.findViewById(R.id.usedtime);
        groupname = (TextView) this.findViewById(R.id.groupname);
        jindu = (TextView) this.findViewById(R.id.jindu);
        progressHorizontal = (ProgressBar) findViewById(R.id.myprogress);
   
        SharedPreferences preferences = getSharedPreferences("store", Context.MODE_WORLD_READABLE);
        preferences = getSharedPreferences("store", Context.MODE_WORLD_READABLE);
        read_username = preferences.getString("username", "");
        read_password = preferences.getString("password", "");
        
		Button refresh_button = (Button)findViewById(R.id.button_refresh);
		refresh_button.setOnClickListener(this);
		
		 if(read_username == "" || read_password == ""){
    	    Intent intent = new Intent(MainActivity.this, Setting_binding_accountActivity.class); 
			startActivity(intent); 
		}else{	        
        	if(!ConnectionUtil.isConn(getApplicationContext())){
                ConnectionUtil.setNetworkMethod(MainActivity.this);
            }
	    	else{
		        sendGetForKey(read_username,read_password);
		        pushdata();
	    	}
		}
	}
	
	//填充数据函数
	private void pushdata() {
        if(!key.equals("")){
        	sendGetForInfo(read_username);	        
        	realname.setText("账户名: "+userinfo.get("RealName"));
        	account.setText("账号: "+userinfo.get("Account"));
        	traffic_up.setText("上行流量:"+(traffic.get("UsedFlow")-traffic.get("DownFlow"))+"MB");
        	traffic_down.setText("下行流量:"+traffic.get("DownFlow")+"MB");
        	traffic_all.setText("已用流量:"+traffic.get("UsedFlow")+"MB");
        	groupname.setText(userinfo.get("GroupName"));
        	usedtime.setText(userinfo.get("Balance")+"元");

        	if(userinfo.get("OnLineIP").toString().equals("Null")){
        		phone.setText("0.0.0.0");
        	}else{
        		phone.setText(userinfo.get("OnLineIP"));		        		
        	}
        	if(userinfo.get("OnLineIP").equals("Null")){
        		state.setText("账号离线");
        	}else{
        		state.setText("正常在线");		        		
        	}
        	
        	int progress = (int)((traffic.get("UsedFlow")/traffic.get("Sum_Flow"))*100);
        	progressHorizontal.setProgress(progress);
        	jindu.setText("本月流量("+progress+"%)");
        	
        }
		
	}
	
	//获取令牌函数
	private void sendGetForKey(String username,String password)
	{
		HttpClient client = new DefaultHttpClient();
		StringBuilder builder = new StringBuilder();
		HttpGet myget = new HttpGet(ServerIP.GetIP()+"getkey.php?account="+username+"&password="+password);
		try {
			HttpResponse response = client.execute(myget);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
			response.getEntity().getContent()));
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				builder.append(s);
			}
			JSONObject jsonObject = new JSONObject(builder.toString());       	                 
                key = jsonObject.getString("GetKey");			
		} catch (Exception e) {
			Log.v("url response", "false");
		}
	}
	
	//获取数据函数（Json）
	private void sendGetForInfo(String username)
	{
		sendGetForKey(read_username,read_password);
		HttpClient client = new DefaultHttpClient();
		StringBuilder builder = new StringBuilder();
		HttpGet myget = new HttpGet(ServerIP.GetIP()+"userinfo.php?key="+key+"&account="+username+"&version="+ConnectionUtil.getCurrentVersion(MainActivity.this));
		try {
			HttpResponse response = client.execute(myget);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
			response.getEntity().getContent()));
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				builder.append(s);
			}
			JSONObject jsonObject = new JSONObject(builder.toString());  
                userinfo.put("Account",jsonObject.getString("Account"));	
                userinfo.put("RealName",jsonObject.getString("RealName"));
                traffic.put("Sum_Flow",jsonObject.getDouble("Sum_Flow"));
                traffic.put("UsedFlow",jsonObject.getDouble("UsedFlow"));//xiugai
                traffic.put("DownFlow",jsonObject.getDouble("DownFlow"));
                userinfo.put("Online",jsonObject.getString("Online"));//e
                userinfo.put("GroupName",jsonObject.getString("GroupName"));
                userinfo.put("OnLineIP",jsonObject.getString("OnLineIP")); //ip
                userinfo.put("Balance",jsonObject.getString("Balance"));//yue
		} catch (Exception e) {
			Log.v("url response", "false");
		}
	}
	
	//重写菜单按钮
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST + 2, 2, "更新").setIcon(
        android.R.drawable.ic_menu_edit);
        menu.add(Menu.NONE, Menu.FIRST + 1, 1, "绑定").setIcon(
        android.R.drawable.ic_menu_add);
        menu.add(Menu.NONE, Menu.FIRST + 3, 3, "关于").setIcon(
         android.R.drawable.ic_menu_help);
        menu.add(Menu.NONE, Menu.FIRST + 4, 4, "退出").setIcon(
        android.R.drawable.ic_menu_send);
        return true;
    }
		
	//自定义菜单项
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) 
		{        
	       case Menu.FIRST + 1:
	    	    intent = new Intent(MainActivity.this, Setting_binding_accountActivity.class); 
				startActivity(intent); 
	            break;
	        case Menu.FIRST + 2:
            	proDialog = ProgressDialog.show(MainActivity.this,null, "请稍后...");
    			Thread UpdateThread = new Thread(new UpdateHandler());
    			UpdateThread.start();	
	            break;
		    case Menu.FIRST + 3:
		    	    intent = new Intent(MainActivity.this, AboutusActivity.class); 
					startActivity(intent); 
		            break;
	        case Menu.FIRST + 4:
               	AlertDialog dialog = new AlertDialog.Builder(this)  
                .setTitle("退出程序")  
                .setMessage("您确定退出程序？")  
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {  
                      
                    @Override  
                    public void onClick(DialogInterface dialog, int which) {  
    	                finish();  
    	                System.exit(0); 
                    }
                })  
                .setNegativeButton("否", new DialogInterface.OnClickListener() {  
                      
                    @Override  
                    public void onClick(DialogInterface dialog, int which) {  
                        dialog.cancel();  
                    }  
                })  
                .show();
		} 
		return false;  
	}
	
	//点击返回 退出程序
	public boolean onKeyDown(int keyCode, KeyEvent event) {  
        // TODO Auto-generated method stub  
        if(keyCode == KeyEvent.KEYCODE_BACK){  
        	AlertDialog dialog = new AlertDialog.Builder(this)  
            .setTitle("退出程序")  
            .setMessage("您确定退出程序？")  
            .setPositiveButton("确定", new DialogInterface.OnClickListener() {  
                  
                @Override  
                public void onClick(DialogInterface dialog, int which) {  
	                finish();  
	                System.exit(0); 
                }
            })  
            .setNegativeButton("否", new DialogInterface.OnClickListener() {  
                  
                @Override  
                public void onClick(DialogInterface dialog, int which) {  
                    dialog.cancel();  
                }  
            })  
            .show();  
 
        }                          
        return false;  
	}

	//刷新Handler
	class LoaddataHandler implements Runnable {
		@Override
		public void run() {
			sendGetForKey(read_username,read_password);
        	result = true;
			Message message = new Message();
			Bundle bundle = new Bundle();
			bundle.putBoolean("ischeckError", result);
			message.setData(bundle);
			checkHandler.sendMessage(message);	
		}
	}
	
	//程序更新Handler
	class UpdateHandler implements Runnable {
		@Override
		public void run() {		
        	result = true;
			Message message = new Message();
			Bundle bundle = new Bundle();
			bundle.putBoolean("ischeckError", result);
			message.setData(bundle);
			updateHandler.sendMessage(message);	
		}
	}
	
	//刷新按钮 单击监听
	@Override
	public void onClick(View v) {
		switch (v.getId()){		
		case R.id.button_refresh:
			
        	if(!ConnectionUtil.isConn(getApplicationContext())){
                ConnectionUtil.setNetworkMethod(MainActivity.this);
	    	}else{
	    		
	    		
	            SharedPreferences preferences = getSharedPreferences("store", Context.MODE_WORLD_READABLE);
	            preferences = getSharedPreferences("store", Context.MODE_WORLD_READABLE);
	            read_username = preferences.getString("username", "");
	            read_password = preferences.getString("password", "");
	            
		   		if(read_username == "" || read_password == ""){
		     	    Intent intent = new Intent(MainActivity.this, Setting_binding_accountActivity.class); 
		 			startActivity(intent); 
		 		}else{	
	            	proDialog = ProgressDialog.show(MainActivity.this, "加载中...", "数据加载中...");
	    			Thread loginThread = new Thread(new LoaddataHandler());
	    			loginThread.start();	
		 		}
	    	}
		}   	
		
	} 
	
	//返回到该界面时进行刷新
	/*
    @Override
    public void  onResume(){
    	super.onResume(); 
    	if(!ConnectionUtil.isConn(getApplicationContext())){
            ConnectionUtil.setNetworkMethod(MainActivity.this);
    	}else{  		
            SharedPreferences preferences = getSharedPreferences("store", Context.MODE_WORLD_READABLE);
            preferences = getSharedPreferences("store", Context.MODE_WORLD_READABLE);
            read_username = preferences.getString("username", "");
            read_password = preferences.getString("password", "");
            
	   		if(read_username == "" || read_password == ""){
	     	    Intent intent = new Intent(MainActivity.this, Setting_binding_accountActivity.class); 
	 			startActivity(intent); 
	 		}else{	
            	proDialog = ProgressDialog.show(MainActivity.this, "加载中...", "数据加载中...");
    			Thread loginThread = new Thread(new LoaddataHandler());
    			loginThread.start();	
	 		}
    	}
    }
   */
}

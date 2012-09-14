package cn.edu.sdust;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import cn.edu.sdust.Service.ConnectionUtil;
import cn.edu.sdust.Service.ServerIP;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Setting_binding_accountActivity extends Activity implements OnClickListener {
	private EditText username;
	private EditText password;
	private String r_username;
	private String r_password;
	private String flag = "";
	boolean result;	
	boolean netcheck;
	private ProgressDialog proDialog;
	Handler checkHandler = new Handler() {
		public void handleMessage(Message msg) {
			result = msg.getData().getBoolean("ischeckError");
			if (proDialog != null) {
				proDialog.dismiss();
			}
			if (result == false) {
				Toast.makeText(Setting_binding_accountActivity.this, "验证失败，亲 检查您的用户名和密码！",Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(Setting_binding_accountActivity.this, "绑定成功!",Toast.LENGTH_LONG).show();
				finish();
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.layout_binding_account);
	    
        SharedPreferences preferences = getSharedPreferences("store", Context.MODE_WORLD_READABLE);
	    
        Button Button=(Button)findViewById(R.id.button_binding);
        Button.setOnClickListener(this);
        
        TextView textView_url_reg = (TextView) findViewById(R.id.url_reg);  
        String source = "<b><font color=#ff0000>注意 查询密码不是拨号密码 " 
        	+ "</font><a href='http://219.218.128.137:803/public/reg.aspx'></a><br>请到http://10.200.255.0:8080/public/reg.aspx设置查询密码<b>"; 
        	textView_url_reg.setText(Html.fromHtml(source));  
        	//textView_url_reg.setMovementMethod(LinkMovementMethod.getInstance()); 
        
        
        username =(EditText)findViewById(R.id.binding_username);        
        password = (EditText)findViewById(R.id.binding_password);
        
        preferences = getSharedPreferences("store", Context.MODE_WORLD_READABLE);
        String read_username = preferences.getString("username", "");
        String read_password = preferences.getString("password", "");
        if(read_username != ""){
        	username.setText(read_username);
        }
        if(read_password != ""){
        	password.setText(read_password);
        }
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		r_username =  username.getText().toString();
		r_password =  password.getText().toString();
		if(r_username.equals("") || r_password.equals("")){
			Toast.makeText(Setting_binding_accountActivity.this, "您填写的数据不能为空!",Toast.LENGTH_LONG).show();
		}else{
        	if(!ConnectionUtil.isConn(getApplicationContext())){
                ConnectionUtil.setNetworkMethod(Setting_binding_accountActivity.this);
        	}else{
				proDialog = ProgressDialog.show(Setting_binding_accountActivity.this, "验证中..","亲，验证中..请稍后....", true, true);
				Thread loginThread = new Thread(new checkFailureHandler());
				loginThread.start();	
        	}
		}
	}
	
	class checkFailureHandler implements Runnable {
		@Override
		public void run() {
			flag = sendPostFor(r_username,r_password);	
			if(!flag.equals("0") && !flag.equals("-1") && !flag.equals("-2") && !flag.equals("-3") && !flag.equals("")){
				SharedPreferences preferences = getSharedPreferences("store", Context.MODE_WORLD_READABLE+ Context.MODE_WORLD_WRITEABLE);
				Editor editor = preferences.edit();
				editor.putString("username", r_username);
				editor.putString("password", r_password);
				editor.commit();
				result = true; 
			}else{
				result = false; 
			}
			Message message = new Message();
			Bundle bundle = new Bundle();
			bundle.putBoolean("ischeckError", result);
			message.setData(bundle);
			checkHandler.sendMessage(message);
		}
	}
	
	private String sendPostFor(String username,String password)
	{
		String res = null;
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
                res = jsonObject.getString("GetKey");			
		} catch (Exception e) {
			Log.v("url response", "false");
		}
		return res;		
	}
	

}

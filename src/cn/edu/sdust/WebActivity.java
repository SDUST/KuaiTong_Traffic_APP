package cn.edu.sdust;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.widget.Toast;

public class WebActivity extends Activity {
	   private WebView webview; 
	   private ProgressDialog proDialog;
	   boolean result;
		Handler checkHandler = new Handler() {
			public void handleMessage(Message msg) {	
				result = msg.getData().getBoolean("ischeckError");
				if (proDialog != null) {
					proDialog.dismiss();
				}
				if (result == false) {
					Toast.makeText(WebActivity.this, "加载失败，亲 检查您连接！",Toast.LENGTH_SHORT).show();
				}
			}
		};
	   
	   public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState); 
	        webview = new WebView(this); 
	        webview.getSettings().setJavaScriptEnabled(true); 
	        
        	//proDialog = ProgressDialog.show(WebActivity.this, "加载中...", "数据加载中...");
			//Thread loginThread = new Thread(new LoaddataHandler());
			//loginThread.start();
	        webview.loadUrl("http://www.sdkd.net.cn/index__A626AC3F4032BDF32CAA5828298FEABE.htm"); 
	        setContentView(webview);
	        //proDialog.dismiss();
	    } 
	   
		class LoaddataHandler implements Runnable {
			@Override
			public void run() {
	        	result = true;
				Message message = new Message();
				Bundle bundle = new Bundle();
				bundle.putBoolean("ischeckError", result);
				message.setData(bundle);
				checkHandler.sendMessage(message);	
			}
		}
	   
		//退出
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

}

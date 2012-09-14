package cn.edu.sdust;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class WelcomeActivity extends Activity{
	
	public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,WindowManager.LayoutParams. FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_welcome);
        Start();
    }
    
    public void Start() {
                new Thread() {
                        public void run() {
                                try {
                                        Thread.sleep(500);
                                } catch (InterruptedException e) {
                                        e.printStackTrace();
                                }
                                Intent intent = new Intent();
                                intent.setClass(WelcomeActivity.this, MainTabActivity.class);
                                startActivity(intent);
                                finish();
                        }
                }.start();
        }
}

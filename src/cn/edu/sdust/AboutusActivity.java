package cn.edu.sdust;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


public class AboutusActivity extends Activity{

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.setting_layout_aboutus);	
		
        TextView tv_reg = (TextView)findViewById(R.id.tv_aboutus);
        tv_reg.setBackgroundResource(R.drawable.skinpic_green);
        
        }
	
	
}

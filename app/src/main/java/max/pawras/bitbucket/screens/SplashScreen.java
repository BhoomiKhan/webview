package max.pawras.bitbucket.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import max.pawras.bitbucket.R;
import max.pawras.bitbucket.auth.SignUp;

public class SplashScreen extends AppCompatActivity {

    final static int SPLASH_TIME_OUT=3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sPref=getSharedPreferences("Registration",MODE_PRIVATE);
                String  registrationCode=sPref.getString("isRegister","no");
                if(registrationCode.equals("no")){
                    Intent i=new Intent(SplashScreen.this,SignUp.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.signin_incoming_screen_right_to_mean_position,R.anim.signin_current_screen_move_mean_to_left);
                    finish();
                }
                else if(registrationCode.equals("yes")){
                    Intent i=new Intent(SplashScreen.this,MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);
    }
}
package max.pawras.bitbucket.auth;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import max.pawras.bitbucket.R;
import max.pawras.bitbucket.screens.MainActivity;

import static max.pawras.bitbucket.auth.AuthUtils.haveNetworkConnection;
import static max.pawras.bitbucket.auth.AuthUtils.isEmailValid;

public class SignIn extends AppCompatActivity implements View.OnClickListener {

    private EditText mEmail, mPass;
    private Button sign_in;
    private TextView registration;
    private String email, password, forgetEmail;
    private boolean isExist = false, isForgetPass = false;
    private AlertDialog findMeDialog;
    private ProgressDialog loading;
    private boolean doubleBackToExitPressedOnce = false;
    private ProgressDialog loadingdialog;
    private String loginType = "0", url;
    private Button driver, customer;
    private JsonObject mJsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        initialization();
    }

    //initialization
    public void initialization() {
        driver = (Button) findViewById(R.id.driver);
        customer = (Button) findViewById(R.id.customer);
        driver.setOnClickListener(this);
        customer.setOnClickListener(this);
        mEmail = (EditText) findViewById(R.id.login_email);
        mPass = (EditText) findViewById(R.id.login_password);
        registration = (TextView) findViewById(R.id.switch_to_sign_up);
        registration.setOnClickListener(SignIn.this);
        sign_in = (Button) findViewById(R.id.sign_in);
        sign_in.setOnClickListener(this);
        clickOnCustomer();
    }

    //it will handle all clicks of Login Screen
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_to_sign_up:
                Intent i = new Intent(SignIn.this, SignUp.class);
                startActivity(i);
                overridePendingTransition(R.anim.signin_incoming_screen_right_to_mean_position, R.anim.signin_current_screen_move_mean_to_left);
                finish();
                break;
            case R.id.sign_in:
                validation();
                break;
            case R.id.customer:
                clickOnCustomer();
                break;
            case R.id.driver:
                clickOnDriver();
                break;
        }
    }

    public void validation() {
        email = mEmail.getText().toString();
        password = mPass.getText().toString();

        if (haveNetworkConnection(SignIn.this)) {
            if (isEmailValid(email)) {
                if (!(password.isEmpty() || password.equals(null))) {
                    //when fields will be validated
                    getPermision();
                } else {
                    mPass.requestFocus();
                    Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
                }

            } else {
                mEmail.requestFocus();
                Toast.makeText(this, "invalid email", Toast.LENGTH_SHORT).show();
            }
        } else {
            openDialog();
        }
    }

    private void loginRequest() {
        loadingdialog = new ProgressDialog(SignIn.this);
        loadingdialog.setTitle("Logging");
        loadingdialog.setMessage("Please wait..");
        loadingdialog.show();

        //get uuid of device
        TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String uuid = tManager.getDeviceId();
        Ion.with(SignIn.this)
                .load("http://demo.purewisdomtech.com/car_booking/driver/app_login")
                .setBodyParameter("email", mEmail.getText().toString())
                .setBodyParameter("password", mPass.getText().toString())
                .setBodyParameter("login_type", loginType)
                .setBodyParameter("uuid", uuid)
                .asJsonObject().setCallback(new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
                if ((e==null)) {
                    Log.d("mylog", result.toString());
                    mJsonObject = result;
                    if (loadingdialog.isShowing()) {
                        loadingdialog.dismiss();
                    }
                    if (result.get("msg").toString().equals("\"\"")) {
                        url = result.get("redirect_url").toString();
                        whenSignInSuccessfully();
                    } else {
                        openErrorDialog();
                    }
                } else {
                    if (loadingdialog.isShowing()) {
                        loadingdialog.dismiss();
                    }
                    Toast.makeText(SignIn.this, "Try Again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void whenSignInSuccessfully() {
        //we store values in local database using Shared Preferences
        SharedPreferences sPref = getSharedPreferences("Registration", MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putString("isRegister", "yes");
        editor.putString("url", url);
        editor.commit();

        //we move from one screen to another using Intent Class
        Intent i = new Intent(SignIn.this, MainActivity.class);
        i.putExtra("url", url);
        startActivity(i);
        overridePendingTransition(R.anim.signin_incoming_screen_right_to_mean_position, R.anim.signin_current_screen_move_mean_to_left);
        finish();
        Toast.makeText(SignIn.this, "Successfully Logged In", Toast.LENGTH_SHORT).show();
    }

    public void openDialog() {
        //Dial log box will be appear when there will be no Internet connection
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_connection, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog findMeDialog = dialogBuilder.create();
        findMeDialog.show();
        LinearLayout reset_btn = (LinearLayout) dialogView.findViewById(R.id.ok);
        reset_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findMeDialog.dismiss();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (false) {
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    private void clickOnCustomer() {
        customer.setBackgroundColor(getResources().getColor(R.color.red));
        customer.setTextColor(getResources().getColor(R.color.golden));
        driver.setBackgroundColor(getResources().getColor(R.color.golden));
        driver.setTextColor(getResources().getColor(R.color.red));
        loginType = "0";
    }

    private void clickOnDriver() {
        driver.setBackgroundColor(getResources().getColor(R.color.red));
        driver.setTextColor(getResources().getColor(R.color.golden));
        customer.setBackgroundColor(getResources().getColor(R.color.golden));
        customer.setTextColor(getResources().getColor(R.color.red));
        loginType = "1";
    }


    //A dialog will be opened which ask to enter the code sent to the entered phone number
    public void openErrorDialog() {
        if (loadingdialog.isShowing()) {
            loadingdialog.dismiss();
        }
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_error_log, null);
        TextView tex = dialogView.findViewById(R.id.error_log);
        Log.d("mylog", mJsonObject.get("msg").toString());
        tex.setText(mJsonObject.get("msg").toString());
        dialogBuilder.setView(dialogView);

        findMeDialog = dialogBuilder.create();
        findMeDialog.show();
        LinearLayout reset_btn = (LinearLayout) dialogView.findViewById(R.id.ok);
        reset_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findMeDialog.dismiss();
            }
        });
    }

    private void getPermision() {
        ActivityCompat.requestPermissions(SignIn.this,
                new String[]{Manifest.permission.READ_PHONE_STATE},
                1);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loginRequest();
                } else {
                    Toast.makeText(SignIn.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}
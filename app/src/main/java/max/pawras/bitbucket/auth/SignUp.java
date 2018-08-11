package max.pawras.bitbucket.auth;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import max.pawras.bitbucket.R;
import static max.pawras.bitbucket.auth.AuthUtils.haveNetworkConnection;
import static max.pawras.bitbucket.auth.AuthUtils.isEmailValid;

public class SignUp extends AppCompatActivity implements View.OnClickListener {

    private EditText mFirstName, mLastName, mPhoneNumber, mEmail, mPass;
    private Button sign_up;
    private TextView swith_to_login;
    private FirebaseAuth auth;
    private ProgressDialog loading;
    private String first_name, last_name, email, password, phone_number;
    private boolean isExist = false;
    private boolean doubleBackToExitPressedOnce = false;
    private ProgressDialog loadingdialog;
    private String uuid;
    private JsonObject mJsonObject;
    private AlertDialog findMeDialog;
    private Button driver, customer;
    private String loginType = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initialization();
    }

    //firstly objects will initialize
    public void initialization() {
        driver = (Button) findViewById(R.id.driver);
        customer = (Button) findViewById(R.id.customer);
        driver.setOnClickListener(this);
        customer.setOnClickListener(this);
        mFirstName = (EditText) findViewById(R.id.f_name);
        mLastName = (EditText) findViewById(R.id.l_name);
        mEmail = (EditText) findViewById(R.id.signup_email);
        mPass = (EditText) findViewById(R.id.password);
        mPhoneNumber = (EditText) findViewById(R.id.phone_number);
        swith_to_login = (TextView) findViewById(R.id.switch_to_login);
        sign_up = (Button) findViewById(R.id.sign_up);

        sign_up.setOnClickListener(this);
        swith_to_login.setOnClickListener(SignUp.this);
        swith_to_login.setOnClickListener(this);
        clickOnDriver();
    }

    //it will handle all clicks on sign up screen
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_to_login:
                openActivity();
                break;
            case R.id.sign_up:
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

    //first case & second case
    public void openActivity() {
        Intent i = new Intent(SignUp.this, SignIn.class);
        startActivity(i);
        overridePendingTransition(R.anim.signup_icoming_screen_left_to_mean, R.anim.signup_current_screen_mean_to_right);
        finish();
    }

    public void validation() {
        first_name = mFirstName.getText().toString();
        last_name = mLastName.getText().toString();
        email = mEmail.getText().toString();
        phone_number = mPhoneNumber.getText().toString();
        password = mPass.getText().toString();

        if (haveNetworkConnection(SignUp.this)) {
            if (!first_name.isEmpty()) {
                if (!last_name.isEmpty()) {
                    if (!(email.isEmpty() || !isEmailValid(email))) {
                        if (!(password.isEmpty())) {
                            if (!(phone_number.isEmpty())) {
                                getPermision();
                            } else {
                                mPhoneNumber.setError("Enter phone number");
                            }
                        } else {
                            mPass.setError("Enter Password");
                        }
                    } else {
                        mEmail.setError("Enter valid Email");
                    }
                } else {
                    mLastName.setError("Enter Last name");
                }
            } else {
                mFirstName.setError("Enter first name");
            }

        } else {
            openDialog();
        }
    }

    private void getPermision() {
        ActivityCompat.requestPermissions(SignUp.this,
                new String[]{Manifest.permission.READ_PHONE_STATE},
                1);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    registerUser();

                } else {
                    Toast.makeText(SignUp.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void registerUser() {
        loadingdialog = new ProgressDialog(SignUp.this);
        loadingdialog.setTitle("Signing Up");
        loadingdialog.setMessage("Please wait..");
        loadingdialog.show();

        //get uuid of device
        TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        uuid = tManager.getDeviceId();
        Ion.with(SignUp.this)
                .load("http://demo.purewisdomtech.com/car_booking/driver/app_driver_register")
                .setBodyParameter("fname", mFirstName.getText().toString())
                .setBodyParameter("lname", mLastName.getText().toString())
                .setBodyParameter("email", mEmail.getText().toString())
                .setBodyParameter("password", mPass.getText().toString())
                .setBodyParameter("phone", mPhoneNumber.getText().toString())
                .setBodyParameter("uuid", uuid)
                .setBodyParameter("login_type", loginType)
                .asJsonObject().setCallback(new FutureCallback<JsonObject>() {

            @Override
            public void onCompleted(Exception e, JsonObject result) {
                if (e==null) {
                    mJsonObject = result;
                    if (loadingdialog.isShowing()) {
                        loadingdialog.dismiss();
                    }
                    if (!result.get("message").toString().isEmpty()) {
                        Toast.makeText(SignUp.this, result.get("message").toString(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUp.this, SignIn.class));
                        finish();
                    } else {
                        openErrorDialog();
                    }
                } else {
                    if (loadingdialog.isShowing()){
                        loadingdialog.dismiss();
                    }
                    Toast.makeText(SignUp.this, "Try Again", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    public void openDialog() {
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

    //A dialog will be opened which ask to enter the code sent to the entered phone number
    public void openErrorDialog() {
        if (loadingdialog.isShowing()) {
            loadingdialog.dismiss();
        }
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_forget_password, null);
        TextView tex=dialogView.findViewById(R.id.error_log);
        tex.setText( mJsonObject.get("message").toString());
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
}
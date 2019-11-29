package planet.info.skyline.tech.runtime_permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import planet.info.skyline.R;

public class PermissionActivity extends AppCompatActivity {
    static final int RequestPermissionCode = 100;
    static String[] Permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };

    public static boolean CheckingPermissionIsEnabledOrNot(Context context) {


        return
                        ContextCompat.checkSelfPermission(context, Permissions[0]) == PackageManager.PERMISSION_GRANTED
                        &&
                        ContextCompat.checkSelfPermission(context, Permissions[1]) == PackageManager.PERMISSION_GRANTED
                        &&
                        ContextCompat.checkSelfPermission(context, Permissions[2]) == PackageManager.PERMISSION_GRANTED;


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        Button btn_allow_permission = findViewById(R.id.btn_allow_permission);
        btn_allow_permission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CheckingPermissionIsEnabledOrNot(PermissionActivity.this)) {

                } else {
                    RequestMultiplePermission();
                }
            }
        });


    }

    private void RequestMultiplePermission() {
        try {
            ActivityCompat.requestPermissions(PermissionActivity.this, Permissions, RequestPermissionCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean Camera = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadExternalStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadPhoneState = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    if (ReadExternalStorage && Camera && ReadPhoneState) {
                        Intent intent = new Intent();
                        intent.putExtra("result", ReadExternalStorage && Camera && ReadPhoneState);
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(PermissionActivity.this, "Please allow permissions!", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }


}

package com.android.google.baocao;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private  int RC_SIGN_IN = 100;
    SignInButton mGoogleSignLoginBtn;
    GoogleSignInClient mGoogleSignInClient;
    EditText emailIT, passwordIT;
    Button login ;
    TextView dangnhap , passwordFG;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        emailIT = findViewById(R.id.emailIT);
        passwordIT = findViewById(R.id.passwordIT);
        login = findViewById(R.id.login);
        dangnhap = findViewById(R.id.dangnhap);
        passwordFG = findViewById(R.id.passwordFG);
        mGoogleSignLoginBtn = findViewById(R.id.GoogleLoginBtn);

        mAuth = FirebaseAuth.getInstance();
        //show password
        passwordFG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });
        //show google
        mGoogleSignLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
        

        dangnhap.setOnClickListener(view ->startActivity(new Intent(LoginActivity.this,
                RegisterActivity.class)));
        login.setOnClickListener(view -> {
            ProgressDialog pd = new ProgressDialog(LoginActivity.this);
            pd.setMessage("Please wait...");
            pd.show();
            String str_email = emailIT.getText().toString();
            String str_password = passwordIT.getText().toString();

            if (TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_password)) {
                Toast.makeText(LoginActivity.this, "Hồ sơ duyệt thành công!", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.signInWithEmailAndPassword(str_email,str_password)
                        .addOnCompleteListener(LoginActivity.this, task -> {
                            if (task.isSuccessful()){
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                                        .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
                                Toast.makeText(LoginActivity.this,"Đang cập nhật hồ sơ , đăng nhập thành công" , Toast.LENGTH_SHORT).show();

                                reference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                        pd.dismiss();
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                                        pd.dismiss();
                                    }
                                });
                            } else{
                                pd.dismiss();
                                Toast.makeText(LoginActivity.this,"Đăng nhập thất bại xin thử lại!",Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void showRecoverPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đổi mật khẩu");

        LinearLayout linearLayout = new LinearLayout(this);
        EditText emailIT = new EditText(this);
        emailIT.setHint("Email");
        emailIT.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        emailIT.setMinEms(16);

        linearLayout.addView(emailIT);
        linearLayout.setPadding(10, 10,10,10);

        builder.setView(linearLayout);
        //button recover password
        builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailIT.getText().toString().trim();
                beginRecovery(email);
            }
        });
        //button cancel
        builder.setNegativeButton("Huỷ bỏ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        //show diglog
        builder.create().show();
    }
    private void beginRecovery(String email) {
        ProgressDialog pd = new ProgressDialog(LoginActivity.this);
        pd.setMessage("Đang gửi email...");
        pd.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                pd.dismiss();
                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Gửi email thành công", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(LoginActivity.this, "Lỗi ! Kiểm tra lại", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(LoginActivity.this, ""+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
    if (requestCode == RC_SIGN_IN) {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            // Google Sign In failed, update UI appropriately
            Log.w(TAG, "Google sign in failed", e);
        }
    }
}

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this ,""+user.getEmail(),Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                            if (task.getResult().getAdditionalUserInfo().isNewUser()){
                                String email = user.getEmail();
                                String uid = user.getUid();

                                HashMap<Object, String> hashMap = new HashMap<>();
                                hashMap.put("email", email);
                                hashMap.put("uid", uid);
                                hashMap.put("name", "");
                                hashMap.put("onlineStatus", "online");
                                hashMap.put("typingTo", "noOne");
                                hashMap.put("phone", "");
                                hashMap.put("image", "");
                                hashMap.put("cover", "");

                                FirebaseDatabase database = FirebaseDatabase.getInstance();

                                DatabaseReference reference = database.getReference("Users");

                                reference.child(uid).setValue(hashMap);
                            }

                            Toast.makeText(LoginActivity.this, ""+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                            // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}

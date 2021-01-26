package dev.nicolas.firebasedemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import dev.nicolas.firebasedemo.model.User;

public class EditProfileActivity extends AppCompatActivity {
    private ImageView imgClose;
    private TextView save, changePhoto;
    private CircleImageView imageProfile;
    private MaterialEditText fullname, username, bio;
    private FirebaseUser fUser;
    private Uri imageUri;
    private StorageTask uploadTask;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        imgClose = findViewById(R.id.imgClose);
        save = findViewById(R.id.save);
        changePhoto = findViewById(R.id.change_photo);
        imageProfile = findViewById(R.id.image_profile);
        fullname = findViewById(R.id.fullname_et);
        username = findViewById(R.id.username_et);
        bio = findViewById(R.id.bio_et);

        loadUserData();
        storageRef = FirebaseStorage.getInstance().getReference().child("Uploads");

        imgClose.setOnClickListener(v -> finish());
        changePhoto.setOnClickListener(v -> CropImage.activity()
                .setCropShape(CropImageView.CropShape.OVAL).
                start(EditProfileActivity.this));
        imageProfile.setOnClickListener(v -> CropImage.activity()
                .setCropShape(CropImageView.CropShape.OVAL).
                        start(EditProfileActivity.this));
        save.setOnClickListener(v -> updateProfile());
    }

    private void updateProfile() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("fullname", fullname.getText().toString());
        map.put("username", username.getText().toString());
        map.put("bio", bio.getText().toString());

        FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid())
                .updateChildren(map);

        loadUserData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            uploadImage();
        }else {
            Toast.makeText(EditProfileActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImage() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading image...");
        pd.show();

        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(String.format("%s.jpeg", System.currentTimeMillis()));
            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return fileRef.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>)  task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    String url = downloadUri.toString();
                    FirebaseDatabase.getInstance().getReference().child("Users")
                            .child(fUser.getUid()).child("imgUrl").setValue(url);
                    pd.dismiss();
                }else {
                    Toast.makeText(EditProfileActivity.this, "Upload failed!",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            Toast.makeText(EditProfileActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserData() {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseDatabase.getInstance().getReference().child("Users").child(fUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        fullname.setText(user.getFullname());
                        username.setText(user.getUsername());
                        bio.setText(user.getBio());
                        Picasso.get().load(user.getImgUrl()).placeholder(R.drawable.icon)
                                .into(imageProfile);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
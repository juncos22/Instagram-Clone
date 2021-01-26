package dev.nicolas.firebasedemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hendraanggrian.appcompat.socialview.Hashtag;
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.theartofdev.edmodo.cropper.CropImage;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostActivity extends AppCompatActivity {
    private ImageView closeImg, imageAdded;
    private TextView postTxt;
    private SocialAutoCompleteTextView descriptionTxt;
    private Uri imageUri;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        closeImg = findViewById(R.id.close);
        imageAdded = findViewById(R.id.image_added);
        postTxt = findViewById(R.id.post);
        descriptionTxt = findViewById(R.id.description);

        closeImg.setOnClickListener(v -> {
            startActivity(new Intent(PostActivity.this, MainActivity.class));
            finish();
        });
        postTxt.setOnClickListener(v -> uploadImage());

        CropImage.activity().start(PostActivity.this);
    }

    private void uploadImage() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading image...");
        pd.show();

        if (imageUri != null) {
            StorageReference storage = FirebaseStorage.getInstance().getReference("Posts")
                    .child(String.format("%s.%s", System.currentTimeMillis(), getFileExtension(imageUri)));
            StorageTask uploadTask = storage.putFile(imageUri);
            uploadTask.continueWithTask((Continuation) task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return storage.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                Uri uri = task.getResult();
                imageUrl = uri.toString();
                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Posts");
                String postId = dbRef.push().getKey();
                HashMap<String, Object> map = new HashMap<>();
                map.put("postId", postId);
                map.put("imageUrl", imageUrl);
                map.put("description", descriptionTxt.getText().toString());
                map.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
                dbRef.child(postId).setValue(map);

                DatabaseReference hashtagRef = FirebaseDatabase.getInstance().getReference().child("HashTags");
                List<String> hashTags = descriptionTxt.getHashtags();
                if (!hashTags.isEmpty()) {
                    for (String tag : hashTags) {
                        map.clear();
                        map.put("tag", tag.toLowerCase());
                        map.put("postId", postId);
                        hashtagRef.child(tag.toLowerCase()).child(postId).setValue(map);
                    }
                }
                pd.dismiss();
                startActivity(new Intent(PostActivity.this, MainActivity.class));
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                pd.dismiss();
            });
        }else {
            Toast.makeText(PostActivity.this, "No image was selected", Toast.LENGTH_SHORT).show();
            pd.dismiss();
        }
    }

    private String getFileExtension(Uri uri) {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(this.getContentResolver().getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            imageAdded.setImageURI(imageUri);
        }else {
            Toast.makeText(this, "Try again!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PostActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        final ArrayAdapter<Hashtag> hashtagAdapter = new HashtagArrayAdapter<>(getApplicationContext());
        FirebaseDatabase.getInstance().getReference().child("HashTags")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            hashtagAdapter.add(new Hashtag(data.getKey(), (int)data.getChildrenCount()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        descriptionTxt.setHashtagAdapter(hashtagAdapter);
    }
}
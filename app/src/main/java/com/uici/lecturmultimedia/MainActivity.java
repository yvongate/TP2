package com.uici.lecturmultimedia;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.uici.lecturmultimedia.databinding.ActivityMainBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements MediaFileAdapter.OnMediaFileActionListener {

    private ActivityMainBinding binding;
    private MediaFileManager mediaFileManager;
    private MediaFileAdapter mediaFileAdapter;
    private List<MediaFile> mediaFiles;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> pickMediaLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mediaFileManager = new MediaFileManager(this);
        mediaFiles = new ArrayList<>();

        setupRecyclerView();
        setupFab();
        loadMediaFiles();
        setupPermissionLauncher();
        setupMediaPickerLauncher();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMediaFiles();
    }

    private void setupRecyclerView() {
        mediaFileAdapter = new MediaFileAdapter(mediaFiles, this);
        binding.recyclerViewMediaFiles.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewMediaFiles.setAdapter(mediaFileAdapter);
    }

    private void setupFab() {
        binding.fabAddMedia.setOnClickListener(v -> showMediaTypeDialog());
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openMediaPicker();
                } else {
                    Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }

    private void setupMediaPickerLauncher() {
        pickMediaLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        addMediaFileFromUri(uri);
                    }
                }
            }
        );
    }

    private void showMediaTypeDialog() {
        String[] options = {"Audio", "Video"};
        new AlertDialog.Builder(this)
            .setTitle("Choisir le type de fichier")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    checkPermissionAndPickMedia("audio/*");
                } else {
                    checkPermissionAndPickMedia("video/*");
                }
            })
            .show();
    }

    private void checkPermissionAndPickMedia(String mimeType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (mimeType.startsWith("audio")) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO);
                } else {
                    openMediaPicker(mimeType);
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                    != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO);
                } else {
                    openMediaPicker(mimeType);
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                openMediaPicker(mimeType);
            }
        }
    }

    private void openMediaPicker() {
        openMediaPicker("*/*");
    }

    private void openMediaPicker(String mimeType) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        pickMediaLauncher.launch(intent);
    }

    private void addMediaFileFromUri(Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION);

            String fileName = getFileName(uri);
            String mimeType = getContentResolver().getType(uri);
            String type = mimeType != null && mimeType.startsWith("video") ? "video" : "audio";
            long duration = getMediaDuration(uri);
            long size = getFileSize(uri);

            MediaFile mediaFile = new MediaFile(
                UUID.randomUUID().toString(),
                fileName,
                uri.toString(),
                type,
                duration,
                size
            );

            mediaFileManager.addMediaFile(mediaFile);
            loadMediaFiles();
            Toast.makeText(this, "Fichier ajouté", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors de l'ajout du fichier", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private long getFileSize(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    return cursor.getLong(sizeIndex);
                }
            }
        }
        return 0;
    }

    private long getMediaDuration(Uri uri) {
        try (Cursor cursor = getContentResolver().query(uri,
            new String[]{MediaStore.Video.Media.DURATION}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int durationIndex = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
                if (durationIndex != -1) {
                    return cursor.getLong(durationIndex);
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return 0;
    }

    private void loadMediaFiles() {
        mediaFiles.clear();
        mediaFiles.addAll(mediaFileManager.getMediaFiles());
        mediaFileAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (mediaFiles.isEmpty()) {
            binding.recyclerViewMediaFiles.setVisibility(View.GONE);
            binding.textViewEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewMediaFiles.setVisibility(View.VISIBLE);
            binding.textViewEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPlayClick(MediaFile mediaFile) {
        Intent intent = new Intent(this, MediaPlayerActivity.class);
        intent.putExtra("media_file", mediaFile);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(MediaFile mediaFile) {
        new AlertDialog.Builder(this)
            .setTitle("Supprimer le fichier")
            .setMessage("Voulez-vous vraiment supprimer " + mediaFile.getName() + " ?")
            .setPositiveButton("Oui", (dialog, which) -> {
                mediaFileManager.deleteMediaFile(mediaFile.getId());
                loadMediaFiles();
                Toast.makeText(this, "Fichier supprimé", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Non", null)
            .show();
    }
}

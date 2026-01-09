package com.uici.lecturmultimedia;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.uici.lecturmultimedia.databinding.ItemMediaFileBinding;
import java.util.List;

public class MediaFileAdapter extends RecyclerView.Adapter<MediaFileAdapter.MediaFileViewHolder> {

    private List<MediaFile> mediaFiles;
    private OnMediaFileActionListener listener;

    public interface OnMediaFileActionListener {
        void onPlayClick(MediaFile mediaFile);
        void onDeleteClick(MediaFile mediaFile);
    }

    public MediaFileAdapter(List<MediaFile> mediaFiles, OnMediaFileActionListener listener) {
        this.mediaFiles = mediaFiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MediaFileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMediaFileBinding binding = ItemMediaFileBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new MediaFileViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaFileViewHolder holder, int position) {
        holder.bind(mediaFiles.get(position));
    }

    @Override
    public int getItemCount() {
        return mediaFiles.size();
    }

    class MediaFileViewHolder extends RecyclerView.ViewHolder {
        private ItemMediaFileBinding binding;

        public MediaFileViewHolder(ItemMediaFileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MediaFile mediaFile) {
            binding.textViewFileName.setText(mediaFile.getName());
            binding.textViewFileType.setText(mediaFile.isAudio() ? "Audio" : "Video");
            binding.textViewFileSize.setText(mediaFile.getFormattedSize());

            if (mediaFile.getDuration() > 0) {
                binding.textViewFileDuration.setText(mediaFile.getFormattedDuration());
            } else {
                binding.textViewFileDuration.setText("--:--");
            }

            int iconRes = mediaFile.isAudio() ? R.drawable.ic_audio : R.drawable.ic_video;
            binding.imageViewFileIcon.setImageResource(iconRes);

            binding.imageButtonPlay.setOnClickListener(v -> listener.onPlayClick(mediaFile));
            binding.imageButtonDelete.setOnClickListener(v -> listener.onDeleteClick(mediaFile));

            itemView.setOnClickListener(v -> listener.onPlayClick(mediaFile));
        }
    }
}

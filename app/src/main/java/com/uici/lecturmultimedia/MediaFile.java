package com.uici.lecturmultimedia;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaFile implements Parcelable {
    private String id;
    private String name;
    private String path;
    private String type;
    private long duration;
    private long size;

    public MediaFile(String id, String name, String path, String type, long duration, long size) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.type = type;
        this.duration = duration;
        this.size = size;
    }

    protected MediaFile(Parcel in) {
        id = in.readString();
        name = in.readString();
        path = in.readString();
        type = in.readString();
        duration = in.readLong();
        size = in.readLong();
    }

    public static final Creator<MediaFile> CREATOR = new Creator<MediaFile>() {
        @Override
        public MediaFile createFromParcel(Parcel in) {
            return new MediaFile(in);
        }

        @Override
        public MediaFile[] newArray(int size) {
            return new MediaFile[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getType() {
        return type;
    }

    public long getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }

    public boolean isAudio() {
        return type.equals("audio");
    }

    public boolean isVideo() {
        return type.equals("video");
    }

    public String getFormattedDuration() {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds = seconds % 60;
        minutes = minutes % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    public String getFormattedSize() {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(path);
        dest.writeString(type);
        dest.writeLong(duration);
        dest.writeLong(size);
    }
}

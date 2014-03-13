package com.micdm.transportlive.misc;

import android.content.Context;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.tilesource.ITileSource;

import java.io.IOException;
import java.io.InputStream;

public class AssetArchive implements IArchiveFile {

    public static AssetArchive getAssetArchive(Context context) {
        return new AssetArchive(context);
    }

    private Context context;

    private AssetArchive(Context context) {
        this.context = context;
    }

    @Override
    public InputStream getInputStream(ITileSource source, MapTile tile) {
        String path = String.format("atlas/%s", source.getTileRelativeFilenameString(tile));
        try {
            return context.getAssets().open(path);
        } catch (IOException e) {
            return null;
        }
    }
}

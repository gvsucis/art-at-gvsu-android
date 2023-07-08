package edu.gvsu.art.gallery.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

/**
 * @author josiah
 * @version 2/2/16
 */
public class BorderCircleTransform implements Transformation {
    private static BorderCircleTransform borderCircleTransform;
    private final int BORDER_COLOR = Color.WHITE;
    private final int ALT_BORDER_COLOR = Color.parseColor("#99FFFFFF");
    private final int BORDER_RADIUS = 5;

    private BorderCircleTransform() {
        // Private for use of singleton class
    }

    /**
     * Retrieve instance of BorderCircleTransform.
     *
     * @return border circle transformation
     */
    public static BorderCircleTransform getBorderCircleTransform() {
        if (borderCircleTransform == null) {
            borderCircleTransform = new BorderCircleTransform();
        }
        return borderCircleTransform;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader
                = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size / 2f;

        // Prepare the background
        Paint paintBg = new Paint();
        paintBg.setColor(ALT_BORDER_COLOR);
        paintBg.setAntiAlias(true);

        // Draw the background circle
        canvas.drawCircle(r, r, r, paintBg);

        // Draw the image smaller than the background so a little border will be seen
        canvas.drawCircle(r, r, r - BORDER_RADIUS, paint);

        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "circle";
    }
}
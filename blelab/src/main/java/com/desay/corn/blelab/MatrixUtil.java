package com.desay.corn.blelab;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;


/**
 * Created by 方奕峰 on 14-10-14.
 */
public class MatrixUtil {
    public static void main(String[] args) throws IOException {
        int w = 200;
        int h = 40;
        String str = "奥 巴 马";
//        t1(w, h, "黑体", 30, str);
        Bitmap bi = font2Bitmap(w, h, 20, str);
//        bi = ImageIO.read(new File("F:/cc.png"));
        bitmap2Bytes(1, bi, Color.LTGRAY);
    }

    public static Bitmap font2Bitmap(int w, int h, int fontSize, String str) {
        Bitmap bi = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        Canvas g = new Canvas(bi);
        Paint paint = new Paint();
        g.drawARGB(255, 255, 255, 255);
        paint.setColor(Color.BLACK);
        paint.setTextSize(fontSize);
        Rect r = new Rect();
        paint.getTextBounds(str, 0, str.length(), r);
        g.drawText(str, (w - r.width()) / 2, r.height() + (h - r.height()) / 2, paint);
        return bi;
    }

    public static byte[][] bitmap2Bytes(int offset, Bitmap bi, int color) {
        byte[][] bytes = new byte[bi.getHeight()][bi.getWidth()];
        for (int i = offset / 2; i < bi.getHeight(); i += offset) {
            for (int j = offset / 2; j < bi.getWidth(); j += offset) {
                int pixel = bi.getPixel(j, i);
                bytes[i][j] = pixel > color ? (byte) 0 : 1;
                System.out.print(" " + bytes[i][j]);
            }
            System.out.println();
        }
        return bytes;
    }

    public static final int fontByteSize = 72;

    public static byte[] unicode2Bytes(Context context, int unicode) throws IOException {
        final int BASE_UNICODE = 0x4e00;
        final int BASE_EN_UNICODE = 0x0020;
        final int BASE_EN_UNICODE_START = 0x51a6;

//        int index = (unicode - BASE_UNICODE) * fontByteSize;

        int index = 0;
        if (unicode < 0x4e00 && unicode >= BASE_EN_UNICODE)
            index = (unicode - BASE_EN_UNICODE + BASE_EN_UNICODE_START) * fontByteSize;
        else if (unicode >= BASE_UNICODE && unicode <= 0x9fa5)
            index = (unicode - BASE_UNICODE) * fontByteSize;
        else
            index = BASE_EN_UNICODE_START * fontByteSize;

        if (index < 0) return null;

        byte[] buf = new byte[fontByteSize];
        InputStream is = context.getAssets().open("ziku24_all.bin", AssetManager.ACCESS_BUFFER);
        try {
            is.skip(index);
            if (is.read(buf) != -1) {
                for (int i = 0; i < buf.length; i++) {
                    if (i % 16 == 0) {
                        System.out.println();
                    }
                    System.out.printf("%x ", buf[i]);
                }
            }
        } catch (Exception e) {
            DesayLog.d("e = "+e);
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return buf;
    }
}

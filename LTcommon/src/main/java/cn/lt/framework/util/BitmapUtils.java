package cn.lt.framework.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;


/**
 * 图片处理帮助类
 * Created by wenchao on 2015/12/3.
 */
public class BitmapUtils {
    /**
     * 计算bitmap sampleSize
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int caculateInSampleSize(BitmapFactory.Options options,int reqWidth,int reqHeight){
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if(reqWidth == 0 || reqHeight == 0)return 1;
        if(height > reqHeight || width >reqWidth){
            int heightRatio = Math.round((float)height/(float)reqHeight);
            int widthRatio = Math.round((float)width/(float)reqWidth);
            inSampleSize = Math.max(heightRatio,widthRatio);
        }
        return inSampleSize;

    }

    /**
     * 压缩图片
     * @param path
     * @param reqWidth 最大宽度
     * @param reqHeight 最大高度
     * @return
     */
    public static Bitmap compressBitmap(String path,int reqWidth,int reqHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        options.inSampleSize = caculateInSampleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path,options);
    }


    /**
     * 压缩图片并保存
     * @param srcPath 原图路劲
     * @param destPath 压缩图路劲
     * @param reqWidth 最大宽
     * @param reqHeight 最大高
     * @param isDelSrc 删除原图标志
     * @return
     */
    public static String compressBitmap(String srcPath,String destPath,int reqWidth,int reqHeight,boolean isDelSrc){
        Bitmap bitmap = compressBitmap(srcPath,reqWidth,reqHeight);
        File srcFile = new File(srcPath);
        int degress = getDegress(srcPath);
        try {
            if(degress!=0)bitmap = rotateBitmap(bitmap,degress);
            File destFile = new File(destPath);
            FileOutputStream fos = new FileOutputStream(destFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
            fos.close();
            if(isDelSrc)srcFile.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return destPath;

    }

    /**
     * 压缩某个输入流中的图片，可以解决网络输入流压缩问题，并得到图片对象
     * @param is
     * @param reqWidth
     * @param reqHeight
     * @return
     */

    public static Bitmap compressBitmap(InputStream is,int reqWidth,int reqHeight){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ReadableByteChannel channel = Channels.newChannel(is);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while(channel.read(buffer)!=-1){
                buffer.flip();
                while(buffer.hasRemaining())baos.write(buffer.get());
                buffer.clear();
            }
            byte[] bts = baos.toByteArray();
            Bitmap bitmap = compressBitmap(bts,reqWidth,reqHeight);
            is.close();
            channel.close();
            baos.close();
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 压缩制定byte[]图片，并得到压缩后的图像
     * @param bts
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap compressBitmap(byte[] bts,int reqWidth,int reqHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds =true;
        BitmapFactory.decodeByteArray(bts, 0, bts.length, options);
        options.inSampleSize = caculateInSampleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(bts, 0, bts.length, options);
    }

    /**
     * 压缩已存在的图片对象，并返回压缩后的图片
     * @param bitmap
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap compressBitmap(Bitmap bitmap,int reqWidth,int reqHeight){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
            byte[] bts = baos.toByteArray();
            Bitmap res = compressBitmap(bts,reqWidth,reqHeight);
            baos.close();
            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    /**
     * 压缩图片资源，并返回图片对象
     * @param res
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap compressBitmap(Resources res,int resId,int reqWidth,int reqHeight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res,resId,options);
        options.inSampleSize = caculateInSampleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res,resId,options);
    }

    /**
     * 基于质量的压缩算法，此方法未解决压缩后图像失真的问题
     * 可先调用比列压缩适当压缩图片后，再调用此方法可解决上述问题
     * @param bitmap
     * @param maxBytes  单位byte
     * @return
     */
    public static Bitmap compressBitmap(Bitmap bitmap,long maxBytes){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
            int quality = 90;
            while(baos.toByteArray().length > maxBytes){
                baos.reset();
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                quality -= 10;
            }
            byte[] bts = baos.toByteArray();
            Bitmap destBitmap = BitmapFactory.decodeByteArray(bts,0,bts.length);
            baos.close();
            return destBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    /**
     * 压缩图片，得到压缩后的路劲，
     * @param srcPath 图片原路径
     * @return
     */
    public static String compressBitmap(String srcPath,String destPath){
        Bitmap bitmap = compressBitmap(srcPath,700,700);
        BufferedOutputStream os = null;
        try {
            File file = new File(destPath+File.separator+TimeUtils.getCurrentTimeInLong()+".jpg");
            if(file.exists()){
                file.delete();
            }
            file.createNewFile();
            os = new BufferedOutputStream(new FileOutputStream(file));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
            int quality = 90;
            while(baos.toByteArray().length/1024 > 300){
                baos.reset();
                bitmap.compress(Bitmap.CompressFormat.JPEG,quality,baos);
                quality -= 10;
                if(quality <= 10){
                    break;
                }
            }
            os.write(baos.toByteArray());
            os.flush();
            return file.getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(os!=null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return srcPath;
    }

    /**
     * 压缩图片，得到压缩后的路劲，
     * @return
     */
    public static File compressBitmap(Bitmap bitmap,String destPath,String name){
        bitmap = compressBitmap(bitmap,700,700);
        BufferedOutputStream os = null;
        try {
            File file = new File(destPath+File.separator+name+".jpg");
            if(file.exists()){
                file.delete();
            }
            file.createNewFile();
            os = new BufferedOutputStream(new FileOutputStream(file));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
            int quality = 90;
            while(baos.toByteArray().length/1024 > 300){
                baos.reset();
                bitmap.compress(Bitmap.CompressFormat.JPEG,quality,baos);
                quality -= 10;
                if(quality <= 10){
                    break;
                }
            }
            os.write(baos.toByteArray());
            os.flush();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(os!=null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 获取bitmap的弧度转角度
     * @param path
     * @return
     */
    public static int getDegress(String path){
        int degress = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
            switch (orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degress = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degress = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degress = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degress;
    }

    /**
     * 根据角度旋转图片
     * @param bitmap
     * @param degress
     * @return
     */
    public static Bitmap rotateBitmap(Bitmap bitmap,int degress){
        if(bitmap!=null){
            Matrix m = new Matrix();
            m.postRotate(degress);
            bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),m,true);
        }
        return bitmap;
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565);

        Canvas canvas = new Canvas(bitmap);

        //canvas.setBitmap(bitmap);

        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;

    }

    public static String getImagePath(Context context, Uri uri) {
        if (null == uri) {
            Log.e("getImagePath", "uri return null");
            return null;
        }

        Log.e("getImagePath", uri.toString());
        String path = null;
        final String scheme = uri.getScheme();
        if (null == scheme) {
            path = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            path = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
            int nPhotoColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            if (null != cursor) {
                cursor.moveToFirst();
                path = cursor.getString(nPhotoColumn);
            }
            cursor.close();
        }

        return path;
    }


    /**
     * 以最省内存的方式读取本地资源的图片
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap readBitMap(Context context, int resId)
    {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

}

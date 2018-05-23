package cn.lt.android.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.lt.android.entity.HotSearchBean;
import cn.lt.android.util.DensityUtil;
import cn.lt.appstore.R;
import cn.lt.framework.log.Logger;

/***
 * Created by atian on 2016/1/20.
 * 大家搜标签控件
 */
public class ImageTitleView extends View {
    private String textColor[] = {"#f26d66", "#f0a53c", "#6fa305", "#3db7cc"};
    private int textColor2 = 0;
    private Paint mPaint;
    private List<HotSearchBean> titleList = null;
    private List<HotSearchBean> strList = null;
    private int width = 0;
    private int height = 0;
    private float spaceWidth = 0;
    private float spaceHeight = 0;
    private float padding = 0;
    private float textSize = 0;
    private Context context;
    private float linesWidth = 2f;
    private float textHeight = 0;
    private Map<String, Rect> rectList = new HashMap<>();
    private ImageViewCallBack callBack = null;


    public ImageTitleView(Context context, List<HotSearchBean> titleList, float spaceWidth, float spaceHeight, float textSizePX) {
        super(context);
        // TODO Auto-generated constructor stub
        this.context = context;
        init(spaceWidth, spaceHeight, textSizePX);
        this.titleList = new ArrayList<>();
        this.titleList.addAll(titleList);
        strList = new ArrayList<>();
        strList.addAll(this.titleList);
    }

    private void init(float Width, float Height, float Size) {
        width = DensityUtil.getScreenSize(context)[0];
        // 默认与字体宽度间隔
        spaceWidth = Width;
        padding = spaceWidth;
        // 默认与字体高度间隔
        spaceHeight = Height;
        // 字体大小
        textSize = Size;
        initPaint();
        textHeight = spaceHeight * 2 + getTextHeight(mPaint);
        //默认线 1dp粗
        linesWidth = DensityUtil.dip2px(context, 0.5f);
    }

    private void initPaint() {
        if (mPaint == null) {
            mPaint = new Paint();
        }
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(textSize);
        mPaint.setColor(Color.TRANSPARENT);
        mPaint.setDither(true);
        mPaint.setFilterBitmap(true);
        mPaint.setSubpixelText(true);
        mPaint.setStrokeWidth(1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //分割线的颜色
        canvas.drawColor(Color.parseColor("#FFFFFF"));
        restWidthSpace(titleList, padding);
        //linesWidth上边线
        drawTitle(canvas, titleList, width, (int) linesWidth, textHeight, 1);
    }

    // 重新分配间隔
    private void restWidthSpace(List<HotSearchBean> textDate, float space) {
        float restWidth = space;
        spaceWidth = space;
        int sumWidth = 0;
        int index = 0;
        float residueSpace = 0;
        for (int i = 0; i < textDate.size(); i++) {
            sumWidth += restWidth * 2 + getTextWidth(mPaint, textDate.get(i).getTitle());
            if (width - sumWidth > 0) {
                residueSpace = width - sumWidth;
                index = i;
            }
        }
        spaceWidth += (float) residueSpace / ((index + 1) * 2);
    }

    private void drawTitle(Canvas canvas, List<HotSearchBean> textDate, float width, int top, float bottom, int indexRow) {
        // TODO Auto-generated method stub
        List<HotSearchBean> temp = new ArrayList<>();
        temp.addAll(textDate);
        float sumWidth = 0;
        //左边线
        float startLeft = linesWidth;
        float textX = top;
        float textY = spaceHeight + getTextHeight(mPaint) + top;
        for (int i = 0; i < temp.size(); i++) {
            sumWidth += spaceWidth * 2 + getTextWidth(mPaint, temp.get(i).getTitle());
            if (sumWidth - linesWidth <= width) {
                textX = spaceWidth + startLeft;
                Logger.i("i = " + i +"sumWidth =" +sumWidth+"temp.size ="+temp.size() );
                if (canvas != null) {
                    drawBackGround(canvas, startLeft, top, sumWidth, bottom * indexRow, textX, textY);
                    String title = temp.get(i).getTitle();
                    Logger.i("title =          " +title );
                    drawText(canvas, title, startLeft, top, sumWidth, bottom * indexRow, textX, textY);
                }
                startLeft = sumWidth + linesWidth;
            } else {
                List<HotSearchBean> tempList = new ArrayList<>();
                for (int j = i; j < temp.size(); j++) {
                    tempList.add(temp.get(j));
                }
                temp.clear();
                if (indexRow<4){
                    indexRow++;
                    restWidthSpace(tempList, padding);
                    //下边线
                    float toBottom = (int) bottom * (indexRow - 1) + linesWidth;
                    drawTitle(canvas, tempList, width, (int) toBottom, bottom, indexRow);
                    return;
                }else{
                    indexRow=4;
                    Log.i("zzz","超过四行，indexRow="+indexRow);
                }
            }
        }
        height = (int) bottom * indexRow;
    }

    private void drawText(Canvas canvas, String text, float left, float top, float right, float bottom, float x, float y) {
        mPaint.setColor(Color.TRANSPARENT);
        Rect rect = new Rect((int) left, (int) top, (int) right, (int) bottom);
        canvas.drawRect(rect, mPaint);
        mPaint.setColor(Color.WHITE);
        if (textColor2 != 0) {
            mPaint.setColor(textColor2);
        } else {
            int index = (int) (Math.random() * 4);
            mPaint.setColor(Color.parseColor(textColor[index]));
        }
        canvas.drawText(text, x, y, mPaint);
        rectList.put(text, rect);

    }

    /***
     * 画背景图片
     *
     * @param canvas
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param x
     * @param y
     */
    private void drawBackGround(Canvas canvas, float left, float top, float right, float bottom, float x, float y) {
        try {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.tag_bg);
            byte[] chunk = bitmap.getNinePatchChunk();
            NinePatchDrawable np_drawable = new NinePatchDrawable(bitmap, chunk, null, null);
            np_drawable.setBounds((int) left, (int) top, (int) right, (int) bottom);
            Bitmap.Config config = np_drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
            Bitmap output_bitmap = Bitmap.createBitmap(np_drawable.getIntrinsicWidth(), np_drawable.getIntrinsicHeight(), config);
            np_drawable.draw(canvas);
            canvas.drawBitmap(output_bitmap, x, y, mPaint);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /***
     * 点击回调
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        try {
            if (MotionEvent.ACTION_UP == event.getAction()) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                final int count = rectList.size();
                for (int i = 0; i < count; i++) {
                    if (rectList.get(strList.get(i).getTitle()).contains(x, y)) {
                        if (callBack != null) {
                            callBack.onClick(strList.get(i).getTitle(), i);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));

    }

    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            //右边线
            width = (int) (specSize - linesWidth);
            result = specSize;
        } else {
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);// 60,480
            }
        }

        return result;
    }

    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            restWidthSpace(titleList, padding);
            drawTitle(null, titleList, width, 0, textHeight, 1);
            result = (int) (height + linesWidth);
            if (specMode == MeasureSpec.AT_MOST) {

                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    public int getTextWidth(Paint paint, String str) {
        int iRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) Math.ceil(widths[j]);
            }
        }
        return iRet;
    }

    public int getTextHeight(Paint pain) {
        return (int) ((pain.getFontMetrics().descent - pain.getFontMetrics().ascent) / 2);
    }


    public ImageViewCallBack getCallBack() {
        return callBack;
    }

    public void setCallBack(ImageViewCallBack callBack) {
        this.callBack = callBack;
    }

    public void setTextColor(int color) {
        textColor2 = color;
    }

    public interface ImageViewCallBack {
        void onClick(String info, int position);
    }
}

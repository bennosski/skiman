package com.example.ben.skiman3;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.widget.TextView;

import static android.graphics.Color.argb;

/**
 * Created by Ben on 6/19/2017.
 */

public class DrawingView extends View {
    public int data = 0;
    public int width = 100, top_pad=30, xmin, xmax, ymin, ymax, N=1;
    public int ix_start=0, smooth_ix_start=0, smooth_ix_end=0;
    public float iy_start = 0;
    public float xs_user[], ys_user[], xs_hist[], ys_hist[];
    public boolean runSimulation=false;

    //drawing path
    private Path drawPath;

    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    //private int paintColor = 0xFF660000;
    private int paintColor = argb(255,0,0,0);
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;


    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }

    private void setupDrawing() {
        drawPath = new Path();
        drawPaint = new Paint();

        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(1);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //view given size
        super.onSizeChanged(w, h, oldw, oldh);

        BitmapFactory.Options opts=new BitmapFactory.Options();
        opts.inDither=false;
        opts.inSampleSize = 8;
        opts.inPurgeable=true;
        opts.inInputShareable=true;
        opts.inTempStorage=new byte[16 * 1024];

        canvasBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.myimage), w, h, true);

        drawCanvas = new Canvas(canvasBitmap);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE); //may not be necessary

        canvas.drawBitmap(canvasBitmap,new Rect(0,0,width,width), new Rect(0,0,width,width+top_pad), null);

        //canvas.drawPath(drawPath, drawPaint);
        for(int i=0; i<N-1; i++)
            canvas.drawLine(xs_user[i], ys_user[i], xs_user[i+1], ys_user[i+1], drawPaint);

        //drawCanvas.drawPoint(myi*10, 100, drawPaint);
        double x;
        double y;
        if(runSimulation) {
            x = mySkier.xs_frames.get(myi);
            y = mySkier.ys_frames.get(myi);
            //unscale the coordinates
            x =  x*(xmax-xmin) + xmin;
            y = -y*(ymax-ymin) + ymin;
        }
        else{
            x = 0.;
            y = 0.;
        }
        canvas.drawCircle((int)(x), (int)(y), 5, canvasPaint);
        //canvas.drawCircle(myi*10, myi*10, 5, canvasPaint);
        //canvas.drawRect(0,0,width,width,drawPaint);


        Path outline = new Path();
        outline.moveTo(xmin,ymin);
        outline.lineTo(xmax,ymin);
        outline.lineTo(xmax,ymax);
        outline.lineTo(xmin,ymax);
        outline.lineTo(xmin,ymin);
        canvas.drawPath(outline, drawPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //detect user touch
        float touchX = event.getX();
        float touchY = event.getY();
        int ix = x_float_to_pixel(touchX)-xmin;
        float iy = touchY;

        //data = (int)touchX;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //drawPath.moveTo(touchX, touchY);

                for(int i=0; i<N; i++){
                    xs_hist[i] = xs_user[i];
                    ys_hist[i] = ys_user[i];
                }

                ys_user[ix] = iy;
                ix_start = ix;
                iy_start = iy;
                smooth_ix_start = ix;
                smooth_ix_end = ix;

                break;
            case MotionEvent.ACTION_MOVE:
                //drawPath.lineTo(touchX, touchY);
                if(ix<smooth_ix_start) smooth_ix_start=ix;
                if(ix>smooth_ix_end) smooth_ix_end=ix;

                int i1x=0;
                int i2x=0;
                float i1y=0;
                float i2y=0;
                if(ix_start<ix) {
                    i1x = ix_start;
                    i2x = ix;
                    i1y = iy_start;
                    i2y = iy;
                }
                else{
                    i2x = ix_start;
                    i1x = ix;
                    i2y = iy_start;
                    i1y = iy;
                }
                for(int i=i1x+1; i<i2x; i++) {
                    ys_user[i] = Math.round(i1y*(i-i2x)/(i1x-i2x) + i2y*(i-i1x)/(i2x-i1x));
                }
                ys_user[ix] = iy;
                ix_start = ix;
                iy_start = iy;
                break;
            case MotionEvent.ACTION_UP:
                assist_drawing_beginning();
                assist_drawing_ending();

                //apply some smoothing
                float[] ys_temp = new float[N];
                for(int i=0; i<N; i++) {
                    if(i<smooth_ix_start)
                        ys_temp[i] = ys_user[smooth_ix_start];
                    else if(i>smooth_ix_end)
                        ys_temp[i] = ys_user[smooth_ix_end];
                    else
                        ys_temp[i] = ys_user[i];
                }
                for(int i=smooth_ix_start; i<=smooth_ix_end; i++){
                    int L = get_smoothing_dist(i);
                    if(L>=1) {
                        float mean = 0;
                        for (int j = i - L; j <= i + L; j++) {
                            mean += ys_temp[j] / (2 * L + 1);
                        }
                        //ys_user[i] = y_float_to_pixel(mean);
                        ys_user[i] = mean;
                    }
                }

                ix_start = 0;
                iy_start = 0;

                //drawCanvas.drawPath(drawPath, drawPaint);
                //drawPath.reset();
                //drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
                //invalidate();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    private int get_smoothing_dist(int i){
        int[] ans;
        int L = Math.min(i, N-i-1);
        L = Math.min(10, L);
        return L;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        xmin = width/50;
        xmax = width - width/50;
        //ymin = width/50 + top_pad;
        //ymax = width - width/50 + top_pad;
        ymin = width/25 - 1 + top_pad;
        ymax = width - 1 + top_pad;
        N = xmax-xmin+1;
        xs_user = new float[N];
        ys_user = new float[N];
        xs_hist = new float[N];
        ys_hist = new float[N];
        for(int i=0; i<N; i++) {
            xs_user[i] = xmin + i;
            ys_user[i] = ymax;
            xs_hist[i] = xmin + i;
            ys_hist[i] = ymax;
        }
        setMeasuredDimension(width, width+top_pad);
    }

    private void assist_drawing_beginning(){

        int dist = 70;
        if(smooth_ix_start>dist) return;

        boolean flag = false;
        float deriv;
        float max_deriv = 0;
        int i_start = smooth_ix_start;
        for(int i=smooth_ix_start; i<dist; i++){
            deriv = ys_user[i+1]-ys_user[i];
            if(deriv>max_deriv) max_deriv = deriv;

            if(ys_user[i]<ymax) {
                flag = true;
            }
        }

        if(flag==false) return;

        float lim_deriv = ys_user[i_start]/(i_start);
        max_deriv = Math.min(lim_deriv, max_deriv);

        for(int i=0; i<i_start; i++) {
            //ys_user[i] = y_float_to_pixel((i-i_start)*max_deriv + ys_user[i_start]);
            ys_user[i] = (i-i_start)*max_deriv + ys_user[i_start];
        }
        smooth_ix_start = xmin;

        return;
    }

    private void assist_drawing_ending(){
        int dist = 50;
        if(smooth_ix_end<N-dist) return;

        for(int i=smooth_ix_end; i<N; i++)
            ys_user[i] = ys_user[smooth_ix_end];
        smooth_ix_end = N-1;

        return;
    }

    private int x_float_to_pixel(float x){
        int f = Math.round(x);
        if(f<xmin)
            f = xmin;
        if(f>xmax)
            f = xmax;
        return f;
    }

    private int y_float_to_pixel(float y){
        int f = Math.round(y);
        if(f<ymin)
            f = ymin;
        if(f>ymax)
            f = ymax;
        return f;
    }

    private float bound_y(float y){
        float f = y;
        if(f<ymin)
            f = ymin;
        if(f>ymax)
            f = ymax;
        return f;
    }


    static int myi=0;
    public Skier mySkier;
    public void makeAnimation(Skier s){
        mySkier = s;
        runSimulation = true;
        myi = 0;
        for(int i=0; i<mySkier.xs_frames.size(); i++) {
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    //buttons[inew][jnew].setBackgroundColor(Color.BLACK);
                    myi = myi+1;
                    invalidate();
                    if(myi==mySkier.xs_frames.size()-1) runSimulation = false;
                }
            }, 20*i);
        }
    }

    public void undo(){
        for(int i=0; i<N; i++){
            xs_user[i] = xs_hist[i];
            ys_user[i] = ys_hist[i];
            xs_hist[i] = xmin + i;
            ys_hist[i] = ymax;
        }
        invalidate();
    }

}

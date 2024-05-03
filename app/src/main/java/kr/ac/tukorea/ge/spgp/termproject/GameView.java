package kr.ac.tukorea.ge.spgp.termproject;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;


public class GameView extends View implements Choreographer.FrameCallback {
    private static final String TAG = GameView.class.getSimpleName();
    private Activity activity;

    // Debug Helper
    private Paint borderPaint;
    private Paint fpsPaint;
    private final RectF borderRect = new RectF(0, 0, Metrics.SCREEN_WIDTH, Metrics.SCREEN_HEIGHT);

    private void initDebugObjects(){
        borderPaint = new Paint();
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(0.1f);
        borderPaint.setColor(Color.RED);

        fpsPaint = new Paint();
        fpsPaint.setColor(Color.BLUE);
        fpsPaint.setTextSize(100f);
    }

    // View Constructor
    public GameView(Context context, AttributeSet attr){
        super(context, attr);
        if(context instanceof Activity) {
            this.activity = (Activity) context;
        }

        setFullScreen(); // default behavior?

        if(BuildConfig.DEBUG){
            initDebugObjects();
        }

        initGame();
        scheduleUpdate();
    }

    // Game Objects
    public static Resources res;
    private final ArrayList<IGameObject> gameObjects = new ArrayList<>();

    private void initGame(){
        res = getResources();
    }

    // Game Loop
    private long previousNanos = 0;
    private float elapsedSeconds;
    private void scheduleUpdate() {
        Choreographer.getInstance().postFrameCallback(this);
    }

    @Override
    public void doFrame(long nanos) {
        long elapsedNanos = nanos - previousNanos;
        elapsedSeconds = elapsedNanos / 1_000_000_000f;
        if (previousNanos != 0) {
            update();
        }
        invalidate();
        if (isShown()) {
            scheduleUpdate();
        }
        previousNanos = nanos;
    };

    private void update() {
        Scene scene = Scene.top();
        scene.update(elapsedSeconds);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        Metrics.concat(canvas);
        if(BuildConfig.DEBUG) {
            canvas.drawRect(borderRect, borderPaint);
        }

        Scene scene = Scene.top();
        scene.draw(canvas);
        canvas.restore();

        if (BuildConfig.DEBUG) {
            int fps = (int) (1.0f / elapsedSeconds);
            canvas.drawText("FPS: " + fps, 100f, 200f, fpsPaint);
        }
    }

    // Utilities
    public void setFullScreen() {
        int flags = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        setSystemUiVisibility(flags);
    }

    public Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }


    // coordinate System


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        Metrics.onSize(w, h);
    }


    // Touch Event
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = Scene.top().onTouch(event);
        if (handled) return true;
        return super.onTouchEvent(event);
    }
}

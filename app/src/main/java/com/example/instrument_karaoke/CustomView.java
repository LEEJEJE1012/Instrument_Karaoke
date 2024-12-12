package com.example.instrument_karaoke;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class CustomView extends View {
    private List<DataPoint> ansPoints;
    private List<DataPoint> recPoints;

    private Paint ansPaint;
    private Paint recPaint;

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        ansPaint = new Paint();
        ansPaint.setColor(Color.BLUE);
        ansPaint.setStyle(Paint.Style.FILL);
        ansPaint.setAntiAlias(true);

        recPaint = new Paint();
        recPaint.setColor(Color.RED);
        recPaint.setStyle(Paint.Style.FILL);
        recPaint.setAntiAlias(true);
    }

    public void setPoints(List<DataPoint> ansPoints, List<DataPoint> recPoints) {
        this.ansPoints = ansPoints;
        this.recPoints = recPoints;
        invalidate(); // 다시 그리기 요청
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (ansPoints != null) {
            for (DataPoint point : ansPoints) {
                canvas.drawCircle(point.getX(), point.getY(), 20, ansPaint);
            }
        }

        if (recPoints != null) {
            for (DataPoint point : recPoints) {
                canvas.drawCircle(point.getX(), point.getY(), 10, recPaint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 1000;  // 기본 너비 (예제)
        int desiredHeight = 500;  // 기본 높이 (예제)

        // 데이터 포인트에 기반하여 동적 너비 설정
        if (ansPoints != null && !ansPoints.isEmpty()) {
            for (DataPoint point : ansPoints) {
                desiredWidth = Math.max(desiredWidth, (int) point.getX() + 100);
                desiredHeight = Math.max(desiredHeight, (int) point.getY() + 100);
            }
        }

        if (recPoints != null && !recPoints.isEmpty()) {
            for (DataPoint point : recPoints) {
                desiredWidth = Math.max(desiredWidth, (int) point.getX() + 100);
                desiredHeight = Math.max(desiredHeight, (int) point.getY() + 100);
            }
        }
        setMeasuredDimension(desiredWidth, desiredHeight);
    }
}


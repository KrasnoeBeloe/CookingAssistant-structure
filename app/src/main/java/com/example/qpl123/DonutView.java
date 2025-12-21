package com.example.qpl123;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class DonutView extends View {

    private Paint donutPaint;
    private Paint glazePaint;
    private Paint sprinklePaint;
    private RectF donutRect;
    private float donutSize = 1.0f;
    private float rotation = 0f;
    private int progress = 0;

    public DonutView(Context context) {
        super(context);
        init();
    }

    public DonutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Краска для пончика
        donutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        donutPaint.setColor(Color.parseColor("#FFC107")); // Желтый
        donutPaint.setStyle(Paint.Style.FILL);

        // Краска для глазури
        glazePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glazePaint.setColor(Color.parseColor("#FF4081")); // Розовый
        glazePaint.setStyle(Paint.Style.STROKE);
        glazePaint.setStrokeWidth(30);

        // Краска для посыпки
        sprinklePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sprinklePaint.setColor(Color.parseColor("#4CAF50")); // Зеленый
        sprinklePaint.setStyle(Paint.Style.FILL);

        donutRect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;

        // Сохраняем состояние canvas для вращения
        canvas.save();
        canvas.rotate(rotation, centerX, centerY);

        // Размер пончика в зависимости от прогресса
        float currentSize = 200 * donutSize;

        // Отрисовываем пончик
        donutRect.set(
                centerX - currentSize / 2,
                centerY - currentSize / 2,
                centerX + currentSize / 2,
                centerY + currentSize / 2
        );

        // Основа пончика
        canvas.drawOval(donutRect, donutPaint);

        // Глазурь
        canvas.drawOval(donutRect, glazePaint);

        // Дырка в пончике
        canvas.drawCircle(centerX, centerY, currentSize * 0.3f, donutPaint);

        // Посыпка
        canvas.drawCircle(centerX - currentSize * 0.3f, centerY - currentSize * 0.3f, 8, sprinklePaint);
        canvas.drawCircle(centerX + currentSize * 0.3f, centerY - currentSize * 0.3f, 8, sprinklePaint);
        canvas.drawCircle(centerX - currentSize * 0.3f, centerY + currentSize * 0.3f, 8, sprinklePaint);
        canvas.drawCircle(centerX + currentSize * 0.3f, centerY + currentSize * 0.3f, 8, sprinklePaint);

        // Восстанавливаем canvas
        canvas.restore();
    }

    public void setDonutSize(float size) {
        this.donutSize = size;
        invalidate();
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
        invalidate();
    }

    public void setProgress(int progress) {
        this.progress = progress;
        // Размер пончика зависит от прогресса
        donutSize = 1.0f - (progress / 100f);
        invalidate();
    }

    public void startEatingAnimation(int duration) {
        // Анимация уменьшения размера
        ValueAnimator sizeAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
        sizeAnimator.setDuration(duration);
        sizeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        sizeAnimator.addUpdateListener(animation -> {
            setDonutSize((float) animation.getAnimatedValue());
        });
        sizeAnimator.start();

        // Анимация вращения
        ValueAnimator rotationAnimator = ValueAnimator.ofFloat(0f, 720f);
        rotationAnimator.setDuration(duration);
        rotationAnimator.addUpdateListener(animation -> {
            setRotation((float) animation.getAnimatedValue());
        });
        rotationAnimator.start();
    }
}
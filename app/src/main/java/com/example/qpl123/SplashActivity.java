package com.example.qpl123;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class SplashActivity extends AppCompatActivity {

    private ImageView donutImageView;
    private TextView progressTextView;
    private TextView loadingTextView;
    private ProgressBar loadingProgressBar;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Инициализация элементов
        donutImageView = findViewById(R.id.donutImageView);
        progressTextView = findViewById(R.id.progressTextView);
        loadingTextView = findViewById(R.id.loadingTextView);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        // Случайное время загрузки от 4 до 7 секунд
        int loadingTime = 4000 + random.nextInt(3001); // 4000-7000 мс

        // Запуск анимации загрузки
        startLoadingAnimation(loadingTime);

        // Через loadingTime миллисекунд переходим на MainActivity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }, loadingTime);
    }

    private void startLoadingAnimation(int duration) {
        // 1. Анимация съедания пончика (уменьшение размера)
        ValueAnimator donutAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
        donutAnimator.setDuration(duration);
        donutAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        donutAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            donutImageView.setScaleX(scale);
            donutImageView.setScaleY(scale);

            // Прозрачность тоже уменьшаем
            donutImageView.setAlpha(scale);
        });
        donutAnimator.start();

        // 2. Анимация процентов (0-100%)
        ValueAnimator progressAnimator = ValueAnimator.ofInt(0, 100);
        progressAnimator.setDuration(duration);
        progressAnimator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            progressTextView.setText(progress + "%");

            // Обновляем ProgressBar
            loadingProgressBar.setProgress(progress);

            // Меняем цвет текста в зависимости от прогресса
            if (progress < 30) {
                progressTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else if (progress < 70) {
                progressTextView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                progressTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }

        });
        progressAnimator.start();

        // 3. Анимация вращения пончика
        ValueAnimator rotationAnimator = ValueAnimator.ofFloat(0f, 720f);
        rotationAnimator.setDuration(duration);
        rotationAnimator.addUpdateListener(animation -> {
            float rotation = (float) animation.getAnimatedValue();
            donutImageView.setRotation(rotation);
        });
        rotationAnimator.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Приостанавливаем анимации при паузе
        donutImageView.clearAnimation();
    }
}
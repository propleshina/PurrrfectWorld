package com.example.purrrfectworld;

import com.example.purrrfectworld.R;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.FrameLayout;
import androidx.appcompat.app.AlertDialog;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity {

    private FrameLayout rootLayout;

    // Элементы интерфейса
    private View gameOverlay; // Основное окно игры
    private TextView storyTextView; // Текст истории
    private ImageView avatarImageView; // Аватар персонажа
    private TextView nameTextView; // Имя персонажа

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Создаем корневой layout программно для гибкости
        rootLayout = new FrameLayout(this);
        setContentView(rootLayout);

        initGameWindow();
    }

    private void initGameWindow() {
        // Фон игры (можно заменить на изображение)
        ImageView background = new ImageView(this);
        background.setImageResource(R.drawable.background_main); // замените на свой ресурс
        background.setScaleType(ImageView.ScaleType.CENTER_CROP);
        rootLayout.addView(background, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        // Основное окно с текстом внизу (прямоугольник)
        LinearLayout storyBox = new LinearLayout(this);
        storyBox.setOrientation(LinearLayout.VERTICAL);
        storyBox.setBackgroundColor(Color.parseColor("#AA000000")); // полупрозрачный черный фон
        int margin = dpToPx(16);
        FrameLayout.LayoutParams storyParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dpToPx(150));
        storyParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        storyParams.setMargins(margin, margin, margin, margin);
        rootLayout.addView(storyBox, storyParams);

        // Текст истории внутри storyBox
        storyTextView = new TextView(this);
        storyTextView.setTextColor(Color.WHITE);
        storyTextView.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        storyTextView.setTextSize(16f);
        storyTextView.setText("Начальный текст истории...");
        storyBox.addView(storyTextView);

        // Имя персонажа сверху справа от окна (над текстом)
        nameTextView = new TextView(this);
        nameTextView.setText("Имя");
        nameTextView.setTextColor(Color.WHITE);
        nameTextView.setBackgroundColor(Color.parseColor("#88000000"));
        nameTextView.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));

        FrameLayout.LayoutParams nameParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        nameParams.topMargin = dpToPx(20);
        nameParams.leftMargin = dpToPx(20);
        nameParams.gravity = Gravity.TOP | Gravity.START;

        rootLayout.addView(nameTextView, nameParams);

        // Аватар персонажа посередине снизу над текстом (можно заменить на изображение)
        avatarImageView = new ImageView(this);
        avatarImageView.setImageResource(R.drawable.avatar_happypunk); // замените на свой ресурс

        int avatarSize = dpToPx(100);

        FrameLayout.LayoutParams avatarParams = new FrameLayout.LayoutParams(
                avatarSize,
                avatarSize);

        avatarParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

        // Смещение вверх чтобы не перекрывать текст окна (поднимаем чуть вверх)
        avatarParams.bottomMargin = dpToPx(150);

        rootLayout.addView(avatarImageView, avatarParams);

        // Верхние кнопки (дом и карандаш)
        LinearLayout topButtonsContainer = new LinearLayout(this);
        topButtonsContainer.setOrientation(LinearLayout.HORIZONTAL);

        ImageButton homeBtn = new ImageButton(this);
        homeBtn.setImageResource(R.drawable.home); // добавьте свои ресурсы
        homeBtn.setBackground(null);

        ImageButton pencilBtn = new ImageButton(this);
        pencilBtn.setImageResource(R.drawable.pencil);
        pencilBtn.setBackground(null);

        topButtonsContainer.addView(homeBtn, new LinearLayout.LayoutParams(dpToPx(48), dpToPx(48)));
        topButtonsContainer.addView(pencilBtn, new LinearLayout.LayoutParams(dpToPx(48), dpToPx(48)));

        FrameLayout.LayoutParams topBtnsParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.START);
        topBtnsParams.topMargin = dpToPx(16);
        topBtnsParams.leftMargin = dpToPx(16);

        rootLayout.addView(topButtonsContainer, topBtnsParams);

        homeBtn.setOnClickListener(v -> finish()); // возвращает на главный экран

        pencilBtn.setOnClickListener(v -> showSaveNameDialog());

        // Кнопка автопроигрывания текста (справа сверху)
        Button autoPlayBtn = new Button(this);
        autoPlayBtn.setText("▶");

        FrameLayout.LayoutParams autoPlayParam = new FrameLayout.LayoutParams(
                dpToPx(48),
                dpToPx(48),
                Gravity.TOP | Gravity.END);
        autoPlayParam.topMargin = dpToPx(16);
        autoPlayParam.rightMargin = dpToPx(16);

        rootLayout.addView(autoPlayBtn, autoPlayParam);

        autoPlayBtn.setOnClickListener(v -> startAutoStory());

        // Изначально запускаем рассказ или ждём по событию.
        startStory();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void startAutoStory() {
        // Например, автоматическая прокрутка текста истории
        // Можно реализовать, например, запуск последовательности displayStory с автоматической задержкой
        String[] storyLines = {"Это первая строка.", "Это вторая.", "Выбор?", "Конец."};
        displayStoryAuto(storyLines, 0);
    }

    // Вспомогательный метод для автоматической прокрутки
    private void displayStoryAuto(String[] lines, int index) {
        if (index >= lines.length) {
            return; // завершение
        }
        storyText(lines[index]);
        handler.postDelayed(() -> displayStoryAuto(lines, index + 1), 2000); // задержка между строками
    }

    private void startStory() {
        String[] storyLines = {"Это первая строка.", "Это вторая.", "Выбор?", "Конец."};
        displayStory(storyLines, 0, new Runnable() {
            public void run() {
                showChoices(new String[]{"Выбор 1", "Выбор 2"}, choices -> {
                    if (choices == 0) {
                        storyText("Вы выбрали первый вариант");
                    } else {
                        storyText("Вы выбрали второй вариант");
                    }
                });
            }
        });
    }

    private void displayStory(String[] lines, int index, Runnable onComplete) {
        if (index >= lines.length) {
            if (onComplete != null) onComplete.run();
            return;
        }

        storyText(lines[index]);

        handler.postDelayed(() -> displayStory(lines, index + 1, onComplete), 2000); // задержка между строками
    }

    private void showSaveNameDialog() {
        // Создаем диалог для ввода имени
        final EditText input = new EditText(this);
        input.setHint("Введите имя");

        new AlertDialog.Builder(this)
                .setTitle("Сохранить имя")
                .setView(input)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String name = input.getText().toString();
                    // Обработка введенного имени
                    saveName(name);
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // Метод для обработки сохранения имени (можете реализовать по своему)
    private void saveName(String name) {
        // Например, сохранить в SharedPreferences или вывести сообщение
        Toast.makeText(this, "Имя сохранено: " + name, Toast.LENGTH_SHORT).show();
    }

    private void storyText(String text) {
        storyTextView.setText(text);
    }

    // Определение интерфейса ChoiceCallback
    public interface ChoiceCallback {
        void onChoice(int choiceIndex);
    }

    private void showChoices(String[] options, ChoiceCallback callback) {
        // Создаем контейнер для кнопок выбора
        LinearLayout choicesLayout = new LinearLayout(this);
        choicesLayout.setOrientation(LinearLayout.VERTICAL);
        choicesLayout.setBackgroundColor(Color.parseColor("#CCFFFFFF")); // полупрозрачный фон
        int padding = dpToPx(16);
        choicesLayout.setPadding(padding, padding, padding, padding);

        // Создаем диалоговое окно с выбором
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Выберите вариант");

        // Создаем массив названий вариантов для отображения
        builder.setItems(options, (dialog, which) -> {
            // вызов коллбека с индексом выбранного варианта
            callback.onChoice(which);
            dialog.dismiss();
        });

        // Показываем диалог
        builder.show();
    }
}
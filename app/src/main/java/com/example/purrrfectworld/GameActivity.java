package com.example.purrrfectworld;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GameActivity extends AppCompatActivity {

    private Map<String, List<String>> dialogBranches = new HashMap<>();
    private String currentBranch = "main";
    private int branchLineIndex = 0;
    private boolean waitingForChoice = false;

    private static final String PREFS_NAME = "GameSavePrefs";
    private static final String KEY_SCROLL_SPEED     = "scroll_speed";
    private static final String KEY_CURRENT_INDEX    = "currentIndex";
    private static final String KEY_CURRENT_BACKGROUND = "currentBackground";

    private ImageView backgroundImageView;
    private ImageView characterIcon;
    private TextView  characterName;
    private TextView  storyTextView;
    private Button    autoPlayButton;

    private Handler handler = new Handler(Looper.getMainLooper());

    private String[] dialogLines = new String[0];
    private int      currentLineIndex = 0;

    private boolean isAutoPlay      = false;
    private boolean isWaitingForClick = false;

    // Задержка между символами в мс
    private long charDelayMs = 50L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        boolean reset = getIntent().getBooleanExtra("RESET_PROGRESS", false);
        if (reset) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit()
                    .remove(KEY_CURRENT_INDEX)
                    .remove(KEY_CURRENT_BACKGROUND)
                    .apply();

            currentLineIndex = 0;
        }

        backgroundImageView = findViewById(R.id.backgroundImage);
        characterIcon       = findViewById(R.id.avatarImageView);
        characterName       = findViewById(R.id.nameTextView);
        storyTextView       = findViewById(R.id.storyTextView);
        autoPlayButton      = findViewById(R.id.autoPlayButton);

        // Загрузка сохранённой скорости (0..100)
        SharedPreferences appPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        int savedPercent = appPrefs.getInt("Scroll_speed", 50);
        int savedFontSize = appPrefs.getInt("font_size", 14);
        storyTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, savedFontSize);
        int intentSpeed = getIntent().getIntExtra("SCROLL_SPEED", savedPercent);
        updateCharDelay(intentSpeed);

        loadDialogFromFile();
        if (!reset) {
            loadProgress();
        } else {
            showNextLine();
        }

        ImageButton homeBtn = findViewById(R.id.homeButton);
        if (homeBtn != null) {
            homeBtn.setOnClickListener(v -> {
                playClickSound();
                startActivity(new Intent(this, MainActivity.class));
            });
        }

        ImageButton pencilBtn = findViewById(R.id.pencilButton);
        if (pencilBtn != null) {
            pencilBtn.setOnClickListener(v -> {
                playClickSound();
                showSaveDialog();
            });
        }

        autoPlayButton.setOnClickListener(v -> {
            playClickSound();
            isAutoPlay = !isAutoPlay;
            if (isAutoPlay) {
                autoPlayButton.setText("⏸️");
                showNextLine();
            } else {
                autoPlayButton.setText("▶️");
                handler.removeCallbacksAndMessages(null);
            }
        });

        storyTextView.setOnClickListener(v -> {
            if (isWaitingForClick) {
                isWaitingForClick = false;
                handler.removeCallbacksAndMessages(null);
                showNextLine();
            }
        });
    }

    /** Пересчитывает charDelayMs из процента скорости (0..100) */
    private void updateCharDelay(int speedPercent) {
        // 0→20ms, 100→220ms
        charDelayMs = 220L - speedPercent * 2L;
    }

    private void playClickSound() {
        MediaPlayer clickSound = MediaPlayer.create(this, R.raw.click);
        clickSound.setVolume(1.0f, 1.0f); // максимальная громкость
        clickSound.setOnCompletionListener(mp -> {
            mp.release(); // освобождаем ресурсы после воспроизведения
        });
        clickSound.start();
    }

    private void loadDialogFromFile() {
        AssetManager am = getAssets();
        dialogBranches.clear();
        List<String> currentLines = new ArrayList<>();
        String branch = "main";

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(am.open("dialog.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("<<")) {
                    currentLines.add(line.trim());
                } else if (line.trim().startsWith("<") && !line.trim().startsWith("<end>")) {
                    dialogBranches.put(branch, currentLines);
                    branch = line.trim().replaceAll("[<>]", "");
                    currentLines = new ArrayList<>();
                } else if (line.trim().startsWith("<end>")) {
                    currentLines.add("<end>");
                    dialogBranches.put(branch, currentLines);
                } else {
                    currentLines.add(line.trim());
                }
            }
            dialogBranches.put(branch, currentLines); // Последняя ветка
        } catch (IOException e) {
            storyTextView.setText("Ошибка загрузки диалога");
        }
    }

    private void showNextLine() {
        List<String> lines = dialogBranches.get(currentBranch);
        if (lines == null || branchLineIndex >= lines.size()) {
            return;
        }

        String line = lines.get(branchLineIndex++);
        if (line.startsWith("<<")) {
            showChoices(line);
        } else if (line.equals("<end>")) {
            storyTextView.setText("Конец игры");
            Toast.makeText(this, "Поздравляем! Вы прошли игру!", Toast.LENGTH_LONG).show();
            showEndButton();
        } else {
            parseAndDisplayLine(line);
        }
    }

    private void showEndButton() {
        LinearLayout choicesLayout = findViewById(R.id.choicesLayout);
        choicesLayout.setVisibility(View.VISIBLE);
        choicesLayout.removeAllViews();

        Button endBtn = new Button(this);
        endBtn.setText("Выйти в главное меню");
        endBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        choicesLayout.addView(endBtn);
    }

    private void showChoices(String line) {
        isWaitingForClick = false;
        isAutoPlay = false;
        handler.removeCallbacksAndMessages(null);
        storyTextView.setText("");

        LinearLayout choicesLayout = findViewById(R.id.choicesLayout);
        choicesLayout.removeAllViews();
        choicesLayout.setVisibility(View.VISIBLE);

        Pattern pattern = Pattern.compile("<(.*?)>");
        Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String choiceText = matcher.group(1);
            Button choiceBtn = new Button(this);
            choiceBtn.setText(choiceText);
            choiceBtn.setOnClickListener(v -> {
                playClickSound();
                currentBranch = choiceText;
                branchLineIndex = 0;
                choicesLayout.setVisibility(View.GONE);
                showNextLine();
            });
            choicesLayout.addView(choiceBtn);
        }
    }

    private void parseAndDisplayLine(String line) {
        String[] parts = line.split(";", 4);
        if (parts.length < 4) return;

        // Фон
        String bg = parts[0].trim();
        if (!bg.equals("-") && !bg.isEmpty()) {
            int resId = getResources()
                    .getIdentifier(bg.replace(".png",""), "drawable", getPackageName());
            if (resId != 0) backgroundImageView.setImageResource(resId);
        }

        // Текст диалога
        String dialogue = parts[1].trim();
        dialogue = dialogue.replaceAll("^\\(|\\)$", "");

        // Иконка
        String iconFile = parts[2].trim();
        if (!iconFile.equals("-") && !iconFile.isEmpty()) {
            int iconId = getResources()
                    .getIdentifier(iconFile.replace(".png",""), "drawable", getPackageName());
            if (iconId != 0) characterIcon.setImageResource(iconId);
        } else {
            characterIcon.setImageDrawable(null);
        }

        // Имя
        String name = parts[3].trim().replaceAll("^\\(|\\)$", "");
        if (!name.equals("-") && !name.isEmpty()) {
            characterName.setText(name);
            characterName.setVisibility(View.VISIBLE);
        } else {
            characterName.setVisibility(View.INVISIBLE);
        }

        // Анимированное появление текста
        showDialogText(dialogue, new Runnable() {
            @Override
            public void run() {
                if (isAutoPlay) {
                    showNextLine();
                }
            }
        });
    }

    /**
     * Отображает строку по символам с задержкой charDelayMs.
     * После окончания вызывает onComplete.
     */
    private void showDialogText(final String text, final Runnable onComplete) {
        handler.removeCallbacksAndMessages(null);
        storyTextView.setText("");
        isWaitingForClick = false;

        final char[] chars = text.toCharArray();
        storyTextView.setText("");
        final Runnable[] typer = new Runnable[1];
        typer[0] = new Runnable() {
            int idx = 0;
            @Override
            public void run() {
                if (idx < chars.length) {
                    storyTextView.append(String.valueOf(chars[idx++]));
                    // всегда берём актуальную задержку
                    handler.postDelayed(this, charDelayMs);
                } else {
                    // Завершили
                    if (onComplete != null) onComplete.run();
                    if (!isAutoPlay) {
                        isWaitingForClick = true;
                    }
                }
            }
        };
        handler.post(typer[0]);
    }

    private void showSaveDialog() {
        final EditText input = new EditText(this);
        input.setHint("Название");
        new AlertDialog.Builder(this)
                .setTitle("Введите название сохранения")
                .setView(input)
                .setPositiveButton("Сохранить", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        saveGame(name);
                    } else {
                        Toast.makeText(this, "Введите название", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    private void saveGame(String saveName) {
        Intent intent = new Intent(this, SavesActivity.class);
        intent.putExtra("SAVE_NAME", saveName);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveProgress();
    }

    private void saveProgress() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit()
                .putInt(KEY_CURRENT_INDEX, currentLineIndex)
                .apply();
    }

    private void loadProgress() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        currentLineIndex = prefs.getInt(KEY_CURRENT_INDEX, 0);
        String bg = prefs.getString(KEY_CURRENT_BACKGROUND, "");
        if (!bg.isEmpty()) {
            int resId = getResources().getIdentifier(bg.replace(".png", ""), "drawable", getPackageName());
            if (resId != 0) backgroundImageView.setImageResource(resId);
        }
        // НЕ вызываем здесь showNextLine()
    }
}
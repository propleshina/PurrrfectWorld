package com.example.purrrfectworld;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GameActivity extends AppCompatActivity {

    private ImageView backgroundImageView;
    private ImageView characterIcon;
    private TextView characterName;
    private TextView dialogueText;

    private TextView storyTextView;
    private Button autoPlayButton;

    private Handler handler = new Handler();

    private String[] dialogues;
    private int currentIndex = 0;
    private String currentBackground = "";

    private boolean isAutoPlay = false;
    private boolean isWaitingForClick = false;

    private String[] dialogLines;
    private int currentLineIndex = 0;

    private static final String PREFS_NAME = "GameSavePrefs";
    private static final String KEY_CURRENT_INDEX = "currentIndex";
    private static final String KEY_CURRENT_BACKGROUND = "currentBackground";
    private static final String SCROLL_SPEED_KEY = "scroll_speed";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        backgroundImageView = findViewById(R.id.backgroundImage);
        characterIcon = findViewById(R.id.avatarImageView);
        characterName = findViewById(R.id.nameTextView);
        dialogueText = findViewById(R.id.storyTextView);

        storyTextView = findViewById(R.id.storyTextView);
        autoPlayButton = findViewById(R.id.autoPlayButton);

        loadDialogFromFile();
        loadProgress();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int scrollSpeed = prefs.getInt(SCROLL_SPEED_KEY, 50);

        setupScrollSpeed(scrollSpeed);

        ImageButton homeButton = findViewById(R.id.homeButton);
        if (homeButton != null) {
            homeButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            });
        }

        ImageButton pencilButton = findViewById(R.id.pencilButton);
        if (pencilButton != null) {
            pencilButton.setOnClickListener(v -> showSaveDialog());
        }

        autoPlayButton.setOnClickListener(v -> {
            isAutoPlay = !isAutoPlay;
            if (isAutoPlay) {
                autoPlayButton.setText("⏸");
                showNextLine();
            } else {
                autoPlayButton.setText("▶");
                handler.removeCallbacksAndMessages(null);
            }
        });

        dialogueText.setOnClickListener(v -> showNextDialogue());

        storyTextView.setOnClickListener(v -> {
            if (isWaitingForClick) {
                showNextLine();
            }
        });

        loadDialogFromFile();
    }

    private void setupScrollSpeed(int speed) {
        final int delay = 100 - speed; // Преобразуем скорость в миллисекунды
        if (delay < 10) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isAutoPlay) {
                        showNextLine(); // Показываем следующий диалог
                        handler.postDelayed(this, 10); // Устанавливаем минимальную задержку
                    }
                }
            }, 10);
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isAutoPlay) {
                        showNextLine(); // Показываем следующий диалог
                        handler.postDelayed(this, delay); // Устанавливаем следующую задержку
                    }
                }
            }, delay);
        }
    }

    private void loadDialogFromFile() {
        AssetManager assetManager = getAssets();
        try (InputStream is = assetManager.open("dialog.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            String fullText = sb.toString().trim();

            dialogLines = fullText.split("\\n");
            currentLineIndex = 0;

            showNextLine();

        } catch (IOException e) {
            e.printStackTrace();
            storyTextView.setText("Ошибка загрузки диалога");
        }
    }

    private void showNextLine() {
        if (dialogLines == null || currentLineIndex >= dialogLines.length) {
            storyTextView.setText("Конец диалога");
            return;
        }

        final String lineToShow = dialogLines[currentLineIndex];
        currentLineIndex++;

        parseAndDisplayLine(lineToShow);

        if (isAutoPlay) {
            handler.postDelayed(() -> showNextLine(), 1500);
        }
    }

    public void showDialogText(String text, Runnable onComplete) {
        storyTextView.setText(text);

        if (isAutoPlay) {
            isWaitingForClick = false;
            if (onComplete != null) onComplete.run();

        } else {
            isWaitingForClick = true;

            storyTextView.setOnClickListener(v -> {
                if (isWaitingForClick) {
                    isWaitingForClick = false;
                    if (onComplete != null) onComplete.run();
                    showNextLine();
                }
            });
        }
    }

    private void showSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Введите название сохранения");
        final EditText input = new EditText(this);
        input.setHint("Название");
        builder.setView(input);
        builder.setPositiveButton("Сохранить", (dialog, which) -> {
            String saveName = input.getText().toString().trim();
            if (!saveName.isEmpty()) {
                saveGame(saveName);
            } else {
                Toast.makeText(this, "Пожалуйста, введите название", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void saveGame(String saveName) {
        Intent intent = new Intent(this, SavesActivity.class);
        intent.putExtra("SAVE_NAME", saveName);
        startActivity(intent);
    }

    private void showNextDialogue() {
        if (currentIndex >= dialogues.length) {
            dialogueText.setText("Конец диалога");
            return;
        }
    }

    private void parseAndDisplayLine(String line) {
        String[] parts = line.split(";", -1);

        if (parts.length < 4) return;

        String backgroundFile = parts[0].trim();

        if (!backgroundFile.equals("-") && !backgroundFile.isEmpty()) {
            int resId = getResources().getIdentifier(
                    backgroundFile.replace(".png", ""), "drawable", getPackageName());
            if (resId != 0) {
                backgroundImageView.setImageResource(resId);
                currentBackground = backgroundFile; // сохраняем текущий фон
            }
        } else {
            currentBackground = "";
        }

        String dialoguePart = parts[1].trim();
        String iconFile = parts[2].trim();
        String characterNamePart = parts[3].trim();

        if (!characterNamePart.equals("-") && !characterNamePart.isEmpty()) {
            String characterNameStr = characterNamePart.replaceAll("[()]", "").trim();
            characterName.setText(characterNameStr);
            characterName.setVisibility(View.VISIBLE);
        } else {
            characterName.setVisibility(View.INVISIBLE);
        }

        if (!iconFile.equals("-") && !iconFile.isEmpty()) {
            int resIdIcon = getResources().getIdentifier(
                    iconFile.replace(".png", ""), "drawable", getPackageName());
            if (resIdIcon != 0) {
                characterIcon.setImageResource(resIdIcon);
            }
        } else {
            characterIcon.setImageResource(0);
        }

        String dialogueTextStr = "";
        if (!dialoguePart.equals("-") && !dialoguePart.isEmpty()) {
            dialogueTextStr = dialoguePart.replaceAll("[()]", "").trim();
            showDialogText(dialogueTextStr, null);
        } else {
            showDialogText("", null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveProgress();}

        private void saveProgress () {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_CURRENT_INDEX, currentLineIndex);
            editor.putString(KEY_CURRENT_BACKGROUND, currentBackground);
            editor.apply();
        }
        private void loadProgress () {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            currentLineIndex = prefs.getInt(KEY_CURRENT_INDEX, 0);
            currentBackground = prefs.getString(KEY_CURRENT_BACKGROUND, "");

            if (!currentBackground.isEmpty()) {
                int resId = getResources().getIdentifier(
                        currentBackground.replace(".png", ""), "drawable", getPackageName());
                if (resId != 0) {
                    backgroundImageView.setImageResource(resId);
                }
            }
            showNextLine();
        }
    }
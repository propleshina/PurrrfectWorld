package com.example.purrrfectworld;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ContextThemeWrapper;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    private MediaPlayer musicPlayer;
    private String currentMusic = "";

    private Handler handler = new Handler(Looper.getMainLooper());

    private String[] dialogLines = new String[0];
    private int      currentLineIndex = 0;

    private boolean isAutoPlay      = false;
    private boolean isWaitingForClick = false;
    private boolean isTyping = false;
    private String currentFullLine = "";
    private String pendingSaveName = null;

    // Задержка между символами в мс
    private long charDelayMs = 50L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        View rootLayout = findViewById(R.id.rootLayout);

        rootLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                View clickedView = getTouchedView(rootLayout, (int) event.getX(), (int) event.getY());

                // Игнорируем кнопки
                if (clickedView instanceof Button || clickedView instanceof ImageButton) {
                    return false;
                }

                // 1. Если печатает — сразу показать весь текст
                if (isTyping) {
                    handler.removeCallbacksAndMessages(null);
                    storyTextView.setText(currentFullLine);
                    isTyping = false;
                    isWaitingForClick = true;
                    return true;
                }

                // 2. Если ждём — показать следующую строку
                if (isWaitingForClick) {
                    isWaitingForClick = false;
                    handler.removeCallbacksAndMessages(null);
                    showNextLine();
                    return true;
                }
            }
            return false;
        });

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

        Intent intent = getIntent();
        String passedBranch = intent.getStringExtra("CURRENT_BRANCH");
        int passedIndex = intent.getIntExtra("CURRENT_INDEX", -1);

        if (!reset && passedBranch != null && passedIndex >= 0) {
            currentBranch = passedBranch;
            branchLineIndex = passedIndex;
            showNextLine();
        } else if (reset) {
            currentBranch = "main";
            branchLineIndex = 0;
            showNextLine();
        } else {
            loadProgress();
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

            if (isTyping) {
                handler.removeCallbacksAndMessages(null);
                storyTextView.setText(currentFullLine);
                isTyping = false;
                isWaitingForClick = true;
                return;
            }

            isAutoPlay = !isAutoPlay;
            autoPlayButton.setText(isAutoPlay ? "⏸️" : "▶️");

            if (isAutoPlay) {
                showNextLine();
            } else {
                handler.removeCallbacksAndMessages(null);
                isWaitingForClick = true;
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

    private void updateCharDelay(int speedPercent) {
        charDelayMs = 220L - speedPercent * 2L;
        if (charDelayMs < 20L) charDelayMs = 20L;
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

        String currentBranch = "main";
        List<String> currentLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(am.open("dialog.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.matches("^<<.*>>$")) {
                    currentLines.add(line);
                } else if (line.matches("^<[^<>]+>$") && !line.equalsIgnoreCase("<end>")) {
                    // Новая ветка: обязательно сохраняем предыдущую
                    if (!currentLines.isEmpty()) {
                        dialogBranches.put(currentBranch, new ArrayList<>(currentLines));
                    }
                    currentBranch = line.replaceAll("[<>]", "").trim();
                    currentLines.clear();
                } else if (line.equalsIgnoreCase("<end>")) {
                    currentLines.add("<end>");
                    dialogBranches.put(currentBranch, new ArrayList<>(currentLines));
                    currentLines.clear();
                } else {
                    currentLines.add(line);
                }
            }

            if (!dialogBranches.containsKey(currentBranch) && !currentLines.isEmpty()) {
                dialogBranches.put(currentBranch, currentLines);
            }

        } catch (IOException e) {
            storyTextView.setText("Ошибка чтения файла диалога");
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

    private View getTouchedView(View parent, int x, int y) {
        if (!(parent instanceof ViewGroup)) return parent;

        ViewGroup group = (ViewGroup) parent;
        for (int i = group.getChildCount() - 1; i >= 0; i--) {
            View child = group.getChildAt(i);
            if (child.getVisibility() != View.VISIBLE) continue;

            int[] location = new int[2];
            child.getLocationOnScreen(location);
            int childX = location[0];
            int childY = location[1];

            if (x >= childX && x <= (childX + child.getWidth())
                    && y >= childY && y <= (childY + child.getHeight())) {
                return getTouchedView(child, x, y);
            }
        }
        return parent;
    }


    private void showEndButton() {
        LinearLayout choicesLayout = findViewById(R.id.choicesLayout);
        choicesLayout.setVisibility(View.VISIBLE);
        choicesLayout.removeAllViews();

        Button endBtn = new Button(new ContextThemeWrapper(this, R.style.ChoiceButtonStyle));
        endBtn.setText("Выйти в главное меню");
        endBtn.setBackgroundColor(Color.parseColor("#80000000"));
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
            String choiceText = matcher.group(1).trim().replaceAll("^<|>$", "");
            ContextThemeWrapper themedContext = new ContextThemeWrapper(this, R.style.ChoiceButtonStyle);
            Button choiceBtn = new Button(themedContext);
            choiceBtn.setText(choiceText);
            choiceBtn.setOnClickListener(v -> {
                playClickSound();
                if (dialogBranches.containsKey(choiceText)) {
                    currentBranch = choiceText;
                    branchLineIndex = 0;
                    choicesLayout.setVisibility(View.GONE);
                    showNextLine();
                } else {
                    Toast.makeText(this, "Ветка \"" + choiceText + "\" не найдена", Toast.LENGTH_SHORT).show();
                }
            });
            choicesLayout.addView(choiceBtn);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 16, 0, 16);
            choiceBtn.setLayoutParams(params);
        }
    }

    private void parseAndDisplayLine(String line) {
        String[] parts = line.split(";", 4);
        if (parts.length < 4) return;

        // Фон
        String bg = parts[0].trim();
        SharedPreferences prefs = getSharedPreferences("GameSavePrefs", MODE_PRIVATE);
        prefs.edit().putString("currentBackground", bg).apply();
        if (!bg.equals("-") && !bg.isEmpty()) {
            int resId = getResources()
                    .getIdentifier(bg.replace(".png",""), "drawable", getPackageName());
            if (resId != 0) backgroundImageView.setImageResource(resId);
            playBackgroundMusic(bg);
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
            updateNameStyle(name);
        } else {
            characterName.setVisibility(View.INVISIBLE);
        }

        // Анимированное появление текста
        showDialogText(dialogue, new Runnable() {
            @Override

            public void run() {
                isTyping=true;

                if (isAutoPlay) {
                    showNextLine();
                }
            }
        });
    }

    private void showDialogText(final String text, final Runnable onComplete) {
        handler.removeCallbacksAndMessages(null);
        storyTextView.setText("");
        isWaitingForClick = false;
        isTyping = true;
        currentFullLine = text; // ← ТУТ правильно

        final char[] chars = text.toCharArray();
        final Runnable[] typer = new Runnable[1];
        typer[0] = new Runnable() {
            int idx = 0;

            @Override
            public void run() {
                if (idx < chars.length) {
                    storyTextView.append(String.valueOf(chars[idx++]));
                    handler.postDelayed(this, charDelayMs);
                } else {
                    isTyping = false; // Печать завершена
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

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences appPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        int savedPercent = appPrefs.getInt("scroll_speed", 50);
        updateCharDelay(savedPercent);
    }



    private void saveGame(String saveName) {
        Intent intent = new Intent(this, SavesActivity.class);
        intent.putExtra("SAVE_MODE", true); // флаг: это режим сохранения
        intent.putExtra("SAVE_NAME", saveName);
        intent.putExtra("CURRENT_BRANCH", currentBranch);
        intent.putExtra("CURRENT_INDEX", branchLineIndex);
        startActivityForResult(intent, 1001);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            currentBranch = data.getStringExtra("CURRENT_BRANCH");
            branchLineIndex = data.getIntExtra("CURRENT_INDEX", 0);
            showNextLine();
        }
    }

    private void updateNameStyle(String name) {
        TextView nameTextView = findViewById(R.id.nameTextView);

        if (name == null || name.isEmpty()) {
            nameTextView.setVisibility(View.INVISIBLE);
            return;
        }

        nameTextView.setVisibility(View.VISIBLE);

        String emoji = "🐾"; // по умолчанию
        int textColor = Color.WHITE; // по умолчанию

        switch (name.toLowerCase(Locale.ROOT)) {
            case "пуньк":
                textColor = Color.parseColor("#FFB273"); // рыжий
                emoji = "🐾";
                break;

            case "петра":
                textColor = Color.parseColor("#60D5AC"); // зелёный
                emoji = "🐾";
                break;

            case "молли":
                textColor = Color.parseColor("#FB717E"); // красный
                emoji = "🐾";
                break;

            case "бабушка":
                textColor = Color.parseColor("#EB6AA3"); // розовый
                emoji = "❤️";
                break;

            default:
                textColor = Color.WHITE;
                emoji = "";
                break;
        }

        nameTextView.setTextColor(textColor);
        nameTextView.setText(emoji + " " + name);
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

    private void playBackgroundMusic(String bgName) {
        String musicKey;

        if (bgName.contains("black")) {
            musicKey = "naght";
        } else if (bgName.contains("kitchen")|| bgName.contains("punkandgrandmother"))  {
            musicKey = "kitchen";
        } else if (bgName.contains("main_room") || bgName.contains("broakenvase")) {
            musicKey = "mainroom";
        } else if (bgName.contains("okno") || bgName.contains("friends")) {
            musicKey = "okno";
        } else {
            musicKey = "default_music"; // по умолчанию
        }

        if (musicKey.equals(currentMusic)) return; // уже играет нужная

        // Остановить текущую музыку
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.release();
        }

        int resId = getResources().getIdentifier(musicKey, "raw", getPackageName());
        if (resId != 0) {
            musicPlayer = MediaPlayer.create(this, resId);
            musicPlayer.setLooping(true);
            musicPlayer.start();
            currentMusic = musicKey;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (musicPlayer != null) {
            musicPlayer.release();
            musicPlayer = null;
        }
    }
}
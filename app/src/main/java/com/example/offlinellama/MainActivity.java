package com.example.offlinellama;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.offlinellama.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatMessageAdapter adapter;
    private final LlamaModelManager modelManager = new LlamaModelManager();

    private final ActivityResultLauncher<Intent> modelPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }
                Uri modelUri = result.getData().getData();
                if (modelUri == null) {
                    return;
                }

                final int takeFlags = result.getData().getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                try {
                    getContentResolver().takePersistableUriPermission(modelUri, takeFlags);
                } catch (SecurityException ignored) {
                    // Em alguns fornecedores o acesso persistente pode não ser suportado.
                }

                appendAssistantMessage(getString(R.string.model_loading));
                String label = modelUri.getLastPathSegment();
                binding.txtModelPath.setText(getString(R.string.model_path_prefix) + " " + (label != null ? label : modelUri.toString()));
                modelManager.setModel(this, modelUri, new LlamaModelManager.ModelLoadCallback() {
                    @Override
                    public void onSuccess(@NonNull java.io.File modelFile) {
                        updateModelPath();
                        appendAssistantMessage(getString(R.string.model_loaded));
                    }

                    @Override
                    public void onError(@NonNull String errorMessage) {
                        appendAssistantMessage(errorMessage);
                        binding.txtModelPath.setText(R.string.model_not_selected);
                    }
                });
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new ChatMessageAdapter(this, messages);
        binding.listMessages.setAdapter(adapter);

        appendAssistantMessage(getString(R.string.placeholder_reply));

        binding.btnLoadModel.setOnClickListener(v -> openModelPicker());

        binding.btnSend.setOnClickListener(v -> sendPrompt());
        binding.editPrompt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendPrompt();
                return true;
            }
            return false;
        });

        updateModelPath();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        modelManager.release();
    }

    private void openModelPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_TITLE, "Meta-Llama-3-8B-Instruct-bf16-correct-pre-tokenizer-and-EOS-token-Q4_K_M.gguf");
        modelPickerLauncher.launch(Intent.createChooser(intent, getString(R.string.load_model)));
    }

    private void sendPrompt() {
        String prompt = binding.editPrompt.getText().toString().trim();
        if (TextUtils.isEmpty(prompt)) {
            return;
        }

        appendUserMessage(prompt);
        binding.editPrompt.setText("");
        binding.editPrompt.clearFocus();

        modelManager.generateResponse(this, prompt, new LlamaModelManager.ResponseCallback() {
            @Override
            public void onResult(String response) {
                appendAssistantMessage(response);
            }

            @Override
            public void onError(String errorMessage) {
                appendAssistantMessage(errorMessage == null ? getString(R.string.placeholder_reply) : errorMessage);
            }
        });
    }

    private void appendUserMessage(String message) {
        messages.add(new ChatMessage(message, true));
        adapter.notifyDataSetChanged();
        binding.listMessages.smoothScrollToPosition(messages.size() - 1);
    }

    private void appendAssistantMessage(String message) {
        messages.add(new ChatMessage(message, false));
        adapter.notifyDataSetChanged();
        binding.listMessages.smoothScrollToPosition(messages.size() - 1);
    }

    private void updateModelPath() {
        String pathLabel = modelManager.getModelPathLabel();
        if (TextUtils.isEmpty(pathLabel)) {
            binding.txtModelPath.setText(R.string.model_not_selected);
        } else {
            binding.txtModelPath.setText(getString(R.string.model_path_prefix) + " " + pathLabel);
        }
    }
}

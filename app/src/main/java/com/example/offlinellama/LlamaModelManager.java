package com.example.offlinellama;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class LlamaModelManager {

    interface ResponseCallback {
        void onResult(String response);

        void onError(String errorMessage);
    }

    interface ModelLoadCallback {
        void onSuccess(@NonNull File modelFile);

        void onError(@NonNull String errorMessage);
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Uri modelUri;
    private File cachedModel;

    @MainThread
    void setModel(@NonNull Context context, @NonNull Uri uri, @NonNull ModelLoadCallback callback) {
        this.modelUri = uri;
        cacheModelFile(context, callback);
    }

    boolean hasModelLoaded() {
        return cachedModel != null && cachedModel.exists();
    }

    @Nullable
    String getModelPathLabel() {
        if (cachedModel != null) {
            return cachedModel.getName();
        }
        if (modelUri == null) {
            return null;
        }
        String lastSegment = modelUri.getLastPathSegment();
        return lastSegment != null ? lastSegment : modelUri.toString();
    }

    void generateResponse(@NonNull Context context, @NonNull String prompt, @NonNull ResponseCallback callback) {
        if (!hasModelLoaded()) {
            if (modelUri != null) {
                callback.onError(context.getString(R.string.model_loading));
            } else {
                callback.onError(context.getString(R.string.placeholder_reply));
            }
            return;
        }

        executorService.execute(() -> {
            try {
                String response = runInference(prompt);
                mainHandler.post(() -> callback.onResult(response));
            } catch (Exception exception) {
                mainHandler.post(() -> callback.onError(exception.getMessage()));
            }
        });
    }

    private void cacheModelFile(@NonNull Context context, @NonNull ModelLoadCallback callback) {
        if (modelUri == null) {
            cachedModel = null;
            mainHandler.post(() -> callback.onError(context.getString(R.string.model_not_selected)));
            return;
        }

        executorService.execute(() -> {
            try (InputStream inputStream = context.getContentResolver().openInputStream(modelUri)) {
                if (inputStream == null) {
                    throw new IOException("Não foi possível abrir o modelo selecionado");
                }

                File cacheDir = new File(context.getFilesDir(), "models");
                if (!cacheDir.exists() && !cacheDir.mkdirs()) {
                    throw new IOException("Não foi possível criar o diretório de cache");
                }

                File modelFile = new File(cacheDir, "selected-model.gguf");
                try (FileOutputStream outputStream = new FileOutputStream(modelFile)) {
                    byte[] buffer = new byte[8 * 1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                }
                cachedModel = modelFile;
                mainHandler.post(() -> callback.onSuccess(modelFile));
            } catch (IOException e) {
                cachedModel = null;
                mainHandler.post(() -> callback.onError(e.getMessage() == null
                        ? "Falha ao copiar o modelo"
                        : e.getMessage()));
            }
        });
    }

    private String runInference(String prompt) {
        // TODO: conectar com uma ponte JNI ou biblioteca nativa que consome arquivos GGUF
        // gerados pelo llama.cpp. Este método deve abrir cachedModel e executar a inferência.
        // Enquanto a integração não for configurada, retornamos uma resposta estática que
        // confirma o recebimento do prompt.
        return "Modelo offline respondeu: " + prompt;
    }

    void release() {
        executorService.shutdownNow();
    }
}

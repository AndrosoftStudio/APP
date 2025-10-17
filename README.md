# Offline Llama Android App

Aplicativo Android em Java que oferece uma interface de chat offline para modelos compatíveis com o formato GGUF, como o `Meta-Llama-3-8B-Instruct-bf16-correct-pre-tokenizer-and-EOS-token-Q4_K_M.gguf`.

## Requisitos

- Android Studio Iguana (ou superior) com Android Gradle Plugin 8.3+
- Dispositivo ou emulador Android com Android 8.0 (API 26) ou superior
- Arquivo do modelo GGUF disponível em um diretório acessível pelo dispositivo

## Como executar

1. Abra o diretório do projeto no Android Studio.
2. Sincronize o Gradle quando solicitado.
3. Conecte um dispositivo físico ou inicie um emulador.
4. Execute o app (`Run > Run 'app'`).
5. No aplicativo, toque em **Selecionar modelo** e escolha o arquivo `Meta-Llama-3-8B-Instruct-bf16-correct-pre-tokenizer-and-EOS-token-Q4_K_M.gguf` (ou outro modelo compatível).
6. Aguarde o cache do modelo e inicie a conversa.

## Integração com a inferência

O arquivo [`LlamaModelManager`](app/src/main/java/com/example/offlinellama/LlamaModelManager.java) contém o método `runInference`, que atualmente devolve uma resposta estática. Substitua esse método pela integração real com a biblioteca nativa que fará a inferência (por exemplo, ligando com `llama.cpp` via JNI).

### Passos sugeridos para conectar com `llama.cpp`

1. Compile o `llama.cpp` para Android (armeabi-v7a, arm64-v8a) gerando uma biblioteca nativa (`.so`).
2. Adicione os binários ao diretório `app/src/main/jniLibs/<abi>/`.
3. Crie uma camada JNI em Java/Kotlin que carregue a biblioteca (`System.loadLibrary`) e exponha métodos para inicializar o modelo GGUF e gerar respostas.
4. No método `runInference`, invoque a camada JNI passando o caminho do arquivo em `cachedModel` e o prompt.
5. Utilize `ExecutorService` já fornecido para manter a inferência fora da thread principal.

## Estrutura do app

- UI baseada em `ActivityMainBinding` (View Binding) com lista de mensagens e campo de entrada.
- Persistência de acesso ao arquivo via `ACTION_OPEN_DOCUMENT` para suportar armazenamento compartilhado.
- Cache local do modelo em `filesDir/models/selected-model.gguf` para uso posterior.

## Observações

- O projeto inclui uma resposta placeholder enquanto a integração com o modelo nativo não é implementada.
- Certifique-se de ter espaço suficiente no dispositivo para copiar o arquivo GGUF para o cache interno.
- Ajuste as mensagens da UI ou traduções conforme necessário.

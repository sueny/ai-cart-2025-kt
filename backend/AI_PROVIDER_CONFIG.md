# AI Provider Configuration

This application supports three AI providers: **OpenAI**, **Claude (Anthropic)**, and **Google Gemini (Vertex AI)**.

## Switching Between Providers

The AI provider is controlled by the `AI_PROVIDER` environment variable. By default, it uses OpenAI.

### Using OpenAI (Default)

Set these environment variables:

```bash
export AI_PROVIDER=openai
export OPENAI_API_KEY=your-openai-api-key
export OPENAI_MODEL=gpt-4o  # Optional, defaults to gpt-4o
```

### Using Claude (Anthropic)

Set these environment variables:

```bash
export AI_PROVIDER=claude  # or 'anthropic'
export ANTHROPIC_API_KEY=your-anthropic-api-key
export CLAUDE_MODEL=claude-3-5-sonnet-20241022  # Optional, defaults to claude-3-5-sonnet-20241022
```

**Get your API key**: Sign up at https://console.anthropic.com/

### Using Google Gemini

1. **Set up Google Cloud credentials:**

   ```bash
   # Set the path to your service account JSON key file
   export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your-service-account-key.json
   ```

2. **Set environment variables:**

   ```bash
   export AI_PROVIDER=gemini
   export GCP_PROJECT_ID=your-gcp-project-id
   export GCP_LOCATION=us-central1  # Optional, defaults to us-central1
   export GEMINI_MODEL=gemini-1.5-pro  # Optional, defaults to gemini-1.5-pro
   ```

3. **Enable Vertex AI API in your GCP project:**

   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Navigate to "APIs & Services" > "Library"
   - Search for "Vertex AI API" and enable it

## Configuration in application.yml

The configuration is already set up in `application.yml`:

```yaml
spring:
  ai:
    provider: ${AI_PROVIDER:openai}  # Switch between 'openai' and 'gemini'

    openai:
      api-key: ${OPENAI_API_KEY:your-api-key-here}
      chat:
        options:
          model: ${OPENAI_MODEL:gpt-4o}
          temperature: 0.7

    vertex:
      ai:
        gemini:
          project-id: ${GCP_PROJECT_ID:your-project-id}
          location: ${GCP_LOCATION:us-central1}
          chat:
            options:
              model: ${GEMINI_MODEL:gemini-1.5-pro}
              temperature: 0.7
```

## Running the Application

### With OpenAI:
```bash
export AI_PROVIDER=openai
export OPENAI_API_KEY=sk-...
./gradlew bootRun
```

### With Gemini:
```bash
export AI_PROVIDER=gemini
export GCP_PROJECT_ID=my-project-id
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/key.json
./gradlew bootRun
```

## Testing

The application will automatically use the configured provider. The `AgentService` works with the generic `ChatModel` interface, so it's provider-agnostic.

## Available Models

### OpenAI Models:
- `gpt-4o` (default)
- `gpt-4-turbo`
- `gpt-3.5-turbo`

### Claude Models:
- `claude-3-5-sonnet-20241022` (default, recommended - best balance)
- `claude-3-opus-20240229` (most capable, slower)
- `claude-3-sonnet-20240229` (good balance)
- `claude-3-haiku-20240307` (fastest, cheapest)

### Gemini Models (outdated TODO update)
- `gemini-1.5-pro` (default)
- `gemini-1.5-flash`
- `gemini-1.0-pro`

## Troubleshooting

### OpenAI Issues:
- **Error: "Incorrect API key"** - Check that your `OPENAI_API_KEY` is valid
- **Error: "Model not found"** - Ensure you have access to the specified model

### Claude Issues:
- **Error: "Invalid API key"** - Check that your `ANTHROPIC_API_KEY` is valid and starts with `sk-ant-`
- **Error: "Model not found"** - Ensure you're using a valid Claude model name
- **Rate limits** - Check your API usage limits in the Anthropic console

### Gemini Issues:
- **Error: "PERMISSION_DENIED"** - Ensure Vertex AI API is enabled in your GCP project
- **Error: "Project ID not set"** - Set the `GCP_PROJECT_ID` environment variable
- **Error: "Could not load credentials"** - Set `GOOGLE_APPLICATION_CREDENTIALS` to point to your service account key

## Cost Considerations

### OpenAI Pricing (as of 2024):
- GPT-4o: ~$5 per 1M input tokens, ~$15 per 1M output tokens
- GPT-3.5-turbo: ~$0.50 per 1M input tokens, ~$1.50 per 1M output tokens

### Claude Pricing (as of 2024):
- Claude 3.5 Sonnet: ~$3 per 1M input tokens, ~$15 per 1M output tokens
- Claude 3 Opus: ~$15 per 1M input tokens, ~$75 per 1M output tokens
- Claude 3 Sonnet: ~$3 per 1M input tokens, ~$15 per 1M output tokens
- Claude 3 Haiku: ~$0.25 per 1M input tokens, ~$1.25 per 1M output tokens

### Gemini Pricing (as of 2024):
- Gemini 1.5 Pro: ~$3.50 per 1M input tokens, ~$10.50 per 1M output tokens
- Gemini 1.5 Flash: ~$0.35 per 1M input tokens, ~$1.05 per 1M output tokens

Pricing may vary. Check official documentation for current rates.

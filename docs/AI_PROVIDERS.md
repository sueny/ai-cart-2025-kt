# AI Provider Configuration

This document provides instructions on how to configure and use the supported AI providers with the ProcureFlow application.

## Overview

ProcureFlow is a modern procurement prototype that demonstrates AI-first interfaces for enterprise purchasing workflows. It features both a traditional UI and an AI-first conversational interface powered by a large language model (LLM). The AI agent can orchestrate complex procurement workflows end-to-end, including searching for items, registering new items, adding items to a cart, and checking out.

## Supported AI Providers

The application supports the following AI providers through the Spring AI framework:

-   **OpenAI** (Default)
-   **Claude (Anthropic)**
-   **Google Gemini (Vertex AI)**

The AI provider can be switched using the `AI_PROVIDER` environment variable.

## Configuration

The following sections describe how to configure each AI provider.

### OpenAI (Default)

To use OpenAI, set the following environment variables:

```bash
export AI_PROVIDER=openai
export OPENAI_API_KEY=your-openai-api-key
export OPENAI_MODEL=gpt-4o  # Optional, defaults to gpt-4o
```

Available models include `gpt-4o`, `gpt-4-turbo`, and `gpt-3.5-turbo`.

### Claude (Anthropic)

To use Claude, set the following environment variables:

```bash
export AI_PROVIDER=claude  # or 'anthropic'
export ANTHROPIC_API_KEY=your-anthropic-api-key
export CLAUDE_MODEL=claude-3-5-sonnet-20241022  # Optional, defaults to claude-3-5-sonnet-20241022
```

You can get an API key from the [Anthropic Console](https://console.anthropic.com/).

Available models include `claude-3-5-sonnet-20241022`, `claude-3-opus-20240229`, `claude-3-sonnet-20240229`, and `claude-3-haiku-20240307`.

### Google Gemini

To use Google Gemini, you need to set up Google Cloud credentials:

1.  **Set up Google Cloud credentials**:

    ```bash
    export GOOGLE_APPLICATION_CREDENTIALS=/path/to/your-service-account-key.json
    ```

2.  **Set environment variables**:

    ```bash
    export AI_PROVIDER=gemini
    export GCP_PROJECT_ID=your-gcp-project-id
    export GCP_LOCATION=us-central1  # Optional, defaults to us-central1
    export GEMINI_MODEL=gemini-1.5-pro  # Optional, defaults to gemini-1.5-pro
    ```

3.  **Enable the Vertex AI API** in your Google Cloud project.

Available models include `gemini-1.5-pro`, `gemini-1.5-flash`, and `gemini-1.0-pro`.

## Running the Application

To run the application, first set the environment variables for your chosen AI provider, then use one of the following methods:

### Using Gradle

```bash
# Example with OpenAI
export AI_PROVIDER=openai
export OPENAI_API_KEY=sk-...

cd backend
./gradlew bootRun
```

### Using Docker

```bash
# The docker-compose.yml file reads environment variables from your shell
docker-compose up --build
```

Make sure your `.env` file is configured correctly if you are using it with `docker-compose`.

## Troubleshooting

-   **OpenAI**: Check for "Incorrect API key" or "Model not found" errors. Ensure your API key is valid and you have access to the specified model.
-   **Claude**: Check for "Invalid API key" or "Model not found" errors. Ensure your API key is correct and you are using a valid model name.
-   **Gemini**: Check for "PERMISSION_DENIED" (ensure Vertex AI API is enabled), "Project ID not set" (check `GCP_PROJECT_ID`), or "Could not load credentials" (check `GOOGLE_APPLICATION_CREDENTIALS`) errors.

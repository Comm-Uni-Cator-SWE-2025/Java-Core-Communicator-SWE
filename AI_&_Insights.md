# AI and Insights

## Overview
We aim to provide the following features for the Comm-Uni-cator app.
- Take meeting notes
- Ask AI : Answer the questions based on the discussions in the meeting when mentioned in the chat as @ai or directly selecting the Ask AI button after clicking the permanent AI button.
- Summarise discussion points as bulletin points
- Track down actions items for individual users
- Drawing interactions in white board when prompted after selection.
    - This includes two interactions:
        1. User asks the AI to edit the drawing already present in the canvas or create a new drawing on to the canvas
        2. User asks the AI to give a description of the drawing already present in the canvas when prompted after selection.
- Provide insights in the dashboard about the number of attendees during the meeting and sentiment analysis at any point during the meeting.

## Objectives
- Provide support for the above features
- In the initial phase, we aim to achieve the above features by analysing the chat message.
- In the next phase, we will extend the above features by analysing the audio of the meeting.
- Create API endpoints to retrieve the required inputs.
- Create UI elements for interaction with UI tools.

## Design

### User Interface
- A permanent button adjacent to the button to end the call, will be dedicated to the AI features. This will provide quick access to the following AI features.
    1. Take meeting notes
    2. Ask AI (answer questions based on the discussions in the meeting)
    3. Summarise discussion points as bulletin points
    4. Track down action items.
    5. Provide meeting insights.

- In the chats, when mentioned as @ai, AI answers the questions based on the discussions in the meeting.

- In the canvas, when the user clicks on a shape, an AI button pops up. User can give prompts to edit the figure, create a new figure in place of the selected figure or give a description of the selected figure.

### LLMs and STT Models
- Gemini Flash 2.5 (cloud LLM)
- Whisper (OpenAI) (primary STT)
- Groq Distil-Whisper (fast STT fallback)
- Ollama (local LLM fallback)

+ Gemini Flash 2.5 will serve as the default backend for summarisation, note taking, Q&A and action item tracking.
+ Image generation uses Stable Diffusion (SDXL) implemeted via Ollama and run locally as default. It can also be routed to Gemini if needed.
+ For image interpretaiton, LLaVA (run locally with Ollama) can be used. For the same purpose, Gemini 2.5 Vision may also be used. 
+ For audio transcribing (will be implemented after successfully achieving the functionalities by parsing chats), Open AI whisper and Groq Distil Whisper can be used. 
+ For insights generation, DistilBERT (local) and Gemini can be used.
+ Default LLM Service will be HybridLLMService. This uses the cloud LLMs by default for text generation, falls back to local models if the cloud services reach their limit. Users can also explicitly choose whether to use cloud LLM or locally run LLM.

### UML Diagrams

1. Overall UML

- Input Data intefaces include IAIRequest, IMeetingData, IWhiteboardData. IAIRequest contains the `requestType`, `prompt` and `metaData`. `IMeetingData` includes `ChatData` and `AudioData`. `IWhiteBoard` data defines the information regarding the images selected from the whiteboard.

- The `ILLMService` interface enables switching between `HybridLLM`, `CloudLLM` and `LocalLLM`. System uses `HybridLLM` by default. `HybridLLM` uses cloud services by default. When this goes out of the limit, it switches to locally availble models. Users can also explicitly choose to select cloud or local LLM.

- `IAIResponse` serves as the inteface which handles the response from the AI. This includes text response as well as image response.


## Responsibilities of team members

1. Abhirami R Iyer : Handles the image generation and image interpretaion part

2. Berelli Gouthami and Nandhana Sunil : Handles the part related to Note taking, Summarising discussion points, Q&A Module

3. Vemula Veneela: Handles the part related to insight generation


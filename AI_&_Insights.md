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

![ai_button](https://github.com/user-attachments/assets/9e4d4cf9-3b35-424e-aa63-8e888d7a7065)

- A permanent button adjacent to the button to end the call, will be dedicated to the AI features. This will provide quick access to the following AI features.
    1. Take meeting notes
    2. Ask AI (answer questions based on the discussions in the meeting)
    3. Summarise discussion points as bulletin points
    4. Track down action items.
    5. Provide meeting insights.
    6. 
![ai_in_chats](https://github.com/user-attachments/assets/def326e7-9819-47e8-981a-6c71f3414c65)

- In the chats, when mentioned as @ai, AI answers the questions based on the discussions in the meeting.

![ai_in_canvas](https://github.com/user-attachments/assets/215d1615-4d11-4d11-8202-db4417ab584b)

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
   
![full_uml](https://github.com/user-attachments/assets/c2b6bde5-2d82-42b7-916a-56e5e41a63eb)

- Input Data intefaces include IAIRequest, IMeetingData, IWhiteboardData. IAIRequest contains the `requestType`, `prompt` and `metaData`. `IMeetingData` includes `ChatData` and `AudioData`. `IWhiteBoard` data defines the information regarding the images selected from the whiteboard.

- The `ILLMService` interface enables switching between `HybridLLM`, `CloudLLM` and `LocalLLM`. System uses `HybridLLM` by default. `HybridLLM` uses cloud services by default. When this goes out of the limit, it switches to locally availble models. Users can also explicitly choose to select cloud or local LLM.

- `IAIResponse` serves as the inteface which handles the response from the AI. This includes text response as well as image response.


## Responsibilities of team members

1. Abhirami R Iyer : Handles the image generation and image interpretaion part

   ![ai_abhi](https://github.com/user-attachments/assets/d801caec-8c7d-4a77-9db4-720175b2230e)

2. Berelli Gouthami and Nandhana Sunil : Handles the part related to Note taking, Summarising discussion points, Q&A Module

![ai_gou_nand](https://github.com/user-attachments/assets/d771903e-28ea-4294-b3ec-d374569d4922)

3. Vemula Veneela: Handles the part related to insight generation

![ai_veneela](https://github.com/user-attachments/assets/f4b5e210-b55c-4618-a29a-880f905224b2)


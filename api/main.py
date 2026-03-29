import os
import sys
import shutil
import tempfile
from fastapi import FastAPI, Depends, HTTPException, Security, UploadFile, File, Form
from fastapi.security.api_key import APIKeyHeader
from fastapi.responses import StreamingResponse
from pydantic import BaseModel
import uvicorn

# Ensure the core folder is available
sys.path.append(os.path.join(os.path.dirname(os.path.dirname(__file__))))

from api.test_manager import TestSessionManager
from core.ai_engine import AIEngine

# Basic Security
API_KEY = "sk-english-test-secret-12345"
API_KEY_NAME = "X-API-Key"
api_key_header = APIKeyHeader(name=API_KEY_NAME, auto_error=False)

def get_api_key(api_key_header: str = Security(api_key_header)):
    if api_key_header == API_KEY:
        return api_key_header
    raise HTTPException(status_code=403, detail="Could not validate API Key")

app = FastAPI(title="English Mastery AI Testing API")
manager = TestSessionManager()
ai_engine = AIEngine()

class StartRequest(BaseModel):
    user_name: str
    level: str = "beginner"

@app.get("/")
def read_root():
    return {"message": "Welcome to the English Mastery AI API. Please use the endpoints with your X-API-Key."}

@app.post("/api/test/start", tags=["Test Core"])
def start_test(req: StartRequest, api_key: str = Depends(get_api_key)):
    session_data = manager.start_test(req.user_name, req.level)
    if "error" in session_data:
        raise HTTPException(status_code=400, detail=session_data["error"])
        
    session_id = session_data["session_id"]
    return {
        "session_id": session_id,
        "level": req.level,
        "message": f"Test for level {req.level} started successfully"
    }

@app.get("/api/test/{session_id}/question/text", tags=["Test Core"])
def get_question_text(session_id: str, api_key: str = Depends(get_api_key)):
    q = manager.get_current_question(session_id)
    if "error" in q:
        raise HTTPException(status_code=400, detail=q["error"])
    return {
        "question_num": q["question_num"],
        "question_text": q["question_text"],
        "options": q["options"]
    }

@app.get("/api/test/{session_id}/question/audio", tags=["AI Engine"])
def get_question_audio(session_id: str, api_key: str = Depends(get_api_key)):
    """
    Returns an MP3 audio stream of the question being spoken by the AI.
    """
    q = manager.get_current_question(session_id)
    if "error" in q:
        raise HTTPException(status_code=400, detail=q["error"])
    
    # Combine question and options into one spoken text string
    full_text = f"Question {q['question_num']}. {q['question_text']} "
    for opt, val in q["options"].items():
         full_text += f"Option {opt}: {val}. "
         
    try:
         audio_stream = ai_engine.synthesize_speech(full_text)
         return StreamingResponse(audio_stream, media_type="audio/mpeg")
    except Exception as e:
         raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/test/{session_id}/answer/voice", tags=["AI Engine"])
async def submit_voice_answer(session_id: str, audio: UploadFile = File(...), api_key: str = Depends(get_api_key)):
    """
    Upload an audio file (e.g. .wav) containing the user's spoken answer.
    The AI Engine transcribes it and evaluates if it is correct.
    """
    q = manager.get_current_question(session_id)
    if "error" in q:
        raise HTTPException(status_code=400, detail=q["error"])
        
    correct_answer = q.get("correct_answer")
    
    # Save the uploaded file to a temporary location for the STT engine
    with tempfile.NamedTemporaryFile(delete=False, suffix=".wav") as temp_audio:
        shutil.copyfileobj(audio.file, temp_audio)
        temp_audio_path = temp_audio.name
        
    try:
        # 1. Transcribe the audio
        transcription = ai_engine.transcribe_audio(temp_audio_path)
        
        # 2. Evaluate if correct
        is_correct = ai_engine.evaluate_answer(transcription, correct_answer)
        
        # 3. Advance the session
        result = manager.evaluate_and_advance(session_id, is_correct)
        
        return {
            "transcribed_text": transcription,
            "was_correct": is_correct,
            "correct_answer_was": correct_answer,
            "session_status": result
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        os.remove(temp_audio_path)

@app.get("/api/test/{session_id}/score", tags=["Test Core"])
def get_final_score(session_id: str, api_key: str = Depends(get_api_key)):
    score = manager.get_score(session_id)
    if "error" in score:
        raise HTTPException(status_code=404, detail=score["error"])
    return score

class SynthesizeRequest(BaseModel):
    text: str

@app.post("/api/ai/synthesize", tags=["Stateless AI Engine"])
def stateless_synthesize(req: SynthesizeRequest, api_key: str = Depends(get_api_key)):
    """
    Stateless TTS endpoint for the Java backend.
    Returns an MP3 audio stream of the given text.
    """
    try:
         audio_stream = ai_engine.synthesize_speech(req.text)
         return StreamingResponse(audio_stream, media_type="audio/mpeg")
    except Exception as e:
         raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/ai/evaluate-voice", tags=["Stateless AI Engine"])
async def stateless_evaluate(audio: UploadFile = File(...), correct_answer: str = Form(...), api_key: str = Depends(get_api_key)):
    """
    Stateless STT and Evaluation endpoint for the Java backend.
    Upload an audio file and the expected answer.
    Returns transcription and correctness.
    """
    with tempfile.NamedTemporaryFile(delete=False, suffix=".wav") as temp_audio:
        shutil.copyfileobj(audio.file, temp_audio)
        temp_audio_path = temp_audio.name
        
    try:
        transcription = ai_engine.transcribe_audio(temp_audio_path)
        is_correct = ai_engine.evaluate_answer(transcription, correct_answer)
        
        return {
            "transcribed_text": transcription,
            "was_correct": is_correct,
            "correct_answer_was": correct_answer
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        if os.path.exists(temp_audio_path):
            os.remove(temp_audio_path)

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)

import os
import io
import speech_recognition as sr
from gtts import gTTS

class AIEngine:
    """
    A unified AI Engine that provides agnostic Speech-to-Text and Text-to-Speech
    functions, entirely decoupled from Google Colab or IPython.
    """
    
    def __init__(self):
        self.recognizer = sr.Recognizer()

    def synthesize_speech(self, text: str) -> io.BytesIO:
        """
        Converts text into an MP3 Audio file stored in a BytesIO memory buffer.
        The backend API can send this buffer directly to the user's browser.
        """
        try:
            tts = gTTS(text=text, lang="en", slow=False)
            audio_buffer = io.BytesIO()
            tts.write_to_fp(audio_buffer)
            audio_buffer.seek(0)
            return audio_buffer
        except Exception as e:
            raise RuntimeError(f"TTS Error: {str(e)}")

    def transcribe_audio(self, audio_file_path: str) -> str:
        """
        Reads a saved audio file (uploaded from the frontend) and
        returns the text transcription.
        """
        if not os.path.exists(audio_file_path):
            raise FileNotFoundError("Audio file not found for transcription.")

        try:
            with sr.AudioFile(audio_file_path) as source:
                audio_data = self.recognizer.record(source)
            text = self.recognizer.recognize_google(audio_data, language="en-US")
            return text.strip().lower()
        except sr.UnknownValueError:
            return ""  # Could not understand audio
        except sr.RequestError as e:
            raise RuntimeError(f"Speech Recognition Service Error: {str(e)}")

    def evaluate_answer(self, user_transcript: str, correct_answer: str) -> bool:
        """
        Evaluates whether the user's transcribed speech matches the correct answer.
        """
        user_transcript = user_transcript.lower().strip()
        correct_answer = str(correct_answer).lower().strip()

        # Check for direct match or conversational mentions (e.g. "I think it's option B")
        is_correct = (
            user_transcript == correct_answer or
            f"option {correct_answer}" in user_transcript or
            f"answer {correct_answer}" in user_transcript
        )
        return is_correct

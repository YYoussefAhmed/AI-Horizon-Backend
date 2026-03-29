import uuid
import json
import os
from typing import Dict

class TestSessionManager:
    def __init__(self):
        self.sessions: Dict[str, dict] = {}
        
        # Load the unified JSON database
        db_path = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'database', 'questions.json')
        try:
            with open(db_path, 'r', encoding='utf-8') as f:
                raw_data_map = json.load(f)
        except FileNotFoundError:
            raise RuntimeError(f"Database not found at {db_path}")
        
        # Robust filtering: Only keep items that have a question and a non-empty answer
        self.tests_data = {}
        for level, data in raw_data_map.items():
            self.tests_data[level] = [
                q for q in data 
                if q.get("question") and q.get("answer") and str(q.get("answer")).strip() != ""
            ]
        
    def start_test(self, user_name: str, level: str = "beginner") -> dict:
        """Starts a new test and returns the session details"""
        if level not in self.tests_data:
            return {"error": f"Invalid test level. Choose from: {list(self.tests_data.keys())}"}
            
        session_id = str(uuid.uuid4())
        self.sessions[session_id] = {
            "user_name": user_name,
            "level": level,
            "score": 0,
            "current_question_index": 0,
            "status": "active",
            "total_questions": len(self.tests_data[level])
        }
        return {"session_id": session_id}
        
    def get_current_question(self, session_id: str) -> dict:
        session = self.sessions.get(session_id)
        if not session or session["status"] != "active":
            return {"error": "Invalid or inactive session"}
            
        level = session["level"]
        index = session["current_question_index"]
        questions = self.tests_data[level]
        
        if index >= len(questions):
             return {"error": "Test completed"}
             
        q = questions[index]
        return {
             "question_num": index + 1,
             "question_text": q.get("question", ""),
             "options": q.get("options", {}),
             "correct_answer": q.get("answer", "")
        }

    def evaluate_and_advance(self, session_id: str, is_correct: bool) -> dict:
        """Advances the session based on whether the AI engine determined the voice answer was correct."""
        session = self.sessions.get(session_id)
        if not session or session["status"] != "active":
            return {"error": "Invalid or inactive session"}
            
        level = session["level"]
        index = session["current_question_index"]
        questions = self.tests_data[level]
        
        if index >= len(questions):
             return {"error": "Test completed"}
             
        if is_correct:
            session["score"] += 1
            
        session["current_question_index"] += 1
        
        # Check if test finished
        if session["current_question_index"] >= len(questions):
            session["status"] = "completed"
            
        return {
            "is_correct": is_correct,
            "current_score": session["score"],
            "test_status": session["status"]
        }
        
    def get_score(self, session_id: str) -> dict:
        session = self.sessions.get(session_id)
        if not session:
            return {"error": "Invalid session"}
            
        total = session["total_questions"]
        percentage = (session["score"] / total) * 100 if total > 0 else 0
        return {
             "user_name": session["user_name"],
             "level": session["level"],
             "score": session["score"],
             "total_questions": total,
             "percentage": percentage,
             "status": session["status"]
        }

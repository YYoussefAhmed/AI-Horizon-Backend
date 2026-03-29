@echo off
echo ===================================================
echo    Starting English Mastery Tests API Server...
echo ===================================================
echo.
echo Installing requirements if needed...
pip install -r requirements.txt
echo.
echo Starting the server...
python -m uvicorn server:app --reload --host 127.0.0.1 --port 8000
pause

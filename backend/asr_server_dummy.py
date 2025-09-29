# asr_server_dummy.py
from fastapi import FastAPI, File, UploadFile

app = FastAPI()

@app.post("/asr/transcribe")
async def transcribe(file: UploadFile = File(...)):
    # 실제 음성 인식 대신 항상 고정된 텍스트 반환
    return {"text": "더미 인식 결과: 안녕하세요"}

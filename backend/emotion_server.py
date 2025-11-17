from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from transformers import pipeline
import torch
import traceback

app = FastAPI()

# --- (1/2) 모델 로드 ---
try:
    # [수정] 상위 폴더 기호 '..' 삭제
    model_path = "models/emotion_model"

    print(f"--- 🔄 감정 분석 모델 로딩 시도... (경로: {model_path}) ---")

    emotion_pipeline = pipeline(
        "text-classification",
        model=model_path,
        device="cpu",
        local_files_only=True # (이 폴더는 모든 파일이 있으므로 True)
    )

    print(f"--- 🚀 감정 분석 모델 로딩 성공 ({model_path}) 🚀 ---")

except Exception as e:
    print(f"--- 🚨 감정 분석 모델 로딩 실패 🚨 ---: {e}")
    traceback.print_exc()
    emotion_pipeline = None
    raise e # (실패 시 서버 중지)
# -------------------------


class EmotionRequest(BaseModel):
    text: str

@app.post("/emotion/analyze")
async def analyze_emotion(request: EmotionRequest):

    if emotion_pipeline is None:
        raise HTTPException(status_code=500, detail="감정 분석 모델이 로드되지 않았습니다.")

    input_text = request.text
    if not input_text:
        return {"emotion": "중립"}

    try:
        result_list = emotion_pipeline(input_text)
        result_emotion = result_list[0]['label']
    except Exception as e:
        print(f"모델 예측 오류: {e}")
        raise HTTPException(status_code=500, detail=f"모델 예측 오류: {e}")

    print(f"감정 분석 결과: {result_emotion}")
    return {"emotion": result_emotion}
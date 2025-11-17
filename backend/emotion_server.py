# from fastapi import FastAPI, HTTPException
# from pydantic import BaseModel
# from transformers import pipeline
# import torch
# import traceback
#
# app = FastAPI()
#
# # --- (1/2) 모델 로드 ---
# try:
#     # [수정] 상위 폴더 기호 '..' 삭제
#     model_path = "models/emotion_model"
#
#     print(f"--- 🔄 감정 분석 모델 로딩 시도... (경로: {model_path}) ---")
#
#     emotion_pipeline = pipeline(
#         "text-classification",
#         model=model_path,
#         device="cpu",
#         local_files_only=True # (이 폴더는 모든 파일이 있으므로 True)
#     )
#
#     print(f"--- 🚀 감정 분석 모델 로딩 성공 ({model_path}) 🚀 ---")
#
# except Exception as e:
#     print(f"--- 🚨 감정 분석 모델 로딩 실패 🚨 ---: {e}")
#     traceback.print_exc()
#     emotion_pipeline = None
#     raise e # (실패 시 서버 중지)
# # -------------------------
#
#
# class EmotionRequest(BaseModel):
#     text: str
#
# @app.post("/emotion/analyze")
# async def analyze_emotion(request: EmotionRequest):
#
#     if emotion_pipeline is None:
#         raise HTTPException(status_code=500, detail="감정 분석 모델이 로드되지 않았습니다.")
#
#     input_text = request.text
#     if not input_text:
#         return {"emotion": "중립"}
#
#     try:
#         result_list = emotion_pipeline(input_text)
#         result_emotion = result_list[0]['label']
#     except Exception as e:
#         print(f"모델 예측 오류: {e}")
#         raise HTTPException(status_code=500, detail=f"모델 예측 오류: {e}")
#
#     print(f"감정 분석 결과: {result_emotion}")
#     return {"emotion": result_emotion}

from fastapi import FastAPI
from pydantic import BaseModel

# Spring(EmotionClient)으로부터 받을 요청 JSON의 모델 정의
# ({"text": "..."})
class EmotionRequest(BaseModel):
    text: str

app = FastAPI()

# 감정 분석 API의 엔드포인트
# EmotionClient.java의 @Value와 일치하는 주소
@app.post("/emotion/analyze")
async def analyze_emotion(request: EmotionRequest):

    # (테스트용) 터미널에 수신된 텍스트를 출력
    print(f"--- FastAPI (Emotion Server) ---")
    print(f"수신된 텍스트: '{request.text}'")

    # (더미 데이터) 실제 모델 대신 항상 '기쁨'을 반환
    # (TODO: 추후 실제 감정 분석 모델 로직으로 교체)
    dummy_emotion = "기쁨"

    # (테스트용) 터미널에 반환할 감정을 출력
    print(f"반환할 감정: '{dummy_emotion}'")
    print(f"----------------------------------")

    # Spring(EmotionClient)으로 보낼 응답 JSON 반환
    # ({"emotion": "..."})
    return {"emotion": dummy_emotion}
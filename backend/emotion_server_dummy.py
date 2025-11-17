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
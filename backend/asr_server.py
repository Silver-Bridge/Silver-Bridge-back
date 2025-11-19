# from fastapi import FastAPI, File, UploadFile, HTTPException
# import librosa # 오디오 파일 로딩
# from transformers import pipeline # Hugging Face 파이프라인
# import torch # PyTorch 백엔드
# import traceback # 오류 추적
#
# app = FastAPI()
#
# # --- (1/2) 모델 로드 ---
# try:
#     # [수정 1] 상위 폴더 기호 '..' 삭제
#     model_path = "models/stt_model"
#
#     # [수정 2] config.json 기반 원본 모델 ID
#     BASE_MODEL_ID = "openai/whisper-large-v2"
#
#     print(f"--- 🔄 STT 모델 로딩 시도... (경로: {model_path}) ---")
#
#     stt_pipeline = pipeline(
#         "automatic-speech-recognition",
#         model=model_path,                # 모델(Weight)은 로컬에서
#         tokenizer=BASE_MODEL_ID,       # 토크나이저(어휘)는 Hub에서
#         feature_extractor=model_path,  # 전처리기(Preprocessor)는 로컬에서
#         device="cpu"
#         # [수정 3] local_files_only=True 삭제 (토크나이저를 Hub에서 받아야 함)
#     )
#
#     print(f"--- 🚀 STT 모델 로딩 성공 ({model_path}) 🚀 ---")
#
# except Exception as e:
#     print(f"--- 🚨 STT 모델 로딩 실패 🚨 ---: {e}")
#     traceback.print_exc() # 상세 오류 출력
#     stt_pipeline = None
#     raise e # (실패 시 서버 중지)
# # -------------------------
#
#
# @app.post("/asr/transcribe")
# async def transcribe(file: UploadFile = File(...)):
#
#     if stt_pipeline is None:
#         raise HTTPException(status_code=500, detail="STT 모델이 로드되지 않았습니다.")
#
#     try:
#         audio_data, sample_rate = librosa.load(file.file, sr=16000)
#     except Exception as e:
#         raise HTTPException(status_code=400, detail=f"오디오 파일 처리 오류: {e}")
#
#     try:
#         result_dict = stt_pipeline(audio_data, chunk_length_s=30)
#         result_text = result_dict["text"]
#     except Exception as e:
#         print(f"모델 예측 오류: {e}")
#         raise HTTPException(status_code=500, detail=f"모델 예측 오류: {e}")
#
#     print(f"STT 예측 결과: {result_text}")
#     return {"text": result_text}

# asr_server_dummy.py
from fastapi import FastAPI, File, UploadFile

app = FastAPI()

@app.post("/asr/transcribe")
async def transcribe(file: UploadFile = File(...)):
    # 실제 음성 인식 대신 항상 고정된 텍스트 반환
    return {"text": "더미 인식 결과: 안녕하세요"}

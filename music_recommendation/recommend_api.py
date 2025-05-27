from flask import Flask, request, jsonify
from flask_cors import CORS
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import pandas as pd
import re

app = Flask(__name__)
CORS(app)

# 감정 확장 사전
MOOD_EXPANSION = {
    "기쁨": ["기쁨", "신남", "활기", "에너지", "웃음", "행복"],
    "슬픔": ["슬픔", "우울", "비", "조용함", "감성", "눈물"],
    "아쉬움": ["아쉬움", "서운함", "회상", "그리움", "여운"],
    "화남": ["화남", "분노", "짜증", "격앙", "긴장"],
    "쏘쏘": ["무난", "일상", "조용", "잔잔", "보통", "쏘쏘"]
}

def clean_text(text):
    return re.sub(r"[^\w\s]", "", text.lower().strip())

# CSV 로드
df = pd.read_csv("youtube_playlist_videos.csv")
video_data = [{"title": row["title"], "url": row["url"]} for _, row in df.iterrows()]

@app.route("/recommend", methods=["GET"])
def recommend():
    mood = request.args.get("mood", "")
    expanded_mood = " ".join(MOOD_EXPANSION.get(mood, [mood]))
    expanded_mood = clean_text(expanded_mood)

    titles = [clean_text(item["title"]) for item in video_data]
    corpus = [expanded_mood] + titles

    vectorizer = TfidfVectorizer(ngram_range=(1, 2), stop_words='english')
    tfidf_matrix = vectorizer.fit_transform(corpus)
    similarities = cosine_similarity(tfidf_matrix[0:1], tfidf_matrix[1:])
    best_idx = similarities.argmax()

    return jsonify({
        "recommendation": video_data[best_idx]["title"],
        "url": video_data[best_idx]["url"]
    })

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5001)

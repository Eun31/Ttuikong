from flask import Flask, request, jsonify
from flask_cors import CORS
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
import pandas as pd

app = Flask(__name__)
CORS(app)

# CSV에서 제목/URL 로드
df = pd.read_csv("youtube_playlist_videos.csv")  # "title", "url" 컬럼 존재해야 함
video_data = [{"title": row["title"], "url": row["url"]} for _, row in df.iterrows()]

@app.route("/recommend", methods=["GET"])
def recommend():
    mood = request.args.get("mood", "")
    titles = [item["title"] for item in video_data]
    corpus = [mood] + titles

    vectorizer = TfidfVectorizer()
    tfidf_matrix = vectorizer.fit_transform(corpus)
    similarities = cosine_similarity(tfidf_matrix[0:1], tfidf_matrix[1:])
    best_idx = similarities.argmax()

    return jsonify({
        "recommendation": video_data[best_idx]["title"],
        "url": video_data[best_idx]["url"]
    })

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5001)

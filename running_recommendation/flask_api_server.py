
from flask import Flask, request, jsonify
import pandas as pd
import numpy as np
import pickle
import os
import json
from datetime import datetime

# 모델 파일 경로
MODEL_PATH = 'running_recommendation_models.pkl'
CONFIG_PATH = 'running_config.json'

app = Flask(__name__)

# 설정 로드
def load_config():
    if os.path.exists(CONFIG_PATH):
        with open(CONFIG_PATH, 'r') as f:
            return json.load(f)
    return {}

# 모델 로드
def load_models():
    try:
        with open(MODEL_PATH, 'rb') as f:
            models = pickle.load(f)
        return models
    except Exception as e:
        print(f"모델 로드 중 오류 발생: {e}")
        return None

# 전역 변수
models = load_models()
config = load_config()

# 데이터 유효성 검사
def validate_user_data(data):
    """사용자 데이터 유효성 검사"""
    errors = []

    # 필수 필드 확인
    required_fields = ['gender', 'age', 'height', 'weight', 'activityLevel']
    missing_fields = [field for field in required_fields if field not in data]

    if missing_fields:
        errors.append(f"필수 필드가 누락되었습니다: {', '.join(missing_fields)}")
        return False, errors

    valid_ranges = config.get("valid_ranges", {
        "age": {"min": 12, "max": 100},
        "height": {"min": 100, "max": 250},
        "weight": {"min": 30, "max": 200}
    })

    # 나이 검사
    try:
        age = float(data['age'])
        if age < valid_ranges["age"]["min"] or age > valid_ranges["age"]["max"]:
            errors.append(f"나이는 {valid_ranges['age']['min']}~{valid_ranges['age']['max']} 사이여야 합니다.")
    except (ValueError, TypeError):
        errors.append("나이는 숫자여야 합니다.")

    # 키 검사
    try:
        height = float(data['height'])
        if height < valid_ranges["height"]["min"] or height > valid_ranges["height"]["max"]:
            errors.append(f"키는 {valid_ranges['height']['min']}~{valid_ranges['height']['max']} 사이여야 합니다.")
    except (ValueError, TypeError):
        errors.append("키는 숫자여야 합니다.")

    # 몸무게 검사
    try:
        weight = float(data['weight'])
        if weight < valid_ranges["weight"]["min"] or weight > valid_ranges["weight"]["max"]:
            errors.append(f"몸무게는 {valid_ranges['weight']['min']}~{valid_ranges['weight']['max']} 사이여야 합니다.")
    except (ValueError, TypeError):
        errors.append("몸무게는 숫자여야 합니다.")

    return len(errors) == 0, errors

# 사용자 데이터 정규화
def normalize_user_data(data):
    """API 요청 데이터를 모델 입력 형식으로 변환"""
    return {
        'gender': data['gender'],
        'age': float(data['age']),
        'height': float(data['height']),
        'weight': float(data['weight']),
        'activity_level': data['activityLevel'],
        'activity_goal': data.get('activityGoal', data['activityLevel']),
        'avg_distance': float(data.get('avgDistance', 0)),
        'total_distance': float(data.get('totalDistance', 0)),
        'id': data.get('userId'),
        'nickname': data.get('nickname', 'User')
    }

# 사용자 추천 생성
def generate_recommendation(user_data):
    # 모델이 없으면 기본값으로 추천
    if not models or 'nn_model' not in models or 'preprocessor' not in models:
        return generate_fallback_recommendation(user_data)

    nn_model = models['nn_model']
    preprocessor = models['preprocessor']
    calorie_model_info = models.get('calorie_model_info')
    model_config = models.get('config', config)

    try:
        # 사용자 정보를 DataFrame으로 변환
        user_df = pd.DataFrame([user_data])

        features = ['gender', 'age', 'height', 'weight', 'activity_level', 'activity_goal']
        available_features = [f for f in features if f in user_df.columns]
        user_features = user_df[available_features]

        # 사용자 특성 전처리
        user_processed = preprocessor.transform(user_features)
        n_neighbors = min(5, nn_model.n_neighbors)
        distances, indices = nn_model.kneighbors(user_processed, n_neighbors=n_neighbors)

        user_dataset = models.get('user_data', pd.DataFrame())

        similar_distances = []
        weights = []

        for i, idx in enumerate(indices[0]):
            if idx < len(user_dataset) and 'avg_distance' in user_dataset.columns:
                distance = user_dataset.iloc[idx]['avg_distance']
                if distance > 0:
                    similar_distances.append(distance)
                    weights.append(1 / (distances[0][i] + 0.1))

        if similar_distances:
            weighted_avg = sum(d * w for d, w in zip(similar_distances, weights)) / sum(weights)
        else:
            activity_levels = model_config.get("activity_levels", {})
            weighted_avg = activity_levels.get(user_data['activity_level'], {}).get("base_distance", 5.0)

        # 활동 수준에 따른 조정
        activity_levels = model_config.get("activity_levels", {})
        user_level = activity_levels.get(user_data['activity_level'], {}).get("level", 3)
        goal_level = activity_levels.get(user_data['activity_goal'], {}).get("level", user_level)

        # 목표와 현재 수준의 차이에 따른 조정
        level_diff = goal_level - user_level
        goal_adjustment = 1.0 + (level_diff * model_config.get("model_parameters", {}).get("goal_adjustment_factor", 0.03))

        # 최종 추천 거리
        recommended_distance = weighted_avg * goal_adjustment

        # 이전 기록이 있으면 사용
        current_avg_distance = user_data['avg_distance']
        if current_avg_distance > 0:
            # 급격한 증가 방지 (최대 20%)
            max_increase = current_avg_distance * 1.2
            recommended_distance = min(max(recommended_distance, current_avg_distance), max_increase)

        # 활동 수준에 따른 최소/최대 제한
        level_info = activity_levels.get(user_data['activity_level'], {})
        min_distance = level_info.get("min_distance", 3.0)
        max_distance = level_info.get("max_distance", 8.0)

        recommended_distance = max(min_distance, min(recommended_distance, max_distance))

        # 소수점 둘째 자리까지 반올림
        recommended_distance = round(recommended_distance, 2)

        # 페이스와 소요 시간 계산
        avg_pace = level_info.get("pace_min_km", 6.0)
        estimated_time = round(recommended_distance * avg_pace, 0)

        # 속도 계산
        speed = level_info.get("speed_kmh", round(60 / avg_pace, 1))

        # 칼로리 계산
        if calorie_model_info:
            # 칼로리 모델 사용
            calorie_model = calorie_model_info['model']
            features = calorie_model_info['features']

            # 입력 데이터 생성
            calorie_input = pd.DataFrame([{
                'Gender': 'M' if user_data['gender'] in ['남성', 'M'] else 'F',
                'Weight(kg)': user_data['weight'],
                'Running Speed(km/h)': speed,
                'Distance(km)': recommended_distance
            }])

            # 추가 특성이 있으면 추가
            if 'Age' in features:
                calorie_input['Age'] = user_data['age']
            if 'Height(cm)' in features:
                calorie_input['Height(cm)'] = user_data['height']
            if 'BMI' in features:
                bmi = user_data['weight'] / ((user_data['height']/100) ** 2)
                calorie_input['BMI'] = bmi
            if 'Running Time(min)' in features:
                calorie_input['Running Time(min)'] = estimated_time

            # 원-핫 인코딩
            calorie_input_encoded = pd.get_dummies(calorie_input, columns=['Gender'])

            # 필요한 특성만 선택
            for feature in features:
                if feature not in calorie_input_encoded.columns:
                    calorie_input_encoded[feature] = 0

            input_features = calorie_input_encoded[features]

            # 칼로리 예측
            estimated_calories = round(float(calorie_model.predict(input_features)[0]), 0)
        else:
            # 간단한 공식으로 예상 칼로리 계산
            weight = user_data['weight']
            calorie_factor = model_config.get("model_parameters", {}).get("calorie_default_factor", 1.036)
            estimated_calories = round(weight * recommended_distance * calorie_factor, 0)

        # 응답 생성 - 핵심 3개 값만
        return {
            "status": "success",
            "data": {
                "recommendedDistance": recommended_distance,
                "estimatedTime": int(estimated_time),
                "estimatedCalories": int(estimated_calories)
            }
        }

    except Exception as e:
        print(f"추천 생성 중 오류 발생: {e}")
        return generate_fallback_recommendation(user_data)

def generate_fallback_recommendation(user_data):
    """모델이 없거나 오류 발생 시 기본 추천"""
    activity_levels = config.get("activity_levels", {
        "느긋한 코알라": {"level": 1, "base_distance": 2.5, "min_distance": 1.5, "max_distance": 3.0, "pace_min_km": 9.0, "speed_kmh": 6.7},
        "산책하는 거북이": {"level": 2, "base_distance": 4.0, "min_distance": 3.0, "max_distance": 5.0, "pace_min_km": 7.5, "speed_kmh": 8.0},
        "신나는 강아지": {"level": 3, "base_distance": 6.0, "min_distance": 4.0, "max_distance": 8.0, "pace_min_km": 6.0, "speed_kmh": 10.0},
        "힘찬 질주 말": {"level": 4, "base_distance": 8.0, "min_distance": 6.0, "max_distance": 12.0, "pace_min_km": 5.0, "speed_kmh": 12.0},
        "전광석화 치타": {"level": 5, "base_distance": 10.0, "min_distance": 8.0, "max_distance": 15.0, "pace_min_km": 4.5, "speed_kmh": 13.3}
    })

    # 사용자 활동 수준에 따른 기본 추천
    level_info = activity_levels.get(user_data['activity_level'], {
        "base_distance": 5.0,
        "min_distance": 3.0,
        "max_distance": 8.0,
        "pace_min_km": 6.0,
        "speed_kmh": 10.0
    })

    recommended_distance = level_info.get("base_distance", 5.0)

    # 이전 기록이 있으면 사용
    current_avg_distance = user_data.get('avg_distance', 0)
    if current_avg_distance > 0:
        recommended_distance = max(recommended_distance, current_avg_distance)

    # 소수점 둘째 자리까지 반올림
    recommended_distance = round(recommended_distance, 2)

    # 페이스와 소요 시간 계산
    avg_pace = level_info.get("pace_min_km", 6.0)
    estimated_time = round(recommended_distance * avg_pace, 0)

    # 간단한 공식으로 예상 칼로리 계산
    weight = user_data.get('weight', 65)
    calorie_factor = config.get("model_parameters", {}).get("calorie_default_factor", 1.036)
    estimated_calories = round(weight * recommended_distance * calorie_factor, 0)

    # 응답 생성 - 핵심 3개 값만
    return {
        "status": "success",
        "data": {
            "recommendedDistance": recommended_distance,
            "estimatedTime": int(estimated_time),
            "estimatedCalories": int(estimated_calories)
        }
    }

# API 엔드포인트 정의
@app.route('/health', methods=['GET'])
def health_check():
    """서버 상태 확인"""
    if models:
        return jsonify({
            "status": "healthy",
            "models_loaded": True,
            "api_version": "1.0.0"
        })
    else:
        return jsonify({
            "status": "degraded",
            "models_loaded": False,
            "fallback": "basic recommendation available",
            "api_version": "1.0.0"
        }), 200

@app.route('/api/recommend', methods=['POST'])
def recommend():
    """러닝 추천 API"""
    # 요청 데이터 확인
    if not request.json:
        return jsonify({"status": "error", "message": "요청 본문이 필요합니다."}), 400

    data = request.json

    # 데이터 유효성 검사
    is_valid, errors = validate_user_data(data)
    if not is_valid:
        return jsonify({
            "status": "error",
            "message": "유효하지 않은 데이터입니다.",
            "errors": errors
        }), 400

    # 데이터 정규화
    user_data = normalize_user_data(data)

    # 추천 생성
    recommendation = generate_recommendation(user_data)

    return jsonify(recommendation)

@app.route('/api/model/info', methods=['GET'])
def model_info():
    """모델 정보 API"""
    if models:
        return jsonify({
            "status": "success",
            "data": {
                "created_at": models.get('metadata', {}).get('created_at', 'unknown'),
                "num_users": models.get('metadata', {}).get('num_users', 0),
                "num_calories_records": models.get('metadata', {}).get('num_calories_records', 0)
            }
        })
    else:
        return jsonify({
            "status": "error",
            "message": "모델이 로드되지 않았습니다."
        }), 404

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5000))
    app.run(host='0.0.0.0', port=port, debug=True)

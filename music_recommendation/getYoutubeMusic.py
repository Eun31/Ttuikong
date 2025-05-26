from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.options import Options
import time
import pandas as pd

# 재생목록 URL
PLAYLIST_URL = "https://youtube.com/playlist?list=PLtmZj29rItKfvHjuY-ykhEiCC2EbBW2J1"

# 크롬 드라이버 옵션 설정
options = Options()
options.add_argument("--headless")  # 백그라운드 실행 원할 시
options.add_argument("--disable-gpu")
options.add_argument("--no-sandbox")

# 드라이버 실행
driver = webdriver.Chrome(options=options)
driver.get(PLAYLIST_URL)
time.sleep(3)  # 페이지 로딩 대기

# 스크롤 반복 (스크롤 끝까지 내리기)
last_height = driver.execute_script("return document.documentElement.scrollHeight")
while True:
    driver.execute_script("window.scrollTo(0, document.documentElement.scrollHeight);")
    time.sleep(2)
    new_height = driver.execute_script("return document.documentElement.scrollHeight")
    if new_height == last_height:
        break
    last_height = new_height

# 영상 목록 추출
video_elements = driver.find_elements(By.CSS_SELECTOR, "ytd-playlist-video-renderer")

video_data = []
for video in video_elements:
    try:
        title = video.find_element(By.ID, "video-title").text
        url = video.find_element(By.ID, "video-title").get_attribute("href")
        video_data.append({"title": title, "url": url})
    except Exception as e:
        print("에러 발생:", e)

driver.quit()

# CSV 저장
df = pd.DataFrame(video_data)
df.to_csv("youtube_playlist_videos.csv", index=False, encoding="utf-8-sig")

print("CSV 파일 저장 완료: youtube_playlist_videos.csv")

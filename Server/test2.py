### 테스트 ###

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.options import Options
import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
import time
import asyncio
from flask import Flask, jsonify, render_template, request, make_response
import threading
from math import radians, sin, cos, sqrt, atan2
from datetime import datetime
import json
from pytz import timezone
import requests

server = Flask(__name__)

# Use a service account.
cred = credentials.Certificate('./buss-8a962-firebase-adminsdk-6bwrz-da828f8270.json')
app = firebase_admin.initialize_app(cred)
db = firestore.client()
url = 'http://www.hoseo.ac.kr/Home/Contents.mbz?action=MAPP_2302082206'

options = Options()
options.add_argument('--disable-gpu')
options.add_argument('--no-sandbox')
options.add_argument('--headless')
options.add_argument('--disable-dev-shm-usage')
options.add_argument('--disable-gpu')
options.add_argument('--ignore-ssl-errors=yes')
options.add_argument('--ignore-certificate-errors=yes')

stations = ['아산캠퍼스', '천안아산역', '쌍용2동', '충무병원', '천안역', '천안터미널', '천안캠퍼스']
# stations_2 = ['아산캠퍼스', '롯데캐슬', '배방역', '아산시외버스터미널', '온양온천역', '배방역']

CLIENT_ID = ""
CLIENT_SECRET = ""

# Directions 5 API URL
URL = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving"

@server.route('/', methods=['POST'])
def send_back_data():
    # 가장 가까운 위치 찾기
    print(json.loads(request.get_data()))
    my_location = json.loads(request.get_data())
    min_distance = float('inf')
    locations = db.collection("loc").document("아<->천").get().to_dict()
    for name, coords in locations.items():
        distance = haversine(my_location['latitude'], my_location['longitude'], coords[0], coords[1])
        if distance < min_distance:
            min_distance = distance
            closest = name
    # print(closest)
    now = datetime.now(timezone('Asia/Seoul'))
    if now.weekday() == 5:
        test = db.collection("main").document("아<->천(토요일)").get().to_dict()
    elif now.weekday() == 6:
        test = db.collection("main").document("아<->천(일요일)").get().to_dict()
    else:
        test = db.collection("main").document("아<->천(평일)").get().to_dict()

    time_dts = [now.replace(hour=int(t.split(':')[0]), minute=int(t.split(':')[1]), second=0, microsecond=0) for t in test['아->천'][closest]]
    time_dts_2 = [now.replace(hour=int(t.split(':')[0]), minute=int(t.split(':')[1]), second=0, microsecond=0) for t in test['천->아'][closest]]    
    #print(datetime.strftime(min([t for t in time_dts if t >= datetime.now()], key=lambda x: abs(x -  datetime.now())), '%H:%M'))
    # print(datetime.strftime(min([t for t in time_dts_2 if t >= datetime.now()], key=lambda x: abs(x - datetime.now())), '%H:%M'))

    keys = list(locations.keys())

    
    # 출발지와 도착지 설정 (위도, 경도)
    start = f"{locations[keys[keys.index(closest) - 1]][1]},{locations[keys[keys.index(closest) - 1]][1]}"  # 출발지 (예: 네이버 본사)
    goal = f"{locations[closest][1]},{locations[closest][0]}"   # 도착지 (예: 강남역)

    # API 요청 매개변수
    params = {
        "start": start,
        "goal": goal,
        "option": "trafast"  # 경로 옵션 (trafast: 빠른길, tracomfort: 편안한길 등)
    }

    # API 요청 헤더
    headers = {
        "X-NCP-APIGW-API-KEY-ID": CLIENT_ID,
        "X-NCP-APIGW-API-KEY": CLIENT_SECRET
    }

    # API 요청 보내기
    response = requests.get(URL, headers=headers, params=params)

    # 응답 처리
    if response.status_code == 200:
        data = response.json()
        print("경로 탐색 결과:")
        print(response)
        for path in data.get("route", {}).get("trafast", []):  # trafast 경로 정보
            summary = path.get("summary", {})
            distance = summary.get("distance")  # 거리 (미터)
            duration = summary.get("duration")  # 예상 소요 시간 (밀리초)
            print(f"거리: {distance}m, 예상 소요 시간: {duration // 1000}초")
    else:
        print(f"API 요청 실패: {response.status_code}, {response.text}")
    try:
        data = {
            "station": closest,
            "distance": round(min_distance, 1),
            "천안캠퍼스행": str(datetime.strftime(min([t for t in time_dts if t >= now], key=lambda x: abs(x -  now)), '%H:%M')),
            "아산캠퍼스행": str(datetime.strftime(min([t for t in time_dts_2 if t >= now], key=lambda x: abs(x - now)), '%H:%M'))
        }
    except:
        data = {
            "station": closest, 
            "distance": min_distance,
            # "천안캠퍼스행": str(datetime.strftime(min([t for t in time_dts if t >= now], key=lambda x: abs(x -  now)), '%H:%M')), 
            # "아산캠퍼스행": str(datetime.strftime(min([t for t in time_dts_2 if t >= now], key=lambda x: abs(x - now)), '%H:%M')) ### 나중에 서버꺼 복붙
        }
    return json.dumps(data, ensure_ascii=False)    

def haversine(lat1, lon1, lat2, lon2):
    # 위도와 경도를 라디안으로 변환
    lat1, lon1, lat2, lon2 = map(radians, [lat1, lon1, lat2, lon2])
    
    # 위도와 경도의 차이
    dlat = lat2 - lat1
    dlon = lon2 - lon1

    # 하버사인 공식
    a = sin(dlat / 2)**2 + cos(lat1) * cos(lat2) * sin(dlon / 2)**2
    c = 2 * atan2(sqrt(a), sqrt(1 - a))
    radius = 6371  # 지구 반지름 (단위: km)
    return radius * c

async def get_bus_time(): 
    while True:
        driver = webdriver.Chrome(options=options)
        driver.get(url)
        time.sleep(3)

        try:
            ### 아산캠퍼스 <-> 천안캠퍼스 구간 (월~금) 정류장 : 7개(양방향 14개) ###
            element = driver.find_element(By.XPATH, '//*[@id="body"]/div[2]/div[5]/table')
            value = element.text.split('도착')[4].split('\n')
            value.pop(0)
            doc_ref = db.collection("main").document("time")
            parsed = []
            for x in range(len(value)):
                temp = value[x].split(' ')
                temp.pop(0)
                if temp[0] == '***':
                    continue
                elif '직행' in temp:
                    fixed = [_ for _ in range(14)]
                    fixed[0] = temp[0]
                    fixed[1] = temp[1]
                    fixed[12] = temp[6]
                    fixed[13] = temp[7]
                    parsed.append(fixed)
                else:
                    parsed.append(temp)
            result = [list(pair) for pair in zip(*parsed)]

            for i in range(len(result)):
                if (i < 7):
                    doc_ref.set({"아->천": {stations[i]: [x for x in result[i] if not isinstance(x, int)]}}, merge=True)
                else:
                    doc_ref.set({"천->아": {list(reversed(stations))[i - len(stations)]: [x for x in result[i] if not isinstance(x, int)]}}, merge=True)

            print("A new time has been uploaded.")

            driver.quit()
            await asyncio.sleep(1440)
        except Exception as e:
            print(f"Err: {e}")

def run_flask():
    server.run(debug=True, use_reloader=False, host='0.0.0.0')

if __name__ == "__main__":
    try:
        threading.Thread(target=run_flask).start()
        asyncio.run(get_bus_time())
    except KeyboardInterrupt:
        exit(1)
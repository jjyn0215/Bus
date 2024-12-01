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
from zoneinfo import ZoneInfo
import json
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



@server.route('/time', methods=['GET'])
def send_back_time():
    # 가장 가까운 위치 찾기
    print(json.loads(request.get_data(), encoding='utf-8'))
    my_location = [36.735, 127.074]
    min_distance = float('inf')
    locations = db.collection("loc").document("아<->천").get().to_dict()
    for name, coords in locations.items():
        distance = haversine(my_location[0], my_location[1], coords[0], coords[1])
        if distance < min_distance:
            min_distance = distance
            closest = name
    print(closest)
    print(datetime.now())
    test = db.collection("main").document("time").get().to_dict()
    print(test)
    time_dts = [datetime.now().replace(hour=int(t.split(':')[0]), minute=int(t.split(':')[1]), second=0, microsecond=0) for t in test['아->천'][closest]]
    time_dts_2 = [datetime.now().replace(hour=int(t.split(':')[0]), minute=int(t.split(':')[1]), second=0, microsecond=0) for t in test['천->아'][closest]]
    try:
        print(datetime.strftime(min([t for t in time_dts if t >= datetime.now()], key=lambda x: abs(x -  datetime.now())), '%H:%M'))
        print(datetime.strftime(min([t for t in time_dts_2 if t >= datetime.now()], key=lambda x: abs(x - datetime.now())), '%H:%M'))
    except:
        
        print(datetime.strftime(time_dts[0], '%H:%M'))
        print(datetime.strftime(time_dts_2[0], '%H:%M'))
    data = {
        "station": closest,

    }
    return json.dumps(data, ensure_ascii=False)

# @server.route('/station', method)

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

stations = ['아산캠퍼스', '천안아산역', '쌍용2동', '충무병원', '천안역', '천안터미널', '천안캠퍼스']

async def get_bus_time(): 
    while True:
        driver = webdriver.Chrome(options=options)
        driver.get(url)
        time.sleep(3)

        try:
            ### 아산캠퍼스 <-> 천안캠퍼스 구간 정류장 : 7개(양방향 14개) ###
            for k in range(3):
                if k == 0:
                    element = driver.find_element(By.XPATH, '//*[@id="body"]/div[2]/div[5]/table/tbody')
                    doc_ref = db.collection("main").document("아<->천(평일)")
                elif k == 1:
                    element = driver.find_element(By.XPATH, '//*[@id="body"]/div[2]/div[8]/table/tbody')
                    doc_ref = db.collection("main").document("아<->천(토요일)")
                elif k == 2:
                    element = driver.find_element(By.XPATH, '//*[@id="body"]/div[2]/div[9]/table/tbody')
                    doc_ref = db.collection("main").document("아<->천(일요일)")
            
                value = element.text.split('\n')
                parsed = []
                if k == 0:
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
                else:
                    for x in range(len(value)):
                        temp = value[x].split(' ')
                        temp.pop(0)
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

   
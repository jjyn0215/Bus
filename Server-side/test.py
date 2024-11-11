from selenium import webdriver
from selenium.webdriver.common.by import By

import time

driver_path = "C:\chromedriver_win32\chromedriver.exe"  # ChromeDriver가 설치된 위치를 입력하세요.
url = 'http://www.hoseo.ac.kr/Home/Contents.mbz?action=MAPP_2302082206'

# 1. 브라우저 열기
driver = webdriver.Chrome()
driver.get(url)

# 2. 페이지가 완전히 로드될 때까지 잠시 대기
time.sleep(3)

# 3. XPath를 사용하여 요소 찾기 (예: 특정 텍스트가 있는 div 요소)
try:
    # 예: XPath가 '//*[@id="some-id"]/div[2]/span'인 요소를 찾음
    element = driver.find_element(By.XPATH, '//*[@id="body"]/div[2]')
    
    # 요소의 텍스트 출력
    print(element.text)
except Exception as e:
    print(f"Element not found: {e}")

# 4. 브라우저 닫기
driver.quit()


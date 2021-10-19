#
# Copyright (c) 2021, Shashank Verma <shashank.verma2002@gmail.com>(shank03)
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#

import base64
import json
import sys
from time import sleep

from selenium import webdriver

if len(sys.argv) <= 1:
    print("Missing args")
    exit(1)


def index_of(string: str, token: str) -> int:
    _index = -1
    try:
        _index = string.index(token)
    except ValueError:
        _index = -1
    return _index


def write_html(html_source: str):
    file = open("data/ref.html", "w", encoding='utf-8')
    file.write(html_source)
    file.close()


def write_stat(msg: str, _print: bool = False):
    if _print:
        print(msg)

    stat_file = open("data/stat.txt", "w")
    stat_file.write(msg)
    stat_file.close()


options = webdriver.ChromeOptions()
options.add_argument("user-data-dir=D:\\Avishkar\\OffCoder\\data\\chrome_cache")
options.add_argument('headless')

driver = webdriver.Chrome(executable_path='D:\\Avishkar\\OffCoder\\data\\chromedriver.exe', chrome_options=options)

cmd_arg: str = str(sys.argv[1])

if cmd_arg == 'login':
    driver.get("https://codeforces.com")
    html: str = driver.page_source
    index = index_of(html, 'handle = ')

    if index == -1:
        print("Login")

        user_dat = open('data/cf_coder.dat')
        data = json.load(user_dat)

        handle = data['handle']
        password = str(base64.b64decode(data['password']).decode('utf-8'))
        auto_login = bool(data['auto_login'])

        driver.get("https://codeforces.com/enter?back=%2Fproblemset%3Forder%3DBY_RATING_ASC")
        driver.find_element_by_xpath('//*[@id="handleOrEmail"]').send_keys(handle)
        driver.find_element_by_xpath('//*[@id="password"]').send_keys(password)
        if auto_login:
            driver.find_element_by_xpath('//*[@id="remember"]').click()
        driver.find_element_by_xpath('//*[@id="enterForm"]/table/tbody/tr[4]/td/div[1]/input').click()

        sleep(3)
        html = driver.page_source
        html = str(html)
        index = index_of(html, 'handle = ')
        print(html[index + 10:html.index('"', index + 10)])
    else:
        print("Logged in")

    write_html(str(html))
    driver.quit()

elif cmd_arg == 'logout':
    driver.get("https://codeforces.com")
    driver.find_element_by_link_text('Logout').click()
    sleep(2)

    write_html(str(driver.page_source))
    driver.quit()

elif cmd_arg == 'get_page':
    driver.get(str(sys.argv[2]))
    sleep(1)
    write_html(str(driver.page_source))

    driver.quit()

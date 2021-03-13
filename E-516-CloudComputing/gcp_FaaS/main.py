from selenium import webdriver
from bs4 import BeautifulSoup
#import urllib.request

import os
from flask import Flask, request, jsonify

app = Flask(__name__)

#@app.route('/')
@app.route('/input', methods=['POST', 'GET'])
def hello_world():
    """Responds to any HTTP request.
    Args:
        request (flask.Request): HTTP request object.
    Returns:
        The response text or any set of values that can be turned into a
        Response object using
        `make_response <http://flask.pocoo.org/docs/1.0/api/#flask.Flask.make_response>`.
    """
    
    if request.method == 'GET':
        string_url = request.args.get("link")

        # string_url = 'http://www.google.com'
        # f = urllib.request.urlopen(string_url)
        # lines = f.readlines()
        # return 'this is it...'

        chrome_options = webdriver.ChromeOptions()
        chrome_options.add_argument("--headless")
        chrome_options.add_argument("--disable-gpu")
        chrome_options.add_argument("window-size=1024,768")
        chrome_options.add_argument("--no-sandbox")

        driver = webdriver.Chrome(chrome_options=chrome_options, executable_path='/usr/bin/chromedriver')
        driver.get(string_url)
        
        page_html = driver.page_source
        soup = BeautifulSoup(page_html, "html.parser")
        
        per_line_list = []
        tags = soup.body.find_all('span')
        for tag in tags:
            per_line_list.append(tag.text)
            
        frequency_of_length = {}
        for v in per_line_list:
            length = len(v.split())
            if length not in frequency_of_length.keys():
                frequency_of_length[length] = 1
            else:
                frequency_of_length[length] = frequency_of_length[length] + 1
                
        return str(frequency_of_length)
    
    elif request.method == 'POST':
        
        string_url = request.form['url_input']
        
        chrome_options = webdriver.ChromeOptions()
        chrome_options.add_argument("--headless")
        chrome_options.add_argument("--disable-gpu")
        chrome_options.add_argument("window-size=1024,768")
        chrome_options.add_argument("--no-sandbox")

        driver = webdriver.Chrome(chrome_options=chrome_options, executable_path='/usr/bin/chromedriver')
        driver.get(string_url)
        
        page_html = driver.page_source
        soup = BeautifulSoup(page_html, "html.parser")
        
        per_line_list = []
        tags = soup.body.find_all('span')
        for tag in tags:
            per_line_list.append(tag.text)
            
        frequency_of_length = {}
        for v in per_line_list:
            length = len(v.split())
            if length not in frequency_of_length.keys():
                frequency_of_length[length] = 1
            else:
                frequency_of_length[length] = frequency_of_length[length] + 1
                
        return str(frequency_of_length)


if __name__ == "__main__":
    app.run(debug=True, host='0.0.0.0', port=int(os.environ.get('PORT', 8080)))

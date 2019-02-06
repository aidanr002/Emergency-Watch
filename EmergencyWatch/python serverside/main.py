from bs4 import BeautifulSoup
import requests
import json
import time
from requests.adapters import HTTPAdapter
from requests.packages.urllib3.util.retry import Retry

session = requests.Session()
retry = Retry(connect = 3, backoff_factor = 0.5)
adapter = HTTPAdapter(max_retries = retry)
session.mount('https://', adapter)
session.mount('http://', adapter)
while True:
    data = {}
    data['events'] = []

    #fire
    INFORMATION_FIRE_ICON = "emergencywatch.cyclonewebservices.com/01_NOTIFICATION.bmp"
    ADVICE_FIRE_ICON = "https://www.ruralfire.qld.gov.au/PublishingImages/01_ADVICE_K-edge_96RGB_30px.png"
    WATCHACT_FIRE_ICON = "https://www.ruralfire.qld.gov.au/PublishingImages/02_WATCH_K-edge_96RGB_30px.png"
    EMERGENCY_FIRE_ICON = "https://www.ruralfire.qld.gov.au/PublishingImages/03_EMERGENCY_K-edge_96RGB_30px.png"

    #QLD Rural Fire Service

    #Loads link as focus source
    source = session.get('https://www.qfes.qld.gov.au/data/alerts/bushfireAlert.xml', verify = False).text
    #Creates object with this source
    soup = BeautifulSoup(source, 'lxml')

    # Goes through the soup object and for each class it iterates through some extractions
    for entry in soup.find_all('entry'):

        # Since it is QRFS, the type is set to generic fire
        event_type = 'Fire'

        #Gets the level
        event_level = entry.find("category")['term']

        #Depending on level, sets icon
        if "information" in event_level.lower() or "notification" in event_level.lower():
            event_icon = INFORMATION_FIRE_ICON
        elif "advice" in event_level.lower():
            event_icon = ADVICE_FIRE_ICON
        elif "watch and act" in event_level.lower():
            event_icon = WATCHACT_FIRE_ICON
        elif "emergency warning" in event_level.lower():
            event_icon = EMERGENCY_FIRE_ICON
        else:
            event_icon = INFORMATION_FIRE_ICON

        #Gets written address
        event_title = entry.find('title').text

        #Gets updated time
        event_time = entry.find('updated').text

        #Gets the description
        event_content = entry.find("content").text
        event_content.replace('<div>','')
        event_content.replace('</div>','')
        event_content.replace('<b>','\n')
        # Gets the set of coord's and sets them to sperate variables
        event_lat, event_lng = entry.find("georss:point").text.split(" ")


        data['events'].append({
            'event_heading': event_type + ": " + event_level,
            'location': event_title,
            'time': event_time,
            'description': event_content,
            'event_icon': event_icon,
            'event_lat': event_lat,
            'event_lng': event_lng
        })

    with open('events.json', 'w') as outfile:
        json.dump(data, outfile)

    print ('Completed Scrape. Sleeping for 5 minutes')
    time.sleep(300)

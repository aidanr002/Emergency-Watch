from bs4 import BeautifulSoup
import requests
import json
import time
from requests.adapters import HTTPAdapter
from requests.packages.urllib3.util.retry import Retry
from datetime import datetime
from dateutil import tz
from scraper_cleanup import character_ord_check
from scraper_cleanup import tag_removal
from scraper_cleanup import content_scraper_scraper
from scraper_cleanup import url_removal_in_description
from scraper_cleanup import special_tag_removal
from scraper_cleanup import tag_removal_for_linebreak
from scraper_cleanup import extra_character_removal

    #Start of loop for NSW RFS
def get_nsw_fire_events(data):
    session = requests.Session()
    retry = Retry(connect = 3, backoff_factor = 0.5)
    adapter = HTTPAdapter(max_retries = retry)
    session.mount('https://', adapter)
    session.mount('http://', adapter)

    INFORMATION_FIRE_ICON = "http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/information.png"
    ADVICE_FIRE_ICON = "http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/yellowfire.png"
    WATCHACT_FIRE_ICON = "http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/orangefire.png"
    EMERGENCY_FIRE_ICON = "http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/redfire.png"

    #Loads link as focus source
    source = session.get('http://www.rfs.nsw.gov.au/feeds/majorIncidents.xml', verify = False).text
    #Creates object with this source
    soup = BeautifulSoup(source, 'lxml')
    channel  = soup.find('channel')
    # Goes through the soup object and for each class it iterates through some extractions
    for entry in channel.find_all('item'):
        # Since it is NSW RFS, the type is set to generic fire
        event_type = 'Fire'
        #Gets the level
        event_level = entry.find("category").text

        #Depending on level, sets icon
        if "information" in event_level.lower() or "notification" in event_level.lower():
            event_icon = INFORMATION_FIRE_ICON
        elif "advice" in event_level.lower():
            event_icon = ADVICE_FIRE_ICON
        elif "watch and act" in event_level.lower():
            event_icon = WATCHACT_FIRE_ICON
        elif "emergency warning" in event_level.lower():
            event_icon = EMERGENCY_FIRE_ICON
        elif "not applicable" in event_level.lower():
            event_icon = INFORMATION_FIRE_ICON
        else:
            event_icon = INFORMATION_FIRE_ICON

        #Gets written address
        event_title = entry.find('title').text
        event_title = character_ord_check(event_title)

        #Gets updated time
        event_time = entry.find("pubdate").text
        event_time = event_time.replace(' GMT', '')
        from_zone = tz.gettz('UTC')
        to_zone = tz.gettz('Australia/Brisbane')
        utc = datetime.strptime(event_time, '%a, %d %b %Y %H:%M:%S')
        utc = utc.replace(tzinfo=from_zone)
        event_time = utc.astimezone(to_zone)
        event_time_converted = event_time.isoformat()

        #Seperates into usable parts
        year = "%d" % event_time.year
        month = "%d" % event_time.month
        day = "%d" % event_time.day
        hour = "%d" % event_time.hour
        minute = "%d" % event_time.minute

        if int(minute) < 10:
            minute = "0" + minute

        #Concatanate the parts into the ideal string
        if int(hour) > 12:
            hour = int(hour) % 12
            event_time = str(hour) + ':' + minute + 'pm ' + day + '/' + month + '/' + year
        elif int(hour) == 12:
            event_time = str(hour) + ':' + minute + 'pm ' + day + '/' + month + '/' + year
        elif int(hour) < 12:
            event_time = str(hour) + ':' + minute + 'am ' + day + '/' + month + '/' + year

        #Gets the description
        event_content = entry.find("description")
        event_content = tag_removal_for_linebreak(event_content)
        event_content = extra_character_removal(event_content)

        # Gets the set of coord's and sets them to sperate variables
        event_lat, event_lng = entry.find("point").text.split(" ")

        data['events'].append({
            'event_heading': event_type + ": " + event_level,
            'location': event_title,
            'time': event_time,
            'description': event_content,
            'event_icon': event_icon,
            'event_lat': event_lat,
            'event_lng': event_lng
        })
    return data

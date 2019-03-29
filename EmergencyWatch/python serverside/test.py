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

#Email alert
import smtplib, ssl
import traceback

port = 465  # For SSL
smtp_server = "smtp.gmail.com"
sender_email = "emergencywatchalert@gmail.com"  # Enter your address
receiver_email = "aidanr002@gmail.com"  # Enter receiver address
password = 'nice try'
message = """\
Subject: Warning - Crash Alert
This message is sent from Python. \n"""

def send_error_email(e):
    just_the_string = traceback.format_exc()
    server = smtplib.SMTP('smtp.gmail.com:587')
    server.ehlo()
    server.starttls()
    server.login(sender_email, password)
    server.sendmail(sender_email, receiver_email, (message + just_the_string))
    server.close()
    time.sleep(86400)


CODE_VERSION = "Version 1.9 - 29 / 3 / 2019"

session = requests.Session()
retry = Retry(connect = 3, backoff_factor = 0.5)
adapter = HTTPAdapter(max_retries = retry)
session.mount('https://', adapter)
session.mount('http://', adapter)
while True:
#try:
    print ('here1')
    data = {}
    data['events'] = []


    #fire
    INFORMATION_FIRE_ICON = "http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/information.png"
    ADVICE_FIRE_ICON = "http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/yellowfire.png"
    WATCHACT_FIRE_ICON = "http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/orangefire.png"
    EMERGENCY_FIRE_ICON = "http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/redfire.png"

        #Start of loop for NSW RFS
    #Loads link as focus source
    source = session.get('http://www.rfs.nsw.gov.au/feeds/majorIncidents.xml', verify = False).text
    #Creates object with this source
    soup = BeautifulSoup(source, 'lxml')
    print ('here2')
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
            continue
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
        event_content = entry.find("description").text
        #event_content = content_scraper_scraper(event_content)
        event_content  = character_ord_check(event_content)
        event_content = tag_removal_for_linebreak(event_content)
        event_content = url_removal_in_description(event_content)
        #event_content = special_tag_removal(event_content)

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
        print (data)
        #End of loop for NSW RFS
###
### Start of general script
###

    # Last updated time
    #Gets the time string
    currentDT = datetime.now()

    from_zone = tz.gettz('GMT')
    to_zone = tz.gettz('Australia/Brisbane')
    utc = currentDT.replace(tzinfo=from_zone)
    currentDT = utc.astimezone(to_zone)

    #Seperates into usable parts
    year = "%d" % currentDT.year
    month = "%d" % currentDT.month
    day = "%d" % currentDT.day
    hour = "%d" % currentDT.hour
    minute = "%d" % currentDT.minute

    if int(minute) < 10:
        minute = "0" + minute

    #Concatanate the parts into the ideal string
    if int(hour) > 12:
        hour = int(hour) % 12
        last_updated = 'Last Updated: ' + str(hour) + ':' + minute + 'pm ' + day + '/' + month + '/' + year
    elif int(hour) == 12:
        last_updated = 'Last Updated: ' + str(hour) + ':' + minute + 'pm ' + day + '/' + month + '/' + year
    elif int(hour) < 12:
        last_updated = 'Last Updated: ' + hour + ':' + minute + 'am ' + day + '/' + month + '/' + year
    print (last_updated)

    #Add to dict
    data['last_updated'] = []
    data['last_updated'].append({
    'last_updated': last_updated,
    'code_version': CODE_VERSION
    })

    with open('events.json', 'w') as outfile:
        json.dump(data, outfile, default=str)

    print ('Completed Scrape. Sleeping for 5 minutes')
    print (CODE_VERSION)
    time.sleep(300)

    # except IOError as e:
    #     print('An error occured trying to read the file.')
    #     send_error_email(e)
    #
    # except ValueError as  e:
    #     print('Non-numeric data found in the file.')
    #     send_error_email(e)
    #
    # except ImportError as e:
    #     print ("NO module found")
    #     send_error_email(e)
    #
    # except EOFError as e:
    #     print('Why did you do an EOF on me?')
    #     send_error_email(e)
    #
    # except NameError as e:
    #     send_error_email(e)
    #
    # except:
    #     print('An error occured.')
    #     time.sleep(300)

        # #context = ssl.create_default_context()
        # with smtplib.SMTP_SSL(smtp_server, port, context=context) as server:
        #     server.login(sender_email, password)
        #     server.sendmail(sender_email, receiver_email, message)
        #     server.close()
        #     time.sleep(86400)

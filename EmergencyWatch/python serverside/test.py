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
    data = {}
    data['events'] = []

    #fire
    INFORMATION_FIRE_ICON = "http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/information.png"
    ADVICE_FIRE_ICON = "http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/yellowfire.png"
    WATCHACT_FIRE_ICON = "http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/orangefire.png"
    EMERGENCY_FIRE_ICON = "http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/redfire.png"
    APP_ICON = 'http://images001.cyclonewebservices.com/wp-content/uploads/2019/03/triangle.png'

        #Start of loop for WA Fire
    #Loads link as focus source
    source = session.get('https://www.emergency.wa.gov.au/data/message_DFESCap.xml', verify = False).text
    #Creates object with this source
    soup = BeautifulSoup(source, 'lxml')

    # Goes through the soup object and for each class it iterates through some extractions
    for entry in soup.find_all('cap:alert'):
        #event_type = 'Fire'
        #Gets the level
        if 'fire' in entry.find("cap:category").text.lower():
            event_level = entry.find("cap:severity").text

            #For the emergency type fire, add one of the fire icons
            #Depending on level, sets icon
            if "unknown" in event_level.lower() or "minor" in event_level.lower():
                event_icon = INFORMATION_FIRE_ICON
            elif "moderate" in event_level.lower():
                event_icon = ADVICE_FIRE_ICON
            elif "severe" in event_level.lower():
                event_icon = WATCHACT_FIRE_ICON
            elif "extreme" in event_level.lower():
                event_icon = EMERGENCY_FIRE_ICON
            else:
                event_icon = INFORMATION_FIRE_ICON

            #Gets headline
            event_title = entry.find('cap:headline').text
            event_title = character_ord_check(event_title)

            #Gets updated time
            event_time = entry.find("cap:sent").text
            event_time = event_time.replace('+08:00', '')
            from_zone = tz.gettz('UTC')
            to_zone = tz.gettz('Australia/Perth')
            utc = datetime.strptime(event_time, '%Y-%m-%dT%H:%M:%S')
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

            #Makes the description
            event_headline = None
            event_category = None
            event_response = None
            event_urgency = None
            event_severity = None
            event_certainty = None
            event_areaDesc = None

            event_description = None
            event_instruction = None

            try:
                event_headline = entry.find("cap:headline").text
                event_category = entry.find("cap:category").text
                event_response = entry.find("cap:responseType").text
                event_urgency = entry.find("cap:urgency").text
                event_severity = entry.find("cap:severity").text
                event_certainty = entry.find("cap:certainty").text
                event_areaDesc = entry.find("cap:areaDesc").text

                event_description = entry.find("description").text
                event_instruction = entry.find("instruction").text

            except Exception:
                pass

            event_content = ''
            if event_category != None and event_headline != None:
                event_content += event_category + ": " + event_headline + "\n"
            if event_areaDesc != None:
                event_content += 'Location: ' + event_areaDesc + '\n'
            if event_severity !=  None:
                event_content += "Severity: " + event_severity + '\n'
            if event_urgency != None:
                event_content += "Urgency: " + event_urgency + '\n'
            if event_certainty != None:
                event_content += "Certainty: " + event_certainty +  '\n'
            if event_response != None:
                event_content += "Response: " + event_response + '\n'
            if event_description != None:
                event_content += "Description: " + event_description + '\n'
            if event_instruction != None:
                event_content += "Instructions: " + event_instruction + '\n'

            event_content  = character_ord_check(event_content)
            event_content = tag_removal_for_linebreak(event_content)
            event_content = special_tag_removal(event_content)

            # Gets the set of coord's and sets them to sperate variables
            event_lat_long, throw_away = entry.find("cap:circle").text.split(" ")
            event_lat, event_lng = event_lat_long.split(",")

            data['events'].append({
                'event_heading': event_title,
                'location': 'Unknown',
                'time': event_time,
                'description': event_content,
                'event_icon': event_icon,
                'event_lat': event_lat,
                'event_lng': event_lng
            })
            print (data)

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
    #
    #     #context = ssl.create_default_context()
    #     with smtplib.SMTP_SSL(smtp_server, port, context=context) as server:
    #         server.login(sender_email, password)
    #         server.sendmail(sender_email, receiver_email, message)
    #         server.close()
    #         time.sleep(86400)

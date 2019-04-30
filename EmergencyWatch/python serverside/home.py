from qldfire import get_qld_fire_events
from nswfire import get_nsw_fire_events
from last_updated import get_last_updated_time
import time
import json

#Email alert
import smtplib
import traceback

"""
Module Status:
Online : Connected to primary module of type
Error : Error reported. Disconnected
Offline : Disconnected. Running disconnection script
"""
#Modules:
QLD_FIRE = 'ONLINE'
NSW_FIRE = 'ONLINE'
LAST_UPDATED = 'ONLINE'

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


CODE_VERSION = "Version 3.0 - 30 / 4 / 2019"

while True:
    try:
        data = {}
        data['events'] = []


        #QLD FIRE Module Status Check
        if QLD_FIRE == 'ONLINE':
            try:
                data = get_qld_fire_events(data)
                print (" QLD_FIRE was successful")

            except Exception as e:
                print (e)
                QLD_FIRE = "ERROR"
                send_error_email(e)

        elif QLD_FIRE == 'OFFLINE':
            print ("Warning: QLD_FIRE is OFFLINE")

        elif QLD_FIRE == "ERROR":
            print ("Warning: QLD_FIRE is in ERROR")

        #NSW FIRE Module Status Check
        if NSW_FIRE == 'ONLINE':
            try:
                data = get_nsw_fire_events(data)
                print (" NSW_FIRE was successful")

            except Exception as e:
                print (e)
                NSW_FIRE = "ERROR"
                send_error_email(e)

        elif NSW_FIRE == 'OFFLINE':
            print ("Warning: NSW_FIRE is OFFLINE")

        elif NSW_FIRE == "ERROR":
            print ("Warning: NSW_FIRE is in ERROR")

        #Last Updated Module Status Check
        if LAST_UPDATED == 'ONLINE':
            try:
                data = get_last_updated_time(data, CODE_VERSION)
                print (" LAST_UPDATED was successful")
            except Exception as e:
                print (e)
                LAST_UPDATED = "ERROR"
                send_error_email(e)

        elif LAST_UPDATED == 'OFFLINE':
            print ("Warning: LAST_UPDATED is OFFLINE")

        elif LAST_UPDATED == "ERROR":
            print ("Warning: LAST_UPDATED is in ERROR")


        with open('events.json', 'w') as outfile:
            json.dump(data, outfile, default=str)

        print ('Completed Scrape. Sleeping for 5 minutes')
        print (CODE_VERSION)
        time.sleep(300)

    except Exception as e:
        send_error_email(e)

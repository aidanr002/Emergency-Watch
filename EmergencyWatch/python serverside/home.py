from qldfire import get_qld_fire_events
from nswfire import get_nsw_fire_events
from wafire import get_wa_fire_events
from last_updated import get_last_updated_time
from email_systems import send_error_email
import time
import json
import traceback

"""
Module Status:
Online : Connected to primary module of type
Error : Error reported. Disconnected
Offline : Disconnected. Running disconnection script
"""

#Modules:  Sets the initial state for the core modules
QLD_FIRE = 'ONLINE'
NSW_FIRE = 'ONLINE'
WA_FIRE = 'ONLINE'
LAST_UPDATED = 'ONLINE'

CODE_VERSION = "Version 3.3 - 7 / 9 / 2019" #Current code version for data stream tracking

SLEEP_TIME_MINUTES = 5 #Sets time in between scrapes
sleep_time_seconds = SLEEP_TIME_MINUTES * 60 #Converts the sleep time to seconds for time.sleep

while True: #Main logic loop running the scraper scripts
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

        #WA FIRE Module Status Check
        if WA_FIRE == 'ONLINE':
            try:
                data = get_wa_fire_events(data)
                print (" WA_FIRE was successful")

            except Exception as e:
                print (e)
                WA_FIRE = "ERROR"
                send_error_email(e)

        elif WA_FIRE == 'OFFLINE':
            print ("Warning: WA_FIRE is OFFLINE")

        elif WA_FIRE == "ERROR":
            print ("Warning: WA_FIRE is in ERROR")

        #Last Updated Module Status Check
        if LAST_UPDATED == 'ONLINE':
            try:
                last_updated, data = get_last_updated_time(data, CODE_VERSION)
                print (" LAST_UPDATED was successful")
            except Exception as e:
                print (e)
                LAST_UPDATED = "ERROR"
                send_error_email(e)

        elif LAST_UPDATED == 'OFFLINE':
            print ("Warning: LAST_UPDATED is OFFLINE")

        elif LAST_UPDATED == "ERROR":
            print ("Warning: LAST_UPDATED is in ERROR")

        #Saves the data in json file
        with open('events.json', 'w') as outfile:
            json.dump(data, outfile, default=str)

        #Update Module Status/es
        modules = {}  #Create dict for module status
        modules['qld_fire'] = QLD_FIRE
        modules['nsw_fire'] = NSW_FIRE
        modules['wa_fire'] = WA_FIRE
        modules['last_updated'] = LAST_UPDATED

        #Saves the module statuses in json file
        with open('module_statuses.json', 'w') as outfile:
            json.dump(modules, outfile, default=str)

        #Provide text outputs
        print ('Completed Scrape. Sleeping for '+ str(SLEEP_TIME_MINUTES) + ' minutes')
        print (last_updated)
        print (CODE_VERSION)
        time.sleep(sleep_time_seconds)

    #If error at any stage, send email.
    except Exception as e:
        send_error_email(e)
        print (e)
        quit()

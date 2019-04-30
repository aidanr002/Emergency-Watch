import time
from requests.adapters import HTTPAdapter
from requests.packages.urllib3.util.retry import Retry
from datetime import datetime
from dateutil import tz

def get_last_updated_time(data, CODE_VERSION):
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

    return data

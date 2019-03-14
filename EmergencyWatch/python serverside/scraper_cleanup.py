def character_ord_check(event_content):
    #Iterates through every character and if the ord of the character isn't  a letter, number or approved character, it ignores itself.
    temp_event_content = ''
    for character in event_content:
        #Gets ord and sets it to character_ord
        character_ord  = ord(character)
        if character_ord >= 32  and   character_ord <= 127:
            temp_event_content += character
        #Checks to see if it is a wanted special character
        if character_ord == 176:
            temp_event_content += character
    return temp_event_content

def tag_removal(event_content):
    #Removes some key tags from the  text entered  by replacing them with '' or \n
    event_content = event_content.replace('<div>','')
    event_content = event_content.replace('</div>','\n')
    event_content = event_content.replace('<b>',' ')
    event_content = event_content.replace('<br />',' ')
    #Returns the  result
    return event_content

def special_tag_removal(event_content):
    temp_event_content = ''
    inside_wanted_word_range = False
    for character in event_content:
        #Checks if character is the start of replacable string
        if character  == '<':
            inside_wanted_word_range = True
        if character == ">":
            inside_wanted_word_range = False
        if inside_wanted_word_range == False:
            temp_event_content += character
    return temp_event_content


def content_scraper_scraper(event_content):
    #Takes in the event content text. It splits this into individual words and then checks for the word 'details'. From here, it adds all other words to the returnable string.
    temp_event_content_result = ''
    event_description_words = []
    event_description_words = event_content.split(' ')
    inside_wanted_word_range = False
    for word in event_description_words:
        if 'details' in word.lower():
            inside_wanted_word_range  = True

        elif inside_wanted_word_range == True:
            temp_event_content_result += (word + " ")
    return temp_event_content_result

def url_removal_in_description(event_content):
    #Takes in the event content text. It splits this into individual words and then checks for the word 'details'. From here, it adds all other words to the returnable string.
    words_to_replace_string = ''
    event_description_words = []
    event_description_words = event_content.split(' ')
    inside_wanted_word_range = False
    for word in event_description_words:
        #Checks for first word
        if 'for' in word.lower():
            inside_wanted_word_range = True
        #If inside removal section and the second part it correct, add it to the list.
        if 'more:bit' in word.lower():
            words_to_replace_string += word
            inside_wanted_word_range = False
            event_content = event_content.replace('For ' + words_to_replace_string, '')
        #If the next word isn't on target stop
        else:
            words_to_replace_string = ''
            inside_wanted_word_range = False
    return event_content

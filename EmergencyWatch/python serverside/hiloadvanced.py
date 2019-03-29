
"""
Program name: HiLo Advanced
Description: a probability based guessing game
Programmer: Aidan Ryan
Date: 27/3/19
Version control: vs1 the base game
"""
import os
import random
import time
import csv

# declaring user experience features including colours
bold = "\033[1m"
reset = "\033[0m"
purple = "\033[95m"
blue = "\033[94m"
green = "\033[92m"
red = "\033[91m"
underline = "\033[4m"

#Preset variables
AVAILABLE_GUESSES = 5
#Printing the instructions
print  ("WELCOME TO HILO Advanced!")


def get_user_details():
    pass

def check_high_score():
    pass

def get_score_value(historic_score_data_element):
    for character in str(historic_score_data_element):
        if character in "123456789":
            score += character
    return int(score)

def get_probability(guesses_taken):
    probability_of_guess = (1 / (20 - guesses_taken)) * 100
    probability_of_guess  =  round(probability_of_guess, 2)
    return probability_of_guess

def scoreboard():
    with open('scorehistory.csv') as csvfile:
        readCSV = csv.reader(csvfile, delimiter=',')
        historic_score_data = []
        for row in readCSV:
            historic_score_data = row[0]
    historic_score_data = sorted(historic_score_data, key=get_score_value)
    historic_score_data  =  historic_score_data.replace('""', '')
    historic_score_data  =  historic_score_data.replace(',', '\n')
    print ("The top 5 scores are: ")
    print (scoreboard)


def hilo_game(mode_level):
    #Endless loop to  continue running logic.
    while True:
        #Generate the random number based on mode_level
        if mode_level == 'easy':
            computer_guess = random.randint(1,20)
            range_upper = 20
            range_lower = 1

        if mode_level == 'intermediate':
            computer_guess = random.randint(1,30)
            range_upper = 30
            range_lower = 1

        if mode_level == 'hard':
            computer_guess = random.randint(1,50)
            range_upper = 50
            range_lower = 1

        #Sets the guesses taken back to 0
        guesses_taken = 0

        #Sets the user guess to 0
        user_guess = 0

        #Sets score to 0
        score = 0
    #Loop to check if guesses remain
        if guesses_taken < AVAILABLE_GUESSES:
            #Run the body of the code
            #Try to get user input as a integer
            try:
                user_guess = int(input("Enter your guess: "))
            #if it fails, show an error message
            except ValueError:
                pass
            #Check for the number to be between 1-20
            if user_guess <= range_upper and user_guess > 0:
                if mode_level =-= 'hard':
                    user_probability = input('Enter the probability of guessing correctly (%): ')
                    if user_probability in get_probability():
                        print ('Correct. +1 point.')
                        score += 1
                    else:
                        print ("Incorrect, you have a " + str(get_probability(guesses_taken)) + "% chance of guessing correctly")
                else:
                    print ("You have a " + str(get_probability(guesses_taken)) + "% chance of guessing correctly")
                print ("Checking guess...")

                time.sleep(3)

                #Game check and increment logic
                if user_guess == computer_guess:
                    #Player wins
                    guesses_taken += 1
                    print ("You guessed correctly!")
                    score += (AVAILABLE_GUESSES - guesses_taken + 1)
                    print ("You scored " + str(score) + ' points.')
                    check_high_score()

                elif user_guess > computer_guess:
                    #guess is too high
                    print ("Too high")
                    guesses_taken += 1

                elif user_guess < computer_guess:
                    #User guess is too low
                    print ("Too low")
                    guesses_taken += 1

            #If the number is not within the range, give the user an error message.
            else:
                print ("Make sure the number you enter is within 1-20")

        else:
            #end game
            print ("You are out of guesses")
            break

def hilo_csv(file_name):
    #Endless loop to  continue running logic.
    while True:

    #Loop to check if guesses remain
        if guesses_taken < AVAILABLE_GUESSES:
            #Run the body of the code

            try:
                with open('guesses.csv') as csvfile:
                    readCSV = csv.reader(csvfile, delimiter=',')
                    user_guesses = []
                    for row in readCSV:
                        user_guesses = int(row[0])

            #if it fails, show an error message
            except ValueError:
                pass

            for user_guess in user_guesses:
                #Check for the number to be between 1-20
                if user_guess < 21 and user_guess > 0:
                    print ("Your guess was: " + str(user_guess)
                    print ("Checking guess...")
                    print ("You have a " + str(get_probability(guesses_taken)) + "% chance of guessing correctly")
                    time.sleep(3)
                    #Game check and increment logic
                    if user_guess == computer_guess:
                        #Player wins
                        guesses_taken += 1
                        print ("You guessed correctly!")
                        score = (AVAILABLE_GUESSES - guesses_taken + 1)
                        print ("You scored " + str(score) + ' points.')
                        break

                    elif user_guess > computer_guess:
                        #guess is too high
                        print ("Too high")
                        guesses_taken += 1

                    elif user_guess < computer_guess:
                        #User guess is too low
                        print ("Too low")
                        guesses_taken += 1

                #If the number is not within the range, give the user an error message.
                else:
                    print ("Make sure the numbers are within 1-20")

            else:
                #end game
                print ("You are out of guesses")
                break

def game_mode_menu():
    print ("Options go here")
    while True:
        user_selection = input('Make a selection: ')
        if user_selection == '1':
            mode_level = 'easy'
            hilo_game(mode_level)
        if user_selection == '2':
            mode_level = 'intermediate'
            hilo_game(mode_level)
        if user_selection == '3':
            mode_level = 'hard'
            hilo_game(mode_level)
        if user_selection == '4':
            break

def main_menu():
    print ("Options go here")
    while True:
        user_selection = input('Make a selection: ')
        if user_selection == '1':
            game_mode_menu()
        if user_selection == '2':
            hilo_csv()
        if user_selection == '3':
            scoreboard()
        if user_selection == '4':
            exit()

#Sets off the chain reaction
main_menu()

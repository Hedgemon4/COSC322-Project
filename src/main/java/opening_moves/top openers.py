########################################################
# Run via terminal with:
# python "src/main/java/opening_moves/top openers.py"
# Might need to install matplotlib via "pip install matplotlib"
########################################################

import csv

# Create a dictionary to store the number of times each move is made
moveCount = {}
# Open the csv file
with open('src/main/java/opening_moves/parsed_games.csv', 'r') as f:
    # Create a csv reader object
    reader = csv.reader(f)
    # Skip the header
    next(reader)
    # Loop through each row in the csv file
    for row in reader:
        # Check what the first move is
        firstMove = row[1]
        # If the move is not in the dictionary, add it
        if firstMove not in moveCount:
            moveCount[firstMove] = 0
        # Increment the move count
        moveCount[firstMove] += 1

# Sort the dictionary by the move count
moveCount = sorted(moveCount.items(), key=lambda x: x[1], reverse=True)
import matplotlib.pyplot as plt
minMoves = 0.01
# Graph the move as a pie chart and show any move that was played less than 1% of the time as "Other"
# Sum the total number of moves
totalMoves = sum([move[1] for move in moveCount])
labels = [move[0][2:-1] for move in moveCount if move[1] > minMoves * totalMoves]
sizes = [move[1] for move in moveCount if move[1] > minMoves * totalMoves]
labels.append('Other')
sizes.append(totalMoves - sum(sizes))
plt.pie(sizes, labels=labels, autopct='%1.1f%%', startangle=90)
plt.axis('equal')
plt.show()

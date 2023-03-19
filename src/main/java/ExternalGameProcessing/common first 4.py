# ########################################################
# # Run via terminal with:
# # python "src/main/java/ExternalGameProcessing/common first 4.py"
# ########################################################

import csv

# Find the first 4 moves that are common to all games
# Create a dictionary to store the number of times each move is made
moveCount = {}
totalGames = 0
# Open the csv file
with open('bin/training/parsed_games.csv', 'r') as f:
    # Create a csv reader object
    reader = csv.reader(f)
    # Skip the header
    next(reader)
    # Loop through each row in the csv file
    for row in reader:
        totalGames += 1
        # Combine the first 4 moves into a single string
        first4Moves = row[1] + row[2] + row[3] + row[4]
        # If the move is not in the dictionary, add it
        if first4Moves not in moveCount:
            moveCount[first4Moves] = 0
        # Increment the move count
        moveCount[first4Moves] += 1
    
# Print the total number of games
print('Total num games:', totalGames)
# Sort the dictionary by the move count
moveCount = sorted(moveCount.items(), key=lambda x: x[1], reverse=True)
# Print the top 10 moves
for move in moveCount[:10]:
    print(move)
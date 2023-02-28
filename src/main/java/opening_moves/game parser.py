########################################################
# Run via terminal with:
# python "src/main/java/opening_moves/game parser.py"
########################################################

import re, os

dir = 'src/main/java/opening_moves/txt_games'
with open('src/main/java/opening_moves/parsed_games.csv', 'w') as g:
    g.write('winner,moves...\n')
    # Open the folder with the games
    for filename in os.listdir(dir):
        # Open the file
        with open(dir + '/' + filename, 'r') as f:
            # Print all matches of the regex "\(;EV.+\)"gm
            games = re.findall(r'\(;EV.+\)', f.read(), re.MULTILINE)
            for game in games:
                # Make sure the game starts in the regular position, not the cross position
                crossStartIdentifiers = {'[B|W]\[d10':'W', '[B|W]\[g10':'W', '[B|W]\[a4':'B', '[B|W]\[j4':'B'}
                for position in crossStartIdentifiers:
                    match = re.search(position, game)
                    if match and match.group()[0] == crossStartIdentifiers[position]:
                        continue
                # Get each move of the game
                moves = re.findall(r';[W|B]\[[a-j]\d+\-[a-j]\d+/[a-j]\d+\]', game)
                if len(moves) <= 10:
                    continue
                # Get the winner of the game
                winner = moves[-1][1]
                g.write(winner + ',')
                # Write the moves to the csv file
                for move in moves:
                    g.write(move[1:] + ',')
                g.write('\n')
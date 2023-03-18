package OpeningMoves;

import State.Action;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class GenerateMoveDictionary {
    public static void main(String[] args) {
        String line = "";
        String splitBy = ",";
        HashMap<Integer, HashMap<Integer, ArrayList<Action>>> moveDictionary = new HashMap<>();
        for (int i = 0; i < 100; i ++){
            moveDictionary.put(i, new HashMap<>());
            for (int j = 0; j < 100; j++)
                moveDictionary.get(i).put(j, new ArrayList<>());
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader("bin/training/parsed_games.csv"));
            while ((line = br.readLine()) != null) {
                String[] array = line.split(splitBy);
                if (array.length < 26)
                    continue;
                for (int i = 1; i < 11; i++){
                    Action output = parseMove(array[i]);
                    int index = output.getOldX() + output.getOldY() * 10;
                    int move = output.getNewX() + output.getNewY() * 10;
                    moveDictionary.get(index).get(move).add(output);
                }
            }
            System.out.println(moveDictionary.get(3));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Action parseMove(String move) {
        move = move.substring(2, move.length() - 1);
        String[] parts = move.split("[-/]");
        int oldX = parts[0].charAt(0) - 'a';
        int oldY = Integer.parseInt(parts[0].substring(1)) - 1;
        int newX = parts[1].charAt(0) - 'a';
        int newY = Integer.parseInt(parts[1].substring(1)) - 1;
        int arrowX = parts[2].charAt(0) - 'a';
        int arrowY = Integer.parseInt(parts[2].substring(1)) - 1;
        return new Action(oldX, oldY, newX, newY, arrowX, arrowY);
    }
}

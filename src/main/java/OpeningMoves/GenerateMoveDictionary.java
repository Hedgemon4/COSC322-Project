package OpeningMoves;

import State.Action;

import java.io.*;
import java.util.*;

public class GenerateMoveDictionary {
    public static void main(String[] args) {
        String line = "";
        String splitBy = ",";
        int[] moveDictionary = new int[1000000];
        try {
            BufferedReader br = new BufferedReader(new FileReader("bin/training/parsed_games.csv"));
            while ((line = br.readLine()) != null) {
                String[] array = line.split(splitBy);
                if (array.length < 26)
                    continue;
                for (int i = 1; i < 11; i++) {
                    Action output = parseMove(array[i]);
                    StringBuilder sb = new StringBuilder();
                    int index = output.getOldX() + output.getOldY() * 10;
                    int move = output.getNewX() + output.getNewY() * 10;
                    int arrow = output.getArrowX() + output.getArrowY() * 10;
                    sb.append(arrow < 10 ? "0" : "").append(arrow);
                    sb.append(move < 10 ? "0" : "").append(move);
                    sb.append(index < 10 ? "0" : "").append(index);
                    moveDictionary[Integer.parseInt(sb.toString())] += 1;
                }
            }
            FileOutputStream fileOutputStream = new FileOutputStream("bin/training/moveDictionary.ser");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(moveDictionary);
            objectOutputStream.close();
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

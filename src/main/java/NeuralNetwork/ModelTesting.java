package NeuralNetwork;

import State.State;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ModelTesting {
    public static void main(String[] args) throws IOException, UnsupportedKerasConfigurationException, InvalidKerasConfigurationException {

        MultiLayerNetwork model = KerasModelImport.importKerasSequentialModelAndWeights(Files.newInputStream(Paths.get("bin/models/model1.h5")));

        State nodeState = new State(new ArrayList<>(Arrays.asList(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 1, 1, 3, 0, 0, 0, 0, 0, 0, 0,
                0, 1, 1, 3, 0, 0, 0, 0, 0, 0, 0,
                0, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 2, 2, 2, 2, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        )));

        INDArray input = Nd4j.zeros(1, 10, 10, 3);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int piece = nodeState.getPos(i,j);
                if (piece == State.BLACK_QUEEN) {
                    input.putScalar(new int[]{0, i, j, 0}, 1);
                } else if (piece == State.WHITE_QUEEN) {
                    input.putScalar(new int[]{0, i, j, 1}, 1);
                } else if (piece == State.ARROW) {
                    input.putScalar(new int[]{0, i, j, 2}, 1);
                }
            }
        }

        // Get the output of the model
        double[] result = model.output(input).data().getDoublesAt(0,2);;
        System.out.println(Arrays.toString(result));
    }

    static INDArray randomBoard() {
        Random r = new Random();

        int[][][] arr = new int[10][10][];

        // Place queens
        for (int i = 0; i < 4; i++) {
            int x, y;
            do {
                x = r.nextInt(10);
                y = r.nextInt(10);
            } while (arr[x][y] != null);

            arr[x][y] = new int[]{1, 0, 0};
        }
        for (int i = 0; i < 4; i++) {
            int x, y;
            do {
                x = r.nextInt(10);
                y = r.nextInt(10);
            } while (arr[x][y] != null);

            arr[x][y] = new int[]{0, 1, 0};
        }

        int numArrows = r.nextInt(35) + 5;
        for (int i = 0; i < numArrows; i++) {
            int x, y;
            do {
                x = r.nextInt(10);
                y = r.nextInt(10);
            } while (arr[x][y] != null);

            arr[x][y] = new int[]{0, 0, 1};
        }

        // Fill the rest with empty
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (arr[i][j] == null) {
                    arr[i][j] = new int[]{0, 0, 0};
                }
            }
        }

        return convert(arr);
    }

    public static INDArray convert(int[][][] arr) {
        int dim1 = arr.length;
        int dim2 = arr[0].length;
        int dim3 = arr[0][0].length;

        INDArray ndArr = Nd4j.zeros(dim1, dim2, dim3);

        for (int i = 0; i < dim1; i++) {
            for (int j = 0; j < dim2; j++) {
                for (int k = 0; k < dim3; k++) {
                    ndArr.putScalar(i, j, k, arr[i][j][k]);
                }
            }
        }

        return ndArr;
    }

    static INDArray joinArrays(INDArray... arrays) {
        long[] shape = arrays[0].shape();
        long[] newShape = new long[shape.length + 1];
        newShape[0] = arrays.length;
        System.arraycopy(shape, 0, newShape, 1, shape.length);
        return Nd4j.concat(0,arrays).reshape(newShape);
    }
}
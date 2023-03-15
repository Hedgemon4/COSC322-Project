package Training;

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

public class ModelTesting {
    public static void main(String[] args) throws IOException, UnsupportedKerasConfigurationException, InvalidKerasConfigurationException {

        MultiLayerNetwork model = KerasModelImport.importKerasSequentialModelAndWeights(Files.newInputStream(Paths.get("bin/models/model1.h5")));

        INDArray a = convert(new int[][][]{
                {{1, 0, 0}, {1, 0, 0}, {0, 0, 1}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}},
                {{1, 0, 0}, {1, 0, 0}, {0, 0, 1}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}},
                {{0, 0, 1}, {0, 0, 1}, {0, 0, 1}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}},
                {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}},
                {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}},
                {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}},
                {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}},
                {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}},
                {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}},
                {{0, 0, 0}, {0, 0, 0}, {0, 1, 0}, {0, 1, 0}, {0, 1, 0}, {0, 1, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}}
        });

        INDArray b = Nd4j.create(10, 10, 3);
        INDArray c = Nd4j.create(10, 10, 3);

        INDArray input = joinArrays(a, b, c);

        INDArray prediction = model.output(input);

        int player = State.WHITE_QUEEN - 1;
        int maxIndex = prediction.getColumn(player).argMax(0).getInt();
        System.out.println(prediction);
        System.out.println(prediction.getDouble(maxIndex, player));
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
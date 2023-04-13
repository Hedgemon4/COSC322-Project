package NeuralNetwork;

import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Model {
    private static MultiLayerNetwork[] models;

    public static void init(int numModels) {
        try {
            MultiLayerNetwork m = KerasModelImport.importKerasSequentialModelAndWeights(Files.newInputStream(Paths.get("bin/models/model1.h5")));

            models = new MultiLayerNetwork[numModels];

            for (int i = 0; i < numModels; i++)
                models[i] = m.clone();
        } catch (IOException | InvalidKerasConfigurationException | UnsupportedKerasConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static MultiLayerNetwork getModel(int i) {
        if (models == null)
            throw new RuntimeException("Model not initialized");
        return models[i];
    }
}

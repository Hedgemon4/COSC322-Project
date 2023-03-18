import datetime
import os

import keras.models
import numpy as np
from keras import Model, Sequential
from keras.layers import Input, Dense, Conv2D, Flatten, MaxPooling2D
from keras.metrics import top_k_categorical_accuracy


def default_model() -> Model:
    input_layer = Input((10, 10, 3))
    conv_1 = Conv2D(32, (3, 3), activation='relu', input_shape=(10, 10, 3))(input_layer)
    pool_1 = MaxPooling2D((2, 2))(conv_1)
    conv_2 = Conv2D(64, (3, 3), activation='relu')(pool_1)
    pool_2 = MaxPooling2D((2, 2))(conv_2)
    flat = Flatten()(pool_2)
    dense = Dense(128, activation='relu')(flat)
    old_queen = Dense(100, activation='softmax', name='old_queen')(dense)
    new_queen = Dense(100, activation='softmax', name='new_queen')(old_queen)
    arrow = Dense(100, activation='softmax', name='arrow')(new_queen)

    model = Model(inputs=input_layer, outputs=[old_queen, new_queen, arrow])

    model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy', top_k_categorical_accuracy])

    return model


if __name__ == '__main__':
    # Load Data
    input = []
    output = [[], [], []]
    print('Parsing training data', end='')
    for i, filename in enumerate(os.listdir('../../../../bin/training/ideal_moves')):
        print('.', end='')
        with open('../../../../bin/training/ideal_moves/' + filename, 'r') as file:
            for line in file:
                start, old_queen, new_queen, arrow = line.strip().split('\t')
                board = np.array(list(start), dtype=int).reshape(10, 10)
                final_board = np.zeros((10, 10, 3))
                final_board[:, :, 0] = board == 1
                final_board[:, :, 1] = board == 2
                final_board[:, :, 2] = board == 3
                input.append(final_board)
                output[0].append(list(map(lambda x: int(x), list(old_queen))))
                output[1].append(list(map(lambda x: int(x), list(new_queen))))
                output[2].append(list(map(lambda x: int(x), list(arrow))))
    print()

    train_x = np.array(input)
    train_y = [np.array(output[0]), np.array(output[1]), np.array(output[2])]

    # Load model
    model = default_model()

    # Fit model
    while datetime.datetime.now() < datetime.datetime(2023, 3, 4, 6, 30):
        model.fit(train_x, train_y, epochs=5, batch_size=64, validation_split=0.1)

    # Save model
    model.save('../../../../bin/models/full_model2.h5')  # save everything in HDF5 format

    model_json = model.to_json()  # save just the config. replace with "to_yaml" for YAML serialization
    with open("../../../../bin/models/model2_config.json", "w") as f:
        f.write(model_json)

    model.save_weights('../../../../bin/models/model2_weights.h5') # save just the weights.
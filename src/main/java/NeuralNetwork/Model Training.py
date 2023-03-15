import os

import keras.models
import numpy as np
from keras import Model, Sequential
from keras.layers import Input, Conv2D, MaxPooling2D, Flatten, Dense, Dropout


def default_model() -> Model:
    model = Sequential()
    model.add(Conv2D(32, (3, 3), activation='relu', input_shape=(10, 10, 3)))
    model.add(MaxPooling2D((2, 2)))
    model.add(Conv2D(64, (3, 3), activation='relu'))
    model.add(MaxPooling2D((2, 2)))
    model.add(Flatten())
    model.add(Dense(128, activation='relu'))
    model.add(Dense(2, activation='softmax'))

    model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy'])
    
    return model


if __name__ == '__main__':
    # Parse in data
    inputs = []
    outputs = []
    print('Parsing in data...', end='')
    for i, file in enumerate(os.listdir('../../../../bin/training/win_probabilities')):
        if i == None:
            break
        print('.', end='')
        with open('../../../../bin/training/win_probabilities/' + file, 'r') as f:
            for line in f:
                player, board_str, black_win_percentage, white_win_percentage = line.strip().split('\t')
                board = np.array(list(board_str[1:-1]), dtype=int).reshape(10, 10)
                final_board = np.zeros((10, 10, 3))
                final_board[:, :, 0] = board == 1
                final_board[:, :, 1] = board == 2
                final_board[:, :, 2] = board == 3
                inputs.append(final_board)
                outputs.append([float(black_win_percentage), float(white_win_percentage)])
                # Make values that are at the end of the game more common
                if abs(float(black_win_percentage) - 0.5) >= 0.3:
                    inputs.append(final_board)
                    outputs.append([float(black_win_percentage), float(white_win_percentage)])
                    if abs(float(black_win_percentage) - 0.5) >= 0.2:
                        inputs.append(final_board)
                        outputs.append([float(black_win_percentage), float(white_win_percentage)])

    train_x = np.array(inputs)
    train_y = np.array(outputs)

    # Shuffle data
    print('\nShuffling data...')
    permutation = np.random.permutation(train_x.shape[0])
    train_x = train_x[permutation]
    train_y = train_y[permutation]

    print('Train X size: {}'.format(train_x.shape))
    print('Train Y size: {}'.format(train_y.shape))

    # Compile model
    model = default_model()
    # model.summary()

    # Train model
    try:
        model.fit(train_x, train_y, epochs=250, batch_size=64, validation_split=0.1)
    finally:
        # Save model
        keras.models.save_model(model, '../../../../bin/models/model1.h5', include_optimizer=False)
        model.save(model, '../../../../bin/models/full_model1.h5')

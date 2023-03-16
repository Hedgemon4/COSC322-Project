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
    old_queen = Dense(100, activation='softmax')(dense)
    new_queen = Dense(100, activation='softmax')(old_queen)
    arrow = Dense(100, activation='softmax')(new_queen)

    model = Model(inputs=input_layer, outputs=[old_queen, new_queen, arrow])

    model.compile(optimizer='adam', loss='categorical_crossentropy', metrics=['accuracy', top_k_categorical_accuracy])

    return model


if __name__ == '__main__':
    # Load Data


    # Load model
    model = default_model()
    model.summary()

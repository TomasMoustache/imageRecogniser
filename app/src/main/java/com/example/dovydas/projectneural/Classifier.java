package com.example.dovydas.projectneural;

/**
 * Created by Dovydas on 2018.01.09.
 */
/**
 * Created by Piasy{github.com/Piasy} on 29/05/2017.
 */

public interface Classifier {
    String name();

    Classification recognize(final float[] pixels);
}
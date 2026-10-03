package com.darkgreen_world.flashcarts.config;

import java.io.Serializable;

public interface ICartConfig extends Serializable {
    boolean shouldUseExperimentalPhysics();
    int getMaxSpeed();
}
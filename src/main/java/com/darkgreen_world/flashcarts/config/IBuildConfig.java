package com.darkgreen_world.flashcarts.config;

public interface IBuildConfig {

    /**
     * Whether to enable the rail selection building feature, which allows players to build rails by right-clicking on a block with a rail in their hand.
     * @return whether to enable the rail selection building feature.
     */
    boolean isRailSelectionBuildingEnabled();

    /**
     * Get the maximum distance from the start point that the rail selection building feature can be used.
     * @return the maximum distance from the start point that the rail selection building feature can be used.
     */
    int getRailSelectionBuildingMaxDistance();

    /**
     * Get the frequency at which powered rails should be placed when using the rail selection building feature.
     * For example, if this is 3, a powered rail will be placed every 3 blocks.
     * @return the frequency at which powered rails should be placed when using the rail selection building feature.
     */
    int getPoweredRailFrequency();

    /**
     * Whether to show selection when using the rail selection building feature.
     * @return whether to show selection when using the rail selection building feature.
     */
    boolean shouldShowSelection();

    /**
     * Whether to enable the rail extend building feature, which allows players to extend rails by right-clicking on the end of a rail with a rail in their hand.
     * If selection building is also enabled, this will only work if the player is sneaking.
     * @return whether to enable the rail extend building feature.
     */
    boolean isRailExtendBuildingEnabled();

    /**
     * Get the maximum distance from the player that the rail extend building feature can be used.
     * @return the maximum distance from the player that the rail extend building feature can be used.
     */
    int getRailExtendBuildingMaxDistance();

}

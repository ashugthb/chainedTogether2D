package com.chainedclimber.utils;

import java.util.ArrayList;
import java.util.List;

import com.chainedclimber.entities.BouncyBlock;
import com.chainedclimber.entities.BreakableBlock;
import com.chainedclimber.entities.Checkpoint;
import com.chainedclimber.entities.Goal;
import com.chainedclimber.entities.IceBlock;
import com.chainedclimber.entities.MovingPlatform;
import com.chainedclimber.entities.Platform;
import com.chainedclimber.entities.Ramp;
import com.chainedclimber.entities.Spike;

/**
 * Container for all level entities
 */
public class LevelData {
    public List<Platform> platforms = new ArrayList<>();
    public List<Spike> spikes = new ArrayList<>();
    public List<BouncyBlock> bouncyBlocks = new ArrayList<>();
    public List<IceBlock> iceBlocks = new ArrayList<>();
    public List<MovingPlatform> movingPlatforms = new ArrayList<>();
    public List<BreakableBlock> breakableBlocks = new ArrayList<>();
    public List<Checkpoint> checkpoints = new ArrayList<>();
    public List<Goal> goals = new ArrayList<>();
    public List<Ramp> ramps = new ArrayList<>();
    
    public LevelData() {
    }
}

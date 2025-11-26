# Physics System V2: Positional Correction Chain

## Overview
This document describes the new physics system implemented to solve the "jittery chain" and "wall clipping" issues. The system moves away from spring-based forces (Hooke's Law) and uses a **Verlet-style Positional Correction** approach.

## The Pipeline
The physics update loop in `GameScreen.java` has been strictly ordered to prevent conflicts between the chain constraint and map collisions.

### Step 1: Moving Platforms
*   **Logic**: If a player is standing on a moving platform, the platform's frame delta is applied *directly* to the player's position.
*   **Why**: This ensures the player moves *with* the platform before any other forces are calculated.

### Step 2: Input & Forces
*   **Logic**: Player input (run/jump) and gravity are applied to the velocity.
*   **Integration**: `velocity += acceleration * dt`.

### Step 3: Prediction (Integration)
*   **Logic**: The potential new position is calculated: `position += velocity * dt`.
*   **Note**: We do *not* commit this as the final state yet. We are "predicting" where the player wants to go.

### Step 4: Chain Constraint (The Solver)
*   **Algorithm**: Positional Correction.
*   **Logic**:
    1.  Check distance between P1 and P2.
    2.  If `distance > maxLength`:
        *   Calculate `correctionVector` (how much to pull them together).
        *   Determine `weights` based on state:
            *   **Both Air**: 50% / 50% split.
            *   **P1 Grounded / P2 Air**: P1 0% (Anchor) / P2 100%.
            *   **P1 Air / P2 Grounded**: P1 100% / P2 0% (Anchor).
            *   **Both Grounded**: 50% / 50% (or minimal correction).
    3.  Apply correction directly to `position`.

### Step 5: Map Collision
*   **Logic**: Standard AABB collision resolution against the tile map.
*   **Crucial Detail**: This runs *after* the chain solver. If the chain pulls a player into a wall, this step pushes them back out, preventing the "Wall Clip" bug.

## Key Classes
*   `ChainPhysics.java`: Contains the solver logic and rendering.
*   `GameScreen.java`: Orchestrates the pipeline.

## Tuning
*   `CHAIN_LENGTH`: Currently set to **150.0** units.
*   `SOLVER_ITERATIONS`: Currently set to **1** (sufficient for simple 2-player chain). Increase if chain feels "stretchy".

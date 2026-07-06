# trace.py flood-fill bleeds ~238m into adjacent same-colored pavement

**Severity:** sev2  
**Date:** 2026-05-31

## Root cause

K-means LAB segmentation assigns asphalt track and surrounding gravel/pavement to the same cluster. Flood-fill from seed pixels has no spatial constraint and spreads across any connected same-cluster region.

## Resolution

Added --linestring mode with build_corridor_mask(): rasterizes the linestring in pixel space, dilates by N metres, applies as binary mask before and after flood-fill. Constrains fill to within the corridor.

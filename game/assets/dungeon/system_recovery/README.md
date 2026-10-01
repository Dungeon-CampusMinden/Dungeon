# System Recovery tile set

This design is for the AI research facility in the System Recovery escape room. Its visual language
is clean engineered surfaces: blue-gray alloy panels, narrow rails, sparse cyan access lights, and
restrained amber hazard marks. Wall variants retain the default set's per-tile depth and recess
map, including opaque dark cavities; the pixel values are remapped to cool alloy and isolated noise
is reduced. Highlights and shadows follow each variant's structure instead of repeating one generic
panel bevel. The floor uses flush service panels; doors are square sliding bulkheads.

All 119 PNGs keep the source 16×16 canvas, PNG format, color mode, and asset paths. Wall, floor,
portal, glass, and conduit tiles retain their original alpha masks, which encode wall joins and
element silhouettes. Door art is redrawn within the same tile bounds: open doors have a clear
passage, while closed doors have a solid shutter. PNG `Source` fields identify their matching
default files. Every PNG has a CC0 1.0 sidecar naming AMatutat as author and noting generative
AI use. Each sidecar records the matching default tile and preserves its known original-author
attribution.

## How the level selects these files

`levels/systemRecovery/systemrecovery_1.level` selects `SYSTEM_RECOVERY`. The renderer resolves the
matching neighborhood-aware tile to this folder. Wall sprites encode how a wall joins neighboring
wall/floor cells, so variants such as corners, T-junctions, and ends retain their original alpha
silhouettes. Floor holes use `floor_hole.png` or `floor_hole1.png` depending on the tile above.
Both variants use the default hole tile's broad lattice as a base, recolored as a steel security
grille with cyan emitter pixels and a high-contrast segmented amber barrier strip. The continuation
tile places its emitter markers at the lower edge so vertical runs keep the same grille rhythm.

Doors use the orientation that faces the room. When a door closes, `DoorTile` appends `_closed` to
that path; System Recovery closes its doors at level start, so the directional `_closed.png`
variants are visible until a puzzle unlocks them. Their shutters use a deeper center seam, clear
upper-left bevel, and restrained amber warning strip. Open doors use the matching unsuffixed file,
with cyan-lit side rails and a transparent passage.

Portal, glass-wall, and grate elements normally resolve to the shared default set. System Recovery
has matching copies here and routes those three element types to this design so the room remains
self-contained. The level editor's tile picker uses the same themed files.

The client warms the unique tile textures used by the loaded map on its first tick. This keeps
first-use PNG decoding out of later draw frames; both open and closed door variants are included.

The whole default set is retained, including variants not currently present in the System Recovery
map. They remain available for later map edits and for the shared tile resolver's optional element
types.

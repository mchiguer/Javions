# Javions — partial repository

Why only part of the project? 
This repo contains only the `Javions` source code 
Large and generated files (e.g. ADS-B recordings .bin > 100 MB, OSM tile cache, `out/`) are not versioned to keep the repo lightweight and within GitHub limits.
**If you need the rest of the project (large datasets/binaries and caches), please contact me directly.**

## Goals of this project
- Render a smooth, interactive map with aircraft overlays.
- Parse raw ADS-B frames and maintain up-to-date aircraft states.
- Provide a usable UI: table view, status line, and quick navigation.

## Included
- Java sources (JavaFX GUI, ADS-B parsing ...).

## Excluded (not pushed)
- ADS-B recordings very large.
- OSM tile cache.
- Build outputs. 

## Prerequisites
JDK 21 recommended or  JDK 19 + JavaFX SDK 

## How to run 
Run without arguments to read from System.in via AdsbDemodulator.

java --add-modules=javafx.controls,javafx.graphics \
     -cp "out:resources" ch.epfl.javions.gui.Main




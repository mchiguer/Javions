# Javions — partial repository

Why only part of the project? 
This repo contains only the `Javions` source code 
Large and generated files (e.g. ADS-B recordings .bin > 100 MB, OSM tile cache, `out/`) are not versioned to keep the repo lightweight and within GitHub limits.
**If you need the rest of the project (large datasets/binaries and caches), please contact me directly.**

## Goals of this project
Build a program that **decodes** ADS-B messages from an SDR and **plots** the emitting aircraft on a map.

## Background

ADS-B messages are transmitted on **1090 MHz**. They can be received with a dedicated receiver or with a **software-defined radio (SDR)** connected to an antenna and a computer running a decoder.

In this project we use an **AirSpy R2** SDR: once tuned to the target frequency, it digitizes the RF signal from the antenna and streams it to the computer, where Javions **decodes** the frames.

Reception requires **line-of-sight** between aircraft and antenna. Even with good conditions, the **Earth’s curvature** limits range to a few **hundred kilometers**. With an antenna near a rooftop in central **Lausanne**, Javions will display aircraft only **around Lausanne**.

To cover larger areas, one could aggregate ADS-B data from many radios worldwide (communities, research, activism, or commercial services). **For simplicity, this project does not interact with such sites** and runs with **local reception**.

## Included
- Java sources (JavaFX GUI, ADS-B parsing ...).

## Excluded (not pushed)
- ADS-B recordings very large.
- OSM tile cache.
- Build outputs. 

## Features 

- Input of ADS-B frames (1090 MHz) from **AirSpy R2** (or compatible SDR)
- **Mode S / ADS-B** decoding: identity, position, speed, heading
- Temporal reconstruction / filtering of aircraft state
- **Map visualization** with aircraft markers and details

> Depending on project progress, some features may be partial or experimental.

## Hardware

- **AirSpy R2** (or any SDR capable of 1090 MHz reception)
- **Antenna** with clear placement (height, unobstructed view recommended)
## Prerequisites
JDK 21 recommended or  JDK 19 + JavaFX SDK 

## How to run 
Run without arguments to read from System.in via AdsbDemodulator.

java --add-modules=javafx.controls,javafx.graphics \
     -cp "out:resources" ch.epfl.javions.gui.Main




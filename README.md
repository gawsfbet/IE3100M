# Shipping Carton Optimisation
This program was created by NUS ISE SDP Group 11 for our System Design Project.

# User Guide

* [Installation Guide](#installation-guide)
* [Fields](#fields)

## Installation Guide

### Before you start
1. This app runs on Java. Ensure that Java version `1.8.0_91` or later installed in your computer.
    > This app might not work with earlier versions of Java 8.
2. CPLEX is needed for the app to perform. Ensure that CPLEX version `12.7.1` or later is installed. Click [here](https://www.ibm.com/products/ilog-cplex-optimization-studio/pricing) to view the versions and pricing for CPLEX.
    > Not sure if the community edition can be used for this program, will check soon

### How to run
1. Download the jar from the releases tab. TODO: Add to releases
2. Double click the jar to run. Or might need to add the Djava thing in order to run it. TODO: check
3. The GUI should appear in a few seconds.
    > <img src="images/GUIprototype.png" width="600">
4. Type the fields in the `Order Specifications` panel and press <kbd>Enter</kbd> to determine the shipping carton boxes to be used.
5. Some example values you can try:
    * **`Quantity`** : 50
    * **`Length`** : 190
    * **`Width`** : 190
    * **`Height`** : 25
    * **`Weight`** : 0.3
6. Refer to the [Features](#features) section below for details of each field.<br>

## Features
#### `Quantity`<br>
The number of product boxes to be packed.<br>
#### `Length`<br>
The length of the product box to be packed, in mm.<br>
#### `Width`<br>
The width of the product box to be packed, in mm.<br>
#### `Height`<br>
The height of the product box to be packed, in mm.<br>
#### `Weight`<br>
The weight of the product box to be packed, in kg.<br>
#### `Buffer`<br>
The buffer to add to the sides of the shipping box, in mm.<br>

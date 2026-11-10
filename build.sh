#!/usr/bin/env bash
set -e

echo "========================================================"
echo "  Building Campus Smart-Parking System"
echo "========================================================"

mkdir -p bin

echo "Compiling Java source files..."
javac -d bin $(find src tasks -name "*.java")

echo "Packaging standalone JAR..."
jar cfe campus-parking.jar parking.system.main.ParkingDemo -C bin .

echo "========================================================"
echo "  Build Complete! Executable: campus-parking.jar"
echo "========================================================"

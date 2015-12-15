#!/bin/bash

# Try and remove output file if it already exists
rm -f papers.txt

# Compile and run
javac -cp .:jsoup-1.8.3.jar WebCrawler.java
java -cp .:jsoup-1.8.3.jar WebCrawler $1 $2 $3 $4


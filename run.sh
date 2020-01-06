#!/bin/bash

set -e

mvn clean verify

java -jar target/javalinsample-1.0-jar-with-dependencies.jar
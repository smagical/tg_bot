#!/usr/local/bin/bash
#!/bin/bash
APP="leetcode.jar"
export JAVA_VERSION="21"
run_dir=$(dirname $(readlink -f "$0"))
cd $run_dir
export HANLP_ROOT=$run_dir
./run.sh start $APP

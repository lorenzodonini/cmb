#!/bin/bash

#Usage ex: ./simulations.sh offloading_case3.txt case2_hoevery

OUT=out/production/the-one
CURRENT=$(pwd)
TARGET=$CURRENT/simulations
HOTSPOT_PREFIX=hotspots_every_
CASE=$1
DEST_NAME=$2

cd $OUT;
echo $(pwd)
for j in 2 3 4 5 10;
do
    for i in {1..5};
    do
        ./one.sh -b 1 offloading_settings.txt "$CASE" "$HOTSPOT_PREFIX$j.txt"
        cd reports
        mv helsinki_offloading_OffloadingReport.txt "${TARGET}/${DEST_NAME}${j}_0${i}.txt"
        cd ..
    done
done

echo "Done!"

#!/bin/bash


# plotTrends.sh
#
# Takes the csv results from FileNet healthcheck, builds trending graphs and emails to support contacts.
# Requires gnuplot:  sudo yum install gnuplot
#
# 6/17 riflechess 

#plot last x number of snapshots (i.e. PROD runs hourly, so 168=1 week)
SNAPSHOTS=336

gnuplot << eor
set terminal png
set output 'trend_Object_Store_Name.png'
set style data lines
set datafile separator ","
set autoscale fix
set xdata time
set timefmt '%b %d %H:%M:%S GMT %Y'
#set xlabel "date/time"
set ylabel "ms"
set title "PROD Object_Store_Name Trending"



plot '< tail -n +2 trending_Object_Store_Name.csv | grep Object_Store_Name | tail -n $SNAPSHOTS |cut -f 1 --complement -d" "' using 1:3 title "Domain Connect", '' using 1:4 title "Search", '' using 1:6 title "Upload", '' using 1:7 title "Delete"

set output 'trend_Object_Store_Name2.png'
set title "PROD Object Store Name"
plot '< tail -n +2 trending_Object_Store_Name2.csv | grep Object_Store_Name2 | tail -n $SNAPSHOTS | cut -f 1 --complement -d" "' using 1:3 title "Domain Connect", '' using 1:4 title "Search", '' using 1:6 title "Upload", '' using 1:7 title "Delete"

eor



#email graphs out
echo "Please see the attached info for healthcheck trending for the previous $((SNAPSHOTS/24)) days." | mail -a trend_Object_Store_Name.png -a trend_Object_Store_Name2.png  -s "P8 Trending" riflechess@email.com




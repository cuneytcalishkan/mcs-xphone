set term postscript eps enhanced color
set output "output1Dropped.eps"
set title "Number of dropped calls over time"
set xlabel "Time"
set ylabel "# Dropped calls"
set xrange [0:2800]
set yrange [0:10]
set xtics 0,250
set ytics 0,1
plot "output1.txt" using 1:2 with linespoints title "dropped calls"

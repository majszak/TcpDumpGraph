import java.time.LocalTime
import java.util.stream.Collectors

import static java.nio.file.Files.lines

import java.nio.charset.CharsetDecoder
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.function.Predicate

final String TCPDUMP_INPUT_FILE = '..\\resources\\out1.txt'
final String GNUPLOT_OUTPUT_FILE = '..\\resources\\gnuplot.txt'
final String FILTER = "192.168.56.1.5001"
final int TIME_SEGMENT_LENGTH = 8
final def LENGTH_REGEXP = /.*length (\d+).*/
final int HOUR_POSITION = 0
final int MINUTE_POSITION = 1
final int SECONDS_POSITION = 2


println("Start")

def predicate = new Predicate<String>() {
    @Override
    boolean test(String t) {
        t.contains(FILTER)
    }
}

CharsetDecoder dec = StandardCharsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.IGNORE)

List<String> filtered = lines(Paths.get(TCPDUMP_INPUT_FILE), dec.charset()).filter(predicate).collect(Collectors.toList());
Map<LocalTime, Double> timeSizeMap = [:]
filtered.each {
    def timeTokens = it.substring(0, TIME_SEGMENT_LENGTH).tokenize(':');
    def matcher = (it =~ LENGTH_REGEXP)
//    println matcher[0][1]
    LocalTime key = new LocalTime(timeTokens.get(HOUR_POSITION).toInteger(), timeTokens.get(MINUTE_POSITION).toInteger(),
            timeTokens.get(SECONDS_POSITION).toInteger(), 0)
    Double newValue = matcher[0][1] as Double
    newValue = newValue*8/1000
    Double currentValue = timeSizeMap.get(key) == null ? 0 : timeSizeMap.get(key)
    cumulativeValue = newValue + currentValue
    timeSizeMap.put(key, cumulativeValue)
}

println 'Przetworzono sekund: ' + timeSizeMap.size()

def gnuplotFile = new File(GNUPLOT_OUTPUT_FILE)
def writerOverwrite = gnuplotFile.newWriter()
writerOverwrite << ''
timeSizeMap.each {
    gnuplotFile << it.key
    gnuplotFile << ' '
    gnuplotFile << it.value
    gnuplotFile << '\n'
}

/*
set terminal png size 1200,800
set xdata time
set timefmt "%H:%M:%S"
set output "d:\\gnuplot.png"
# time range must be in same format as data file
# set xrange ["Mar-25-00:00:00":"Mar-26-00:00:00"]
# set yrange [0:50]
set grid
set xlabel "Date\\nTime"
set ylabel "Load"
set title "Load Averages"
set key left box
plot "d:\\gnuplot.txt" using 1:2 index 0 title "ahost" with lines



# root class and defaultl class definition and default class
tc qdisc add dev eth0 root handle 1: hfsc default 2
tc class add dev eth0 parent 1: classid 1:2 hfsc ls rate 1000kbit ul rate 1000kbit

#jesli chcemy "pomoc" pakietom dotrzec do celu, algorytm sfq
tc qdisc add dev eth0 parent 1:2 handle 2:0 sfq perturb 10

# stworzymy teraz specjalna kolejke dla ruchu priorytetowego, aby pasmo sie dzielilo

tc class add dev eth0 parent 1: classid 1:1 hfsc ls rate 100kbit
tc class add dev eth0 parent 1:1 classid 1:3 hfsc ls rate 40kbit
tc class add dev eth0 parent 1:1 classid 1:4 hfsc ls rate 10kbit

#Filtry na dport 5001 i 5002
tc filter add dev eth0 parent 1: protocol ip prio 1 u32 match ip dport 5001 0xffff classid 1:3
tc filter add dev eth0 parent 1: protocol ip prio 1 u32 match ip dport 5002 0xffff classid 1:4

tc qdisc add dev eth0 parent 1:1 handle 1:0 sfq perturb 10
# match icmp traffic
tc filter add dev eth0 parent 1: protocol ip prio 1 u32 match ip protocol 1 0xff classid 1:1


 */
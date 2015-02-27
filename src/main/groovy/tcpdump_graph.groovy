import java.math.MathContext
import java.nio.file.Files
import java.time.Duration
import java.time.LocalTime
import java.util.stream.Collectors
import java.nio.charset.CharsetDecoder
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.function.Predicate

final String TCPDUMP_INPUT_FILE = '..\\resources\\out'
final String GNUPLOT_SPEED_OUTPUT_FILE = '..\\resources\\gnuplot_speed'
final String GNUPLOT_DELAY_OUTPUT_FILE = '..\\resources\\gnuplot_delay'
final String GNUPLOT_JITTER_OUTPUT_FILE = '..\\resources\\gnuplot_jitter'
final String FILTER = '192.168.56.1.500'
final int TIME_SEGMENT_LENGTH = 8
final int TIME_NANOSECONDS_LENGTH = 6
final LENGTH_REGEXP = /.*length (\d+).*/
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

//add prefix
[1, 2].each {
    List<String> filtered = Files.lines(Paths.get(TCPDUMP_INPUT_FILE + it.toString()), dec.charset()).filter(predicate).collect(Collectors.toList());
    Map<LocalTime, Double> timeSpeedMap = [:]
    Map<LocalTime, Double> timeDelayMap = [:]
    Map<LocalTime, Double> timeJitterMap = [:]

    LocalTime previousTime
    boolean doNotCreate = false

    filtered.each {
        def timeTokens = it.substring(0, TIME_SEGMENT_LENGTH).tokenize(':');
        def matcher = (it =~ LENGTH_REGEXP)
        LocalTime keySpeed = new LocalTime(timeTokens.get(HOUR_POSITION).toInteger(), timeTokens.get(MINUTE_POSITION).toInteger(),
                timeTokens.get(SECONDS_POSITION).toInteger(), 0)

        def nanosecond = it.substring(TIME_SEGMENT_LENGTH + 1, TIME_SEGMENT_LENGTH + 1 + TIME_NANOSECONDS_LENGTH)
        LocalTime currentTime = new LocalTime(timeTokens.get(HOUR_POSITION).toInteger(), timeTokens.get(MINUTE_POSITION).toInteger(),
                timeTokens.get(SECONDS_POSITION).toInteger(), nanosecond.toInteger() * 1000)

        Long delay
        if (previousTime == null) {
            delay = 0
            doNotCreate = true
        } else {
            delay = Duration.between(previousTime, currentTime)
                    .toNanos();
            doNotCreate = false
        }
        previousTime = currentTime

        Double newValue = matcher[0][1] as Double
        newValue = newValue * 8 / 1000
        Double currentValue = timeSpeedMap.get(keySpeed) == null ? 0 : timeSpeedMap.get(keySpeed)
        cumulativeValue = newValue + currentValue
        timeSpeedMap.put(keySpeed, cumulativeValue)
        //skip first element
        if (!doNotCreate) timeDelayMap.put(currentTime, delay / 1000000)
    }

    println 'Number of seconds processed: ' + timeSpeedMap.size()
    println 'Number of miliseconds processed: ' + timeDelayMap.size()

    println 'Calculating jitter'

    boolean isFirst = true
    Double previousDelay = 0
    timeDelayMap.each {
        if (isFirst) {
            isFirst = false
        } else {
            timeJitterMap.put(it.key, (it.value - previousDelay).abs())
        }
        previousDelay = it.value
    }

    println 'WRITING spped file'
    def gnuplotFile = new File(GNUPLOT_SPEED_OUTPUT_FILE + it.toString())
    def writerOverwrite = gnuplotFile.newWriter()
    writerOverwrite << ''
    timeSpeedMap.each {
        gnuplotFile << it.key
        gnuplotFile << ' '
        gnuplotFile << it.value
        gnuplotFile << '\n'
    }


    println 'WRITING delay file'
    gnuplotFile = new File(GNUPLOT_DELAY_OUTPUT_FILE + it.toString())
    writerOverwrite = gnuplotFile.newWriter()
    writerOverwrite << ''
    timeDelayMap.each {
        gnuplotFile << it.key
        gnuplotFile << ' '
        gnuplotFile << it.value
        gnuplotFile << '\n'
    }

    println 'WRITING jitter file'
    gnuplotFile = new File(GNUPLOT_JITTER_OUTPUT_FILE + it.toString())
    writerOverwrite = gnuplotFile.newWriter()
    writerOverwrite << ''
    timeJitterMap.each {
        gnuplotFile << it.key
        gnuplotFile << ' '
        gnuplotFile << it.value
        gnuplotFile << '\n'
    }

}
/*
set terminal pdf
set xdata time
set timefmt "%H:%M:%S"
set output "d:\\MM\\Programowanie\\Java\\TcpDumpGraph\\src\\main\\resources\\gnuplot_speed.pdf"
# time range must be in same format as data file
# set xrange ["Mar-25-00:00:00":"Mar-26-00:00:00"]
# set yrange [0:50]
set grid
set xlabel "Time [s]"
set ylabel "Speed [Kbit\\s]"
set title "HFSC experiments"
set key left box
plot "d:\\MM\\Programowanie\\Java\\TcpDumpGraph\\src\\main\\resources\\gnuplot_speed1" using 1:2 index 0 title "port 5001" with lines lw 2, \
 "d:\\MM\\Programowanie\\Java\\TcpDumpGraph\\src\\main\\resources\\gnuplot_speed2" using 1:2 index 0 title "port 5002" with lines lw 2
#
#
#
#
#


set terminal pdf
set xdata time
set timefmt "%H:%M:%S"
set output "d:\\MM\\Programowanie\\Java\\TcpDumpGraph\\src\\main\\resources\\gnuplot_delay.pdf"
# time range must be in same format as data file
# set xrange ["Mar-25-00:00:00":"Mar-26-00:00:00"]
# set yrange [0:50]
set grid
set xlabel "Time [s]"
set ylabel "Speed [Kbit\\s]"
set title "HFSC experiments"
set key left box
plot "d:\\MM\\Programowanie\\Java\\TcpDumpGraph\\src\\main\\resources\\gnuplot_delay1" using 1:2 index 0 title "port 5001" with lines lw 2, \
 "d:\\MM\\Programowanie\\Java\\TcpDumpGraph\\src\\main\\resources\\gnuplot_delay2" using 1:2 index 0 title "port 5002" with lines lw 2

#
#
#
#

set terminal pdf
set xdata time
set timefmt "%H:%M:%S"
set output "d:\\MM\\Programowanie\\Java\\TcpDumpGraph\\src\\main\\resources\\gnuplot_jitter.pdf"
# time range must be in same format as data file
# set xrange ["Mar-25-00:00:00":"Mar-26-00:00:00"]
# set yrange [0:50]
set grid
set xlabel "Time [s]"
set ylabel "Jitter [ms]"
set title "HFSC experiments"
set key left box
plot "d:\\MM\\Programowanie\\Java\\TcpDumpGraph\\src\\main\\resources\\gnuplot_jitter1" using 1:2 index 0 title "port 5001" with lines lw 2, \
 "d:\\MM\\Programowanie\\Java\\TcpDumpGraph\\src\\main\\resources\\gnuplot_jitter2" using 1:2 index 0 title "port 5002" with lines lw 2



!!! WAZNE
VirtualBox z karta sieciowa ustawiona na "internal network" (dostanie adres 192.168.56.101)
#2) HFSC test
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
#jperf SERVER na Windows
tcpdump -n dst port 5001 > out1
tcpdump -n dst port 5002 > out2
3) iperf client (10s, 100s, 10Mbit)
iperf -c 192.168.56.1 -u -P 1 -i 1 -p 5001 -f k -b 10.0M -t 100 -T 1 &
iperf -c 192.168.56.1 -u -P 1 -i 1 -p 5002 -f k -b 10.0M -t 10 -T 1 &
 */

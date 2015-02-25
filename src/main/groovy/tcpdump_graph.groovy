import java.time.LocalTime
import java.util.stream.Collectors

import static java.nio.file.Files.lines

import java.nio.charset.CharsetDecoder
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.function.Predicate

final String TCPDUMP_INPUT_FILE = '..\\resources\\dump.txt'
final String FILTER = "IP 172.16.1.1"
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
Map<LocalTime, Integer> timeSizeMap = [:]
filtered.each {
    def timeTokens = it.substring(0, TIME_SEGMENT_LENGTH).tokenize(':');
    def matcher = (it =~ LENGTH_REGEXP)
    println matcher[0][1]
    LocalTime key = new LocalTime(timeTokens.get(HOUR_POSITION).toInteger(), timeTokens.get(MINUTE_POSITION).toInteger(),
            timeTokens.get(SECONDS_POSITION).toInteger(), 0)
    Integer newValue = matcher[0][1] as Integer
    Integer currentValue = timeSizeMap.get(key) == null ? 0 : timeSizeMap.get(key)
    cumulativeValue = newValue + currentValue
    timeSizeMap.put(key, cumulativeValue)
}

//print filtered.size()
println timeSizeMap
println timeSizeMap.size()

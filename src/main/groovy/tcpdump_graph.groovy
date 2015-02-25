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
final String LENGTH_REGEXP = /length (.*)/

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
Map<LocalTime, String> map = [:]
filtered.each {def timeTokens = it.substring(0, TIME_SEGMENT_LENGTH).tokenize(':');

    String matcher = ( it =~ LENGTH_REGEXP )
    println it;
    map.put(new LocalTime(timeTokens.get(0).toInteger(), timeTokens.get(1).toInteger(), timeTokens.get(2).toInteger(), 0), matcher[0])}

//print filtered.size()
println map
println map.size()
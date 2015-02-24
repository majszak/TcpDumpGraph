import java.util.stream.Collectors

import static java.nio.file.Files.lines

import java.nio.charset.CharsetDecoder
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.function.Predicate

println("EX")


Predicate<String> predicate = new Predicate<String>() {
    @Override
    boolean test(String t) {
        t.contains("size");
    }
}

String f = '..\\resources\\dump.txt'

CharsetDecoder dec = StandardCharsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.IGNORE)

//.each( { i -> print i } )

//List<String> filtered = lines(Paths.get(f), dec.charset()).filter(predicate).collect(Collectors.toList());

HashMap<String, String>[] array =
        lines(Paths.get(f), dec.charset()).filter(predicate).toArray( { n -> print n; new HashMap[n] })
print array
package dev.mfataka.transporter.rest;

import dev.mfataka.transporter.models.LargeData;
import dev.mfataka.transporter.models.LargeDataResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author HAMMA FATAKA
 * @project transporter
 * @date 31.05.2023 14:08
 */
@Slf4j
@RestController
public class TestController {

    @GetMapping(path = "/large/data", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LargeDataResponse> spamLargeDataToResponse() {
        final List<LargeData> largeDataList = IntStream.range(0, 200)
                .mapToObj(ignored -> LargeData.builder()
                        .firstField("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam scelerisque sollicitudin massa, vel vulputate nisi convallis et. Vivamus consectetur ipsum nec turpis finibus ultrices. Fusce sit amet facilisis metus, vitae tincidunt est. Pellentesque blandit odio ac nisl interdum finibus. Nam sit amet felis nec nisl bibendum venenatis. Integer sed commodo erat. Aenean volutpat consequat consectetur. Aliquam gravida felis ut tortor convallis, eu posuere tellus cursus.\n" +
                                "\n" +
                                "Sed euismod lacus eu eros pulvinar sollicitudin. Fusce gravida rutrum sem, id mattis est accumsan et. Ut bibendum cursus lectus. Duis mollis sapien ac massa euismod, vitae elementum leo hendrerit. Nullam sed sapien lacinia, varius lectus id, pretium ex. Nulla facilisi. Praesent finibus urna id nisl dapibus, vitae efficitur risus malesuada. Curabitur vel dui vel libero cursus consequat non id odio.\n" +
                                "\n" +
                                "Quisque interdum consequat risus, vitae tincidunt nisl facilisis et. Proin consectetur nisl id dolor accumsan, ut interdum ex interdum. Integer fermentum libero non luctus pulvinar. Phasellus quis semper nisi. Aliquam erat volutpat. Sed commodo justo nec elit ultricies, non aliquet massa consectetur. Suspendisse fermentum condimentum lacus vel suscipit.\n" +
                                "\n" +
                                "Aenean at semper neque. Fusce feugiat leo in libero fringilla, id tincidunt enim pellentesque. Cras efficitur augue a dui laoreet, sed congue urna convallis. In in fermentum mauris. Mauris id ipsum eu lectus dapibus lacinia. Morbi gravida nunc ac lacinia tempor. Vivamus fermentum luctus lacus, a dapibus est lacinia vitae. Mauris consequat, metus eu consectetur consequat, lorem neque consectetur velit, eget tristique massa ligula ut nisl.")
                        .secondField("Nullam pulvinar purus at felis malesuada, sed congue nisi ultrices. Donec semper tincidunt leo, sed ullamcorper tellus suscipit non. Etiam et orci quam. Integer sed elit diam. In dapibus fermentum odio, ac sollicitudin leo lacinia id. Sed vestibulum bibendum quam, ac dictum dolor laoreet a. Aenean congue vestibulum lectus, id efficitur erat. Donec lacinia placerat turpis in placerat. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Suspendisse ultrices magna at justo cursus, a lacinia elit commodo. Integer in ullamcorper erat. In id vestibulum risus. Cras auctor est in lobortis vulputate. Sed varius risus mauris, sed hendrerit arcu commodo vitae.")
                        .build()
                )
                .collect(Collectors.toList());
        final int length = largeDataList.toString().length();
        log.debug("content length is [{}]", length);
        return ResponseEntity.ok(LargeDataResponse.builder()
                .largeDataList(largeDataList)
                .build()
        );
    }
}

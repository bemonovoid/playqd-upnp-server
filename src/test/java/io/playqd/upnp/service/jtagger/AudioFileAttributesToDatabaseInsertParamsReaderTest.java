//package io.playqd.mediaserver.service.jtagger;
//
//import io.playqd.mediaserver.service.metadata.AudioFileAttributesReader;
//import org.junit.jupiter.api.Test;
//
//import java.nio.file.Paths;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//class AudioFileAttributesToDatabaseInsertParamsReaderTest {
//
//  private static final String ORIGINAL_SAMPLE_FILENAME = "/files/audio/sample_original.mp3";
//
//  private final AudioFileAttributesReader reader = new JTaggerAudioFileAttributesToDatabaseParamsMapper();
//
//  @Test
//  void successfullyReadFromPath() throws Exception {
//    var url = this.getClass().getResource(ORIGINAL_SAMPLE_FILENAME);
//    assertNotNull(url);
//    var result = reader.read(Paths.get(url.toURI()));
//    assertFalse(result.isEmpty());
//  }
//
//}
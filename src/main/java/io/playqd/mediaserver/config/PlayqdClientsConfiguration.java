package io.playqd.mediaserver.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.codec.Decoder;
import feign.optionals.OptionalDecoder;
import io.playqd.commons.client.MediaLibraryClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.cloud.openfeign.support.PageJacksonModule;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SortJacksonModule;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.List;

@Configuration
@EnableFeignClients(clients = MediaLibraryClient.class)
class PlayqdClientsConfiguration {

  @Bean
  public Decoder feignDecoder(@Qualifier("mediaLibraryClientObjectMapper") ObjectMapper mediaLibraryClientObjectMapper,
                              ObjectProvider<HttpMessageConverterCustomizer> customizers) {
    var httpMessageConverters =
        new HttpMessageConverters
            (List.of(new MappingJackson2HttpMessageConverter(mediaLibraryClientObjectMapper)));
    return new OptionalDecoder(
        new ResponseEntityDecoder(
            new SpringDecoder(() -> httpMessageConverters, customizers)));
  }

  @Bean
  ObjectMapper mediaLibraryClientObjectMapper() {
    return new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//        .setPropertyNamingStrategy(SnakeCaseStrategy.SNAKE_CASE)
        .registerModule(new JavaTimeModule())
        .registerModule(new PageJacksonModule())
        .registerModule(new SortJacksonModule());
  }
}

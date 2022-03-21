package com.coddweaver.seamless.talk.rabbit.repositorymanager.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "repository")
@Data
@NoArgsConstructor
public class RepositoryProperties {
    private String folder = "examples/2-ms-with-shared-lib/repository-manager/src/main/resources/library";
}

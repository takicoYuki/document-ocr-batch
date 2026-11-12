package io.github.takicoyuki.docbatch.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DocumentRequest(
        @NotBlank
        @Size(max = 255)
        @Pattern(regexp = ".+\\.(pdf|png|jpg|jpeg|tif|tiff)$", flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "対応している拡張子は pdf / png / jpg / jpeg / tif / tiff です")
        String fileName,

        @NotBlank
        @Size(max = 100_000)
        String content
) {
}

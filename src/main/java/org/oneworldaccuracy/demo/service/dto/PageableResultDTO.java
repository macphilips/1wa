package org.oneworldaccuracy.demo.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
public class PageableResultDTO<T> {
    private List<T> results;
    private int page;
    private int size;
    private int totalSize;

    public PageableResultDTO(Page<T> page) {
        this.results = page.getContent();
        this.totalSize = page.getTotalPages();
        this.size = getSize();
        this.page = page.getNumber() + 1;
    }
}

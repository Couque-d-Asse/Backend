package kr.ddm.civic.civicdraft.dto;

import lombok.Data;

@Data
public class BookmarkletResponse {
    private String bookmarkletUrl;
    private String bookmarkletCode;
    private String templateId;
    private boolean success;
    private String message;
}
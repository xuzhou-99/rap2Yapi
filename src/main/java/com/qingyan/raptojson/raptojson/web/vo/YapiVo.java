package com.qingyan.raptojson.raptojson.web.vo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author xuzhou
 * @since 2022/8/2
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class YapiVo {
    private String token;
    private List<String> req_query;
    private List<Req_headers> req_headers;
    private List<String> req_body_form;
    private String title;
    private String catid;
    private String path;
    private String status;
    private String res_body_type;
    private String res_body;
    private boolean switch_notice;
    private String message;
    private String desc;
    private String method;
    private List<String> req_params;
    private String id;

    public class Req_headers {

        private String name;
        public void setName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }

    }
}

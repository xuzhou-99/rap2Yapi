
package com.qingyan.raptojson.raptojson.pojo;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Auto-generated: 2022-08-03 10:7:35
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ModuleList {

    private String name;
    private int id;
    private List<PageList> pageList;
    private String introduction;

}
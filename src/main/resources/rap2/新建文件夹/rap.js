function changeRap() {
        var origin = document.getElementById('origin').value;
        if (!origin) return alert('原始json不能为空');
        if (origin.indexOf('http') === 0) {
          $.ajax({
            type: 'get',
            url: origin,
            dataType: 'jsonp',
            success: r =>{
              var m = {};
              m.swagger = '2.0';
              m.info = {};
              m.info.title = r.data.name;
              m.info.description = r.data.description;
              m.tags = r.data.modules.map(({
                name,
                description
              }) =>({
                name,
                description
              }));
              m.paths = parsePaths(r.data.modules);
              m.definitions = parseDefinitions(r.data.modules);
              //下载为json文件
              var Link = document.createElement('a');
              Link.download = "api.json";
              Link.style.display = 'none';
              // 字符内容转变成blob地址
              var blob = new Blob([JSON.stringify(m)]);
              Link.href = URL.createObjectURL(blob);
              // 触发点击
              document.body.appendChild(Link);
              Link.click();
              // 然后移除
              document.body.removeChild(Link);
            }
          })
        }
      }
 
      function parsePaths(modules) {
        var paths = {};
        for (var i = 0,
        len = modules.length; i < len; i++) {
          for (var j = 0,
          jlen = modules[i].interfaces.length; j < jlen; j++) {
            var m = modules[i].interfaces[j];
            paths[m.url] = {};
            var method = m.method.toLowerCase();
            paths[m.url][method] = {};
            paths[m.url][method].tags = [modules[i].name];
            paths[m.url][method].summary = m.name;
            if (method === 'post') paths[m.url][method].consumes = ["multipart/form-data"];
            paths[m.url][method].description = m.description;
            paths[m.url][method].parameters = parseParameters(m.properties, method);
            paths[m.url][method].deprecated = false;
            paths[m.url][method].responses = {
              "200": {
                "description": "ok",
                "schema": {
                  "$ref": "#/definitions/Response" + m.id
                }
              }
            };
          }
        }
        return paths;
      }
 
      function parseParameters(props, method) {
        var list = [];
        for (var i = 0,
        len = props.length; i < len; i++) {
          var p = props[i];
          if (p.scope === 'response') continue;
          list.push({
            name: p.name,
            "in": method === 'get' ? 'query': 'formData',
            example: 'default',
            description: p.description || '',
            type: p.type.toLowerCase(),
            required: p.required
          });
        }
        return list;
      }
 
      function parseDefinitions(modules) {
        var ds = {};
        for (var i = 0,
        len = modules.length; i < len; i++) {
          for (var j = 0,
          jlen = modules[i].interfaces.length; j < jlen; j++) {
            var m = modules[i].interfaces[j];
            for (var n = 0,
            nlen = m.properties.length; n < nlen; n++) {
              var p = m.properties[n];
              if (p.scope === 'request') continue;

              var suf = p.parentId === -1 ? m.id: p.parentId;
              if (!ds['Response' + suf]) ds['Response' + suf] = {
                title: 'Response' + suf,
                type: "object",
                properties: {}
              };
              if (p.type === 'Object') ds['Response' + suf].properties[p.name] = {
                "$ref": "#/definitions/Response" + p.id
              };
              else if (p.type === 'Array') ds['Response' + suf].properties[p.name] = {
                type: "array",
                items: {
                  "$ref": "#/definitions/Response" + p.id
                }
              };
              else ds['Response' + suf].properties[p.name] = {
                type: p.type.toLowerCase(),
                description: p.description || '',
              default:
                p.value || undefined
              }
            }
          }
        }
        return ds;
      }
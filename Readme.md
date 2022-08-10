# Rap接口转YApi

## 前言

本项目主要目的是提供Rap接口转YApi接口的解决方案，支持对Rap、Rap2版本的接口转成Swagger格式json，YApi json格式，从而进行接口迁移。

功能：

* Rap接口转 Yapi json
* Rap2接口转 Swagger json
* Rap2接口转 Yapi json

## Swagger

api：[Api接口文档 · xuzhou-99/rap2Yapi Wiki (github.com)](https://github.com/xuzhou-99/rap2Yapi/wiki/Api接口文档)

swagger：http://IP:PORT/swagger-ui/index.html

[![47BU6.png](https://s1.328888.xyz/2022/08/10/47BU6.png)](https://imgloc.com/i/47BU6)

## 主要功能

### Rap 接口转 json

Rap在1.x.x版本中，组织结构是团队-业务线-分组，接口管理是项目-模块-分类-接口，在Yapi中的组织结构则是分组-项目，接口管理是项目-分类-接口，所以在Rap接口转Yapi可导入json时，也需要进行结构上的处理。

* 两种结构转换：
  * **module**：项目-模块-分类-接口  重组为  项目-分类（模块作为分类）-接口
    * rap项目对应 YApi 项目；
    * rap模块对应 YApi 分类；
    * rap接口对应 YApi 接口；
    * 场景：适用于模块多的
  * **page**：项目-模块-分类-接口  重组为  项目（模块作为项目）-分类-接口
    * rap模块对应 YApi 项目；
    * rap分类对应 YApi 分类；
    * rap接口对应 YApi 接口；
    * 场景：适用于模块少的
* 两种输出方式
  * 分模块输出
  * 输出一个json



### Rap2 接口转 json

Rap在2.x.x版本中，接口管理是仓库-分类-接口，在Yapi中接口管理是项目-分类-接口，所以在Rap2接口转Yapi可导入json时，不用做结构上的处理，最后都只输出一个json文件。

* 两种文件转换：
  * **swagger**：接口以 Swagger json 格式输出
  * **page**：接口以 Yapi json格式输出

## 主要配置

项目在本地启动，修改配置文件，指定rap地址和文件输出位置

```yml
# the target path , rap to json
json:
  # local share，如果是内网部署多团队使用，可以建立一个共享文件夹存放结果
  isShare: true
  # target path，输出结果路径
  rootPath: /shareFile/apiJson
  # local share path
  sharePath: file://DESKTOP-B7C8MM8

url:
  # rap url
  rap: http://ip:8080
  # rap2 url
  rap2: http://ip:38080
```

##  YApi导入

新建项目后，选择数据管理进行导入，YApi接口支持多个数据格式和数据同步方式。

数据格式： postman、HAR、swagger、json(yapi)，四个格式的导入

数据同步：普通模式、智能合并、完全覆盖

[![4Q1P5.png](https://s1.328888.xyz/2022/08/10/4Q1P5.png)](https://imgloc.com/i/4Q1P5)
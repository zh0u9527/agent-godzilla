# 1.介绍
jmg(java-memshell-generator) 项目整合agent。
- 支持哥斯拉连接，配置
  - User-Agent: Ioyrns      只需要包含Ioyrns即可，不区分大小写
  - pass: pass
  - key: key
- 支持web中间件
  - tomcat7/8/9/10（已测试）
  - undertow（已测试）
  - jetty（已测试）
  - WebLogic
  - GlassFish（已测试）
  - Apusic（金蝶）
  - BES（宝兰德）
  - InforSuite（中创）
  - TongWeb（东方通）
# 2.原理
shell代码主要是经过其中的Filter进行修改的，原理是通过修改每个中间件在调用开发者自定义Filter之前的第一个中间件Filter来实现shell执行；所以，如果有遇到执行失败，请检查将对应的Web中间件调用开发者自定义Filter之前的第一个Filter加入到项目WEB_SERVER_LIST列表中即可。

注意事项
  - 运行agent时，应该确保运行agent的jdk版本与需要附件项目的jdk版本一致，否则会出现 Non-numeric value found - int expected
  - 请注意web项目的，context-path: /api  ，即使是注入成功之后，在godzilla中设置路径时，访问路径为，http(s)://ip/context-path

# 3、致谢
感谢pen4uin师傅jmg的开源项目，其中shell代码主要参考其中的Filter

感谢罗师傅的指点，项目得以顺利完成。

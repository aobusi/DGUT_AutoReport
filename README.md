## 技术

Java8、MySQL5.7、chromedriver、Spring Boot。系统提醒模块使用server酱。

本系统基于selenium，使用Java语言，也可使用python轻松改写。

本系统用到的chromedriver，根据你系统上安装的chrome版本，去[这里](https://npm.taobao.org/mirrors/chromedriver)下载相应的版本。不同版本的使用结果未知。可使用其他浏览器，只需要下载相应的浏览器驱动，再对代码少量修改即可。

## 数据

dgut中央认证系统账号密码，保存于MySQL的users表，由于需要使用明文密码登录，所以加密的意义不大；

[server酱](https://sct.ftqq.com/login)的sendKey（微信登录，关注方糖公众号即可接收通知）。

## 用途

本系统实现了dgut的每日疫情防控打卡，可直接用于二次开发。

## 声明


用于学习，请勿用于非法用途。


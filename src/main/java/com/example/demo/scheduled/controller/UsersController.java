package com.example.demo.scheduled.controller;

import cn.hutool.http.HttpUtil;
import com.example.demo.scheduled.entity.Users;
import com.example.demo.scheduled.mapper.UsersMapper;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

/**
 * Create By aobs
 * Date 2021/10/2 9:01
 * Description
 */
@RestController
@Slf4j
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UsersMapper usersMapper;

    /**
     * 添加用户
     * @param users {"username":"211211xxxx" , "password":"xxxxxx"}
     * @return 提示信息
     */
    @PostMapping("/user")
    public String addNewUser(@RequestBody Users users){
        Users selectByUserName = usersMapper.selectByUserName(users.getUsername());
        if (null == selectByUserName){
            usersMapper.insertUser(users);
        } else {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST , "账号已存在");
        }
        return "添加成功";
    }

    /**
     * 查询单个用户
     * @param username 学号
     * @return user
     */
    @GetMapping("/user/{username}")
    public Users selectByUserName(@PathVariable("username") String username){
        return usersMapper.selectByUserName(username);
    }


    private String successMessage = "今日打卡成功";
    private String failMessage = "打卡异常";

    /**
     * 手动激活打卡
     */
    @GetMapping("/do")
    public void dakaByHand(){
        List<Users> users = usersMapper.selectUsers();
        String currnentUser = "";
        if (!users.isEmpty()){
            //打卡地址
            String url = "https://cas.dgut.edu.cn/home/Oauth/getToken/appid/illnessProtectionHome/state/home";
            String newUrl = "https://yqfk.dgut.edu.cn/main";
            //chromedriver本地文件位置，注意版本要与电脑上安装的chrome版本一致或相近
            // win
            //System.setProperty("webdriver.chrome.driver", "D:/cache/chromedriver.exe");
            // linux
            System.setProperty("webdriver.chrome.driver" , "/usr/local/java/chromedriver");
            //“–no-sandbox” 让Chrome在root权限下跑
            //“–headless” 不用打开图形界面
            //不加此参数会报异常
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--headless");
            WebDriver driver = new ChromeDriver(options);
            for (Users user : users) {
                try {
                    currnentUser = user.getUsername();
                    log.info("开始给账号 " + user.getUsername() + " 打卡");
                    driver = new ChromeDriver(options);
                    driver.get(url);
                    WebElement username = driver.findElement(By.id("username"));
                    WebElement password = driver.findElement(By.id("casPassword"));
                    WebElement loginBtn = driver.findElement(By.id("loginBtn"));
                    username.sendKeys(user.getUsername());
                    password.sendKeys(user.getPassword());
                    loginBtn.click();
                    //打开打卡页面
                    driver.get(newUrl);
                    //线程暂停，否则速度过快，下面会获取不到 element，时间无所谓，至少要保证页面接收过来
                    Thread.sleep(3000);
                    WebElement temperature;
                    //目前测试两人账号，两人账号页面不同，所以获取元素位置不同，获取不到直接报错
                    try {
                        temperature = driver.findElement(By.xpath("//div[@class=\"myList___2zpNy\"]/div[27]/div[2]/div/div/textarea"));
                    } catch (Exception e){
                        temperature = driver.findElement(By.xpath("//div[@class=\"myList___2zpNy\"]/div[29]/div[2]/div/div/textarea"));
                    }
                    //体温清除旧数据
                    temperature.sendKeys(Keys.CONTROL, "a");
                    temperature.sendKeys(Keys.DELETE);
                    //体温赋随机值 36.0~37.0
                    temperature.sendKeys(String.valueOf(generateRandomTempature()));
                    WebElement element = driver.findElement(By.linkText("提 交"));
                    element.click();
                    //微信通知
                    notifyByServer(user.getSendKey() , successMessage);
                } catch (Exception e){
                    log.error("账号：" + currnentUser + "打卡异常" , e);
                } finally {
                    //清除cookie，多账号登录
                    driver.manage().deleteAllCookies();
                    driver.close();
                }
            }
        }
    }

    /**
     * 生成随机体温
     * @return double，36.0~37.0
     */
    public double generateRandomTempature(){
        return Math.random() + 36;
    }

    /**
     * 打卡结果进行微信通知，非必须
     * @param sendKey
     */
    public void notifyByServer(String sendKey , String message){
        String url = "https://sctapi.ftqq.com/" + sendKey + ".send?title="+ message;
        HttpUtil.createPost(url).execute();
    }

}

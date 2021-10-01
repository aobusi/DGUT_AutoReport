package com.example.demo.scheduled;

import cn.hutool.core.math.MathUtil;
import cn.hutool.http.HttpUtil;
import com.example.demo.scheduled.entity.Users;
import com.example.demo.scheduled.mapper.UsersMapper;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * Create By aobs
 * Date 2021/10/1 17:15
 * Description
 */

@Slf4j
@Component
public class AutoSubmitScheduled {

    @Autowired
    private UsersMapper usersMapper;

    /**
     * 每天早 6 点进行打卡
     *     个人部署需要修改的部分：
     *          浏览器驱动文件位置
     *          体温随机模块
     *          是否需要微信通知功能(server酱)
     */
    @Scheduled(cron = "0 0 6 * * ? ")
    public void autoScheduled(){
        List<Users> users = usersMapper.selectUsers();
        if (!users.isEmpty()){
            try {
                for (Users user : users) {
                    log.info("开始给账号 " + 1 + " 打卡");
                    //打卡地址
                    String url = "https://cas.dgut.edu.cn/home/Oauth/getToken/appid/illnessProtectionHome/state/home";
                    //chromedriver本地文件位置
                    // win
                    System.setProperty("webdriver.chrome.driver", "D:/cache/chromedriver.exe");
                    // linux
                    //System.setProperty("webdriver.chrome.driver" , "/usr/local/java/chromedriver");
                    WebDriver driver = new ChromeDriver();
                    driver.get(url);
                    //执行登录
                    WebElement username = driver.findElement(By.id("username"));
                    WebElement password = driver.findElement(By.id("casPassword"));
                    WebElement loginBtn = driver.findElement(By.id("loginBtn"));
                    username.sendKeys(user.getUsername());
                    password.sendKeys(user.getPassword());
                    loginBtn.click();
                    //执行打卡
                    String newUrl = "https://yqfk.dgut.edu.cn/main";
                    driver.get(newUrl);
                    //体温赋随机值
                    WebElement temperature = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/form/div/div[27]/div[2]/div/div/textarea"));
                    temperature.sendKeys(String.valueOf(generateRandomTempature()));
                    WebElement element = driver.findElement(By.linkText("提 交"));
                    element.click();
                    //微信通知
                    notifyByServer(user.getSendKey());
                    //关闭
                    driver.close();
                }
            } catch (Exception e){
                log.error("daka fail" , e);
            }
        }
    }

    public double generateRandomTempature(){
        return Math.random() + 36;
    }

    public void notifyByServer(String sendKey){
        String url = "https://sctapi.ftqq.com/" + sendKey + ".send?title="+"今日打卡成功";
        HttpUtil.createPost(url).execute();
    }

}

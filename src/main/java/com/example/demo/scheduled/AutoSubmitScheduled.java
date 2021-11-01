package com.example.demo.scheduled;

import cn.hutool.core.math.MathUtil;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    private String successMessage = "今日打卡成功";
    private String failMessage = "打卡异常";

    @Autowired
    private UsersMapper usersMapper;

    /**
     * 每天早 6 点进行打卡
     *     个人部署需要修改的部分：
     *          浏览器驱动文件位置
     *          体温随机数
     *          微信通知(server酱)
     *      由于对异常类型没做统计，所以用了分开的try来区分
     */
    //@Scheduled(cron = "0 26 8 * * ? ")
    @Scheduled(cron = "0 3 0 * * ? ")
    public void autoScheduled(){
        Date date = new Date();
        System.out.println("today : " + date);
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

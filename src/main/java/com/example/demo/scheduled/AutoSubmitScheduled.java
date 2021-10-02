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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
     */
    @Scheduled(cron = "0 0 6 * * ? ")
    public void autoScheduled(){
        List<Users> users = usersMapper.selectUsers();
        if (!users.isEmpty()){
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
            for (Users user : users) {
                //执行登录
                WebElement username = driver.findElement(By.id("username"));
                WebElement password = driver.findElement(By.id("casPassword"));
                WebElement loginBtn = driver.findElement(By.id("loginBtn"));
                username.sendKeys(user.getUsername());
                password.sendKeys(user.getPassword());
                try {
                    loginBtn.click();
                } catch (Exception e){
                    notifyByServer(user.getSendKey() , failMessage);
                    log.error("登录失败：" + user.getUsername() , e);
                }
                //执行打卡
                String newUrl = "https://yqfk.dgut.edu.cn/main";
                driver.get(newUrl);
                //线程暂停，否则速度过快，下面会获取不到 element，时间无所谓，至少要保证页面接收过来
                try {
                    Thread.sleep(3000);
                } catch (Exception e){
                    notifyByServer(user.getSendKey() , failMessage);
                    log.error("线程休眠异常" , e);
                }
                //体温赋随机值
                WebElement temperature = driver.findElement(By.xpath("//div[@class=\"myList___2zpNy\"]/div[27]/div[2]/div/div/textarea"));
                //清除旧数据
                //temperature.clear(); 此方法不知为何无效
                temperature.sendKeys(Keys.CONTROL, "a");
                temperature.sendKeys(Keys.DELETE);
                //体温赋随机值 36.0~37.0
                temperature.sendKeys(String.valueOf(generateRandomTempature()));
                WebElement element = driver.findElement(By.linkText("提 交"));
                try {
                    element.click();
                } catch (Exception e){
                    notifyByServer(user.getSendKey() , failMessage);
                    log.error("提交失败：" + user.getUsername() , e);
                }
                //微信通知
                try {
                    notifyByServer(user.getSendKey() , successMessage);
                } catch (Exception e){
                    log.error("微信通知失败：" + user.getUsername() , e);
                }
                //关闭
                driver.close();
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

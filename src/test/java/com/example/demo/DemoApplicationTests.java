package com.example.demo;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.example.demo.scheduled.entity.Users;
import com.example.demo.scheduled.mapper.UsersMapper;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.HttpCookie;
import java.util.List;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    private UsersMapper usersMapper;

    @Test
    void testMain() {
        String url = "https://cas.dgut.edu.cn/home/Oauth/getToken/appid/illnessProtectionHome/state/home";
        System.setProperty("webdriver.chrome.driver", "D:/cache/chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.get(url);
        String title = driver.getTitle();
        System.out.printf(title);

        WebElement username = driver.findElement(By.id("username"));
        WebElement password = driver.findElement(By.id("casPassword"));
        WebElement loginBtn = driver.findElement(By.id("loginBtn"));
        username.sendKeys("2112115018");
        password.sendKeys("xxx");
        loginBtn.click();
        String newUrl = "https://yqfk.dgut.edu.cn/main";
        driver.get(newUrl);
        WebElement element = driver.findElement(By.linkText("提 交"));
        //WebElement submit = driver.findElement(By.className("am-button am-button-primary"));
        element.click();
        driver.close();
    }

    @Test
    void testSpring(){
        List<Users> users = usersMapper.selectUsers();
        System.out.println("users = " + users);
    }

}

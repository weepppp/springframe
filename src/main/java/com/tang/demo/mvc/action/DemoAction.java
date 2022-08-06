package com.tang.demo.mvc.action;
import com.tang.demo.service.IDemoService;
import com.tang.mvcframework.annotation.Autowired;
import com.tang.mvcframework.annotation.Controller;
import com.tang.mvcframework.annotation.RequestMapping;
import com.tang.mvcframework.annotation.RequestParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author weepppp 2022/8/6 8:46
 * 配置请求入口
 **/
@Controller
@RequestMapping("/demo")
public class DemoAction {

    @Autowired
    private IDemoService demoService;

    @RequestMapping("/query")
    public void query(HttpServletRequest req, HttpServletResponse resp,
                      @RequestParam("name") String name){
        String result = demoService.get(name);
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/add")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @RequestParam("a") Integer a, @RequestParam("b") Integer b){
        try {
            resp.getWriter().write(a + "+" + b + "=" + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/remove")
    public String  remove(@RequestParam("id") Integer id){
        return "" + id;
    }

}

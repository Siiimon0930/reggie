package com.cf.reggie.controller;

import com.cf.reggie.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

@RestController
@RequestMapping("/common")
public class CommonController {

    // 图片文件夹路径
    @Value("${reggie.path}")
    private String basePath;

    /**
     * decription: 文件上传
     * @param file
     * @return com.cf.reggie.common.R<java.lang.String>
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        String originName = file.getOriginalFilename();
        // 获取文件后缀
        String suffix = originName.substring(originName.lastIndexOf("."));
        // 使用uuid生成新文件名
        String fileName = UUID.randomUUID().toString() + suffix;

        // 判断路径是否存在，不存在则新建
        File dir = new File(basePath);
        if(!dir.exists()){
            dir.mkdir();
        }

        // 把临时文件转存到本地
        try {
            file.transferTo(new File(basePath + fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 把文件名传回浏览器，用于回显
        return R.success(fileName);
    }

    /**
     * decription: 下载图片
     * @param name
     * @param response
     * @return void
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            // 文件输入流
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));

            // 输出流
            OutputStream outputStream = response.getOutputStream();

            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            //关闭资源
            outputStream.close();
            fileInputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

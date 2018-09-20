package com.coupon.user.controller;

import com.coupon.user.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <h1>PassTemplate Token Upload</h1>
 */
@Slf4j
@Controller
public class TokenUploadController {

    /**
     * redis 客户端
     */
    private StringRedisTemplate redisTemplate;

    @Autowired
    public TokenUploadController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 134f2d2a1e78e985805c7bc5ccde2570
    @GetMapping("/upload")
    public String upload() {
        return "upload";
    }

    @PostMapping("/token")
    public String tokenFileUpload(@RequestParam("merchantsId") String merchantsId,
                                  @RequestParam("passTemplateId") String passTemplateId,
                                  @RequestParam("file") MultipartFile file,
                                  RedirectAttributes redirectAttributes) {

        //  这里只做是否为空的校验，实际上会复杂很多
        if (passTemplateId == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message",
                    "passTemplateId is null or file is empty.");
        }

        try {
            File cur = new File(Constants.TOKEN_DIR + merchantsId);
            if (!cur.exists()) {
                log.info("Create File: {}", cur.mkdir());
            }

            Path path = Paths.get(Constants.TOKEN_DIR, merchantsId, passTemplateId);
            Files.write(path, file.getBytes());

            if (!writeTokenToRedis(path, passTemplateId)) {
                redirectAttributes.addFlashAttribute("message",
                        "write token error");
            } else {
                redirectAttributes.addFlashAttribute("message",
                        "You successfully uploaded" + "'" + file.getOriginalFilename() + "'");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/uploadStatus";
    }

    @GetMapping("/uploadStatus")
    public String uploadStatus() {
        return "uploadStatus";
    }

    /**
     * <h2>将 token 写入</h2>
     * 注意：这里的实现并不适合 redis 集群
     * @param path {@link Path}
     * @param key  redis key
     * @return boolean
     */
    private boolean writeTokenToRedis(Path path, String key) {

        Set<String> tokens;

        try (Stream<String> stream = Files.lines(path)) {
            tokens = stream.collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (!CollectionUtils.isEmpty(tokens)) {
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (String token : tokens) {
                    connection.sAdd(key.getBytes(), token.getBytes());
                }
                return null;
            });
            return true;
        }
        return false;
    }
}

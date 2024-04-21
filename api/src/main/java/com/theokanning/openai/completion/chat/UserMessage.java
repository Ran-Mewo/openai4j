package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * @author LiangTao
 * @date 2024年04月10 10:17
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMessage implements ChatMessage {
    private final String role = ChatMessageRole.USER.value();

    /**
     * 字符串或者数组;
     * String: 一个文本消息;
     * Array: 具有定义类型的内容部分的数组，传入图像时每个部分的类型可以是 text 或 image_url 。您可以通过添加多个image_url内容部分来传递多个图像。仅当使用gpt-4-visual-preview型号时才支持图像输入。
     * eg:
     * curl https://api.openai.com/v1/chat/completions \
     * -H "Content-Type: application/json" \
     * -H "Authorization: Bearer $OPENAI_API_KEY" \
     * -d '{
     * "model": "gpt-4-turbo",
     * "messages": [
     * {
     * "role": "user",
     * "content": [
     * {
     * "type": "text",
     * "text": "What'\''s in this image?"
     * },
     * {
     * "type": "image_url",
     * "image_url": {
     * "url": "https://upload.wikimedia.org/wikipedia/commons/thumb/d/dd/Gfp-wisconsin-madison-the-nature-boardwalk.jpg/2560px-Gfp-wisconsin-madison-the-nature-boardwalk.jpg"
     * }
     * }
     * ]
     * }
     * ],
     * "max_tokens": 300
     * }'
     */
    @JsonDeserialize(using = ContentDeserializer.class)
    @JsonSerialize(using = ContentSerializer.class)
    private Object content;

    //An optional name for the participant. Provides the model information to differentiate between participants of the same role.
    private String name;

    public UserMessage(Object content) {
        this.content = content;
    }

    @Override
    @JsonIgnore
    public String getTextContent() {
        if (content instanceof String) {
            return (String) content;
        }
        if (content instanceof Collection) {
            Collection<ImageContent> collection = (Collection<ImageContent>) content;
            for (ImageContent item : collection) {
                if (item.getType().equals("text")) return item.getText();
            }
        }
        return null;
    }

    /**
     * 构件一个图片识别请求消息,支持多个图片
     *
     * @param text      query text
     * @param imageUrls image urls
     * @return com.theokanning.openai.completion.chat.UserMessage
     * @author liangtao, Ran
     * @date 2024/4/12
     **/
    @JsonIgnore
    public static UserMessage buildImageMessage(String text, String... imageUrls) {
        List<ImageContent> imageContents = new ArrayList<>(imageUrls.length + 1);
        imageContents.add(new ImageContent(text));
        for (String url : imageUrls) {
            imageContents.add(new ImageContent(new ImageUrl(url)));
        }
        return new UserMessage(imageContents);
    }
}


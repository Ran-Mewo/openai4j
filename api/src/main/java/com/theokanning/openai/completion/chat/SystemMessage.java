package com.theokanning.openai.completion.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * system message
 *
 * @author LiangTao
 * @date 2024年04月10 10:13
 **/

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemMessage implements ChatMessage {
    private final String role = ChatMessageRole.SYSTEM.value();


    // content should always exist in the call, even if it is null
    @JsonInclude
    @JsonDeserialize(using = ContentDeserializer.class)
    @JsonSerialize(using = ContentSerializer.class)
    private Object content;

    //An optional name for the participant. Provides the model information to differentiate between participants of the same role.
    private String name;


    public SystemMessage(Object content) {
        this.content = content;
        contentTypeCheck();
    }

    public void setContent(Object content) {
        this.content = content;
        contentTypeCheck();
    }

    private void contentTypeCheck() {
        if (content instanceof String) {
            return;
        }
        if (content instanceof Collection) {
            Collection collection = (Collection) content;
            collection.forEach(item -> {
                if (!(item instanceof ImageContent)) {
                    throw new IllegalArgumentException("content must be a string or a collection of ImageContent");
                }
            });
            return;
        }
        throw new IllegalArgumentException("content must be a string or a collection of ImageContent");
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
    public static SystemMessage buildImageMessage(String text, String... imageUrls) {
        List<ImageContent> imageContents = new ArrayList<>(imageUrls.length + 1);
        imageContents.add(new ImageContent(text));
        for (String url : imageUrls) {
            imageContents.add(new ImageContent(new ImageUrl(url)));
        }
        return new SystemMessage(imageContents);
    }
}

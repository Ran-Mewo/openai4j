package example;

import com.theokanning.openai.assistants.message.content.ImageFile;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.utils.TikTokensUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class TikTokensExample {

    public static void main(String... args) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage("Hello OpenAI 1."));
        messages.add(new SystemMessage("Hello OpenAI 2.   "));
        messages.add(new UserMessage(Arrays.asList(new ImageContent("text", "", new ImageUrl("dddd"), new ImageFile()))));
        int tokens_1 = TikTokensUtil.tokens(TikTokensUtil.ModelEnum.GPT_3_5_TURBO.getName(), messages);
        int tokens_2 = TikTokensUtil.tokens(TikTokensUtil.ModelEnum.GPT_3_5_TURBO.getName(), "Hello OpenAI 1.");
        int tokens_3 = TikTokensUtil.tokens(TikTokensUtil.ModelEnum.GPT_4_TURBO.getName(), messages);
    }

}

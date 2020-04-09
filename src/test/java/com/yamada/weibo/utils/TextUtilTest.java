package com.yamada.weibo.utils;

import com.yamada.weibo.vo.TextVO;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TextUtilTest {

    @Test
    public void test() {
        String content = "【#美国社交隔离措施将延长# 特朗普：若死亡人数控制在10-20万，我们的工作就算做得很好了】#特朗普称死亡人数降到10万算不错了# 美国白宫应对疫情协调员伯克斯表示，根据模型预测，如果不进行管控，将会有160万至220万人死亡。对此，美国总统特朗普表示，将死亡人数控制在10到20万人就说明采取的措施是有效的。同时，特朗普表示，根据疫情模型估算，两周后美国的新冠肺炎死亡人数将达到峰值，为了防止疫情扩散，他宣布将“社交隔离”措施延长至4月30日。美国首席传染病专家、美国国家过敏和传染病研究所所长福奇在当天的简报会上表示，采取“社交隔离”措施将有助于减少死亡人数。 此前，特朗普3月26日曾提议在4月12日“复活节”的时候复工，但于3月29日，他改变了说法，表示希望在6月1号复工。特朗普说，如有必要，他将再度接受新冠肺炎检测";
        List<TextVO> textVOList = TextUtil.convertToTextVO(content);
        System.out.println(textVOList);
    }
}

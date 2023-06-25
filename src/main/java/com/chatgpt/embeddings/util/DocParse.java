package com.chatgpt.embeddings.util;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class DocParse {
    private static final int MAX_LENGTH = 200;
    private static final int MIN_LENGTH = 3;

    public List<String> pdfParse(InputStream inputStream) throws IOException {
        // 打开 PDF 文件
        PDDocument document = PDDocument.load(inputStream);
        // 创建 PDFTextStripper 对象
        PDFTextStripper stripper = new PDFTextStripper();
        // 获取文本内容
        String text = stripper.getText(document);
        //过滤字符
        text = text.replaceAll("\\s", " ").replaceAll("(\\r\\n|\\r|\\n|\\n\\r)"," ");
        String[] sentence = text.split("。");
        List<String> ans = new ArrayList<>();
        for (String s : sentence) {
            if (s.length() > MAX_LENGTH) {
                for (int index = 0; index < sentence.length; index = (index + 1) * MAX_LENGTH) {
                    String substring = s.substring(index, MAX_LENGTH);
                    if(substring.length() < 5) {
                        continue;
                    }
                    ans.add(substring);
                }
            } else {
                ans.add(s);
            }
        }
        // 关闭文档
        document.close();
        return ans;
    }

    public List<String> txtParse(InputStream inputStream) throws IOException {
        List<String> textList = new ArrayList<>();
        textList = IoUtil.readLines(inputStream, Charset.forName("utf-8"), textList);

        List<String> ans = new ArrayList<>();
        for (String text:textList) {
            text = text.replaceAll("\\s", " ").replaceAll("(\\r\\n|\\r|\\n|\\n\\r)"," ");
            if (StrUtil.isBlank(text) || text.length() < MIN_LENGTH) {
                continue;
            }
            ans.add(text);
        }
        return ans;
    }
}

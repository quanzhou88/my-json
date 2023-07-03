package com.qz.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author qz
 * @date 2023年06月28日 9:21
 */
public class MyToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 创建输入框
        JTextArea inputTextArea = new JTextArea();
        inputTextArea.addFocusListener(new JTextAreaHintListener(inputTextArea,"请输入JSON"));
        //自动换行
        // inputTextArea.setLineWrap(true);
        // inputTextArea.setWrapStyleWord(true);
        // 创建 JScrollPane
        JScrollPane scrollPane = new JScrollPane(inputTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
       // scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        // 创建格式化按钮
        JButton formatButton = new JButton("格式化");
        formatButton.addActionListener(e -> {
            String input = inputTextArea.getText().trim();
            if (input.isEmpty() || "请输入JSON".equals(input)) {
                JOptionPane.showMessageDialog(null, "请输入JSON");
            } else {
                try {
                    String formattedText = formatInput(input);
                    inputTextArea.setText(formattedText);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Invalid JSON format");
                }
            }
        });

        // 创建压缩按钮
        JButton compressButton = new JButton("压缩");
        compressButton.addActionListener(e -> {
            String input = inputTextArea.getText();
            if (input.isEmpty() ||"请输入JSON".equals(input) ) {
                JOptionPane.showMessageDialog(null, "请输入JSON");
            } else {
                try {
                    String compressedText = compressInput(input);
                    inputTextArea.setText(compressedText);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Invalid JSON format");
                }
            }
        });
        // 创建一个面板，将输入框和两个按钮添加到面板中
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER); // 将 JScrollPane 添加到面板中
      //  panel.add(inputTextArea, BorderLayout.NORTH);
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(formatButton);
        buttonPanel.add(compressButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        // 将面板添加到工具窗口中
        toolWindow.getComponent().add(panel).addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                //  inputTextArea.setPreferredSize(new Dimension(toolWindow.getComponent().getWidth() / 2, panel.getPreferredSize().height));
                //  inputTextArea.setPreferredSize(new Dimension(panel.getPreferredSize().width, panel.getPreferredSize().height/2));

            }
        });


        // 创建下部分面板
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JTextField inputField = new JTextField(10);
        String[] options = {"毫秒", "秒"};
        JComboBox<String> comboBox = new JComboBox<>(options);
        JTextField outputField = new JTextField(12);
        outputField.setEditable(false); // 设置outputField为不可编辑
        JButton convertButton = new JButton("转换");
        bottomPanel.add(inputField);
        bottomPanel.add(comboBox);
        bottomPanel.add(outputField);
        bottomPanel.add(convertButton);
        convertButton.addActionListener(e -> {
            String input = inputField.getText();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(null, "请输入时间");
            } else {
                String compressedText = convert(input, (String) comboBox.getSelectedItem());
                outputField.setText(compressedText);
            }
        });
        // 将上下两部分面板添加到工具窗口中
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panel, bottomPanel);
        splitPane.setResizeWeight(0.5); // 设置分割比例为50%
        toolWindow.getComponent().add(splitPane);
    }

    private String formatInput(String input) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = gson.fromJson(input, JsonElement.class);
        return gson.toJson(jsonElement);
    }

    private String compressInput(String input) {
        // 在这里编写压缩的逻辑
        // 创建Gson对象
        Gson gson = new GsonBuilder().create();
        // 将JSON字符串解析为对象
        Object jsonObject = gson.fromJson(input, Object.class);
        // 将对象转换为压缩后的JSON字符串
        String compressedJson = gson.toJson(jsonObject);
        return compressedJson;
    }

    private String convert(String input, String unit) {
        long time = Long.parseLong(input);
        // 根据单位参数判断时间单位（秒或毫秒）
        if ("秒".equals(unit)) {
            // 将秒转换为毫秒
            time *= 1000;
        }
        // 创建日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 将时间转换为日期对象
        Date date = new Date(time);
        // 格式化日期对象为指定格式的字符串
        String formattedTime = dateFormat.format(date);
        // 返回格式化后的时间字符串
        return formattedTime;
    }
}

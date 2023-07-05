package com.qz.json;

import com.google.gson.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * @author qz
 * @date 2023年06月28日 9:21
 */
public class MyToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 创建输入框
        JTextArea inputTextArea = new JTextArea();
        inputTextArea.addFocusListener(new JTextAreaHintListener(inputTextArea, "请输入JSON"));
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
            if (input.isEmpty() || "请输入JSON".equals(input)) {
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
        // 创建解析按钮
        JButton analysisButton = new JButton("解析");
        analysisButton.addActionListener(e -> {
            String input = inputTextArea.getText();
            if (input.isEmpty() || "请输入JSON".equals(input)) {
                JOptionPane.showMessageDialog(null, "请输入JSON");
            } else {
                try {

                   /* // 当点击解析按钮时，弹出一个确认框，确认框中有一个叫保存地址的输入框
                    String savePath = JOptionPane.showInputDialog("保存地址");
                    // 检查用户是否输入了保存路径
                    if (savePath != null && !savePath.trim().isEmpty()) {
                        // 在这里执行您的代码，例如保存输入的 JSON 到指定的路径
                        // saveJson(input, savePath);
                    } */
                    // 当点击解析按钮时，弹出一个确认框，确认框中有一个叫保存地址的输入框
                    JFileChooser fileChooser = new JFileChooser();
                    int result = fileChooser.showSaveDialog(null);
                    // 检查用户是否选择了保存路径
                    if (result == JFileChooser.APPROVE_OPTION) {
                        String savePath = fileChooser.getSelectedFile().getAbsolutePath();
                        // 在这里执行您的代码，例如保存输入的 JSON 到指定的路径
                        saveJson(input, savePath);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
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
        buttonPanel.add(analysisButton);
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

    private void saveJson(String input, String savePath) {
        File file = new File(savePath);
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
            fileName = fileName.substring(0, dotIndex);
        }
        String path = file.getParent();

        JsonElement jsonElement = JsonParser.parseString(input);
        processJsonElement(jsonElement, path, fileName, "");

    }


    private void processJsonElement(JsonElement jsonElement, String path, String fileName, String parentFieldName) {
        JsonObject jsonObject = null;
        if (jsonElement.isJsonObject()) {
            jsonObject = jsonElement.getAsJsonObject();
        } else if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            if (jsonArray.size() > 0) {
                JsonElement arrayElement = jsonArray.get(0);
                jsonObject = arrayElement.getAsJsonObject();

            }
        }

        if (Objects.nonNull(jsonObject)) {
            StringWriter sw = new StringWriter();
            TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(fileName)
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(Serializable.class);

            try {
                for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    String fieldName = entry.getKey();
                    JsonElement fieldValue = entry.getValue();
                    String fieldType = getFieldType(fieldValue);
                    if ("Object".equals(fieldType) || "Array".equals(fieldType)) {
                        String fullFieldName = parentFieldName.isEmpty() ? fieldName : parentFieldName + "." + fieldName;
                        processJsonElement(fieldValue, path, replaceFirstCharWithUpperCase(fieldName), fullFieldName);
                        FieldSpec.Builder fieldBuilder = FieldSpec.builder(ClassName.get(("Array".equals(fieldType) ? List.class : Object.class)), fieldName, Modifier.PRIVATE);
                        typeBuilder.addField(fieldBuilder.build());
                    } else {
                        FieldSpec.Builder fieldBuilder = FieldSpec.builder(getPropertyType(fieldType), fieldName, Modifier.PRIVATE);
                        typeBuilder.addField(fieldBuilder.build());
                    }

                }
                JavaFile.builder("", typeBuilder.build())
                        .indent("    ")
                        .build()
                        .writeTo(sw);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Path of = Path.of(path + File.separator + fileName + ".java");
                Files.createDirectories(Path.of(path));
                if (!Files.exists(of)) {
                    Files.createFile(of);
                }
                Files.writeString(of, sw.toString(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public ClassName getPropertyType(String fieldType) {
        switch (fieldType) {
            case "JsonPrimitive":
                return ClassName.get(String.class);
            case "String":
                return ClassName.get(String.class);
            case "Integer":
                return ClassName.get(Integer.class);
            case "Long":
                return ClassName.get(Long.class);
            case "Boolean":
                return ClassName.get(Boolean.class);
            case "BigDecimal":
                return ClassName.get(BigDecimal.class);
            case "Double":
                return ClassName.get(Double.class);
            case "Float":
                return ClassName.get(Float.class);


        }
        return ClassName.get(String.class);
    }

    private static String getFieldType(JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive asJsonPrimitive = jsonElement.getAsJsonPrimitive();
            if (asJsonPrimitive.isString()) {
                return "String";
            } else if (asJsonPrimitive.isBoolean()) {
                return "Boolean";
            } else if (asJsonPrimitive.isNumber()) {
                Number asNumber = asJsonPrimitive.getAsNumber();
                String string = asNumber.toString();
                if (string.indexOf(".") > -1) {
                    return "BigDecimal";
                } else {
                    if (asNumber.longValue() > Integer.MAX_VALUE) {
                        return "Long";
                    } else {
                        return "Integer";
                    }
                }
            }
            return "String";
        } else if (jsonElement.isJsonObject()) {
            return "Object";
        } else if (jsonElement.isJsonArray()) {
            return "Array";
        } else if (jsonElement.isJsonNull()) {
            return "String";
        } else {
            return "Unknown";
        }
    }

    public String replaceFirstCharWithUpperCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        char firstChar = Character.toUpperCase(input.charAt(0));
        return firstChar + input.substring(1);
    }
}

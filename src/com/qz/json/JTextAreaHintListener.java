package com.qz.json;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * @author qz
 * @date 2023年07月03日 9:28
 */
public class JTextAreaHintListener implements FocusListener {

    private String hintText;
    private JTextArea inputTextArea;

    public JTextAreaHintListener(JTextArea inputTextArea, String hintText) {
        this.inputTextArea = inputTextArea;
        this.hintText = hintText;
        //默认直接显示
        inputTextArea.setText(hintText);
        inputTextArea.setForeground(Color.GRAY);
    }

    @Override
    public void focusGained(FocusEvent e) {
        //获取焦点时，清空提示内容
        String temp = inputTextArea.getText();
        if (temp.equals(hintText)) {
            inputTextArea.setText("");
            inputTextArea.setForeground(Color.LIGHT_GRAY);
        }

    }

    @Override
    public void focusLost(FocusEvent e) {
        //失去焦点时，没有输入内容，显示提示内容
        String temp = inputTextArea.getText();
        if ("".equals(temp)) {
            inputTextArea.setForeground(Color.GRAY);
            inputTextArea.setText(hintText);
        }

    }
}

package com.benatt.passwordsmanager.utils;

import org.junit.Test;

/**
 * @author ben-mathu
 * @time 20/12/2021
 */
public class DecryptorTest {
    @Test
    public void test_password_decryption() {
        String plainText = Decryptor.decryptPassword("XRgdKEW7wVB14Zqr" +
                ":refuPijRGgn5miiP+gOszYz/IzmXu4yqA1A0EA==", "Example PassPhrase");

        System.out.println("test_password_decryption: " + plainText);
    }
}